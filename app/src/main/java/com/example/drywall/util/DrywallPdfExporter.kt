package com.example.drywall.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.drywall.data.local.entity.ProjectEntity
import com.example.drywall.data.local.entity.WallCalculationEntity
import com.example.drywall.data.local.entity.WallWithOpenings
import com.example.drywall.data.mapper.ProjectBudgetView
import com.example.drywall.domain.model.WallCalculationResult
import com.example.drywall.domain.model.WallCost
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generador de reportes PDF con [PdfDocument], sin librerías externas.
 *
 * El PDF es adicional al reporte de texto de WhatsApp: incluye tabla de materiales con precio
 * unitario y subtotal, vanos, pintura, mano de obra y el total del presupuesto. Si el contenido
 * no cabe en una hoja, continúa en otra página.
 */
object DrywallPdfExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val LINE = 14f

    private const val COL_QTY = 330f
    private const val COL_UNIT = 400f
    private const val COL_TOTAL = 555f

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    /**
     * Genera el PDF de un muro suelto (pantalla de resultados).
     */
    fun generateWallPdf(
        context: Context,
        result: WallCalculationResult,
        cost: WallCost?,
        projectName: String? = null,
        clientOrLocation: String? = null
    ): File {
        val document = PdfDocument()
        val writer = PageWriter(document, "Reporte de materiales — DrywallCalc")
        writer.start()

        writer.header(
            title = projectName?.ifBlank { null } ?: "Cómputo de muro",
            subtitle = clientOrLocation?.ifBlank { null }
        )

        writer.section("Especificaciones del muro")
        writer.kv("Dimensiones", "${NumberFormatUtils.formatMeters(result.input.lengthMeters)} × ${NumberFormatUtils.formatMeters(result.input.heightMeters)}")
        writer.kv("Instalación", result.input.faces.label)
        writer.kv("Placa", result.input.boardType.displayName)
        writer.kv("Modulación de parantes", "${(result.input.studSpacingMeters * 100).toInt()} cm")
        writer.kv("Aislamiento", if (result.input.hasInsulation) "Sí" else "No")
        writer.kv("Desperdicio", "${(result.input.wasteFactor * 100).toInt()}%")

        writer.section("Superficies")
        writer.kv("Área bruta (una cara)", NumberFormatUtils.formatSquareMeters(result.input.wallAreaOneFace))
        if (result.openingsCount > 0) {
            writer.kv("Vanos", "${result.openingsCount} (descuentan ${NumberFormatUtils.formatSquareMeters(result.openingsAreaM2)} por cara)")
        }
        writer.kv("Área neta emplazada", NumberFormatUtils.formatSquareMeters(result.netTotalDrywallArea))

        if (result.input.openings.isNotEmpty()) {
            writer.section("Vanos")
            result.input.openings.forEachIndexed { index, opening ->
                val name = opening.label.ifBlank { "${opening.type.label} #${index + 1}" }
                writer.row(
                    "${name} — ${NumberFormatUtils.formatDecimal(opening.widthMeters)} × ${NumberFormatUtils.formatDecimal(opening.heightMeters)} m",
                    "",
                    "",
                    NumberFormatUtils.formatSquareMeters(opening.areaM2)
                )
            }
        }

        writer.section("Materiales")
        writer.tableHeader()
        result.requirements.forEach { requirement ->
            val line = cost?.lines?.firstOrNull { it.itemKey == requirement.material.id }
            writer.row(
                "${requirement.material.code}  ${requirement.material.name}",
                requirement.commercialQuantity.toString(),
                line?.takeIf { it.isPriced }?.let { NumberFormatUtils.formatCurrencyCop(it.unitPrice) } ?: "s/c",
                line?.takeIf { it.isPriced }?.let { NumberFormatUtils.formatCurrencyCop(it.subtotal) } ?: "—"
            )
        }

        cost?.let { wallCost ->
            writer.subtotalRow("Subtotal materiales", NumberFormatUtils.formatCurrencyCop(wallCost.materialsSubtotal))
            if (!wallCost.complete) {
                writer.note("Presupuesto incompleto: faltan ${wallCost.missingPrices} insumo(s) por cotizar.")
            }
        }

        result.paint?.let { paint ->
            writer.section("Pintura")
            writer.kv("Producto", paint.productName.ifBlank { "Sin especificar" })
            writer.kv("Área a pintar", NumberFormatUtils.formatSquareMeters(paint.areaM2))
            writer.kv("Sellador (${paint.sealerCoats} mano)", "${paint.sealerUnits} ${paint.unitLabelForTotal}")
            writer.kv("Acabado (${paint.finishCoats} manos)", "${paint.finishUnits} ${paint.unitLabelForTotal}")
        }

        writer.finish()
        return writeToCache(context, document, "muro")
    }

    /**
     * Genera el PDF de una obra completa.
     */
    fun generateProjectPdf(
        context: Context,
        project: ProjectEntity,
        walls: List<WallWithOpenings>,
        budgetView: ProjectBudgetView?
    ): File {
        val document = PdfDocument()
        val writer = PageWriter(document, "Presupuesto de obra — DrywallCalc")
        writer.start()

        writer.header(
            title = project.name,
            subtitle = project.clientOrLocation.ifBlank { null }
        )

        budgetView?.let { view ->
            writer.section("Resumen de la obra")
            writer.kv("Muros", walls.size.toString())
            writer.kv("Área neta total", NumberFormatUtils.formatSquareMeters(walls.sumOf { it.effectiveNetAreaM2 }))
        }

        walls.forEachIndexed { index, item ->
            val wall = item.wall
            val result = budgetView?.wallResults?.get(wall.id)
            val cost = budgetView?.wallCosts?.get(wall.id)

            writer.section("Muro ${index + 1}: ${wall.wallName}")
            writer.kv("Dimensiones", "${NumberFormatUtils.formatDecimal(wall.lengthMeters)} × ${NumberFormatUtils.formatDecimal(wall.heightMeters)} m")
            writer.kv("Caras", if (wall.faces == "BOTH_FACES") "2 Caras" else "1 Cara")
            writer.kv("Área neta", NumberFormatUtils.formatSquareMeters(item.effectiveNetAreaM2))
            if (item.openings.isNotEmpty()) {
                writer.kv("Vanos", item.openings.size.toString())
            }

            if (result != null && cost != null) {
                writer.tableHeader()
                result.requirements.forEach { requirement ->
                    val line = cost.lines.firstOrNull { it.itemKey == requirement.material.id }
                    writer.row(
                        "${requirement.material.code}  ${requirement.material.name}",
                        requirement.commercialQuantity.toString(),
                        line?.takeIf { it.isPriced }?.let { NumberFormatUtils.formatCurrencyCop(it.unitPrice) } ?: "s/c",
                        line?.takeIf { it.isPriced }?.let { NumberFormatUtils.formatCurrencyCop(it.subtotal) } ?: "—"
                    )
                }
                writer.subtotalRow("Subtotal muro", NumberFormatUtils.formatCurrencyCop(cost.materialsSubtotal))
                if (!cost.complete) {
                    writer.note("Faltan ${cost.missingPrices} precio(s) en este muro.")
                }
            }
        }

        budgetView?.let { view ->
            writer.section("Presupuesto")
            writer.kv("Materiales", NumberFormatUtils.formatCurrencyCop(view.budget.materialsSubtotal))
            writer.kv(
                "Mano de obra",
                if (view.labor.isConfigured) {
                    "${NumberFormatUtils.formatCurrencyCop(view.budget.labor)} (${view.labor.mode.label})"
                } else {
                    "No configurada"
                }
            )
            if (view.budget.complete) {
                writer.totalRow("TOTAL", NumberFormatUtils.formatCurrencyCop(view.budget.total))
            } else {
                writer.note(
                    "Presupuesto INCOMPLETO: faltan ${view.budget.missingPrices} precio(s) por cotizar. " +
                        "El parcial de materiales es ${NumberFormatUtils.formatCurrencyCop(view.budget.materialsSubtotal)}."
                )
            }
        }

        writer.finish()
        return writeToCache(context, document, "obra")
    }

    /** Comparte un PDF ya generado. */
    fun sharePdf(context: Context, file: File, subject: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, "Reporte generado por DrywallCalc")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, "Compartir reporte PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun writeToCache(context: Context, document: PdfDocument, prefix: String): File {
        val directory = File(context.cacheDir, "reports").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(directory, "${prefix}_$stamp.pdf")
        FileOutputStream(file).use { output ->
            document.writeTo(output)
        }
        document.close()
        return file
    }


    /**
     * Escritor paginado: mantiene el cursor vertical, abre páginas nuevas y numera.
     */
    private class PageWriter(
        private val document: PdfDocument,
        private val documentTitle: String
    ) {
        private val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 9f
            isAntiAlias = true
        }
        private val boldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        private val mutedPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 8f
            isAntiAlias = true
        }
        private val rulePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 0.6f
        }

        private var page: PdfDocument.Page? = null
        private var canvas: Canvas? = null
        private var y = MARGIN
        private var pageNumber = 0

        fun start() = newPage()

        private fun newPage() {
            page?.let { document.finishPage(it) }
            pageNumber += 1
            val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            page = document.startPage(info)
            canvas = page!!.canvas
            y = MARGIN
        }

        private fun ensureSpace(needed: Float = LINE) {
            if (y + needed > PAGE_HEIGHT - MARGIN - 16f) newPage()
        }

        private fun draw(text: String, x: Float, paint: Paint, yPos: Float) {
            canvas?.drawText(text, x, yPos, paint)
        }

        fun header(title: String, subtitle: String?) {
            draw(documentTitle.uppercase(), MARGIN, mutedPaint, y)
            y += 12f
            draw(ellipsize(title, boldPaint, PAGE_WIDTH - 2 * MARGIN), MARGIN, boldPaint.apply { textSize = 15f }, y + 6f)
            y += 22f
            boldPaint.textSize = 9f
            if (subtitle != null) {
                draw(ellipsize(subtitle, textPaint, PAGE_WIDTH - 2 * MARGIN), MARGIN, textPaint, y)
                y += LINE
            }
            draw("Emitido: ${dateFormat.format(Date())}", MARGIN, mutedPaint, y)
            y += LINE + 2f
            canvas?.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, rulePaint)
            y += 12f
        }

        fun section(title: String) {
            ensureSpace(LINE * 3)
            y += 4f
            draw(title.uppercase(), MARGIN, boldPaint, y)
            y += 4f
            canvas?.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, rulePaint)
            y += LINE
        }

        fun kv(label: String, value: String) {
            ensureSpace()
            draw(label, MARGIN, mutedPaint, y)
            draw(ellipsize(value, textPaint, 260f), 200f, textPaint, y)
            y += LINE
        }

        fun tableHeader() {
            ensureSpace()
            draw("Insumo", MARGIN, mutedPaint, y)
            draw("Cant.", COL_QTY, mutedPaint, y)
            draw("Unitario", COL_UNIT, mutedPaint, y)
            draw("Subtotal", COL_TOTAL - 40f, mutedPaint, y)
            y += 4f
            canvas?.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, rulePaint)
            y += 10f
        }

        fun row(name: String, quantity: String, unitPrice: String, subtotal: String) {
            ensureSpace()
            val maxWidth = COL_QTY - MARGIN - 6f
            draw(ellipsize(name, textPaint, maxWidth), MARGIN, textPaint, y)
            draw(quantity, COL_QTY, textPaint, y)
            draw(unitPrice, COL_UNIT, textPaint, y)
            draw(subtotal, COL_TOTAL - 40f, textPaint, y)
            y += LINE
        }

        fun subtotalRow(label: String, value: String) {
            ensureSpace()
            y += 2f
            canvas?.drawLine(COL_UNIT - 30f, y, PAGE_WIDTH - MARGIN, y, rulePaint)
            y += 11f
            draw(label, COL_UNIT - 30f, boldPaint, y)
            draw(value, COL_TOTAL - 40f, boldPaint, y)
            y += LINE
        }

        fun totalRow(label: String, value: String) {
            ensureSpace(LINE * 2)
            y += 6f
            val big = Paint(boldPaint).apply { textSize = 11f }
            draw(label, MARGIN, big, y)
            draw(value, COL_TOTAL - 60f, big, y)
            y += LINE + 4f
        }

        fun note(text: String) {
            val warning = Paint(textPaint).apply { color = Color.rgb(154, 52, 18) }
            val lines = wrap(text, warning, PAGE_WIDTH - 2 * MARGIN)
            ensureSpace(LINE * lines.size + 4f)
            lines.forEach { line ->
                draw(line, MARGIN, warning, y)
                y += LINE
            }
        }

        fun finish() {
            page?.let { current ->
                val footer = "Página $pageNumber"
                current.canvas.drawText(footer, PAGE_WIDTH - MARGIN - 40f, PAGE_HEIGHT - 24f, mutedPaint)
                document.finishPage(current)
            }
            page = null
            canvas = null
        }

        private fun ellipsize(text: String, paint: Paint, maxWidth: Float): String {
            if (paint.measureText(text) <= maxWidth) return text
            var end = text.length
            while (end > 3 && paint.measureText(text.substring(0, end) + "…") > maxWidth) end--
            return text.substring(0, end) + "…"
        }

        private fun wrap(text: String, paint: Paint, maxWidth: Float): List<String> {
            val words = text.split(' ')
            val lines = mutableListOf<String>()
            var current = StringBuilder()
            words.forEach { word ->
                val candidate = if (current.isEmpty()) word else "$current $word"
                if (paint.measureText(candidate) > maxWidth) {
                    if (current.isNotEmpty()) lines += current.toString()
                    current = StringBuilder(word)
                } else {
                    current = StringBuilder(candidate)
                }
            }
            if (current.isNotEmpty()) lines += current.toString()
            return if (lines.isEmpty()) listOf("") else lines
        }
    }
}
