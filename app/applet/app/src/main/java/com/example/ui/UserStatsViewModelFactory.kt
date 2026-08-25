package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.UserStatsDao

class UserStatsViewModelFactory(private val userStatsDao: UserStatsDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserStatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserStatsViewModel(userStatsDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
