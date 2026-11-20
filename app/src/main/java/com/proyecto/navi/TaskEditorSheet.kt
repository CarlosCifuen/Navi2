package com.proyecto.navi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.proyecto.navi.data.Task

/**
 * El tray de abajo, para crear o editar. Lo usan Tareas y Calendario, por eso
 * vive aparte: así los dos abren exactamente el mismo formulario.
 *
 * task == null  -> tarea nueva.
 * task != null  -> edición de esa tarea; onDelete debe venir para poder eliminarla.
 *
 * ModalBottomSheet ya trae el scrim y la animación de subir desde abajo.
 * skipPartiallyExpanded evita el estado "a medias", y fillMaxHeight(0.67f) lo
 * fija en 2/3 de pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorSheet(
    task: Task?,
    categories: List<String>,
    onSave: (NewTaskInput) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Box(modifier = Modifier.fillMaxHeight(0.67f)) {
            NewTaskScreen(
                task = task,
                categories = categories,
                onSave = { input ->
                    onSave(input)
                    onDismiss()
                },
                onDelete = if (onDelete != null) {
                    {
                        onDelete()
                        onDismiss()
                    }
                } else {
                    null
                },
                onClose = onDismiss
            )
        }
    }
}
