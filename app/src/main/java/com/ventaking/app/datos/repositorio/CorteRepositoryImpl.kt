package com.ventaking.app.datos.repositorio

import androidx.room.withTransaction
import com.ventaking.app.datos.local.base.AppDatabase
import com.ventaking.app.datos.local.dao.CorteDiarioDao
import com.ventaking.app.datos.local.dao.HistorialVentaDao
import com.ventaking.app.datos.local.dao.VentaDao
import com.ventaking.app.datos.local.entidades.CorteDiarioEntity
import com.ventaking.app.datos.local.entidades.HistorialVentaEntity
import com.ventaking.app.datos.local.entidades.VentaEntity
import com.ventaking.app.dominio.modelo.CorteDiario
import com.ventaking.app.dominio.modelo.Venta
import com.ventaking.app.dominio.repositorio.CorteRepository
import com.ventaking.app.dominio.repositorio.ResultadoCrearCorteDiario
import com.ventaking.app.nucleo.constantes.CorteEstado
import com.ventaking.app.nucleo.constantes.HistorialAccion
import com.ventaking.app.nucleo.constantes.SyncEstado
import com.ventaking.app.nucleo.constantes.VentaEstado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class CorteRepositoryImpl(
    private val database: AppDatabase,
    private val corteDiarioDao: CorteDiarioDao,
    private val ventaDao: VentaDao,
    private val historialVentaDao: HistorialVentaDao
) : CorteRepository {

    override fun observarVentasAbiertasParaCorte(
        negocioId: String,
        fechaCorte: String,
        dispositivoId: String
    ): Flow<List<Venta>> {
        return ventaDao.observarVentasAbiertasParaCorte(
            negocioId = negocioId,
            fechaVenta = fechaCorte,
            dispositivoId = dispositivoId,
            estado = VentaEstado.ACTIVE
        ).map { ventas ->
            ventas.map { it.toDomain() }
        }
    }

    override suspend fun obtenerCorteExistente(
        negocioId: String,
        fechaCorte: String,
        dispositivoId: String
    ): CorteDiario? {
        return corteDiarioDao.obtenerPorNegocioFechaDispositivo(
            negocioId = negocioId,
            fechaCorte = fechaCorte,
            dispositivoId = dispositivoId
        )?.toDomain()
    }

    override suspend fun crearCorteDiario(
        negocioId: String,
        fechaCorte: String,
        dispositivoId: String
    ): ResultadoCrearCorteDiario {
        return try {
            database.withTransaction {
                val corteExistente = corteDiarioDao.obtenerPorNegocioFechaDispositivo(
                    negocioId = negocioId,
                    fechaCorte = fechaCorte,
                    dispositivoId = dispositivoId
                )

                if (corteExistente != null) {
                    return@withTransaction ResultadoCrearCorteDiario.YaExiste(
                        corte = corteExistente.toDomain()
                    )
                }

                val ventasParaCerrar = ventaDao.obtenerVentasAbiertasParaCorte(
                    negocioId = negocioId,
                    fechaVenta = fechaCorte,
                    dispositivoId = dispositivoId,
                    estado = VentaEstado.ACTIVE
                )

                if (ventasParaCerrar.isEmpty()) {
                    return@withTransaction ResultadoCrearCorteDiario.SinVentas
                }

                val fechaActual = System.currentTimeMillis()
                val corteId = UUID.randomUUID().toString()

                val totalCentavos = ventasParaCerrar.sumOf { it.totalCentavos }
                val totalVentas = ventasParaCerrar.map { it.grupoVentaId }.distinct().size
                val totalPiezas = ventasParaCerrar.sumOf { it.cantidad }

                val corte = CorteDiarioEntity(
                    id = corteId,
                    negocioId = negocioId,
                    fechaCorte = fechaCorte,
                    dispositivoId = dispositivoId,
                    totalCentavos = totalCentavos,
                    totalVentas = totalVentas,
                    totalPiezas = totalPiezas,
                    creadoEn = fechaActual,
                    cerradoEn = fechaActual,
                    estado = CorteEstado.CLOSED,
                    syncEstado = SyncEstado.PENDING_SYNC,
                    sincronizadoEn = null,
                    mensajeError = null
                )

                corteDiarioDao.insertar(corte)

                ventaDao.cerrarVentasConCorte(
                    ventaIds = ventasParaCerrar.map { it.id },
                    corteId = corteId,
                    nuevoEstado = VentaEstado.CLOSED,
                    syncEstado = SyncEstado.PENDING_SYNC,
                    actualizadoEn = fechaActual
                )

                val historiales = ventasParaCerrar.map { venta ->
                    HistorialVentaEntity(
                        id = UUID.randomUUID().toString(),
                        ventaId = venta.id,
                        grupoVentaId = venta.grupoVentaId,
                        negocioId = venta.negocioId,
                        dispositivoId = venta.dispositivoId,
                        tipoAccion = HistorialAccion.CLOSED,
                        totalAnteriorCentavos = venta.totalCentavos,
                        totalNuevoCentavos = venta.totalCentavos,
                        cantidadAnterior = venta.cantidad,
                        cantidadNueva = venta.cantidad,
                        nota = "Venta cerrada en corte diario.",
                        creadoEn = fechaActual,
                        syncEstado = SyncEstado.PENDING_SYNC
                    )
                }

                historialVentaDao.insertarTodos(historiales)

                ResultadoCrearCorteDiario.Exito(
                    corte = corte.toDomain()
                )
            }
        } catch (e: Exception) {
            ResultadoCrearCorteDiario.Error(
                mensaje = e.message ?: "No se pudo crear el corte diario."
            )
        }
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
}