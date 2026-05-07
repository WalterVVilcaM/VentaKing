package com.ventaking.app.dominio.repositorio

import com.ventaking.app.datos.local.entidades.DispositivoEntity
import com.ventaking.app.dominio.modelo.Dispositivo
import kotlinx.coroutines.flow.Flow

interface DispositivoRepository {

    suspend fun crearDispositivoSiNoExiste()

    suspend fun obtenerDispositivoActual(): DispositivoEntity?

    suspend fun obtenerIdDispositivoActual(): String?

    suspend fun actualizarNombreDispositivo(nombreDispositivo: String?)

    fun observarDispositivoActual(): Flow<Dispositivo?>
}