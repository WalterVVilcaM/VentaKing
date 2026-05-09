package com.ventaking.app.datos.excel

import com.ventaking.app.datos.local.entidades.CorteDiarioEntity
import com.ventaking.app.datos.local.entidades.DispositivoEntity
import com.ventaking.app.datos.local.entidades.NegocioEntity
import com.ventaking.app.datos.local.entidades.VentaEntity
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class GeneradorExcel {

    private companion object {
        const val STYLE_NORMAL = 0
        const val STYLE_TITULO = 1
        const val STYLE_SUBTITULO = 2
        const val STYLE_SECCION = 3
        const val STYLE_ENCABEZADO = 4
        const val STYLE_TEXTO = 5
        const val STYLE_ETIQUETA = 6
        const val STYLE_ENTERO = 7
        const val STYLE_DINERO = 8
        const val STYLE_DINERO_VENTA = 9
        const val STYLE_DINERO_COSTO = 10
        const val STYLE_DINERO_GANANCIA = 11
        const val STYLE_DINERO_EDITABLE = 12
        const val STYLE_PORCENTAJE = 13
        const val STYLE_INSTRUCCION = 14
        const val STYLE_RESUMEN_VALOR = 15
        const val STYLE_RESUMEN_GANANCIA = 16
        const val STYLE_RESUMEN_COSTO = 17
        const val STYLE_BARRA_GANANCIA = 18
        const val STYLE_TECNICO = 19
        const val STYLE_ALERTA = 20
        const val STYLE_OK = 21
    }

    fun construirCorteExcel(
        corte: CorteDiarioEntity,
        ventas: List<VentaEntity>,
        negocio: NegocioEntity,
        dispositivo: DispositivoEntity,
        nombreArchivo: String,
        hashArchivo: String?
    ): ByteArray {
        val resumenPorProducto = ventas
            .groupBy { it.nombreProductoSnapshot.trim().ifBlank { "Producto sin nombre" } }
            .toSortedMap()
            .map { (producto, ventasProducto) ->
                ResumenProductoExcel(
                    producto = producto,
                    cantidad = ventasProducto.sumOf { it.cantidad },
                    totalCentavos = ventasProducto.sumOf { it.totalCentavos }
                )
            }

        val totalVendidoCentavos = ventas.sumOf { it.totalCentavos }
        val totalPiezas = ventas.sumOf { it.cantidad }
        val totalTransacciones = corte.totalVentas

        val masVendidoCantidad = resumenPorProducto.maxByOrNull { it.cantidad }
        val masVendidoMonto = resumenPorProducto.maxByOrNull { it.totalCentavos }

        val filas = mutableListOf<List<CeldaExcel>>()

        fun agregar(vararg celdas: CeldaExcel) {
            filas.add(celdas.toList())
        }

        fun espacio() {
            filas.add(emptyList())
        }

        agregar(CeldaExcel.texto("VentaKing", STYLE_TITULO))
        agregar(CeldaExcel.texto("Reporte de corte diario", STYLE_SUBTITULO))

        agregar(
            CeldaExcel.texto("Negocio", STYLE_ETIQUETA),
            CeldaExcel.texto(negocio.nombre, STYLE_TEXTO),
            CeldaExcel.texto("Fecha", STYLE_ETIQUETA),
            CeldaExcel.texto(corte.fechaCorte, STYLE_TEXTO),
            CeldaExcel.texto("Dispositivo", STYLE_ETIQUETA),
            CeldaExcel.texto(dispositivo.nombreDispositivo ?: dispositivo.id.take(8), STYLE_TEXTO)
        )

        espacio()

        agregar(CeldaExcel.texto("Resumen del corte", STYLE_SECCION))
        agregar(
            CeldaExcel.texto("Indicador", STYLE_ENCABEZADO),
            CeldaExcel.texto("Valor", STYLE_ENCABEZADO),
            CeldaExcel.texto("Detalle", STYLE_ENCABEZADO)
        )

        val filaTotalVendido = filas.size + 1
        agregar(
            CeldaExcel.texto("Total vendido", STYLE_ETIQUETA),
            CeldaExcel.dinero(totalVendidoCentavos, STYLE_RESUMEN_VALOR),
            CeldaExcel.texto("$totalTransacciones transacciones · $totalPiezas piezas", STYLE_TEXTO)
        )

        val filaCostoCapturado = filas.size + 1
        agregar(
            CeldaExcel.texto("Costo capturado", STYLE_ETIQUETA),
            CeldaExcel.formula("0", STYLE_RESUMEN_COSTO),
            CeldaExcel.texto("Suma de costos unitarios capturados por producto", STYLE_TEXTO)
        )

        val filaGanancia = filas.size + 1
        agregar(
            CeldaExcel.texto("Ganancia del día", STYLE_ETIQUETA),
            CeldaExcel.formula("0", STYLE_RESUMEN_GANANCIA),
            CeldaExcel.texto("Se calcula cuando captures costos unitarios", STYLE_TEXTO)
        )

        val filaMargen = filas.size + 1
        agregar(
            CeldaExcel.texto("Margen del día", STYLE_ETIQUETA),
            CeldaExcel.formula("IF(B$filaTotalVendido=0,0,B$filaGanancia/B$filaTotalVendido)", STYLE_PORCENTAJE),
            CeldaExcel.texto("Ganancia / total vendido", STYLE_TEXTO)
        )

        agregar(
            CeldaExcel.texto("Más vendido por piezas", STYLE_ETIQUETA),
            CeldaExcel.texto(masVendidoCantidad?.producto ?: "Sin ventas", STYLE_TEXTO),
            CeldaExcel.texto(
                masVendidoCantidad?.let { "${it.cantidad} piezas" } ?: "Sin datos",
                STYLE_TEXTO
            )
        )

        agregar(
            CeldaExcel.texto("Más vendido por monto", STYLE_ETIQUETA),
            CeldaExcel.texto(masVendidoMonto?.producto ?: "Sin ventas", STYLE_TEXTO),
            CeldaExcel.texto(
                masVendidoMonto?.let { formatearDinero(it.totalCentavos) } ?: "Sin datos",
                STYLE_TEXTO
            )
        )

        espacio()

        agregar(CeldaExcel.texto("Costos y ganancia por producto", STYLE_SECCION))
        agregar(
            CeldaExcel.texto(
                "Escribe el costo unitario de producción o compra en la columna amarilla. La ganancia usa el total real vendido, incluyendo extras y descuentos.",
                STYLE_INSTRUCCION
            )
        )

        agregar(
            CeldaExcel.texto("Producto", STYLE_ENCABEZADO),
            CeldaExcel.texto("Cantidad", STYLE_ENCABEZADO),
            CeldaExcel.texto("Precio prom.", STYLE_ENCABEZADO),
            CeldaExcel.texto("Total vendido", STYLE_ENCABEZADO),
            CeldaExcel.texto("Costo unitario", STYLE_ENCABEZADO),
            CeldaExcel.texto("Costo total", STYLE_ENCABEZADO),
            CeldaExcel.texto("Ganancia", STYLE_ENCABEZADO),
            CeldaExcel.texto("Margen", STYLE_ENCABEZADO),
            CeldaExcel.texto("Indicador", STYLE_ENCABEZADO)
        )

        val filaInicioProductos = filas.size + 1

        if (resumenPorProducto.isEmpty()) {
            agregar(
                CeldaExcel.texto("Sin productos vendidos", STYLE_TEXTO),
                CeldaExcel.numero(0.0, STYLE_ENTERO),
                CeldaExcel.numero(0.0, STYLE_DINERO),
                CeldaExcel.numero(0.0, STYLE_DINERO_VENTA),
                CeldaExcel.numero(0.0, STYLE_DINERO_EDITABLE),
                CeldaExcel.numero(0.0, STYLE_DINERO_COSTO),
                CeldaExcel.numero(0.0, STYLE_DINERO_GANANCIA),
                CeldaExcel.numero(0.0, STYLE_PORCENTAJE),
                CeldaExcel.texto("", STYLE_BARRA_GANANCIA)
            )
        } else {
            resumenPorProducto.forEach { resumen ->
                val fila = filas.size + 1
                val totalDecimal = resumen.totalCentavos / 100.0

                agregar(
                    CeldaExcel.texto(resumen.producto, STYLE_TEXTO),
                    CeldaExcel.numero(resumen.cantidad.toDouble(), STYLE_ENTERO),
                    CeldaExcel.formula("IF(B$fila=0,0,D$fila/B$fila)", STYLE_DINERO),
                    CeldaExcel.numero(totalDecimal, STYLE_DINERO_VENTA),
                    CeldaExcel.numero(0.0, STYLE_DINERO_EDITABLE),
                    CeldaExcel.formula("IF(E$fila=0,0,B$fila*E$fila)", STYLE_DINERO_COSTO),
                    CeldaExcel.formula("IF(E$fila=0,0,D$fila-F$fila)", STYLE_DINERO_GANANCIA),
                    CeldaExcel.formula("IF(E$fila=0,0,IF(D$fila=0,0,G$fila/D$fila))", STYLE_PORCENTAJE),
                    CeldaExcel.texto("", STYLE_BARRA_GANANCIA)
                )
            }
        }

        val filaFinProductos = filas.size

        actualizarFormulas(
            filas = filas,
            filaCostoCapturado = filaCostoCapturado,
            filaGanancia = filaGanancia,
            filaInicioProductos = filaInicioProductos,
            filaFinProductos = filaFinProductos
        )

        espacio()

        agregar(CeldaExcel.texto("Ranking de rentabilidad", STYLE_SECCION))
        agregar(
            CeldaExcel.texto("Producto", STYLE_ENCABEZADO),
            CeldaExcel.texto("Vendido", STYLE_ENCABEZADO),
            CeldaExcel.texto("Costo", STYLE_ENCABEZADO),
            CeldaExcel.texto("Ganancia", STYLE_ENCABEZADO),
            CeldaExcel.texto("Margen", STYLE_ENCABEZADO),
            CeldaExcel.texto("Lectura", STYLE_ENCABEZADO)
        )

        if (resumenPorProducto.isEmpty()) {
            agregar(
                CeldaExcel.texto("Sin datos", STYLE_TEXTO),
                CeldaExcel.numero(0.0, STYLE_DINERO),
                CeldaExcel.numero(0.0, STYLE_DINERO_COSTO),
                CeldaExcel.numero(0.0, STYLE_DINERO_GANANCIA),
                CeldaExcel.numero(0.0, STYLE_PORCENTAJE),
                CeldaExcel.texto("", STYLE_TEXTO)
            )
        } else {
            resumenPorProducto.forEachIndexed { index, resumen ->
                val filaProducto = filaInicioProductos + index

                agregar(
                    CeldaExcel.texto(resumen.producto, STYLE_TEXTO),
                    CeldaExcel.formula("D$filaProducto", STYLE_DINERO_VENTA),
                    CeldaExcel.formula("F$filaProducto", STYLE_DINERO_COSTO),
                    CeldaExcel.formula("G$filaProducto", STYLE_DINERO_GANANCIA),
                    CeldaExcel.formula("H$filaProducto", STYLE_PORCENTAJE),
                    CeldaExcel.formula(
                        "IF(E$filaProducto=0,\"Captura costo\",IF(G$filaProducto<0,\"Revisar costo\",IF(H$filaProducto>=0.3,\"Buen margen\",IF(H$filaProducto>=0.15,\"Margen medio\",\"Margen bajo\"))))",
                        STYLE_TEXTO
                    )
                )
            }
        }

        espacio()

        agregar(CeldaExcel.texto("Detalle de ventas", STYLE_SECCION))
        agregar(
            CeldaExcel.texto("Hora", STYLE_ENCABEZADO),
            CeldaExcel.texto("Producto", STYLE_ENCABEZADO),
            CeldaExcel.texto("Cantidad", STYLE_ENCABEZADO),
            CeldaExcel.texto("Precio unitario", STYLE_ENCABEZADO),
            CeldaExcel.texto("Subtotal", STYLE_ENCABEZADO),
            CeldaExcel.texto("Extra", STYLE_ENCABEZADO),
            CeldaExcel.texto("Descuento", STYLE_ENCABEZADO),
            CeldaExcel.texto("Total", STYLE_ENCABEZADO),
            CeldaExcel.texto("Estado", STYLE_ENCABEZADO),
            CeldaExcel.texto("ID venta", STYLE_ENCABEZADO)
        )

        ventas.forEach { venta ->
            agregar(
                CeldaExcel.texto(venta.horaVenta, STYLE_TEXTO),
                CeldaExcel.texto(venta.nombreProductoSnapshot, STYLE_TEXTO),
                CeldaExcel.numero(venta.cantidad.toDouble(), STYLE_ENTERO),
                CeldaExcel.dinero(venta.precioUnitarioSnapshotCentavos, STYLE_DINERO),
                CeldaExcel.dinero(venta.subtotalCentavos, STYLE_DINERO),
                CeldaExcel.dinero(venta.extraCentavos, STYLE_DINERO),
                CeldaExcel.dinero(venta.descuentoCentavos, STYLE_DINERO),
                CeldaExcel.dinero(venta.totalCentavos, STYLE_DINERO_VENTA),
                CeldaExcel.texto(venta.estado, STYLE_TEXTO),
                CeldaExcel.texto(venta.id, STYLE_TECNICO)
            )
        }

        espacio()

        agregar(CeldaExcel.texto("Información técnica", STYLE_SECCION))
        agregar(CeldaExcel.texto("Archivo", STYLE_ETIQUETA), CeldaExcel.texto(nombreArchivo, STYLE_TECNICO))
        agregar(CeldaExcel.texto("Hash", STYLE_ETIQUETA), CeldaExcel.texto(hashArchivo ?: "PENDIENTE", STYLE_TECNICO))
        agregar(CeldaExcel.texto("ID dispositivo", STYLE_ETIQUETA), CeldaExcel.texto(dispositivo.id, STYLE_TECNICO))
        agregar(CeldaExcel.texto("ID corte", STYLE_ETIQUETA), CeldaExcel.texto(corte.id, STYLE_TECNICO))
        agregar(CeldaExcel.texto("Estado corte", STYLE_ETIQUETA), CeldaExcel.texto(corte.estado, STYLE_TECNICO))
        agregar(CeldaExcel.texto("Estado sync", STYLE_ETIQUETA), CeldaExcel.texto(corte.syncEstado, STYLE_TECNICO))

        return construirXlsx(construirSheetXml(filas))
    }

    private fun actualizarFormulas(
        filas: MutableList<List<CeldaExcel>>,
        filaCostoCapturado: Int,
        filaGanancia: Int,
        filaInicioProductos: Int,
        filaFinProductos: Int
    ) {
        if (filaInicioProductos > filaFinProductos) return

        val filaCostoIndex = filaCostoCapturado - 1
        val filaCosto = filas[filaCostoIndex].toMutableList()
        filaCosto[1] = CeldaExcel.formula(
            "SUM(F$filaInicioProductos:F$filaFinProductos)",
            STYLE_RESUMEN_COSTO
        )
        filas[filaCostoIndex] = filaCosto

        val filaGananciaIndex = filaGanancia - 1
        val filaGananciaLista = filas[filaGananciaIndex].toMutableList()
        filaGananciaLista[1] = CeldaExcel.formula(
            "IF(SUM(F$filaInicioProductos:F$filaFinProductos)=0,0,SUM(G$filaInicioProductos:G$filaFinProductos))",
            STYLE_RESUMEN_GANANCIA
        )
        filas[filaGananciaIndex] = filaGananciaLista

        for (rowIndex in (filaInicioProductos - 1)..(filaFinProductos - 1)) {
            val filaActual = rowIndex + 1
            val fila = filas[rowIndex].toMutableList()

            if (fila.size >= 9) {
                fila[8] = CeldaExcel.formula(
                    "REPT(\"█\",ROUND(IF(MAX(G$filaInicioProductos:G$filaFinProductos)=0,0,MAX(0,G$filaActual)/MAX(G$filaInicioProductos:G$filaFinProductos)*14),0))",
                    STYLE_BARRA_GANANCIA
                )
                filas[rowIndex] = fila
            }
        }
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

    private fun construirSheetXml(filas: List<List<CeldaExcel>>): String {
        val filasXml = filas.mapIndexed { indexFila, fila ->
            val numeroFila = indexFila + 1

            val celdasXml = fila.mapIndexed { indexColumna, celda ->
                val referencia = "${columnaExcel(indexColumna)}$numeroFila"
                celda.toXml(referencia)
            }.joinToString("")

            val altura = when {
                numeroFila == 1 -> """ ht="30" customHeight="1""""
                numeroFila == 2 -> """ ht="20" customHeight="1""""
                fila.any { it.estilo == STYLE_SECCION } -> """ ht="24" customHeight="1""""
                fila.any { it.estilo == STYLE_INSTRUCCION } -> """ ht="42" customHeight="1""""
                else -> ""
            }

            """<row r="$numeroFila"$altura>$celdasXml</row>"""
        }.joinToString("")

        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<sheetViews>
<sheetView workbookViewId="0" showGridLines="0">
<pane ySplit="13" topLeftCell="A14" activePane="bottomLeft" state="frozen"/>
</sheetView>
</sheetViews>
<cols>
<col min="1" max="1" width="30" customWidth="1"/>
<col min="2" max="2" width="18" customWidth="1"/>
<col min="3" max="3" width="18" customWidth="1"/>
<col min="4" max="4" width="18" customWidth="1"/>
<col min="5" max="5" width="18" customWidth="1"/>
<col min="6" max="6" width="18" customWidth="1"/>
<col min="7" max="7" width="18" customWidth="1"/>
<col min="8" max="8" width="15" customWidth="1"/>
<col min="9" max="9" width="26" customWidth="1"/>
<col min="10" max="10" width="44" customWidth="1"/>
</cols>
<sheetData>
$filasXml
</sheetData>
<mergeCells count="7">
<mergeCell ref="C6:F6"/>
<mergeCell ref="C7:F7"/>
<mergeCell ref="C8:F8"/>
<mergeCell ref="C9:F9"/>
<mergeCell ref="C10:F10"/>
<mergeCell ref="C11:F11"/>
<mergeCell ref="C12:F12"/>
</mergeCells>
</worksheet>"""
    }

    private fun CeldaExcel.toXml(referencia: String): String {
        val atributoEstilo = """ s="$estilo""""

        return when (tipo) {
            TipoCeldaExcel.TEXTO -> {
                """<c r="$referencia"$atributoEstilo t="inlineStr"><is><t>${escaparXml(valorTexto)}</t></is></c>"""
            }

            TipoCeldaExcel.NUMERO -> {
                """<c r="$referencia"$atributoEstilo><v>${formatoNumero(valorNumero)}</v></c>"""
            }

            TipoCeldaExcel.FORMULA -> {
                """<c r="$referencia"$atributoEstilo><f>${escaparFormula(formula)}</f><v>0</v></c>"""
            }
        }
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

    private fun formatoNumero(valor: Double): String {
        return String.format(Locale.US, "%.2f", valor)
    }

    private fun formatearDinero(centavos: Long): String {
        return "$" + String.format(Locale.US, "%,.2f", centavos / 100.0)
    }

    private fun escaparXml(valor: String): String {
        return valor
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun escaparFormula(valor: String): String {
        return valor
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
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
<sheet name="Reporte" sheetId="1" r:id="rId1"/>
</sheets>
<calcPr calcMode="auto" fullCalcOnLoad="1" forceFullCalc="1"/>
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
<numFmts count="2">
<numFmt numFmtId="164" formatCode="&quot;${'$'}&quot;#,##0.00"/>
<numFmt numFmtId="165" formatCode="0.00%"/>
</numFmts>

<fonts count="8">
<font><sz val="11"/><name val="Calibri"/></font>
<font><b/><sz val="20"/><color rgb="FF111827"/><name val="Calibri"/></font>
<font><sz val="11"/><color rgb="FF6B7280"/><name val="Calibri"/></font>
<font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
<font><b/><sz val="11"/><color rgb="FF111827"/><name val="Calibri"/></font>
<font><b/><sz val="12"/><color rgb="FFD63384"/><name val="Calibri"/></font>
<font><b/><sz val="12"/><color rgb="FF059669"/><name val="Calibri"/></font>
<font><i/><sz val="10"/><color rgb="FF6B7280"/><name val="Calibri"/></font>
</fonts>

<fills count="11">
<fill><patternFill patternType="none"/></fill>
<fill><patternFill patternType="gray125"/></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFF8FAFC"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFD63384"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FF7C3AED"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FF14B8A6"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFFFF2CC"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFD1FAE5"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFE0F2FE"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFFEE2E2"/><bgColor indexed="64"/></patternFill></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FFF3E8FF"/><bgColor indexed="64"/></patternFill></fill>
</fills>

<borders count="2">
<border/>
<border>
<left style="thin"><color rgb="FFE5E7EB"/></left>
<right style="thin"><color rgb="FFE5E7EB"/></right>
<top style="thin"><color rgb="FFE5E7EB"/></top>
<bottom style="thin"><color rgb="FFE5E7EB"/></bottom>
</border>
</borders>

<cellStyleXfs count="1">
<xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
</cellStyleXfs>

<cellXfs count="22">
<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>

<xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1">
<alignment horizontal="left" vertical="center"/>
</xf>

<xf numFmtId="0" fontId="2" fillId="0" borderId="0" xfId="0" applyFont="1">
<alignment horizontal="left" vertical="center"/>
</xf>

<xf numFmtId="0" fontId="4" fillId="10" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1">
<alignment horizontal="left" vertical="center"/>
</xf>

<xf numFmtId="0" fontId="3" fillId="3" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1">
<alignment horizontal="center" vertical="center" wrapText="1"/>
</xf>

<xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyBorder="1">
<alignment horizontal="left" vertical="center"/>
</xf>

<xf numFmtId="0" fontId="4" fillId="2" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1">
<alignment horizontal="left" vertical="center"/>
</xf>

<xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyBorder="1">
<alignment horizontal="center" vertical="center"/>
</xf>

<xf numFmtId="164" fontId="0" fillId="0" borderId="1" xfId="0" applyNumberFormat="1" applyBorder="1">
<alignment horizontal="right" vertical="center"/>
</xf>

<xf numFmtId="164" fontId="5" fillId="8" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyFill="1" applyBorder="1">
<alignment horizontal="right" vertical="center"/>
</xf>

<xf numFmtId="164" fontId="4" fillId="9" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyFill="1" applyBorder="1">
<alignment horizontal="right" vertical="center"/>
</xf>

<xf numFmtId="164" fontId="6" fillId="7" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyFill="1" applyBorder="1">
<alignment horizontal="right" vertical="center"/>
</xf>

<xf numFmtId="164" fontId="4" fillId="6" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyFill="1" applyBorder="1">
<alignment horizontal="right" vertical="center"/>
</xf>

<xf numFmtId="165" fontId="4" fillId="0" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyBorder="1">
<alignment horizontal="right" vertical="center"/>
</xf>

<xf numFmtId="0" fontId="7" fillId="6" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1">
<alignment horizontal="left" vertical="center" wrapText="1"/>
</xf>

<xf numFmtId="164" fontId="5" fillId="8" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyFill="1" applyBorder="1">
<alignment horizontal="right" vertical="center"/>
</xf>

<xf numFmtId="164" fontId="6" fillId="7" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyFill="1" applyBorder="1">
<alignment horizontal="right" vertical="center"/>
</xf>

<xf numFmtId="164" fontId="4" fillId="9" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyFill="1" applyBorder="1">
<alignment horizontal="right" vertical="center"/>
</xf>

<xf numFmtId="0" fontId="4" fillId="8" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1">
<alignment horizontal="left" vertical="center" wrapText="1"/>
</xf>

<xf numFmtId="0" fontId="7" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1">
<alignment horizontal="left" vertical="center"/>
</xf>

<xf numFmtId="0" fontId="4" fillId="9" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1">
<alignment horizontal="left" vertical="center"/>
</xf>

<xf numFmtId="0" fontId="6" fillId="7" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1">
<alignment horizontal="left" vertical="center"/>
</xf>
</cellXfs>

<cellStyles count="1">
<cellStyle name="Normal" xfId="0" builtinId="0"/>
</cellStyles>
</styleSheet>"""
    }

    private fun appXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/docPropsVTypes">
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

    private data class ResumenProductoExcel(
        val producto: String,
        val cantidad: Int,
        val totalCentavos: Long
    )

    private enum class TipoCeldaExcel {
        TEXTO,
        NUMERO,
        FORMULA
    }

    private data class CeldaExcel(
        val tipo: TipoCeldaExcel,
        val valorTexto: String = "",
        val valorNumero: Double = 0.0,
        val formula: String = "",
        val estilo: Int = STYLE_NORMAL
    ) {
        companion object {
            fun texto(valor: String, estilo: Int = STYLE_NORMAL): CeldaExcel {
                return CeldaExcel(
                    tipo = TipoCeldaExcel.TEXTO,
                    valorTexto = valor,
                    estilo = estilo
                )
            }

            fun numero(valor: Double, estilo: Int = STYLE_NORMAL): CeldaExcel {
                return CeldaExcel(
                    tipo = TipoCeldaExcel.NUMERO,
                    valorNumero = valor,
                    estilo = estilo
                )
            }

            fun dinero(centavos: Long, estilo: Int = STYLE_DINERO): CeldaExcel {
                return CeldaExcel(
                    tipo = TipoCeldaExcel.NUMERO,
                    valorNumero = centavos / 100.0,
                    estilo = estilo
                )
            }

            fun formula(valor: String, estilo: Int = STYLE_NORMAL): CeldaExcel {
                return CeldaExcel(
                    tipo = TipoCeldaExcel.FORMULA,
                    formula = valor,
                    estilo = estilo
                )
            }
        }
    }
}