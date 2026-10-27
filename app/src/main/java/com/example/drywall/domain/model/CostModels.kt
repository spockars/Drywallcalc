package com.example.drywall.domain.model

/**
 * Modo de cálculo de la mano de obra de una obra.
 */
enum class LaborMode(val label: String) {
    PER_M2("Por m²"),
    GLOBAL("Valor global")
}

/**
 * Rol del producto de pintura dentro del presupuesto.
 */
enum class PaintRole(val label: String) {
    SELLADOR("Sellador"),
    ACABADO("Acabado")
}

/**
 * Claves del catálogo de precios unitarios.
 *
 * Los materiales usan su `id` de dominio. La pintura se cotiza por rol **y por unidad**,
 * porque el mismo producto vale distinto por galón que por litro.
 */
object PriceKeys {

    const val PAINT_SEALER = "paint_sealer"
    const val PAINT_FINISH = "paint_finish"

    fun material(materialId: String): String = materialId

    fun paint(role: PaintRole, unit: PaintUnit): String {
        val base = if (role == PaintRole.SELLADOR) PAINT_SEALER else PAINT_FINISH
        return "$base:${unit.name}"
    }
}

/**
 * Precio unitario cotizado para un insumo.
 *
 * Un precio de 0 (o negativo) significa **sin cotizar**, nunca "gratis": el presupuesto
 * se marca como incompleto y no se presenta un total como si fuera real.
 */
data class PriceEntry(
    val itemKey: String,
    val unitPrice: Double = 0.0,
    val unitLabel: String = "",
    val updatedAt: Long = 0L
) {
    val isPriced: Boolean
        get() = unitPrice > 0.0
}

/**
 * Línea de costo: cantidad **comercial** (la redondeada del cómputo) por precio unitario.
 */
data class CostLine(
    val itemKey: String,
    val name: String,
    val unitLabel: String,
    val commercialQuantity: Int,
    val unitPrice: Double
) {
    val isPriced: Boolean
        get() = unitPrice > 0.0

    val subtotal: Double
        get() = commercialQuantity * unitPrice
}

/**
 * Costo de un muro.
 */
data class WallCost(
    val lines: List<CostLine>
) {
    val missingPrices: Int
        get() = lines.count { !it.isPriced }

    val materialsSubtotal: Double
        get() = lines.filter { it.isPriced }.sumOf { it.subtotal }

    val complete: Boolean
        get() = missingPrices == 0
}

/**
 * Mano de obra de una obra: opcional, por m² o por valor global.
 */
data class ProjectLabor(
    val enabled: Boolean = false,
    val mode: LaborMode = LaborMode.PER_M2,
    val amount: Double = 0.0
) {
    fun costFor(netAreaM2: Double): Double = when {
        !enabled || amount <= 0.0 -> 0.0
        mode == LaborMode.PER_M2 -> amount * netAreaM2
        else -> amount
    }

    val isConfigured: Boolean
        get() = enabled && amount > 0.0
}

/**
 * Fila del catálogo editable de precios.
 */
data class PriceCatalogItem(
    val itemKey: String,
    val name: String,
    val unitLabel: String,
    val categoryLabel: String
)

/**
 * Presupuesto: materiales más mano de obra.
 *
 * Si falta algún precio, [complete] es falso y la UI debe mostrar el faltante en lugar de un
 * total que parezca definitivo.
 */
data class Budget(
    val materialsSubtotal: Double,
    val labor: Double,
    val missingPrices: Int
) {
    val total: Double
        get() = materialsSubtotal + labor

    val complete: Boolean
        get() = missingPrices == 0
}
