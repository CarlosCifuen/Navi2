package com.proyecto.navi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyecto.navi.data.Task
import com.proyecto.navi.data.TaskTag
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla de Calendario: cuadrícula del mes + lista de actividades del día
 * elegido. Las tareas salen de Room (la misma tabla que Tareas), así que una
 * tarea nueva o completada se refleja aquí sin código extra.
 *
 * Misma separación que TasksScreen: esta función conecta el ViewModel, y la
 * de abajo es sin estado para poder usarla en los @Preview.
 */
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CalendarScreen(
        uiState = uiState,
        onPreviousMonth = { viewModel.changeMonth(-1) },
        onNextMonth = { viewModel.changeMonth(1) },
        onSelectDay = viewModel::selectDay
    )
}

@Composable
fun CalendarScreen(
    uiState: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDay: (DayKey) -> Unit
) {
    // Sin topBar, igual que Tareas: Scaffold ya reserva el inset de la barra
    // de estado, así que el contenido no queda debajo de ella.
    Scaffold { innerPadding ->
        // Todo en un solo LazyColumn: la pantalla completa se desplaza si la
        // lista del día es larga, y las actividades usan key estable (task.id).
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "month_header") {
                MonthHeader(
                    month = uiState.visibleMonth,
                    onPrevious = onPreviousMonth,
                    onNext = onNextMonth
                )
            }

            item(key = "month_grid") {
                MonthGrid(cells = uiState.cells, onSelectDay = onSelectDay)
            }

            item(key = "legend") { Legend() }

            item(key = "day_title") {
                SelectedDayHeader(
                    day = uiState.selected,
                    count = uiState.selectedTasks.size
                )
            }

            if (uiState.selectedTasks.isEmpty()) {
                item(key = "empty") {
                    Text(
                        stringResource(R.string.sin_actividades),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(uiState.selectedTasks, key = { it.id }) { task ->
                ActivityCard(task)
            }
        }
    }
}

@Composable
private fun MonthHeader(month: DayKey, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.mes_anterior)
            )
        }
        Text(
            month.format("MMMM yyyy"),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNext) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.mes_siguiente)
            )
        }
    }
}

@Composable
private fun MonthGrid(cells: List<DayCell>, onSelectDay: (DayKey) -> Unit) {
    val weekdayInitials = stringArrayResource(R.array.dias_semana_inicial)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                weekdayInitials.forEach { initial ->
                    Text(
                        initial,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 6 filas de 7. Se arma con Row y weight(1f) en vez de
            // LazyVerticalGrid: son siempre 42 celdas fijas y una lista Lazy
            // dentro de otra Lazy (esta pantalla ya es un LazyColumn) no
            // puede medirse sin altura acotada.
            cells.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { cell ->
                        DayCellView(
                            cell = cell,
                            onClick = { onSelectDay(cell.date) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCellView(cell: DayCell, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme

    val background = when {
        cell.isSelected -> scheme.primary
        cell.hasTasks -> scheme.primaryContainer
        else -> Color.Transparent
    }
    val textColor = when {
        cell.isSelected -> scheme.onPrimary
        !cell.inMonth -> scheme.onSurface.copy(alpha = 0.35f)
        cell.hasTasks -> scheme.onPrimaryContainer
        else -> scheme.onSurface
    }
    val dotColor = if (cell.isSelected) scheme.onPrimary else scheme.primary

    Box(
        modifier = modifier.height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(background)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                cell.date.day.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
            if (cell.hasTasks) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 5.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }
    }
}

@Composable
private fun Legend() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(MaterialTheme.colorScheme.primaryContainer, stringResource(R.string.dia_con_tareas))
        LegendItem(MaterialTheme.colorScheme.primary, stringResource(R.string.dia_seleccionado))
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SelectedDayHeader(day: DayKey, count: Int) {
    Column {
        Text(
            day.format("EEEE, d 'de' MMMM"),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            // plurals y no "$count actividades": en 1 debe decir "actividad".
            pluralStringResource(R.plurals.actividades, count, count),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Una actividad del día. La barra y la hora toman el color de la etiqueta
 * (el mismo que el chip en Tareas), así una Clase se ve azul en ambas pantallas.
 */
@Composable
private fun ActivityCard(task: Task) {
    val (tagBg, tagText) = task.tag.colors()
    val time = SimpleDateFormat("HH:mm", Locale.forLanguageTag("es")).format(Date(task.dueAt))
    val kind =
        if (task.isDaily) stringResource(R.string.habito_diario) else task.tag.label()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(tagBg)
        )
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                time,
                modifier = Modifier.width(56.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = tagText
            )
            Column {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )
                Text(
                    "${task.category} · $kind",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** "julio 2026", "jueves, 24 de julio"... capitalizado, en español. */
private fun DayKey.format(pattern: String): String {
    val locale = Locale.forLanguageTag("es")
    return SimpleDateFormat(pattern, locale)
        .format(Date(toCalendar().timeInMillis))
        .replaceFirstChar { it.uppercase(locale) }
}

/** Datos SOLO para los previews: nunca llegan al dispositivo. */
private fun previewState(): CalendarUiState {
    val today = todayKey()
    val now = System.currentTimeMillis()
    val day = 24L * 60 * 60 * 1000
    val tasks = listOf(
        Task("p1", "Programación móvil", "Universidad", TaskTag.CLASE, now, false, false, ""),
        Task("p2", "Idear prompt", "Universidad", TaskTag.IDEA, now + 2 * day, false, false, ""),
        Task("p3", "Comida de Laika", "Casa", TaskTag.HABITO, now, true, false, ""),
        Task("p4", "Lavar la ropa", "Casa", TaskTag.CASA, now + 4 * day, false, true, "")
    )
    return buildCalendarState(tasks, selected = today, visibleMonth = today.copy(day = 1))
}

@Preview(showBackground = true, name = "Calendario - Modo claro")
@Composable
private fun CalendarScreenLightPreview() {
    NaviTheme(darkTheme = false) {
        CalendarScreen(previewState(), onPreviousMonth = {}, onNextMonth = {}, onSelectDay = {})
    }
}

@Preview(
    showBackground = true,
    name = "Calendario - Modo oscuro",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CalendarScreenDarkPreview() {
    NaviTheme(darkTheme = true) {
        CalendarScreen(previewState(), onPreviousMonth = {}, onNextMonth = {}, onSelectDay = {})
    }
}
