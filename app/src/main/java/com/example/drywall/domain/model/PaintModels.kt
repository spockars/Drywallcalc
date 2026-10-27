package com.example.drywall.domain.model

/**
 * Unidad comercial en la que se vende el producto de pintura.
 */
enum class PaintUnit(val label: String, val pluralLabel: String) {
    GALON("Galón", "Galones"),
    LITRO("Litro", "Litros")
}

/**
 * Producto de pintura con su rendimiento real de ficha técnica.
 *
 * El rendimiento no se asume: es un dato editable que el usuario toma de la etiqueta
 * del producto, expresado en metros cuadrados por envase.
 */
data class PaintProduct(
    val name: String = "",
    val unit: PaintUnit = PaintUnit.GALON,
    val coverageM2PerUnit: Double = 0.0
) {
    val displayName: String
        get() = name.ifBlank { "Pintura sin especificar" }
}

/**
 * Configuración de pintura de un muro: una mano de sellador más N manos de acabado.
 */
data class PaintSpec(
    val finishCoats: Int = DEFAULT_FINISH_COATS,
    val wasteFactor: Double = DEFAULT_WASTE_FACTOR,
    val product: PaintProduct = PaintProduct()
) {
    companion object {
        /** El sellador siempre se aplica en una sola mano. */
        const val SEALER_COATS: Int = 1

        const val DEFAULT_FINISH_COATS: Int = 2

        const val DEFAULT_WASTE_FACTOR: Double = 0.10
    }

    val totalCoats: Int
        get() = SEALER_COATS + finishCoats
}

/**
 * Resultado del cálculo de pintura de un muro.
 *
 * Se guarda por muro para poder reproducir el cómputo al recargar un proyecto.
 */
data class PaintEstimate(
    val areaM2: Double,
    val sealerCoats: Int,
    val finishCoats: Int,
    val coverageM2PerUnit: Double,
    val unit: PaintUnit = PaintUnit.GALON,
    val wasteFactor: Double,
    val sealerUnits: Int,
    val finishUnits: Int,
    val productName: String = ""
) {
    val totalUnits: Int
        get() = sealerUnits + finishUnits

    /** Etiqueta del envase en singular. */
    val unitLabel: String
        get() = unit.label

    /** Etiqueta del envase concordante con la cantidad total. */
    val unitLabelForTotal: String
        get() = if (totalUnits == 1) unit.label.lowercase() else unit.pluralLabel.lowercase()

    val isValid: Boolean
        get() = areaM2 > 0.0 && coverageM2PerUnit > 0.0 && finishCoats > 0 && totalUnits > 0

    companion object {
        /** Estimación vacía y explícitamente inválida, para muros sin pintura configurada. */
        fun empty(): PaintEstimate = PaintEstimate(
            areaM2 = 0.0,
            sealerCoats = PaintSpec.SEALER_COATS,
            finishCoats = 0,
            coverageM2PerUnit = 0.0,
            unit = PaintUnit.GALON,
            wasteFactor = 0.0,
            sealerUnits = 0,
            finishUnits = 0,
            productName = ""
        )
    }
}
