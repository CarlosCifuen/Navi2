package com.proyecto.navi

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * Rutas de las 3 pantallas principales de la app (aún no usamos Navigation Compose,
 * así que por ahora esto solo controla qué ítem se ve seleccionado).
 */
enum class NaviRoute { TAREAS, CALENDARIO, AJUSTES }

/**
 * Barra de navegación inferior compartida por Tareas, Calendario y Ajustes.
 * Se extrajo a su propio archivo porque las 3 pantallas la necesitan (ver wireframes).
 *
 * @param selected pantalla actualmente activa (se resalta en el theme).
 * @param onSelect callback con la ruta elegida; cada pantalla decide qué hacer
 *   (por ahora, hasta que exista NavHost, puede ignorarse o loguearse).
 */
@Composable
fun NaviBottomBar(
    selected: NaviRoute,
    onSelect: (NaviRoute) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selected == NaviRoute.TAREAS,
            onClick = { onSelect(NaviRoute.TAREAS) },
            icon = { Icon(Icons.Default.CheckBox, contentDescription = "Tareas") },
            label = { Text("Tareas") },
            colors = NavigationBarItemDefaults.colors()
        )
        NavigationBarItem(
            selected = selected == NaviRoute.CALENDARIO,
            onClick = { onSelect(NaviRoute.CALENDARIO) },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendario") },
            label = { Text("Calendario") }
        )
        NavigationBarItem(
            selected = selected == NaviRoute.AJUSTES,
            onClick = { onSelect(NaviRoute.AJUSTES) },
            icon = { Icon(Icons.Default.Tune, contentDescription = "Ajustes") },
            label = { Text("Ajustes") }
        )
    }
}
