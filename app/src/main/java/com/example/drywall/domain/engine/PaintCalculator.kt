package com.example.drywall.domain.engine

import com.example.drywall.domain.model.PaintEstimate
import com.example.drywall.domain.model.PaintSpec
import com.example.drywall.domain.model.PaintUnit
import com.example.drywall.util.NumberFormatUtils
import kotlin.math.ceil

/**
 * Calculadora de pintura, deliberadamente separada del motor de drywall.
 *
 * Reglas:
 * - Área a pintar: área neta de placa del muro (bruta menos vanos, por cada cara emplacada).
 * - Una mano de sellador más N manos de acabado (2 por defecto, configurable).
 * - Consumo = área x manos x (1 + merma) / rendimiento del producto.
 * - El rendimiento es un dato editable por producto (m² por galón o m² por litro); no hay
 *   constantes de rendimiento inventadas.
 * - El resultado se redondea a envases enteros, por etapa (sellador y acabado).
 */
object PaintCalculator {

    /**
     * Valida la configuración antes de calcular.
     *
     * @return mensajes en español; lista vacía cuando la configuración es utilizable.
     */
    fun validate(areaM2: Double, spec: PaintSpec): List<String> {
        val errors = mutableListOf<String>()

        if (areaM2 <= 0.0) {
            errors += "El muro no tiene área neta de placa para pintar."
        }
        if (spec.product.coverageM2PerUnit <= 0.0) {
            errors += "Ingresa el rendimiento del producto en m² por ${spec.product.unit.label.lowercase()}."
        }
        if (spec.finishCoats < 1) {
            errors += "El número de manos de acabado debe ser al menos 1."
        }
        if (spec.wasteFactor < 0.0) {
            errors += "La merma de pintura no puede ser negativa."
        }

        return errors
    }

    /**
     * Calcula los envases necesarios.
     *
     * @throws IllegalArgumentException si la configuración no es válida.
     */
    fun estimate(areaM2: Double, spec: PaintSpec): PaintEstimate {
        val errors = validate(areaM2, spec)
        require(errors.isEmpty()) { errors.joinToString(" ") }

        val multiplier = 1.0 + spec.wasteFactor
        val coverage = spec.product.coverageM2PerUnit

        val sealerUnits = ceil(areaM2 * PaintSpec.SEALER_COATS * multiplier / coverage).toInt()
        val finishUnits = ceil(areaM2 * spec.finishCoats * multiplier / coverage).toInt()

        return PaintEstimate(
            areaM2 = areaM2,
            sealerCoats = PaintSpec.SEALER_COATS,
            finishCoats = spec.finishCoats,
            coverageM2PerUnit = coverage,
            unit = spec.product.unit,
            wasteFactor = spec.wasteFactor,
            sealerUnits = sealerUnits,
            finishUnits = finishUnits,
            productName = spec.product.name
        )
    }

    /**
     * Texto de sustento del cálculo, usado en la UI y en los reportes.
     */
    fun explain(estimate: PaintEstimate): String {
        val unit = estimate.unitLabel.lowercase()
        return buildString {
            append(
                "${NumberFormatUtils.formatSquareMeters(estimate.areaM2)} de placa neta × " +
                    "${estimate.sealerCoats} mano de sellador + ${estimate.finishCoats} manos de acabado"
            )
            append(
                ", merma ${NumberFormatUtils.formatDecimal(estimate.wasteFactor * 100, 0)}%, " +
                    "rendimiento ${NumberFormatUtils.formatDecimal(estimate.coverageM2PerUnit)} m²/$unit"
            )
            append(
                " = ${estimate.sealerUnits} + ${estimate.finishUnits} = " +
                    "${estimate.totalUnits} ${estimate.unitLabelForTotal}"
            )
            if (estimate.productName.isNotBlank()) {
                append(" (${estimate.productName})")
            }
        }
    }

    /**
     * Etiqueta de la unidad con concordancia de número.
     */
    fun unitLabelFor(units: Int, unit: PaintUnit): String =
        if (units == 1) unit.label.lowercase() else unit.pluralLabel.lowercase()
}
