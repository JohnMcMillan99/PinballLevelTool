package com.grok.pinlevel.data

import kotlin.math.abs

data class LegHint(
    val leg: String,
    val direction: String,
    val message: String
)

object LevelGuideService {

    fun getNextLegHint(
        pitch: Double,
        roll: Double,
        targetAngle: Double,
        targetRoll: Double = 0.0,
        pitchTolerance: Double = 0.5,
        rollTolerance: Double = 0.5
    ): LegHint? {
        val pitchError = pitch - targetAngle
        val rollError = roll - targetRoll

        val pitchOff = abs(pitchError) > pitchTolerance
        val rollOff = abs(rollError) > rollTolerance

        if (!pitchOff && !rollOff) return null

        val flErr = (-pitchError + rollError) / 2.0
        val frErr = (-pitchError - rollError) / 2.0
        val rlErr = (pitchError + rollError) / 2.0
        val rrErr = (pitchError - rollError) / 2.0

        val legs = listOf("FL" to flErr, "FR" to frErr, "RL" to rlErr, "RR" to rrErr)
        val sorted = legs.sortedByDescending { abs(it.second) }
        val worst = sorted[0]

        val message = when {
            pitchOff && !rollOff -> buildPitchMessage(pitchError)
            !pitchOff && rollOff -> buildRollMessage(rollError)
            else -> buildCombinedMessage(sorted)
        }

        val (leg, error) = worst
        val direction = if (error > 0) "lower" else "raise"
        return LegHint(leg = leg, direction = direction, message = message)
    }

    private fun buildPitchMessage(pitchError: Double): String {
        return if (pitchError > 0)
            "Raise the front legs or lower the back legs"
        else
            "Lower the front legs or raise the back legs"
    }

    private fun buildRollMessage(rollError: Double): String {
        return if (rollError > 0)
            "Lower the left legs or raise the right legs"
        else
            "Lower the right legs or raise the left legs"
    }

    private fun buildCombinedMessage(
        sorted: List<Pair<String, Double>>
    ): String {
        val (worstLeg, worstErr) = sorted[0]
        val (secondLeg, secondErr) = sorted[1]

        val shareRow = worstLeg[0] == secondLeg[0]
        val shareCol = worstLeg[1] == secondLeg[1]
        val sameSign = (worstErr > 0) == (secondErr > 0)

        if ((shareRow || shareCol) && sameSign) {
            val dir = if (worstErr > 0) "Lower" else "Raise"
            val altDir = if (worstErr > 0) "raise" else "lower"

            val (pairDesc, altDesc) = when {
                shareRow && worstLeg.startsWith("F") -> "both front legs" to "both back legs"
                shareRow -> "both back legs" to "both front legs"
                shareCol && worstLeg.endsWith("L") -> "both left legs" to "both right legs"
                else -> "both right legs" to "both left legs"
            }
            return "$dir $pairDesc or $altDir $altDesc"
        }

        val worstDir = if (worstErr > 0) "Lower" else "Raise"
        val worstName = legToName(worstLeg)
        val secondDir = if (secondErr > 0) "lower" else "raise"
        val secondName = legToName(secondLeg)
        return "$worstDir the $worstName leg, then $secondDir the $secondName leg"
    }

    private fun legToName(leg: String): String = when (leg) {
        "FL" -> "front left"
        "FR" -> "front right"
        "RL" -> "rear left"
        "RR" -> "rear right"
        else -> leg.lowercase()
    }
}
