package com.proyecto.navi

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
// OJO: `entry` NO se importa. Es miembro del scope de entryProvider { },
// así que dentro de ese bloque está disponible sin import.
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay

/**
 * Raíz de la app: es la dueña del back stack y la única que sabe navegar.
 *
 * La idea central de Navigation 3 (y lo que cambia respecto a Nav2) es que
 * el back stack es *tu* estado: una lista observable que vos modificás con
 * add() / removeLastOrNull(). No hay NavController ni NavHost escondiendo
 * el estado, así que "¿en qué pantalla estoy?" se responde con
 * backStack.lastOrNull().
 */
@Composable
fun NaviApp() {
    // rememberNavBackStack sobrevive rotación Y process death (por eso las
    // keys necesitan @Serializable). Arrancamos en Tareas, no en Ajustes.
    val backStack = rememberNavBackStack(Tareas)

    val currentKey: NavKey? = backStack.lastOrNull()

    // La barra solo existe en las 3 pantallas principales.
    val isTopLevel = TOP_LEVEL_ROUTES.any { it.key == currentKey }

    // Vive aquí (no en SettingsScreen ni en NaviBottomBar) porque ambos son
    // hermanos bajo NaviApp y necesitan leer/escribir el mismo valor. Cuando
    // tengamos persistencia (DataStore/Room) esto se movería ahí; por ahora,
    // rememberSaveable alcanza para que sobreviva una rotación de pantalla.
    var showBottomBarLabels by rememberSaveable { mutableStateOf(true) }

    /**
     * Cambio de pestaña, patrón "salir a través de Tareas":
     * el stack se reconstruye como [Tareas] o [Tareas, pestaña], de modo que
     * el botón atrás desde Calendario o Ajustes regresa a Tareas en lugar de
     * cerrar la app. Tocar la pestaña activa no hace nada.
     */
    val selectTopLevel: (NavKey) -> Unit = { target ->
        if (backStack.lastOrNull() != target) {
            backStack.clear()
            if (target != Tareas) backStack.add(Tareas)
            backStack.add(target)
        }
    }

    Scaffold(
        bottomBar = {
            if (isTopLevel) {
                NaviBottomBar(
                    current = currentKey,
                    onSelect = selectTopLevel,
                    showLabels = showBottomBarLabels
                )
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            // Solo el padding de abajo: cada pantalla tiene su propio Scaffold
            // con TopAppBar, y si le pasáramos innerPadding completo el inset
            // de la barra de estado se aplicaría dos veces.
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            entryProvider = entryProvider {
                entry<Tareas> { TasksScreen() }
                entry<Calendario> { CalendarScreen() }
                entry<Ajustes> {
                    SettingsScreen(
                        showBottomBarLabels = showBottomBarLabels,
                        onToggleBottomBarLabels = { showBottomBarLabels = it }
                    )
                }
            }
        )
    }
}