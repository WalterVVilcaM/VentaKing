package com.ventaking.app.datos.repositorio

import com.ventaking.app.datos.local.dao.NegocioDao
import com.ventaking.app.datos.local.entidades.NegocioEntity
import com.ventaking.app.dominio.repositorio.NegocioRepository
import java.util.UUID

class NegocioRepositoryImpl(
    private val negocioDao: NegocioDao
) : NegocioRepository {

    override suspend fun contarNegocios(): Int {
        return negocioDao.contar()
    }

    override suspend fun insertarNegociosBaseSiNoExisten() {
        val totalNegocios = negocioDao.contar()

        if (totalNegocios > 0) return

        val fechaActual = System.currentTimeMillis()

        val negociosBase = listOf(
            NegocioEntity(
                id = UUID.randomUUID().toString(),
                nombre = "Paletería",
                estaActivo = true,
                creadoEn = fechaActual,
                actualizadoEn = fechaActual
            ),
            NegocioEntity(
                id = UUID.randomUUID().toString(),
                nombre = "Comida",
                estaActivo = true,
                creadoEn = fechaActual,
                actualizadoEn = fechaActual
            )
        )

        negocioDao.insertarTodos(negociosBase)
    }
}