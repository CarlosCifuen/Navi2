package com.proyecto.navi.data

import java.util.Calendar

/**
 * Construye un timestamp relativo a hoy: `at(0, 18)` es hoy a las 6:00 p. m.,
 * `at(1, 10)` mañana a las 10:00 a. m.
 *
 * Se usa offset relativo y no fechas fijas a propósito: así el filtro "Hoy"
 * siempre tiene algo que mostrar, sin importar cuándo se instale la app. Con
 * fechas quemadas, el seed envejecería y la demo quedaría vacía.
 */
private fun at(dayOffset: Int, hour: Int, minute: Int = 0): Long =
    Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, dayOffset)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

private const val DICEBEAR = "https://api.dicebear.com/9.x/shapes/png?seed="

/**
 * Datos iniciales. Se insertan una sola vez, cuando Room crea la base
 * (ver NaviDatabase.onCreate). A partir de ahí la fuente de verdad es SQLite:
 * si el usuario marca una tarea, el cambio persiste entre reinicios.
 */
fun seedTasks(): List<Task> = listOf(
    Task("t1", "Tarea 1 de ecuaciones", "Universidad", TaskTag.ALTA, at(0, 18), false, true, "${DICEBEAR}ecuaciones"),
    Task("t2", "Programación móvil", "Universidad", TaskTag.CLASE, at(1, 10), false, true, "${DICEBEAR}movil"),
    Task("t3", "Idear prompt", "Universidad", TaskTag.IDEA, at(4, 16, 30), false, false, "${DICEBEAR}prompt"),
    Task("t4", "Entregar wireframes", "Universidad", TaskTag.ALTA, at(0, 20), false, false, "${DICEBEAR}wireframes"),
    Task("t5", "Leer capítulo 3", "Universidad", TaskTag.CLASE, at(5, 9), false, false, "${DICEBEAR}lectura"),
    Task("t6", "Preparar exposición", "Universidad", TaskTag.CLASE, at(3, 14), false, false, "${DICEBEAR}exposicion"),
    Task("t7", "Comida de Laika", "Casa", TaskTag.HABITO, at(0, 19), true, false, "${DICEBEAR}laika"),
    Task("t8", "Lavar la ropa", "Casa", TaskTag.CASA, at(2, 9), false, true, "${DICEBEAR}ropa"),
    Task("t9", "Regar las plantas", "Casa", TaskTag.HABITO, at(0, 8), false, true, "${DICEBEAR}plantas"),
    Task("t10", "Sacar la basura", "Casa", TaskTag.CASA, at(6, 20), false, false, "${DICEBEAR}basura"),
    Task("t11", "Pagar servicios", "Casa", TaskTag.CASA, at(4, 17), false, false, "${DICEBEAR}servicios"),
    Task("t12", "Meditar 10 minutos", "Casa", TaskTag.HABITO, at(0, 7), true, true, "${DICEBEAR}meditar")
)

/** Perfil inicial. avatarUrl arranca en null hasta que se suba una foto. */
fun seedProfile(): Profile = Profile(
    name = "Marlos",
    subtitle = "placeholder",
    avatarUrl = null
)
