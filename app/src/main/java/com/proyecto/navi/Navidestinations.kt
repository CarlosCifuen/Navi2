package com.proyecto.navi

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Destinos de la app en Navigation 3.
 *
 * En Nav3 un destino NO es un String de ruta (como en Nav2), sino una *key*:
 * un objeto serializable que implementa NavKey. Eso nos da type-safety gratis
 * y permite pasar argumentos como propiedades normales de un data class.
 *
 * Los dos requisitos son obligatorios para poder usar rememberNavBackStack:
 *  1. implementar NavKey
 *  2. estar anotado con @Serializable (para sobrevivir process death)
 */
@Serializable
data object Tareas : NavKey

@Serializable
data object Calendario : NavKey

@Serializable
data object Ajustes : NavKey

/*
 * "Nueva tarea" NO está aquí: en vez de un destino empujado al back stack,
 * ahora es un ModalBottomSheet manejado con estado local dentro de
 * TasksScreen.kt (ver `showNewTaskSheet`). Eso nos da el look de "tray" sobre
 * la pantalla de atrás, que NavDisplay no ofrece por defecto (reemplaza la
 * pantalla completa en vez de apilar un overlay encima).
 *
 * Cuando necesiten pasar argumentos (por ejemplo, abrir una tarea existente),
 * en Nav3 es simplemente un data class — sin rutas con placeholders ni Bundles:
 *
 * @Serializable
 * data class DetalleTarea(val taskId: String) : NavKey
 *
 * y se navega con: backStack.add(DetalleTarea(task.id))
 */