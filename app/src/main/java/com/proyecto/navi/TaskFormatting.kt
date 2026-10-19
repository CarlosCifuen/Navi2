package com.proyecto.navi

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.proyecto.navi.data.Task
import com.proyecto.navi.data.TaskTag
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Colores de cada etiqueta, resueltos en la UI y no guardados en la base.
 *
 * Esto arregla el problema que tenían los chips en modo oscuro: los pasteles
 * fijos (0xFFFBE3D0 y compañía) se veían como parches claros sobre superficies
 * oscuras. Ahora cada tag tiene su par para cada tema.
 *
 * @return (color de fondo, color de texto)
 */
@Composable
fun TaskTag.colors(): Pair<Color, Color> {
    val dark = isSystemInDarkTheme()
    return when (this) {
        TaskTag.ALTA, TaskTag.CASA ->
            if (dark) Color(0xFF4A3524) to Color(0xFFF2BE8E)
            else Color(0xFFFBE3D0) to Color(0xFFB25E1E)

        TaskTag.CLASE ->
            if (dark) Color(0xFF26354F) to Color(0xFF9EBDF2)
            else Color(0xFFDCE7FB) to Color(0xFF2E5FAE)

        TaskTag.IDEA, TaskTag.HABITO ->
            if (dark) Color(0xFF23402F) to Color(0xFF8BD4AC)
            else Color(0xFFDDF3E8) to Color(0xFF2F8A5B)
    }
}

/** Texto corto de la etiqueta, para el chip. */
@Composable
fun TaskTag.label(): String = stringResource(
    when (this) {
        TaskTag.ALTA -> R.string.tag_alta
        TaskTag.CLASE -> R.string.tag_clase
        TaskTag.IDEA -> R.string.tag_idea
        TaskTag.HABITO -> R.string.tag_habito
        TaskTag.CASA -> R.string.tag_casa
    }
)

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
