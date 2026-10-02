package com.proyecto.navi

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey

/**
 * Describe un ítem de la barra inferior. Tener esto como dato (y no como
 * tres bloques de NavigationBarItem escritos a mano) significa que agregar
 * una pestaña nueva es una línea en TOP_LEVEL_ROUTES, no un copy-paste.
 */
data class TopLevelRoute(
    val key: NavKey,
    val label: String,
    val icon: ImageVector
)

/**
 * Las 3 pantallas principales, en el orden en que aparecen en la barra.
 * NaviApp también usa esta lista para decidir si muestra u oculta la barra.
 */
val TOP_LEVEL_ROUTES: List<TopLevelRoute> = listOf(
    TopLevelRoute(Tareas, "Tareas", Icons.Default.CheckBox),
    TopLevelRoute(Calendario, "Calendario", Icons.Default.CalendarMonth),
    TopLevelRoute(Ajustes, "Ajustes", Icons.Default.Tune)
)

/**
 * Barra de navegación inferior.
 *
 * Nótese que ya no recibe un enum NaviRoute: recibe la NavKey que está
 * actualmente en el tope del back stack. La barra no sabe nada de navegación,
 * solo reporta qué tocó el usuario (state hoisting) — quien decide qué hacer
 * con el back stack es NaviApp.
 *
 * @param current key en el tope del back stack; determina el ítem resaltado.
 * @param onSelect se invoca con la key de la pestaña tocada.
 */
@Composable
fun NaviBottomBar(
    current: NavKey?,
    onSelect: (NavKey) -> Unit,
    showLabels: Boolean = true
) {
    NavigationBar {
        TOP_LEVEL_ROUTES.forEach { route ->
            NavigationBarItem(
                selected = current == route.key,
                onClick = { onSelect(route.key) },
                icon = { Icon(route.icon, contentDescription = route.label) },
                // NavigationBarItem acepta label = null: el ítem queda solo
                // con el ícono, pero conserva el indicador de seleccionado.
                label = if (showLabels) {
                    { Text(route.label) }
                } else {
                    null
                }
            )
        }
    }
}