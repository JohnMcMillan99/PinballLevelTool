package com.grok.pinlevel.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grok.pinlevel.data.DataStoreManager
import com.grok.pinlevel.data.LegHint
import com.grok.pinlevel.data.LevelGuideService
import com.grok.pinlevel.data.SensorMath
import com.grok.pinlevel.data.SensorRepository
import com.grok.pinlevel.model.MachineLevelConfig
import com.grok.pinlevel.model.MachineProfile
import com.grok.pinlevel.model.defaultMachines
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject

data class LevelState(
    val pitch: Double = 0.0,
    val roll: Double = 0.0,
    val pitchOffset: Double = 0.0,
    val rollOffset: Double = 0.0,
    val targetAngle: Double = 6.5,
    val targetRoll: Double = 0.0,
    val pitchTolerance: Double = 0.5,
    val rollTolerance: Double = 0.5,
    val machineLevelConfigs: Map<String, MachineLevelConfig> = emptyMap(),
    val currentMachine: MachineProfile? = null,
    val machines: List<MachineProfile> = defaultMachines,
    val isVoiceGuideEnabled: Boolean = false,
    val voiceGuideIntervalSeconds: Int = 4,
    val glassOffsetEnabled: Boolean = false,
    val glassOffsetDegrees: Double = 8.5
)

class LevelViewModel(application: Application) : AndroidViewModel(application) {

    val sensorRepository = SensorRepository(application)
    private val dataStoreManager = DataStoreManager(application)
    private val appContext = application.applicationContext

    private val _state = MutableStateFlow(LevelState())
    val state: StateFlow<LevelState> = _state.asStateFlow()

    private var tts: TextToSpeech? = null
    private var ttsJob: Job? = null

    val currentVoiceHint: LegHint?
        get() {
            val s = _state.value
            return LevelGuideService.getNextLegHint(
                s.pitch, s.roll, s.targetAngle,
                s.targetRoll, s.pitchTolerance, s.rollTolerance
            )
        }

