package com.example.pokedex.data

import com.example.pokedex.api.PokeApiService
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.data.models.PokemonListResponse
import com.example.pokedex.data.models.PokemonTeam
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PokemonRepository {

    private val pokeApiService: PokeApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PokeApiService::class.java)
    }

    private val _pokemonTeams = mutableListOf<PokemonTeam>()
    val pokemonTeams: List<PokemonTeam> get() = _pokemonTeams

    suspend fun getPokemonList(limit: Int, offset: Int): PokemonListResponse {
        return pokeApiService.getPokemonList(limit, offset)
    }

    fun addTeam(team: PokemonTeam) {
        _pokemonTeams.add(team)
    }

    fun updateTeam(updatedTeam: PokemonTeam) {
        val index = _pokemonTeams.indexOfFirst { it.id == updatedTeam.id }
        if (index != -1) {
            _pokemonTeams[index] = updatedTeam
        }
    }

    fun deleteTeam(teamId: String) {
        _pokemonTeams.removeAll { it.id == teamId }
    }
}