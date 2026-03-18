package app.pinbuddy.lvl.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SensorRepository(context: Context) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    data class AccelReading(
        val x: Float = 0f,
        val y: Float = 0f,
        val z: Float = 9.81f
    )

    private val _accelFlow = MutableStateFlow(AccelReading())
    val accelFlow: StateFlow<AccelReading> = _accelFlow.asStateFlow()

    val hasAccelerometer: Boolean get() = accelerometer != null

    private val alpha = 0.15f

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { e ->
            if (e.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val prev = _accelFlow.value
                _accelFlow.value = AccelReading(
                    x = prev.x + alpha * (e.values[0] - prev.x),
                    y = prev.y + alpha * (e.values[1] - prev.y),
                    z = prev.z + alpha * (e.values[2] - prev.z)
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
