package com.proyecto.navi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.proyecto.navi.data.NaviDatabase
import com.proyecto.navi.data.Task
import com.proyecto.navi.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

/**
 * Un día calendario, sin hora. `month` va de 0 a 11 (igual que Calendar.MONTH).
 *
 * Es una data class y no un Long de milisegundos porque dos tareas a distinta
 * hora del mismo día deben ser "el mismo día", y con data class eso lo da el
 * equals() gratis (sirve para Set, para comparar, para usarlo de key).
 *
 * Se usa Calendar y no java.time por la misma razón que en TaskFormatting.kt:
 * minSdk es 24 y java.time necesita API 26 o desugaring.
 */
data class DayKey(val year: Int, val month: Int, val day: Int)

/** Una celda de la cuadrícula del mes (siempre hay 42: 6 semanas x 7 días). */
data class DayCell(
    val date: DayKey,
    /** false para los días grises del mes anterior/siguiente. */
    val inMonth: Boolean,
    val hasTasks: Boolean,
    val isSelected: Boolean
)

data class CalendarUiState(
    /** Siempre el día 1 del mes que se está viendo. */
    val visibleMonth: DayKey,
    val cells: List<DayCell>,
    val selected: DayKey,
    val selectedTasks: List<Task>
)

// ---------- Helpers de fechas ----------

fun DayKey.toCalendar(): Calendar = Calendar.getInstance().apply {
    clear()
    set(year, month, day)
}

private fun Calendar.toDayKey(): DayKey =
    DayKey(get(Calendar.YEAR), get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH))

fun Long.toDayKey(): DayKey =
    Calendar.getInstance().apply { timeInMillis = this@toDayKey }.toDayKey()

fun todayKey(): DayKey = Calendar.getInstance().toDayKey()

private fun DayKey.plusMonths(n: Int): DayKey =
    toCalendar().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MONTH, n)
    }.toDayKey()

private fun minutesOfDay(millis: Long): Int {
    val c = Calendar.getInstance().apply { timeInMillis = millis }
    return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
}

/**
 * Los 42 días que se pintan para un mes. Arranca en el domingo de la semana
 * que contiene el día 1 (como en el wireframe: D L M M J V S), y por eso
 * incluye algunos días grises del mes anterior y del siguiente.
 */
private fun monthGrid(month: DayKey): List<DayKey> {
    val cursor = month.toCalendar().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        val offset = get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
        add(Calendar.DAY_OF_YEAR, -offset)
    }
    val days = ArrayList<DayKey>(42)
    repeat(42) {
        days.add(cursor.toDayKey())
        cursor.add(Calendar.DAY_OF_YEAR, 1)
    }
    return days
}

/**
 * Función pura (recibe todo, no toca Room ni estado): por eso los @Preview
 * pueden armar un CalendarUiState llamándola, sin ViewModel.
 *
 * Decisión de diseño: un hábito diario (isDaily) aparece en la lista de
 * CUALQUIER día, pero no pinta el punto en la cuadrícula. Si lo pintara, los
 * 30 días del mes tendrían punto y la marca dejaría de decir algo útil.
 */
fun buildCalendarState(
    tasks: List<Task>,
    selected: DayKey,
    visibleMonth: DayKey
): CalendarUiState {
    val daysWithTasks = tasks.filter { !it.isDaily }.map { it.dueAt.toDayKey() }.toSet()

    val cells = monthGrid(visibleMonth).map { day ->
        DayCell(
            date = day,
            inMonth = day.month == visibleMonth.month,
            hasTasks = day in daysWithTasks,
            isSelected = day == selected
        )
    }

    val selectedTasks = tasks
        .filter { it.isDaily || it.dueAt.toDayKey() == selected }
        .sortedBy { minutesOfDay(it.dueAt) }

    return CalendarUiState(visibleMonth, cells, selected, selectedTasks)
}

// ---------- ViewModel ----------

class CalendarViewModel(repository: TaskRepository) : ViewModel() {

    private val selected = MutableStateFlow(todayKey())
    private val visibleMonth = MutableStateFlow(todayKey().copy(day = 1))

    /**
     * Mismo patrón que TasksViewModel: combine() reacciona a Room y a los dos
     * estados de la pantalla (día elegido, mes visible). Si en Tareas se marca
     * algo o se crea una tarea, el calendario se entera solo porque ambos leen
     * el mismo Flow de la misma tabla.
     *
     * El initialValue ya trae la cuadrícula armada (con 0 tareas) para que el
     * mes no aparezca vacío el primer frame mientras Room responde.
     */
    val uiState: StateFlow<CalendarUiState> =
        combine(repository.tasks, selected, visibleMonth) { tasks, sel, month ->
            buildCalendarState(tasks, sel, month)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = buildCalendarState(emptyList(), selected.value, visibleMonth.value)
        )

    /** Tocar un día gris (de otro mes) también lleva la vista a ese mes. */
    fun selectDay(day: DayKey) {
        selected.value = day
        visibleMonth.value = day.copy(day = 1)
    }

    /**
     * Al cambiar de mes se selecciona hoy (si es el mes actual) o el día 1.
     * Así siempre hay un día elegido visible en la cuadrícula y la lista de
     * abajo nunca muestra un día que no se ve arriba.
     */
    fun changeMonth(delta: Int) {
        val target = visibleMonth.value.plusMonths(delta)
        val today = todayKey()
        visibleMonth.value = target
        selected.value =
            if (target.year == today.year && target.month == today.month) today else target
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                val dao = NaviDatabase.getInstance(application).taskDao()
                CalendarViewModel(TaskRepository(dao))
            }
        }
    }
}
