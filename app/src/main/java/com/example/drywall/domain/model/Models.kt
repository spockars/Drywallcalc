package com.example.drywall.domain.model

import com.example.drywall.util.NumberFormatUtils

/**
 * Categorías principales de materiales para construcción en seco (Drywall).
 */
enum class MaterialCategory(val displayName: String, val iconName: String) {
    ESTRUCTURA("Estructura Metálica", "architecture"),
    PLACAS("Placas de Yeso", "layers"),
    FIJACIONES("Tornillería y Anclajes", "build"),
    ACABADOS("Tratamiento de Juntas", "brush"),
    AISLAMIENTO("Aislamiento Termoacústico", "shield")
}

/**
 * Unidades de medida técnicas y presentaciones comerciales.
 */
enum class UnitOfMeasure(val symbol: String, val singularName: String, val pluralName: String) {
    UNIDADES("unid", "unidad", "unidades"),
    PIEZAS_3M("piezas", "pieza de 3.0 m", "piezas de 3.0 m"),
    PLACAS_ESTANDAR("placas", "placa (1.22 x 2.44 m)", "placas (1.22 x 2.44 m)"),
    METROS_LINEALES("ml", "metro lineal", "metros lineales"),
    METROS_CUADRADOS("m²", "metro cuadrado", "metros cuadrados"),
    KILOGRAMOS("kg", "kilogramo", "kilogramos"),
    ROLLOS("rollos", "rollo", "rollos"),
    BALDES_28KG("baldes", "balde de 28 kg", "baldes de 28 kg")
}

/**
 * Tipos de placa de yeso comúnmente usadas según el ambiente.
 */
enum class DrywallBoardType(
    val code: String,
    val displayName: String,
    val description: String,
    val thicknessMm: Double
) {
    ESTANDAR_ST("ST", "Estándar (ST 1/2\")", "Para muros interiores y cielos rasos estándar", 12.7),
    RESISTENTE_HUMEDAD_RH("RH", "Resistente a Humedad (RH 1/2\")", "Ideal para baños, cocinas y zonas húmedas", 12.7),
    RESISTENTE_FUEGO_RF("RF", "Resistente al Fuego (RF 5/8\")", "Mayor resistencia al fuego para salidas de emergencia y cocinas", 15.9)
}

/**
 * Cantidad de caras emplacadas del muro.
 */
enum class WallFaces(val factor: Int, val label: String, val detail: String) {
    ONE_FACE(1, "1 Cara", "Revestimiento o trasdosado sobre muro existente"),
    BOTH_FACES(2, "2 Caras", "Muro divisorio completo con ambas caras emplacadas")
}

/**
 * Tipo de vano (abertura) que puede atravesar un muro de drywall.
 */
enum class OpeningType(val label: String, val detail: String) {
    PUERTA("Puerta", "Circulación peatonal entre ambientes"),
    VENTANA("Ventana", "Iluminación y ventilación natural")
}

/**
 * Vano (puerta o ventana) que atraviesa el muro de lado a lado.
 *
 * El vano descuenta superficie emplazada en **cada cara** del muro, exige refuerzo
 * estructural (dos postes de jamba y un canal de dintel) y consume cinta adicional
 * en todo su perímetro.
 */
data class WallOpening(
    val type: OpeningType = OpeningType.PUERTA,
    val label: String = "",
    val widthMeters: Double = 0.0,
    val heightMeters: Double = 0.0
) {
    /** Superficie del vano en metros cuadrados (ancho x alto). */
    val areaM2: Double
        get() = widthMeters * heightMeters

    /** Perímetro del vano en metros lineales (2 x (ancho + alto)). */
    val perimeterMeters: Double
        get() = 2 * (widthMeters + heightMeters)

    /** Nombre legible para mensajes de validación y reportes. */
    fun displayName(position: Int): String {
        val base = label.ifBlank { "${type.label} #$position" }
        return "$base (${NumberFormatUtils.formatDecimal(widthMeters)} x ${NumberFormatUtils.formatDecimal(heightMeters)} m)"
    }
}

/**
 * Representa la definición base de un insumo de construcción liviana en seco.
 */
data class DrywallMaterial(
    val id: String,
    val code: String,
    val name: String,
    val category: MaterialCategory,
    val defaultUnit: UnitOfMeasure,
    val commercialSpec: String,
    val technicalFunction: String
)

/**
 * Requisito cuantificado de un material para el muro evaluado.
 */
data class MaterialRequirement(
    val material: DrywallMaterial,
    val rawQuantity: Double,
    val wastePercentage: Double,
    val quantityWithWaste: Double,
    val commercialQuantity: Int,
    val unit: UnitOfMeasure,
    val formulaExplanation: String
) {
    /** Nombre de la unidad concordante con la cantidad comercial (singular o plural). */
    val unitLabel: String
        get() = if (commercialQuantity == 1) unit.singularName else unit.pluralName

    val formattedCommercialSummary: String
        get() = "$commercialQuantity $unitLabel"
}

/**
 * Parámetros de entrada para el cálculo de un muro en seco.
 */
