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
        assertTrue(report.contains("PEDIDO DE MATERIALES - DRYWALLCALC"))
        assertTrue(report.contains("Edificio Los Álamos - Apto 501"))
        assertTrue(report.contains("Arquitectura y Diseños S.A.S."))

        // Verificación de especificaciones técnicas y métricas
        assertTrue(report.contains("5.00 m largo × 2.60 m alto"))
        assertTrue(report.contains("26.00 m²"))

        // Verificación de insumos clave en el reporte
        assertTrue(report.contains("Canal / Solera Guía"))
        assertTrue(report.contains("Poste / Parante / Montante"))
        assertTrue(report.contains("Placa de Yeso Estándar"))

        // Verificación de notas
        assertTrue(report.contains("Entregar en obra el día lunes a primera hora"))
    }
}
