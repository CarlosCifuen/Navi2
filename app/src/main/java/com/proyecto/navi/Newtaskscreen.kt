package com.proyecto.navi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
 * Pantalla de detalle (no top-level): se empuja encima del back stack desde el
 * FAB de Tareas. Sirve para demostrar la diferencia entre *cambiar de pestaña*
 * (reemplaza el stack) y *navegar hacia adentro* (agrega al stack).
 *
 * Mientras esta pantalla está arriba, NaviApp oculta la bottom bar y el botón
 * atrás del sistema la cierra automáticamente vía NavDisplay.onBack.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskScreen(onClose: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva tarea", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cerrar"
                        )
                    }
                },
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