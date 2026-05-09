package com.ventaking.app.presentacion.pantallas.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                .padding(horizontal = 20.dp)
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. ENCABEZADO MÁS GRANDE Y LEGIBLE
            EncabezadoSenior()

            // 2. BOTÓN DE ACCIÓN PRINCIPAL (Gigante y con color de éxito)
            BotonPrincipalVenta(onClick = onIrAVenta)

            Text(
                text = "Opciones del negocio",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 8.dp)
            )

            // 3. OPCIONES SECUNDARIAS EN COLUMNA (Más fáciles de leer que en filas)
            OpcionMenuSenior(
                titulo = "Ver Productos",
                subtitulo = "Lista de lo que vendes",
                icono = "🍦",
                colorFondo = Color(0xFFE3F2FD),
                onClick = onIrAProductos
            )

            OpcionMenuSenior(
                titulo = "Hacer Corte de Caja",
                subtitulo = "Cerrar las ventas de hoy",
                icono = "💰",
                colorFondo = Color(0xFFE8F5E9),
                onClick = onIrACorte
            )

            OpcionMenuSenior(
                titulo = "Historial de Ventas",
                subtitulo = "Revisar ventas pasadas",
                icono = "🧾",
                colorFondo = Color(0xFFFFF3E0),
                onClick = onIrAVentasDia
            )

            // AQUÍ ESTÁ EL "PERDIDO": HISTORIAL DE CORTES
            OpcionMenuSenior(
                titulo = "Cortes Pasados",
                subtitulo = "Revisar cuentas de otros días",
                icono = "📊",
                colorFondo = Color(0xFFF3E5F5), // Morado claro
                onClick = onIrAHistorial
            )

            // 4. CONFIGURACIÓN Y AYUDA (Separados visualmente)
            Divider(modifier = Modifier.padding(vertical = 10.dp))

            OpcionMenuSeniorSimple(
                titulo = "Enviar datos (Nube)",
                icono = "☁️",
                onClick = onIrASincronizacion
            )

            OpcionMenuSeniorSimple(
                titulo = "Configuración",
                icono = "⚙️",
                onClick = onIrAConfiguracion
            )
        }
    }
}

@Composable
private fun EncabezadoSenior() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "👑",
            fontSize = 40.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "VentaKing",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = "¿Qué desea hacer hoy?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun BotonPrincipalVenta(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("🛒", fontSize = 50.sp)
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = "NUEVA VENTA",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
private fun OpcionMenuSenior(
    titulo: String,
    subtitulo: String,
    icono: String,
    colorFondo: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icono, fontSize = 35.sp)
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
private fun OpcionMenuSeniorSimple(
    titulo: String,
    icono: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icono, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}