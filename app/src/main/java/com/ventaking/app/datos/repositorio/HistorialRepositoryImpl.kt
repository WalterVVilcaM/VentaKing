package com.ventaking.app.datos.repositorio

import com.ventaking.app.datos.local.dao.CorteDiarioDao
import com.ventaking.app.datos.local.dao.HistorialVentaDao
import com.ventaking.app.datos.local.dao.VentaDao
import com.ventaking.app.datos.local.entidades.CorteDiarioEntity
import com.ventaking.app.datos.local.entidades.HistorialVentaEntity
import com.ventaking.app.datos.local.entidades.VentaEntity
import com.ventaking.app.dominio.modelo.CorteDiario
import com.ventaking.app.dominio.modelo.HistorialVenta
import com.ventaking.app.dominio.modelo.Venta
import com.ventaking.app.dominio.repositorio.HistorialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistorialRepositoryImpl(
    private val ventaDao: VentaDao,
    private val corteDiarioDao: CorteDiarioDao,
    private val historialVentaDao: HistorialVentaDao
) : HistorialRepository {

    override fun observarVentasHistorial(
        negocioId: String,
        fechaVenta: String,
        dispositivoId: String?,
        estado: String?
    ): Flow<List<Venta>> {
        val ventasFlow = when {
            !dispositivoId.isNullOrBlank() && !estado.isNullOrBlank() -> {
                ventaDao.observarPorNegocioFechaDispositivoYEstado(
                    negocioId = negocioId,
                    fechaVenta = fechaVenta,
                    dispositivoId = dispositivoId,
                    estado = estado
                )
            }

            !dispositivoId.isNullOrBlank() -> {
                ventaDao.observarPorNegocioFechaYDispositivo(
                    negocioId = negocioId,
                    fechaVenta = fechaVenta,
                    dispositivoId = dispositivoId
                )
            }

            !estado.isNullOrBlank() -> {
                ventaDao.observarPorNegocioFechaYEstado(
                    negocioId = negocioId,
                    fechaVenta = fechaVenta,
                    estado = estado
                )
            }

            else -> {
                ventaDao.observarPorNegocioYFecha(
                    negocioId = negocioId,
                    fechaVenta = fechaVenta
                )
            }
        }

        return ventasFlow.map { ventas ->
            ventas.map { it.toDomain() }
        }
    }

    override fun observarCortesHistorial(
        negocioId: String,
        fechaCorte: String,
        dispositivoId: String?
    ): Flow<List<CorteDiario>> {
        val cortesFlow = if (dispositivoId.isNullOrBlank()) {
            corteDiarioDao.observarPorFechaYNegocio(
                fechaCorte = fechaCorte,
                negocioId = negocioId
            )
        } else {
            corteDiarioDao.observarPorFechaNegocioYDispositivo(
                fechaCorte = fechaCorte,
                negocioId = negocioId,
                dispositivoId = dispositivoId
            )
        }

        return cortesFlow.map { cortes ->
            cortes.map { it.toDomain() }
        }
    }

    override fun observarHistorialDeVenta(
        ventaId: String
    ): Flow<List<HistorialVenta>> {
        return historialVentaDao.observarPorVenta(ventaId).map { movimientos ->
            movimientos.map { it.toDomain() }
        }
    }

    override fun observarHistorialDeGrupoVenta(
        grupoVentaId: String
    ): Flow<List<HistorialVenta>> {
        return historialVentaDao.observarPorGrupoVenta(grupoVentaId).map { movimientos ->
            movimientos.map { it.toDomain() }
        }
    }

    override fun observarMovimientosHistorial(
        negocioId: String,
        dispositivoId: String?,
        desde: Long,
        hasta: Long
    ): Flow<List<HistorialVenta>> {
        val movimientosFlow = if (dispositivoId.isNullOrBlank()) {
            historialVentaDao.observarPorNegocioYRango(
                negocioId = negocioId,
                desde = desde,
                hasta = hasta
            )
        } else {
            historialVentaDao.observarPorNegocioDispositivoYRango(
                negocioId = negocioId,
                dispositivoId = dispositivoId,
                desde = desde,
                hasta = hasta
            )
        }

        return movimientosFlow.map { movimientos ->
            movimientos.map { it.toDomain() }
        }
    }

    private fun VentaEntity.toDomain(): Venta {
        return Venta(
            id = id,
            grupoVentaId = grupoVentaId,
            negocioId = negocioId,
            productoId = productoId,
            nombreProductoSnapshot = nombreProductoSnapshot,
            cantidad = cantidad,
            precioUnitarioSnapshotCentavos = precioUnitarioSnapshotCentavos,
            subtotalCentavos = subtotalCentavos,
            extraCentavos = extraCentavos,
            descuentoCentavos = descuentoCentavos,
            totalCentavos = totalCentavos,
            fechaVenta = fechaVenta,
            horaVenta = horaVenta,
            creadoEn = creadoEn,
            actualizadoEn = actualizadoEn,
            dispositivoId = dispositivoId,
            corteId = corteId,
            estado = estado,
            syncEstado = syncEstado
        )
    }

    private fun CorteDiarioEntity.toDomain(): CorteDiario {
        return CorteDiario(
            id = id,
            negocioId = negocioId,
            fechaCorte = fechaCorte,
            dispositivoId = dispositivoId,
            totalCentavos = totalCentavos,
            totalVentas = totalVentas,
            totalPiezas = totalPiezas,
            creadoEn = creadoEn,
            cerradoEn = cerradoEn,
            estado = estado,
            syncEstado = syncEstado,
            sincronizadoEn = sincronizadoEn,
            mensajeError = mensajeError
        )
    }

    private fun HistorialVentaEntity.toDomain(): HistorialVenta {
        return HistorialVenta(
            id = id,
            ventaId = ventaId,
            grupoVentaId = grupoVentaId,
            negocioId = negocioId,
            dispositivoId = dispositivoId,
            tipoAccion = tipoAccion,
            totalAnteriorCentavos = totalAnteriorCentavos,
            totalNuevoCentavos = totalNuevoCentavos,
            cantidadAnterior = cantidadAnterior,
            cantidadNueva = cantidadNueva,
            nota = nota,
            creadoEn = creadoEn,
            syncEstado = syncEstado
        )
    }
}