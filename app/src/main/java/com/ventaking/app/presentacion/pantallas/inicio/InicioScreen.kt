package com.ventaking.app.presentacion.pantallas.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun InicioScreen(
    onIrAVenta: () -> Unit,
    onIrAProductos: () -> Unit,
    onIrAVentasDia: () -> Unit,
    onIrACorte: () -> Unit,
    onIrAHistorial: () -> Unit,
    onIrASincronizacion: () -> Unit,
    onIrAConfiguracion: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "VentaKing",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Panel principal",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Fase 2: navegación principal conectada. Todavía no hay datos reales ni base de datos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Button(
                onClick = onIrAVenta,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Nueva venta")
            }

            OutlinedButton(
                onClick = onIrAProductos,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Productos rápidos")
            }

            OutlinedButton(
                onClick = onIrAVentasDia,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ventas del día")
            }

            OutlinedButton(
                onClick = onIrACorte,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Corte de caja")
            }

            OutlinedButton(
                onClick = onIrAHistorial,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Historial")
            }

            OutlinedButton(
                onClick = onIrASincronizacion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sincronización")
            }

            OutlinedButton(
                onClick = onIrAConfiguracion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Configuración")
            }
        }
    }
}