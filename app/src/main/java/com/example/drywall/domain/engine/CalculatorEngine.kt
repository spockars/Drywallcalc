package com.example.drywall.domain.engine

import com.example.drywall.domain.model.DrywallBoardType
import com.example.drywall.domain.model.DrywallCeilingInput
import com.example.drywall.domain.model.DrywallMaterial
import com.example.drywall.domain.model.FurnitureCalculationInput
import com.example.drywall.domain.model.MaterialCategory
import com.example.drywall.domain.model.MaterialRequirement
import com.example.drywall.domain.model.PvcCeilingInput
import com.example.drywall.domain.model.UnitOfMeasure
import com.example.drywall.domain.model.WallCalculationInput
import com.example.drywall.domain.model.WallCalculationResult
import com.example.drywall.domain.model.WallFaces
import com.example.drywall.domain.model.WallOpening
import com.example.drywall.util.NumberFormatUtils
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Catálogo estándar de insumos para construcción liviana en seco (Drywall).
 */
object DrywallCatalog {
    // Estructura metálica 2.44 m (Estándar Colombia)
    val CANAL_GUIA_2_44M = DrywallMaterial(
        id = "canal_guia_2_44m",
        code = "CANAL-60-2.44M",
        name = "Canal / Solera Guía (60 mm x 2.44 m)",
        category = MaterialCategory.ESTRUCTURA,
        defaultUnit = UnitOfMeasure.PIEZAS_2_44M,
        commercialSpec = "Perfil de acero galvanizado cal. 26 x 2.44 m (base para piso y techo)",
        technicalFunction = "Guía horizontal anclada a piso y losa para recibir los montantes verticales"
    )

    val POSTE_MONTANTE_2_44M = DrywallMaterial(
        id = "poste_montante_2_44m",
        code = "POSTE-59-2.44M",
        name = "Poste / Parante / Montante (59 mm x 2.44 m)",
        category = MaterialCategory.ESTRUCTURA,
        defaultUnit = UnitOfMeasure.PIEZAS_2_44M,
        commercialSpec = "Perfil de acero galvanizado cal. 26 x 2.44 m con perforaciones para ductos",
        technicalFunction = "Estructura vertical portante que recibe las placas de yeso fijadas con tornillos"
    )

    val PERFIL_OMEGA_2_44M = DrywallMaterial(
        id = "perfil_omega_2_44m",
        code = "OMEGA-2.44M",
        name = "Perfil Omega Rigidizador (2.44 m)",
        category = MaterialCategory.ESTRUCTURA,
        defaultUnit = UnitOfMeasure.PIEZAS_2_44M,
        commercialSpec = "Perfil omega galvanizado cal. 26 x 2.44 m",
        technicalFunction = "Refuerzo y arriostramiento horizontal intermedio para muros o trasdosados"
    )

    val PERFIL_ANGULO_PERIMETRAL_2_44M = DrywallMaterial(
        id = "perfil_angulo_2_44m",
        code = "ANGULO-L-2.44M",
        name = "Perfil Ángulo L Perimetral (2.44 m)",
        category = MaterialCategory.ESTRUCTURA,
        defaultUnit = UnitOfMeasure.PIEZAS_2_44M,
        commercialSpec = "Perfil en L de acero galvanizado cal. 26 x 2.44 m",
        technicalFunction = "Soporte perimetral anclado a muro para recibir estructuras de cielo raso"
    )

    // Estructura metálica 3.00 m (Industrial)
    val CANAL_GUIA_3M = DrywallMaterial(
        id = "canal_guia_3m",
        code = "CANAL-60-3M",
        name = "Canal / Solera Guía (60 mm x 3.0 m)",
        category = MaterialCategory.ESTRUCTURA,
        defaultUnit = UnitOfMeasure.PIEZAS_3M,
        commercialSpec = "Perfil de acero galvanizado cal. 26 x 3.00 m (base para piso y techo)",
        technicalFunction = "Guía horizontal anclada a piso y losa para recibir los montantes verticales"
    )

    val POSTE_MONTANTE_3M = DrywallMaterial(
        id = "poste_montante_3m",
        code = "POSTE-59-3M",
        name = "Poste / Parante / Montante (59 mm x 3.0 m)",
        category = MaterialCategory.ESTRUCTURA,
        defaultUnit = UnitOfMeasure.PIEZAS_3M,
        commercialSpec = "Perfil de acero galvanizado cal. 26 x 3.00 m con perforaciones para ductos",
        technicalFunction = "Estructura vertical portante que recibe las placas de yeso fijadas con tornillos"
    )

    val PERFIL_OMEGA_3M = DrywallMaterial(
        id = "perfil_omega_3m",
        code = "OMEGA-3M",
        name = "Perfil Omega Rigidizador (3.0 m)",
        category = MaterialCategory.ESTRUCTURA,
        defaultUnit = UnitOfMeasure.PIEZAS_3M,
        commercialSpec = "Perfil omega galvanizado cal. 26 x 3.00 m",
        technicalFunction = "Refuerzo y arriostramiento horizontal intermedio para muros altos (>2.6 m) o trasdosados"
    )

    val PERFIL_ANGULO_PERIMETRAL_3M = DrywallMaterial(
        id = "perfil_angulo_3m",
        code = "ANGULO-L-3M",
        name = "Perfil Ángulo L Perimetral (3.0 m)",
        category = MaterialCategory.ESTRUCTURA,
        defaultUnit = UnitOfMeasure.PIEZAS_3M,
        commercialSpec = "Perfil en L de acero galvanizado cal. 26 x 3.00 m",
        technicalFunction = "Soporte perimetral anclado a muro para recibir estructuras de cielo raso"
    )

    fun getTrackMaterial(lengthMeters: Double): DrywallMaterial =
        if (lengthMeters <= 2.50) CANAL_GUIA_2_44M else CANAL_GUIA_3M

    fun getStudMaterial(lengthMeters: Double): DrywallMaterial =
        if (lengthMeters <= 2.50) POSTE_MONTANTE_2_44M else POSTE_MONTANTE_3M

    fun getOmegaMaterial(lengthMeters: Double): DrywallMaterial =
        if (lengthMeters <= 2.50) PERFIL_OMEGA_2_44M else PERFIL_OMEGA_3M

    fun getAngleMaterial(lengthMeters: Double): DrywallMaterial =
        if (lengthMeters <= 2.50) PERFIL_ANGULO_PERIMETRAL_2_44M else PERFIL_ANGULO_PERIMETRAL_3M

    val ALAMBRE_GALVANIZADO = DrywallMaterial(
        id = "alambre_galvanizado_n12",
        code = "ALAMBRE-GALV-KG",
        name = "Alambre Galvanizado N° 12 (kg)",
        category = MaterialCategory.FIJACIONES,
        defaultUnit = UnitOfMeasure.KILOGRAMOS,
        commercialSpec = "Alambre galvanizado de alta resistencia N° 12 para suspensión",
        technicalFunction = "Sujeta y suspende las vigas principales del cielo raso a la losa o techo"
    )

