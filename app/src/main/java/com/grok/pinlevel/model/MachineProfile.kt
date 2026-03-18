package com.grok.pinlevel.model

import kotlinx.serialization.Serializable

@Serializable
data class MachineProfile(
    val id: String,
    val name: String,
    val targetAngle: Double = 6.5
)

enum class Preset(val label: String, val angle: Double) {
    MODERN_SS("Modern SS", 6.5),
    EM_CLASSIC("EM/Classic", 4.0),
    COMPETITION("Competition", 7.0),
    CUSTOM("Custom", 6.5)
}

val defaultMachines = listOf(
    MachineProfile("addams_family", "The Addams Family"),
    MachineProfile("twilight_zone", "Twilight Zone"),
    MachineProfile("jurassic_park", "Jurassic Park (Stern)"),
    MachineProfile("godzilla", "Godzilla"),
    MachineProfile("mandalorian", "The Mandalorian"),
    MachineProfile("deadpool", "Deadpool"),
    MachineProfile("iron_maiden", "Iron Maiden"),
    MachineProfile("stranger_things", "Stranger Things"),
    MachineProfile("black_knight", "Black Knight"),
    MachineProfile("attack_mars", "Attack from Mars"),
    MachineProfile("medieval_madness", "Medieval Madness"),
    MachineProfile("monster_bash", "Monster Bash"),
    MachineProfile("simpsons_pinball", "The Simpsons Pinball Party"),
    MachineProfile("white_water", "White Water"),
    MachineProfile("funhouse", "Funhouse")
)
