package com.example.pokedex.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.data.models.PokemonTeam
import com.example.pokedex.ui.viewmodel.PokedexViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementScreen(
    viewModel: PokedexViewModel = viewModel(),
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar Times") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
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
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = viewModel.currentTeamName.value,
                onValueChange = { viewModel.currentTeamName.value = it },
                label = { Text("Nome do Time") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pokémon no novo Time: ${viewModel.currentTeamMembers.size}/6")
                Button(
                    onClick = { viewModel.addTeam() },
                    enabled = viewModel.currentTeamName.value.isNotBlank() && viewModel.currentTeamMembers.isNotEmpty()
                ) {
                    Text("Criar time")
                }
            }


            if (viewModel.currentTeamMembers.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(viewModel.currentTeamMembers) { pokemon ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(
                                model = pokemon.getImageUrl(),
                                contentDescription = pokemon.name,
                                modifier = Modifier.size(50.dp)
                            )
                            Text(pokemon.name.capitalize(Locale.ROOT), style = MaterialTheme.typography.bodySmall)
                            IconButton(onClick = { viewModel.removePokemonFromCurrentTeam(pokemon) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover")
                            }
                        }
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }


            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Seus Times",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))


            if (viewModel.teams.isEmpty()) {
                Text("Nenhum time criado ainda. Crie um acima!")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.teams, key = { it.id }) { team ->
                        TeamCard(
                            team = team,
                            onDeleteTeam = { viewModel.deleteTeam(it) },
                            onRemovePokemon = { teamId, pokemon ->
                                val teamToUpdate = viewModel.teams.find { it.id == teamId }
                                teamToUpdate?.let { existingTeam ->
                                    val updatedMembers = existingTeam.members.toMutableList()
                                    updatedMembers.remove(pokemon)
                                    val updatedTeam = existingTeam.copy(members = updatedMembers)
                                    viewModel.updateTeam(updatedTeam)
                                }
                            },

                            onAddMarkedPokemon = { teamId ->
                                viewModel.addMarkedPokemonToExistingTeam(teamId)
                            },

                            hasMarkedPokemon = viewModel.currentTeamMembers.isNotEmpty(),

                            canAddMorePokemon = team.members.size < 6
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamCard(
    team: PokemonTeam,
    onDeleteTeam: (String) -> Unit,
    onRemovePokemon: (String, Pokemon) -> Unit,

    onAddMarkedPokemon: (String) -> Unit,
    hasMarkedPokemon: Boolean,
    canAddMorePokemon: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = team.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {

                    if (hasMarkedPokemon && canAddMorePokemon) {
                        IconButton(onClick = { onAddMarkedPokemon(team.id) }) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar Pokémon Marcados", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else if (hasMarkedPokemon && !canAddMorePokemon) {

                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = {
                                PlainTooltip { Text("Time Cheio!") }
                            },
                            state = TODO(),
                            modifier = TODO(),
                            focusable = TODO(),
                            enableUserInput = TODO()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Time Cheio", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        }
                    }

                    IconButton(onClick = { onDeleteTeam(team.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Deletar Time")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (team.members.isEmpty()) {
                Text("Nenhum Pokémon neste time.")
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(team.members, key = { it.name }) { pokemon ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(
                                model = pokemon.getImageUrl(),
                                contentDescription = pokemon.name,
                                modifier = Modifier.size(60.dp)
                            )
                            Text(pokemon.name.capitalize(Locale.ROOT), style = MaterialTheme.typography.bodyMedium)
                            IconButton(onClick = { onRemovePokemon(team.id, pokemon) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover Pokémon do time", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}