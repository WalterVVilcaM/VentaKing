package com.ventaking.app.presentacion.pantallas.venta

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Star
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
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

// ─────────────────────────────────────────────────────────────────────────────
// PANTALLA PRINCIPAL
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentaScreen(
    viewModel: VentaViewModel,
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val corteState by viewModel.corteState.collectAsState()

    LaunchedEffect(uiState.negocioSeleccionadoId) {
        viewModel.refrescarEstadoCorte()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Punto de Venta",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onVolver,
                        modifier = Modifier
                            .padding(start = 8.dp)
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
                    // Píldora "Total hoy" en el TopAppBar
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
            BarraCobroFija(
                total = viewModel.formatearCentavos(uiState.totalCentavos),
                habilitado = uiState.carrito.isNotEmpty() &&
                        !corteState.tieneCorteCerrado &&
                        !uiState.guardando &&
                        uiState.totalCentavos > 0L &&
                        uiState.mensajeError == null,
                guardando = uiState.guardando,
                exito = uiState.mensajeExito != null,
                onRegistrar = { viewModel.registrarVenta() },
                onNuevaVenta = { viewModel.limpiarMensajes() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (uiState.cargando) {
                PantallaCargandoVenta()
            } else {

                // ── Selector de negocio ──────────────────────────────────
                SelectorNegocioVenta(
                    uiState = uiState,
                    onSeleccionarNegocio = viewModel::seleccionarNegocio
                )

                if (corteState.tieneCorteCerrado) {
                    AvisoVentaBloqueadaPorCorte(
                        mensaje = corteState.mensaje
                            ?: "Este negocio ya tiene corte cerrado hoy. Las ventas nuevas se podrán registrar mañana."
                    )
                }

                // ── Mensajes de error o éxito ────────────────────────────
                uiState.mensajeError?.let { MensajeVenta(texto = it, esError = true) }
                uiState.mensajeExito?.let { MensajeVenta(texto = it, esError = false) }

                // ── Etiqueta "Productos" ─────────────────────────────────
                Text(
                    text = "Productos",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // ── Grid de productos o estado vacío ─────────────────────
                if (uiState.productos.isEmpty()) {
                    EstadoProductosVacios()
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = uiState.productos,
                            key = { producto -> producto.id }
                        ) { producto ->
                            ProductoGridCard(
                                nombre = producto.nombre,
                                precio = viewModel.formatearCentavos(producto.precioCentavos),
                                esPromocion = producto.esPromocion,
                                onClick = { viewModel.agregarAlCarrito(producto) }
                            )
                        }
                    }
                }

                // ── Panel del carrito (bottom sheet fijo) ────────────────
                ResumenCarritoSection(
                    items = uiState.carrito,
                    extra = uiState.extraTexto,
                    desc = uiState.descuentoTexto,
                    formatear = viewModel::formatearCentavos,
                    onModificar = viewModel::modificarCantidad,
                    onEliminar = viewModel::eliminarDelCarrito,
                    onExtraChange = viewModel::cambiarExtra,
                    onDescChange = viewModel::cambiarDescuento
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TARJETA DE PRODUCTO — altura fija, badge de promo como overlay
// El badge NO ocupa espacio en el layout → todas las cards tienen igual altura
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProductoGridCard(
    nombre: String,
    precio: String,
    esPromocion: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp),          // altura fija → grid uniforme siempre
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        // Box permite superponer el badge sin alterar el layout interior
        Box(modifier = Modifier.fillMaxSize()) {

            // ── Contenido principal ──────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = nombre,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = precio,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // ── Badge "Promo" superpuesto en esquina superior derecha ────
            // Al ser un overlay dentro del Box no afecta la altura de la card
            if (esPromocion) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            modifier = Modifier.size(8.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Promo",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SELECTOR DE NEGOCIO
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SelectorNegocioVenta(
    uiState: VentaUiState,
    onSeleccionarNegocio: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        uiState.negocios.forEach { negocio ->
            val seleccionado = negocio.id == uiState.negocioSeleccionadoId
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

// ─────────────────────────────────────────────────────────────────────────────
// AVISO DE CORTE CERRADO
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AvisoVentaBloqueadaPorCorte(
    mensaje: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Corte ya realizado",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PANEL DEL CARRITO — bottom sheet fijo sobre la barra de cobro
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ResumenCarritoSection(
    items: List<CartItem>,
    extra: String,
    desc: String,
    formatear: (Long) -> String,
    onModificar: (String, Int) -> Unit,
    onEliminar: (String) -> Unit,
    onExtraChange: (String) -> Unit,
    onDescChange: (String) -> Unit
) {
    var expandirAjustes by remember { mutableStateOf(false) }

    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

            // ── Encabezado del carrito ───────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Carrito",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Contador de items como badge
                    if (items.isNotEmpty()) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = items.size.toString(),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                TextButton(onClick = { expandirAjustes = !expandirAjustes }) {
                    Text(
                        text = if (expandirAjustes) "Ocultar ajustes" else "Ajustes",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // ── Ajustes colapsables (extra + descuento) ──────────────────
            AnimatedVisibility(
                visible = expandirAjustes,
                enter = expandVertically(tween(200)),
                exit = shrinkVertically(tween(200))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = extra,
                            onValueChange = onExtraChange,
                            label = { Text("Extra") },
                            placeholder = { Text("0.00") },
                            prefix = {
                                Text(
                                    text = "+$",
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        OutlinedTextField(
                            value = desc,
                            onValueChange = onDescChange,
                            label = { Text("Desc.") },
                            placeholder = { Text("0.00") },
                            prefix = {
                                Text(
                                    text = "-$",
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.error,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // ── Lista de items del carrito ───────────────────────────────
            if (items.isEmpty()) {
                Text(
                    text = "Toca un producto para agregarlo al carrito.",
                    modifier = Modifier.padding(vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(
                        items = items,
                        key = { item -> item.producto.id }
                    ) { item ->
                        CarritoItemRow(
                            item = item,
                            formatear = formatear,
                            onModificar = onModificar,
                            onEliminar = onEliminar
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FILA DE ITEM EN EL CARRITO
// Usa Icons.Rounded para +/- y ×, más táctiles y visualmente limpios
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CarritoItemRow(
    item: CartItem,
    formatear: (Long) -> String,
    onModificar: (String, Int) -> Unit,
    onEliminar: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Info del producto
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.producto.nombre,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${formatear(item.producto.precioCentavos)} c/u · Total ${formatear(item.totalItemCentavos)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Botón disminuir
        OutlinedIconButton(
            onClick = { onModificar(item.producto.id, -1) },
            modifier = Modifier.size(32.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = IconButtonDefaults.outlinedIconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.Remove,
                contentDescription = "Disminuir",
                modifier = Modifier.size(14.dp)
            )
        }

        // Cantidad
        Text(
            text = item.cantidad.toString(),
            modifier = Modifier.width(24.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        // Botón aumentar
        OutlinedIconButton(
            onClick = { onModificar(item.producto.id, 1) },
            modifier = Modifier.size(32.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = IconButtonDefaults.outlinedIconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Aumentar",
                modifier = Modifier.size(14.dp)
            )
        }

        // Botón eliminar
        IconButton(
            onClick = { onEliminar(item.producto.id) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Eliminar",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BARRA DE COBRO INFERIOR (sin cambios de lógica)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BarraCobroFija(
    total: String,
    habilitado: Boolean,
    guardando: Boolean,
    exito: Boolean,
    onRegistrar: () -> Unit,
    onNuevaVenta: () -> Unit
) {
    val verdeExito = Color(0xFF16A34A)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 20.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total a pagar",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when {
                            guardando -> "Registrando venta..."
                            exito     -> "Venta guardada correctamente"
                            else      -> "Listo para cobrar"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (guardando || exito) verdeExito
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = total,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = if (guardando || exito) verdeExito
                    else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            BotonVentaConPulsoVerde(
                habilitado = habilitado,
                guardando = guardando,
                exito = exito,
                onRegistrar = onRegistrar,
                onNuevaVenta = onNuevaVenta
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BOTÓN CON PULSO VERDE (sin cambios de lógica ni animación)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BotonVentaConPulsoVerde(
    habilitado: Boolean,
    guardando: Boolean,
    exito: Boolean,
    onRegistrar: () -> Unit,
    onNuevaVenta: () -> Unit
) {
    val verdeExito = Color(0xFF16A34A)
    val verdeBase  = Color(0xFF15803D)
    val botonActivo = habilitado || guardando || exito

    val escalaPulso by animateFloatAsState(
        targetValue = if (guardando || exito) 1.45f else 0.05f,
        animationSpec = tween(durationMillis = 850),
        label = "escalaPulsoVerdeVenta"
    )

    val textoBoton = when {
        guardando -> "REGISTRANDO..."
        exito     -> "VENTA EXITOSA - NUEVA"
        else      -> "REGISTRAR VENTA"
    }

    Button(
        onClick = {
            when {
                guardando -> Unit
                exito     -> onNuevaVenta()
                else      -> onRegistrar()
            }
        },
        enabled = botonActivo,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (guardando || exito) verdeBase
            else MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Efecto de pulso animado (estado guardando/éxito)
            if (guardando || exito) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .scale(scaleX = escalaPulso, scaleY = 2.3f)
                        .background(color = verdeExito, shape = CircleShape)
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
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                if (exito && !guardando) {
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.22f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "✓",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Text(
                    text = textoBoton,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ESTADOS DE LA PANTALLA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PantallaCargandoVenta() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Cargando venta...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EstadoProductosVacios() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No hay productos activos para este negocio.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Primero crea productos rápidos en la pantalla Productos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MensajeVenta(
    texto: String,
    esError: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = if (esError) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.secondaryContainer,
        contentColor = if (esError) MaterialTheme.colorScheme.onErrorContainer
        else MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}