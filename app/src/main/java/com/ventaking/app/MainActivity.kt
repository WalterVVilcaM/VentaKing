package com.ventaking.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ventaking.app.presentacion.navegacion.AppNavigation
import com.ventaking.app.presentacion.tema.TemaVentaKing

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TemaVentaKing {
                AppNavigation()
            }
        }
    }
}