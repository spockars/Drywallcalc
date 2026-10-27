package com.example.drywall.domain.engine

import com.example.drywall.domain.model.DrywallBoardType
import com.example.drywall.domain.model.DrywallMaterial
import com.example.drywall.domain.model.MaterialCategory
import com.example.drywall.domain.model.MaterialRequirement
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
    // Estructura metálica
    val CANAL_GUIA_3M = DrywallMaterial(
        id = "canal_guia_3m",
        code = "CANAL-60-3M",
        name = "Canal / Solera Guía (60 mm)",
        category = MaterialCategory.ESTRUCTURA,
        defaultUnit = UnitOfMeasure.PIEZAS_3M,
        commercialSpec = "Perfil de acero galvanizado cal. 26 x 3.00 m (base para piso y techo)",
        technicalFunction = "Guía horizontal anclada a piso y losa para recibir los montantes verticales"
    )

    val POSTE_MONTANTE_3M = DrywallMaterial(
        id = "poste_montante_3m",
        code = "POSTE-59-3M",
        name = "Poste / Parante / Montante (59 mm)",
        category = MaterialCategory.ESTRUCTURA,
        defaultUnit = UnitOfMeasure.PIEZAS_3M,
        commercialSpec = "Perfil de acero galvanizado cal. 26 x 3.00 m con perforaciones para ductos",
        technicalFunction = "Estructura vertical portante que recibe las placas de yeso fijadas con tornillos"
    )

    val PERFIL_OMEGA_3M = DrywallMaterial(
        id = "perfil_omega_3m",
        code = "OMEGA-3M",
        name = "Perfil Omega Rigidizador",
        category = MaterialCategory.ESTRUCTURA,
        defaultUnit = UnitOfMeasure.PIEZAS_3M,
        commercialSpec = "Perfil omega galvanizado cal. 26 x 3.00 m",
        technicalFunction = "Refuerzo y arriostramiento horizontal intermedio para muros altos (>2.6 m) o trasdosados"
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
    }

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
        // El muro requiere canal a lo largo en la base (piso) y en la coronación (techo).
        // Un canal por vano hace las veces de dintel sobre la abertura.
        val headersLinearMetersRaw = input.headersLinearMeters
        val trackLinearMetersRaw = length * 2.0 + headersLinearMetersRaw
        val trackLinearMetersWithWaste = trackLinearMetersRaw * wasteMultiplier
        val trackPieces3m = ceil(trackLinearMetersWithWaste / COMMERCIAL_PROFILE_LENGTH_METERS)
            .toInt().coerceAtLeast(1)

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.CANAL_GUIA_3M,
                rawQuantity = trackLinearMetersRaw / COMMERCIAL_PROFILE_LENGTH_METERS,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = trackLinearMetersWithWaste / COMMERCIAL_PROFILE_LENGTH_METERS,
                commercialQuantity = trackPieces3m,
                unit = UnitOfMeasure.PIEZAS_3M,
                formulaExplanation = buildString {
                    append("Canal piso + techo: ${(length * 2.0).format(2)} m")
                    if (input.openings.isNotEmpty()) {
                        append(" + dinteles de vano: ${headersLinearMetersRaw.format(2)} m (1 canal por vano)")
                    }
                    append(" = ${trackLinearMetersRaw.format(2)} m lineales base + ${input.wastePercentageString()} desperdicio")
                    append(" = ${trackLinearMetersWithWaste.format(2)} m. Comerciales: $trackPieces3m piezas de 3.0m.")
                }
            )
        )

        // =========================================================================
        // 2. POSTES / PARANTES VERTICALES (Separación típica 0.407 m o 0.61 m)
        // =========================================================================
        // Número de líneas verticales = división del largo entre modulación + 1 (arranque inicial)
        val modulationStudLinesCount = ceil(length / studSpacing).toInt() + 1
        // Cada vano exige dos postes de jamba, uno a cada lado de la abertura.
        val studLinesCount = modulationStudLinesCount + input.jambStudsCount
        val studLinearMetersRaw = studLinesCount * height
        val studLinearMetersWithWaste = studLinearMetersRaw * wasteMultiplier

        val studPieces3m = if (height <= COMMERCIAL_PROFILE_LENGTH_METERS) {
            // Cada línea de poste requiere 1 pieza completa de 3m (sin empalmes)
            ceil(studLinesCount * wasteMultiplier).toInt().coerceAtLeast(studLinesCount)
        } else {
            // Para muros de más de 3m, se empalman perfiles comerciales de 3m
            ceil(studLinearMetersWithWaste / COMMERCIAL_PROFILE_LENGTH_METERS).toInt()
        }

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.POSTE_MONTANTE_3M,
                rawQuantity = studLinesCount.toDouble(),
                wastePercentage = input.wasteFactor,
                quantityWithWaste = studLinesCount * wasteMultiplier,
                commercialQuantity = studPieces3m,
                unit = UnitOfMeasure.PIEZAS_3M,
                formulaExplanation = buildString {
                    append("$studLinesCount líneas verticales a cada ${(studSpacing * 100).roundToInt()} cm x ${height.format(2)} m de alto")
                    if (input.openings.isNotEmpty()) {
                        append(" ($modulationStudLinesCount por modulación + ${input.jambStudsCount} postes de jamba en ${input.openings.size} vano/s)")
                    }
                    append(". Con ${input.wastePercentageString()} desperdicio = $studPieces3m piezas de 3.0m.")
                }
            )
        )

        // =========================================================================
        // 3. PERFIL OMEGA (Rigidizador para muros de gran altura > 2.6 m)
        // =========================================================================
        if (height > 2.60) {
            val omegaLinearMetersRaw = length // Una fila horizontal intermedia de arriostramiento
            val omegaLinearMetersWithWaste = omegaLinearMetersRaw * wasteMultiplier
            val omegaPieces3m = ceil(omegaLinearMetersWithWaste / COMMERCIAL_PROFILE_LENGTH_METERS)
                .toInt().coerceAtLeast(1)

            requirements.add(
                MaterialRequirement(
                    material = DrywallCatalog.PERFIL_OMEGA_3M,
                    rawQuantity = omegaLinearMetersRaw / COMMERCIAL_PROFILE_LENGTH_METERS,
                    wastePercentage = input.wasteFactor,
                    quantityWithWaste = omegaLinearMetersWithWaste / COMMERCIAL_PROFILE_LENGTH_METERS,
                    commercialQuantity = omegaPieces3m,
                    unit = UnitOfMeasure.PIEZAS_3M,
                    formulaExplanation = "Rigidizador intermedio por altura > 2.60m (${length.format(2)} m lineales + ${input.wastePercentageString()} desperdicio) = $omegaPieces3m piezas de 3.0m."
                )
            )
        }

        // =========================================================================
        // 4. PLACAS DE YESO (Drywall Boards 1.22 x 2.44 m)
        // =========================================================================
        val boardMaterial = DrywallCatalog.getBoardMaterial(input.boardType)
        val boardsRawCount = netDrywallArea / BOARD_SURFACE_AREA_M2
        val boardsCountWithWaste = boardsRawCount * wasteMultiplier
        val commercialBoardsCount = ceil(boardsCountWithWaste).toInt().coerceAtLeast(1)

        requirements.add(
            MaterialRequirement(
                material = boardMaterial,
                rawQuantity = boardsRawCount,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = boardsCountWithWaste,
                commercialQuantity = commercialBoardsCount,
                unit = UnitOfMeasure.PLACAS_ESTANDAR,
                formulaExplanation = buildString {
                    append("Área neta a emplacar: ${netDrywallArea.format(2)} m² (${input.faces.label}")
                    if (input.openings.isNotEmpty()) {
                        append("; ${input.openings.size} vano/s descuentan ${input.openingsAreaOneFace.format(2)} m² por cara")
                    }
                    append("). Área por placa: ${BOARD_SURFACE_AREA_M2.format(2)} m².")
                    append(" Base: ${boardsRawCount.format(2)} + ${input.wastePercentageString()} desperdicio = $commercialBoardsCount placas.")
                }
            )
        )

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
        // 6. TORNILLOS T2 (Placa a Metal)
        // =========================================================================
        // Norma constructiva: tornillos cada 25-30 cm = promedio técnico 30 tornillos por placa
        val t2RawCount = commercialBoardsCount * SCREWS_T2_PER_BOARD.toDouble()
        val t2WithWaste = t2RawCount * wasteMultiplier
        val commercialT2Count = ceil(t2WithWaste).toInt()

        requirements.add(
            MaterialRequirement(
                material = DrywallCatalog.TORNILLO_T2,
                rawQuantity = t2RawCount,
                wastePercentage = input.wasteFactor,
                quantityWithWaste = t2WithWaste,
                commercialQuantity = commercialT2Count,
                unit = UnitOfMeasure.UNIDADES,
                formulaExplanation = "$commercialBoardsCount placas x $SCREWS_T2_PER_BOARD tornillos/placa = ${t2RawCount.toInt()} tornillos + ${input.wastePercentageString()} desperdicio = $commercialT2Count unidades."
            )
        )

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

    private fun WallCalculationInput.wastePercentageString(): String =
        "${(wasteFactor * 100).roundToInt()}%"

    private fun Double.format(decimals: Int): String = NumberFormatUtils.formatDecimal(this, decimals)
}
