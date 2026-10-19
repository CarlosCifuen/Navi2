package com.proyecto.navi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Etiqueta de prioridad/tipo de una tarea.
 *
 * Antes esto era un String más dos objetos Color guardados dentro de Task. Un
 * Color no se puede persistir en SQLite de forma sensata, y además un color es
 * una decisión de presentación: el mismo tag se pinta distinto en claro y en
 * oscuro. Acá queda solo el dato; los colores se resuelven en la UI
 * (ver TaskFormatting.kt).
 */
enum class TaskTag { ALTA, CLASE, IDEA, HABITO, CASA }

/**
 * Una tarea, tal como se guarda en la tabla `tasks`.
 *
 * Se usa directamente como modelo de la UI en vez de tener un `Task` de dominio
 * aparte con mappers. Para una app de este tamaño el mapeo extra no paga: si más
 * adelante entra una API remota con una forma distinta, ahí sí conviene separar.
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val tag: TaskTag,
    /**
     * Vencimiento en epoch millis. Antes era el String "Hoy · 6:00 p. m.", que
     * obligaba al filtro HOY a hacer startsWith("Hoy") sobre texto de UI. Con un
     * número, el filtro compara fechas de verdad y el texto se deriva al pintar.
     */
    val dueAt: Long,
    /** Hábito diario ("Todos los días · 7:00 p. m."), no vence una sola vez. */
    val isDaily: Boolean = false,
    val isCompleted: Boolean = false,
    val avatarUrl: String
)

/**
 * Perfil del usuario. Es una tabla de una sola fila: `id` siempre vale
 * SINGLE_ROW_ID, así cualquier upsert reemplaza la fila existente en vez de
 * acumular perfiles.
 *
 * `avatarUrl` es la URL que devuelve Cloudinary después de subir la foto, o
 * null mientras el usuario no haya elegido ninguna.
 */
@Entity(tableName = "profile")
data class Profile(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val name: String,
    val subtitle: String,
    val avatarUrl: String?
) {
    companion object {
        const val SINGLE_ROW_ID = 1
    }
}
