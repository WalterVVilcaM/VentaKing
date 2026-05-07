package com.ventaking.app.presentacion.pantallas.historial

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.PointOfSale
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ventaking.app.dominio.modelo.CorteDiario
import com.ventaking.app.dominio.modelo.HistorialVenta
import com.ventaking.app.dominio.modelo.Negocio
import com.ventaking.app.dominio.modelo.Venta
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel,
    onVolver: () -> Unit,
    onImportarJson: () -> Unit,
    onExportarCorte: (String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensajeError) {
        val mensaje = uiState.mensajeError
        if (!mensaje.isNullOrBlank()) {
            snackbarHostState.showSnackbar(mensaje)
            viewModel.limpiarMensajeError()
        }
    }

    LaunchedEffect(uiState.mensajeAccion) {
        val mensaje = uiState.mensajeAccion
        if (!mensaje.isNullOrBlank()) {
            snackbarHostState.showSnackbar(mensaje)
            viewModel.limpiarMensajeAccion()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        HistorialContent(
            uiState = uiState,
            onVolver = onVolver,
            onSeleccionarNegocio = viewModel::seleccionarNegocio,
            onSeleccionarEstado = viewModel::seleccionarEstado,
            onSeleccionarOrigen = viewModel::seleccionarOrigen,
            onSeleccionarFecha = viewModel::seleccionarFecha,
            onSeleccionarCorte = viewModel::seleccionarCorte,
            onDiaAnterior = viewModel::irAlDiaAnterior,
            onDiaSiguiente = viewModel::irAlDiaSiguiente,
            onHoy = viewModel::irAHoy,
            onSeleccionarGrupo = viewModel::seleccionarGrupoVenta,
            onImportarJson = onImportarJson,
            onExportarCorte = {
                onExportarCorte(uiState.corteSeleccionado?.id)
            },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun HistorialContent(
    uiState: HistorialUiState,
    onVolver: () -> Unit,
    onSeleccionarNegocio: (String) -> Unit,
    onSeleccionarEstado: (EstadoVentaFiltro) -> Unit,
    onSeleccionarOrigen: (OrigenDatosFiltro) -> Unit,
    onSeleccionarFecha: (String) -> Unit,
    onSeleccionarCorte: (String) -> Unit,
    onDiaAnterior: () -> Unit,
    onDiaSiguiente: () -> Unit,
    onHoy: () -> Unit,
    onSeleccionarGrupo: (String) -> Unit,
    onImportarJson: () -> Unit,
    onExportarCorte: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (uiState.cargando) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Surface
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CabeceraHistorial(
                onVolver = onVolver,
                onImportarJson = onImportarJson,
                onExportarCorte = onExportarCorte
            )

            if (!uiState.hayNegocios) {
                EstadoVacio(
                    titulo = "No hay negocios disponibles",
                    descripcion = "Primero debe existir al menos un negocio para consultar el historial."
                )
                return@Column
            }

            PanelOpcionesHistorial(
                uiState = uiState,
                onSeleccionarNegocio = onSeleccionarNegocio,
                onSeleccionarEstado = onSeleccionarEstado,
                onSeleccionarOrigen = onSeleccionarOrigen,
                onSeleccionarFecha = onSeleccionarFecha,
                onDiaAnterior = onDiaAnterior,
                onDiaSiguiente = onDiaSiguiente,
                onHoy = onHoy
            )

            ResumenCompacto(
                uiState = uiState
            )

            CorteCompacto(
                cortes = uiState.cortes,
                corteSeleccionadoId = uiState.corteSeleccionadoId,
                onSeleccionarCorte = onSeleccionarCorte
            )

            VentasDelCorteSeleccionado(
                grupos = uiState.gruposVenta,
                corteSeleccionadoId = uiState.corteSeleccionadoId,
                grupoSeleccionadoId = uiState.grupoSeleccionadoId,
                historialGrupoSeleccionado = uiState.historialGrupoSeleccionado,
                onSeleccionarGrupo = onSeleccionarGrupo
            )
        }
    }
}

@Composable
private fun CabeceraHistorial(
    onVolver: () -> Unit,
    onImportarJson: () -> Unit,
    onExportarCorte: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
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

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Historial",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Consulta cortes y ventas guardadas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onImportarJson,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.FileUpload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Importar")
            }

            OutlinedButton(
                onClick = onExportarCorte,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Exportar")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PanelOpcionesHistorial(
    uiState: HistorialUiState,
    onSeleccionarNegocio: (String) -> Unit,
    onSeleccionarEstado: (EstadoVentaFiltro) -> Unit,
    onSeleccionarOrigen: (OrigenDatosFiltro) -> Unit,
    onSeleccionarFecha: (String) -> Unit,
    onDiaAnterior: () -> Unit,
    onDiaSiguiente: () -> Unit,
    onHoy: () -> Unit
) {
    var mostrarAvanzado by remember { mutableStateOf(false) }
    var mostrarCalendario by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SelectorFechaCompacto(
                fecha = uiState.fechaSeleccionada,
                onAbrirCalendario = {
                    mostrarCalendario = true
                },
                onDiaAnterior = onDiaAnterior,
                onDiaSiguiente = onDiaSiguiente,
                onHoy = onHoy
            )

            HorizontalDivider()

            Text(
                text = "Negocio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            NegociosEnCardsGrandes(
                negocios = uiState.negocios,
                negocioSeleccionadoId = uiState.negocioSeleccionadoId,
                onSeleccionarNegocio = onSeleccionarNegocio
            )

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { mostrarAvanzado = !mostrarAvanzado },
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Opciones avanzadas",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "${uiState.estadoSeleccionado.etiqueta} · ${uiState.origenSeleccionado.etiqueta}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = if (mostrarAvanzado) {
                            Icons.Rounded.ExpandLess
                        } else {
                            Icons.Rounded.ExpandMore
                        },
                        contentDescription = null
                    )
                }
            }

            AnimatedVisibility(visible = mostrarAvanzado) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider()

                    Text(
                        text = "Estado de venta",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.estadosDisponibles.forEach { estado ->
                            FilterChip(
                                selected = estado == uiState.estadoSeleccionado,
                                onClick = { onSeleccionarEstado(estado) },
                                label = { Text(estado.etiqueta) }
                            )
                        }
                    }

                    Text(
                        text = "Origen de datos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OrigenDatosFiltro.entries.forEach { origen ->
                            FilterChip(
                                selected = origen == uiState.origenSeleccionado,
                                onClick = { onSeleccionarOrigen(origen) },
                                label = { Text(origen.etiqueta) }
                            )
                        }
                    }

                    Text(
                        text = uiState.descripcionOrigen,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (mostrarCalendario) {
        DialogCalendarioHistorial(
            fechaActual = uiState.fechaSeleccionada,
            onSeleccionarFecha = { nuevaFecha ->
                onSeleccionarFecha(nuevaFecha)
                mostrarCalendario = false
            },
            onCerrar = {
                mostrarCalendario = false
            }
        )
    }
}

