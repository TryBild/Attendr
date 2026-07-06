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
    private val USER_KIND = stringPreferencesKey(Constants.USER_KIND_KEY)
    private val COMPANY_NAME = stringPreferencesKey(Constants.COMPANY_NAME_KEY)
    private val SETUP_COMPLETE = stringPreferencesKey(Constants.SETUP_COMPLETE_KEY)
    private val ORG_ID = stringPreferencesKey(Constants.ORG_ID_KEY)
    private val EMPLOYEE_NAME = stringPreferencesKey("employee_name")
    private val PHOTO_URL = stringPreferencesKey(Constants.PHOTO_URL_KEY)

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN] }
    val deviceId: Flow<String?> = context.dataStore.data.map { it[DEVICE_ID] }
    val userKind: Flow<String?> = context.dataStore.data.map { it[USER_KIND] }
    val companyName: Flow<String?> = context.dataStore.data.map { it[COMPANY_NAME] }
    val setupComplete: Flow<Boolean> = context.dataStore.data.map { it[SETUP_COMPLETE] == "true" }
    val orgId: Flow<String?> = context.dataStore.data.map { it[ORG_ID] }
    val employeeName: Flow<String?> = context.dataStore.data.map { it[EMPLOYEE_NAME] }
    val photoUrl: Flow<String?> = context.dataStore.data.map { it[PHOTO_URL] }

    suspend fun saveToken(token: String) { context.dataStore.edit { it[TOKEN] = token } }
    suspend fun saveDeviceId(id: String) { context.dataStore.edit { it[DEVICE_ID] = id } }
    suspend fun saveUserKind(kind: String) { context.dataStore.edit { it[USER_KIND] = kind } }
    suspend fun saveCompanyName(name: String) { context.dataStore.edit { it[COMPANY_NAME] = name } }
    suspend fun saveSetupComplete(complete: Boolean) { context.dataStore.edit { it[SETUP_COMPLETE] = complete.toString() } }
    suspend fun saveOrgId(id: String) { context.dataStore.edit { it[ORG_ID] = id } }
    suspend fun saveEmployeeName(name: String) { context.dataStore.edit { it[EMPLOYEE_NAME] = name } }
    suspend fun savePhotoUrl(url: String) { context.dataStore.edit { it[PHOTO_URL] = url } }
    suspend fun clearToken() {
        context.dataStore.edit {
            it.remove(TOKEN)
            it.remove(USER_KIND)
            it.remove(COMPANY_NAME)
            it.remove(SETUP_COMPLETE)
            it.remove(ORG_ID)
            it.remove(EMPLOYEE_NAME)
            it.remove(PHOTO_URL)
        }
    }
}