    // Materiales para Cielo Raso en PVC
    val TABLILLA_PVC_5_95M = DrywallMaterial(
        id = "tablilla_pvc_5_95m",
        code = "TABLILLA-PVC-25CM-5.95M",
        name = "Tablilla de PVC Machihembrada (0.25 x 5.95 m)",
        category = MaterialCategory.PLACAS,
        defaultUnit = UnitOfMeasure.PIEZAS_5_95M,
        commercialSpec = "Lámina de PVC machihembrada de 25 cm de ancho x 5.95 m de largo",
        technicalFunction = "Revestimiento acabado impermeable para cielos rasos en PVC"
    )

    val CORNISA_PVC_3M = DrywallMaterial(
        id = "cornisa_pvc_3m",
        code = "CORNISA-PVC-3M",
        name = "Cornisa / Moldura Perimetral PVC (3.0 m)",
        category = MaterialCategory.ACABADOS,
        defaultUnit = UnitOfMeasure.PIEZAS_3M,
        commercialSpec = "Perfil perimetral en J / cornisa de PVC de 3.00 m",
        technicalFunction = "Recibe y remata estéticamente las tablillas de PVC en todo el perímetro"
    )

    val TORNILLO_SOMBRERILLO_PVC = DrywallMaterial(
        id = "tornillo_sombrerillo_pvc",
        code = "TORN-SOMBRERILLO-8x1/2",
        name = "Tornillo Sombrerillo para PVC (8 x 1/2\")",
        category = MaterialCategory.FIJACIONES,
        defaultUnit = UnitOfMeasure.UNIDADES,
        commercialSpec = "Tornillo cabeza extraplana tipo sombrerillo para machihembrado de PVC",
        technicalFunction = "Sujeta la pestaña del PVC a la estructura metálica sin dañar la lámina"
    )

    // Esquineros para Muebles
    val ESQUINERO_PERFORADO_3M = DrywallMaterial(
        id = "esquinero_perforado_3m",
        code = "ESQUINERO-METAL-3M",
        name = "Esquinero Metálico Perforado (3.0 m)",
        category = MaterialCategory.ESTRUCTURA,
        defaultUnit = UnitOfMeasure.PIEZAS_3M,
        commercialSpec = "Protector de canto metálico perforado cal. 26 x 3.00 m",
        technicalFunction = "Protege contra impactos y define aristas rectas en esquinas exteriores de muebles"
    )

    // Placas de yeso según tipo
    fun getBoardMaterial(type: DrywallBoardType): DrywallMaterial = when (type) {
        DrywallBoardType.ESTANDAR_ST -> DrywallMaterial(
            id = "placa_st_12_7",
            code = "PLACA-ST-12.7",
            name = "Placa de Yeso Estándar ST (1/2\")",
            category = MaterialCategory.PLACAS,
            defaultUnit = UnitOfMeasure.PLACAS_ESTANDAR,
            commercialSpec = "Placa de yeso 1.22 m x 2.44 m (área 2.98 m²), espesor 12.7 mm",
            technicalFunction = "Revestimiento interior para muros divisorios y cielos rasos en ambientes secos"
        )
        DrywallBoardType.RESISTENTE_HUMEDAD_RH -> DrywallMaterial(
            id = "placa_rh_12_7",
            code = "PLACA-RH-12.7",
            name = "Placa de Yeso Antihumedad RH (1/2\")",
            category = MaterialCategory.PLACAS,
            defaultUnit = UnitOfMeasure.PLACAS_ESTANDAR,
            commercialSpec = "Placa hidrófuga siliconada 1.22 m x 2.44 m (2.98 m²), color verde",
            technicalFunction = "Revestimiento para zonas de alta humedad relativa (baños, cocinas, lavanderías)"
        )
        DrywallBoardType.RESISTENTE_FUEGO_RF -> DrywallMaterial(
            id = "placa_rf_15_9",
            code = "PLACA-RF-15.9",
            name = "Placa de Yeso Antifuego RF (5/8\")",
            category = MaterialCategory.PLACAS,
            defaultUnit = UnitOfMeasure.PLACAS_ESTANDAR,
            commercialSpec = "Placa con fibra de vidrio 1.22 m x 2.44 m (2.98 m²), espesor 15.9 mm, color rosa",
            technicalFunction = "Protección pasiva contra incendios en muros cortafuego y rutas de evacuación"
        )
        DrywallBoardType.FIBROCEMENTO_6MM -> DrywallMaterial(
            id = "placa_fc_6",
            code = "PLACA-FC-6MM",
            name = "Placa de Fibrocemento Superboard (6 mm)",
            category = MaterialCategory.PLACAS,
            defaultUnit = UnitOfMeasure.PLACAS_ESTANDAR,
            commercialSpec = "Placa cementicia 1.22 m x 2.44 m, espesor 6 mm",
            technicalFunction = "Revestimiento exterior resistente a la intemperie"
        )
        DrywallBoardType.FIBROCEMENTO_8MM -> DrywallMaterial(
            id = "placa_fc_8",
            code = "PLACA-FC-8MM",
            name = "Placa de Fibrocemento Superboard (8 mm)",
            category = MaterialCategory.PLACAS,
            defaultUnit = UnitOfMeasure.PLACAS_ESTANDAR,
            commercialSpec = "Placa cementicia 1.22 m x 2.44 m, espesor 8 mm",
            technicalFunction = "Revestimiento para fachadas exteriores y muros semipesados"
        )
        DrywallBoardType.FIBROCEMENTO_10MM -> DrywallMaterial(
            id = "placa_fc_10",
            code = "PLACA-FC-10MM",
            name = "Placa de Fibrocemento Superboard (10 mm)",
            category = MaterialCategory.PLACAS,
            defaultUnit = UnitOfMeasure.PLACAS_ESTANDAR,
            commercialSpec = "Placa cementicia 1.22 m x 2.44 m, espesor 10 mm",
            technicalFunction = "Alta resistencia estructural para fachadas expuestas y intemperie"
        )
    }

    val TORNILLO_FIBROCEMENTO = DrywallMaterial(
        id = "tornillo_fibrocemento",
        code = "TORN-FC-AUTOPERFORANTE",
        name = "Tornillo para Fibrocemento con Aletas (8 x 1-5/8\")",
        category = MaterialCategory.FIJACIONES,
        defaultUnit = UnitOfMeasure.UNIDADES,
        commercialSpec = "Tornillo autoperforante con aletas escariadoras y cabeza avellanada",
        technicalFunction = "Fija placas de fibrocemento a perfiles de acero avellanando la superficie"
    )

    // Fijaciones y tornillería
    val TORNILLO_T1 = DrywallMaterial(
        id = "tornillo_t1",
        code = "TORN-T1-8x1/2",
        name = "Tornillo Estructura T1 (Punta Aguja 8 x 1/2\")",
        category = MaterialCategory.FIJACIONES,
        defaultUnit = UnitOfMeasure.UNIDADES,
        commercialSpec = "Tornillo wafer cabeza extraplana cincado para unión metal a metal",
        technicalFunction = "Fija cada montante a las soleras superior e inferior (2 por unión)"
    )

    val TORNILLO_T2 = DrywallMaterial(
        id = "tornillo_t2",
        code = "TORN-T2-6x1",
        name = "Tornillo Placa T2 (Trompeta 6 x 1\")",
        category = MaterialCategory.FIJACIONES,
        defaultUnit = UnitOfMeasure.UNIDADES,
        commercialSpec = "Tornillo fosfatado negro punta aguja para fijación de placa a perfilería",
        technicalFunction = "Sujeta las placas de yeso a los montantes cada 25-30 cm sin rasgar el cartón"
    )