@Composable
private fun SelectorFechaCompacto(
    fecha: String,
    onAbrirCalendario: () -> Unit,
    onDiaAnterior: () -> Unit,
    onDiaSiguiente: () -> Unit,
    onHoy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAbrirCalendario() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Fecha",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = fecha,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        TextButton(
            onClick = onHoy
        ) {
            Text("Hoy")
        }

        IconButton(
            onClick = onDiaAnterior
        ) {
            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = "Día anterior"
            )
        }

        IconButton(
            onClick = onDiaSiguiente
        ) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Día siguiente"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogCalendarioHistorial(
    fechaActual: String,
    onSeleccionarFecha: (String) -> Unit,
    onCerrar: () -> Unit
) {
    val fechaInicialMillis = remember(fechaActual) {
        fechaTextoAMillisUtc(fechaActual)
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = fechaInicialMillis
    )

    DatePickerDialog(
        onDismissRequest = onCerrar,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        onSeleccionarFecha(millisUtcAFechaTexto(millis))
                    } else {
                        onCerrar()
                    }
                }
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "Seleccionar fecha",
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 18.dp)
                )
            },
            headline = {
                Text(
                    text = "Historial por día",
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
                )
            }
        )
    }
}

@Composable
private fun NegociosEnCardsGrandes(
    negocios: List<Negocio>,
    negocioSeleccionadoId: String?,
    onSeleccionarNegocio: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        negocios.chunked(2).forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                fila.forEach { negocio ->
                    NegocioCardGrande(
                        negocio = negocio,
                        seleccionado = negocio.id == negocioSeleccionadoId,
                        onClick = { onSeleccionarNegocio(negocio.id) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (fila.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun NegocioCardGrande(
    negocio: Negocio,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formaCard = MaterialTheme.shapes.large

    OutlinedCard(
        modifier = modifier
            .height(92.dp)
            .clip(formaCard)
            .clickable { onClick() },
        shape = formaCard,
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (seleccionado) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = BorderStroke(
            width = if (seleccionado) 2.dp else 1.dp,
            color = if (seleccionado) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Rounded.Storefront,
                contentDescription = null,
                tint = if (seleccionado) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(22.dp)
            )

            Text(
                text = negocio.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (seleccionado) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ResumenCompacto(
    uiState: HistorialUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Total del filtro",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = formatoDinero(uiState.totalVentasCentavos),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChipResumen("Ventas: ${uiState.totalVentasVisibles}")
                ChipResumen("Piezas: ${uiState.totalPiezas}")
                ChipResumen("Cortes: ${uiState.totalCortes}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChipResumen(
    texto: String
) {
    AssistChip(
        onClick = {},
        label = {
            Text(texto)
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun CorteCompacto(
    cortes: List<CorteDiario>,
    corteSeleccionadoId: String?,
    onSeleccionarCorte: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Cortes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (cortes.isEmpty()) {
            EstadoVacio(
                titulo = "Sin cortes",
                descripcion = "No hay cortes para el negocio y fecha seleccionados."
            )
            return
        }

        Text(
            text = "Selecciona un corte para ver sus ventas.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        cortes.forEach { corte ->
            CorteMiniCard(
                corte = corte,
                seleccionado = corte.id == corteSeleccionadoId,
                onClick = { onSeleccionarCorte(corte.id) }
            )
        }
    }
}

@Composable
private fun CorteMiniCard(
    corte: CorteDiario,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (seleccionado) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (seleccionado) 2.dp else 1.dp,
            color = if (seleccionado) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Corte del día",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${corte.totalVentas} ventas · ${corte.totalPiezas} piezas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (seleccionado) {
                    Text(
                        text = "Seleccionado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatoDinero(corte.totalCentavos),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = textoCorte(corte.estado),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun VentasDelCorteSeleccionado(
    grupos: List<GrupoVentaHistorialUi>,
    corteSeleccionadoId: String?,
    grupoSeleccionadoId: String?,
    historialGrupoSeleccionado: List<HistorialVenta>,
    onSeleccionarGrupo: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Ventas del corte",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (corteSeleccionadoId.isNullOrBlank()) {
            EstadoVacio(
                titulo = "Selecciona un corte",
                descripcion = "Las ventas se mostrarán únicamente después de elegir un corte."
            )
            return
        }

        val gruposDelCorte = grupos.filter { grupo ->
            grupo.corteId == corteSeleccionadoId
        }

        if (gruposDelCorte.isEmpty()) {
            EstadoVacio(
                titulo = "Sin ventas en este corte",
                descripcion = "No se encontraron ventas asociadas al corte seleccionado."
            )
            return
        }

        gruposDelCorte.forEach { grupo ->
            TicketVentaCard(
                grupo = grupo,
                seleccionado = grupo.grupoVentaId == grupoSeleccionadoId,
                historial = if (grupo.grupoVentaId == grupoSeleccionadoId) {
                    historialGrupoSeleccionado
                } else {
                    emptyList()
                },
                onClick = { onSeleccionarGrupo(grupo.grupoVentaId) }
            )
        }
    }
}

@Composable
private fun TicketVentaCard(
    grupo: GrupoVentaHistorialUi,
    seleccionado: Boolean,
    historial: List<HistorialVenta>,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (seleccionado) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EncabezadoTicket(
                hora = grupo.hora,
                estado = grupo.estadoResumen,
                total = grupo.totalCentavos
            )

            HorizontalDivider()

            grupo.ventas.forEach { venta ->
                ProductoVentaRow(venta = venta)
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${grupo.totalPiezas} pieza(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = formatoDinero(grupo.totalCentavos),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (seleccionado) {
                ActividadDiscreta(
                    historial = historial
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EncabezadoTicket(
    hora: String,
    estado: String,
    total: Long
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.PointOfSale,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = hora.ifBlank { "Venta" },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = textoEstadoVenta(estado),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        InputChip(
            selected = false,
            onClick = {},
            label = {
                Text(formatoDinero(total))
            }
        )
    }
}

@Composable
private fun ProductoVentaRow(
    venta: Venta
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "${venta.cantidad} x",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(42.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = venta.nombreProductoSnapshot,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Precio: ${formatoDinero(venta.precioUnitarioSnapshotCentavos)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (venta.extraCentavos > 0L) {
                Text(
                    text = "Extra: ${formatoDinero(venta.extraCentavos)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (venta.descuentoCentavos > 0L) {
                Text(
                    text = "Descuento: -${formatoDinero(venta.descuentoCentavos)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = formatoDinero(venta.totalCentavos),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ActividadDiscreta(
    historial: List<HistorialVenta>
) {
    val resumen = resumenActividad(historial)

    if (resumen.isBlank()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        HorizontalDivider()

        Text(
            text = resumen,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val ultimoMovimiento = historial.maxByOrNull { it.creadoEn }
        if (ultimoMovimiento != null) {
            Text(
                text = "Última actividad: ${formatoFechaHora(ultimoMovimiento.creadoEn)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
private fun EstadoVacio(
    titulo: String,
    descripcion: String
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatoDinero(centavos: Long): String {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    return formato.format(centavos / 100.0)
}

private fun formatoFechaHora(timestamp: Long): String {
    return try {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "MX"))
        formato.format(Date(timestamp))
    } catch (_: Exception) {
        ""
    }
}

private fun textoEstadoVenta(estado: String): String {
    return when (estado.uppercase()) {
        "ACTIVE" -> "Venta abierta"
        "CANCELLED" -> "Venta cancelada"
        "CLOSED" -> "Venta cerrada"
        "CORRECTED" -> "Venta corregida"
        "MIXTO" -> "Venta con cambios"
        else -> "Venta registrada"
    }
}

private fun textoCorte(estado: String): String {
    return when (estado.uppercase()) {
        "CLOSED" -> "Cerrado"
        "PENDING_SYNC" -> "Pendiente"
        "SYNCED" -> "Respaldado"
        "SYNC_ERROR" -> "Error"
        else -> "Registrado"
    }
}

private fun resumenActividad(historial: List<HistorialVenta>): String {
    if (historial.isEmpty()) return ""

    val acciones = historial.map { it.tipoAccion.uppercase() }

    val fueCancelada = acciones.contains("CANCELLED")
    val fueCerrada = acciones.contains("CLOSED")
    val fueEditada = acciones.any { it == "UPDATED" || it == "EDITED" || it == "CORRECTED" }

    return when {
        fueCancelada -> "Esta venta fue cancelada."
        fueEditada && fueCerrada -> "Esta venta tuvo correcciones y luego fue incluida en un corte."
        fueEditada -> "Esta venta tuvo correcciones."
        fueCerrada -> "Esta venta fue incluida en un corte."
        else -> "Venta registrada correctamente."
    }
}

private fun fechaTextoAMillisUtc(fechaTexto: String): Long {
    return try {
        val fecha = LocalDate.parse(fechaTexto, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        fecha.atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()
    } catch (_: Exception) {
        LocalDate.now()
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()
    }
}

private fun millisUtcAFechaTexto(millis: Long): String {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}