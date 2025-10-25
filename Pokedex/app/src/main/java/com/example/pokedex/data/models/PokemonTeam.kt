package com.example.pokedex.data.models

data class PokemonTeam(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String,
    val members: MutableList<Pokemon> = mutableListOf()
)