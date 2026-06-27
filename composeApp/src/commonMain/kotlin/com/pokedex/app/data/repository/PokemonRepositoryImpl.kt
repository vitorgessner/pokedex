package com.pokedex.app.data.repository

import com.pokedex.app.data.local.dao.PokemonDao
import com.pokedex.app.data.local.dao.TeamDao
import com.pokedex.app.data.local.entity.PokemonEntity
import com.pokedex.app.data.local.entity.TeamEntity
import com.pokedex.app.data.remote.PokeApiService
import com.pokedex.app.domain.model.Pokemon
import com.pokedex.app.domain.model.PokemonDetail
import com.pokedex.app.domain.model.PokemonStat
import com.pokedex.app.domain.repository.PokemonRepository
import com.pokedex.app.presentation.screens.team.TeamMember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PokemonRepositoryImpl(
    private val api: PokeApiService,
    private val pokemonDao: PokemonDao,
    private val teamDao: TeamDao
) : PokemonRepository {

    override suspend fun getPokemonList(
        limit: Int,
        offset: Int,
        searchQuery: String?,
        typeFilter: String?
    ): List<Pokemon> = withContext(Dispatchers.IO) {
        if (pokemonDao.getCount() == 0) {
            syncInitialData()
        }

        val nameParam = if (searchQuery.isNullOrBlank()) null else searchQuery
        val typeParam = if (typeFilter.isNullOrBlank() || typeFilter == "All") null else typeFilter

        pokemonDao.getFilteredPokemons(
            limit = limit,
            offset = offset,
            name = nameParam,
            type = typeParam
        ).map { it.toDomain() }
    }

    private fun PokemonEntity.toDomain() = Pokemon(
        id = id,
        name = name,
        imageUrl = imageUrl,
        types = types.split(",").filter { it.isNotBlank() }
    )

    private fun TeamEntity.toDomain() = TeamMember(
        pokemon = Pokemon(
            id = id,
            name = name,
            imageUrl = imageUrl,
            types = types.split(",").filter { it.isNotBlank() }
        ),
        capturedLocation = capturedLocation,
        latitude = latitude,
        longitude = longitude,
        photoPath = photoPath
    )

    private suspend fun syncInitialData() {
        try {
            val response = api.getPokemonList(limit = 151, offset = 0)

            val entities = coroutineScope {
                response.results.map { item ->
                    async {
                        val id = extractIdFromUrl(item.url)
                        val detail = api.getPokemonDetail(id)
                        PokemonEntity(
                            id = id,
                            name = item.name.formatPokemonName(),
                            imageUrl = buildImageUrl(id),
                            types = detail.types
                                .sortedBy { it.slot }
                                .joinToString(",") { it.type.name }
                        )
                    }
                }.awaitAll()
            }
            pokemonDao.insertPokemons(entities)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun getPokemonDetail(id: Int): PokemonDetail = withContext(Dispatchers.IO) {
        val detail  = api.getPokemonDetail(id)
        val species = runCatching { api.getPokemonSpecies(id) }.getOrNull()

        val description = species?.flavorTextEntries
            ?.firstOrNull { it.language.name == "en" }
            ?.flavorText
            ?.cleanFlavorText()
            ?: "No description available."

        PokemonDetail(
            id          = detail.id,
            name        = detail.name.formatPokemonName(),
            imageUrl    = buildImageUrl(detail.id),
            types       = detail.types.sortedBy { it.slot }.map { it.type.name },
            stats       = detail.stats.map { PokemonStat(it.stat.name, it.baseStat) },
            description = description,
            heightM     = detail.height / 10f,
            weightKg    = detail.weight / 10f
        )
    }

    override suspend fun addToTeam(
        pokemon: PokemonDetail,
        location: String,
        latitude: Double?,
        longitude: Double?,
        photoPath: String?
    ) {
        val entity = TeamEntity(
            id = pokemon.id,
            name = pokemon.name,
            imageUrl = pokemon.imageUrl,
            types = pokemon.types.joinToString(","),
            capturedLocation = location,
            latitude = latitude,
            longitude = longitude,
            photoPath = photoPath
        )
        teamDao.insertTeamMember(entity)
    }

    override suspend fun removeFromTeam(id: Int) {
        teamDao.removeTeamMember(id)
    }

    override fun getTeam(): Flow<List<TeamMember>> {
        return teamDao.getAllTeamMembers().map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun getAddressFromCoords(lat: Double, lon: Double): String = withContext(Dispatchers.IO) {
        try {
            val response = api.reverseGeocode(lat, lon)
            response.address?.getBestLocationName() ?: "Local Desconhecido"
        } catch (e: Exception) {
            e.printStackTrace()
            "Local Desconhecido"
        }
    }

    private fun extractIdFromUrl(url: String): Int =
        url.trimEnd('/').substringAfterLast('/').toInt()

    private fun buildImageUrl(id: Int): String =
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"

    private fun String.formatPokemonName(): String =
        split("-").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

    private fun String.cleanFlavorText(): String =
        replace("\n", " ").replace("\u000c", " ").trim()
}
