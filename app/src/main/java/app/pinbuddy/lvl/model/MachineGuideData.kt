package app.pinbuddy.lvl.model

data class MachineGuide(
    val skillShots: List<String>,
    val multiballTriggers: List<String>,
    val tips: List<String>
) {
    val isEmpty: Boolean
        get() = skillShots.isEmpty() && multiballTriggers.isEmpty() && tips.isEmpty()
}

val machineGuides = mapOf(
    "addams_family" to MachineGuide(
        skillShots = listOf(
            "Left ramp for Thing Flips",
            "Right ramp for Power",
            "Center shot to start modes"
        ),
        multiballTriggers = listOf(
            "Thing Multiball: Complete Thing targets",
            "Mansion Multiball: Light all mansion inserts",
            "Tour the Mansion: Hit swamp and vault"
        ),
        tips = listOf(
            "Focus on starting modes via center shot",
            "Thing Flips are key for scoring",
            "Save Power for multiball"
        )
    ),
    "twilight_zone" to MachineGuide(
        skillShots = listOf(
            "Camera skill shot",
            "Power field skill shot",
            "Slot machine skill shot"
        ),
        multiballTriggers = listOf(
            "Lost in the Zone: Collect 3 Zone letters",
            "Powerball Mania: Light power field",
            "Camera Multiball: Complete camera sequence"
        ),
        tips = listOf(
            "Slot machine adds valuable features",
            "Camera is central to progression",
            "Power field controls ball behavior"
        )
    ),
    "medieval_madness" to MachineGuide(
        skillShots = listOf(
            "Castle lock shot",
            "Moat shot",
            "Tower shot"
        ),
        multiballTriggers = listOf(
            "Siege Multiball: Lock 3 balls in castle",
            "Madness Multiball: Complete all kingdom modes",
            "Peasant Revolt: Hit peasants repeatedly"
        ),
        tips = listOf(
            "Destroy the castle for big points",
            "Trolls and peasants add variety",
            "Build toward Madness Multiball"
        )
    ),
    "attack_mars" to MachineGuide(
        skillShots = listOf(
            "Super jackpot ramp",
            "Mars ramp",
            "Center saucer"
        ),
        multiballTriggers = listOf(
            "Attack from Mars: Lock 3 balls",
            "Total Annihilation: Light all saucers",
            "Super jackpot: During multiball"
        ),
        tips = listOf(
            "Mars ramp is the main scoring path",
            "Saucer shots build super jackpot",
            "Destroy all cities for max score"
        )
    ),
    "deadpool" to MachineGuide(
        skillShots = listOf(
            "Left ramp for Mercs",
            "Right ramp for X-Force",
            "Spinner for combos"
        ),
        multiballTriggers = listOf(
            "Merc Madness: Complete mercenary missions",
            "X-Force Multiball: Light X-Force inserts",
            "Battle the Boss: Progress through story"
        ),
        tips = listOf(
            "Spinner builds combos quickly",
            "Merc missions unlock features",
            "Save multiball for boss battles"
        )
    )
)

fun getMachineGuide(machineId: String): MachineGuide? = machineGuides[machineId]

fun getMachineGuideOrEmpty(machineId: String): MachineGuide =
    machineGuides[machineId] ?: MachineGuide(emptyList(), emptyList(), emptyList())
