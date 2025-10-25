package com.example.pokedex.data.models

data class Pokemon(
    val name: String,
    val url: String
) {
    fun getImageUrl(): String {
        val id = url.split("/").dropLast(1).last()
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
    }
}