package com.example.pokedex.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pokedex.ui.screens.PokedexScreen
import com.example.pokedex.ui.screens.TeamManagementScreen
import com.example.pokedex.ui.viewmodel.PokedexViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

sealed class Screen(val route: String) {
    object PokedexList : Screen("pokedex_list")
    object TeamManagement : Screen("team_management")
}

@Composable
fun PokedexApp() {
    val navController = rememberNavController()
    val pokedexViewModel: PokedexViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.PokedexList.route) {
        composable(Screen.PokedexList.route) {
            PokedexScreen(
                viewModel = pokedexViewModel,
                onNavigateToTeamManagement = { navController.navigate(Screen.TeamManagement.route) }
            )
        }
        composable(Screen.TeamManagement.route) {
            TeamManagementScreen(
                viewModel = pokedexViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}