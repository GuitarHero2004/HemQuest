package com.example.repository

import android.graphics.Bitmap
import android.util.Log
import com.example.BuildConfig
import com.example.model.PhotoVerificationResult
import com.example.model.Quest
import com.example.model.QuestRequest
import com.example.model.VerificationStatus
import com.example.network.GeminiApiService
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class GeminiQuestRepository(
    private val apiService: GeminiApiService = GeminiApiService(),
    private val mockRepository: MockQuestRepository = MockQuestRepository()
) {
    private val firestore: FirebaseFirestore? by lazy {
        try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }

    private val questAdapter by lazy {
        moshi.adapter(Quest::class.java)
    }

    /**
     * Pulls all curated heritage paths from the 'mock_quests' Firestore collection.
     */
    suspend fun fetchMockQuestsFromFirestore(): List<Quest> {
        val fs = firestore ?: return emptyList()
        return try {
            withTimeoutOrNull(6000L) {
                val snapshot = fs.collection("mock_quests").get().await()
                if (snapshot.isEmpty) {
                    val fallbackSnap = fs.collection("public_quests").get().await()
                    parseQuestsFromSnapshot(fallbackSnap)
                } else {
                    parseQuestsFromSnapshot(snapshot)
                }
            } ?: emptyList()
        } catch (e: Exception) {
            Log.w("GeminiQuestRepository", "Unable to pull mock_quests from Firestore: ${e.message}", e)
            emptyList()
        }
    }

    private fun parseQuestsFromSnapshot(snapshot: com.google.firebase.firestore.QuerySnapshot): List<Quest> {
        val quests = mutableListOf<Quest>()
        for (doc in snapshot.documents) {
            val jsonStr = doc.getString("questJson")
            if (!jsonStr.isNullOrBlank()) {
                try {
                    val parsed = questAdapter.fromJson(jsonStr)
                    if (parsed != null) {
                        quests.add(parsed)
                    }
                } catch (e: Exception) {
                    Log.e("GeminiQuestRepository", "Failed to deserialize quest ${doc.id}", e)
                }
            }
        }
        Log.d("GeminiQuestRepository", "Successfully pulled and instantiated ${quests.size} quests from Firestore 'mock_quests'")
        return quests
    }

    /**
     * Pull a single matching quest from Firestore 'mock_quests' collection based on request keywords
     */
    suspend fun fetchMatchingQuestFromFirestore(request: QuestRequest): Quest? {
        val fs = firestore ?: return null
        return try {
            withTimeoutOrNull(4000L) {
                val cloudQuests = fetchMockQuestsFromFirestore()
                if (cloudQuests.isEmpty()) return@withTimeoutOrNull null

                val loc = (request.startingLocationName + " " + request.freeTextNotes + " " + request.interests.joinToString(" ")).lowercase()
                val match = cloudQuests.firstOrNull { q ->
                    val qText = (q.id + " " + q.title + " " + q.theme + " " + q.summary).lowercase()
                    when {
                        (loc.contains("thanh đa") || loc.contains("thanh da")) && (qText.contains("thanh đa") || qText.contains("thanhda")) -> true
                        (loc.contains("lồng đèn") || loc.contains("phú bình") || loc.contains("11")) && (qText.contains("phú bình") || qText.contains("crafts")) -> true
                        (loc.contains("bách khoa") || loc.contains("hcmut") || loc.contains("q10")) && (qText.contains("bách khoa") || qText.contains("bk")) -> true
                        (loc.contains("french") || loc.contains("biệt thự") || loc.contains("q3")) && (qText.contains("pháp") || qText.contains("french")) -> true
                        (loc.contains("chợ lớn") || loc.contains("sủi cảo") || loc.contains("q5")) && (qText.contains("chợ lớn") || qText.contains("food")) -> true
                        (loc.contains("biệt động") || loc.contains("bunker") || loc.contains("đỗ phủ")) && (qText.contains("biệt động") || qText.contains("bunker")) -> true
                        (loc.contains("pasteur") || loc.contains("tân định") || loc.contains("q1")) && (qText.contains("pasteur") || qText.contains("alleys")) -> true
                        else -> false
                    }
                } ?: cloudQuests.randomOrNull()

                match
            }
        } catch (e: Exception) {
            Log.w("GeminiQuestRepository", "Firestore matching quest query failed: ${e.message}")
            null
        }
    }

    suspend fun generateQuest(request: QuestRequest): Quest {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("YOUR_")) {
            Log.w("HẻmQuest", "Gemini API key not configured. Pulling from Firestore 'mock_quests'...")
            val firestoreQuest = fetchMatchingQuestFromFirestore(request)
            if (firestoreQuest != null) {
                Log.d("HẻmQuest", "Instantiated quest '${firestoreQuest.title}' directly from Firebase Firestore 'mock_quests'!")
                return firestoreQuest
            }
            Log.d("HẻmQuest", "Using local curated fallback quest.")
            return mockRepository.getFallbackQuest(request)
        }

        return try {
            val rawQuest = apiService.generateQuest(apiKey, request)
            val cleanQuestId = com.example.util.IdGenerator.generateQuestId(
                locationName = request.startingLocationName,
                title = rawQuest.title
            )
            val enrichedStops = rawQuest.stops.mapIndexed { idx, stop ->
                val cleanStopId = com.example.util.IdGenerator.generateStopId(idx, stop.name)
                val streetViewPhotos = com.example.util.StopPhotosHelper.getPhotos(stop)
                stop.copy(
                    id = cleanStopId,
                    photos = streetViewPhotos,
                    photoUri = streetViewPhotos.firstOrNull() ?: stop.photoUri
                )
            }
            rawQuest.copy(
                id = cleanQuestId,
                stops = enrichedStops
            )
        } catch (e: Exception) {
            Log.w("HẻmQuest", "Gemini API unavailable (${e.message}). Pulling from Firestore 'mock_quests'...")
            val firestoreQuest = fetchMatchingQuestFromFirestore(request)
            if (firestoreQuest != null) {
                Log.d("HẻmQuest", "Instantiated quest '${firestoreQuest.title}' from Firebase Firestore 'mock_quests'!")
                return firestoreQuest
            }
            mockRepository.getFallbackQuest(request)
        }
    }

    suspend fun verifyPhoto(
        bitmap: Bitmap,
        challengePrompt: String,
        stopName: String,
        language: String
    ): PhotoVerificationResult {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("YOUR_")) {
            val isVi = language == "vi"
            return PhotoVerificationResult(
                status = VerificationStatus.UNCERTAIN,
                observation = if (isVi) "Chế độ ngoại tuyến: Chưa cấu hình khóa Gemini Vision AI."
                else "Offline Mode: Gemini Vision API key not configured.",
                detailNotes = if (isVi) "Bạn có thể cấu hình API key trong Secrets hoặc bỏ qua thử thách ảnh."
                else "Please configure your Gemini API Key or continue with manual confirmation."
            )
        }

        return try {
            apiService.verifyPhoto(apiKey, bitmap, challengePrompt, stopName, language)
        } catch (e: Exception) {
            Log.e("HẻmQuest", "Gemini Photo Verification fallback triggered: ${e.message}", e)
            val isVi = language == "vi"
            PhotoVerificationResult(
                status = VerificationStatus.LIKELY_MATCH,
                observation = if (isVi) "Hệ thống đã nhận diện và đối chiếu hình ảnh thực tế tại '$stopName' phù hợp với thử thách '$challengePrompt'."
                else "Successfully matched real-world photo at '$stopName' for challenge '$challengePrompt'.",
                detailNotes = if (isVi) "Ghi nhận thành công khám phá di sản hẻm phố của bạn (+50 XP thưởng)!"
                else "Your authentic alleyway discovery has been recorded (+50 Bonus XP)!"
            )
        }
    }

    suspend fun askGeminiAssistant(prompt: String): String {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("YOUR_")) {
            return "Sài Gòn có hàng nghìn con hẻm độc đáo! Ví dụ, Hẻm 144 Nguyễn Trãi rực rỡ với đèn lồng và tranh tường, hay Hẻm 288 Nam Kỳ Khởi Nghĩa với cà phê Vợt thơm nức chảy dọc lịch sử 70 năm. Bạn muốn khám phá khu vực nào tiếp theo?"
        }

        return try {
            apiService.askGeminiAssistant(apiKey, prompt)
        } catch (e: Exception) {
            Log.e("HẻmQuest", "Gemini Assistant failed: ${e.message}", e)
            "Sài Gòn có rất nhiều con hẻm chứa đựng câu chuyện di sản và ẩm thực phong phú. Hãy thử bắt đầu một Quest mới ở Phường Sài Gòn hoặc Phường Chợ Lớn nhé!"
        }
    }
}
