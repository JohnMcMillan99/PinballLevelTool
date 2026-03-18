package com.grok.pinlevel.data

import kotlin.math.atan2
import kotlin.math.sqrt

object SensorMath {

    data class TiltReading(
        val pitch: Double,
        val roll: Double
    )

    fun computeTilt(
        accelX: Float,
        accelY: Float,
        accelZ: Float,
        pitchOffset: Double = 0.0,
        rollOffset: Double = 0.0
    ): TiltReading {
        val pitch = Math.toDegrees(
            atan2(
                accelY.toDouble(),
                sqrt((accelX * accelX + accelZ * accelZ).toDouble())
            )
        )
        val roll = Math.toDegrees(
            atan2(-accelX.toDouble(), accelZ.toDouble())
        )
        return TiltReading(
            pitch = pitch - pitchOffset,
            roll = roll - rollOffset
        )
    }
}
