package com.ventaking.app.trabajos

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.ventaking.app.datos.drive.DriveServiceProvider
import com.ventaking.app.datos.local.base.AppDatabase
import com.ventaking.app.datos.repositorio.SincronizacionRepositoryImpl
import com.ventaking.app.dominio.casos.sincronizacion.ReintentarSincronizacionUseCase
import com.ventaking.app.dominio.casos.sincronizacion.ResultadoReintentoSincronizacion
import com.ventaking.app.dominio.casos.sincronizacion.VerificarConexionUseCase
import com.ventaking.app.nucleo.constantes.GoogleDriveConfig
import com.ventaking.app.nucleo.red.MonitorConexion
import java.util.Collections

class ReintentarSincronizacionWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val monitorConexion = MonitorConexion(applicationContext)

        if (!monitorConexion.hayInternet()) {
            return Result.retry()
        }

        val driveServiceProvider = crearDriveServiceProvider()
            ?: return Result.success()

        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "ventaking_db"
        )
            .fallbackToDestructiveMigration()
            .build()

        return try {
            val repository = SincronizacionRepositoryImpl(
                registroArchivoSyncDao = database.registroArchivoSyncDao(),
                corteDiarioDao = database.corteDiarioDao(),
                dispositivoDao = database.dispositivoDao(),
                driveServiceProvider = driveServiceProvider
            )

            val useCase = ReintentarSincronizacionUseCase(
                sincronizacionRepository = repository,
                verificarConexionUseCase = VerificarConexionUseCase(monitorConexion)
            )

            val cortesPendientes = repository.obtenerCortesPendientes()

            var huboErrorTemporal = false

            cortesPendientes.forEach { corte ->
                when (useCase(corte.id)) {
                    is ResultadoReintentoSincronizacion.Exito -> Unit

                    ResultadoReintentoSincronizacion.SinInternet -> {
                        huboErrorTemporal = true
                    }

                    is ResultadoReintentoSincronizacion.Error -> {
                        Unit
                    }
                }
            }

            if (huboErrorTemporal) {
                Result.retry()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Result.retry()
        } finally {
            database.close()
        }
    }

    private fun crearDriveServiceProvider(): DriveServiceProvider? {
        val cuenta = GoogleSignIn.getLastSignedInAccount(applicationContext)

        if (cuenta == null || !GoogleSignIn.hasPermissions(cuenta, Scope(DriveScopes.DRIVE_FILE))) {
            return null
        }

        val credencial = GoogleAccountCredential.usingOAuth2(
            applicationContext,
            Collections.singleton(DriveScopes.DRIVE_FILE)
        )

        credencial.selectedAccount = cuenta.account

        val driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credencial
        )
            .setApplicationName(GoogleDriveConfig.APP_NAME)
            .build()

        return DriveServiceProvider().apply {
            configurar(driveService)
        }
    }
}