package com.proyecto.navi

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.proyecto.navi.data.NaviDatabase
import com.proyecto.navi.data.Task
import com.proyecto.navi.data.TaskRepository
import com.proyecto.navi.data.TaskTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class TaskFilter(@StringRes val labelRes: Int) {
    TODAS(R.string.todas),
    HOY(R.string.hoy),
    PENDIENTES(R.string.pendientes)
}

/**
 * Todo lo que la pantalla de Tareas necesita para pintarse, en un solo objeto.
 * Agrupar el estado así evita que la UI tenga que recalcular o combinar flows.
 */
data class TasksUiState(
    val grouped: Map<String, List<Task>> = emptyMap(),
    val filter: TaskFilter = TaskFilter.TODAS,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    /** Categorías que ya existen, para sugerirlas en el formulario de Nueva tarea. */
    val categories: List<String> = emptyList()
)

/**
 * Lo que el formulario entrega al guardar. Es un objeto aparte y no un Task
 * porque el formulario no conoce (ni debe conocer) el id ni las columnas
 * internas: eso lo completa el ViewModel.
 *
 * priority: 0 = sin prioridad, 1 = baja, 2 = media, 3 = alta.
 */
data class NewTaskInput(
    val title: String,
    val category: String,
    val dueAt: Long,
    val priority: Int
)

class TasksViewModel(private val repository: TaskRepository) : ViewModel() {

    private val filter = MutableStateFlow(TaskFilter.TODAS)

    /**
     * combine() reacciona a dos fuentes: la tabla (que cambia cuando se marca
     * una tarea) y el filtro elegido. Cualquiera de las dos que cambie vuelve a
     * emitir el estado completo.
     *
     * stateIn con WhileSubscribed(5s) mantiene viva la suscripción a Room
     * durante cambios de configuración (rotar la pantalla dura mucho menos que
     * 5 segundos), pero la corta si el usuario se va de verdad.
     */
    val uiState: StateFlow<TasksUiState> =
        combine(repository.tasks, filter) { tasks, selected ->
            val visible = when (selected) {
                TaskFilter.TODAS -> tasks
                // Antes: it.schedule.startsWith("Hoy"). Ahora compara fechas.
                TaskFilter.HOY -> tasks.filter { isToday(it.dueAt) || it.isDaily }
                TaskFilter.PENDIENTES -> tasks.filter { !it.isCompleted }
            }
            TasksUiState(
                grouped = visible.groupBy { it.category },
                filter = selected,
                completedCount = tasks.count { it.isCompleted },
                totalCount = tasks.size,
                // De todas las tareas, no de las visibles: con el filtro "Hoy"
                // activo igual se deben poder elegir todas las categorías.
                categories = tasks.map { it.category }.distinct().sorted()
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TasksUiState()
        )

    fun setFilter(value: TaskFilter) {
        filter.value = value
    }

    /**
     * Crea una tarea. Igual que toggleCompleted: solo se escribe en Room y las
     * pantallas de Tareas y Calendario se actualizan solas por el Flow.
     *
     * `tag` y `avatarUrl` son columnas que ya no se muestran en ninguna parte;
     * se llenan con valores neutros hasta que se eliminen en una migración.
     */
    fun addTask(input: NewTaskInput) {
        viewModelScope.launch {
            repository.save(
                Task(
                    id = UUID.randomUUID().toString(),
                    title = input.title,
                    category = input.category,
                    tag = TaskTag.IDEA,
                    dueAt = input.dueAt,
                    isDaily = false,
                    isCompleted = false,
                    avatarUrl = "",
                    priority = input.priority
                )
            )
        }
    }

    /**
     * Marca/desmarca una tarea. No hay que tocar ninguna lista en memoria: se
     * escribe en Room, Room re-emite el Flow y la UI se recompone sola.
     */
    fun toggleCompleted(task: Task) {
        viewModelScope.launch {
            repository.setCompleted(task.id, !task.isCompleted)
        }
    }

    companion object {
        /**
         * Sin Hilt ni inyección de dependencias: esta factory arma el grafo a
         * mano (base → DAO → repositorio → ViewModel). APPLICATION_KEY es la
         * forma soportada de conseguir el Application desde CreationExtras.
         */
        val Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                val dao = NaviDatabase.getInstance(application).taskDao()
                TasksViewModel(TaskRepository(dao))
            }
        }
    }
}
