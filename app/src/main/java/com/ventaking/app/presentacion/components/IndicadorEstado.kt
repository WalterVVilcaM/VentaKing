package com.ventaking.app.presentacion.componentes

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class TipoIndicadorEstado {
    INFO,
    EXITO,
    ADVERTENCIA,
    ERROR
}

@Composable
fun IndicadorEstado(
    texto: String,
    tipo: TipoIndicadorEstado,
    modifier: Modifier = Modifier
) {
    val containerColor = when (tipo) {
        TipoIndicadorEstado.INFO -> MaterialTheme.colorScheme.primary
        TipoIndicadorEstado.EXITO -> MaterialTheme.colorScheme.secondary
        TipoIndicadorEstado.ADVERTENCIA -> MaterialTheme.colorScheme.tertiary
        TipoIndicadorEstado.ERROR -> MaterialTheme.colorScheme.error
    }

    val contentColor = when (tipo) {
        TipoIndicadorEstado.INFO -> MaterialTheme.colorScheme.onPrimary
        TipoIndicadorEstado.EXITO -> MaterialTheme.colorScheme.onSecondary
        TipoIndicadorEstado.ADVERTENCIA -> MaterialTheme.colorScheme.onTertiary
        TipoIndicadorEstado.ERROR -> MaterialTheme.colorScheme.onError
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}