    init {
        sensorRepository.start()

        viewModelScope.launch {
            combine(
                dataStoreManager.pitchOffset,
                dataStoreManager.rollOffset
            ) { p, r -> Pair(p, r) }.collect { (pOff, rOff) ->
                _state.update { it.copy(pitchOffset = pOff, rollOffset = rOff) }
            }
        }

        viewModelScope.launch {
            sensorRepository.accelFlow.collect { accel ->
                val s = _state.value
                val tilt = SensorMath.computeTilt(
                    accel.x, accel.y, accel.z,
                    s.pitchOffset, s.rollOffset
                )
                val glassAdj = if (s.glassOffsetEnabled) s.glassOffsetDegrees else 0.0
                _state.update {
                    it.copy(pitch = tilt.pitch - glassAdj, roll = tilt.roll)
                }
            }
        }

        viewModelScope.launch {
            dataStoreManager.lastMachineId.collect { id ->
                if (id.isNotEmpty()) {
                    val machine = _state.value.machines.find { it.id == id }
                    if (machine != null) {
                        val config = _state.value.machineLevelConfigs[machine.id]
                            ?: MachineLevelConfig(targetPitch = machine.targetAngle)
                        _state.update {
                            it.copy(
                                currentMachine = machine,
                                targetAngle = config.targetPitch,
                                targetRoll = config.targetRoll,
                                pitchTolerance = config.pitchTolerance,
                                rollTolerance = config.rollTolerance
                            )
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            dataStoreManager.machineOverrides.collect { json ->
                if (json != "{}") {
                    try {
                        val jsonObj = Json.parseToJsonElement(json).jsonObject
                        val configs = mutableMapOf<String, MachineLevelConfig>()
                        jsonObj.forEach { (id, elem) ->
                            configs[id] = when {
                                elem is JsonPrimitive && elem.doubleOrNull != null ->
                                    MachineLevelConfig(targetPitch = elem.doubleOrNull!!)
                                else -> Json.decodeFromJsonElement(elem)
                            }
                        }
                        val updated = _state.value.machines.map { m ->
                            configs[m.id]?.let { c -> m.copy(targetAngle = c.targetPitch) } ?: m
                        }
                        _state.update { s ->
                            val current = s.currentMachine
                            val currentConfig = current?.let { configs[it.id] }
                            s.copy(
                                machines = updated,
                                machineLevelConfigs = configs,
                                targetAngle = currentConfig?.targetPitch ?: s.targetAngle,
                                targetRoll = currentConfig?.targetRoll ?: s.targetRoll,
                                pitchTolerance = currentConfig?.pitchTolerance ?: s.pitchTolerance,
                                rollTolerance = currentConfig?.rollTolerance ?: s.rollTolerance
                            )
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        }

        viewModelScope.launch {
            dataStoreManager.voiceGuideEnabled.collect { enabled ->
                _state.update { it.copy(isVoiceGuideEnabled = enabled) }
                if (enabled) startTtsLoop() else stopTtsLoop()
            }
        }

        viewModelScope.launch {
            dataStoreManager.voiceGuideIntervalSeconds.collect { interval ->
                _state.update { it.copy(voiceGuideIntervalSeconds = interval) }
            }
        }

        viewModelScope.launch {
            combine(
                dataStoreManager.glassOffsetEnabled,
                dataStoreManager.glassOffsetDegrees
            ) { enabled, degrees -> Pair(enabled, degrees) }.collect { (enabled, degrees) ->
                _state.update {
                    it.copy(glassOffsetEnabled = enabled, glassOffsetDegrees = degrees)
                }
            }
        }
    }

    fun toggleVoiceGuide() {
        val newValue = !_state.value.isVoiceGuideEnabled
        _state.update { it.copy(isVoiceGuideEnabled = newValue) }
        viewModelScope.launch {
            dataStoreManager.saveVoiceGuideEnabled(newValue)
        }
        if (newValue) {
            startTtsLoop()
        } else {
            stopTtsLoop()
        }
    }

    fun setVoiceGuideIntervalSeconds(seconds: Int) {
        val clamped = seconds.coerceIn(5, 60)
        _state.update { it.copy(voiceGuideIntervalSeconds = clamped) }
        viewModelScope.launch {
            dataStoreManager.saveVoiceGuideIntervalSeconds(clamped)
        }
        if (_state.value.isVoiceGuideEnabled) {
            stopTtsLoop()
            startTtsLoop()
        }
    }

    private fun startTtsLoop() {
        ttsJob?.cancel()
        if (tts == null) {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS && _state.value.isVoiceGuideEnabled) {
                    ttsJob = viewModelScope.launch {
                        while (_state.value.isVoiceGuideEnabled) {
                            val hint = currentVoiceHint
                            if (hint != null) {
                                tts?.speak(hint.message, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                            delay(_state.value.voiceGuideIntervalSeconds * 1000L)
                        }
                    }
                }
            }
        } else {
            ttsJob = viewModelScope.launch {
                while (_state.value.isVoiceGuideEnabled) {
                    val hint = currentVoiceHint
                    if (hint != null) {
                        tts?.speak(hint.message, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                    delay(_state.value.voiceGuideIntervalSeconds * 1000L)
                }
            }
        }
    }

    private fun stopTtsLoop() {
        ttsJob?.cancel()
        ttsJob = null
    }

    fun selectMachine(machine: MachineProfile) {
        val config = _state.value.machineLevelConfigs[machine.id]
            ?: MachineLevelConfig(targetPitch = machine.targetAngle)
        _state.update {
            it.copy(
                currentMachine = machine,
                targetAngle = config.targetPitch,
                targetRoll = config.targetRoll,
                pitchTolerance = config.pitchTolerance,
                rollTolerance = config.rollTolerance
            )
        }
        viewModelScope.launch {
            dataStoreManager.saveLastMachineId(machine.id)
        }
    }

    fun setTargetAngle(angle: Double) {
        _state.update { it.copy(targetAngle = angle) }
    }

    fun updateMachineLevelConfig(machineId: String, config: MachineLevelConfig) {
        val configs = _state.value.machineLevelConfigs.toMutableMap()
        configs[machineId] = config
        val updated = _state.value.machines.map {
            if (it.id == machineId) it.copy(targetAngle = config.targetPitch) else it
        }
        _state.update { s ->
            val current = s.currentMachine
            s.copy(
                machineLevelConfigs = configs,
                machines = updated,
                currentMachine = if (current?.id == machineId) current.copy(targetAngle = config.targetPitch) else current,
                targetAngle = if (s.currentMachine?.id == machineId) config.targetPitch else s.targetAngle,
                targetRoll = if (s.currentMachine?.id == machineId) config.targetRoll else s.targetRoll,
                pitchTolerance = if (s.currentMachine?.id == machineId) config.pitchTolerance else s.pitchTolerance,
                rollTolerance = if (s.currentMachine?.id == machineId) config.rollTolerance else s.rollTolerance
            )
        }
        viewModelScope.launch {
            dataStoreManager.saveMachineOverrides(Json.encodeToString(configs))
        }
    }

    fun updateMachineAngle(machineId: String, angle: Double) {
        val existing = _state.value.machineLevelConfigs[machineId]
            ?: MachineLevelConfig()
        updateMachineLevelConfig(machineId, existing.copy(targetPitch = angle))
    }

    fun calibrateFlat() {
        val accel = sensorRepository.accelFlow.value
        val raw = SensorMath.computeTilt(accel.x, accel.y, accel.z)
        _state.update {
            it.copy(
                pitchOffset = raw.pitch,
                rollOffset = raw.roll
            )
        }
        viewModelScope.launch {
            dataStoreManager.saveCalibrationOffset(raw.pitch, raw.roll)
        }
    }

    fun cancelCalibration() {}

    fun setGlassOffsetEnabled(enabled: Boolean) {
        _state.update { it.copy(glassOffsetEnabled = enabled) }
        viewModelScope.launch {
            dataStoreManager.saveGlassOffsetEnabled(enabled)
        }
    }

    fun setGlassOffsetDegrees(degrees: Double) {
        val clamped = degrees.coerceIn(5.0, 12.0)
        _state.update { it.copy(glassOffsetDegrees = clamped) }
        viewModelScope.launch {
            dataStoreManager.saveGlassOffsetDegrees(clamped)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorRepository.stop()
        stopTtsLoop()
        tts?.shutdown()
        tts = null
    }
}
