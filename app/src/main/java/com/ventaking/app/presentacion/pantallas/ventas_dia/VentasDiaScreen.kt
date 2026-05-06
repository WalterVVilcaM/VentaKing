package com.ventaking.app.presentacion.pantallas.ventas_dia

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentasDiaScreen(
    viewModel: VentasDiaViewModel,
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ventas del día",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onVolver,
                        modifier = Modifier
                            .padding(start = 16.dp)
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
                },
                actions = {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = viewModel.formatearCentavos(uiState.totalDiaCentavos),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            BarraResumenVentasDia(
                total = viewModel.formatearCentavos(uiState.totalDiaCentavos),
                transacciones = uiState.cantidadVentas,
                piezas = uiState.piezasVendidas
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (uiState.cargando) {
                PantallaCargandoVentasDia()
            } else {
                SelectorNegocioVentasDia(
                    negocios = uiState.negocios,
                    negocioSeleccionadoId = uiState.negocioSeleccionadoId,
                    onSeleccionarNegocio = viewModel::seleccionarNegocio
                )

                if (uiState.ventaEnEdicion == null &&
                    uiState.ventaParaCancelar == null &&
                    uiState.grupoParaCancelar == null
                ) {
                    uiState.mensajeError?.let { mensaje ->
                        MensajeVentasDia(
                            texto = mensaje,
                            esError = true
                        )
                    }
                }

                uiState.mensajeExito?.let { mensaje ->
                    MensajeVentasDia(
                        texto = mensaje,
                        esError = false
                    )
                }

                Text(
                    text = "Transacciones abiertas",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (uiState.grupos.isEmpty()) {
                    EstadoVentasDiaVacias()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 10.dp,
                            bottom = 18.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.grupos,
                            key = { grupo -> grupo.grupoVentaId }
                        ) { grupo ->
                            GrupoVentaDiaCard(
                                grupo = grupo,
                                formatear = viewModel::formatearCentavos,
                                onEditarVenta = viewModel::abrirEdicion,
                                onCancelarVenta = viewModel::abrirConfirmacionCancelacionProducto,
                                onCancelarGrupo = {
                                    viewModel.abrirConfirmacionCancelacion(grupo)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    uiState.ventaEnEdicion?.let { venta ->
        DialogoEditarVentaDia(
            venta = venta,
            cantidadTexto = uiState.cantidadTexto,
            extraTexto = uiState.extraTexto,
            descuentoTexto = uiState.descuentoTexto,
            notaTexto = uiState.notaTexto,
            mensajeError = uiState.mensajeError,
            editando = uiState.editando,
            formatear = viewModel::formatearCentavos,
            onCantidadChange = viewModel::cambiarCantidadTexto,
            onExtraChange = viewModel::cambiarExtraTexto,
            onDescuentoChange = viewModel::cambiarDescuentoTexto,
            onNotaChange = viewModel::cambiarNotaTexto,
            onGuardar = viewModel::guardarEdicion,
            onCerrar = viewModel::cerrarEdicion
        )
    }

    uiState.grupoParaCancelar?.let { grupo ->
        DialogoCancelarGrupoVenta(
            grupo = grupo,
            notaTexto = uiState.notaTexto,
            cancelando = uiState.cancelando,
            formatear = viewModel::formatearCentavos,
            onNotaChange = viewModel::cambiarNotaTexto,
            onConfirmar = viewModel::cancelarGrupoSeleccionado,
            onCerrar = viewModel::cerrarConfirmacionCancelacion
        )
    }

    uiState.ventaParaCancelar?.let { venta ->
        DialogoCancelarProductoVenta(
            venta = venta,
            notaTexto = uiState.notaTexto,
            cancelando = uiState.cancelando,
            formatear = viewModel::formatearCentavos,
            onNotaChange = viewModel::cambiarNotaTexto,
            onConfirmar = viewModel::cancelarProductoSeleccionado,
            onCerrar = viewModel::cerrarConfirmacionCancelacionProducto
        )
    }
}

@Composable
private fun SelectorNegocioVentasDia(
    negocios: List<NegocioVentasDiaUi>,
    negocioSeleccionadoId: String?,
    onSeleccionarNegocio: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
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

@Composable
private fun GrupoVentaDiaCard(
    grupo: GrupoVentaDiaUi,
    formatear: (Long) -> String,
    onEditarVenta: (VentaDiaUi) -> Unit,
    onCancelarVenta: (VentaDiaUi) -> Unit,
    onCancelarGrupo: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Transacción #${grupo.grupoVentaId.takeLast(6).uppercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${grupo.horaVenta} · ${grupo.productosDistintos} productos · ${grupo.piezas} piezas",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = formatear(grupo.totalCentavos),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = grupo.resumenProductos,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grupo.ventas.forEach { venta ->
                    VentaDentroGrupoRow(
                        venta = venta,
                        formatear = formatear,
                        onEditar = {
                            onEditarVenta(venta)
                        },
                        onEliminar = {
                            onCancelarVenta(venta)
                        }
                    )
                }
            }

            OutlinedButton(
                onClick = onCancelarGrupo,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Cancelar transacción",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun VentaDentroGrupoRow(
    venta: VentaDiaUi,
    formatear: (Long) -> String,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .width(36.dp)
                    .height(36.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = venta.cantidad.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = venta.nombreProducto,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${formatear(venta.precioUnitarioCentavos)} c/u · ${formatear(venta.totalCentavos)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            TextButton(
                onClick = onEditar,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text("Editar")
            }

            IconButton(
                onClick = onEliminar,
                modifier = Modifier.size(34.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Eliminar producto",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun DialogoEditarVentaDia(
    venta: VentaDiaUi,
    cantidadTexto: String,
    extraTexto: String,
    descuentoTexto: String,
    notaTexto: String,
    mensajeError: String?,
    editando: Boolean,
    formatear: (Long) -> String,
    onCantidadChange: (String) -> Unit,
    onExtraChange: (String) -> Unit,
    onDescuentoChange: (String) -> Unit,
    onNotaChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onCerrar: () -> Unit
) {
    var mostrarAjustes by remember {
        mutableStateOf(extraTexto.isNotBlank() || descuentoTexto.isNotBlank())
    }

    var mostrarNota by remember {
        mutableStateOf(notaTexto.isNotBlank())
    }

    AlertDialog(
        onDismissRequest = {
            if (!editando) onCerrar()
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Editar producto",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onCerrar,
                    enabled = !editando,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = venta.nombreProducto,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Actual: ${formatear(venta.totalCentavos)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Cantidad",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = cantidadTexto,
                    onValueChange = onCantidadChange,
                    label = { Text("Nueva cantidad") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !editando
                )

                if (!mostrarAjustes) {
                    TextButton(
                        onClick = { mostrarAjustes = true },
                        enabled = !editando,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text("Agregar extra o descuento")
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Ajustes opcionales",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            mensajeError?.let { mensaje ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = mensaje,
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = extraTexto,
                                onValueChange = onExtraChange,
                                label = { Text("Extra") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !editando
                            )

                            OutlinedTextField(
                                value = descuentoTexto,
                                onValueChange = onDescuentoChange,
                                label = { Text("Descuento") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !editando
                            )
                        }
                    }
                }

                if (!mostrarNota) {
                    TextButton(
                        onClick = { mostrarNota = true },
                        enabled = !editando,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text("Agregar nota opcional")
                    }
                } else {
                    OutlinedTextField(
                        value = notaTexto,
                        onValueChange = onNotaChange,
                        label = { Text("Nota opcional") },
                        placeholder = { Text("Ej. Ajuste por corrección manual") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        enabled = !editando
                    )
                }
            }
        },
        confirmButton = {
            BotonGuardarConPulsoVerde(
                texto = if (editando) "GUARDANDO..." else "GUARDAR CAMBIOS",
                guardando = editando,
                onClick = onGuardar
            )
        },
        dismissButton = {}
    )
}

@Composable
private fun DialogoCancelarGrupoVenta(
    grupo: GrupoVentaDiaUi,
    notaTexto: String,
    cancelando: Boolean,
    formatear: (Long) -> String,
    onNotaChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCerrar: () -> Unit
) {
    var mostrarMotivo by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            if (!cancelando) onCerrar()
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cancelar transacción",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onCerrar,
                    enabled = !cancelando,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Se cancelarán todos los productos de esta transacción.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Total: ${formatear(grupo.totalCentavos)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            text = "${grupo.productosDistintos} productos · ${grupo.piezas} piezas",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (!mostrarMotivo) {
                    TextButton(
                        onClick = { mostrarMotivo = true },
                        enabled = !cancelando,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text("Agregar motivo opcional")
                    }
                } else {
                    OutlinedTextField(
                        value = notaTexto,
                        onValueChange = onNotaChange,
                        label = { Text("Motivo opcional") },
                        placeholder = { Text("Ej. Error al capturar la venta") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        enabled = !cancelando
                    )
                }
            }
        },
        confirmButton = {
            BotonCancelarConPulsoRojo(
                texto = if (cancelando) "CANCELANDO..." else "CANCELAR TRANSACCIÓN",
                cancelando = cancelando,
                onClick = onConfirmar
            )
        },
        dismissButton = {}
    )
}

@Composable
private fun DialogoCancelarProductoVenta(
    venta: VentaDiaUi,
    notaTexto: String,
    cancelando: Boolean,
    formatear: (Long) -> String,
    onNotaChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCerrar: () -> Unit
) {
    var mostrarMotivo by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            if (!cancelando) onCerrar()
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Eliminar producto",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onCerrar,
                    enabled = !cancelando,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Este producto se cancelará dentro de la transacción. Los demás productos seguirán activos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = venta.nombreProducto,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            text = "${venta.cantidad} piezas · ${formatear(venta.totalCentavos)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (!mostrarMotivo) {
                    TextButton(
                        onClick = { mostrarMotivo = true },
                        enabled = !cancelando,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text("Agregar motivo opcional")
                    }
                } else {
                    OutlinedTextField(
                        value = notaTexto,
                        onValueChange = onNotaChange,
                        label = { Text("Motivo opcional") },
                        placeholder = { Text("Ej. Producto agregado por error") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        enabled = !cancelando
                    )
                }
            }
        },
        confirmButton = {
            BotonCancelarConPulsoRojo(
                texto = if (cancelando) "ELIMINANDO..." else "ELIMINAR PRODUCTO",
                cancelando = cancelando,
                onClick = onConfirmar
            )
        },
        dismissButton = {}
    )
}

@Composable
private fun BotonGuardarConPulsoVerde(
    texto: String,
    guardando: Boolean,
    onClick: () -> Unit
) {
    val verdePulso = Color(0xFF86EFAC)
    val verdeBase = Color(0xFF16A34A)

    val escalaPulso by animateFloatAsState(
        targetValue = if (guardando) 1.9f else 0.05f,
        animationSpec = tween(durationMillis = 1000),
        label = "escalaPulsoVerdeGuardar"
    )

    Button(
        onClick = {
            if (!guardando) onClick()
        },
        enabled = !guardando,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = verdeBase,
            contentColor = Color.White,
            disabledContainerColor = verdeBase,
            disabledContentColor = Color.White
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (guardando) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(42.dp)
                        .scale(escalaPulso)
                        .background(
                            color = verdePulso.copy(alpha = 0.45f),
                            shape = CircleShape
                        )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.4.dp
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = texto,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun BotonCancelarConPulsoRojo(
    texto: String,
    cancelando: Boolean,
    onClick: () -> Unit
) {
    val rojoPulso = Color(0xFFFB7185)
    val rojoBase = Color(0xFFDC2626)

    val escalaPulso by animateFloatAsState(
        targetValue = if (cancelando) 1.9f else 0.05f,
        animationSpec = tween(durationMillis = 1000),
        label = "escalaPulsoRojoCancelar"
    )

    Button(
        onClick = {
            if (!cancelando) onClick()
        },
        enabled = !cancelando,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = rojoBase,
            contentColor = Color.White,
            disabledContainerColor = rojoBase,
            disabledContentColor = Color.White
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (cancelando) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(42.dp)
                        .scale(escalaPulso)
                        .background(
                            color = rojoPulso.copy(alpha = 0.45f),
                            shape = CircleShape
                        )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (cancelando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.4.dp
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = texto,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun BarraResumenVentasDia(
    total: String,
    transacciones: Int,
    piezas: Int
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 20.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total activo del día",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "$transacciones transacciones · $piezas piezas",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = total,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun MensajeVentasDia(
    texto: String,
    esError: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = if (esError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        contentColor = if (esError) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        },
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun PantallaCargandoVentasDia() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Cargando ventas del día...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EstadoVentasDiaVacias() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 260.dp)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No hay transacciones abiertas.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Cuando registres ventas, aparecerán agrupadas aquí por transacción.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}