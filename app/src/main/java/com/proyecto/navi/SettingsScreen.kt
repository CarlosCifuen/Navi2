package com.proyecto.navi

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Colores de ícono para cada SettingsItem, según el diseño original
 */
private object MintIconColors {
    val background = Color(0xFFDFF3EA)
    val navegacion = Color(0xFF4C8C7A)
    val habitos = Color(0xFF3B6FD6)
    val apariencia = Color(0xFFE0913F)
}

/**
 * Esquemas de color para modo claro y oscuro.
 */
private val NaviLightColors = lightColorScheme(
    primary = Color(0xFF6C63FF),
    background = Color(0xFFFDFBFF),
    surfaceVariant = Color(0xFFF3EFFB),
    primaryContainer = Color(0xFFEDE7FA)
)

private val NaviDarkColors = darkColorScheme(
    primary = Color(0xFFB8B1FF),
    background = Color(0xFF141218),
    surfaceVariant = Color(0xFF2A2733),
    primaryContainer = Color(0xFF352F45)
)

@Composable
fun NaviTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) NaviDarkColors else NaviLightColors
    MaterialTheme(colorScheme = colors, content = content)
}

/**
 * Pantalla principal de Ajustes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    showBottomBarLabels: Boolean = true,
    onToggleBottomBarLabels: (Boolean) -> Unit = {}
) {
    Scaffold(
        topBar = {
            // CenterAlignedTopAppBar en vez de TopAppBar: es el único cambio
            // necesario para centrar el título, el resto del bloque es igual.
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(R.string.ajustes), style = MaterialTheme.typography.headlineMedium)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
        // Sin bottomBar: la barra la provee NaviApp para las 3 pantallas top-level.
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            ProfileCard(
                name = "Marlos",
                subtitle = "placeholder",
                avatarUrl = "https://media1.tenor.com/m/ITyftHRkwiMAAAAd/monkey-monkey-thinking.gif",
                onEditClick = { }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Grupo: Navegación / Hábitos / Apariencia
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                //composable
                SettingsItem(
                    icon = Icons.Default.Star,
                    iconTint = MintIconColors.habitos,
                    title = stringResource(R.string.h_bitos),
                    description = stringResource(R.string.rachas_y_recordatorios),
                    onClick = { }
                )
                HorizontalDivider()
                //composable
                SettingsItem(
                    icon = Icons.Default.Palette,
                    iconTint = MintIconColors.apariencia,
                    title = stringResource(R.string.apariencia),
                    description = stringResource(R.string.tema_claro_y_color_menta),
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                stringResource(R.string.notificaciones),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                //composable
                NotificationToggleItem(
                    title = stringResource(R.string.recordatorios_de_tareas),
                    description = stringResource(R.string._15_minutos_antes),
                    checked = true,
                    onCheckedChange = { }
                )
                HorizontalDivider()
                //composable
                NotificationToggleItem(
                    title = stringResource(R.string.resumen_diario),
                    description = stringResource(R.string.cada_d_a_a_las_7_00_p_m),
                    checked = true,
                    onCheckedChange = { }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "GENERAL",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                //composable
                SettingsItem(
                    title = stringResource(R.string.semana_inicia_el_lunes),
                    description = stringResource(R.string.idioma_espa_ol),
                    onClick = { }
                )
                HorizontalDivider()
                //composable
                NotificationToggleItem(
                    title = stringResource(R.string.mostrar_etiquetas_barra),
                    description = stringResource(R.string.mostrar_etiquetas_barra_desc),
                    checked = showBottomBarLabels,
                    onCheckedChange = onToggleBottomBarLabels
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Tarjeta de perfil: avatar, nombre, subtítulo y botón Editar.
 * Se usa una vez en esta pantalla.
 */
@Composable
fun ProfileCard(
    name: String,
    subtitle: String,
    avatarUrl: String,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = stringResource(R.string.foto_de_perfil_de, name),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedButton(onClick = onEditClick) {
            Text(stringResource(R.string.editar))
        }
    }
}

/**
 * Ítem reutilizable de ajuste con ícono, título, descripción y acción al tocar.
 * Se usa 4 veces en esta pantalla: Navegación, Hábitos, Apariencia, Semana inicia el lunes.
 */
@Composable
fun SettingsItem(
    title: String,
    description: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MintIconColors.background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Ítem reutilizable de notificación con Switch. Se usa 2 veces: Recordatorios de tareas, Resumen diario.
 */
@Composable
fun NotificationToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true, name = "Ajustes - Modo claro")
@Composable
fun SettingsScreenLightPreview() {
    NaviTheme(darkTheme = false) {
        SettingsScreen()
    }
}

@Preview(
    showBackground = true,
    name = "Ajustes - Modo oscuro",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SettingsScreenDarkPreview() {
    NaviTheme(darkTheme = true) {
        SettingsScreen()
    }
}