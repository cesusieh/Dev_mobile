package com.example.pokedex.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.ui.viewmodel.PokedexViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokedexScreen(
    viewModel: PokedexViewModel = viewModel(),
    onNavigateToTeamManagement: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pokedex") },
                actions = {
                    IconButton(onClick = onNavigateToTeamManagement) {
                        Icon(Icons.Default.Add, contentDescription = "Gerenciar Times")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (viewModel.isLoading.value) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (viewModel.errorMessage.value != null) {
                Text(
                    text = viewModel.errorMessage.value ?: "Erro desconhecido",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.pokemonList) { pokemon ->
                        val isSelected = viewModel.currentTeamMembers.contains(pokemon)
                        PokemonCard(pokemon = pokemon, isSelected = isSelected) { clickedPokemon ->
                            if (isSelected) {
                                viewModel.removePokemonFromCurrentTeam(clickedPokemon)
                            } else {
                                viewModel.addPokemonToCurrentTeam(clickedPokemon)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentPageNumber = viewModel.currentPage.value
                    val totalPokemon = viewModel.totalPokemonCount.value
                    val pageSize = 4
                    val maxPages = (totalPokemon + pageSize - 1) / pageSize

                    Button(
                        onClick = { viewModel.goToPreviousPage() },
                        enabled = currentPageNumber > 1 && !viewModel.isLoading.value
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Página Anterior")
                        Text("Anterior")
                    }

                    Button(
                        onClick = { viewModel.goToNextPage() },
                        enabled = currentPageNumber < maxPages && !viewModel.isLoading.value
                    ) {
                        Text("Próximo")
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Próxima Página")
                    }
                }
            }
        }
    }
}

@Composable
fun PokemonCard(pokemon: Pokemon, isSelected: Boolean, onClick: (Pokemon) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(pokemon) },
        colors = if (isSelected) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        else CardDefaults.cardColors(),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.error) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = pokemon.getImageUrl(),
                contentDescription = pokemon.name,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = pokemon.name.capitalize(Locale.ROOT),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}