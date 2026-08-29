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
        val modelsToTry = listOf("gemini-3.5-flash", "gemini-3.1-pro-preview", "gemini-flash-latest")
        var lastException: Exception? = null

        val systemPrompt = """
            You are HẻmQuest AI, a world-class cultural curator and urbanist specializing in low-impact mindful walking quests through authentic Saigon (Ho Chi Minh City) alleyways (hẻm).
            
            Generate a completely UNIQUE, engaging 3-to-4 stop walking quest in ${questRequest.startingLocationName} (Coordinates: ${questRequest.latitude}, ${questRequest.longitude}).
            
            CRITICAL DESIGN DIRECTIVES FOR MAXIMUM VARIETY & AUTHENTICITY:
            1. NEVER use generic or cookie-cutter templates. Craft an imaginative, culturally deep micro-theme based on the specific location and user interest.
            2. FOCUS ON HIDDEN ALLEYS (HẺM): Prioritize narrow residential corridors, tucked-away family workshops, 24/7 maker cafes, historic shaded courtyards, secret wartime bunkers, century-old clan temples, antique book/hardware alleyways, and independent street craft artisans OVER major commercial mega-attractions (like Notre Dame or Central Post Office).
            3. DIVERSE THEMATIC VARIATIONS TO EXPLORE:
               - "Bách Khoa & Maker Hub": 24/7 CAD project cafes, A0 blueprint plotting alleys, Nhat Tao electronics bazaar, legendary student broken rice diners, Gate 1 & 3 campus tea stalls.
               - "Colonial Villa & Hidden Courtyard": French louvre-shuttered alleys in Phường Xuân Hòa, vintage garden walls in Phường Nhiêu Lộc, shaded art alleyways.
               - "Chinatown Heritage & Guilds": Herbal medicine drawers in Phường Chợ Lớn, handmade dumpling alleys in Hà Tôn Quyền, century-old Hao Si Phuong courtyards.
               - "Riverfront & Canal Life": Breeze-filled canal alleys on Thanh Đa peninsula, historic 1893 Cầu Mống bridge corridor, riverside timber docks.
               - "Artisans & Street Crafts": Phú Bình cellophane lantern ateliers, woodcarving shops in Phường Hòa Bình, lacquerware and ceramic corners.
               - "Mid-Century Modernist Saigon": 1970s apartment balconies, vintage tile corridors in Tôn Thất Đạm / Pasteur alleys.
            
            USER PREFERENCES:
            - Target Duration: ${questRequest.durationMinutes} minutes (${if (questRequest.durationMinutes <= 45) "Generate 3 well-paced stops" else "Generate 4 immersive stops"})
            - Interests: ${questRequest.interests.joinToString(", ")}
            - Custom User Notes: ${questRequest.freeTextNotes}
            - Language: ${
                when (questRequest.language) {
                    "vi" -> "Vietnamese (Tiếng Việt)"
                    "zh" -> "Chinese (Simplified 中文)"
                    "ja" -> "Japanese (日本語)"
                    "ko" -> "Korean (한국어)"
                    else -> "English"
                }
            }. All output fields (title, theme, summary, stop names, category, whySelected, story, challenge prompt, successGuidance, green score labels/explanations) MUST be fully localized in this language.
            
            GEOGRAPHIC ACCURACY (Use accurate central Saigon coordinates and new merged Phường names):
            - Bách Khoa / Diên Hồng (HCMUT, Tô Hiến Thành, Lữ Gia): Lat 10.770 - 10.776, Lng 106.655 - 106.663
            - Thanh Đa Peninsula (Cư xá Lô S, Bờ Sông, Bình Quới): Lat 10.820 - 10.835, Lng 106.720 - 106.735
            - Chợ Lớn / Chợ Quán (Hà Tôn Quyền, Lương Nhữ Học, Hào Sĩ Phường): Lat 10.750 - 10.758, Lng 106.652 - 106.663
            - Bàn Cờ (Mê cung bàn cờ, Phở Bình, Cà phê Đỗ Phủ, Hầm Biệt Động): Lat 10.772 - 10.779, Lng 106.680 - 106.687
            - Xuân Hòa / Tân Định / Sài Gòn (Pasteur, Lý Tự Trọng, Tôn Thất Đạm, Biệt thự cổ): Lat 10.774 - 10.786, Lng 106.690 - 106.705
            - Phú Bình / Hòa Bình (Làng lồng đèn, xưởng mộc, gốm): Lat 10.760 - 10.768, Lng 106.646 - 106.653
            - Cầu Mống / Bến Vân Đồn (Bờ kênh Bến Nghé, Xóm cổ): Lat 10.767 - 10.773, Lng 106.702 - 106.708
            
            Return ONLY a JSON object strictly matching this schema:
            {
              "id": "quest_district_theme_timestamp",
              "title": "Inspiring Quest Title",
              "theme": "Captivating micro-theme description",
              "summary": "Short 2-sentence summary highlighting the unique route atmosphere",
              "estimatedMinutes": 60,
              "estimatedDistanceMetres": 1600,
              "greenScore": {
                "score": 92,
                "factors": [
                  {"label": "Walkable Route", "explanation": "Compact pedestrian corridor through shaded alleys"},
                  {"label": "Local Artisans", "explanation": "Directly visits independent local alleyway creators"}
                ]
              },
              "stops": [
                {
                  "id": "stop_01_unique_name",
                  "placeId": "ChIJ_real_place_id_or_generated",
                  "name": "Distinct Stop Name (with Phường & Alley Number)",
                  "category": "Architecture / Food / Craft / Community",
                  "latitude": 10.7745,
                  "longitude": 106.6621,
                  "whySelected": "Why this specific hidden gem fits the route",
                  "story": "Deep cultural story with verified historical/sociological facts",
                  "factReference": "Verified Saigon Heritage Registry",
                  "challenge": {
                    "type": "PHOTO_OR_SKIP",
                    "prompt": "Specific mindful observation or photo prompt doable from sidewalk",
                    "successGuidance": "Clues to spot the exact architectural or cultural detail"
                  },
                  "photos": [
                    "https://images.unsplash.com/photo-1583417319070-4a69db38a482?w=800&auto=format&fit=crop&q=80",
                    "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800&auto=format&fit=crop&q=80"
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
                put("temperature", 0.85)
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
