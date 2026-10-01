package com.proyecto.navi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // NaviTheme, no MaterialTheme: antes la paleta de Navi solo se veía
            // en los @Preview y la app real salía con los colores default de M3.
            NaviTheme {
                NaviApp()
            }
        }
    }
}