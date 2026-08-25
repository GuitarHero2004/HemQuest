package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.UserStatsDao
import com.example.data.UserStatsEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserStatsViewModel(
    private val userStatsDao: UserStatsDao
) : ViewModel() {

    val userStats: StateFlow<UserStatsEntity> = userStatsDao.getUserStats()
        .map { it ?: UserStatsEntity() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserStatsEntity()
        )

    init {
        // Initialize if empty
        viewModelScope.launch {
            if (userStatsDao.getUserStatsSync() == null) {
                userStatsDao.insertOrUpdate(UserStatsEntity())
            }
        }
    }

    fun addSteps(steps: Int) {
        viewModelScope.launch {
            userStatsDao.addSteps(steps)
        }
    }

    fun incrementCheckpoints() {
        viewModelScope.launch {
            userStatsDao.incrementCheckpoints()
        }
    }

    fun addXp(xp: Int) {
        viewModelScope.launch {
            userStatsDao.addXp(xp)
        }
    }
}
