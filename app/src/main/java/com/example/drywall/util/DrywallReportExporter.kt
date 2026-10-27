package com.example.drywall.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.drywall.domain.model.WallCalculationResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Utilidad para generar y compartir reportes técnicos de materiales de Drywall
 * formateados especialmente con sintaxis Markdown compatible con WhatsApp.
 *
 * Todo el formato numérico técnico usa [NumberFormatUtils.formatDecimal], por lo que
 * el reporte es idéntico sin importar el locale configurado en el dispositivo.
 */
object DrywallReportExporter {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    /**
     * Construye un reporte estructurado y estilizado para WhatsApp.
     * Utiliza negritas (*texto*), cursivas (_texto_) y monoespaciado (`codigo`).
     */
    fun generateWhatsAppFormattedReport(
        result: WallCalculationResult,
        projectName: String? = null,
        clientOrLocation: String? = null,
        notes: String? = null
    ): String {
        val input = result.input
        val sb = StringBuilder()

        sb.append("📋 *REPORTE DE MATERIALES - DRYWALLCALC*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━\n")

        projectName?.takeIf { it.isNotBlank() }?.let {
            sb.append("🏗️ *Obra / Proyecto:* $it\n")
        }

        clientOrLocation?.takeIf { it.isNotBlank() }?.let {
            sb.append("📍 *Cliente / Ubicación:* $it\n")
        }

        sb.append("📅 *Fecha de emisión:* ${dateFormat.format(Date(result.calculationTimestamp))}\n\n")

        sb.append("📐 *ESPECIFICACIONES TÉCNICAS DEL MURO:*\n")
        sb.append(
            "• *Dimensiones:* ${format(input.lengthMeters)} m largo × " +
                "${format(input.heightMeters)} m alto\n"
        )
        sb.append("• *Tipo de instalación:* ${input.faces.label}\n")
        sb.append("• *Tipo de placa:* ${input.boardType.displayName}\n")
        val spacingCm = (input.studSpacingMeters * 100).toInt()
        sb.append("• *Modulación de parantes:* Cada $spacingCm cm\n")
        sb.append("• *Aislamiento termoacústico:* ${if (input.hasInsulation) "Sí (Lana de vidrio/mineral)" else "No"}\n")
        sb.append("• *Vanos (puertas / ventanas):* ${if (input.openings.isEmpty()) "Sin vanos" else input.openings.size.toString()}\n")
        sb.append("• *Factor de desperdicio:* *${(input.wasteFactor * 100).roundToInt()}%*\n\n")

        if (input.openings.isNotEmpty()) {
            sb.append("🚪 *VANOS DEL MURO:*\n")
            input.openings.forEachIndexed { index, opening ->
                val name = opening.label.ifBlank { "${opening.type.label} #${index + 1}" }
                sb.append(
                    "• *$name:* ${format(opening.widthMeters)} m × ${format(opening.heightMeters)} m " +
                        "= ${format(opening.areaM2)} m²\n"
                )
            }
            sb.append("\n")
        }

        sb.append("📊 *RESUMEN DE MÉTRICAS:*\n")
        sb.append("• *Superficie total emplacada:* ${format(result.netTotalDrywallArea)} m²\n")
        if (input.openings.isNotEmpty()) {
            sb.append("• *Superficie descontada por vanos:* ${format(input.openingsAreaOneFace)} m² por cara\n")
        }
        sb.append("• *Perímetro del muro:* ${format(input.perimeterMeters)} m lineales\n")
        sb.append("• *Total placas estándar:* *${result.totalBoardsRequired} unidades*\n")
        sb.append("• *Total perfiles 3m (canales + postes):* *${result.totalProfiles3mRequired} piezas*\n\n")

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━\n")
        result.paint?.let { paint ->
            sb.append("🎨 *PINTURA DEL MURO:*\n")
            sb.append("• *Producto:* ${paint.productName.ifBlank { "Sin especificar" }}")
            sb.append(" (rendimiento ${format(paint.coverageM2PerUnit)} m²/${paint.unit.label.lowercase()})\n")
            sb.append("• *Área a pintar:* ${format(paint.areaM2)} m²\n")
            sb.append("• *Sellador (${paint.sealerCoats} mano):* ${paint.sealerUnits} ${paint.unitLabelForTotal}\n")
            sb.append("• *Acabado (${paint.finishCoats} manos):* ${paint.finishUnits} ${paint.unitLabelForTotal}\n")
            sb.append("• *Total envases:* *${paint.totalUnits} ${paint.unitLabelForTotal}*\n\n")
        }

        sb.append("🛒 *LISTA DE COMPRA DE MATERIALES:*\n")
        sb.append("_(Cantidades comerciales redondeadas con ${(input.wasteFactor * 100).roundToInt()}% de desperdicio)_\n\n")

        for ((category, items) in result.requirementsByCategory) {
            sb.append("🔹 *${category.displayName.uppercase()}*\n")
            for (req in items) {
                sb.append("• `${req.material.code}` *${req.material.name}*\n")
                sb.append("   👉 *${req.commercialQuantity} ${if (req.commercialQuantity == 1) req.unit.singularName else req.unit.pluralName}*\n")
                sb.append("   _(${req.formulaExplanation})_\n")
            }
            sb.append("\n")
        }

        notes?.takeIf { it.isNotBlank() }?.let {
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("📝 *NOTAS ADICIONALES:*\n$it\n\n")
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("Generado automáticamente por *DrywallCalc* 📱")

        return sb.toString()
    }

    /** Formatea una magnitud técnica con decimal fijo, independiente del locale de la máquina. */
    private fun format(value: Double): String = NumberFormatUtils.formatDecimal(value)

    /**
     * Lanza el Intent para compartir el reporte directamente por WhatsApp
     * o a través del menú de selección nativo de Android si WhatsApp no está disponible.
     */
    fun shareReport(
        context: Context,
        reportText: String,
        title: String = "Compartir Reporte de Materiales"
    ) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, reportText)
            type = "text/plain"
        }

        val shareChooser = Intent.createChooser(sendIntent, title)
        shareChooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            context.startActivity(shareChooser)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo iniciar el servicio de mensajería", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Copia el reporte formateado para WhatsApp al portapapeles y notifica al usuario.
     */
    fun copyToClipboard(context: Context, reportText: String, label: String = "Reporte Drywall") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, reportText)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Reporte formateado copiado al portapapeles", Toast.LENGTH_SHORT).show()
    }
}
