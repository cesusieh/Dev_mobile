package com.example.telausuario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// ---------------------------
// Room entity, DAO, Database
// ---------------------------
@Entity(tableName = "clients")
data class Client(
    @PrimaryKey val id: Long,
    val name: String,
    val email: String
)

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY id")
    suspend fun getAll(): List<Client>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(client: Client)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clients: List<Client>)

    @Query("DELETE FROM clients")
    suspend fun clear()
}

@Database(entities = [Client::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
}

// ---------------------------
// Retrofit API (example)
// ---------------------------
// We'll use https://jsonplaceholder.typicode.com/users as a simple example API
data class RemoteUser(val id: Int, val name: String, val email: String)

interface ApiService {
    @GET("/users")
    suspend fun getUsers(): List<RemoteUser>
}

fun createRetrofitService(): ApiService {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    return retrofit.create(ApiService::class.java)
}

// ---------------------------
// Main Activity with Compose
// ---------------------------
class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize Room DB
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "clients-db")
            .fallbackToDestructiveMigration()
            .build()

        // initialize Retrofit
        apiService = createRetrofitService()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ClientApp(db.clientDao(), apiService)
                }
            }
        }
    }
}

// ---------------------------
// Composable UI and logic
// ---------------------------
@Composable
fun ClientApp(dao: ClientDao, api: ApiService) {
    val coroutineScope = rememberCoroutineScope()

    var clients by remember { mutableStateOf(listOf<Client>()) }
    var loading by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var idText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    // load from DB on composition
    LaunchedEffect(Unit) {
        loading = true
        withContext(Dispatchers.IO) {
            // If DB empty, prepopulate with 20 clients
            val existing = dao.getAll()
            if (existing.isEmpty()) {
                val pre = (1..20).map { i ->
                    Client(
                        id = i.toLong(),
                        name = "Cliente $i",
                        email = "cliente$i@example.com"
                    )
                }
                dao.insertAll(pre)
            }
            val after = dao.getAll()
            withContext(Dispatchers.Main) {
                clients = after
                loading = false
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Cadastro de Clientes (simples)")
        Spacer(Modifier.height(8.dp))

        // Form to add a client
        OutlinedTextField(
            value = idText,
            onValueChange = { idText = it.filter { ch -> ch.isDigit() } },
            label = { Text("ID (somente números)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row {
            Button(onClick = {
                val id = idText.toLongOrNull() ?: run {
                    message = "ID inválido"
                    return@Button
                }
                if (name.isBlank() || email.isBlank()) {
                    message = "Nome e email não podem estar vazios"
                    return@Button
                }
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        dao.insert(Client(id = id, name = name.trim(), email = email.trim()))
                        val updated = dao.getAll()
                        withContext(Dispatchers.Main) {
                            clients = updated
                            message = "Cliente salvo"
                            // clear form
                            idText = ""
                            name = ""
                            email = ""
                        }
                    }
                }
            }) {
                Text("Adicionar/Atualizar")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                // fetch from API and merge (demo)
                coroutineScope.launch {
                    loading = true
                    message = null
                    try {
                        val remote = withContext(Dispatchers.IO) { api.getUsers() }
                        val mapped = remote.mapIndexed { idx, ru ->
                            // map remote user to our Client model, ensure unique id (avoid collision: add 1000)
                            Client(id = (1000 + ru.id).toLong(), name = ru.name, email = ru.email)
                        }
                        withContext(Dispatchers.IO) {
                            dao.insertAll(mapped)
                            val updated = dao.getAll()
                            withContext(Dispatchers.Main) {
                                clients = updated
                                message = "Sincronizado com API (${mapped.size} registros adicionados/atualizados)"
                            }
                        }
                    } catch (e: Exception) {
                        message = "Erro ao buscar API: ${e.message}"
                    } finally {
                        loading = false
                    }
                }
            }) {
                Text("Sincronizar API")
            }
        }

        Spacer(Modifier.height(8.dp))
        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
        }

        message?.let {
            Text(it)
            Spacer(Modifier.height(8.dp))
        }

        Divider()
        Spacer(Modifier.height(8.dp))

        Text("Clientes (${clients.size}):")
        Spacer(Modifier.height(6.dp))

        // Simple listing
        clients.forEach { c ->
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)) {
                Column(Modifier.padding(8.dp)) {
                    Text("ID: ${c.id}")
                    Text(c.name)
                    Text(c.email)
                }
            }
        }
    }
}

