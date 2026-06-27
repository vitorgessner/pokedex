package com.pokedex.app.di

import com.pokedex.app.data.local.database.AppDatabase
import com.pokedex.app.data.local.database.getDatabase
import com.pokedex.app.data.local.database.getDatabaseBuilder
import com.pokedex.app.data.remote.PokeApiService
import com.pokedex.app.data.repository.PokemonRepositoryImpl
import com.pokedex.app.domain.repository.PokemonRepository
import com.pokedex.app.domain.storage.ImageStorage
import com.pokedex.app.domain.usecase.GetPokemonDetailUseCase
import com.pokedex.app.domain.usecase.GetPokemonListUseCase

object AppModule {
    
    private val database: AppDatabase by lazy {
        getDatabase(getDatabaseBuilder())
    }

    private val apiService: PokeApiService by lazy {
        PokeApiService()
    }

    val imageStorage: ImageStorage by lazy {
        com.pokedex.app.data.local.database.getImageStorage()
    }

    val pokemonRepository: PokemonRepository by lazy {
        PokemonRepositoryImpl(
            api = apiService,
            pokemonDao = database.pokemonDao(),
            teamDao = database.teamDao()
        )
    }

    val getPokemonList: GetPokemonListUseCase by lazy {
        GetPokemonListUseCase(pokemonRepository)
    }

    val getPokemonDetail: GetPokemonDetailUseCase by lazy {
        GetPokemonDetailUseCase(pokemonRepository)
    }
}