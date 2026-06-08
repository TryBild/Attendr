package com.trybild.attendr.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.trybild.attendr.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "attendr_prefs")

class TokenDataStore(private val context: Context) {
    private val TOKEN = stringPreferencesKey(Constants.TOKEN_KEY)
    private val DEVICE_ID = stringPreferencesKey(Constants.DEVICE_ID_KEY)

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN] }
    val deviceId: Flow<String?> = context.dataStore.data.map { it[DEVICE_ID] }

    suspend fun saveToken(token: String) { context.dataStore.edit { it[TOKEN] = token } }
    suspend fun saveDeviceId(id: String) { context.dataStore.edit { it[DEVICE_ID] = id } }
    suspend fun clearToken() { context.dataStore.edit { it.remove(TOKEN) } }
}