    val ANCLAJE_CHAZO_TORNILLO = DrywallMaterial(
        id = "anclaje_chazo",
        code = "ANC-CHAZO-5/16",
        name = "Anclajes para Solera (Chazo + Tornillo 5/16\")",
        category = MaterialCategory.FIJACIONES,
        defaultUnit = UnitOfMeasure.UNIDADES,
        commercialSpec = "Chazo plástico / anclaje de impacto con tornillo para fijación en concreto",
        technicalFunction = "Fijación rígida de canales superior e inferior a pisos y techos cada 50 cm"
    )

    // Acabados y tratamiento de juntas
    val CINTA_PAPEL_JUNTAS = DrywallMaterial(
        id = "cinta_juntas",
        code = "CINTA-PAPEL-75M",
        name = "Cinta de Papel Microperforada (Rollo 75 m)",
        category = MaterialCategory.ACABADOS,
        defaultUnit = UnitOfMeasure.ROLLOS,
        commercialSpec = "Cinta de celulosa con pliegue central para uniones planas y esquinas",
        technicalFunction = "Refuerza mecánicamente las juntas entre placas para evitar fisuras"
    )

    val MASILLA_JUNTAS_BALDE = DrywallMaterial(
        id = "masilla_juntas",
        code = "MASILLA-BALDE-28KG",
        name = "Masilla para Drywall (Balde 28 kg)",
        category = MaterialCategory.ACABADOS,
        defaultUnit = UnitOfMeasure.BALDES_28KG,
        commercialSpec = "Compuesto para juntas listo para usar (Ready Mix) en balde de 28 kg",
        technicalFunction = "Tratamiento de juntas en 3 capas (pegado de cinta, carga y acabado superficial)"
    )

    // Aislamiento
    val LANA_AISLAMIENTO = DrywallMaterial(
        id = "lana_aislamiento",
        code = "LANA-VIDRIO-12M2",
        name = "Lana de Vidrio Termoacústica (Rollo 12 m²)",
        category = MaterialCategory.AISLAMIENTO,
        defaultUnit = UnitOfMeasure.ROLLOS,
        commercialSpec = "Colchoneta de fibra de vidrio de 50 mm espesor, rollo de 12 m²",
        technicalFunction = "Absorción acústica y aislamiento térmico en el alma del tabique divisorio"
    )
}

/**
 * Motor de cálculo de materiales de construcción liviana en seco (Drywall).
 *
 * Implementa la cuantificación técnica basada en la práctica constructiva latinoamericana
 * e internacional, aplicando el factor de desperdicio exigido (default 8%) y el redondeo
 * comercial lógico a piezas enteras comerciales (perfiles de 3m, placas 1.22x2.44m, baldes de 28kg,
 * rollos de cinta y aislamiento, y millares/cajas de tornillería).
 */
class CalculatorEngine {

    companion object {
        /**
         * Área geométrica de una placa estándar de 1.22 m x 2.44 m = 2.9768 m²
         */
        const val BOARD_WIDTH_METERS: Double = 1.22
        const val BOARD_HEIGHT_METERS: Double = 2.44
        const val BOARD_SURFACE_AREA_M2: Double = BOARD_WIDTH_METERS * BOARD_HEIGHT_METERS // ~2.9768 m²

        /**
         * Longitud comercial estándar de perfiles metálicos galvanizados (soleras y postes).
         */
        const val COMMERCIAL_PROFILE_LENGTH_METERS: Double = 3.00

        /**
         * Rendimientos de consumo técnico por m² de placa:
         */
        const val TAPE_METERS_PER_M2_BOARD: Double = 1.50   // 1.5 metros lineales de cinta por m² de placa
        const val TAPE_ROLL_COMMERCIAL_METERS: Double = 75.0 // Rollo estándar de 75 metros
        const val COMPOUND_KG_PER_M2_BOARD: Double = 1.00    // 1.0 kg de masilla por m² (3 manos completas)
        const val COMPOUND_BUCKET_KG: Double = 28.0          // Balde comercial de 28 kg
        const val SCREWS_T2_PER_BOARD: Int = 30              // Promedio de 30 tornillos T2 por placa
        const val INSULATION_ROLL_AREA_M2: Double = 12.0     // Rollo de aislamiento de 12 m²
        const val ANCHOR_SPACING_METERS: Double = 0.50       // Anclajes a piso/losa cada 50 cm
    }

