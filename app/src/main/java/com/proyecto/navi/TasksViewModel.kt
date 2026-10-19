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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    val totalCount: Int = 0
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
                totalCount = tasks.size
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
