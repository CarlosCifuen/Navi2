# Navi

App de Android para organizar tareas con calendario, notificaciones
y un sistema de rachas que motiva al usuario a cumplir sus actividades
de forma constante, sin anuncios ni funciones innecesarias.

## Problema
Las apps de calendario, alarmas y tareas funcionan por separado,
lo que provoca olvidos y baja productividad.

## Propuesta de valor
- Calendario integrado para tareas.
- Notificaciones antes de la fecha límite.
- Sistema de rachas para fomentar la constancia.
- Funciones configurables y desactivables.

## Usuarios objetivo
Estudiantes y trabajadores que necesitan organizar sus actividades diarias.

## Tecnologías
Kotlin + Jetpack Compose

## Cómo correrlo
1. Clonar el repositorio.
2. Abrir en Android Studio.
3. Sincronizar Gradle.
4. Ejecutar en emulador o dispositivo físico.

## Uso de IA
Durante el desarrollo de la interfaz utilizamos Claude como asistente. Su
participación se concentró en la migración de la navegación a Jetpack
Navigation 3 (el back stack, `NaviApp` y la refactorización de `NaviBottomBar`),
en la separación de `TasksScreen` y `SettingsScreen` en una versión con
ViewModel y otra sin estado para conservar los `@Preview`, en el formateo de
fechas y los colores de etiqueta por tema de `TaskFormatting.kt`, y en las
pantallas `CalendarScreen` y `NewTaskScreen`. También apoyó en la capa de datos
con Room y en la subida de imágenes a Cloudinary. El diseño de las pantallas,
los wireframes de Figma y las decisiones de producto son del equipo;