package com.example.paranoia.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object FirestoreService {
    private const val TAG = "FirestoreService"

    private const val PROJECT_ID = "spring-octagon-mcwq9"
    private const val DATABASE_ID = "ai-studio-paranoiagametest-17dd4ca2-4dcd-4f9b-a455-c13e462730ab"
    private const val API_KEY = "AIzaSyDS_8h23r7tJsbJKyH0jglq04mBdbk__W4"

    private const val BASE_URL =
        "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/$DATABASE_ID/documents"

    var mySessionId: String = UUID.randomUUID().toString().replace("-", "").take(24)
        private set

    fun resetSessionId(): String {
        mySessionId = UUID.randomUUID().toString().replace("-", "").take(24)
        return mySessionId
    }

    /**
     * Creates a new GameRoom in Firestore with status="lobby" and creates the host's RoomPlayer record.
     */
    suspend fun createRoom(
        roomCode: String,
        hostName: String,
        selectedCategories: Set<QuestionCategory>,
        questions: List<String>,
        maxRounds: Int = 10
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val code = roomCode.trim().uppercase()
            val categoriesMap = JSONObject().apply {
                put("icebreaker", JSONObject().put("booleanValue", selectedCategories.contains(QuestionCategory.ICEBREAKER)))
                put("funny", JSONObject().put("booleanValue", selectedCategories.contains(QuestionCategory.FUNNY)))
                put("spicy", JSONObject().put("booleanValue", selectedCategories.contains(QuestionCategory.SPICY)))
                put("extreme", JSONObject().put("booleanValue", selectedCategories.contains(QuestionCategory.EXTREME)))
            }

            val questionsArray = JSONArray()
            questions.forEach { q ->
                questionsArray.put(JSONObject().put("stringValue", q))
            }

            val playersArray = JSONArray().apply {
                put(JSONObject().put("stringValue", hostName))
            }

            val fields = JSONObject().apply {
                put("room_code", JSONObject().put("stringValue", code))
                put("status", JSONObject().put("stringValue", "lobby"))
                put("phase", JSONObject().put("stringValue", "question"))
                put("host_session_id", JSONObject().put("stringValue", mySessionId))
                put("host_name", JSONObject().put("stringValue", hostName))
                put("categories", JSONObject().put("mapValue", JSONObject().put("fields", categoriesMap)))
                put("questions", JSONObject().put("arrayValue", JSONObject().put("values", questionsArray)))
                put("players", JSONObject().put("arrayValue", JSONObject().put("values", playersArray)))
                put("round", JSONObject().put("integerValue", "0"))
                put("asker_idx", JSONObject().put("integerValue", "0"))
                put("current_question", JSONObject().put("stringValue", ""))
                put("coin_result", JSONObject().put("stringValue", ""))
                put("max_rounds", JSONObject().put("integerValue", maxRounds.toString()))
            }

            val body = JSONObject().put("fields", fields).toString()
            val url = "$BASE_URL/game_rooms?documentId=$code&key=$API_KEY"

            val res = sendHttpRequest("POST", url, body)
            if (!res.isSuccess) {
                // If it already existed or failed, try PATCH
                val patchUrl = "$BASE_URL/game_rooms/$code?key=$API_KEY"
                val patchRes = sendHttpRequest("PATCH", patchUrl, body)
                if (!patchRes.isSuccess) {
                    return@withContext Result.failure(Exception("Failed to create room: ${patchRes.error}"))
                }
            }

            // Also create RoomPlayer for the host
            val playerId = "${code}_$mySessionId"
            val playerFields = JSONObject().apply {
                put("room_code", JSONObject().put("stringValue", code))
                put("name", JSONObject().put("stringValue", hostName))
                put("session_id", JSONObject().put("stringValue", mySessionId))
                put("order", JSONObject().put("integerValue", "0"))
                put("is_host", JSONObject().put("booleanValue", true))
            }
            val playerBody = JSONObject().put("fields", playerFields).toString()
            val playerUrl = "$BASE_URL/room_players?documentId=$playerId&key=$API_KEY"
            sendHttpRequest("POST", playerUrl, playerBody)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating room", e)
            Result.failure(e)
        }
    }

    /**
     * Joins an existing GameRoom and creates a RoomPlayer record in Firestore.
     */
    suspend fun joinRoom(
        roomCode: String,
        playerName: String
    ): Result<OnlineRoomState> = withContext(Dispatchers.IO) {
        try {
            val code = roomCode.trim().uppercase()
            // 1. Check if room exists
            val roomRes = getRoom(code)
            if (roomRes.isFailure || roomRes.getOrNull() == null) {
                return@withContext Result.failure(Exception("Room $code not found! Please check the code."))
            }

            val room = roomRes.getOrNull()!!
            if (room.status == "ended") {
                return@withContext Result.failure(Exception("This game room has already ended."))
            }

            // 2. Fetch current players in room to determine order
            val players = getRoomPlayers(code)
            val myOrder = players.size
            val isHost = room.hostSessionId == mySessionId

            // 3. Create room_players document
            val playerId = "${code}_$mySessionId"
            val playerFields = JSONObject().apply {
                put("room_code", JSONObject().put("stringValue", code))
                put("name", JSONObject().put("stringValue", playerName))
                put("session_id", JSONObject().put("stringValue", mySessionId))
                put("order", JSONObject().put("integerValue", myOrder.toString()))
                put("is_host", JSONObject().put("booleanValue", isHost))
            }
            val playerBody = JSONObject().put("fields", playerFields).toString()
            val playerUrl = "$BASE_URL/room_players?documentId=$playerId&key=$API_KEY"
            val joinRes = sendHttpRequest("POST", playerUrl, playerBody)
            if (!joinRes.isSuccess) {
                val patchUrl = "$BASE_URL/room_players/$playerId?key=$API_KEY"
                sendHttpRequest("PATCH", patchUrl, playerBody)
            }

            // 4. Return updated room state
            val updatedPlayers = getRoomPlayers(code)
            val updatedRoom = room.copy(
                myName = playerName,
                isHost = isHost,
                players = if (updatedPlayers.isNotEmpty()) updatedPlayers else room.players
            )

            Result.success(updatedRoom)
        } catch (e: Exception) {
            Log.e(TAG, "Error joining room", e)
            Result.failure(e)
        }
    }

    /**
     * Gets the current GameRoom document from Firestore.
     */
    suspend fun getRoom(roomCode: String): Result<OnlineRoomState?> = withContext(Dispatchers.IO) {
        try {
            val code = roomCode.trim().uppercase()
            val url = "$BASE_URL/game_rooms/$code?key=$API_KEY"
            val res = sendHttpRequest("GET", url, null)

            if (!res.isSuccess) {
                if (res.statusCode == 404) return@withContext Result.success(null)
                return@withContext Result.failure(Exception("Error fetching room: ${res.error}"))
            }

            val json = JSONObject(res.body)
            val fields = json.optJSONObject("fields") ?: return@withContext Result.success(null)

            val parsedCode = fields.optJSONObject("room_code")?.optString("stringValue") ?: code
            val status = fields.optJSONObject("status")?.optString("stringValue") ?: "lobby"
            val phase = fields.optJSONObject("phase")?.optString("stringValue") ?: "question"
            val hostSessionId = fields.optJSONObject("host_session_id")?.optString("stringValue") ?: ""
            val hostName = fields.optJSONObject("host_name")?.optString("stringValue") ?: ""
            val round = fields.optJSONObject("round")?.optString("integerValue")?.toIntOrNull() ?: 0
            val askerIdx = fields.optJSONObject("asker_idx")?.optString("integerValue")?.toIntOrNull() ?: 0
            val currentQuestion = fields.optJSONObject("current_question")?.optString("stringValue") ?: ""
            val coinResultStr = fields.optJSONObject("coin_result")?.optString("stringValue") ?: ""
            val maxRounds = fields.optJSONObject("max_rounds")?.optString("integerValue")?.toIntOrNull() ?: 10

            val coinResult = when (coinResultStr.lowercase()) {
                "heads" -> CoinSide.HEADS
                "tails" -> CoinSide.TAILS
                else -> null
            }

            // Categories map
            val categoriesObj = fields.optJSONObject("categories")?.optJSONObject("mapValue")?.optJSONObject("fields")
            val selectedCats = mutableSetOf<QuestionCategory>()
            if (categoriesObj?.optJSONObject("icebreaker")?.optBoolean("booleanValue") == true) selectedCats.add(QuestionCategory.ICEBREAKER)
            if (categoriesObj?.optJSONObject("funny")?.optBoolean("booleanValue") == true) selectedCats.add(QuestionCategory.FUNNY)
            if (categoriesObj?.optJSONObject("spicy")?.optBoolean("booleanValue") == true) selectedCats.add(QuestionCategory.SPICY)
            if (categoriesObj?.optJSONObject("extreme")?.optBoolean("booleanValue") == true) selectedCats.add(QuestionCategory.EXTREME)

            // Players array in room doc
            val playersList = mutableListOf<Player>()
            val playersArr = fields.optJSONObject("players")?.optJSONObject("arrayValue")?.optJSONArray("values")
            if (playersArr != null) {
                for (i in 0 until playersArr.length()) {
                    val pItem = playersArr.optJSONObject(i)
                    val pName = pItem?.optString("stringValue") ?: ""
                    if (pName.isNotBlank()) {
                        playersList.add(
                            Player(
                                id = "room_p_$i",
                                name = pName,
                                order = i,
                                isHost = (i == 0)
                            )
                        )
                    }
                }
            }

            val roomState = OnlineRoomState(
                roomCode = parsedCode,
                hostName = hostName,
                hostSessionId = hostSessionId,
                isHost = (hostSessionId == mySessionId),
                players = playersList,
                status = status,
                phase = phase,
                round = round,
                maxRounds = maxRounds,
                askerIdx = askerIdx,
                currentQuestion = currentQuestion,
                coinResult = coinResult,
                selectedCategories = selectedCats
            )

            Result.success(roomState)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing room document", e)
            Result.failure(e)
        }
    }

    /**
     * Gets all players in a room from the /room_players collection.
     */
    suspend fun getRoomPlayers(roomCode: String): List<Player> = withContext(Dispatchers.IO) {
        try {
            val code = roomCode.trim().uppercase()
            val queryUrl = "$BASE_URL:runQuery?key=$API_KEY"
            val queryBody = JSONObject().apply {
                put("structuredQuery", JSONObject().apply {
                    put("from", JSONArray().put(JSONObject().put("collectionId", "room_players")))
                    put("where", JSONObject().apply {
                        put("fieldFilter", JSONObject().apply {
                            put("field", JSONObject().put("fieldPath", "room_code"))
                            put("op", "EQUAL")
                            put("value", JSONObject().put("stringValue", code))
                        })
                    })
                })
            }.toString()

            val res = sendHttpRequest("POST", queryUrl, queryBody)
            if (!res.isSuccess) return@withContext emptyList()

            val jsonArray = JSONArray(res.body)
            val result = mutableListOf<Player>()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.optJSONObject(i) ?: continue
                val doc = item.optJSONObject("document") ?: continue
                val fields = doc.optJSONObject("fields") ?: continue

                val name = fields.optJSONObject("name")?.optString("stringValue") ?: ""
                val sessionId = fields.optJSONObject("session_id")?.optString("stringValue") ?: ""
                val order = fields.optJSONObject("order")?.optString("integerValue")?.toIntOrNull() ?: i
                val isHost = fields.optJSONObject("is_host")?.optBoolean("booleanValue") ?: false

                if (name.isNotBlank()) {
                    result.add(
                        Player(
                            id = sessionId.ifEmpty { "p_$i" },
                            name = name,
                            order = order,
                            isHost = isHost
                        )
                    )
                }
            }

            result.sortedBy { it.order }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching room players", e)
            emptyList()
        }
    }

    /**
     * Updates the GameRoom document in Firestore.
     */
    suspend fun updateGameRoom(
        roomCode: String,
        hostSessionId: String,
        hostName: String,
        status: String,
        phase: String,
        round: Int,
        askerIdx: Int,
        currentQuestion: String,
        coinResult: String,
        players: List<String>,
        categories: Set<QuestionCategory>,
        questions: List<String>,
        maxRounds: Int = 10
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val code = roomCode.trim().uppercase()
            val categoriesMap = JSONObject().apply {
                put("icebreaker", JSONObject().put("booleanValue", categories.contains(QuestionCategory.ICEBREAKER)))
                put("funny", JSONObject().put("booleanValue", categories.contains(QuestionCategory.FUNNY)))
                put("spicy", JSONObject().put("booleanValue", categories.contains(QuestionCategory.SPICY)))
                put("extreme", JSONObject().put("booleanValue", categories.contains(QuestionCategory.EXTREME)))
            }

            val questionsArray = JSONArray()
            questions.forEach { q ->
                questionsArray.put(JSONObject().put("stringValue", q))
            }

            val playersArray = JSONArray()
            players.forEach { p ->
                playersArray.put(JSONObject().put("stringValue", p))
            }

            val fields = JSONObject().apply {
                put("room_code", JSONObject().put("stringValue", code))
                put("status", JSONObject().put("stringValue", status))
                put("phase", JSONObject().put("stringValue", phase))
                put("host_session_id", JSONObject().put("stringValue", hostSessionId))
                put("host_name", JSONObject().put("stringValue", hostName))
                put("categories", JSONObject().put("mapValue", JSONObject().put("fields", categoriesMap)))
                put("questions", JSONObject().put("arrayValue", JSONObject().put("values", questionsArray)))
                put("players", JSONObject().put("arrayValue", JSONObject().put("values", playersArray)))
                put("round", JSONObject().put("integerValue", round.toString()))
                put("asker_idx", JSONObject().put("integerValue", askerIdx.toString()))
                put("current_question", JSONObject().put("stringValue", currentQuestion))
                put("coin_result", JSONObject().put("stringValue", coinResult))
                put("max_rounds", JSONObject().put("integerValue", maxRounds.toString()))
            }

            val body = JSONObject().put("fields", fields).toString()
            val patchUrl = "$BASE_URL/game_rooms/$code?key=$API_KEY"

            val res = sendHttpRequest("PATCH", patchUrl, body)
            if (res.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update game room: ${res.error}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating game room", e)
            Result.failure(e)
        }
    }

    private data class HttpResponse(
        val isSuccess: Boolean,
        val statusCode: Int,
        val body: String,
        val error: String? = null
    )

    private fun sendHttpRequest(
        method: String,
        urlString: String,
        jsonBody: String?
    ): HttpResponse {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                if (jsonBody != null && (method == "POST" || method == "PATCH" || method == "PUT")) {
                    doOutput = true
                }
            }

            if (jsonBody != null && connection.doOutput) {
                OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                    writer.write(jsonBody)
                    writer.flush()
                }
            }

            val statusCode = connection.responseCode
            val isSuccess = statusCode in 200..299

            val inputStream = if (isSuccess) connection.inputStream else connection.errorStream
            val responseText = inputStream?.let { stream ->
                BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
                    reader.readText()
                }
            } ?: ""

            HttpResponse(
                isSuccess = isSuccess,
                statusCode = statusCode,
                body = responseText,
                error = if (!isSuccess) responseText else null
            )
        } catch (e: Exception) {
            Log.e(TAG, "HTTP $method failed on $urlString", e)
            HttpResponse(
                isSuccess = false,
                statusCode = -1,
                body = "",
                error = e.localizedMessage
            )
        } finally {
            connection?.disconnect()
        }
    }
}
