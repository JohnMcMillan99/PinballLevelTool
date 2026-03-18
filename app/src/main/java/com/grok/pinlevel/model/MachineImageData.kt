package com.grok.pinlevel.model

import com.grok.pinlevel.R

/**
 * Maps machine IDs to playfield drawable resource IDs.
 * Add real playfield images by placing PNG/JPEG in res/drawable/playfield_<machine_id>
 * and adding the mapping here. Use placeholder for machines without images.
 */
fun getPlayfieldDrawableId(machineId: String): Int {
    return when (machineId) {
        "addams_family" -> R.drawable.playfield_addams_family
        "twilight_zone" -> R.drawable.playfield_twilight_zone
        "jurassic_park" -> R.drawable.playfield_jurassic_park
        "godzilla" -> R.drawable.playfield_godzilla
        "mandalorian" -> R.drawable.playfield_mandalorian
        "deadpool" -> R.drawable.playfield_deadpool
        "iron_maiden" -> R.drawable.playfield_iron_maiden
        "stranger_things" -> R.drawable.playfield_stranger_things
        "black_knight" -> R.drawable.playfield_black_knight
        "attack_mars" -> R.drawable.playfield_attack_mars
        "medieval_madness" -> R.drawable.playfield_medieval_madness
        "monster_bash" -> R.drawable.playfield_monster_bash
        "simpsons_pinball" -> R.drawable.playfield_simpsons_pinball
        "white_water" -> R.drawable.playfield_whitewater_pinball
        "funhouse" -> R.drawable.playfield_funhouse
        "indiana_jones" -> R.drawable.playfield_indiana_jones
        "star_wars" -> R.drawable.playfield_star_wars
        "acdc" -> R.drawable.playfield_acdc
        "metallica" -> R.drawable.playfield_metallica
        "ghostbusters" -> R.drawable.playfield_ghostbusters
        "guardians" -> R.drawable.playfield_guardians
        "avengers" -> R.drawable.playfield_avengers
        "james_bond" -> R.drawable.playfield_james_bond
        "elvira" -> R.drawable.playfield_elvira
        "tron" -> R.drawable.playfield_tron
        else -> R.drawable.playfield_placeholder
    }
}
