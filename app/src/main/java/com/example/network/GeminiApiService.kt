package com.example.network

import android.graphics.Bitmap
import android.util.Base64
import com.example.model.PhotoVerificationResult
import com.example.model.Quest
import com.example.model.QuestRequest
import com.example.model.VerificationStatus
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateQuest(apiKey: String, questRequest: QuestRequest): Quest = withContext(Dispatchers.IO) {
        val modelsToTry = listOf("gemini-3.5-flash", "gemini-flash-latest")
        var lastException: Exception? = null

        val systemPrompt = """
            You are HẻmQuest AI, an expert cultural tour curator in Vietnam specializing in low-impact walking quests through authentic Saigon alleyways (hẻm).
            Generate a 3-to-4 stop walking quest (maximum 4 stops) in ${questRequest.startingLocationName} (Coordinates: ${questRequest.latitude}, ${questRequest.longitude}).
            
            CRITICAL DESIGN DIRECTIVE - FOCUS ON ALLEYS (HẺM):
            Prioritize hidden alleyways (hẻm), quiet residential alley corridors, alley craft workshops, alley net-filter cafes (cà phê hẻm), secret alley bunkers, alley shrines, and historic alleyway apartments OVER major commercial tourist attractions (like Opera House, Post Office, or large plazas). Users specifically want to discover the intimate, authentic, local life hidden inside Saigon's legendary alley network.
            
            USER PREFERENCES:
            - Target Duration: ${questRequest.durationMinutes} minutes
            - Interests: ${questRequest.interests.joinToString(", ")}
            - Notes: ${questRequest.freeTextNotes}
            - Language: ${
                when (questRequest.language) {
                    "vi" -> "Vietnamese (Tiếng Việt)"
                    "zh" -> "Chinese (Simplified 中文)"
                    "ja" -> "Japanese (日本語)"
                    "ko" -> "Korean (한국어)"
                    else -> "English"
                }
            }. ALL fields in the response JSON (titles, stop names, categories, whySelected, stories, challenge prompts, green score factors) MUST be written in this requested language.
            
            REQUIREMENTS:
            1. Every stop MUST be real, physically verifiable, and located in central Ho Chi Minh City / Saigon alleyways (e.g., Phường Diên Hồng, Phường Sài Gòn, Phường Cầu Ông Lãnh, Phường Tân Định, Phường Bàn Cờ, Phường Xuân Hòa, Phường Chợ Lớn, Phường Chợ Quán, Phường Hòa Bình, Phường Minh Phụng, Phường Bình Thới, Phường Thanh Đa / Bán đảo Thanh Đa). Use new merged ward names (Phường) and avoid using "Quận" / "District" labels.
            2. Real accurate coordinates (latitude, longitude) for each stop strictly matching the specified neighborhood. IMPORTANT GEOGRAPHY NOTES:
               - For Cư Xá Thanh Đa / Bán đảo Thanh Đa: MUST use coordinates on Thanh Đa peninsula across Cầu Kinh (latitude 10.820 - 10.835, longitude 106.720 - 106.735, e.g. Cư xá Lô S ~ 10.8258, 106.7242). Do NOT place Thanh Đa stops around Hàng Xanh / Điện Biên Phủ.
               - For Phường Sài Gòn / Bến Nghé: Latitude ~10.776, Longitude ~106.701.
               - For Phường Chợ Lớn / Chợ Quán: Latitude ~10.753, Longitude ~106.660.
               - For Phường Bàn Cờ: Latitude ~10.776, Longitude ~106.685.
               - For Phường Hòa Bình (Làng Lồng Đèn): Latitude ~10.764, Longitude ~106.649.
               - For Phường Diên Hồng (ĐH Bách Khoa / Tô Hiến Thành): Latitude ~10.774, Longitude ~106.660.
            3. Story MUST be educational, culturally rich, and constrained to verified facts.
            4. Challenge MUST be an observation or photo challenge that can be done from the outside sidewalk.
            5. Calculate a Green Score (0-100) based on walkability, compactness, and local business inclusion.
            
            Return ONLY a JSON object strictly matching this schema:
            {
              "id": "quest_district_theme_timestamp",
              "title": "Quest Title",
              "theme": "Theme description",
              "summary": "Short 2-sentence summary",
              "estimatedMinutes": 60,
              "estimatedDistanceMetres": 1500,
              "greenScore": {
                "score": 90,
                "factors": [
                  {"label": "Walkable Route", "explanation": "Compact 1.5km walking tour"},
                  {"label": "Local Spots", "explanation": "Includes independent artisans"}
                ]
              },
              "stops": [
                {
                  "id": "stop_01_landmark_name",
                  "placeId": "ChIJ_real_place_id_or_generated",
                  "name": "Stop Name",
                  "category": "Architecture",
                  "latitude": 10.776,
                  "longitude": 106.701,
                  "whySelected": "Why this stop fits user interests",
                  "story": "Rich cultural background story",
                  "factReference": "Verified Saigon Architectural Registry",
                  "challenge": {
                    "type": "PHOTO_OR_SKIP",
                    "prompt": "Find and photograph the carved stone motif on the doorway",
                    "successGuidance": "Look closely at the arch above the entrance"
                  },
                  "photos": [
                    "https://images.unsplash.com/photo-1583417319070-4a69db38a482?w=800&auto=format&fit=crop&q=80",
                    "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800&auto=format&fit=crop&q=80",
                    "https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop&q=80"
                  ]
                }
              ]
            }
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemPrompt))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
                put("maxOutputTokens", 4096)
            })
        }

        for (model in modelsToTry) {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            try {
                val request = Request.Builder()
                    .url(url)
                    .post(jsonBody.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                val responseText = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    if (response.code == 429) {
                        lastException = Exception("Quota limit reached for $model (429)")
                        continue
                    }
                    throw Exception("Gemini API Error ${response.code}: $responseText")
                }

                val parsedJson = JSONObject(responseText)
                val candidates = parsedJson.optJSONArray("candidates")
                    ?: throw Exception("No candidates returned from Gemini")
                val content = candidates.getJSONObject(0).getJSONObject("content")
                val parts = content.getJSONArray("parts")
                val text = parts.getJSONObject(0).getString("text")

                var cleanText = text.trim()
                val jsonStartIdx = cleanText.indexOf("{")
                val jsonEndIdx = cleanText.lastIndexOf("}")
                if (jsonStartIdx != -1 && jsonEndIdx != -1 && jsonStartIdx < jsonEndIdx) {
                    cleanText = cleanText.substring(jsonStartIdx, jsonEndIdx + 1)
                }

                if (cleanText.isEmpty()) throw Exception("Gemini returned empty JSON response")

                val adapter = moshi.adapter(Quest::class.java).lenient()
                return@withContext adapter.fromJson(cleanText) ?: throw Exception("Failed to parse Quest JSON")
            } catch (e: Exception) {
                lastException = e
            }
        }

        throw lastException ?: Exception("All Gemini models failed")
    }

    suspend fun verifyPhoto(
        apiKey: String,
        bitmap: Bitmap,
        challengePrompt: String,
        stopName: String,
        language: String
    ): PhotoVerificationResult = withContext(Dispatchers.IO) {
        val modelsToTry = listOf("gemini-3.5-flash", "gemini-flash-latest", "gemini-3.1-flash-lite-preview")
        var lastException: Exception? = null

        val langName = when (language) {
            "vi" -> "Vietnamese (Tiếng Việt)"
            "zh" -> "Chinese (Simplified 中文)"
            "ja" -> "Japanese (日本語)"
            "ko" -> "Korean (한국어)"
            else -> "English"
        }

        val promptText = """
            You are HẻmQuest Multimodal AI Verifier, an expert architectural, cultural, and urban heritage verifier for walking quests in Saigon (Ho Chi Minh City), Vietnam.
            
            TARGET DESTINATION / STOP: "$stopName"
            CHALLENGE PROMPT / MISSION: "$challengePrompt"
            TARGET RESPONSE LANGUAGE: $langName
            
            RIGOROUS PHOTO VERIFICATION DIRECTIVES:
            Analyze the attached image strictly against the Challenge Prompt and Target Destination:
            
            1. STATUS = "REJECTED" if:
               - The photo shows completely unrelated scenes or indoor clutter (e.g. computer monitor, keyboard, blank table, floor, bedroom wall, ceiling, furniture, shoes, cars, random pets, or selfies without the landmark).
               - The user is pointing the camera at a random non-relevant object to bypass the challenge.
               - The image has nothing to do with the requested cultural/architectural subject at $stopName.
            
            2. STATUS = "NOT_ENOUGH_INFORMATION" if:
               - The photo is pitch black, extreme glare/overexposed, heavily blurred, or completely unidentifiable.
            
            3. STATUS = "UNCERTAIN" if:
               - The photo is taken in an alley or Vietnamese street context, but is framed too far away or missing the specific detail demanded in the challenge prompt.
            
            4. STATUS = "LIKELY_MATCH" only if:
               - The photo clearly and genuinely captures the requested cultural item, architectural landmark, sign, food/beverage preparation, or specific scene described in "$challengePrompt".
            
            OUTPUT RULES:
            - If REJECTED or NOT_ENOUGH_INFORMATION:
              * "observation": State clearly and politely what was actually seen in the photo in $langName (e.g. "Ảnh chụp màn hình máy tính/vật dụng phòng làm việc, không phù hợp với thử thách tại $stopName").
              * "detailNotes": Give exact instructions in $langName on what the user needs to point the camera at to pass the challenge.
            - If LIKELY_MATCH:
              * "observation": Warm confirmation in $langName highlighting the specific cultural element detected.
              * "detailNotes": An interesting architectural/cultural tip about this stop in $langName.
            
            Return ONLY a valid JSON object matching this structure (no markdown fences, no extra text):
            {
              "status": "REJECTED", // Options: LIKELY_MATCH, UNCERTAIN, NOT_ENOUGH_INFORMATION, REJECTED
              "observation": "2 clear sentences in $langName",
              "detailNotes": "Clear guidance tip in $langName"
            }
        """.trimIndent()

        val base64Image = bitmap.toBase64()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", promptText))
                        put(JSONObject().put("inlineData", JSONObject().apply {
                            put("mimeType", "image/jpeg")
                            put("data", base64Image)
                        }))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.2)
            })
        }

        for (model in modelsToTry) {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            try {
                val request = Request.Builder()
                    .url(url)
                    .post(jsonBody.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                val responseText = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    if (response.code == 429) {
                        lastException = Exception("Quota limit reached for $model (429)")
                        continue
                    }
                    throw Exception("Gemini Vision API Error ${response.code}: $responseText")
                }

                val parsedJson = JSONObject(responseText)
                val candidates = parsedJson.optJSONArray("candidates")
                    ?: throw Exception("No candidates in vision response")
                val content = candidates.getJSONObject(0).getJSONObject("content")
                val parts = content.getJSONArray("parts")
                val text = parts.getJSONObject(0).getString("text")

                var cleanText = text.trim()
                val jsonStartIdx = cleanText.indexOf("{")
                val jsonEndIdx = cleanText.lastIndexOf("}")
                if (jsonStartIdx != -1 && jsonEndIdx != -1 && jsonStartIdx < jsonEndIdx) {
                    cleanText = cleanText.substring(jsonStartIdx, jsonEndIdx + 1)
                }

                if (cleanText.isEmpty()) throw Exception("Gemini returned empty JSON response")

                val resultObj = JSONObject(cleanText)
                val statusStr = resultObj.optString("status", "REJECTED").uppercase()
                val status = when (statusStr) {
                    "LIKELY_MATCH" -> VerificationStatus.LIKELY_MATCH
                    "UNCERTAIN" -> VerificationStatus.UNCERTAIN
                    "NOT_ENOUGH_INFORMATION" -> VerificationStatus.NOT_ENOUGH_INFORMATION
                    else -> VerificationStatus.REJECTED
                }

                val isVi = language == "vi"
                return@withContext PhotoVerificationResult(
                    status = status,
                    observation = resultObj.optString("observation", if (isVi) "Hình ảnh chưa thể hiện chi tiết phù hợp với thử thách." else "The photo does not display matching details for this challenge."),
                    detailNotes = resultObj.optString("detailNotes", if (isVi) "Vui lòng hướng camera về đúng địa điểm hoặc đối tượng văn hóa của thử thách." else "Please aim the camera at the authentic challenge subject or heritage site.")
                )
            } catch (e: Exception) {
                lastException = e
            }
        }

        throw lastException ?: Exception("All Gemini models failed")
    }

    suspend fun askGeminiAssistant(apiKey: String, promptText: String): String = withContext(Dispatchers.IO) {
        val modelsToTry = listOf("gemini-3.5-flash", "gemini-flash-latest", "gemini-3.1-flash-lite-preview")
        var lastException: Exception? = null

        val jsonBodyWithMaps = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", promptText))
                    })
                })
            })
            put("tools", JSONArray().apply {
                put(JSONObject().apply {
                    put("googleMaps", JSONObject())
                })
            })
        }

        val jsonBodySimple = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", promptText))
                    })
                })
            })
        }

        for (model in modelsToTry) {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            
            // Try with Google Maps grounding first, then fallback to standard prompt if needed
            for (body in listOf(jsonBodyWithMaps, jsonBodySimple)) {
                try {
                    val request = Request.Builder()
                        .url(url)
                        .post(body.toString().toRequestBody(jsonMediaType))
                        .build()

                    val response = client.newCall(request).execute()
                    val responseText = response.body?.string() ?: ""

                    if (!response.isSuccessful) {
                        if (response.code == 429) {
                            lastException = Exception("Quota limit reached for $model (429)")
                            break
                        }
                        // If tools error (400), continue to try jsonBodySimple
                        lastException = Exception("Gemini API Error ${response.code}: $responseText")
                        continue
                    }

                    val parsedJson = JSONObject(responseText)
                    val candidates = parsedJson.optJSONArray("candidates") ?: continue
                    if (candidates.length() == 0) continue
                    val content = candidates.getJSONObject(0).getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    val fullText = StringBuilder()
                    for (p in 0 until parts.length()) {
                        val partObj = parts.getJSONObject(p)
                        if (partObj.has("text")) {
                            fullText.append(partObj.getString("text"))
                        }
                    }
                    if (fullText.isNotEmpty()) {
                        return@withContext fullText.toString()
                    }
                } catch (e: Exception) {
                    lastException = e
                }
            }
        }

        throw lastException ?: Exception("All Gemini models failed")
    }

    private fun Bitmap.toBase64(): String {
        val maxDimension = 768
        val scaledBitmap = if (width > maxDimension || height > maxDimension) {
            val ratio = width.toFloat() / height.toFloat()
            val newWidth = if (ratio > 1f) maxDimension else (maxDimension * ratio).toInt()
            val newHeight = if (ratio > 1f) (maxDimension / ratio).toInt() else maxDimension
            Bitmap.createScaledBitmap(this, newWidth.coerceAtLeast(1), newHeight.coerceAtLeast(1), true)
        } else {
            this
        }
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
