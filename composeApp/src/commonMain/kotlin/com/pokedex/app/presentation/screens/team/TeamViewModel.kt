package com.pokedex.app.presentation.screens.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokedex.app.domain.model.Pokemon
import com.pokedex.app.domain.model.PokemonDetail
import com.pokedex.app.domain.repository.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeamMember(
    val pokemon: com.pokedex.app.domain.model.Pokemon,
    val capturedLocation: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photoPath: String? = null
)
sealed interface TeamUiState {
    data class Success(val pokemons: List<TeamMember>) : TeamUiState
}

class TeamViewModel(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TeamUiState>(TeamUiState.Success(emptyList()))
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getTeam().collect { members ->
                _uiState.update { TeamUiState.Success(members) }
            }
        }
    }


    fun addToTeam(
        pokemon: PokemonDetail,
        capturedLocation: String,
        latitude: Double? = null,
        longitude: Double? = null,
        photoPath: String? = null
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TeamUiState.Success) {
                val alreadyIn = currentState.pokemons.any { it.pokemon.id == pokemon.id }
                val isFull = currentState.pokemons.size >= 6

                if (alreadyIn || isFull) return@launch
            }
            repository.addToTeam(pokemon, capturedLocation, latitude, longitude, photoPath)
        }
    }

    fun removeFromTeam(pokemonId: Int) {
        viewModelScope.launch {
            repository.removeFromTeam(pokemonId)
        }
    }
}
