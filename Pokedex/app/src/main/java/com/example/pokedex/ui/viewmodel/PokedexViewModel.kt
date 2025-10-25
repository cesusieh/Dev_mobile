package com.example.pokedex.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.PokemonRepository
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.data.models.PokemonTeam
import kotlinx.coroutines.launch

class PokedexViewModel(private val repository: PokemonRepository = PokemonRepository()) : ViewModel() {

    val pokemonList = mutableStateListOf<Pokemon>()
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    val currentPage = mutableStateOf(1)
    private val pageSize = 4
    val totalPokemonCount = mutableStateOf(0)

    val teams = mutableStateListOf<PokemonTeam>()
    val currentTeamName = mutableStateOf("")
    val currentTeamMembers = mutableStateListOf<Pokemon>()

    init {
        fetchPokemonList()
        loadTeams()
    }


    fun fetchPokemonList() {
        if (isLoading.value) return

        isLoading.value = true
        errorMessage.value = null
        viewModelScope.launch {
            try {
                val offset = (currentPage.value - 1) * pageSize
                val response = repository.getPokemonList(limit = pageSize, offset = offset)

                pokemonList.clear()
                pokemonList.addAll(response.results)
                totalPokemonCount.value = response.count

            } catch (e: Exception) {
                errorMessage.value = "Erro ao carregar Pokémon: ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    fun goToNextPage() {
        val maxPages = (totalPokemonCount.value + pageSize - 1) / pageSize
        if (currentPage.value < maxPages) {
            currentPage.value++
            fetchPokemonList()
        }
    }

    fun goToPreviousPage() {
        if (currentPage.value > 1) {
            currentPage.value--
            fetchPokemonList()
        }
    }

    private fun loadTeams() {
        teams.clear()
        teams.addAll(repository.pokemonTeams)
    }

    fun addTeam() {
        if (currentTeamName.value.isNotBlank()) {
            val newTeam = PokemonTeam(name = currentTeamName.value, members = currentTeamMembers.toMutableList())
            repository.addTeam(newTeam)
            currentTeamName.value = ""
            currentTeamMembers.clear()
            loadTeams()
        }
    }

    fun updateTeam(updatedTeam: PokemonTeam) {
        val index = teams.indexOfFirst { it.id == updatedTeam.id }
        if (index != -1) {
            if (updatedTeam.members.isEmpty()) {
                deleteTeam(updatedTeam.id)
            } else {
                teams.removeAt(index)
                teams.add(index, updatedTeam)
                repository.updateTeam(updatedTeam)
            }
        }
    }

    fun deleteTeam(teamId: String) {
        repository.deleteTeam(teamId)
        loadTeams()
    }

    fun addPokemonToCurrentTeam(pokemon: Pokemon) {
        if (!currentTeamMembers.contains(pokemon) && currentTeamMembers.size < 6) {
            currentTeamMembers.add(pokemon)
        }
    }

    fun removePokemonFromCurrentTeam(pokemon: Pokemon) {
        currentTeamMembers.remove(pokemon)
    }
    fun addMarkedPokemonToExistingTeam(teamId: String) {
        val teamToUpdate = teams.find { it.id == teamId }
        teamToUpdate?.let { existingTeam ->
            val updatedMembers = existingTeam.members.toMutableList()
            var changed = false

            currentTeamMembers.forEach { pokemonToAdd ->
                if (!updatedMembers.contains(pokemonToAdd) && updatedMembers.size < 6) {
                    updatedMembers.add(pokemonToAdd)
                    changed = true
                }
            }

            if (changed) {
                val updatedTeam = existingTeam.copy(members = updatedMembers)
                updateTeam(updatedTeam)
                currentTeamMembers.clear()
            }
        }
    }
}