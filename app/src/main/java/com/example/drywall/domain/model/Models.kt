package com.example.drywall.domain.model

import com.example.drywall.util.NumberFormatUtils

/**
 * Tipo de elemento constructivo en drywall / construcción liviana.
 */
enum class ConstructionElementType(
    val code: String,
    val displayName: String,
    val iconName: String,
    val description: String
) {
    MURO_DRYWALL("MURO_DRYWALL", "Muro de Drywall", "layers", "Muros divisorios y revestimientos"),
    CIELO_RASO_DRYWALL("CIELO_RASO_DRYWALL", "Cielo Raso Drywall", "ceiling", "Cielos rasos suspendidos de yeso"),
    CIELO_RASO_PVC("CIELO_RASO_PVC", "Cielo Raso PVC", "grid_view", "Cielos rasos en tablillas machihembradas PVC"),
    MUEBLE_DRYWALL("MUEBLE_DRYWALL", "Mueble / Repisa Drywall", "shelves", "Muebles de TV, repisas y nichos")
}

/**
 * Categorías principales de materiales para construcción en seco (Drywall).
 */
enum class MaterialCategory(val displayName: String, val iconName: String) {
    ESTRUCTURA("Estructura Metálica", "architecture"),
    PLACAS("Placas y Tablillas", "layers"),
    FIJACIONES("Tornillería y Anclajes", "build"),
    ACABADOS("Tratamiento de Juntas y Acabados", "brush"),
    AISLAMIENTO("Aislamiento Termoacústico", "shield")
}

/**
 * Unidades de medida técnicas y presentaciones comerciales.
 */
