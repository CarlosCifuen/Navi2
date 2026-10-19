package com.proyecto.navi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Contenido del tray de "Nueva tarea".
 *
 * No es un destino de navegación ni una pantalla completa: se muestra dentro
 * del ModalBottomSheet de TasksScreen. Por eso NO lleva Scaffold ni TopAppBar.
 *
 * Scaffold es un contenedor de pantalla: aplica window insets, reserva espacio
 * para FAB y snackbar, y con fillMaxSize() pelea contra el tamaño que el sheet
 * ya le impuso. Encima, ModalBottomSheet dibuja su propio drag handle arriba,
 * así que un TopAppBar debajo duplica el mismo trabajo visual.
 */
@Composable
fun NewTaskScreen(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            stringResource(R.string.nueva_tarea),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Placeholder: se borra cuando exista el formulario, por eso no se
        // extrajo a strings.xml.
        Text(
            "Aquí va el formulario de la tarea.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Cierra el tray desde adentro. Antes onClose se recibía como parámetro
        // pero no se invocaba en ningún lado (quedó huérfano al quitar la flecha
        // atrás del TopAppBar), así que la única forma de cerrar era deslizar o
        // tocar el scrim. "Guardar" entra aquí cuando haya campos que guardar.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.cancelar))
            }
        }
    }
}

@Preview(showBackground = true, name = "Nueva tarea - Modo claro")
@Composable
private fun NewTaskScreenPreview() {
    NaviTheme(darkTheme = false) { NewTaskScreen(onClose = {}) }
}