package com.ventaking.app.presentacion.pantallas.productos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ventaking.app.dominio.modelo.ProductoRapido
import java.util.Locale

private val BackgroundDark = Color(0xFF0F172A)
private val SurfaceDark = Color(0xFF182235)
private val OutlineDark = Color(0xFF4B5A75)
private val OnSurfaceDark = Color(0xFFF3F6FC)
private val PrimaryDark = Color(0xFF9BB8FF)
private val SurfaceVariantDark = Color(0xFF253149)
private val OnSurfaceVariantDark = Color(0xFFC1CADB)
private val TertiaryContainerDark = Color(0xFF7A4F0E)
private val OnTertiaryContainerDark = Color(0xFFF8E7C0)
private val BackgroundLight = Color(0xFFF6F8FC)
private val SurfaceLight = Color.White
private val OutlineLight = Color(0xFFD5DBE8)
private val PrimaryLight = Color(0xFF2F6FED)
private val SurfaceVariantLight = Color(0xFFF1F4FA)
private val OnSurfaceVariantLight = Color(0xFF5C667A)
private val ErrorLight = Color(0xFFD92D20)
private val PrimaryContainer = Color(0xFF1E3F8F)
private val OnPrimaryContainer = Color(0xFFDCE7FF)
private val SuccessContainer = Color(0xFFDFF8EA)
private val SuccessText = Color(0xFF145A32)
private val InactiveContainer = Color(0xFFE8ECF4)
private val InactiveText = Color(0xFF5C667A)
private val CardShape = RoundedCornerShape(16.dp)

@Composable
fun ProductosScreen(viewModel: ProductosViewModel, onVolver: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Box(Modifier.fillMaxSize().safeDrawingPadding()) {
        ProductosGrid(
            uiState = uiState,
            onVolver = onVolver,
            onNuevo = viewModel::abrirNuevoProducto,
            onEditar = viewModel::abrirEdicion,
            onCambiarEstado = viewModel::pedirConfirmacionCambioEstado,
            onSeleccionarNegocio = viewModel::seleccionarNegocio,
            onCambiarFiltro = viewModel::cambiarFiltro
        )

        FloatingActionButton(
            onClick = viewModel::abrirNuevoProducto,
            modifier = Modifier.align(Alignment.BottomEnd).padding(22.dp),
            shape = CircleShape,
            containerColor = appColors().primary,
            contentColor = if (isSystemInDarkTheme()) BackgroundDark else Color.White
        ) {
            Text("+", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        ProductoEditorModal(
            uiState = uiState,
            onCerrar = viewModel::cerrarFormulario,
            onGuardar = viewModel::guardarProducto,
            onNombreChange = viewModel::cambiarNombre,
            onPrecioChange = viewModel::cambiarPrecio,
            onCategoriaChange = viewModel::cambiarCategoria,
            onPromocionChange = viewModel::cambiarEsPromocion
        )

        ConfirmacionEstadoModal(
            producto = uiState.productoParaConfirmar,
            onCancelar = viewModel::cancelarConfirmacion,
            onConfirmar = viewModel::confirmarCambioEstado
        )
    }
}

@Composable
private fun ProductosGrid(
    uiState: ProductosUiState,
    onVolver: () -> Unit,
    onNuevo: () -> Unit,
    onEditar: (ProductoRapido) -> Unit,
    onCambiarEstado: (ProductoRapido) -> Unit,
    onSeleccionarNegocio: (String) -> Unit,
    onCambiarFiltro: (FiltroProducto) -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val columnas = if (maxWidth < 360.dp) 1 else 2
        Surface(Modifier.fillMaxSize(), color = appColors().background) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnas),
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                fullWidth { HeaderSection(onVolver) }
                fullWidth { NegociosSection(uiState, onSeleccionarNegocio) }
                fullWidth { FiltroStatsBar(uiState, onCambiarFiltro) }

                if (!uiState.mensaje.isNullOrBlank() || !uiState.error.isNullOrBlank()) {
                    fullWidth { FeedbackBanner(uiState.mensaje, uiState.error) }
                }

                if (uiState.productosVisibles.isEmpty()) {
                    fullWidth { EmptyCard(uiState.filtro) }
                } else {
                    items(uiState.productosVisibles, key = { it.id }) { producto ->
                        ProductoGridCard(
                            producto = producto,
                            onEditar = { onEditar(producto) },
                            onCambiarEstado = { onCambiarEstado(producto) }
                        )
                    }
                }

                fullWidth { Spacer(Modifier.height(84.dp)) }
            }
        }
    }
}

