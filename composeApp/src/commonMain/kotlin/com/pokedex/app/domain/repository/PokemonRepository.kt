package com.pokedex.app.domain.repository

import com.pokedex.app.domain.model.Pokemon
import com.pokedex.app.domain.model.PokemonDetail
import com.pokedex.app.presentation.screens.team.TeamMember
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {
    suspend fun getPokemonList(
        limit: Int,
        offset: Int,
        searchQuery: String?,
        typeFilter: String?
    ): List<Pokemon>

    suspend fun getPokemonDetail(id: Int): PokemonDetail

    suspend fun addToTeam(
        pokemon: PokemonDetail,
        location: String,
        latitude: Double? = null,
        longitude: Double? = null,
        photoPath: String? = null
    )
    fun getTeam(): Flow<List<TeamMember>>
    suspend fun removeFromTeam(id: Int)
}