enum class UnitOfMeasure(val symbol: String, val singularName: String, val pluralName: String) {
    UNIDADES("unid", "unidad", "unidades"),
    PIEZAS_2_44M("piezas", "pieza de 2.44 m", "piezas de 2.44 m"),
    PIEZAS_3M("piezas", "pieza de 3.0 m", "piezas de 3.0 m"),
    PIEZAS_5_95M("piezas", "pieza de 5.95 m", "piezas de 5.95 m"),
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
    val thicknessMm: Double,
    val isFiberCement: Boolean = false
) {
    ESTANDAR_ST("ST", "Estándar (ST 1/2\")", "Para muros interiores y cielos rasos estándar", 12.7, false),
    RESISTENTE_HUMEDAD_RH("RH", "Resistente a Humedad (RH 1/2\")", "Ideal para baños, cocinas y zonas húmedas", 12.7, false),
    RESISTENTE_FUEGO_RF("RF", "Resistente al Fuego (RF 5/8\")", "Mayor resistencia al fuego para salidas de emergencia y cocinas", 15.9, false),
    FIBROCEMENTO_6MM("FC-6", "Fibrocemento Superboard 6 mm", "Ideal para revestimientos exteriores y faldones", 6.0, true),
    FIBROCEMENTO_8MM("FC-8", "Fibrocemento Superboard 8 mm", "Para fachadas exteriores y muros semipesados", 8.0, true),
    FIBROCEMENTO_10MM("FC-10", "Fibrocemento Superboard 10 mm", "Alta resistencia para fachadas y intemperie", 10.0, true)
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
 * Anchos de perfiles metálicos según la norma colombiana (NSR-10 / Icontec).
 */
enum class ProfileWidth(val code: String, val displayName: String, val sizeMm: Int) {
    MM_60("60mm", "60 mm (Estándar 2-1/2\")", 60),
    MM_64("64mm", "64 mm (Comercial 2-1/2\")", 64),
    MM_90("90mm", "90 mm (Estructural 3-5/8\")", 90)
}

/**
 * Calibres de acero galvanizado según norma técnica.
 */
enum class ProfileGauge(val code: String, val displayName: String, val description: String) {
    CALIBRE_26("CAL_26", "Calibre 26 (Económico / Residencial)", "Espesor nominal ~0.45 mm"),
    CALIBRE_24("CAL_24", "Calibre 24 (Comercial Estándar)", "Espesor nominal ~0.55 mm"),
    CALIBRE_22("CAL_22", "Calibre 22 (Estructural / Comercial Pesado)", "Espesor nominal ~0.75 mm"),
    CALIBRE_20("CAL_20", "Calibre 20 (Estructural Pesado / Fachadas)", "Espesor nominal ~0.95 mm")
}

/**
 * Longitudes comerciales de perfiles metálicos en Colombia y Latinoamérica.
 */
enum class ProfileLength(val meters: Double, val displayName: String) {
    METERS_2_44(2.44, "2.44 m (8 pies - Estándar Colombia)"),
    METERS_3_00(3.00, "3.00 m (Industrial / Estándar)")
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
    val boardTypeFaceA: DrywallBoardType = boardType,
    val boardTypeFaceB: DrywallBoardType = boardType,
    val wasteFactor: Double = 0.08,
    val profileWidth: ProfileWidth = ProfileWidth.MM_60,
    val profileGauge: ProfileGauge = ProfileGauge.CALIBRE_26,
    val commercialProfileLengthMeters: Double = 2.44,
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

    val totalProfilesRequired: Int
        get() = requirements
            .filter { it.material.category == MaterialCategory.ESTRUCTURA && (it.unit == UnitOfMeasure.PIEZAS_2_44M || it.unit == UnitOfMeasure.PIEZAS_3M) }
            .sumOf { it.commercialQuantity }

    val totalProfiles3mRequired: Int
        get() = totalProfilesRequired

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

/**
 * Parámetros para el cálculo de un cielo raso suspendido en drywall.
 */
data class DrywallCeilingInput(
    val lengthMeters: Double,
    val widthMeters: Double,
    val suspensionHeightMeters: Double = 0.30,
    val omegaSpacingMeters: Double = 0.407,
    val mainBeamSpacingMeters: Double = 0.80,
    val boardType: DrywallBoardType = DrywallBoardType.ESTANDAR_ST,
    val hasInsulation: Boolean = false,
    val wasteFactor: Double = 0.08
) {
    init {
        require(lengthMeters > 0) { "El largo del techo debe ser mayor a 0 m" }
        require(widthMeters > 0) { "El ancho del techo debe ser mayor a 0 m" }
        require(suspensionHeightMeters >= 0) { "La altura de descolgado no puede ser negativa" }
    }

    val areaM2: Double
        get() = lengthMeters * widthMeters

    val perimeterMeters: Double
        get() = 2 * (lengthMeters + widthMeters)
}

/**
 * Parámetros para el cálculo de un cielo raso en tablillas PVC.
 */
data class PvcCeilingInput(
    val lengthMeters: Double,
    val widthMeters: Double,
    val slatWidthMeters: Double = 0.25,
    val commercialSlatLengthMeters: Double = 5.95,
    val supportSpacingMeters: Double = 0.50,
    val wasteFactor: Double = 0.08
) {
    init {
        require(lengthMeters > 0) { "El largo del techo debe ser mayor a 0 m" }
        require(widthMeters > 0) { "El ancho del techo debe ser mayor a 0 m" }
        require(slatWidthMeters > 0) { "El ancho de la tablilla debe ser mayor a 0 m" }
        require(commercialSlatLengthMeters > 0) { "El largo comercial debe ser mayor a 0 m" }
    }

    val areaM2: Double
        get() = lengthMeters * widthMeters

    val perimeterMeters: Double
        get() = 2 * (lengthMeters + widthMeters)
}

/**
 * Parámetros para el cálculo de un mueble / repisa en drywall.
 */
data class FurnitureCalculationInput(
    val widthMeters: Double,
    val heightMeters: Double,
    val depthMeters: Double,
    val horizontalShelvesCount: Int = 2,
    val verticalDividersCount: Int = 1,
    val boardType: DrywallBoardType = DrywallBoardType.ESTANDAR_ST,
    val wasteFactor: Double = 0.12
) {
    init {
        require(widthMeters > 0) { "El ancho del mueble debe ser mayor a 0 m" }
        require(heightMeters > 0) { "El alto del mueble debe ser mayor a 0 m" }
        require(depthMeters > 0) { "La profundidad debe ser mayor a 0 m" }
        require(horizontalShelvesCount >= 0) { "El número de repisas no puede ser negativo" }
        require(verticalDividersCount >= 0) { "El número de separadores no puede ser negativo" }
    }

    /**
     * Estimación de la superficie total a revestir (caras frontales, laterales, repisas superiores e inferiores).
     */
    val totalExposedAreaM2: Double
        get() {
            val mainFrameArea = (widthMeters * heightMeters) * 2
            val shelfArea = (widthMeters * depthMeters) * (horizontalShelvesCount + 1) * 2
            val dividerArea = (heightMeters * depthMeters) * (verticalDividersCount + 1) * 2
            return mainFrameArea + shelfArea + dividerArea
        }

    /**
     * Metros lineales de cantos exteriores que exigen esquinero metálico perforado.
     */
    val totalCornerBeadLinearMeters: Double
        get() {
            val mainFrameEdges = (widthMeters * 2) + (heightMeters * 2)
            val shelfEdges = widthMeters * (horizontalShelvesCount + 1) * 2
            val dividerEdges = heightMeters * (verticalDividersCount + 1) * 2
            return mainFrameEdges + shelfEdges + dividerEdges
        }
}

