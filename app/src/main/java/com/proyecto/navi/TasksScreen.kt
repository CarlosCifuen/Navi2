package com.proyecto.navi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Modelo inmutable de una tarea. `id` es el identificador estable que se usa
 * como key en items(...) más abajo: nunca cambia aunque la tarea se reordene,
 * se filtre o se marque como completada, a diferencia del índice de la lista.
 */
data class Task(
    val id: String,
    val title: String,
    val category: String,
    val schedule: String,
    val tag: String,
    val tagColor: Color,
    val tagTextColor: Color,
    val isCompleted: Boolean,
    val avatarUrl: String
)

private enum class TaskFilter(val label: String) {
    TODAS("Todas"),
    HOY("Hoy"),
    PENDIENTES("Pendientes")
}

/**
 * Paleta de colores para las etiquetas (chips) de cada tarea, siguiendo la
 * misma idea de MintIconColors que ya se usa en SettingsScreen.kt.
 */
private object TagColors {
    val altaBg = Color(0xFFFBE3D0); val altaText = Color(0xFFB25E1E)
    val claseBg = Color(0xFFDCE7FB); val claseText = Color(0xFF2E5FAE)
    val ideaBg = Color(0xFFDDF3E8); val ideaText = Color(0xFF2F8A5B)
    val habitoBg = Color(0xFFDDF3E8); val habitoText = Color(0xFF2F8A5B)
    val casaBg = Color(0xFFFBE3D0); val casaText = Color(0xFFB25E1E)
}

/**
 * Datos de ejemplo en memoria (todavía sin ViewModel ni API real; eso llega
 * más adelante en el curso). Se preparan una sola vez fuera del Composable
 * del ítem para no repetir cálculos en cada recomposición.
 */
private fun sampleTasks(): List<Task> = listOf(
    Task("t1", "Tarea 1 de ecuaciones", "Universidad", "Hoy · 6:00 p. m.", "Alta", TagColors.altaBg, TagColors.altaText, true, "https://api.dicebear.com/9.x/shapes/png?seed=ecuaciones"),
    Task("t2", "Programación móvil", "Universidad", "Mañana · 10:00 a. m.", "Clase", TagColors.claseBg, TagColors.claseText, true, "https://api.dicebear.com/9.x/shapes/png?seed=movil"),
    Task("t3", "Idear prompt", "Universidad", "Viernes · 4:30 p. m.", "Idea", TagColors.ideaBg, TagColors.ideaText, false, "https://api.dicebear.com/9.x/shapes/png?seed=prompt"),
    Task("t4", "Entregar wireframes", "Universidad", "Hoy · 8:00 p. m.", "Alta", TagColors.altaBg, TagColors.altaText, false, "https://api.dicebear.com/9.x/shapes/png?seed=wireframes"),
    Task("t5", "Leer capítulo 3", "Universidad", "Lunes · 9:00 a. m.", "Clase", TagColors.claseBg, TagColors.claseText, false, "https://api.dicebear.com/9.x/shapes/png?seed=lectura"),
    Task("t6", "Preparar exposición", "Universidad", "Jueves · 2:00 p. m.", "Clase", TagColors.claseBg, TagColors.claseText, false, "https://api.dicebear.com/9.x/shapes/png?seed=exposicion"),
    Task("t7", "Comida de Laika", "Casa", "Todos los días · 7:00 p. m.", "Hábito", TagColors.habitoBg, TagColors.habitoText, false, "https://api.dicebear.com/9.x/shapes/png?seed=laika"),
    Task("t8", "Lavar la ropa", "Casa", "Sábado · 9:00 a. m.", "Casa", TagColors.casaBg, TagColors.casaText, true, "https://api.dicebear.com/9.x/shapes/png?seed=ropa"),
    Task("t9", "Regar las plantas", "Casa", "Hoy · 8:00 a. m.", "Hábito", TagColors.habitoBg, TagColors.habitoText, true, "https://api.dicebear.com/9.x/shapes/png?seed=plantas"),
    Task("t10", "Sacar la basura", "Casa", "Miércoles · 8:00 p. m.", "Casa", TagColors.casaBg, TagColors.casaText, false, "https://api.dicebear.com/9.x/shapes/png?seed=basura"),
    Task("t11", "Pagar servicios", "Casa", "Viernes · 5:00 p. m.", "Casa", TagColors.casaBg, TagColors.casaText, false, "https://api.dicebear.com/9.x/shapes/png?seed=servicios"),
    Task("t12", "Meditar 10 minutos", "Casa", "Todos los días · 7:00 a. m.", "Hábito", TagColors.habitoBg, TagColors.habitoText, true, "https://api.dicebear.com/9.x/shapes/png?seed=meditar")
)

