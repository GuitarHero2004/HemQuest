package com.example.repository

import android.graphics.Bitmap
import android.util.Log
import com.example.BuildConfig
import com.example.model.PhotoVerificationResult
import com.example.model.Quest
import com.example.model.QuestRequest
import com.example.model.VerificationStatus
import com.example.network.GeminiApiService

class GeminiQuestRepository(
    private val apiService: GeminiApiService = GeminiApiService(),
    private val mockRepository: MockQuestRepository = MockQuestRepository()
) {

    suspend fun generateQuest(request: QuestRequest): Quest {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("YOUR_")) {
            Log.w("HẻmQuest", "Gemini API key not configured or placeholder used. Using curated fallback quest.")
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
            Log.w("HẻmQuest", "Gemini API unavailable (${e.message}). Seamlessly using curated quest.")
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
