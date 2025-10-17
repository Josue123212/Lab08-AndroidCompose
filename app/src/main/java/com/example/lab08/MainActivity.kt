package com.example.lab08

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room
import kotlinx.coroutines.launch
import com.example.lab08.ui.theme.Lab08Theme

class MainActivity : ComponentActivity() {
    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab08Theme {
                val db = Room.databaseBuilder(
                    applicationContext,
                    TaskDatabase::class.java,
                    "task_db"
                ).build()


                val taskDao = db.taskDao()
                val viewModel = TaskViewModel(taskDao)


                TaskScreen(viewModel)
            }
        }
    }
}

@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var editedDescription by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var newTaskDescription by remember { mutableStateOf("") }

    val visibleTasks = tasks.filter { it.description.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Tareas", style = MaterialTheme.typography.titleLarge)
                        Text("Hoy", style = MaterialTheme.typography.labelSmall)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(imageVector = androidx.compose.material.icons.Icons.Filled.Add, contentDescription = "Agregar tarea")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Barra de búsqueda
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar tareas") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lista estilo Google Tasks
            androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxSize()) {
                androidx.compose.foundation.lazy.items(visibleTasks) { task ->
                    TaskItem(
                        task = task,
                        onToggle = { viewModel.toggleTaskCompletion(task) },
                        onEdit = {
                            editingTask = task
                            editedDescription = task.description
                        },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }
    }

    // Diálogo para añadir tarea (activado por FAB)
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (newTaskDescription.isNotBlank()) {
                        viewModel.addTask(newTaskDescription)
                        newTaskDescription = ""
                        showAddDialog = false
                    }
                }) { Text("Agregar") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
            },
            title = { Text("Nueva tarea") },
            text = {
                TextField(
                    value = newTaskDescription,
                    onValueChange = { newTaskDescription = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }

    // Diálogo para editar descripción
    if (editingTask != null) {
        AlertDialog(
            onDismissRequest = { editingTask = null },
            confirmButton = {
                TextButton(onClick = {
                    val t = editingTask!!
                    viewModel.editTask(t, editedDescription)
                    editingTask = null
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { editingTask = null }) { Text("Cancelar") }
            },
            title = { Text("Editar tarea") },
            text = {
                TextField(
                    value = editedDescription,
                    onValueChange = { editedDescription = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}

@Composable
fun TaskItem(
    task: Task,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Checkbox(checked = task.isCompleted, onCheckedChange = { onToggle() })
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(task.description)
                    Text(
                        text = if (task.isCompleted) "Completada" else "Pendiente",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Filled.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Filled.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