@Composable
private fun HeaderSection(onVolver: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "Productos rápidos",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Administra lo que aparecerá en venta rápida.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) { Text("Volver", fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
private fun NegociosSection(uiState: ProductosUiState, onSeleccionarNegocio: (String) -> Unit) {
    val colors = appColors()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Negocio activo",
            style = MaterialTheme.typography.labelLarge,
            color = colors.mutedText,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            uiState.negocios.forEach { negocio ->
                val seleccionado = negocio.id == uiState.negocioIdSeleccionado

                Surface(
                    onClick = { onSeleccionarNegocio(negocio.id) },
                    modifier = Modifier.widthIn(min = 132.dp),
                    shape = CardShape,
                    color = if (seleccionado) colors.primary.copy(alpha = 0.14f) else colors.card,
                    border = BorderStroke(1.dp, if (seleccionado) colors.primary else colors.outline),
                    tonalElevation = if (seleccionado) 2.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(shape = CircleShape, color = if (seleccionado) colors.primary else colors.outline) {
                            Box(Modifier.width(10.dp).height(10.dp))
                        }
                        Column {
                            Text(
                                negocio.nombre,
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.title,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                if (seleccionado) "Seleccionado" else "Toca para cambiar",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.mutedText,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltroStatsBar(uiState: ProductosUiState, onCambiarFiltro: (FiltroProducto) -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatFilterChip("Activos (${uiState.activos.size})", uiState.filtro == FiltroProducto.ACTIVOS) {
                onCambiarFiltro(FiltroProducto.ACTIVOS)
            }
            StatFilterChip("Inactivos (${uiState.inactivos.size})", uiState.filtro == FiltroProducto.INACTIVOS) {
                onCambiarFiltro(FiltroProducto.INACTIVOS)
            }
            StatFilterChip("Todos (${uiState.productos.size})", uiState.filtro == FiltroProducto.TODOS) {
                onCambiarFiltro(FiltroProducto.TODOS)
            }
        }
    }
}

@Composable
private fun StatFilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = appColors()

    FilterChip(
        selected = selected,
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        label = { Text(text, fontWeight = FontWeight.Bold, maxLines = 1) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = colors.primary,
            selectedLabelColor = if (isSystemInDarkTheme()) BackgroundDark else Color.White,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = colors.primary
        )
    )
}

@Composable
private fun ProductoGridCard(producto: ProductoRapido, onEditar: () -> Unit, onCambiarEstado: () -> Unit) {
    var menuAbierto by remember { mutableStateOf(false) }
    val colors = appColors()
    val categoria = producto.categoria?.trim().orEmpty()
    val tieneCategoria = categoria.isNotBlank()

    Surface(
        modifier = Modifier.fillMaxWidth().heightIn(min = 166.dp),
        shape = CardShape,
        color = colors.card,
        border = BorderStroke(1.dp, colors.outline),
        tonalElevation = 2.dp
    ) {
        Box(Modifier.fillMaxWidth().heightIn(min = 166.dp)) {
            Box(Modifier.align(Alignment.TopEnd)) {
                TextButton(
                    onClick = { menuAbierto = true },
                    contentPadding = ButtonDefaults.TextButtonContentPadding
                ) {
                    Text("⋯", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.mutedText)
                }

                DropdownMenu(
                    expanded = menuAbierto,
                    onDismissRequest = { menuAbierto = false },
                    offset = DpOffset(x = (-8).dp, y = 0.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            menuAbierto = false
                            onEditar()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (producto.estaActivo) "Pausar" else "Reactivar") },
                        onClick = {
                            menuAbierto = false
                            onCambiarEstado()
                        }
                    )
                }
            }

            Column(
                modifier = Modifier.align(Alignment.Center).fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 42.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (tieneCategoria) Arrangement.spacedBy(8.dp, Alignment.CenterVertically) else Arrangement.Center
            ) {
                Text(
                    producto.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    formatearPrecio(producto.precioCentavos),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.primary,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                if (tieneCategoria) {
                    Text(
                        categoria,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.mutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    EstadoPill(producto.estaActivo)
                    if (producto.esPromocion) PromoPill()
                }
            }
        }
    }
}

@Composable
private fun EstadoPill(activo: Boolean) {
    Surface(shape = MaterialTheme.shapes.extraLarge, color = if (activo) SuccessContainer else InactiveContainer) {
        Text(
            text = if (activo) "Activo" else "Inactivo",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (activo) SuccessText else InactiveText,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PromoPill() {
    val isDark = isSystemInDarkTheme()
    Surface(shape = MaterialTheme.shapes.extraLarge, color = if (isDark) TertiaryContainerDark else Color(0xFFFFE8B8)) {
        Text(
            "Promo",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (isDark) OnTertiaryContainerDark else Color(0xFF7A4F0E),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FeedbackBanner(mensaje: String?, error: String?) {
    val esError = !error.isNullOrBlank()
    val texto = if (esError) error.orEmpty() else mensaje.orEmpty()

    ElevatedCard(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (esError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(
            texto,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (esError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun EmptyCard(filtro: FiltroProducto) {
    val texto = when (filtro) {
        FiltroProducto.ACTIVOS -> "No hay productos activos."
        FiltroProducto.INACTIVOS -> "No hay productos inactivos."
        FiltroProducto.TODOS -> "No hay productos registrados."
    }

    ElevatedCard(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Text(
            texto,
            modifier = Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProductoEditorModal(
    uiState: ProductosUiState,
    onCerrar: () -> Unit,
    onGuardar: () -> Unit,
    onNombreChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onCategoriaChange: (String) -> Unit,
    onPromocionChange: (Boolean) -> Unit
) {
    if (!uiState.formularioVisible) return

    Dialog(onDismissRequest = onCerrar) {
        Surface(
            modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp).imePadding(),
            shape = MaterialTheme.shapes.extraLarge,
            color = appColors().card,
            tonalElevation = 8.dp
        ) {
            Column {
                ModalHeader(
                    title = if (uiState.estaEditando) "Editar producto" else "Nuevo producto",
                    subtitle = if (uiState.estaEditando) "Actualiza la información del producto seleccionado." else "Agrega un producto rápido para usarlo en la venta."
                )
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    VentaKingTextField(uiState.nombre, onNombreChange, "Nombre", "Ej. Quesadilla", "🏷", required = true)
                    VentaKingTextField(
                        value = uiState.precio,
                        onValueChange = onPrecioChange,
                        label = "Precio",
                        placeholder = "0.00",
                        iconText = "$",
                        required = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    VentaKingTextField(uiState.categoria, onCategoriaChange, "Categoría", "Seleccionar categoría", "▦", trailingText = "⌄")
                    PromoBlock(uiState.esPromocion, onPromocionChange)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TextButton(
                            onClick = onCerrar,
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = ButtonDefaults.textButtonColors(contentColor = appColors().mutedText)
                        ) { Text("Cancelar", fontWeight = FontWeight.SemiBold) }
                        Button(
                            onClick = onGuardar,
                            enabled = uiState.puedeGuardar,
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryContainer,
                                contentColor = OnPrimaryContainer,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) { Text(if (uiState.estaCargando) "Guardando..." else "Guardar", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PromoBlock(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = appColors().field) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("¿Es promoción?", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("Márcalo si será una promo o combo rápido.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = if (isSystemInDarkTheme()) BackgroundDark else Color.White,
                    checkedTrackColor = PrimaryDark
                )
            )
        }
    }
}

@Composable
private fun VentaKingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    iconText: String,
    required: Boolean = false,
    trailingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val colors = appColors()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { RequiredLabel(label, required, colors.mutedText) },
        placeholder = { Text(placeholder, color = colors.mutedText) },
        leadingIcon = { Text(iconText, color = colors.mutedText, fontWeight = FontWeight.SemiBold) },
        trailingIcon = trailingText?.let { text -> { Text(text, color = colors.mutedText, fontWeight = FontWeight.Bold) } },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.title,
            unfocusedTextColor = colors.title,
            focusedContainerColor = colors.field,
            unfocusedContainerColor = colors.field,
            disabledContainerColor = colors.field,
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.outline,
            focusedLabelColor = colors.mutedText,
            unfocusedLabelColor = colors.mutedText,
            cursorColor = colors.primary
        ),
        shape = CardShape
    )
}

@Composable
private fun RequiredLabel(text: String, required: Boolean, color: Color) {
    Text(
        buildAnnotatedString {
            append(text)
            if (required) {
                append(" ")
                withStyle(SpanStyle(color = ErrorLight, fontWeight = FontWeight.Bold)) { append("*") }
            }
        },
        color = color
    )
}

@Composable
private fun ConfirmacionEstadoModal(producto: ProductoRapido?, onCancelar: () -> Unit, onConfirmar: () -> Unit) {
    producto ?: return
    val activo = producto.estaActivo

    Dialog(onDismissRequest = onCancelar) {
        Surface(
            modifier = Modifier.fillMaxWidth().widthIn(max = 460.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column {
                ModalHeader(
                    title = if (activo) "Pausar producto" else "Reactivar producto",
                    subtitle = producto.nombre,
                    warning = activo
                )
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        if (activo) "Ya no aparecerá en venta rápida, pero seguirá guardado." else "Volverá a aparecer en venta rápida.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onCancelar, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.extraLarge) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = onConfirmar,
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activo) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                contentColor = if (activo) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary
                            )
                        ) { Text(if (activo) "Pausar" else "Reactivar", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModalHeader(title: String, subtitle: String, warning: Boolean = false) {
    val container = if (warning) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
    val content = if (warning) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer

    Column(
        modifier = Modifier.fillMaxWidth().background(container).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = content, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = content.copy(alpha = 0.88f))
    }
}

private fun LazyGridScope.fullWidth(content: @Composable () -> Unit) {
    item(span = { GridItemSpan(maxLineSpan) }) { content() }
}

private fun formatearPrecio(precioCentavos: Long): String {
    return "$" + String.format(Locale.US, "%.2f", precioCentavos / 100.0)
}

@Composable
private fun appColors(): VentaKingScreenColors {
    val dark = isSystemInDarkTheme()
    return VentaKingScreenColors(
        background = if (dark) BackgroundDark else BackgroundLight,
        card = if (dark) SurfaceDark else SurfaceLight,
        outline = if (dark) OutlineDark else OutlineLight,
        title = if (dark) OnSurfaceDark else MaterialTheme.colorScheme.onSurface,
        primary = if (dark) PrimaryDark else PrimaryLight,
        field = if (dark) SurfaceVariantDark else SurfaceVariantLight,
        mutedText = if (dark) OnSurfaceVariantDark else OnSurfaceVariantLight
    )
}

private data class VentaKingScreenColors(
    val background: Color,
    val card: Color,
    val outline: Color,
    val title: Color,
    val primary: Color,
    val field: Color,
    val mutedText: Color
)
