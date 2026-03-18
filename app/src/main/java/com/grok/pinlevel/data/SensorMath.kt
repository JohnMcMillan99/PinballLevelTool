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
        // Roll: use atan2(Z,X)-90 so that flat (screen up, Z≈g) reads 0°.
        // atan2(-X,Z) gives 90° when flat on some devices due to axis orientation.
        val roll = Math.toDegrees(
            atan2(accelZ.toDouble(), accelX.toDouble())
        ) - 90.0
        return TiltReading(
            pitch = pitch - pitchOffset,
            roll = roll - rollOffset
        )
    }
}