data class WallCalculationInput(
    val lengthMeters: Double,
    val heightMeters: Double,
    val faces: WallFaces = WallFaces.BOTH_FACES,
    val hasInsulation: Boolean = false,
    val studSpacingMeters: Double = 0.407,
    val boardType: DrywallBoardType = DrywallBoardType.ESTANDAR_ST,
    val wasteFactor: Double = 0.08,
    val openings: List<WallOpening> = emptyList()
) {
    init {
        require(lengthMeters > 0) { "El largo del muro debe ser mayor a 0 metros" }
        require(heightMeters > 0) { "El alto del muro debe ser mayor a 0 metros" }
        require(studSpacingMeters > 0) { "La separación entre parantes debe ser mayor a 0" }
        require(wasteFactor >= 0) { "El factor de desperdicio no puede ser negativo" }
    }

    /** Área geométrica bruta de una cara, sin descontar vanos. */
    val wallAreaOneFace: Double
        get() = lengthMeters * heightMeters

    /** Área geométrica bruta de las caras emplacadas, sin descontar vanos. */
    val totalDrywallArea: Double
        get() = wallAreaOneFace * faces.factor

    /** Perímetro del muro en metros lineales. */
    val perimeterMeters: Double
        get() = 2 * (lengthMeters + heightMeters)

    /** Suma de las superficies de todos los vanos (una cara). */
    val openingsAreaOneFace: Double
        get() = openings.sumOf { it.areaM2 }

    /** Área neta emplazable de una cara (bruta menos vanos). */
    val netAreaOneFace: Double
        get() = (wallAreaOneFace - openingsAreaOneFace).coerceAtLeast(0.0)

    /** Área neta emplazable de todas las caras del muro. */
    val netTotalDrywallArea: Double
        get() = netAreaOneFace * faces.factor

    /** Suma de perímetros de vanos (base del refuerzo de cinta). */
    val openingsPerimeterMeters: Double
        get() = openings.sumOf { it.perimeterMeters }

    /** Cantidad de postes de jamba adicionales (2 por cada vano). */
    val jambStudsCount: Int
        get() = openings.size * 2

    /** Longitud total de canal usado como dintel (un canal por vano). */
    val headersLinearMeters: Double
        get() = openings.sumOf { it.widthMeters }

    /**
     * Valida la coherencia geométrica de los vanos contra las dimensiones del muro.
     *
     * @return lista de mensajes de error en español; vacía cuando los vanos son válidos.
     */
    fun validateOpenings(): List<String> {
        if (openings.isEmpty()) return emptyList()

        val errors = mutableListOf<String>()

        openings.forEachIndexed { index, opening ->
            val name = opening.displayName(index + 1)
            if (opening.widthMeters <= 0.0) {
                errors += "El ancho del vano $name debe ser mayor a 0 m."
            }
            if (opening.heightMeters <= 0.0) {
                errors += "El alto del vano $name debe ser mayor a 0 m."
            }
            if (opening.widthMeters > lengthMeters) {
                errors += "El vano $name excede el largo del muro (${NumberFormatUtils.formatMeters(lengthMeters)})."
            }
            if (opening.heightMeters > heightMeters) {
                errors += "El vano $name excede el alto del muro (${NumberFormatUtils.formatMeters(heightMeters)})."
            }
        }

        val totalWidth = openings.sumOf { it.widthMeters }
        if (totalWidth > lengthMeters) {
            errors += "La suma de anchos de los vanos (${NumberFormatUtils.formatMeters(totalWidth)}) " +
                "excede el largo del muro (${NumberFormatUtils.formatMeters(lengthMeters)})."
        }

        if (openingsAreaOneFace >= wallAreaOneFace) {
            errors += "Los vanos suman ${NumberFormatUtils.formatSquareMeters(openingsAreaOneFace)} " +
                "sobre un muro de ${NumberFormatUtils.formatSquareMeters(wallAreaOneFace)}: " +
                "el muro quedaría sin superficie emplazable."
        }

        return errors.distinct()
    }
}

/**
 * Resultado completo y categorizado del cálculo de materiales.
 */
data class WallCalculationResult(
    val input: WallCalculationInput,
    val requirements: List<MaterialRequirement>,
    val calculationTimestamp: Long = System.currentTimeMillis(),
    /** Estimación de pintura del muro, calculada fuera del motor de drywall. */
    val paint: PaintEstimate? = null
) {
    val requirementsByCategory: Map<MaterialCategory, List<MaterialRequirement>> =
        requirements.groupBy { it.material.category }

    val totalBoardsRequired: Int
        get() = requirements.firstOrNull { it.material.category == MaterialCategory.PLACAS }?.commercialQuantity ?: 0

    val totalProfiles3mRequired: Int
        get() = requirements
            .filter { it.material.category == MaterialCategory.ESTRUCTURA && it.unit == UnitOfMeasure.PIEZAS_3M }
            .sumOf { it.commercialQuantity }

    /** Área neta emplazable de una cara (bruta menos vanos). */
    val netAreaOneFace: Double
        get() = input.netAreaOneFace

    /** Área neta emplazable de todas las caras. */
    val netTotalDrywallArea: Double
        get() = input.netTotalDrywallArea

    /** Superficie total descontada por vanos (una cara). */
    val openingsAreaM2: Double
        get() = input.openingsAreaOneFace

    /** Cantidad de vanos considerados en el cálculo. */
    val openingsCount: Int
        get() = input.openings.size
}
