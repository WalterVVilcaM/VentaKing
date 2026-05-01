package com.ventaking.app.dominio.repositorio

import com.ventaking.app.datos.local.entidades.DispositivoEntity

interface DispositivoRepository {

    suspend fun crearDispositivoSiNoExiste()

    suspend fun obtenerDispositivoActual(): DispositivoEntity?

    suspend fun obtenerIdDispositivoActual(): String?

    suspend fun actualizarNombreDispositivo(nombreDispositivo: String?)
}