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
    val coroutineScope = rememberCoroutineScope()
    var newTaskDescription by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var editedDescription by remember { mutableStateOf("") }

    // Filtra por búsqueda simple
    val visibleTasks = tasks.filter { it.description.contains(searchQuery, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Barra de búsqueda
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar tareas") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Nueva tarea
        TextField(
            value = newTaskDescription,
            onValueChange = { newTaskDescription = it },
            label = { Text("Nueva tarea") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (newTaskDescription.isNotEmpty()) {
                    viewModel.addTask(newTaskDescription)
                    newTaskDescription = ""
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Agregar tarea")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de tareas con acciones: completar, editar y eliminar
        visibleTasks.forEach { task ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = task.description)
                Row {
                    Button(onClick = { viewModel.toggleTaskCompletion(task) }) {
                        Text(if (task.isCompleted) "Completada" else "Pendiente")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        editingTask = task
                        editedDescription = task.description
                    }) {
                        Text("Editar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { viewModel.deleteTask(task) }) {
                        Text("Eliminar")
                    }
                }
            }
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

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { coroutineScope.launch { viewModel.deleteAllTasks() } },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Eliminar todas las tareas")
        }
    }
}

