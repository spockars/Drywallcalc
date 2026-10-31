package com.example.drywall

import com.example.drywall.domain.engine.CalculatorEngine
import com.example.drywall.domain.model.DrywallBoardType
import com.example.drywall.domain.model.WallCalculationInput
import com.example.drywall.domain.model.WallFaces
import com.example.drywall.util.DrywallReportExporter
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para la generación de reportes de materiales compatibles con WhatsApp.
 */
class DrywallReportExporterTest {

    private val engine = CalculatorEngine()

    @Test
    fun testGenerateWhatsAppReport_containsKeySectionsAndMarkdown() {
        val input = WallCalculationInput(
            lengthMeters = 5.0,
            heightMeters = 2.6,
            faces = WallFaces.BOTH_FACES,
            hasInsulation = true,
            boardType = DrywallBoardType.ESTANDAR_ST,
            wasteFactor = 0.08
        )
        val result = engine.calculate(input)

        val report = DrywallReportExporter.generateWhatsAppFormattedReport(
            result = result,
            projectName = "Edificio Los Álamos - Apto 501",
            clientOrLocation = "Arquitectura y Diseños S.A.S.",
            notes = "Entregar en obra el día lunes a primera hora"
        )

        assertNotNull(report)

        // Verificación de encabezados y metadatos
        assertTrue(report.contains("REPORTE DE MATERIALES - DRYWALLCALC"))
        assertTrue(report.contains("Edificio Los Álamos - Apto 501"))
        assertTrue(report.contains("Arquitectura y Diseños S.A.S."))

        // Verificación de especificaciones técnicas y métricas
        assertTrue(report.contains("5.00 m largo × 2.60 m alto"))
        assertTrue(report.contains("26.00 m²"))
        assertTrue(report.contains("*8%*"))

        // Verificación de insumos clave en formato WhatsApp (*negrita*, `código`)
        assertTrue(report.contains("`CANAL-60-"))
        assertTrue(report.contains("`POSTE-59-"))
        assertTrue(report.contains("`PLACA-ST-12.7`"))
        assertTrue(report.contains("`TORN-T1-8x1/2`"))
        assertTrue(report.contains("`TORN-T2-6x1`"))
        assertTrue(report.contains("`CINTA-PAPEL-75M`"))
        assertTrue(report.contains("`MASILLA-BALDE-28KG`"))
        assertTrue(report.contains("`LANA-VIDRIO-12M2`"))

        // Verificación de notas
        assertTrue(report.contains("Entregar en obra el día lunes a primera hora"))
    }
}
