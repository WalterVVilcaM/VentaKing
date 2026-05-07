package com.ventaking.app.datos.drive

import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File as GoogleDriveFile
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DriveDataSource(
    private val driveService: Drive
) {

    suspend fun obtenerOCrearCarpeta(
        nombreCarpeta: String,
        parentId: String? = null
    ): ResultadoDrive = withContext(Dispatchers.IO) {
        try {
            val carpetaExistenteId = buscarCarpeta(
                nombreCarpeta = nombreCarpeta,
                parentId = parentId
            )

            if (carpetaExistenteId != null) {
                return@withContext ResultadoDrive.Exito(carpetaExistenteId)
            }

            val metadata = GoogleDriveFile().apply {
                name = nombreCarpeta
                mimeType = MIME_TYPE_FOLDER

                if (parentId != null) {
                    parents = listOf(parentId)
                }
            }

            val carpetaCreada = driveService.files()
                .create(metadata)
                .setFields("id")
                .execute()

            val id = carpetaCreada.id
                ?: return@withContext ResultadoDrive.Error("Drive no devolvió ID para la carpeta $nombreCarpeta.")

            ResultadoDrive.Exito(id)
        } catch (e: Exception) {
            ResultadoDrive.Error(
                mensaje = "No se pudo crear o encontrar la carpeta $nombreCarpeta en Drive.",
                excepcion = e
            )
        }
    }

    suspend fun subirArchivoSiNoExiste(
        carpetaDriveId: String,
        nombreArchivo: String,
        rutaLocal: String,
        hashArchivo: String,
        corteId: String,
        tipoArchivo: String
    ): ResultadoDrive = withContext(Dispatchers.IO) {
        try {
            val archivoLocal = File(rutaLocal)

            if (!archivoLocal.exists()) {
                return@withContext ResultadoDrive.Error(
                    mensaje = "No existe el archivo local: $rutaLocal"
                )
            }

            val archivoExistenteId = buscarArchivoExistente(
                carpetaDriveId = carpetaDriveId,
                nombreArchivo = nombreArchivo,
                hashArchivo = hashArchivo
            )

            if (archivoExistenteId != null) {
                return@withContext ResultadoDrive.Exito(archivoExistenteId)
            }

            val metadata = GoogleDriveFile().apply {
                name = nombreArchivo
                parents = listOf(carpetaDriveId)
                appProperties = mapOf(
                    "corteId" to corteId,
                    "tipoArchivo" to tipoArchivo,
                    "hashArchivo" to hashArchivo
                )
            }

            val contenido = FileContent(
                obtenerMimeType(nombreArchivo),
                archivoLocal
            )

            val archivoSubido = driveService.files()
                .create(metadata, contenido)
                .setFields("id")
                .execute()

            val id = archivoSubido.id
                ?: return@withContext ResultadoDrive.Error("Drive no devolvió ID para el archivo $nombreArchivo.")

            ResultadoDrive.Exito(id)
        } catch (e: Exception) {
            ResultadoDrive.Error(
                mensaje = "No se pudo subir el archivo $nombreArchivo a Drive.",
                excepcion = e
            )
        }
    }

    private fun buscarCarpeta(
        nombreCarpeta: String,
        parentId: String?
    ): String? {
        val parentQuery = if (parentId == null) {
            "'root' in parents"
        } else {
            "'${escaparQuery(parentId)}' in parents"
        }

        val query = """
            mimeType = '$MIME_TYPE_FOLDER'
            and name = '${escaparQuery(nombreCarpeta)}'
            and trashed = false
            and $parentQuery
        """.trimIndent().replace("\n", " ")

        val resultado = driveService.files()
            .list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()

        return resultado.files?.firstOrNull()?.id
    }

    private fun buscarArchivoExistente(
        carpetaDriveId: String,
        nombreArchivo: String,
        hashArchivo: String
    ): String? {
        val query = """
            trashed = false
            and '${escaparQuery(carpetaDriveId)}' in parents
            and (
                name = '${escaparQuery(nombreArchivo)}'
                or appProperties has { key = 'hashArchivo' and value = '${escaparQuery(hashArchivo)}' }
            )
        """.trimIndent().replace("\n", " ")

        val resultado = driveService.files()
            .list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name, appProperties)")
            .execute()

        return resultado.files?.firstOrNull()?.id
    }

    private fun obtenerMimeType(nombreArchivo: String): String {
        return when {
            nombreArchivo.endsWith(".json", ignoreCase = true) -> "application/json"
            nombreArchivo.endsWith(".xlsx", ignoreCase = true) ->
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            else -> "application/octet-stream"
        }
    }

    private fun escaparQuery(valor: String): String {
        return valor.replace("'", "\\'")
    }

    private companion object {
        const val MIME_TYPE_FOLDER = "application/vnd.google-apps.folder"
    }
}