package com.claudejobs.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.claudejobs.data.prefs.ApiKeyStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiKeyStore: ApiKeyStore
) : ViewModel() {

    var apiKey by mutableStateOf("")
        private set

    val model = apiKeyStore.getModelFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ApiKeyStore.DEFAULT_MODEL)

    init {
        viewModelScope.launch {
            apiKey = apiKeyStore.getApiKey()
        }
    }

    fun updateApiKey(key: String) { apiKey = key }

    fun saveApiKey() {
        viewModelScope.launch { apiKeyStore.saveApiKey(apiKey) }
    }

    fun saveModel(selectedModel: String) {
        viewModelScope.launch { apiKeyStore.saveModel(selectedModel) }
    }
}
