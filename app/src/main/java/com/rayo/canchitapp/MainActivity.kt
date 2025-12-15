package com.rayo.canchitapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rayo.canchitapp.ui.ReservationApp // Importar tu pantalla

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Llamamos a la función principal de tu UI
            ReservationApp()
        }
    }
}