package com.ventaking.app.nucleo.constantes

object VentaEstado {
    const val ACTIVE = "ACTIVE"
    const val CANCELLED = "CANCELLED"
    const val CLOSED = "CLOSED"
    const val CORRECTED = "CORRECTED"
}

object CorteEstado {
    const val CLOSED = "CLOSED"
    const val PENDING_SYNC = "PENDING_SYNC"
    const val SYNCED = "SYNCED"
    const val SYNC_ERROR = "SYNC_ERROR"
}

object SyncEstado {
    const val LOCAL_ONLY = "LOCAL_ONLY"
    const val PENDING_SYNC = "PENDING_SYNC"
    const val SYNCED = "SYNCED"
    const val SYNC_ERROR = "SYNC_ERROR"
}

object HistorialAccion {
    const val CREATED = "CREATED"
    const val UPDATED = "UPDATED"
    const val CANCELLED = "CANCELLED"
    const val CLOSED = "CLOSED"
    const val CORRECTED = "CORRECTED"
}

object TemaApp {
    const val SISTEMA = "SISTEMA"
    const val CLARO = "CLARO"
    const val OSCURO = "OSCURO"
}