package com.example.telalogin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.telalogin.ui.theme.TelaLoginTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TelaLoginTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavegacao()
                }
            }
        }
    }
}

@Composable
fun AppNavegacao() {
    val navController = rememberNavController()
    var corDeFundo by remember { mutableStateOf(Color(0xFFADD8E6)) }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            TelaLogin(navController = navController)
        }

        composable("boas_vindas/{nomeUsuario}") { backStackEntry ->
            val nome = backStackEntry.arguments?.getString("nomeUsuario") ?: "Usuário"
            TelaBoasVindas(navController = navController, nomeUsuario = nome, corDeFundo = corDeFundo)
        }

        composable("configuracoes") {
            TelaConfiguracoes(
                navController = navController,
                corAtual = corDeFundo,
                onCorSelecionada = { novaCor -> corDeFundo = novaCor }
            )
        }
    }
}

@Composable
fun TelaLogin(navController: NavController) {
    var nome by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Digite seu nome") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (nome.isNotBlank()) {
                navController.navigate("boas_vindas/$nome")
            }
        }) {
            Text("Entrar")
        }
    }
}

@Composable
fun TelaBoasVindas(navController: NavController, nomeUsuario: String, corDeFundo: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(corDeFundo)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bem-vindo(a),", fontSize = 22.sp)
        Text(nomeUsuario, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            navController.navigate("configuracoes")
        }) {
            Text("Ir para Configurações")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            navController.navigate("login") {
                popUpTo("login") {
                    inclusive = true
                }
            }
        }) {
            Text("Deslogar")
        }
    }
}

@Composable
fun TelaConfiguracoes(
    navController: NavController,
    corAtual: Color,
    onCorSelecionada: (Color) -> Unit
) {
    val cores = mapOf(
        "Azul Claro" to Color(0xFFADD8E6),
        "Verde Claro" to Color(0xFF90EE90),
        "Rosa Claro" to Color(0xFFFFB6C1)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Configurações", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Escolha uma cor de fundo:")
        Spacer(modifier = Modifier.height(16.dp))

        cores.forEach { (nome, cor) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                RadioButton(
                    selected = (cor == corAtual),
                    onClick = { onCorSelecionada(cor) }
                )
                Text(nome, modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = {
            navController.popBackStack()
        }) {
            Text("Salvar e Voltar")
        }
    }
}