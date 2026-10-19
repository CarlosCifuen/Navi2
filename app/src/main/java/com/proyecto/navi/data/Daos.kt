package com.proyecto.navi.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a la tabla `tasks`.
 *
 * `observeAll` devuelve Flow, no List: Room re-emite la lista completa cada vez
 * que la tabla cambia. Por eso marcar una tarea como completada no necesita que
 * nadie "refresque" la pantalla — se escribe en la base y la UI se entera sola.
 *
 * Las funciones que escriben son `suspend` porque hacen IO; Room se niega a
 * compilar si se llaman desde el hilo principal.
 */
@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY dueAt ASC")
    fun observeAll(): Flow<List<Task>>

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: String, completed: Boolean)

    @Upsert
    suspend fun upsert(task: Task)

    /** IGNORE para que el seed inicial no pise datos si ya existieran. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(tasks: List<Task>)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun count(): Int
}

/**
 * Acceso a la tabla `profile` (una sola fila).
 */
@Dao
interface ProfileDao {

    @Query("SELECT * FROM profile WHERE id = :id")
    fun observe(id: Int): Flow<Profile?>

    @Upsert
    suspend fun upsert(profile: Profile)

    @Query("UPDATE profile SET avatarUrl = :url WHERE id = :id")
    suspend fun setAvatarUrl(url: String, id: Int)
}
