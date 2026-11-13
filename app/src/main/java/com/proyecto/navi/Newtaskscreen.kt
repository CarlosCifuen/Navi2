package com.proyecto.navi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Contenido del tray de "Nueva tarea": no es un destino de navegación, se
 * muestra dentro de un ModalBottomSheet desde TasksScreen.kt.
 *
 * Este Composable no sabe de Room ni de ViewModels: al guardar solo entrega un
 * NewTaskInput por onSave. Quien lo usa decide qué hacer con él.
 *
 * Los campos viven aquí con rememberSaveable (sobreviven a rotar la pantalla).
 * Cada vez que se abre el sheet el Composable entra de cero, así que el
 * formulario siempre arranca limpio.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskScreen(
    categories: List<String>,
    onSave: (NewTaskInput) -> Unit,
    onClose: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    // Se guarda el día como milisegundos de su medianoche local, y la hora
    // aparte: son tipos que rememberSaveable sabe guardar sin código extra.
    var dayMillis by rememberSaveable { mutableStateOf(todayKey().toCalendar().timeInMillis) }
    var hour by rememberSaveable { mutableStateOf(nextFullHour()) }
    var minute by rememberSaveable { mutableStateOf(0) }
    // 0 = sin prioridad. Es opcional: tocar la chip elegida la quita.
    var priority by rememberSaveable { mutableStateOf(0) }

    var submitted by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    val titleError = submitted && title.isBlank()
    val categoryError = submitted && category.isBlank()
    val required = stringResource(R.string.campo_obligatorio)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 24.dp)
    ) {

        // Solo esta parte se desplaza; los botones de abajo quedan fijos para
        // que Guardar nunca se esconda detrás del teclado.
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.campo_titulo)) },
                placeholder = { Text(stringResource(R.string.titulo_ejemplo)) },
                isError = titleError,
                supportingText = if (titleError) {
                    { Text(required) }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text(stringResource(R.string.campo_categoria)) },
                placeholder = { Text(stringResource(R.string.categoria_ejemplo)) },
                isError = categoryError,
                supportingText = if (categoryError) {
                    { Text(required) }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Las categorías que ya existen se eligen con un toque: evita que
            // "casa" y "Casa" terminen como dos secciones distintas en la lista.
            if (categories.isNotEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { name ->
                        FilterChip(
                            selected = category.trim().equals(name, ignoreCase = true),
                            onClick = { category = name },
                            label = { Text(name) }
                        )
                    }
                }
            }

            PickerField(
                value = formatDate(dayMillis),
                label = stringResource(R.string.campo_fecha),
                icon = {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                },
                onClick = { showDatePicker = true }
            )

            PickerField(
                value = "%02d:%02d".format(hour, minute),
                label = stringResource(R.string.campo_hora),
                icon = {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                },
                onClick = { showTimePicker = true }
            )

            Column {
                Text(
                    stringResource(R.string.campo_prioridad),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    stringResource(R.string.prioridad_opcional),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    1 to R.string.prioridad_baja,
                    2 to R.string.prioridad_media,
                    3 to R.string.prioridad_alta
                ).forEach { (level, label) ->
                    FilterChip(
                        selected = priority == level,
                        onClick = { priority = if (priority == level) 0 else level },
                        label = { Text(stringResource(label)) }
                    )
                }
            }

            Text(
                stringResource(R.string.campos_obligatorios),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.cancelar))
            }
            Button(
                onClick = {
                    submitted = true
                    val cleanTitle = title.trim()
                    val cleanCategory = category.trim()
                    if (cleanTitle.isNotEmpty() && cleanCategory.isNotEmpty()) {
                        onSave(
                            NewTaskInput(
                                title = cleanTitle,
                                // Si escribió "casa" y ya existe "Casa", se
                                // reutiliza la existente para no duplicar sección.
                                category = categories.firstOrNull {
                                    it.equals(cleanCategory, ignoreCase = true)
                                } ?: cleanCategory,
                                dueAt = dueAtMillis(dayMillis, hour, minute),
                                priority = priority
                            )
                        )
                    }
                }
            ) {
                Text(stringResource(R.string.guardar))
            }
        }
    }

    if (showDatePicker) {
        // DatePicker trabaja en UTC: devuelve la medianoche UTC del día elegido.
        // Se lee el día en UTC y se reconstruye en hora local, si no, en
        // zonas horarias al oeste de UTC (como Guatemala) saldría el día anterior.
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = localDayToUtcMillis(dayMillis)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { dayMillis = utcMillisToLocalDay(it) }
                    showDatePicker = false
                }) { Text(stringResource(R.string.aceptar)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    hour = timeState.hour
                    minute = timeState.minute
                    showTimePicker = false
                }) { Text(stringResource(R.string.aceptar)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.cancelar))
                }
            },
            text = { TimePicker(state = timeState) }
        )
    }
}

/**
 * Campo que no se escribe, se elige (fecha y hora). Un OutlinedTextField con
 * readOnly = true no reenvía los toques, así que se le pone encima una capa
 * transparente (matchParentSize) que sí los recibe.
 */
@Composable
private fun PickerField(
    value: String,
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = icon,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}

// ---------- Helpers de fecha/hora ----------

private val UTC: TimeZone = TimeZone.getTimeZone("UTC")

private fun formatDate(dayMillis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es")).format(Date(dayMillis))

/** La hora completa siguiente a ahora: a las 14:20 sugiere 15:00. */
private fun nextFullHour(): Int =
    (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1) % 24

/** Día (medianoche local) + hora y minuto elegidos = el instante de la tarea. */
private fun dueAtMillis(dayMillis: Long, hour: Int, minute: Int): Long =
    dayMillis.toDayKey().toCalendar().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }.timeInMillis

private fun localDayToUtcMillis(dayMillis: Long): Long {
    val day = dayMillis.toDayKey()
    return Calendar.getInstance(UTC).apply {
        clear()
        set(day.year, day.month, day.day)
    }.timeInMillis
}

private fun utcMillisToLocalDay(utcMillis: Long): Long {
    val c = Calendar.getInstance(UTC).apply { timeInMillis = utcMillis }
    return DayKey(
        c.get(Calendar.YEAR),
        c.get(Calendar.MONTH),
        c.get(Calendar.DAY_OF_MONTH)
    ).toCalendar().timeInMillis
}

@Preview(showBackground = true, name = "Nueva tarea")
@Composable
private fun NewTaskScreenPreview() {
    NaviTheme(darkTheme = false) {
        NewTaskScreen(
            categories = listOf("Casa", "Universidad"),
            onSave = {},
            onClose = {}
        )
    }
}
