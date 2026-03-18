package com.grok.pinlevel.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grok.pinlevel.data.DataStoreManager
import com.grok.pinlevel.data.SensorMath
import com.grok.pinlevel.data.SensorRepository
import com.grok.pinlevel.model.MachineProfile
import com.grok.pinlevel.model.defaultMachines
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class LevelState(
    val pitch: Double = 0.0,
    val roll: Double = 0.0,
    val pitchOffset: Double = 0.0,
    val rollOffset: Double = 0.0,
    val targetAngle: Double = 6.5,
    val currentMachine: MachineProfile? = null,
    val machines: List<MachineProfile> = defaultMachines,
    val isCalibrating: Boolean = false,
    val calibrationStep: Int = 0,
    val flatPitch: Double = 0.0,
    val flatRoll: Double = 0.0
)

class LevelViewModel(application: Application) : AndroidViewModel(application) {

    val sensorRepository = SensorRepository(application)
    private val dataStoreManager = DataStoreManager(application)

    private val _state = MutableStateFlow(LevelState())
    val state: StateFlow<LevelState> = _state.asStateFlow()

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
                _state.update { it.copy(pitch = tilt.pitch, roll = tilt.roll) }
            }
        }

        viewModelScope.launch {
            dataStoreManager.lastMachineId.collect { id ->
                if (id.isNotEmpty()) {
                    val machine = _state.value.machines.find { it.id == id }
                    if (machine != null) {
                        _state.update {
                            it.copy(currentMachine = machine, targetAngle = machine.targetAngle)
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            dataStoreManager.machineOverrides.collect { json ->
                if (json != "{}") {
                    try {
                        val overrides: Map<String, Double> = Json.decodeFromString(json)
                        val updated = _state.value.machines.map { m ->
                            overrides[m.id]?.let { angle -> m.copy(targetAngle = angle) } ?: m
                        }
                        _state.update { it.copy(machines = updated) }
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    fun selectMachine(machine: MachineProfile) {
        _state.update { it.copy(currentMachine = machine, targetAngle = machine.targetAngle) }
        viewModelScope.launch {
            dataStoreManager.saveLastMachineId(machine.id)
        }
    }

    fun setTargetAngle(angle: Double) {
        _state.update { it.copy(targetAngle = angle) }
    }

    fun updateMachineAngle(machineId: String, angle: Double) {
        val updated = _state.value.machines.map {
            if (it.id == machineId) it.copy(targetAngle = angle) else it
        }
        _state.update { s ->
            val current = s.currentMachine
            s.copy(
                machines = updated,
                currentMachine = if (current?.id == machineId) current.copy(targetAngle = angle) else current,
                targetAngle = if (s.currentMachine?.id == machineId) angle else s.targetAngle
            )
        }
        viewModelScope.launch {
            val overrides = updated.associate { it.id to it.targetAngle }
            dataStoreManager.saveMachineOverrides(Json.encodeToString(overrides))
        }
    }

    fun startCalibration() {
        _state.update { it.copy(isCalibrating = true, calibrationStep = 1) }
    }

    fun captureFlat() {
        val accel = sensorRepository.accelFlow.value
        val raw = SensorMath.computeTilt(accel.x, accel.y, accel.z)
        _state.update {
            it.copy(flatPitch = raw.pitch, flatRoll = raw.roll, calibrationStep = 2)
        }
    }

    fun captureFlipped() {
        val accel = sensorRepository.accelFlow.value
        val raw = SensorMath.computeTilt(accel.x, accel.y, accel.z)
        val s = _state.value
        val pitchOff = (s.flatPitch + raw.pitch) / 2.0
        val rollOff = (s.flatRoll + raw.roll) / 2.0
        _state.update {
            it.copy(
                pitchOffset = pitchOff,
                rollOffset = rollOff,
                isCalibrating = false,
                calibrationStep = 0
            )
        }
        viewModelScope.launch {
            dataStoreManager.saveCalibrationOffset(pitchOff, rollOff)
        }
    }

    fun cancelCalibration() {
        _state.update { it.copy(isCalibrating = false, calibrationStep = 0) }
    }

    override fun onCleared() {
        super.onCleared()
        sensorRepository.stop()
    }
}
