package com.leo.painelnotificacoes.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "painel_notificacoes_settings",
)

class SettingsRepository(private val context: Context) {

    val retentionDays: Flow<Int> = context.settingsDataStore.data.map { prefs ->
        prefs[RETENTION_DAYS_KEY] ?: DEFAULT_RETENTION_DAYS
    }

    suspend fun setRetentionDays(days: Int) {
        context.settingsDataStore.edit { prefs ->
            prefs[RETENTION_DAYS_KEY] = days.coerceIn(1, 365)
        }
    }

    /** Null when unset, so callers can treat "no key" and "blank key" the same way. */
    val geminiApiKey: Flow<String?> = context.settingsDataStore.data.map { prefs ->
        prefs[GEMINI_API_KEY_KEY]?.takeIf { it.isNotBlank() }
    }

    suspend fun setGeminiApiKey(key: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[GEMINI_API_KEY_KEY] = key.trim()
        }
    }

    /** When the very first notification was ever captured, so the user can see "collecting since". */
    val firstCaptureTimestamp: Flow<Long?> = context.settingsDataStore.data.map { prefs ->
        prefs[FIRST_CAPTURE_TIMESTAMP_KEY]
    }

    /** Only lowers the stored value — safe to call on every capture, including out-of-order catch-up backfills. */
    suspend fun recordFirstCaptureIfEarlier(timestampMillis: Long) {
        context.settingsDataStore.edit { prefs ->
            val current = prefs[FIRST_CAPTURE_TIMESTAMP_KEY]
            if (current == null || timestampMillis < current) {
                prefs[FIRST_CAPTURE_TIMESTAMP_KEY] = timestampMillis
            }
        }
    }

    companion object {
        const val DEFAULT_RETENTION_DAYS = 30
        private val RETENTION_DAYS_KEY = intPreferencesKey("retention_days")
        private val GEMINI_API_KEY_KEY = stringPreferencesKey("gemini_api_key")
        private val FIRST_CAPTURE_TIMESTAMP_KEY = longPreferencesKey("first_capture_timestamp")
    }
}
