import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.util.UUID

data class Movie(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var description: String
)

object MovieRepository {
    val movies = mutableStateListOf<Movie>().apply {
        add(Movie(name = "O Poderoso Chefão", description = "Um clássico do cinema sobre a máfia."))
        add(Movie(name = "Um Sonho de Liberdade", description = "A história de um homem inocente na prisão."))
        add(Movie(name = "Batman: O Cavaleiro das Trevas", description = "O Batman enfrenta o Coringa em Gotham."))
        add(Movie(name = "Pulp Fiction", description = "Várias histórias interligadas no submundo do crime."))
        add(Movie(name = "O Senhor dos Anéis: A Sociedade do Anel", description = "A jornada para destruir o Um Anel."))
        add(Movie(name = "Forrest Gump: O Contador de Histórias", description = "A vida extraordinária de um homem simples."))
        add(Movie(name = "A Origem", description = "Um ladrão que rouba informações do subconsciente."))
        add(Movie(name = "Matrix", description = "Um programador descobre a verdade sobre sua realidade."))
        add(Movie(name = "Interestelar", description = "Uma equipe de exploradores viaja pelo espaço."))
        add(Movie(name = "Clube da Luta", description = "Um homem insone e um vendedor de sabão formam um clube."))
    }

    fun addMovie(movie: Movie) {
        movies.add(movie)
    }

    fun updateMovie(updatedMovie: Movie) {
        val index = movies.indexOfFirst { it.id == updatedMovie.id }
        if (index != -1) {
            movies[index] = updatedMovie
        }
    }

    fun deleteMovie(movie: Movie) {
        movies.remove(movie)
    }

    fun getMovieById(id: String?): Movie? {
        return movies.find { it.id == id }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieApp()
        }
    }
}

@Composable
fun MovieApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "movieList") {
        composable("movieList") {
            MovieListScreen(navController = navController)
        }
        composable("movieDetail/{movieId}") { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")
            MovieDetailScreen(navController = navController, movieId = movieId)
        }
        composable("addMovie") {
            AddMovieScreen(navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MovieListScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Meus Filmes") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addMovie") }) {
                Icon(Icons.Filled.Add, "Adicionar Filme")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(MovieRepository.movies.chunked(4)) { movieChunk ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    movieChunk.forEach { movie ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable { navController.navigate("movieDetail/${movie.id}") },
                            elevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = movie.name, style = MaterialTheme.typography.h6)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = movie.description, style = MaterialTheme.typography.body2)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MovieDetailScreen(navController: NavController, movieId: String?) {
    val movie = remember(movieId) { MovieRepository.getMovieById(movieId) }
    var name by remember { mutableStateOf(movie?.name ?: "") }
    var description by remember { mutableStateOf(movie?.description ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Filme") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome do Filme") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        movie?.let {
                            it.name = name
                            it.description = description
                            MovieRepository.updateMovie(it)
                        }
                        navController.popBackStack()
                    },
                    enabled = name.isNotBlank() && description.isNotBlank()
                ) {
                    Text("Salvar")
                }
                Button(
                    onClick = {
                        movie?.let {
                            MovieRepository.deleteMovie(it)
                        }
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
                ) {
                    Icon(Icons.Filled.Delete, "Deletar Filme")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Deletar")
                }
            }
        }
    }
}

@Composable
fun AddMovieScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adicionar Filme") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome do Filme") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val newMovie = Movie(name = name, description = description)
                    MovieRepository.addMovie(newMovie)
                    navController.popBackStack()
                },
                enabled = name.isNotBlank() && description.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Adicionar Filme")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MovieApp()
}