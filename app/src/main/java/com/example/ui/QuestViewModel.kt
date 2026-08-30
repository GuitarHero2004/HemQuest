package com.example.ui

import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.PhotoVerificationResult
import com.example.model.Quest
import com.example.model.QuestRequest
import com.example.model.QuestStop
import com.example.model.StopStatus
import com.example.repository.OfflineQuestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuestUiState(
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val isJourneyStarted: Boolean = false,
    val currentQuest: Quest? = null,
    val activeStopIndex: Int = 0,
    val selectedStop: QuestStop? = null,
    val questRequest: QuestRequest = QuestRequest(),
    val showQuestBuilderSheet: Boolean = false,
    val showCheckpointDetailSheet: Boolean = false,
    val showPhotoVerificationSheet: Boolean = false,
    val showGreenScoreDialog: Boolean = false,
    val showStreakInfoDialog: Boolean = false,
    val showXpInfoDialog: Boolean = false,
    val showRecapDialog: Boolean = false,
    val showCheckpointDiscoveryDialog: Boolean = false,
    val discoveryStop: QuestStop? = null,
    val verificationResult: PhotoVerificationResult? = null,
    val isVerifyingPhoto: Boolean = false,
    val errorMessage: String? = null,
    val userLocationLat: Double = 10.7741,
    val userLocationLng: Double = 106.7028,
    val lastCapturedBitmap: Bitmap? = null,
    val distanceToOngoingMeters: Int = 0,
    val estimatedMinutesWalk: Int = 0,
    val cardinalDirection: String = "Hướng Bắc ⬆️",
    val isSimulatingMovement: Boolean = false,
    val questStepCount: Int = 0,
    val questDistanceMeters: Double = 0.0,
    val questCaloriesBurned: Int = 0,
    val questCo2SavedKg: Double = 0.0,
    val isSequenceHudExpanded: Boolean = true,
    val autoDiscoveredStopIds: Set<String> = emptySet()
) {
    val ongoingStop: QuestStop?
        get() = currentQuest?.stops?.firstOrNull { it.status == StopStatus.CURRENT }
            ?: currentQuest?.stops?.firstOrNull { it.status != StopStatus.COMPLETED && it.status != StopStatus.SKIPPED }
            ?: selectedStop
            ?: currentQuest?.stops?.firstOrNull()

    val completedStopsCount: Int
        get() = currentQuest?.stops?.count { it.status == StopStatus.COMPLETED } ?: 0

    val totalStopsCount: Int
        get() = currentQuest?.stops?.size ?: 0

    val progressFraction: Float
        get() = if (totalStopsCount > 0) completedStopsCount.toFloat() / totalStopsCount.toFloat() else 0f
}

fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
    val r = 6371000.0 // Earth radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return (r * c).toInt()
}

