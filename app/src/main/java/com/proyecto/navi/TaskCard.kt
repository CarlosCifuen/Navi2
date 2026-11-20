package com.proyecto.navi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.proyecto.navi.data.Task

/**
 * Tarjeta de una tarea, compartida por Tareas y Calendario para que se vean
 * idénticas. El indicador de completada es clicable: ese toque escribe en
 * Room, y las dos pantallas se actualizan porque el DAO expone un Flow.
 *
 * El checkbox toma el color de la categoría (en vez de escribir su nombre);
 * la prioridad va al inicio de la línea de abajo ("!!! Hoy · 6:00 p. m.").
 */
@Composable
fun TaskCard(task: Task, onToggleCompleted: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompletionIndicator(
            isCompleted = task.isCompleted,
            accent = categoryColor(task.category),
            onClick = onToggleCompleted
        )
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                task.title,
                style = MaterialTheme.typography.titleMedium,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
            )
            TaskSubtitle(task)
        }
    }
}

/** "!!! Hoy · 6:00 p. m.": signos de prioridad en rojo y el horario normal. */
@Composable
private fun TaskSubtitle(task: Task) {
    val priorityColor = MaterialTheme.colorScheme.error
    val schedule = scheduleText(task)
    val text = buildAnnotatedString {
        val level = task.priority.coerceIn(0, 3)
        if (level > 0) {
            withStyle(SpanStyle(color = priorityColor, fontWeight = FontWeight.Bold)) {
                append("!".repeat(level))
            }
            append(" ")
        }
        append(schedule)
    }
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun CompletionIndicator(isCompleted: Boolean, accent: Color, onClick: () -> Unit) {
    val description = stringResource(
        if (isCompleted) R.string.completada else R.string.pendiente
    )

    if (isCompleted) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(1.5.dp, accent, RoundedCornerShape(6.dp))
                .clickable(onClickLabel = description, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = description,
                tint = accent,
                modifier = Modifier.size(16.dp)
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(accent.copy(alpha = 0.35f))
                .clickable(onClickLabel = description, onClick = onClick)
        )
    }
}
