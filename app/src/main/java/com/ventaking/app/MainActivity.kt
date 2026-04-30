package com.ventaking.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ventaking.app.presentacion.componentes.BotonPrincipal
import com.ventaking.app.presentacion.componentes.IndicadorEstado
import com.ventaking.app.presentacion.componentes.TarjetaResumen
import com.ventaking.app.presentacion.componentes.TextoTitulo
import com.ventaking.app.presentacion.componentes.TipoIndicadorEstado
import com.ventaking.app.presentacion.tema.TemaVentaKing

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TemaVentaKing {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PantallaPruebaFaseUno()
                }
            }
        }
    }
}

@Composable
fun PantallaPruebaFaseUno() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextoTitulo(
            titulo = "VentaKing",
            subtitulo = "Fase 1 - Tema y componentes base"
        )

        IndicadorEstado(
            texto = "Tema activo",
            tipo = TipoIndicadorEstado.EXITO
        )

        TarjetaResumen(
            titulo = "Total del día",
            valor = "$ 1,250.00",
            descripcion = "Resumen visual provisional para validar estilos"
        )

        Spacer(modifier = Modifier.height(8.dp))

        BotonPrincipal(
            texto = "Continuar",
            onClick = { }
        )
    }
}