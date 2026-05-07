package com.ventaking.app.presentacion.pantallas.corte

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
// PANTALLA PRINCIPAL
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CorteScreen(
    viewModel: CorteViewModel,
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Cabecera fija ────────────────────────────────────────────
            CabeceraCorte(
                fecha = uiState.fechaActual,
                onVolver = onVolver
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Contenido scrolleable ────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Selector de negocio
                if (uiState.negocios.isNotEmpty()) {
                    SelectorNegocioCorte(
                        negocios = uiState.negocios,
                        negocioSeleccionadoId = uiState.negocioSeleccionadoId,
                        onSeleccionarNegocio = viewModel::seleccionarNegocio
                    )
                }

                // Mensajes
                AnimatedVisibility(
                    visible = uiState.mensajeError != null,
                    enter = fadeIn(), exit = fadeOut()
                ) {
                    uiState.mensajeError?.let {
                        BannerMensaje(texto = it, esError = true, onCerrar = viewModel::limpiarMensajeError)
                    }
                }

                AnimatedVisibility(
                    visible = uiState.mensajeExito != null,
                    enter = fadeIn(), exit = fadeOut()
                ) {
                    uiState.mensajeExito?.let {
                        BannerMensaje(texto = it, esError = false, onCerrar = null)
                    }
                }

                // Tarjeta hero de resumen
                if (uiState.cargando) {
                    CardResumenCargando()
                } else {
                    CardResumenCorte(
                        total = viewModel.formatearCentavos(uiState.totalCentavos),
                        ventas = uiState.totalVentas,
                        piezas = uiState.totalPiezas,
                        yaTieneCorte = uiState.corteExistenteId != null,
                        sinVentas = uiState.totalVentas == 0
                    )
                }

                // Aviso informativo (solo si puede crear corte)
                if (uiState.puedeCrearCorte) {
                    AvisoAntesDeCorte()
                }
            }

            // ── Barra de acción fija inferior ────────────────────────────
            BarraAccionCorte(
                puedeCrear = uiState.puedeCrearCorte,
                creando = uiState.creandoCorte,
                yaTieneCorte = uiState.corteExistenteId != null,
                sinVentas = uiState.totalVentas == 0 && !uiState.cargando,
                onCrear = viewModel::pedirConfirmacionCorte
            )
        }
    }

    // ── Diálogo de confirmación ──────────────────────────────────────────────
    if (uiState.mostrarConfirmacionCorte) {
        DialogConfirmacionCorte(
            totalVentas = uiState.totalVentas,
            totalPiezas = uiState.totalPiezas,
            totalFormateado = viewModel.formatearCentavos(uiState.totalCentavos),
            onConfirmar = viewModel::confirmarCorte,
            onCancelar = viewModel::cancelarConfirmacionCorte
        )
    }
    if (uiState.mostrarModalSinInternet) {
        DialogSinInternetCorte(
            mensaje = uiState.mensajeModalSinInternet
                ?: "No hay internet. El corte quedó pendiente de respaldo.",
            onAceptar = viewModel::cerrarModalSinInternet
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CABECERA FIJA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CabeceraCorte(
    fecha: String,
    onVolver: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onVolver,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = "Corte de caja",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = fecha,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Ícono decorativo de candado — refuerza visualmente que esta acción es definitiva
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                modifier = Modifier.padding(8.dp).size(18.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SELECTOR DE NEGOCIO
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SelectorNegocioCorte(
    negocios: List<NegocioCorteUi>,
    negocioSeleccionadoId: String?,
    onSeleccionarNegocio: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Negocio",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            negocios.forEach { negocio ->
                val seleccionado = negocio.id == negocioSeleccionadoId
                FilterChip(
                    selected = seleccionado,
                    onClick = { onSeleccionarNegocio(negocio.id) },
                    label = {
                        Text(
                            text = negocio.nombre,
                            fontWeight = if (seleccionado) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = seleccionado,
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        selectedBorderWidth = 1.5.dp,
                        borderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TARJETA HERO — RESUMEN DEL CORTE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CardResumenCorte(
    total: String,
    ventas: Int,
    piezas: Int,
    yaTieneCorte: Boolean,
    sinVentas: Boolean
) {
    // Determinamos el estado visual de la tarjeta
    val (containerColor, contentColor) = when {
        yaTieneCorte -> Pair(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        sinVentas    -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        else         -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Encabezado de la tarjeta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Resumen del corte",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )

                // Ícono de estado
                when {
                    yaTieneCorte -> Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Corte ya realizado",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                    !sinVentas -> Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = contentColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                    else -> {}
                }
            }

            // Monto total — el número más importante de la pantalla
            Text(
                text = total,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = if (sinVentas) contentColor.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(
                color = contentColor.copy(alpha = 0.12f),
                thickness = 1.dp
            )

            // Métricas en fila
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricaCorte(
                    etiqueta = "Ventas",
                    valor = if (sinVentas) "—" else ventas.toString(),
                    modifier = Modifier.weight(1f),
                    containerColor = contentColor.copy(alpha = 0.10f),
                    contentColor = contentColor
                )
                MetricaCorte(
                    etiqueta = "Piezas",
                    valor = if (sinVentas) "—" else piezas.toString(),
                    modifier = Modifier.weight(1f),
                    containerColor = contentColor.copy(alpha = 0.10f),
                    contentColor = contentColor
                )
            }

            // Mensaje de estado dentro de la tarjeta
            when {
                yaTieneCorte -> BannerEstadoEnCard(
                    texto = "Este negocio ya fue cortado hoy en este dispositivo.",
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                    contentColor = MaterialTheme.colorScheme.secondary
                )
                sinVentas -> BannerEstadoEnCard(
                    texto = "No hay ventas activas pendientes de corte.",
                    color = contentColor.copy(alpha = 0.10f),
                    contentColor = contentColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun MetricaCorte(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.65f)
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = contentColor
            )
        }
    }
}

@Composable
private fun BannerEstadoEnCard(
    texto: String,
    color: Color,
    contentColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = color
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}

@Composable
private fun CardResumenCargando() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Calculando resumen...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AVISO ANTES DEL CORTE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AvisoAntesDeCorte() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 1.dp)
            )
            Text(
                text = "Revisa que las ventas del día estén correctas. Una vez creado el corte, " +
                        "esas ventas quedarán cerradas y no podrán editarse directamente.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BANNER DE MENSAJES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BannerMensaje(
    texto: String,
    esError: Boolean,
    onCerrar: (() -> Unit)?
) {
    val containerColor = if (esError) MaterialTheme.colorScheme.errorContainer
    else MaterialTheme.colorScheme.secondaryContainer

    val contentColor = if (esError) MaterialTheme.colorScheme.onErrorContainer
    else MaterialTheme.colorScheme.onSecondaryContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (esError) Icons.Rounded.Warning else Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
            if (onCerrar != null) {
                TextButton(
                    onClick = onCerrar,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 8.dp, vertical = 4.dp
                    )
                ) {
                    Text(
                        text = "OK",
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BARRA DE ACCIÓN INFERIOR — siempre visible
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BarraAccionCorte(
    puedeCrear: Boolean,
    creando: Boolean,
    yaTieneCorte: Boolean,
    sinVentas: Boolean,
    onCrear: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Texto de contexto debajo del botón
            val textoContexto = when {
                yaTieneCorte -> "Corte ya realizado — ventas cerradas para hoy"
                sinVentas    -> "Registra ventas antes de crear el corte"
                creando      -> "Procesando el corte de caja..."
                puedeCrear   -> "Al confirmar, las ventas activas quedarán cerradas"
                else         -> ""
            }

            if (textoContexto.isNotBlank()) {
                Text(
                    text = textoContexto,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Botón principal
            Button(
                onClick = onCrear,
                enabled = puedeCrear && !creando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                if (creando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Creando corte...",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            yaTieneCorte -> "Corte ya realizado"
                            sinVentas    -> "Sin ventas para cortar"
                            else         -> "Crear corte del día"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
@Composable
private fun DialogSinInternetCorte(
    mensaje: String,
    onAceptar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onAceptar,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = "Corte guardado sin internet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onAceptar,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Entendido")
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// DIÁLOGO DE CONFIRMACIÓN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DialogConfirmacionCorte(
    totalVentas: Int,
    totalPiezas: Int,
    totalFormateado: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            // Ícono visual en el dialog — refuerza la acción definitiva
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = "Confirmar corte de caja",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                // Resumen compacto en el dialog
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$totalVentas ventas · $totalPiezas piezas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = totalFormateado,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Text(
                    text = "Una vez creado, estas ventas quedarán cerradas y no podrán editarse directamente. Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Sí, crear corte",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )

}