    /**
     * Calcula la lista completa de insumos requeridos para la construcción del muro.
     *
     * @param input Parámetros del muro (dimensiones, caras, aislamiento, tipo de placa y desperdicio).
     * @return [WallCalculationResult] con la lista completa de materiales desglosados y explicados.
     */
    fun calculate(input: WallCalculationInput): WallCalculationResult {
        val wasteMultiplier = 1.0 + input.wasteFactor

        // Defensa en profundidad: un vano imposible nunca debe producir cantidades 0 silenciosas.
        if (input.openings.isNotEmpty()) {
            if (input.openings.any { it.widthMeters <= 0.0 || it.heightMeters <= 0.0 }) {
                throw IllegalArgumentException("Cada vano debe tener ancho y alto mayores a 0 m.")
            }
            if (input.openingsAreaOneFace >= input.wallAreaOneFace) {
                throw IllegalArgumentException(
                    "Los vanos suman ${NumberFormatUtils.formatSquareMeters(input.openingsAreaOneFace)} " +
                        "sobre un muro de ${NumberFormatUtils.formatSquareMeters(input.wallAreaOneFace)}: " +
                        "el muro quedaría sin superficie emplazable."
                )
            }
        }
        val requirements = mutableListOf<MaterialRequirement>()

        val length = input.lengthMeters
        val height = input.heightMeters
        val studSpacing = input.studSpacingMeters
        val netDrywallArea = input.netTotalDrywallArea

        // =========================================================================
        // 1. CANAL / GUÍA SUPERIOR E INFERIOR (Piso y Techo)
        // =========================================================================
        val profileLen = input.commercialProfileLengthMeters
        val headersLinearMetersRaw = input.headersLinearMeters
        val trackLinearMetersRaw = length * 2.0 + headersLinearMetersRaw
        val trackLinearMetersWithWaste = trackLinearMetersRaw * wasteMultiplier
        val trackPieces = ceil(trackLinearMetersWithWaste / profileLen)
            .toInt().coerceAtLeast(1)

        val profileUnit = if (profileLen <= 2.50) UnitOfMeasure.PIEZAS_2_44M else UnitOfMeasure.PIEZAS_3M

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.getTrackMaterial(profileLen),
                rawQuantity = trackLinearMetersRaw / profileLen,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = trackLinearMetersWithWaste / profileLen,
                commercialQuantity = trackPieces,
                unit = profileUnit,
                formulaExplanation = buildString {
                    append("Canal piso + techo: ${(length * 2.0).format(2)} m")
                    if (input.openings.isNotEmpty()) {
                        append(" + dinteles de vano: ${headersLinearMetersRaw.format(2)} m (1 canal por vano)")
                    }
                    append(" = ${trackLinearMetersRaw.format(2)} m lineales base + ${input.wastePercentageString()} desperdicio")
                    append(" = ${trackLinearMetersWithWaste.format(2)} m. Comerciales: $trackPieces piezas de ${profileLen.format(2)}m.")
                }
            )
        )

        // =========================================================================
        // 2. POSTES / PARANTES VERTICALES (Separación típica 0.407 m o 0.61 m)
        // =========================================================================
        val modulationStudLinesCount = ceil(length / studSpacing).toInt() + 1
        val studLinesCount = modulationStudLinesCount + input.jambStudsCount
        val studLinearMetersRaw = studLinesCount * height
        val studLinearMetersWithWaste = studLinearMetersRaw * wasteMultiplier

        val studPieces = if (height <= profileLen) {
            ceil(studLinesCount * wasteMultiplier).toInt().coerceAtLeast(studLinesCount)
        } else {
            ceil(studLinearMetersWithWaste / profileLen).toInt()
        }

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.getStudMaterial(profileLen),
                rawQuantity = studLinesCount.toDouble(),
                wastePercentage = input.wasteFactor,
                quantityWithWaste = studLinesCount * wasteMultiplier,
                commercialQuantity = studPieces,
                unit = profileUnit,
                formulaExplanation = buildString {
                    append("$studLinesCount líneas verticales a cada ${(studSpacing * 100).roundToInt()} cm x ${height.format(2)} m de alto")
                    if (input.openings.isNotEmpty()) {
                        append(" ($modulationStudLinesCount por modulación + ${input.jambStudsCount} postes de jamba en ${input.openings.size} vano/s)")
                    }
                    append(". Con ${input.wastePercentageString()} desperdicio = $studPieces piezas de ${profileLen.format(2)}m.")
                }
            )
        )

        // =========================================================================
        // 3. PERFIL OMEGA (Rigidizador para muros de gran altura > 2.6 m)
        // =========================================================================
        if (height > 2.60) {
            val omegaLinearMetersRaw = length
            val omegaLinearMetersWithWaste = omegaLinearMetersRaw * wasteMultiplier
            val omegaPieces = ceil(omegaLinearMetersWithWaste / profileLen)
                .toInt().coerceAtLeast(1)

            requirements.add(
                MaterialRequirement(
                    material = DrywallCatalog.getOmegaMaterial(profileLen),
                    rawQuantity = omegaLinearMetersRaw / profileLen,
                    wastePercentage = input.wasteFactor,
                    quantityWithWaste = omegaLinearMetersWithWaste / profileLen,
                    commercialQuantity = omegaPieces,
                    unit = profileUnit,
                    formulaExplanation = "Rigidizador intermedio por altura > 2.60m (${length.format(2)} m lineales + ${input.wastePercentageString()} desperdicio) = $omegaPieces piezas de ${profileLen.format(2)}m."
                )
            )
        }

        // =========================================================================
        // 4. PLACAS DE YESO Y FIBROCEMENTO (1.22 x 2.44 m)
        // =========================================================================
        val areaFaceA = input.netAreaOneFace
        val areaFaceB = if (input.faces == WallFaces.BOTH_FACES) input.netAreaOneFace else 0.0

        val boardA = input.boardTypeFaceA
        val boardB = if (input.faces == WallFaces.BOTH_FACES) input.boardTypeFaceB else input.boardTypeFaceA

        val netAreasByType = mutableMapOf<DrywallBoardType, Double>()
        netAreasByType[boardA] = (netAreasByType[boardA] ?: 0.0) + areaFaceA
        netAreasByType[boardB] = (netAreasByType[boardB] ?: 0.0) + areaFaceB

        netAreasByType.forEach { (boardType, area) ->
            if (area > 0) {
                val boardMaterial = DrywallCatalog.getBoardMaterial(boardType)
                val rawCount = area / BOARD_SURFACE_AREA_M2
                val withWaste = rawCount * wasteMultiplier
                val commercialCount = ceil(withWaste).toInt().coerceAtLeast(1)
                requirements.add(
                    MaterialRequirement(
                        material = boardMaterial,
                        rawQuantity = rawCount,
                        wastePercentage = input.wasteFactor,
                        quantityWithWaste = withWaste,
                        commercialQuantity = commercialCount,
                        unit = UnitOfMeasure.PLACAS_ESTANDAR,
                        formulaExplanation = "Área neta (${boardType.displayName}): ${area.format(2)} m² / ${BOARD_SURFACE_AREA_M2.format(2)} m² = ${rawCount.format(2)} + ${input.wastePercentageString()} desperdicio = $commercialCount placas."
                    )
                )
            }
        }

        // =========================================================================
        // 5. TORNILLOS T1 (Metal-Metal para fijar postes a canales)
        // =========================================================================
        // 2 fijaciones en el canal superior + 2 fijaciones en el canal inferior = 4 tornillos por montante
        val t1RawCount = studLinesCount * 4.0
        val t1WithWaste = t1RawCount * wasteMultiplier
        val commercialT1Count = ceil(t1WithWaste).toInt()

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.TORNILLO_T1,
                rawQuantity = t1RawCount,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = t1WithWaste,
                commercialQuantity = commercialT1Count,
                unit = UnitOfMeasure.UNIDADES,
                formulaExplanation = "4 fijaciones por poste vertical ($studLinesCount postes) = ${t1RawCount.toInt()} tornillos + ${input.wastePercentageString()} desperdicio = $commercialT1Count unidades."
            )
        )

        // =========================================================================
        // 6. TORNILLOS T2 Y TORNILLOS PARA FIBROCEMENTO
        // =========================================================================
        val hasFiberCement = boardA.isFiberCement || boardB.isFiberCement
        val totalBoardsForScrews = requirements.filter { it.material.category == MaterialCategory.PLACAS }.sumOf { it.commercialQuantity }

        if (hasFiberCement) {
            val fcBoards = requirements.filter { it.material.id.startsWith("placa_fc") }.sumOf { it.commercialQuantity }
            val stBoards = totalBoardsForScrews - fcBoards

            if (stBoards > 0) {
                val t2Raw = stBoards * 30.0
                val t2WithW = t2Raw * wasteMultiplier
                val t2Comm = ceil(t2WithW).toInt().coerceAtLeast(1)
                requirements.add(
                    MaterialRequirement(
                        material = DrywallCatalog.TORNILLO_T2,
                        rawQuantity = t2Raw,
                        wastePercentage = input.wasteFactor,
                        quantityWithWaste = t2WithW,
                        commercialQuantity = t2Comm,
                        unit = UnitOfMeasure.UNIDADES,
                        formulaExplanation = "$stBoards placas de yeso x 30 tornillos/placa = ${t2Raw.toInt()} tornillos + ${input.wastePercentageString()} desperdicio = $t2Comm unidades."
                    )
                )
            }
            if (fcBoards > 0) {
                val fcRaw = fcBoards * 35.0
                val fcWithW = fcRaw * wasteMultiplier
                val fcComm = ceil(fcWithW).toInt().coerceAtLeast(1)
                requirements.add(
                    MaterialRequirement(
                        material = DrywallCatalog.TORNILLO_FIBROCEMENTO,
                        rawQuantity = fcRaw,
                        wastePercentage = input.wasteFactor,
                        quantityWithWaste = fcWithW,
                        commercialQuantity = fcComm,
                        unit = UnitOfMeasure.UNIDADES,
                        formulaExplanation = "$fcBoards placas de fibrocemento x 35 tornillos con aletas/placa = ${fcRaw.toInt()} tornillos + ${input.wastePercentageString()} desperdicio = $fcComm unidades."
                    )
                )
            }
        } else {
            val t2Raw = totalBoardsForScrews * 30.0
            val t2WithW = t2Raw * wasteMultiplier
            val t2Comm = ceil(t2WithW).toInt().coerceAtLeast(1)
            requirements.add(
                MaterialRequirement(
                    material = DrywallCatalog.TORNILLO_T2,
                    rawQuantity = t2Raw,
                    wastePercentage = input.wasteFactor,
                    quantityWithWaste = t2WithW,
                    commercialQuantity = t2Comm,
                    unit = UnitOfMeasure.UNIDADES,
                    formulaExplanation = "$totalBoardsForScrews placas x 30 tornillos/placa = ${t2Raw.toInt()} tornillos + ${input.wastePercentageString()} desperdicio = $t2Comm unidades."
                )
            )
        }

        // =========================================================================
        // 7. ANCLAJES (Chazos con tornillo para canales de piso y techo)
        // =========================================================================
        // Fijación a la estructura soporte cada 0.50 metros lineales de solera
        // Solo las soleras superior e inferior se anclan a piso y losa; los dinteles no llevan anclaje.
        val anchorSolerasLinearMeters = length * 2.0
        val anchorPointsRaw = (anchorSolerasLinearMeters / ANCHOR_SPACING_METERS) + 2.0 // +2 anclajes de borde
        val anchorPointsWithWaste = anchorPointsRaw * wasteMultiplier
        val commercialAnchors = ceil(anchorPointsWithWaste).toInt()

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.ANCLAJE_CHAZO_TORNILLO,
                rawQuantity = anchorPointsRaw,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = anchorPointsWithWaste,
                commercialQuantity = commercialAnchors,
                unit = UnitOfMeasure.UNIDADES,
                formulaExplanation = "Anclajes cada 50 cm sobre ${anchorSolerasLinearMeters.format(2)} m de soleras (largo x 2) = ${anchorPointsRaw.format(1)} puntos + ${input.wastePercentageString()} desperdicio = $commercialAnchors unidades."
            )
        )

        // =========================================================================
        // 8. CINTA DE PAPEL PARA JUNTAS (Rollos de 75 m)
        // =========================================================================
        // Promedio de 1.5 metros lineales de cinta por m² de placa
        val tapeBoardLinearMetersRaw = netDrywallArea * TAPE_METERS_PER_M2_BOARD
        // El vano atraviesa el muro: su perímetro se cinta en cada cara emplazada.
        val tapeOpeningsLinearMetersRaw = input.openingsPerimeterMeters * input.faces.factor
        val tapeLinearMetersRaw = tapeBoardLinearMetersRaw + tapeOpeningsLinearMetersRaw
        val tapeLinearMetersWithWaste = tapeLinearMetersRaw * wasteMultiplier
        val commercialTapeRolls = ceil(tapeLinearMetersWithWaste / TAPE_ROLL_COMMERCIAL_METERS)
            .toInt().coerceAtLeast(1)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.CINTA_PAPEL_JUNTAS,
                rawQuantity = tapeLinearMetersRaw / TAPE_ROLL_COMMERCIAL_METERS,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = tapeLinearMetersWithWaste / TAPE_ROLL_COMMERCIAL_METERS,
                commercialQuantity = commercialTapeRolls,
                unit = UnitOfMeasure.ROLLOS,
                formulaExplanation = buildString {
                    append("${netDrywallArea.format(2)} m² netos de placa x 1.5 m/m² = ${tapeBoardLinearMetersRaw.format(1)} m")
                    if (input.openings.isNotEmpty()) {
                        append(" + cinta de perímetro de vanos: ${tapeOpeningsLinearMetersRaw.format(1)} m")
                        append(" (2 x (ancho + alto) x ${input.faces.factor} cara(s) en ${input.openings.size} vano/s)")
                    }
                    append(" = ${tapeLinearMetersRaw.format(1)} m lineales + ${input.wastePercentageString()} desperdicio")
                    append(" = ${tapeLinearMetersWithWaste.format(1)} m ($commercialTapeRolls rollo/s de 75m).")
                }
            )
        )

        // =========================================================================
        // 9. MASILLA PARA JUNTAS (Baldes de 28 kg)
        // =========================================================================
        // Rendimiento técnico: 1.0 kg de masilla lista por m² de placa instalada (3 manos)
        val compoundKgRaw = netDrywallArea * COMPOUND_KG_PER_M2_BOARD
        val compoundKgWithWaste = compoundKgRaw * wasteMultiplier
        val commercialCompoundBuckets = ceil(compoundKgWithWaste / COMPOUND_BUCKET_KG)
            .toInt().coerceAtLeast(1)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.MASILLA_JUNTAS_BALDE,
                rawQuantity = compoundKgRaw / COMPOUND_BUCKET_KG,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = compoundKgWithWaste / COMPOUND_BUCKET_KG,
                commercialQuantity = commercialCompoundBuckets,
                unit = UnitOfMeasure.BALDES_28KG,
                formulaExplanation = "${netDrywallArea.format(2)} m² netos x 1.0 kg/m² = ${compoundKgRaw.format(1)} kg base + ${input.wastePercentageString()} desperdicio = ${compoundKgWithWaste.format(1)} kg ($commercialCompoundBuckets balde/s de 28kg)."
            )
        )

        // =========================================================================
        // 10. AISLAMIENTO TERMOACÚSTICO (Opcional: Si hasInsulation == true)
        // =========================================================================
        if (input.hasInsulation) {
            val insulationAreaRaw = input.netAreaOneFace
            val insulationAreaWithWaste = insulationAreaRaw * wasteMultiplier
            val commercialInsulationRolls = ceil(insulationAreaWithWaste / INSULATION_ROLL_AREA_M2)
                .toInt().coerceAtLeast(1)

            requirements.add(
                MaterialRequirement(
                    material = DrywallCatalog.LANA_AISLAMIENTO,
                    rawQuantity = insulationAreaRaw / INSULATION_ROLL_AREA_M2,
                    wastePercentage = input.wasteFactor,
                    quantityWithWaste = insulationAreaWithWaste / INSULATION_ROLL_AREA_M2,
                    commercialQuantity = commercialInsulationRolls,
                    unit = UnitOfMeasure.ROLLOS,
                    formulaExplanation = "Área neta de tabique: ${insulationAreaRaw.format(2)} m² + ${input.wastePercentageString()} desperdicio = ${insulationAreaWithWaste.format(2)} m² ($commercialInsulationRolls rollo/s de 12 m²)."
                )
            )
        }

        return WallCalculationResult(
            input = input,
            requirements = requirements
        )
    }

    /**
     * Sobrecarga práctica para invocar la calculadora directamente con valores primitivos.
     */
    fun calculate(
        lengthMeters: Double,
        heightMeters: Double,
        isTwoFaces: Boolean = true,
        hasInsulation: Boolean = false,
        wasteFactor: Double = 0.08,
        studSpacingMeters: Double = 0.407,
        boardType: DrywallBoardType = DrywallBoardType.ESTANDAR_ST,
        openings: List<WallOpening> = emptyList()
    ): WallCalculationResult {
        val faces = if (isTwoFaces) WallFaces.BOTH_FACES else WallFaces.ONE_FACE
        val input = WallCalculationInput(
            lengthMeters = lengthMeters,
            heightMeters = heightMeters,
            faces = faces,
            hasInsulation = hasInsulation,
            studSpacingMeters = studSpacingMeters,
            boardType = boardType,
            wasteFactor = wasteFactor,
            openings = openings
        )
        return calculate(input)
    }

    /**
     * Calcula la lista completa de insumos para un cielo raso suspendido en Drywall.
     */
    fun calculateDrywallCeiling(input: DrywallCeilingInput): List<MaterialRequirement> {
        val wasteMultiplier = 1.0 + input.wasteFactor
        val requirements = mutableListOf<MaterialRequirement>()

        val length = input.lengthMeters
        val width = input.widthMeters
        val area = input.areaM2
        val perimeter = input.perimeterMeters

        val profileLen = 2.44
        val profileUnit = UnitOfMeasure.PIEZAS_2_44M

        // 1. Ángulo L Perimetral
        val angleLinearMetersWithWaste = perimeter * wasteMultiplier
        val anglePieces = ceil(angleLinearMetersWithWaste / profileLen)
            .toInt().coerceAtLeast(1)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.getAngleMaterial(profileLen),
                rawQuantity = perimeter / profileLen,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = angleLinearMetersWithWaste / profileLen,
                commercialQuantity = anglePieces,
                unit = profileUnit,
                formulaExplanation = "Perímetro perimetral: ${perimeter.format(2)} m + ${(input.wasteFactor * 100).roundToInt()}% desperdicio = $anglePieces piezas de ${profileLen.format(2)} m."
            )
        )

        // 2. Omegas Secundarias
        val omegaLinesCount = ceil(length / input.omegaSpacingMeters).toInt() + 1
        val omegaLinearMetersRaw = omegaLinesCount * width
        val omegaLinearMetersWithWaste = omegaLinearMetersRaw * wasteMultiplier
        val omegaPieces = ceil(omegaLinearMetersWithWaste / profileLen)
            .toInt().coerceAtLeast(1)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.getOmegaMaterial(profileLen),
                rawQuantity = omegaLinearMetersRaw / profileLen,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = omegaLinearMetersWithWaste / profileLen,
                commercialQuantity = omegaPieces,
                unit = profileUnit,
                formulaExplanation = "$omegaLinesCount líneas de omega cada ${(input.omegaSpacingMeters * 100).roundToInt()} cm x ${width.format(2)} m = ${omegaLinearMetersRaw.format(2)} m + ${(input.wasteFactor * 100).roundToInt()}% desperdicio = $omegaPieces piezas de ${profileLen.format(2)} m."
            )
        )

        // 3. Vigas Principales (Canal Guía)
        val mainBeamLinesCount = ceil(width / input.mainBeamSpacingMeters).toInt() + 1
        val mainBeamLinearMetersRaw = mainBeamLinesCount * length
        val mainBeamLinearMetersWithWaste = mainBeamLinearMetersRaw * wasteMultiplier
        val mainBeamPieces = ceil(mainBeamLinearMetersWithWaste / profileLen)
            .toInt().coerceAtLeast(1)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.getTrackMaterial(profileLen),
                rawQuantity = mainBeamLinearMetersRaw / profileLen,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = mainBeamLinearMetersWithWaste / profileLen,
                commercialQuantity = mainBeamPieces,
                unit = profileUnit,
                formulaExplanation = "$mainBeamLinesCount vigas principales cada ${(input.mainBeamSpacingMeters * 100).roundToInt()} cm x ${length.format(2)} m = ${mainBeamLinearMetersRaw.format(2)} m + ${(input.wasteFactor * 100).roundToInt()}% desperdicio = $mainBeamPieces piezas de ${profileLen.format(2)} m."
            )
        )

        // 4. Suspensión / Alambre Galvanizado N° 12
        val suspensionPoints = omegaLinesCount * mainBeamLinesCount
        val wireMetersPerPoint = input.suspensionHeightMeters + 0.20
        val wireMetersTotal = suspensionPoints * wireMetersPerPoint
        val wireKgTotal = ceil((wireMetersTotal / 15.0) * wasteMultiplier).toInt().coerceAtLeast(1)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.ALAMBRE_GALVANIZADO,
                rawQuantity = wireMetersTotal / 15.0,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = (wireMetersTotal * wasteMultiplier) / 15.0,
                commercialQuantity = wireKgTotal,
                unit = UnitOfMeasure.KILOGRAMOS,
                formulaExplanation = "$suspensionPoints puntos de suspensión x ${wireMetersPerPoint.format(2)} m = ${wireMetersTotal.format(1)} m de alambre N° 12 (~15m/kg) = $wireKgTotal kg."
            )
        )

        // 5. Placas de Yeso
        val boardMaterial = DrywallCatalog.getBoardMaterial(input.boardType)
        val boardsRawCount = area / BOARD_SURFACE_AREA_M2
        val boardsWithWaste = boardsRawCount * wasteMultiplier
        val commercialBoardsCount = ceil(boardsWithWaste).toInt().coerceAtLeast(1)

        requirements.add(
            MaterialRequirement(
                material = boardMaterial,
                rawQuantity = boardsRawCount,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = boardsWithWaste,
                commercialQuantity = commercialBoardsCount,
                unit = UnitOfMeasure.PLACAS_ESTANDAR,
                formulaExplanation = "Área de techo: ${area.format(2)} m² / ${BOARD_SURFACE_AREA_M2.format(2)} m² = ${boardsRawCount.format(2)} + ${(input.wasteFactor * 100).roundToInt()}% desperdicio = $commercialBoardsCount placas."
            )
        )

        // 6. Tornillos T1 y T2
        val t1RawCount = suspensionPoints * 2.0 + (omegaLinesCount * 2.0)
        val t1Commercial = ceil(t1RawCount * wasteMultiplier).toInt().coerceAtLeast(40)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.TORNILLO_T1,
                rawQuantity = t1RawCount,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = t1RawCount * wasteMultiplier,
                commercialQuantity = t1Commercial,
                unit = UnitOfMeasure.UNIDADES,
                formulaExplanation = "Fijación entre perfiles y elementos de suspensión = $t1Commercial unidades."
            )
        )

        val t2RawCount = commercialBoardsCount * SCREWS_T2_PER_BOARD.toDouble()
        val t2Commercial = ceil(t2RawCount * wasteMultiplier).toInt().coerceAtLeast(50)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.TORNILLO_T2,
                rawQuantity = t2RawCount,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = t2RawCount * wasteMultiplier,
                commercialQuantity = t2Commercial,
                unit = UnitOfMeasure.UNIDADES,
                formulaExplanation = "$commercialBoardsCount placas x $SCREWS_T2_PER_BOARD tornillos/placa = $t2Commercial unidades."
            )
        )

        // 7. Anclajes / Chazos
        val anchorsRaw = ceil(perimeter / ANCHOR_SPACING_METERS).toInt() + suspensionPoints
        val anchorsCommercial = ceil(anchorsRaw * wasteMultiplier).toInt().coerceAtLeast(10)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.ANCLAJE_CHAZO_TORNILLO,
                rawQuantity = anchorsRaw.toDouble(),
                wastePercentage = input.wasteFactor,
                quantityWithWaste = anchorsRaw * wasteMultiplier,
                commercialQuantity = anchorsCommercial,
                unit = UnitOfMeasure.UNIDADES,
                formulaExplanation = "Anclajes perimetrales + puntos de suspensión en techo = $anchorsCommercial unidades."
            )
        )

        // 8. Cinta de Papel
        val tapeMetersRaw = area * TAPE_METERS_PER_M2_BOARD
        val tapeRolls = ceil((tapeMetersRaw * wasteMultiplier) / TAPE_ROLL_COMMERCIAL_METERS)
            .toInt().coerceAtLeast(1)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.CINTA_PAPEL_JUNTAS,
                rawQuantity = tapeMetersRaw / TAPE_ROLL_COMMERCIAL_METERS,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = (tapeMetersRaw * wasteMultiplier) / TAPE_ROLL_COMMERCIAL_METERS,
                commercialQuantity = tapeRolls,
                unit = UnitOfMeasure.ROLLOS,
                formulaExplanation = "${area.format(2)} m² x 1.5 m/m² = ${tapeMetersRaw.format(1)} m lineales ($tapeRolls rollo/s de 75m)."
            )
        )

        // 9. Masilla
        val compoundKgRaw = area * COMPOUND_KG_PER_M2_BOARD
        val compoundBuckets = ceil((compoundKgRaw * wasteMultiplier) / COMPOUND_BUCKET_KG)
            .toInt().coerceAtLeast(1)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.MASILLA_JUNTAS_BALDE,
                rawQuantity = compoundKgRaw / COMPOUND_BUCKET_KG,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = (compoundKgRaw * wasteMultiplier) / COMPOUND_BUCKET_KG,
                commercialQuantity = compoundBuckets,
                unit = UnitOfMeasure.BALDES_28KG,
                formulaExplanation = "${area.format(2)} m² x 1.0 kg/m² = ${compoundKgRaw.format(1)} kg ($compoundBuckets balde/s de 28kg)."
            )
        )

        // 10. Aislamiento termoacústico opcional
        if (input.hasInsulation) {
            val insulationRolls = ceil((area * wasteMultiplier) / INSULATION_ROLL_AREA_M2)
                .toInt().coerceAtLeast(1)

            requirements.add(
                MaterialRequirement(
                    material = DrywallCatalog.LANA_AISLAMIENTO,
                    rawQuantity = area / INSULATION_ROLL_AREA_M2,
                    wastePercentage = input.wasteFactor,
                    quantityWithWaste = (area * wasteMultiplier) / INSULATION_ROLL_AREA_M2,
                    commercialQuantity = insulationRolls,
                    unit = UnitOfMeasure.ROLLOS,
                    formulaExplanation = "Área de techo: ${area.format(2)} m² ($insulationRolls rollo/s de 12 m²)."
                )
            )
        }

        return requirements
    }

    /**
     * Calcula la lista completa de insumos para un cielo raso en PVC.
     */
    fun calculatePvcCeiling(input: PvcCeilingInput): List<MaterialRequirement> {
        val wasteMultiplier = 1.0 + input.wasteFactor
        val requirements = mutableListOf<MaterialRequirement>()

        val area = input.areaM2
        val perimeter = input.perimeterMeters

        // 1. Tablillas PVC Machihembradas (0.25 x 5.95 m)
        val slatArea = input.slatWidthMeters * input.commercialSlatLengthMeters
        val slatsRawCount = area / slatArea
        val slatsCommercialCount = ceil(slatsRawCount * wasteMultiplier).toInt().coerceAtLeast(1)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.TABLILLA_PVC_5_95M,
                rawQuantity = slatsRawCount,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = slatsRawCount * wasteMultiplier,
                commercialQuantity = slatsCommercialCount,
                unit = UnitOfMeasure.PIEZAS_5_95M,
                formulaExplanation = "Área de techo: ${area.format(2)} m² / área tablilla (${slatArea.format(2)} m²) = ${slatsRawCount.format(2)} + ${(input.wasteFactor * 100).roundToInt()}% desperdicio = $slatsCommercialCount piezas de 5.95 m."
            )
        )

        // 2. Cornisa / Moldura PVC (3.0 m)
        val corniceLinearMetersWithWaste = perimeter * wasteMultiplier
        val cornicePieces3m = ceil(corniceLinearMetersWithWaste / COMMERCIAL_PROFILE_LENGTH_METERS)
            .toInt().coerceAtLeast(1)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.CORNISA_PVC_3M,
                rawQuantity = perimeter / COMMERCIAL_PROFILE_LENGTH_METERS,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = corniceLinearMetersWithWaste / COMMERCIAL_PROFILE_LENGTH_METERS,
                commercialQuantity = cornicePieces3m,
                unit = UnitOfMeasure.PIEZAS_3M,
                formulaExplanation = "Perímetro del ambiente: ${perimeter.format(2)} m + ${(input.wasteFactor * 100).roundToInt()}% desperdicio = $cornicePieces3m piezas de 3.0 m."
            )
        )

        // 3. Estructura Portante Omega (2.44 m)
        val profileLen = 2.44
        val supportLinesCount = ceil(input.lengthMeters / input.supportSpacingMeters).toInt() + 1
        val supportLinearMetersRaw = supportLinesCount * input.widthMeters
        val supportLinearMetersWithWaste = supportLinearMetersRaw * wasteMultiplier
        val supportPieces = ceil(supportLinearMetersWithWaste / profileLen)
            .toInt().coerceAtLeast(1)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.getOmegaMaterial(profileLen),
                rawQuantity = supportLinearMetersRaw / profileLen,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = supportLinearMetersWithWaste / profileLen,
                commercialQuantity = supportPieces,
                unit = UnitOfMeasure.PIEZAS_2_44M,
                formulaExplanation = "$supportLinesCount líneas de estructura portante cada ${(input.supportSpacingMeters * 100).roundToInt()} cm x ${input.widthMeters.format(2)} m = ${supportLinearMetersRaw.format(2)} m + ${(input.wasteFactor * 100).roundToInt()}% desperdicio = $supportPieces piezas de ${profileLen.format(2)} m."
            )
        )

        // 4. Tornillos Sombrerillo para PVC
        val screwsRawCount = area * 15.0
        val screwsCommercialCount = ceil(screwsRawCount * wasteMultiplier).toInt().coerceAtLeast(50)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.TORNILLO_SOMBRERILLO_PVC,
                rawQuantity = screwsRawCount,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = screwsRawCount * wasteMultiplier,
                commercialQuantity = screwsCommercialCount,
                unit = UnitOfMeasure.UNIDADES,
                formulaExplanation = "${area.format(2)} m² x 15 tornillos/m² = ${screwsRawCount.roundToInt()} unidades + ${(input.wasteFactor * 100).roundToInt()}% desperdicio = $screwsCommercialCount unidades."
            )
        )

        // 5. Anclajes / Chazos
        val anchorsRaw = ceil(perimeter / ANCHOR_SPACING_METERS).toInt()
        val anchorsCommercial = ceil(anchorsRaw * wasteMultiplier).toInt().coerceAtLeast(10)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.ANCLAJE_CHAZO_TORNILLO,
                rawQuantity = anchorsRaw.toDouble(),
                wastePercentage = input.wasteFactor,
                quantityWithWaste = anchorsRaw * wasteMultiplier,
                commercialQuantity = anchorsCommercial,
                unit = UnitOfMeasure.UNIDADES,
                formulaExplanation = "Anclajes perimetrales cada 50 cm = $anchorsCommercial unidades."
            )
        )

        return requirements
    }

    /**
     * Calcula la lista completa de insumos para un mueble / repisa en Drywall.
     */
    fun calculateFurniture(input: FurnitureCalculationInput): List<MaterialRequirement> {
        val wasteMultiplier = 1.0 + input.wasteFactor
        val requirements = mutableListOf<MaterialRequirement>()

        val exposedArea = input.totalExposedAreaM2
        val cornerBeadLinearMeters = input.totalCornerBeadLinearMeters

        // 1. Placas de Yeso
        val boardMaterial = DrywallCatalog.getBoardMaterial(input.boardType)
        val boardsRawCount = exposedArea / BOARD_SURFACE_AREA_M2
        val boardsWithWaste = boardsRawCount * wasteMultiplier
        val commercialBoardsCount = ceil(boardsWithWaste).toInt().coerceAtLeast(1)

        requirements.add(
            MaterialRequirement(
                material = boardMaterial,
                rawQuantity = boardsRawCount,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = boardsWithWaste,
                commercialQuantity = commercialBoardsCount,
                unit = UnitOfMeasure.PLACAS_ESTANDAR,
                formulaExplanation = "Superficie expuesta a revestir: ${exposedArea.format(2)} m² / ${BOARD_SURFACE_AREA_M2.format(2)} m² = ${boardsRawCount.format(2)} + ${(input.wasteFactor * 100).roundToInt()}% desperdicio = $commercialBoardsCount placas."
            )
        )

        // 2. Estructura Metálica (Canales y Postes de 2.44 m)
        val profileLen = 2.44
        val structuralLinearMetersRaw = cornerBeadLinearMeters * 0.8
        val structuralLinearMetersWithWaste = structuralLinearMetersRaw * wasteMultiplier

        val channelPieces = ceil(structuralLinearMetersWithWaste / profileLen)
            .toInt().coerceAtLeast(1)
        val studPieces = ceil(structuralLinearMetersWithWaste / profileLen)
            .toInt().coerceAtLeast(1)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.getTrackMaterial(profileLen),
                rawQuantity = structuralLinearMetersRaw / profileLen,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = structuralLinearMetersWithWaste / profileLen,
                commercialQuantity = channelPieces,
                unit = UnitOfMeasure.PIEZAS_2_44M,
                formulaExplanation = "Estructura base de amarre e interfaz de repisas = $channelPieces piezas de ${profileLen.format(2)} m."
            )
        )

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.getStudMaterial(profileLen),
                rawQuantity = structuralLinearMetersRaw / profileLen,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = structuralLinearMetersWithWaste / profileLen,
                commercialQuantity = studPieces,
                unit = UnitOfMeasure.PIEZAS_2_44M,
                formulaExplanation = "Refuerzos y montantes internos del mueble = $studPieces piezas de ${profileLen.format(2)} m."
            )
        )

        // 3. Esquinero Metálico Perforado (3.0 m)
        val cornerBeadPieces3m = ceil((cornerBeadLinearMeters * wasteMultiplier) / COMMERCIAL_PROFILE_LENGTH_METERS)
            .toInt().coerceAtLeast(1)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.ESQUINERO_PERFORADO_3M,
                rawQuantity = cornerBeadLinearMeters / COMMERCIAL_PROFILE_LENGTH_METERS,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = (cornerBeadLinearMeters * wasteMultiplier) / COMMERCIAL_PROFILE_LENGTH_METERS,
                commercialQuantity = cornerBeadPieces3m,
                unit = UnitOfMeasure.PIEZAS_3M,
                formulaExplanation = "Protección de cantos y esquinas expuestas: ${cornerBeadLinearMeters.format(2)} m lineales + ${(input.wasteFactor * 100).roundToInt()}% desperdicio = $cornerBeadPieces3m piezas de 3.0 m."
            )
        )

        // 4. Tornillos T1 y T2
        val t1Commercial = ceil((channelPieces + studPieces) * 8 * wasteMultiplier).toInt().coerceAtLeast(30)
        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.TORNILLO_T1,
                rawQuantity = (channelPieces + studPieces) * 8.0,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = (channelPieces + studPieces) * 8.0 * wasteMultiplier,
                commercialQuantity = t1Commercial,
                unit = UnitOfMeasure.UNIDADES,
                formulaExplanation = "Unión metal a metal para armazón interno = $t1Commercial unidades."
            )
        )

        val t2Commercial = ceil(commercialBoardsCount * 35 * wasteMultiplier).toInt().coerceAtLeast(50)
        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.TORNILLO_T2,
                rawQuantity = commercialBoardsCount * 35.0,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = commercialBoardsCount * 35.0 * wasteMultiplier,
                commercialQuantity = t2Commercial,
                unit = UnitOfMeasure.UNIDADES,
                formulaExplanation = "Fijación de placas a perfilería (35 tornillos/placa por cortes pequeños) = $t2Commercial unidades."
            )
        )

        // 5. Masilla y Cinta
        val compoundKgRaw = exposedArea * 1.5 // Mayor masillado para asentamiento de esquineros perforados
        val compoundBuckets = ceil((compoundKgRaw * wasteMultiplier) / COMPOUND_BUCKET_KG)
            .toInt().coerceAtLeast(1)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.MASILLA_JUNTAS_BALDE,
                rawQuantity = compoundKgRaw / COMPOUND_BUCKET_KG,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = (compoundKgRaw * wasteMultiplier) / COMPOUND_BUCKET_KG,
                commercialQuantity = compoundBuckets,
                unit = UnitOfMeasure.BALDES_28KG,
                formulaExplanation = "${exposedArea.format(2)} m² expuestos x 1.5 kg/m² = ${compoundKgRaw.format(1)} kg ($compoundBuckets balde/s de 28kg)."
            )
        )

        val tapeRolls = ceil(((exposedArea * 1.5) * wasteMultiplier) / TAPE_ROLL_COMMERCIAL_METERS)
            .toInt().coerceAtLeast(1)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.CINTA_PAPEL_JUNTAS,
                rawQuantity = (exposedArea * 1.5) / TAPE_ROLL_COMMERCIAL_METERS,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = ((exposedArea * 1.5) * wasteMultiplier) / TAPE_ROLL_COMMERCIAL_METERS,
                commercialQuantity = tapeRolls,
                unit = UnitOfMeasure.ROLLOS,
                formulaExplanation = "Tratamiento de juntas en uniones de placas = $tapeRolls rollo/s de 75m."
            )
        )

        return requirements
    }

    private fun WallCalculationInput.wastePercentageString(): String =
        "${(wasteFactor * 100).roundToInt()}%"

    private fun Double.format(decimals: Int): String = NumberFormatUtils.formatDecimal(this, decimals)
}