/**
 * Pantalla principal de Tareas (LazyColumn agrupado por categoría).
 *
 * Por qué LazyColumn y no un grid: cada tarea es una fila de información
 * textual (título, categoría, hora, prioridad) que el usuario objetivo
 * -estudiantes y trabajadores organizando su semana- necesita leer de
 * arriba hacia abajo, comparando urgencia y hora. Con 12+ tareas esperadas
 * por semana, una lista vertical de una sola columna es más legible que un
 * grid, que serviría mejor para contenido visual (fotos, íconos grandes)
 * donde el orden de lectura importa menos que la exploración libre.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onAddTaskClick: () -> Unit = {},
    onNavigate: (NaviRoute) -> Unit = {}
) {
    val allTasks = remember { sampleTasks() }
    var filter by remember { mutableStateOf(TaskFilter.TODAS) }

    // Filtrado preparado antes de pasarlo a la lista, no dentro del ítem.
    val visibleTasks = remember(filter, allTasks) {
        when (filter) {
            TaskFilter.TODAS -> allTasks
            TaskFilter.HOY -> allTasks.filter { it.schedule.startsWith("Hoy") }
            TaskFilter.PENDIENTES -> allTasks.filter { !it.isCompleted }
        }
    }
    val grouped = remember(visibleTasks) { visibleTasks.groupBy { it.category } }
    val completedCount = remember(allTasks) { allTasks.count { it.isCompleted } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Tareas", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "Organiza tu semana sin perder el ritmo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notificaciones")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = { NaviBottomBar(selected = NaviRoute.TAREAS, onSelect = onNavigate) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva tarea")
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
                WeeklySummaryCard(completed = completedCount, total = allTasks.size)
            }

            item(key = "filters") {
                FilterRow(selected = filter, onSelect = { filter = it })
            }

            grouped.forEach { (category, tasks) ->
                item(key = "header_$category") {
                    Text(
                        category.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
                items(tasks, key = { it.id }) { task ->
                    TaskCard(task = task, onClick = { })
                }
            }

            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(72.dp)) }
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
            Text("Esta semana", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "$completed de $total tareas completadas",
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
                    option.label,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * Composable propio del ítem de la lista, separado de TasksScreen.
 * Es clicable (Modifier.clickable) y produce una acción observable via onClick.
 */
@Composable
private fun TaskCard(task: Task, onClick: (Task) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick(task) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = task.avatarUrl,
            contentDescription = null,
            placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))

        CompletionIndicator(isCompleted = task.isCompleted)
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                task.title,
                style = MaterialTheme.typography.titleMedium,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
            )
            Text(
                task.schedule,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(task.tagColor)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(task.tag, style = MaterialTheme.typography.labelMedium, color = task.tagTextColor)
        }
    }
}

@Composable
private fun CompletionIndicator(isCompleted: Boolean) {
    if (isCompleted) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Completada",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
        )
    }
}

@Preview(showBackground = true, name = "Tareas - Modo claro")
@Composable
private fun TasksScreenLightPreview() {
    NaviTheme(darkTheme = false) { TasksScreen() }
}

@Preview(
    showBackground = true,
    name = "Tareas - Modo oscuro",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun TasksScreenDarkPreview() {
    NaviTheme(darkTheme = true) { TasksScreen() }
}
