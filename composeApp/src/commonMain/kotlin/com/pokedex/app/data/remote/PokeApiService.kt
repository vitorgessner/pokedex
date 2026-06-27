package com.pokedex.app.data.remote

import com.pokedex.app.data.remote.dto.PokemonDetailDto
import com.pokedex.app.data.remote.dto.PokemonListResponseDto
import com.pokedex.app.data.remote.dto.PokemonSpeciesDto
import com.pokedex.app.data.remote.dto.NominatimResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header

private const val BASE_URL = "https://pokeapi.co/api/v2"
private const val NOMINATIM_URL = "https://nominatim.openstreetmap.org/reverse"

class PokeApiService {

    private val client = buildHttpClient()

    suspend fun getPokemonList(limit: Int, offset: Int): PokemonListResponseDto =
        client.get("$BASE_URL/pokemon?limit=$limit&offset=$offset").body()

    suspend fun getPokemonDetail(id: Int): PokemonDetailDto =
        client.get("$BASE_URL/pokemon/$id").body()

    suspend fun getPokemonSpecies(id: Int): PokemonSpeciesDto =
        client.get("$BASE_URL/pokemon-species/$id").body()

    suspend fun reverseGeocode(lat: Double, lon: Double): NominatimResponse =
        client.get("$NOMINATIM_URL?format=jsonv2&lat=$lat&lon=$lon") {
            header("User-Agent", "PokedexApp/1.0")
        }.body()
}
