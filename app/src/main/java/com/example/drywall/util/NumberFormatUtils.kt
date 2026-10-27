package com.example.drywall.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Utilidades de formato numérico con locale explícito.
 *
 * Las magnitudes técnicas (metros, metros cuadrados y cantidades comerciales) se
 * formatean siempre con [Locale.US] para que los reportes y las explicaciones de
 * fórmula sean deterministas e independientes del idioma configurado en la máquina
 * donde se ejecutan (por ejemplo en los tests).
 *
 * Los importes monetarios sí se formatean con el locale colombiano, porque son
 * valores de negocio que el usuario final lee en pesos.
 */
object NumberFormatUtils {

    /** Locale fijo para magnitudes técnicas (separador decimal: punto). */
    val TECHNICAL_LOCALE: Locale = Locale.US

    /** Locale para importes en pesos colombianos (separador de miles: punto, sin decimales). */
    val CURRENCY_LOCALE: Locale = Locale.forLanguageTag("es-CO")

    /**
     * Formatea un número técnico con [decimals] cifras decimales usando [TECHNICAL_LOCALE].
     */
    fun formatDecimal(value: Double, decimals: Int = 2): String =
        String.format(TECHNICAL_LOCALE, "%.${decimals}f", value)

    /**
     * Formatea una longitud en metros, por ejemplo `4.00 m`.
     */
    fun formatMeters(value: Double, decimals: Int = 2): String =
        "${formatDecimal(value, decimals)} m"

    /**
     * Formatea una superficie en metros cuadrados, por ejemplo `17.02 m²`.
     */
    fun formatSquareMeters(value: Double, decimals: Int = 2): String =
        "${formatDecimal(value, decimals)} m²"

    /**
     * Formatea un importe en pesos colombianos, por ejemplo `$ 1.234.567`.
     */
    fun formatCurrencyCop(value: Double): String {
        val formatter = NumberFormat.getNumberInstance(CURRENCY_LOCALE)
        formatter.isGroupingUsed = true
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 0
        return "$ " + formatter.format(value)
    }
}
