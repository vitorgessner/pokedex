package com.pokedex.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NominatimResponse(
    val address: NominatimAddress? = null,
    val display_name: String? = null
)

@Serializable
data class NominatimAddress(
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val suburb: String? = null,
    val city_district: String? = null,
    val road: String? = null,
    val state: String? = null
) {
    fun getBestLocationName(): String {
        return city ?: town ?: village ?: suburb ?: city_district ?: "Local Desconhecido"
    }
}
