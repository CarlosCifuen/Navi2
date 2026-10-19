package com.proyecto.navi

import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey

/**
 * Describe un ítem de la barra inferior. Tener esto como dato (y no como
 * tres bloques de NavigationBarItem escritos a mano) significa que agregar
 * una pestaña nueva es una línea en TOP_LEVEL_ROUTES, no un copy-paste.
 *
 * Guarda el ID del string, no el texto: TOP_LEVEL_ROUTES es un `val` de nivel
 * superior y ahí no se puede llamar stringResource (no es contexto Composable).
 * El texto se resuelve dentro de NaviBottomBar, que sí lo es.
 */
data class TopLevelRoute(
    val key: NavKey,
    @StringRes val labelRes: Int,
    val icon: ImageVector
)

/**
 * Las 3 pantallas principales, en el orden en que aparecen en la barra.
 * NaviApp también usa esta lista para decidir si muestra u oculta la barra.
 */
val TOP_LEVEL_ROUTES: List<TopLevelRoute> = listOf(
    TopLevelRoute(Tareas, R.string.tareas, Icons.Default.CheckBox),
    TopLevelRoute(Calendario, R.string.calendario, Icons.Default.CalendarMonth),
    TopLevelRoute(Ajustes, R.string.ajustes, Icons.Default.Tune)
)

/**
 * Barra de navegación inferior.
 *
 * Recibe la NavKey que está actualmente en el tope del back stack. La barra no
 * sabe nada de navegación, solo reporta qué tocó el usuario (state hoisting) —
 * quien decide qué hacer con el back stack es NaviApp.
 *
 * @param current key en el tope del back stack; determina el ítem resaltado.
 * @param onSelect se invoca con la key de la pestaña tocada.
 * @param showLabels si false, los ítems quedan solo con ícono (ajuste de Ajustes).
 */
@Composable
fun NaviBottomBar(
    current: NavKey?,
    onSelect: (NavKey) -> Unit,
    showLabels: Boolean = true
) {
    NavigationBar {
        TOP_LEVEL_ROUTES.forEach { route ->
            // Se resuelve una sola vez y sirve para dos cosas: el texto visible
            // y el contentDescription del ícono. Esa doble función importa,
            // porque con showLabels apagado el contentDescription es lo único
            // que le queda a TalkBack para nombrar la pestaña.
            val label = stringResource(route.labelRes)

            NavigationBarItem(
                selected = current == route.key,
                onClick = { onSelect(route.key) },
                icon = { Icon(route.icon, contentDescription = label) },
                // NavigationBarItem acepta label = null: el ítem queda solo
                // con el ícono, pero conserva el indicador de seleccionado.
                label = if (showLabels) {
                    { Text(label) }
                } else {
                    null
                }
            )
        }
    }
}