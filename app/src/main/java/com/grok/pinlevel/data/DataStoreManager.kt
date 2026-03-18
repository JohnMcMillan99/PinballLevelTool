package com.grok.pinlevel.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pinlevel_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        val PITCH_OFFSET = doublePreferencesKey("pitch_offset")
        val ROLL_OFFSET = doublePreferencesKey("roll_offset")
        val LAST_MACHINE_ID = stringPreferencesKey("last_machine_id")
        val MACHINE_OVERRIDES = stringPreferencesKey("machine_overrides")
        val VOICE_GUIDE_ENABLED = booleanPreferencesKey("voice_guide_enabled")
        val VOICE_GUIDE_INTERVAL_SECONDS = stringPreferencesKey("voice_guide_interval_seconds")
        val GLASS_OFFSET_ENABLED = booleanPreferencesKey("glass_offset_enabled")
        val GLASS_OFFSET_DEGREES = doublePreferencesKey("glass_offset_degrees")
    }

    val pitchOffset: Flow<Double> =
        context.dataStore.data.map { it[PITCH_OFFSET] ?: 0.0 }

    val rollOffset: Flow<Double> =
        context.dataStore.data.map { it[ROLL_OFFSET] ?: 0.0 }

    val lastMachineId: Flow<String> =
        context.dataStore.data.map { it[LAST_MACHINE_ID] ?: "" }

    val machineOverrides: Flow<String> =
        context.dataStore.data.map { it[MACHINE_OVERRIDES] ?: "{}" }

    val voiceGuideEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[VOICE_GUIDE_ENABLED] ?: false }

    val voiceGuideIntervalSeconds: Flow<Int> =
        context.dataStore.data.map {
            (it[VOICE_GUIDE_INTERVAL_SECONDS] ?: "4").toIntOrNull()?.coerceIn(5, 60) ?: 5
        }

    val glassOffsetEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[GLASS_OFFSET_ENABLED] ?: false }

    val glassOffsetDegrees: Flow<Double> =
        context.dataStore.data.map { it[GLASS_OFFSET_DEGREES] ?: 8.5 }

    suspend fun saveCalibrationOffset(pitchOffset: Double, rollOffset: Double) {
        context.dataStore.edit { prefs ->
            prefs[PITCH_OFFSET] = pitchOffset
            prefs[ROLL_OFFSET] = rollOffset
        }
    }

    suspend fun saveLastMachineId(id: String) {
        context.dataStore.edit { prefs ->
            prefs[LAST_MACHINE_ID] = id
        }
    }

    suspend fun saveMachineOverrides(json: String) {
        context.dataStore.edit { prefs ->
            prefs[MACHINE_OVERRIDES] = json
        }
    }

    suspend fun saveVoiceGuideEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[VOICE_GUIDE_ENABLED] = enabled
        }
    }

    suspend fun saveVoiceGuideIntervalSeconds(seconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[VOICE_GUIDE_INTERVAL_SECONDS] = seconds.coerceIn(5, 60).toString()
        }
    }

    suspend fun saveGlassOffsetEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[GLASS_OFFSET_ENABLED] = enabled
        }
    }

    suspend fun saveGlassOffsetDegrees(degrees: Double) {
        context.dataStore.edit { prefs ->
            prefs[GLASS_OFFSET_DEGREES] = degrees.coerceIn(5.0, 12.0)
        }
    }
}
