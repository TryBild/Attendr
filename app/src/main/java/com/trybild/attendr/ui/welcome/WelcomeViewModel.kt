package com.trybild.attendr.ui.welcome

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trybild.attendr.data.local.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val LANG_KEY = stringPreferencesKey("selected_language")

// Only list languages with real, working translations. No values-*/ locale folders exist yet
// (only default English values/strings.xml) and setLanguage() below doesn't switch locale at
// all — it just stores a display label. Full i18n is deferred; don't offer options that do nothing.
val SUPPORTED_LANGUAGES = listOf("English")

class WelcomeViewModel(app: Application) : AndroidViewModel(app) {
    private val store = app.dataStore

    private val _language = MutableStateFlow("English")
    val language: StateFlow<String> = _language

    init {
        viewModelScope.launch {
            store.data.map { it[LANG_KEY] ?: "English" }.collect { _language.value = it }
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            store.edit { it[LANG_KEY] = lang }
        }
    }
}
