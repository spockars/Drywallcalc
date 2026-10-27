package com.example.drywall.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.drywall.domain.model.PaintEstimate
import com.example.drywall.domain.model.PaintSpec
import com.example.drywall.domain.model.PaintUnit
import com.example.drywall.util.NumberFormatUtils

/**
 * Tarjeta de configuración de pintura del muro.
 *
 * El rendimiento del producto es editable: no se asume un valor de ficha técnica.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaintCard(
    enabled: Boolean,
    productName: String,
    unit: PaintUnit,
    coverageInput: String,
    finishCoatsInput: String,
    wastePercentInput: String,
    errors: List<String>,
    paintAreaM2: Double,
    modifier: Modifier = Modifier,
    onEnabledChange: (Boolean) -> Unit,
    onProductNameChange: (String) -> Unit,
    onUnitChange: (PaintUnit) -> Unit,
    onCoverageChange: (String) -> Unit,
    onFinishCoatsChange: (String) -> Unit,
    onWasteChange: (String) -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("paint_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Brush,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pintura del Muro",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Se calcula sobre el área neta de placa (${NumberFormatUtils.formatSquareMeters(paintAreaM2)}).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    modifier = Modifier.testTag("paint_switch")
                )
            }

            if (enabled) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                OutlinedTextField(
                    value = productName,
                    onValueChange = onProductNameChange,
                    label = { Text("Producto de pintura") },
                    placeholder = { Text("ej. Pintura acrílica tipo 1") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaintUnit.entries.forEach { candidate ->
                        FilterChip(
                            selected = unit == candidate,
                            onClick = { onUnitChange(candidate) },
                            label = { Text("m²/${candidate.label.lowercase()}") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = coverageInput,
                    onValueChange = onCoverageChange,
                    label = { Text("Rendimiento del producto") },
                    suffix = { Text("m²/${unit.label.lowercase()}") },
                    placeholder = { Text("según ficha técnica") },
                    singleLine = true,
                    isError = errors.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("paint_coverage_field")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = finishCoatsInput,
                        onValueChange = onFinishCoatsChange,
                        label = { Text("Manos de acabado") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = wastePercentInput,
                        onValueChange = onWasteChange,
                        label = { Text("Merma (%)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Consumo = área neta × manos ÷ rendimiento, más la merma, redondeado a envases enteros. " +
                        "Incluye ${PaintSpec.SEALER_COATS} mano de sellador y las manos de acabado indicadas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (errors.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        errors.forEach { message ->
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Resumen de pintura para la pantalla de resultados.
 */
@Composable
fun PaintSummaryCard(
    estimate: PaintEstimate,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("paint_summary_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Brush,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (estimate.productName.isBlank()) {
                        "Pintura del muro"
                    } else {
                        "Pintura: ${estimate.productName}"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PaintMetric(
                    label = "Área a pintar",
                    value = NumberFormatUtils.formatSquareMeters(estimate.areaM2)
                )
                PaintMetric(
                    label = "Sellador",
                    value = "${estimate.sealerUnits} ${unitLabels(estimate.sealerUnits, estimate)}"
                )
                PaintMetric(
                    label = "Acabado (${estimate.finishCoats} manos)",
                    value = "${estimate.finishUnits} ${unitLabels(estimate.finishUnits, estimate)}"
                )
            }

            Text(
                text = com.example.drywall.domain.engine.PaintCalculator.explain(estimate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PaintMetric(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun unitLabels(units: Int, estimate: PaintEstimate): String =
    if (units == 1) estimate.unit.label.lowercase() else estimate.unit.pluralLabel.lowercase()
