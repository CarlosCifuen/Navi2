package com.proyecto.navi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Contenido del tray de "Nueva tarea": no es un destino de navegación, se
 * muestra dentro de un ModalBottomSheet desde TasksScreen.kt. Cerrar el sheet
 * (deslizar hacia abajo, tocar el scrim o el botón atrás del sistema) llama a
 * onClose, que en TasksScreen simplemente pone showNewTaskSheet en false.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskScreen(onClose: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva tarea", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            Text(
                "Aquí va el formulario de la tarea.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, name = "Nueva tarea - Modo claro")
@Composable
private fun NewTaskScreenPreview() {
    NaviTheme(darkTheme = false) { NewTaskScreen(onClose = {}) }
}