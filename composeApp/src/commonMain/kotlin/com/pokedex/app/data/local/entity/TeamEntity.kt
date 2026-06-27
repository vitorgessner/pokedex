package com.pokedex.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "team")
data class TeamEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String,
    val capturedLocation: String,
    val types: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photoPath: String? = null
)