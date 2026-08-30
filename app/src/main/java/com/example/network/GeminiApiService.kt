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
        val modelsToTry = listOf("gemini-3.7-flash", "gemini-2.5-flash", "gemini-flash-latest")
        var lastException: Exception? = null

        val randomSeed = System.currentTimeMillis() % 10000
        val dynamicAngle = when ((randomSeed % 10).toInt()) {
            0 -> "FOCUSED ANGLE: Micro-Artisans, Guild Secrets & Family Workshops (làng nghề, xưởng thủ công, tiệm nghề gia truyền)"
            1 -> "FOCUSED ANGLE: Hidden Courtyards, Mid-Century Modernist Corridors & Tile Architecture (cư xá, ban công hoa giấy, gạch hoa xưa)"
            2 -> "FOCUSED ANGLE: Deep Culinary Lore, Heritage Recipe Keepers & Micro-Eateries (cà phê vợt, bánh tráng than hồng, quán gia truyền)"
            3 -> "FOCUSED ANGLE: Student & Maker Culture, 24/7 Hacker Hubs & Blueprint Alleys (đồ án, linh kiện, trà cóc vỉa hè)"
            4 -> "FOCUSED ANGLE: Secret Wartime History, Underground Cellars & Clan Temples (hầm bí mật, hội quán bang hội, di tích khuất)"
            5 -> "FOCUSED ANGLE: Riverfront Alleyways, Old Canals & Breeze Corridors (bến đò xưa, cầu bộ hành Eiffel, rặng dừa nước)"
            6 -> "FOCUSED ANGLE: Forgotten Religious Enclaves & Sacred Alley Shrines (chùa cổ trong hẻm, đình làng Nam Bộ, nhà thờ gỗ xưa)"
            7 -> "FOCUSED ANGLE: Literary, Antique Books & Acoustic Instrument Alleys (hẻm sách cũ, phố đàn guitar thủ công, xưởng tranh)"
            8 -> "FOCUSED ANGLE: Night Market Alleys & Midnight Supper Havens (hẻm ẩm thực đêm, xe hủ tiếu gõ, chè người Hoa)"
            else -> "FOCUSED ANGLE: Botanic Alleys, Green Courtyards & Shaded Balconies (vườn cây hẻm phố, ban công xanh, giàn thiên lý)"
        }

        val systemPrompt = """
            You are HẻmQuest AI, an expert cultural urbanist, investigative ethnographer, and master cartographer specializing in authentic, deep-cut, walking expeditions through the hidden alleys (hẻm, cư xá, xóm) of Ho Chi Minh City (Saigon).

            Generate a completely ORIGINAL, UNPREDICTABLE, HYPER-SPECIFIC 3-to-4 stop walking quest in ${questRequest.startingLocationName} (Around Lat: ${questRequest.latitude}, Lng: ${questRequest.longitude}).
            
            CREATIVE ANGLE FOR THIS SESSION: $dynamicAngle (Seed: $randomSeed)

            ==================================================
            STRICT ANTI-CLICHÉ & NEGATIVE CONSTRAINTS (MANDATORY):
            - NEVER include mainstream tourist monuments: NO Notre Dame Cathedral (Nhà thờ Đức Bà), NO Central Post Office (Bưu điện TP), NO Opera House (Nhà hát TP), NO Ben Thanh Market main hall (Chợ Bến Thành), NO Bitexco / Landmark 81, NO Bui Vien Walking Street, NO Nguyen Hue boulevard.
            - ALL STOPS MUST BE LOCATED IN RESIDENTIAL ALLEYS (Hẻm), OLD HOUSING ESTATES (Cư Xá), HISTORIC COURTYARDS (Hào Sĩ Phường, Xóm Cũ), OR CANAL BOARDWALKS.
            - Every stop name MUST specify its realistic Saigon Alley/Street number (e.g. "Hẻm 493/12 Tô Hiến Thành", "Cư Xá Lô S Thanh Đa", "Hẻm 14/19 Tôn Thất Đạm", "Hẻm 206 Trần Hưng Đạo", "Hẻm 284/4 Lý Thường Kiệt", "Hẻm 339 Lê Văn Sỹ", "Cư Xá Đô Thành", "Hẻm 100 Cô Giang").
            ==================================================

            SAIGON MICRO-NEIGHBORHOOD ENCYCLOPEDIA (Select relevant micro-locations):
            1. Bách Khoa / Diên Hồng / Lữ Gia (Q10): Hẻm in ấn A0 493 Tô Hiến Thành, chợ linh kiện điện tử Nhật Tảo, cư xá Lữ Gia cà phê thức 24/7, quán cơm tấm tăng ca đồ án Cổng 3, vỉa hè trà đá cờ tướng Cổng 1, hẻm 284 Lý Thường Kiệt.
            2. Chợ Lớn / Triệu Quang Phục / Hà Tôn Quyền (Q5): Phố thuốc bắc đông y Lương Nhữ Học, Hào Sĩ Phường (cụm nhà người Hoa 1910), hẻm sủi cảo gia truyền Hà Tôn Quyền, xưởng làm kéo & chảo gang Bùi Hữu Nghĩa, Hội Quán Nghĩa An & Ôn Lăng trong hẻm.
            3. Thanh Đa / Bình Quới (Bình Thạnh): Cư xá Thanh Đa Lô S 1972, hẻm bờ sông dừa nước Lô IV, bến phà đò Bình Quới xưa, hẻm cháo vịt gia truyền, cầu cạn ngắm hoàng hôn rặng dừa nước.
            4. Bàn Cờ / Vườn Chuối / Nguyễn Thiện Thuật (Q3): Mê cung bàn cờ hẻm ngoằn ngoèo, hầm giấu vũ khí Biệt Động Sài Gòn (cà phê Đỗ Phủ / phở Bình), hẻm bún bò xứ Huế, hẻm nghệ nhân làm đàn ghi-ta thủ công Nguyễn Thiện Thuật, Cư xá Đô Thành gạch hoa cổ.
            5. Phú Bình / Lạc Long Quân / Hòa Bình (Q11): Làng nghề làm lồng đèn giấy kiếng kiêm vẽ họa tiết, xưởng mộc tiện gỗ, hẻm thợ làm bánh pía nướng than, hẻm dệt nhuộm vải xưa.
            6. Tân Định / Đặng Dung / Huỳnh Tịnh Của (Q1): Hẻm thợ sửa đồng hồ cơ khí cổ Đặng Dung, hẻm bích họa 18A Nguyễn Thị Minh Khai, hẻm biệt thự cổ Pháp có giàn hoa giấy đường Huỳnh Tịnh Của, hẻm chợ đồ cổ Lê Công Kiều.
            7. Xóm Chiếu / Tôn Đản / Bến Vân Đồn (Q4): Cầu Mống di sản 1893, cư xá Vĩnh Hội ven kênh Bến Nghé, hẻm ẩm thực hải sản & ốc đêm Tôn Đản, xóm dệt chiếu xưa, hẻm nhà thờ Xóm Chiếu.
            8. Chợ Quán / An Bình / Trần Hưng Đạo (Q5): Hẻm xóm lồng đèn Lương Nhữ Học, quán chè cổ truyền Hà Tôn Quyền & quy linh cao, hẻm tiệm đúc đồng và chạm bạc gia truyền Triệu Quang Phục.
            9. Nhiêu Lộc / Thị Nghè / Phan Xích Long (Phú Nhuận): Hẻm bờ kênh Nhiêu Lộc rợp bóng bằng lăng, hẻm ẩm thực 3 miền Phan Xích Long, chợ hoa đêm Đầm Sen kết nối kênh, hẻm lò đúc lư đồng An Hội.
            10. Cầu Kho / Cô Giang / Cô Bắc (Q1): Xóm đình cổ Nam Bộ Cầu Kho, hẻm lò hủ tiếu hồ Cô Giang, hẻm cư xá xi măng xưa Bến Chương Dương, tiệm may áo dài truyền thống ẩn mình.
            11. Gò Vấp / Hạnh Thông Tây (Gò Vấp): Hẻm xóm lò gốm & tráng bánh tráng xưa, nhà thờ Hạnh Thông Tây kiến trúc Byzantine Pháp cổ, hẻm hoa kiểng đường Cây Trâm.
            12. Hào Huê / Lò Gốm (Q6 / Q8): Hẻm bến Bình Đông ghe thuyền miền Tây chở trái cây, lò gốm Hưng Phú xưa, hẻm làm nhang Tháp Mười, hẻm cầu chữ Y ngắm ngã ba kênh.

            USER REQUEST SPECIFICATIONS:
            - Target Duration: ${questRequest.durationMinutes} minutes (${if (questRequest.durationMinutes <= 45) "Generate 3 deeply detailed stops" else "Generate 4 immersive stops"})
            - Selected Interests: ${questRequest.interests.joinToString(", ")}
            - Custom User Prompt / Notes: "${questRequest.freeTextNotes}"
            - Target Language: ${
                when (questRequest.language) {
                    "vi" -> "Vietnamese (Tiếng Việt - tự nhiên, giàu chất thơ đường phố Sài Gòn, am hiểu lịch sử)"
                    "zh" -> "Chinese (Simplified 中文 - 地道文雅的城市漫步探索语调)"
                    "ja" -> "Japanese (日本語 - 情緒豊かな裏路地散策カルチャーガイド調)"
                    "ko" -> "Korean (한국어 - 생생하고 깊이 있는 골목길 탐방 가이드 어조)"
                    else -> "English (Atmospheric, culturally respectful, evocative urbanist tone)"
                }
            }. All output fields (title, theme, summary, stop names, category, whySelected, story, challenge prompt, successGuidance, green score factors) MUST be fully localized in this language.
            - Difficulty Rating Rule: Classify "difficulty" as exactly one of "EASY", "MODERATE", or "CHALLENGING" based on:
                * "EASY": Short gentle walks (< 1.3 km, <= 35 mins, flat paved alleys, leisurely stops).
                * "MODERATE": Medium walks (1.3 km - 2.2 km, 35 - 55 mins, winding multi-branching alleyways).
                * "CHALLENGING": Longer or steeper explorations (> 2.2 km, > 55 mins, stair climbs in vintage apartments, intricate labyrinth networks).

            JSON OUTPUT SCHEMA:
            {
              "id": "quest_district_theme_${System.currentTimeMillis()}",
              "title": "Evocative, authentic quest title",
              "theme": "Rich, specific cultural theme description",
              "summary": "2-sentence punchy summary of the alley exploration",
              "difficulty": "EASY",
              "estimatedMinutes": ${questRequest.durationMinutes},
              "estimatedDistanceMetres": ${if (questRequest.durationMinutes <= 45) 1600 else 2400},
              "greenScore": {
                "score": 95,
                "factors": [
                  {"label": "Shaded Alleyways", "explanation": "Low-carbon pedestrian-only labyrinth shielded from direct sun"},
                  {"label": "Grassroots Micro-Economy", "explanation": "Directly supports multi-generational alley artisans and family shops"}
                ]
              },
              "stops": [
                {
                  "id": "stop_01_unique_slug",
                  "placeId": "ChIJ_saigon_h_${System.currentTimeMillis()}_1",
                  "name": "Specific Alley Stop Name with Real Address & Ward",
                  "category": "Alley Heritage / Micro-Gastronomy / Artisan Workshop / Historic Residence",
                  "latitude": 10.7745,
                  "longitude": 106.6621,
                  "whySelected": "Why this particular hidden spot reveals an undiscovered facet of Saigon",
                  "story": "Rich storytelling with historical facts, local neighborhood anecdotes, and sensorial details (sounds, aromas, textures)",
                  "factReference": "Verified Saigon Urban Heritage Registry",
                  "challenge": {
                    "type": "PHOTO_OR_SKIP",
                    "prompt": "Specific, mindful observation or photo prompt doable directly from the sidewalk",
                    "successGuidance": "Distinct visual or architectural clue to spot on site"
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
                put("temperature", 0.92)
                put("topP", 0.95)
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
        val modelsToTry = listOf("gemini-3.7-flash", "gemini-2.5-flash", "gemini-flash-latest")
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
        val modelsToTry = listOf("gemini-3.7-flash", "gemini-2.5-flash", "gemini-flash-latest")
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
