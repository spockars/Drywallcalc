package com.example.drywall.domain.model

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
    val formattedCommercialSummary: String
        get() = "$commercialQuantity ${if (commercialQuantity == 1) unit.singularName else unit.pluralName}"
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
    val wasteFactor: Double = 0.08
) {
    init {
        require(lengthMeters > 0) { "El largo del muro debe ser mayor a 0 metros" }
        require(heightMeters > 0) { "El alto del muro debe ser mayor a 0 metros" }
        require(studSpacingMeters > 0) { "La separación entre parantes debe ser mayor a 0" }
        require(wasteFactor >= 0) { "El factor de desperdicio no puede ser negativo" }
    }

    val wallAreaOneFace: Double
        get() = lengthMeters * heightMeters

    val totalDrywallArea: Double
        get() = wallAreaOneFace * faces.factor

    val perimeterMeters: Double
        get() = 2 * (lengthMeters + heightMeters)
}

/**
 * Resultado completo y categorizado del cálculo de materiales.
 */
data class WallCalculationResult(
    val input: WallCalculationInput,
    val requirements: List<MaterialRequirement>,
    val calculationTimestamp: Long = System.currentTimeMillis()
) {
    val requirementsByCategory: Map<MaterialCategory, List<MaterialRequirement>> =
        requirements.groupBy { it.material.category }

    val totalBoardsRequired: Int
        get() = requirements.firstOrNull { it.material.category == MaterialCategory.PLACAS }?.commercialQuantity ?: 0

    val totalProfiles3mRequired: Int
        get() = requirements
            .filter { it.material.category == MaterialCategory.ESTRUCTURA && it.unit == UnitOfMeasure.PIEZAS_3M }
            .sumOf { it.commercialQuantity }
}
