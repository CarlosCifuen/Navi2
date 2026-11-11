package com.proyecto.navi

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.proyecto.navi.data.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Color de acento de una categoría (Universidad, Casa...), resuelto en la UI y
 * no guardado en la base. Lo usan el título de la sección, el checkbox de cada
 * tarea y, en Calendario, la barra y la hora de la actividad: así una tarea de
 * Universidad se ve del mismo azul en las dos pantallas.
 *
 * La categoría sigue siendo texto en la base. Las dos conocidas tienen color
 * fijo; cualquier otra que escriba el usuario recibe uno de la paleta según su
 * nombre, y siempre el mismo (por eso se usa el hash del nombre y no un
 * contador: un contador cambiaría de color al reordenar las secciones).
 * Cada color tiene su par para claro y oscuro, como los chips de antes.
 */
@Composable
fun categoryColor(category: String): Color {
    val dark = isSystemInDarkTheme()
    val key = category.trim().lowercase()
    return when (key) {
        "universidad" -> if (dark) Color(0xFF9EBDF2) else Color(0xFF2E5FAE)
        "casa" -> if (dark) Color(0xFF8BD4AC) else Color(0xFF2F8A5B)
        else -> {
            val palette = if (dark) {
                listOf(Color(0xFFF2BE8E), Color(0xFFC9A9F2), Color(0xFFF0A8C0), Color(0xFF86D3D8))
            } else {
                listOf(Color(0xFFB25E1E), Color(0xFF7A4CB8), Color(0xFFB8426E), Color(0xFF1F8A92))
            }
            palette[Math.floorMod(key.hashCode(), palette.size)]
        }
    }
}

/**
 * ¿El timestamp cae en el día de hoy?
 *
 * Se comparan año + día del año, no la diferencia en milisegundos: "dentro de
 * menos de 24 horas" no es lo mismo que "hoy" (las 11 p. m. de hoy y las 10 a. m.
 * de mañana están a 11 horas, pero son días distintos).
 *
 * Se usa Calendar y no java.time porque minSdk es 24 y java.time necesita API 26
 * o core library desugaring. Si algún día suben el minSdk o activan desugaring,
 * esto se puede reescribir con LocalDate y queda bastante más limpio.
 */
fun isToday(millis: Long): Boolean {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = millis }
    return now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
}

private fun isTomorrow(millis: Long): Boolean {
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    val then = Calendar.getInstance().apply { timeInMillis = millis }
    return tomorrow.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
        tomorrow.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
}

/**
 * Reconstruye el texto que antes estaba quemado en el modelo
 * ("Hoy · 6:00 p. m.", "Todos los días · 7:00 p. m.").
 *
 * La diferencia importante es la dirección: antes ese String era el dato y la
 * lógica lo leía; ahora el dato es `dueAt` y el texto se deriva de él. Por eso
 * el seed no envejece y el filtro "Hoy" compara fechas en vez de prefijos.
 */
@Composable
fun scheduleText(task: Task): String {
    // forLanguageTag en vez del constructor Locale("es"), que está deprecado.
    val locale = Locale.forLanguageTag("es")
    // Date explícito: SimpleDateFormat.format acepta Object, así que pasarle un
    // Long compila pero deja la conversión implícita a la vista de nadie.
    val date = Date(task.dueAt)
    val hour = SimpleDateFormat("h:mm a", locale).format(date)

    val day = when {
        task.isDaily -> stringResource(R.string.todos_los_dias)
        isToday(task.dueAt) -> stringResource(R.string.hoy)
        isTomorrow(task.dueAt) -> stringResource(R.string.manana)
        else -> SimpleDateFormat("EEEE", locale).format(date)
            .replaceFirstChar { it.uppercase(locale) }
    }

    return "$day · $hour"
}
