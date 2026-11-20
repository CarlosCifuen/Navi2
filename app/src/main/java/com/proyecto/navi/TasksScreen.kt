package com.proyecto.navi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyecto.navi.data.Task
import com.proyecto.navi.data.TaskTag

/**
 * Pantalla principal de Tareas (LazyColumn agrupado por categoría).
 *
 * Ya no tiene datos propios: el modelo Task vive en data/, los datos salen de
 * Room y el filtrado ocurre en el ViewModel. Esta función solo pinta estado y
 * reporta intenciones hacia arriba.
 *
 * Por qué LazyColumn y no un grid: cada tarea es una fila de información
 * textual (título, categoría, hora, prioridad) que el usuario objetivo
 * -estudiantes y trabajadores organizando su semana- necesita leer de
 * arriba hacia abajo, comparando urgencia y hora.
 */
@Composable
fun TasksScreen(
    viewModel: TasksViewModel = viewModel(factory = TasksViewModel.Factory)
) {
    // collectAsStateWithLifecycle y no collectAsState: deja de recolectar
    // cuando la pantalla pasa a segundo plano, en vez de seguir trabajando.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TasksScreen(
        uiState = uiState,
        onSelectFilter = viewModel::setFilter,
        onToggleCompleted = viewModel::toggleCompleted,
        onAddTask = viewModel::addTask
    )
}

/**
 * Versión sin estado: recibe todo lo que pinta y reporta lo que el usuario hace.
 *
 * La separación existe para que los @Preview sigan funcionando. Un preview no
 * puede construir un ViewModel que abre Room, así que le pasa un TasksUiState
 * armado a mano y listo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    uiState: TasksUiState,
    onSelectFilter: (TaskFilter) -> Unit,
    onToggleCompleted: (Task) -> Unit,
    onAddTask: (NewTaskInput) -> Unit
) {
    var showNewTaskSheet by remember { mutableStateOf(false) }

    Scaffold(
        // Sin topBar: Tareas ya no tiene barra superior, para mostrar más contenido.
        // Sin bottomBar: la barra la provee NaviApp para las 3 pantallas top-level.
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewTaskSheet = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.nueva_tarea))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "summary") {
                WeeklySummaryCard(
                    completed = uiState.completedCount,
                    total = uiState.totalCount
                )
            }

            item(key = "filters") {
                FilterRow(
                    selected = uiState.filter,
                    onSelect = onSelectFilter
                )
            }

            uiState.grouped.forEach { (category, tasks) ->
                item(key = "header_$category") {
                    Text(
                        category.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = categoryColor(category),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
                items(tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggleCompleted = { onToggleCompleted(task) }
                    )
                }
            }

            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    // El tray de "Nueva tarea": ModalBottomSheet ya trae el scrim y la animación
    // de subir desde abajo, así que no hay que escribir ninguna animación a mano.
    if (showNewTaskSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showNewTaskSheet = false },
            sheetState = sheetState
        ) {
            Box(modifier = Modifier.fillMaxHeight(0.67f)) {
                NewTaskScreen(
                    categories = uiState.categories,
                    onSave = { input ->
                        onAddTask(input)
                        showNewTaskSheet = false
                    },
                    onClose = { showNewTaskSheet = false }
                )
            }
        }
    }
}

@Composable
private fun WeeklySummaryCard(completed: Int, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("$completed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                stringResource(R.string.esta_semana),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.tareas_completadas, completed, total),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilterRow(selected: TaskFilter, onSelect: (TaskFilter) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TaskFilter.entries.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onSelect(option) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    stringResource(option.labelRes),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * Datos de ejemplo SOLO para los previews. Los datos reales salen de Room
 * (ver data/SeedData.kt); esto nunca llega al dispositivo.
 */
private fun previewUiState(): TasksUiState {
    val now = System.currentTimeMillis()
    val tasks = listOf(
        Task("p1", "Entregar wireframes", "Universidad", TaskTag.ALTA, now, false, false, "", priority = 3),
        Task("p2", "Programación móvil", "Universidad", TaskTag.CLASE, now, false, true, "", priority = 2),
        Task("p3", "Regar las plantas", "Casa", TaskTag.HABITO, now, true, false, "", priority = 1)
    )
    return TasksUiState(
        grouped = tasks.groupBy { it.category },
        filter = TaskFilter.TODAS,
        completedCount = tasks.count { it.isCompleted },
        totalCount = tasks.size,
        categories = listOf("Casa", "Universidad")
    )
}

@Preview(showBackground = true, name = "Tareas - Modo claro")
@Composable
private fun TasksScreenLightPreview() {
    NaviTheme(darkTheme = false) {
        TasksScreen(previewUiState(), onSelectFilter = {}, onToggleCompleted = {}, onAddTask = {})
    }
}

@Preview(
    showBackground = true,
    name = "Tareas - Modo oscuro",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun TasksScreenDarkPreview() {
    NaviTheme(darkTheme = true) {
        TasksScreen(previewUiState(), onSelectFilter = {}, onToggleCompleted = {}, onAddTask = {})
    }
}
