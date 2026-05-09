package com.ventaking.app.datos.respaldo

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File

class GeneradorZipProtegido {

    fun crearZipProtegido(
        archivoDestino: File,
        archivos: List<File>,
        password: String
    ): File {
        if (archivoDestino.exists()) {
            archivoDestino.delete()
        }

        archivoDestino.parentFile?.mkdirs()

        val zipFile = ZipFile(
            archivoDestino,
            password.toCharArray()
        )

        archivos.forEach { archivo ->
            if (!archivo.exists()) {
                throw IllegalStateException("No existe el archivo para comprimir: ${archivo.name}")
            }

            val parametros = ZipParameters().apply {
                isEncryptFiles = true
                encryptionMethod = EncryptionMethod.AES
                aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256
                compressionMethod = CompressionMethod.DEFLATE
                compressionLevel = CompressionLevel.NORMAL
                fileNameInZip = archivo.name
            }

            zipFile.addFile(archivo, parametros)
        }

        return archivoDestino
    }
}