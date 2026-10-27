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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.drywall.domain.model.OpeningType
import com.example.drywall.ui.viewmodel.WallOpeningDraft
import com.example.drywall.util.NumberFormatUtils

/**
 * Tarjeta de edición de vanos (puertas y ventanas) del muro en cálculo.
 *
 * Permite agregar, editar y quitar vanos y muestra el impacto inmediato sobre la
 * superficie neta emplazable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpeningsCard(
    drafts: List<WallOpeningDraft>,
    errors: List<String>,
    openingsAreaM2: Double,
    netAreaM2: Double,
    onAddOpening: () -> Unit,
    onClearOpenings: () -> Unit,
    onRemoveOpening: (Long) -> Unit,
    onTypeChange: (Long, OpeningType) -> Unit,
    onWidthChange: (Long, String) -> Unit,
    onHeightChange: (Long, String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("openings_card"),
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
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Vanos (Puertas y Ventanas)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Cada vano descuenta su superficie del área a emplacar y del aislamiento, " +
                        "y suma refuerzo de jambas, dintel y cinta de perímetro.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (drafts.isEmpty()) {
                Text(
                    text = "Muro ciego: sin aberturas. Agrega un vano si el muro lleva puerta o ventana.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                drafts.forEachIndexed { index, draft ->
                    OpeningEditorRow(
                        position = index + 1,
                        draft = draft,
                        onTypeChange = { type -> onTypeChange(draft.id, type) },
                        onRemove = { onRemoveOpening(draft.id) },
                        onWidthChange = { value -> onWidthChange(draft.id, value) },
                        onHeightChange = { value -> onHeightChange(draft.id, value) }
                    )
                    if (index != drafts.lastIndex) {
                        HorizontalDivider()
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Superficie de vanos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = NumberFormatUtils.formatSquareMeters(openingsAreaM2),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Área neta del muro",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = NumberFormatUtils.formatSquareMeters(netAreaM2),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (netAreaM2 <= 0.0) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
            }

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onAddOpening,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_opening_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Agregar vano")
                }

                if (drafts.isNotEmpty()) {
                    TextButton(
                        onClick = onClearOpenings,
                        modifier = Modifier.testTag("clear_openings_button")
                    ) {
                        Text(text = "Quitar todos")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OpeningEditorRow(
    position: Int,
    draft: WallOpeningDraft,
    onTypeChange: (OpeningType) -> Unit,
    onRemove: () -> Unit,
    onWidthChange: (String) -> Unit,
    onHeightChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vano $position",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = onRemove,
                colors = IconButtonDefaults.filledTonalIconButtonColors(),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Quitar vano $position"
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OpeningType.entries.forEach { type ->
                FilterChip(
                    selected = draft.type == type,
                    onClick = { onTypeChange(type) },
                    label = { Text(text = type.label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = draft.widthInput,
                onValueChange = onWidthChange,
                label = { Text(text = "Ancho (m)") },
                placeholder = { Text(text = "ej. 0.90") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("opening_width_$position")
            )
            OutlinedTextField(
                value = draft.heightInput,
                onValueChange = onHeightChange,
                label = { Text(text = "Alto (m)") },
                placeholder = { Text(text = "ej. 2.10") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("opening_height_$position")
            )
        }
    }
}
