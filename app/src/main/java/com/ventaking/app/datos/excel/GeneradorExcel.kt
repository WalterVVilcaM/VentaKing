package com.ventaking.app.datos.excel

import com.ventaking.app.datos.local.entidades.CorteDiarioEntity
import com.ventaking.app.datos.local.entidades.DispositivoEntity
import com.ventaking.app.datos.local.entidades.NegocioEntity
import com.ventaking.app.datos.local.entidades.VentaEntity
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class GeneradorExcel {

    fun construirCorteExcel(
        corte: CorteDiarioEntity,
        ventas: List<VentaEntity>,
        negocio: NegocioEntity,
        dispositivo: DispositivoEntity,
        nombreArchivo: String,
        hashArchivo: String?
    ): ByteArray {
        val filas = mutableListOf<List<String>>()

        filas.add(listOf("VentaKing - Corte diario"))
        filas.add(listOf("Archivo", nombreArchivo))
        filas.add(listOf("Hash", hashArchivo ?: "PENDIENTE"))
        filas.add(listOf("Negocio", negocio.nombre))
        filas.add(listOf("Fecha de corte", corte.fechaCorte))
        filas.add(listOf("Dispositivo", dispositivo.nombreDispositivo ?: dispositivo.id))
        filas.add(listOf("ID del dispositivo", dispositivo.id))
        filas.add(listOf("ID del corte", corte.id))
        filas.add(listOf("Estado del corte", corte.estado))
        filas.add(listOf("Estado sync", corte.syncEstado))
        filas.add(emptyList())

        filas.add(listOf("Resumen"))
        filas.add(listOf("Total ventas", corte.totalVentas.toString()))
        filas.add(listOf("Total piezas", corte.totalPiezas.toString()))
        filas.add(listOf("Total", formatearCentavos(corte.totalCentavos)))
        filas.add(emptyList())

        filas.add(listOf("Detalle de ventas"))
        filas.add(
            listOf(
                "Hora",
                "Producto",
                "Cantidad",
                "Precio unitario",
                "Subtotal",
                "Extra",
                "Descuento",
                "Total",
                "Estado",
                "ID venta"
            )
        )

        ventas.forEach { venta ->
            filas.add(
                listOf(
                    venta.horaVenta,
                    venta.nombreProductoSnapshot,
                    venta.cantidad.toString(),
                    formatearCentavos(venta.precioUnitarioSnapshotCentavos),
                    formatearCentavos(venta.subtotalCentavos),
                    formatearCentavos(venta.extraCentavos),
                    formatearCentavos(venta.descuentoCentavos),
                    formatearCentavos(venta.totalCentavos),
                    venta.estado,
                    venta.id
                )
            )
        }

        filas.add(emptyList())
        filas.add(listOf("Resumen por producto"))
        filas.add(listOf("Producto", "Cantidad total", "Total"))

        ventas
            .groupBy { it.nombreProductoSnapshot }
            .toSortedMap()
            .forEach { (producto, ventasProducto) ->
                filas.add(
                    listOf(
                        producto,
                        ventasProducto.sumOf { it.cantidad }.toString(),
                        formatearCentavos(ventasProducto.sumOf { it.totalCentavos })
                    )
                )
            }

        val sheetXml = construirSheetXml(filas)
        return construirXlsx(sheetXml)
    }

    private fun construirXlsx(sheetXml: String): ByteArray {
        val salida = ByteArrayOutputStream()

        ZipOutputStream(salida).use { zip ->
            zip.agregarEntrada("[Content_Types].xml", contentTypesXml())
            zip.agregarEntrada("_rels/.rels", relsXml())
            zip.agregarEntrada("docProps/app.xml", appXml())
            zip.agregarEntrada("docProps/core.xml", coreXml())
            zip.agregarEntrada("xl/workbook.xml", workbookXml())
            zip.agregarEntrada("xl/_rels/workbook.xml.rels", workbookRelsXml())
            zip.agregarEntrada("xl/styles.xml", stylesXml())
            zip.agregarEntrada("xl/worksheets/sheet1.xml", sheetXml)
        }

        return salida.toByteArray()
    }

    private fun ZipOutputStream.agregarEntrada(nombre: String, contenido: String) {
        putNextEntry(ZipEntry(nombre))
        write(contenido.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun construirSheetXml(filas: List<List<String>>): String {
        val filasXml = filas.mapIndexed { indexFila, fila ->
            val numeroFila = indexFila + 1

            val celdasXml = fila.mapIndexed { indexColumna, valor ->
                val referencia = "${columnaExcel(indexColumna)}$numeroFila"
                """<c r="$referencia" t="inlineStr"><is><t>${escaparXml(valor)}</t></is></c>"""
            }.joinToString("")

            """<row r="$numeroFila">$celdasXml</row>"""
        }.joinToString("")

        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<cols>
<col min="1" max="1" width="18" customWidth="1"/>
<col min="2" max="2" width="30" customWidth="1"/>
<col min="3" max="3" width="16" customWidth="1"/>
<col min="4" max="4" width="18" customWidth="1"/>
<col min="5" max="5" width="18" customWidth="1"/>
<col min="6" max="6" width="18" customWidth="1"/>
<col min="7" max="7" width="18" customWidth="1"/>
<col min="8" max="8" width="18" customWidth="1"/>
<col min="9" max="9" width="16" customWidth="1"/>
<col min="10" max="10" width="42" customWidth="1"/>
</cols>
<sheetData>
$filasXml
</sheetData>
</worksheet>"""
    }

    private fun columnaExcel(index: Int): String {
        var numero = index
        val resultado = StringBuilder()

        do {
            val residuo = numero % 26
            resultado.insert(0, ('A'.code + residuo).toChar())
            numero = numero / 26 - 1
        } while (numero >= 0)

        return resultado.toString()
    }

    private fun formatearCentavos(centavos: Long): String {
        return NumberFormat.getCurrencyInstance(Locale("es", "MX"))
            .format(centavos / 100.0)
    }

    private fun escaparXml(valor: String): String {
        return valor
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun contentTypesXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
<Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>"""
    }

    private fun relsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
<Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>"""
    }

    private fun workbookXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets>
<sheet name="Corte" sheetId="1" r:id="rId1"/>
</sheets>
</workbook>"""
    }

    private fun workbookRelsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""
    }

    private fun stylesXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<fonts count="1">
<font>
<sz val="11"/>
<name val="Calibri"/>
</font>
</fonts>
<fills count="1">
<fill>
<patternFill patternType="none"/>
</fill>
</fills>
<borders count="1">
<border/>
</borders>
<cellStyleXfs count="1">
<xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
</cellStyleXfs>
<cellXfs count="1">
<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
</cellXfs>
</styleSheet>"""
    }

    private fun appXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
<Application>VentaKing</Application>
</Properties>"""
    }

    private fun coreXml(): String {
        val fecha = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<dc:creator>VentaKing</dc:creator>
<cp:lastModifiedBy>VentaKing</cp:lastModifiedBy>
<dcterms:created xsi:type="dcterms:W3CDTF">$fecha</dcterms:created>
<dcterms:modified xsi:type="dcterms:W3CDTF">$fecha</dcterms:modified>
</cp:coreProperties>"""
    }
}