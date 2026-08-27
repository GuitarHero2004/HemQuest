package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.auth.AuthManager
import com.example.data.PassportPhotoDao
import com.example.data.QuestDao
import com.example.data.UserStatsDao

class UserStatsViewModelFactory(
    private val userStatsDao: UserStatsDao,
    private val questDao: QuestDao? = null,
    private val authManager: AuthManager? = null,
    private val notificationManager: com.example.util.AppNotificationManager? = null,
    private val passportPhotoDao: PassportPhotoDao? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserStatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserStatsViewModel(userStatsDao, questDao, authManager, notificationManager, passportPhotoDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

