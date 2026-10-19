package com.proyecto.navi.data

import kotlinx.coroutines.flow.Flow

/**
 * Capa entre los DAOs y los ViewModels.
 *
 * Hoy es casi un passthrough, y eso está bien: su valor es que el ViewModel no
 * sabe que existe Room. Cuando aparezca una API remota o un caché, el cambio
 * ocurre acá adentro y ni el ViewModel ni la UI se enteran.
 */
class TaskRepository(private val dao: TaskDao) {

    val tasks: Flow<List<Task>> = dao.observeAll()

    suspend fun setCompleted(id: String, completed: Boolean) = dao.setCompleted(id, completed)

    suspend fun save(task: Task) = dao.upsert(task)

    suspend fun delete(id: String) = dao.delete(id)
}

class ProfileRepository(private val dao: ProfileDao) {

    val profile: Flow<Profile?> = dao.observe(Profile.SINGLE_ROW_ID)

    /** Guarda la URL que devolvió Cloudinary tras subir la foto. */
    suspend fun setAvatarUrl(url: String) = dao.setAvatarUrl(url, Profile.SINGLE_ROW_ID)
}
