package com.example.drywall.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.drywall.domain.model.WallCalculationResult
import com.example.drywall.domain.model.WallCost
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilidad para generar y compartir reportes técnicos de materiales de Drywall
 * formateados especialmente con sintaxis Markdown compacta compatible con WhatsApp.
 */
object DrywallReportExporter {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    /**
     * Construye un reporte ejecutivo, compacto y estilizado para WhatsApp,
     * optimizado para evitar cortes de texto en aplicaciones de mensajería.
     */
    fun generateWhatsAppFormattedReport(
        result: WallCalculationResult,
        projectName: String? = null,
        clientOrLocation: String? = null,
        notes: String? = null,
        cost: WallCost? = null
    ): String {
        val input = result.input
        val sb = StringBuilder()

        sb.append("📋 *PEDIDO DE MATERIALES - DRYWALLCALC*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━\n")

        projectName?.takeIf { it.isNotBlank() }?.let {
            sb.append("🏗️ *Obra / Proyecto:* $it\n")
        }

        clientOrLocation?.takeIf { it.isNotBlank() }?.let {
            sb.append("📍 *Cliente / Ubicación:* $it\n")
        }

        sb.append("📅 *Fecha:* ${dateFormat.format(Date(result.calculationTimestamp))}\n")
        sb.append("📐 *Muro:* ${format(input.lengthMeters)} m largo × ${format(input.heightMeters)} m alto (${input.faces.label})\n")
        sb.append("📏 *Superficie Neta:* *${format(result.netTotalDrywallArea)} m²*\n")

        val profileLenText = String.format(Locale.getDefault(), "%.2fm", input.commercialProfileLengthMeters)
        sb.append("⚙️ *Especificaciones:* Placa ${input.boardType.code} • Perfil ${profileLenText} • ${if (input.hasInsulation) "Con Aislamiento" else "Sin Aislamiento"}\n\n")

        if (input.openings.isNotEmpty()) {
            sb.append("🚪 *VANOS DE LA OBRA (${input.openings.size}):*\n")
            input.openings.forEachIndexed { index, opening ->
                val name = opening.label.ifBlank { "${opening.type.label} #${index + 1}" }
                sb.append("• *$name:* ${format(opening.widthMeters)} m × ${format(opening.heightMeters)} m (${format(opening.areaM2)} m²)\n")
            }
            sb.append("\n")
        }

        sb.append("🛒 *LISTA DE COMPRA DE MATERIALES:*\n")

        for ((category, items) in result.requirementsByCategory) {
            sb.append("\n🔹 *${category.displayName.uppercase()}*\n")
            for (req in items) {
                val qtyText = "${req.commercialQuantity} ${if (req.commercialQuantity == 1) req.unit.singularName else req.unit.pluralName}"
                sb.append("• *${req.material.name}*: *${qtyText}*\n")
            }
        }

        result.paint?.let { paint ->
            sb.append("\n🎨 *ACABADOS Y PINTURA:*\n")
            val prodName = paint.productName.ifBlank { "Pintura Estándar" }
            sb.append("• *$prodName (${format(paint.areaM2)} m²)*:\n")
            sb.append("   👉 Sellador: *${paint.sealerUnits} ${paint.unitLabelForTotal}*\n")
            sb.append("   👉 Acabado (${paint.finishCoats} manos): *${paint.finishUnits} ${paint.unitLabelForTotal}*\n")
        }

        cost?.let { wallCost ->
            sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("💰 *PRESUPUESTO ESTIMADO:* *${NumberFormatUtils.formatCurrencyCop(wallCost.materialsSubtotal)}*\n")
            if (!wallCost.complete) {
                sb.append("⚠️ _(Faltan ${wallCost.missingPrices} insumos por cotizar)_\n")
            }
        }

        notes?.takeIf { it.isNotBlank() }?.let {
            sb.append("\n📝 *Notas:* $it\n")
        }

        sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📱 Generado con *DrywallCalc*")

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