fun calculateBearingDegrees(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val dLon = Math.toRadians(lon2 - lon1)
    val y = Math.sin(dLon) * Math.cos(Math.toRadians(lat2))
    val x = Math.cos(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) -
            Math.sin(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(dLon)
    var brng = Math.toDegrees(Math.atan2(y, x))
    return (brng + 360) % 360
}

fun getCardinalDirection(bearing: Double, lang: String = "vi"): String {
    return when {
        bearing >= 337.5 || bearing < 22.5 -> if (lang == "vi") "Hướng Bắc ⬆️" else "North ⬆️"
        bearing < 67.5 -> if (lang == "vi") "Hướng Đông Bắc ↗️" else "North-East ↗️"
        bearing < 112.5 -> if (lang == "vi") "Hướng Đông ➡️" else "East ➡️"
        bearing < 157.5 -> if (lang == "vi") "Hướng Đông Nam ↘️" else "South-East ↘️"
        bearing < 202.5 -> if (lang == "vi") "Hướng Nam ⬇️" else "South ⬇️"
        bearing < 247.5 -> if (lang == "vi") "Hướng Tây Nam ↙️" else "South-West ↙️"
        bearing < 292.5 -> if (lang == "vi") "Hướng Tây ⬅️" else "West ⬅️"
        else -> if (lang == "vi") "Hướng Tây Bắc ↖️" else "North-West ↖️"
    }
}

class QuestViewModel(
    private val repository: OfflineQuestRepository,
    private val prefs: SharedPreferences
) : ViewModel() {
    private val savedLang = prefs.getString("selected_language", "vi") ?: "vi"
    private val _uiState = MutableStateFlow(
        QuestUiState(questRequest = QuestRequest(language = savedLang))
    )
    val uiState: StateFlow<QuestUiState> = _uiState.asStateFlow()

    private var generateJob: kotlinx.coroutines.Job? = null

    init {
        // Ensure initial 'mock_quests' and 'cultural_glossary' are seeded on Firestore if empty, then sync
        viewModelScope.launch {
            try {
                com.example.repository.MockQuestSeeder.seedIfNeeded()
                repository.pullAndCacheMockQuestsFromFirestore()
            } catch (e: Exception) {
                android.util.Log.d("QuestViewModel", "Initial Firestore mock quests sync skipped: ${e.message}")
            }
            try {
                com.example.repository.CulturalGlossaryRepository.syncFromFirestore()
            } catch (e: Exception) {
                android.util.Log.d("QuestViewModel", "Initial Firestore glossary sync skipped: ${e.message}")
            }
        }
    }

    fun refreshCloudQuests() {
        viewModelScope.launch {
            try {
                repository.pullAndCacheMockQuestsFromFirestore()
            } catch (e: Exception) {
                android.util.Log.w("QuestViewModel", "Failed to refresh cloud quests: ${e.message}")
            }
        }
    }

    private fun loadInitialQuest() {
        generateJob?.cancel()
        generateJob = viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, errorMessage = null) }
            val request = _uiState.value.questRequest
            try {
                val quest = repository.getOrFetchQuest(request)
                // Initialize stops with first stop as CURRENT, rest as UPCOMING
                val initialStops = quest.stops.mapIndexed { idx, stop ->
                    if (idx == 0) stop.copy(status = StopStatus.CURRENT)
                    else stop.copy(status = StopStatus.UPCOMING)
                }
                val initializedQuest = quest.copy(stops = initialStops)
                val firstStop = initialStops.firstOrNull()

                // Starting location slightly offset to show walking route towards 1st stop
                val startLat = (firstStop?.latitude ?: 10.7741) - 0.0018
                val startLng = (firstStop?.longitude ?: 106.7028) - 0.0015

                val dist = if (firstStop != null) {
                    calculateDistanceMeters(startLat, startLng, firstStop.latitude, firstStop.longitude)
                } else 0
                val bearing = if (firstStop != null) {
                    calculateBearingDegrees(startLat, startLng, firstStop.latitude, firstStop.longitude)
                } else 0.0

                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        currentQuest = initializedQuest,
                        activeStopIndex = 0,
                        selectedStop = firstStop,
                        userLocationLat = startLat,
                        userLocationLng = startLng,
                        distanceToOngoingMeters = dist,
                        estimatedMinutesWalk = maxOf(1, (dist / 80.0).toInt()),
                        cardinalDirection = getCardinalDirection(bearing, request.language)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        errorMessage = "Failed to load quest: ${e.message}"
                    )
                }
            }
        }
    }

    fun generateNewQuest(request: QuestRequest) {
        if (_uiState.value.isGenerating && _uiState.value.questRequest == request) {
            return
        }
        generateJob?.cancel()
        generateJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGenerating = true,
                    questRequest = request,
                    showQuestBuilderSheet = false,
                    errorMessage = null
                )
            }
            try {
                val quest = repository.getOrFetchQuest(request)
                val initialStops = quest.stops.mapIndexed { idx, stop ->
                    if (idx == 0) stop.copy(status = StopStatus.CURRENT)
                    else stop.copy(status = StopStatus.UPCOMING)
                }
                val initializedQuest = quest.copy(stops = initialStops)
                val firstStop = initialStops.firstOrNull()

                val startLat = request.latitude
                val startLng = request.longitude

                val dist = if (firstStop != null) {
                    calculateDistanceMeters(startLat, startLng, firstStop.latitude, firstStop.longitude)
                } else 0
                val bearing = if (firstStop != null) {
                    calculateBearingDegrees(startLat, startLng, firstStop.latitude, firstStop.longitude)
                } else 0.0

                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        currentQuest = initializedQuest,
                        activeStopIndex = 0,
                        selectedStop = firstStop,
                        userLocationLat = startLat,
                        userLocationLng = startLng,
                        distanceToOngoingMeters = dist,
                        estimatedMinutesWalk = maxOf(1, (dist / 80.0).toInt()),
                        cardinalDirection = getCardinalDirection(bearing, request.language)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        errorMessage = "Failed to generate quest: ${e.message}"
                    )
                }
            }
        }
    }

    private var lastTrackedLocationLat: Double? = null
    private var lastTrackedLocationLng: Double? = null

    /**
     * Triggered in real-time by physical hardware step sensor, accelerometer motion, or GPS movement.
     */
    fun onStepDetected(stepCount: Int = 1) {
        if (stepCount <= 0) return
        _uiState.update { current ->
            val newSteps = current.questStepCount + stepCount
            val newDist = current.questDistanceMeters + (stepCount * 0.75)
            val newKcal = (newSteps * 0.042).toInt().coerceAtLeast(if (newSteps > 10) 1 else 0)
            val newCo2 = newDist * 0.000154 // 154g CO2/km avoided compared to a 150cc scooter
            current.copy(
                questStepCount = newSteps,
                questDistanceMeters = newDist,
                questCaloriesBurned = newKcal,
                questCo2SavedKg = newCo2
            )
        }
    }

    fun updateUserLocation(lat: Double, lng: Double) {
        val state = _uiState.value
        val ongoing = state.ongoingStop
        val dist = if (ongoing != null) {
            calculateDistanceMeters(lat, lng, ongoing.latitude, ongoing.longitude)
        } else 0
        val bearing = if (ongoing != null) {
            calculateBearingDegrees(lat, lng, ongoing.latitude, ongoing.longitude)
        } else 0.0

        val prevLat = lastTrackedLocationLat
        val prevLng = lastTrackedLocationLng
        lastTrackedLocationLat = lat
        lastTrackedLocationLng = lng

        var deltaDist = 0.0
        if (prevLat != null && prevLng != null) {
            val moved = calculateDistanceMeters(prevLat, prevLng, lat, lng).toDouble()
            // Filter realistic movement and ignore GPS jump spikes (>150m in a single frame)
            if (moved in 0.8..150.0) {
                deltaDist = moved
            }
        }

        var newlyDiscoveredId: String? = null
        if (ongoing != null && dist <= 25 && !state.autoDiscoveredStopIds.contains(ongoing.id)) {
            newlyDiscoveredId = ongoing.id
        }

        _uiState.update { current ->
            val updatedAutoDiscovered = if (newlyDiscoveredId != null) {
                current.autoDiscoveredStopIds + newlyDiscoveredId
            } else {
                current.autoDiscoveredStopIds
            }

            val newDist = current.questDistanceMeters + deltaDist
            val newSteps = if (deltaDist > 0) current.questStepCount + (deltaDist / 0.75).toInt() else current.questStepCount
            val newKcal = (newSteps * 0.042).toInt().coerceAtLeast(if (newSteps > 10) 1 else 0)
            val newCo2 = newDist * 0.000154

            current.copy(
                userLocationLat = lat,
                userLocationLng = lng,
                distanceToOngoingMeters = dist,
                estimatedMinutesWalk = maxOf(1, (dist / 80.0).toInt()),
                cardinalDirection = getCardinalDirection(bearing, current.questRequest.language),
                autoDiscoveredStopIds = updatedAutoDiscovered,
                showCheckpointDiscoveryDialog = newlyDiscoveredId != null || current.showCheckpointDiscoveryDialog,
                discoveryStop = if (newlyDiscoveredId != null) ongoing else current.discoveryStop,
                questDistanceMeters = newDist,
                questStepCount = newSteps,
                questCaloriesBurned = newKcal,
                questCo2SavedKg = newCo2
            )
        }
    }

    /**
     * Simulates walking towards the ongoing destination.
     * Moves the user 35% closer towards the destination on each tap and increments step counter.
     */
    fun simulateStepTowardsOngoingStop() {
        val ongoing = _uiState.value.ongoingStop ?: return
        val currentLat = _uiState.value.userLocationLat
        val currentLng = _uiState.value.userLocationLng

        val targetLat = ongoing.latitude
        val targetLng = ongoing.longitude

        val dist = calculateDistanceMeters(currentLat, currentLng, targetLat, targetLng)

        val newLat: Double
        val newLng: Double

        if (dist <= 30) {
            // Already right at the destination
            newLat = targetLat
            newLng = targetLng
        } else {
            // Move 35% closer
            newLat = currentLat + (targetLat - currentLat) * 0.35
            newLng = currentLng + (targetLng - currentLng) * 0.35
        }

        // Add 50-70 steps and ~40m walking distance per simulate pulse
        val addedSteps = (45..65).random()
        val addedDist = 38.0
        val addedKcal = (1..2).random()
        val addedCo2 = 0.007

        _uiState.update {
            it.copy(
                questStepCount = it.questStepCount + addedSteps,
                questDistanceMeters = it.questDistanceMeters + addedDist,
                questCaloriesBurned = it.questCaloriesBurned + addedKcal,
                questCo2SavedKg = it.questCo2SavedKg + addedCo2
            )
        }

        updateUserLocation(newLat, newLng)
    }

    fun toggleSequenceHud() {
        _uiState.update { it.copy(isSequenceHudExpanded = !it.isSequenceHudExpanded) }
    }

    fun selectStop(stop: QuestStop) {
        _uiState.update {
            it.copy(
                selectedStop = stop,
                discoveryStop = stop,
                showCheckpointDiscoveryDialog = true
            )
        }
    }

    fun openCheckpointDiscovery(stop: QuestStop) {
        _uiState.update {
            it.copy(
                selectedStop = stop,
                discoveryStop = stop,
                showCheckpointDiscoveryDialog = true
            )
        }
    }

    fun closeCheckpointDiscovery() {
        _uiState.update { it.copy(showCheckpointDiscoveryDialog = false) }
    }

    fun closeCheckpointDetail() {
        _uiState.update { it.copy(showCheckpointDetailSheet = false) }
    }

    fun startJourney() {
        _uiState.update { it.copy(isJourneyStarted = true) }
    }

    fun stopJourney() {
        _uiState.update { it.copy(isJourneyStarted = false) }
    }

    fun openQuestBuilder() {
        _uiState.update { it.copy(showQuestBuilderSheet = true) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun setLanguage(lang: String) {
        if (_uiState.value.questRequest.language == lang) return
        prefs.edit().putString("selected_language", lang).apply()
        val newRequest = _uiState.value.questRequest.copy(language = lang)
        _uiState.update { it.copy(questRequest = newRequest) }
        if (_uiState.value.currentQuest != null) {
            generateNewQuest(newRequest)
        }
    }

    fun loadLastSavedQuest() {
        viewModelScope.launch {
            try {
                val savedList = repository.getAllSavedQuests().firstOrNull()
                val lastQuest = savedList?.firstOrNull()
                if (lastQuest != null) {
                    val firstStop = lastQuest.stops.firstOrNull()
                    _uiState.update {
                        it.copy(
                            currentQuest = lastQuest,
                            selectedStop = firstStop,
                            activeStopIndex = 0
                        )
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun toggleGreenScoreDialog(show: Boolean) {
        _uiState.update { it.copy(showGreenScoreDialog = show) }
    }

    fun toggleStreakInfoDialog(show: Boolean) {
        _uiState.update { it.copy(showStreakInfoDialog = show) }
    }

    fun toggleXpInfoDialog(show: Boolean) {
        _uiState.update { it.copy(showXpInfoDialog = show) }
    }

    fun verifyPhotoChallenge(bitmap: Bitmap) {
        val currentStop = _uiState.value.ongoingStop ?: _uiState.value.selectedStop ?: return
        val currentQuest = _uiState.value.currentQuest
        val language = _uiState.value.questRequest.language
        val userLat = _uiState.value.userLocationLat
        val userLng = _uiState.value.userLocationLng
        val distToStop = calculateDistanceMeters(userLat, userLng, currentStop.latitude, currentStop.longitude)

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isVerifyingPhoto = true,
                    lastCapturedBitmap = bitmap,
                    showCheckpointDetailSheet = false
                )
            }
            val result = repository.verifyPhoto(
                bitmap = bitmap,
                stopName = currentStop.name,
                vietnameseName = currentStop.name,
                challengePrompt = currentStop.challenge.prompt,
                category = currentStop.category,
                story = currentStop.story,
                whySelected = currentStop.whySelected,
                culturalTip = currentStop.factReference,
                successGuidance = currentStop.challenge.successGuidance ?: "",
                questTheme = currentQuest?.theme ?: "",
                targetLatitude = currentStop.latitude,
                targetLongitude = currentStop.longitude,
                userLatitude = userLat,
                userLongitude = userLng,
                distanceMeters = distToStop,
                language = language
            )
            _uiState.update {
                it.copy(
                    isVerifyingPhoto = false,
                    verificationResult = result,
                    showPhotoVerificationSheet = true
                )
            }
        }
    }

    /**
     * Confirms the completion of the current ongoing destination.
     * Marks it as COMPLETED, advances to the next UPCOMING destination,
     * updates user location heading, and triggers the recap dialog if all destinations are complete.
     */
    fun confirmStopCompletion() {
        val quest = _uiState.value.currentQuest ?: return
        val targetStop = _uiState.value.ongoingStop ?: _uiState.value.selectedStop ?: return

        val updatedStops = quest.stops.map { stop ->
            if (stop.id == targetStop.id) {
                stop.copy(status = StopStatus.COMPLETED)
            } else stop
        }

        val completedCount = updatedStops.count { it.status == StopStatus.COMPLETED }
        val allCompleted = completedCount == updatedStops.size

        val nextStopIndex = updatedStops.indexOfFirst { it.status != StopStatus.COMPLETED && it.status != StopStatus.SKIPPED }
        val finalStops = if (nextStopIndex != -1) {
            updatedStops.mapIndexed { idx, stop ->
                if (idx == nextStopIndex) {
                    stop.copy(status = StopStatus.CURRENT)
                } else if (stop.status == StopStatus.CURRENT && stop.id != updatedStops[nextStopIndex].id) {
                    stop.copy(status = StopStatus.COMPLETED)
                } else {
                    stop
                }
            }
        } else {
            updatedStops
        }

        val newQuest = quest.copy(stops = finalStops)
        val nextOngoingStop = if (nextStopIndex != -1) finalStops[nextStopIndex] else null

        // Update user location metrics towards new ongoing stop
        val userLat = _uiState.value.userLocationLat
        val userLng = _uiState.value.userLocationLng

        val dist = if (nextOngoingStop != null) {
            calculateDistanceMeters(userLat, userLng, nextOngoingStop.latitude, nextOngoingStop.longitude)
        } else 0
        val bearing = if (nextOngoingStop != null) {
            calculateBearingDegrees(userLat, userLng, nextOngoingStop.latitude, nextOngoingStop.longitude)
        } else 0.0

        _uiState.update {
            it.copy(
                currentQuest = newQuest,
                activeStopIndex = if (nextStopIndex != -1) nextStopIndex else it.activeStopIndex,
                selectedStop = nextOngoingStop ?: targetStop,
                showPhotoVerificationSheet = false,
                showCheckpointDetailSheet = false,
                showCheckpointDiscoveryDialog = false,
                showRecapDialog = allCompleted,
                distanceToOngoingMeters = dist,
                estimatedMinutesWalk = maxOf(1, (dist / 80.0).toInt()),
                cardinalDirection = getCardinalDirection(bearing, it.questRequest.language)
            )
        }
    }

    fun skipCurrentStop() {
        val quest = _uiState.value.currentQuest ?: return
        val targetStop = _uiState.value.ongoingStop ?: _uiState.value.selectedStop ?: return
        val updatedStops = quest.stops.map { stop ->
            if (stop.id == targetStop.id) {
                stop.copy(status = StopStatus.SKIPPED)
            } else stop
        }
        val nextStopIndex = updatedStops.indexOfFirst { it.status == StopStatus.UPCOMING }
        val finalStops = if (nextStopIndex != -1) {
            updatedStops.mapIndexed { idx, stop ->
                if (idx == nextStopIndex) stop.copy(status = StopStatus.CURRENT) else stop
            }
        } else updatedStops

        val newQuest = quest.copy(stops = finalStops)
        val nextStop = if (nextStopIndex != -1) finalStops[nextStopIndex] else targetStop

        val userLat = _uiState.value.userLocationLat
        val userLng = _uiState.value.userLocationLng
        val dist = calculateDistanceMeters(userLat, userLng, nextStop.latitude, nextStop.longitude)
        val bearing = calculateBearingDegrees(userLat, userLng, nextStop.latitude, nextStop.longitude)

        _uiState.update {
            it.copy(
                currentQuest = newQuest,
                selectedStop = nextStop,
                showCheckpointDetailSheet = false,
                distanceToOngoingMeters = dist,
                estimatedMinutesWalk = maxOf(1, (dist / 80.0).toInt()),
                cardinalDirection = getCardinalDirection(bearing, it.questRequest.language)
            )
        }
    }

    fun toggleRecapDialog(show: Boolean) {
        _uiState.update { it.copy(showRecapDialog = show) }
    }

    fun toggleSequenceHudExpanded() {
        _uiState.update { it.copy(isSequenceHudExpanded = !it.isSequenceHudExpanded) }
    }

    /**
     * Save completed quest to local database, clear quest progress HUD, and reset current quest to null
     */
    fun saveAndCompleteQuest(onFinished: (() -> Unit)? = null) {
        val quest = _uiState.value.currentQuest
        if (quest != null) {
            viewModelScope.launch {
                try {
                    repository.saveQuestLocally(quest)
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
        exitAndResetQuest()
        onFinished?.invoke()
    }

    /**
     * Completely resets quest active state and destroys child progress views
     */
    fun exitAndResetQuest() {
        generateJob?.cancel()
        _uiState.update {
            it.copy(
                isGenerating = false,
                isJourneyStarted = false,
                currentQuest = null,
                activeStopIndex = 0,
                selectedStop = null,
                discoveryStop = null,
                showCheckpointDetailSheet = false,
                showPhotoVerificationSheet = false,
                showCheckpointDiscoveryDialog = false,
                showRecapDialog = false,
                questStepCount = 0,
                questDistanceMeters = 0.0,
                questCaloriesBurned = 0,
                questCo2SavedKg = 0.0,
                lastCapturedBitmap = null,
                verificationResult = null,
                distanceToOngoingMeters = 0,
                estimatedMinutesWalk = 0
            )
        }
    }

    fun shortenQuestTo30Mins() {
        val currentRequest = _uiState.value.questRequest
        val newRequest = currentRequest.copy(durationMinutes = 30)
        generateNewQuest(newRequest)
    }

    fun closePhotoVerificationSheet() {
        _uiState.update { it.copy(showPhotoVerificationSheet = false) }
    }
}

