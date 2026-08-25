package com.example.ui

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.repository.OfflineQuestRepository

class QuestViewModelFactory(
    private val repository: OfflineQuestRepository,
    private val prefs: SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuestViewModel(repository, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

