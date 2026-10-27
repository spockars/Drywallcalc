package com.example.drywall.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.drywall.domain.model.DrywallBoardType
import com.example.drywall.domain.model.WallFaces
import com.example.drywall.ui.viewmodel.DrywallCalculatorViewModel

/**
 * Pantalla de Formulario de Cálculo de Muro Drywall con validación numérica rigurosa.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationFormScreen(
    viewModel: DrywallCalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Validación numérica en tiempo real
    val lengthValidation by remember(uiState.lengthInput) {
        derivedStateOf {
            val text = uiState.lengthInput.replace(',', '.').trim()
            val num = text.toDoubleOrNull()
            when {
                text.isEmpty() -> "El largo es obligatorio"
                num == null -> "Ingresa un número válido (ej. 4.5)"
                num <= 0.0 -> "El largo debe ser mayor a 0 m"
                num > 100.0 -> "Verifica el largo (máx. 100 m recomendado)"
                else -> null
            }
        }
    }

    val heightValidation by remember(uiState.heightInput) {
        derivedStateOf {
            val text = uiState.heightInput.replace(',', '.').trim()
            val num = text.toDoubleOrNull()
            when {
                text.isEmpty() -> "El alto es obligatorio"
                num == null -> "Ingresa un número válido (ej. 2.6)"
                num <= 0.0 -> "El alto debe ser mayor a 0 m"
                num > 15.0 -> "Verifica la altura (máx. 15 m)"
                else -> null
            }
        }
    }

    val isFormValid by remember(lengthValidation, heightValidation) {
        derivedStateOf { lengthValidation == null && heightValidation == null }
    }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                    FormBannerCard()
                }
            }

            // Presets rápidos de obra
            item {
                Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                    PresetsRow(
                        lengthInput = uiState.lengthInput,
                        heightInput = uiState.heightInput,
                        faces = uiState.faces,
                        onApplyPreset = viewModel::applyPreset
                    )
                }
            }

            // Dimensiones con Steppers y Validación en Vivo
            item {
                Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                    DimensionsCard(
                        lengthInput = uiState.lengthInput,
                        heightInput = uiState.heightInput,
                        lengthError = lengthValidation,
                        heightError = heightValidation,
                        onLengthChange = viewModel::onLengthChanged,
                        onHeightChange = viewModel::onHeightChanged,
                        onAdjustLength = viewModel::adjustLengthBy,
                        onAdjustHeight = viewModel::adjustHeightBy
                    )
                }
            }

            // Configuración Técnica del Muro
            item {
                Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                    TechnicalSpecsCard(
                        faces = uiState.faces,
                        studSpacingMeters = uiState.studSpacingMeters,
                        boardType = uiState.boardType,
                        hasInsulation = uiState.hasInsulation,
                        onFacesChange = viewModel::onFacesChanged,
                        onStudSpacingChange = viewModel::onStudSpacingChanged,
                        onBoardTypeChange = viewModel::onBoardTypeChanged,
                        onInsulationToggle = viewModel::onInsulationToggled
                    )
                }
            }

            // Botón de Acción Principal para Calcular y Ver Resultados
            item {
                Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                    Button(
                        onClick = { viewModel.calculateAndGoToResults() },
                        enabled = isFormValid,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("calculate_and_view_results_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Calcular y Ver Desglose de Materiales",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FormBannerCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Straighten,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Formulario de Cálculo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ingresa las medidas netas del muro",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "8% Desperdicio",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PresetsRow(
    lengthInput: String,
    heightInput: String,
    faces: WallFaces,
    onApplyPreset: (Double, Double, Boolean, Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Presets Comunes de Construcción",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InputChip(
                selected = lengthInput == "4.0" && heightInput == "2.6" && faces == WallFaces.BOTH_FACES,
                onClick = { onApplyPreset(4.0, 2.6, true, true) },
                label = { Text("Tabique 4.0 x 2.6 m") }
            )
            InputChip(
                selected = lengthInput == "6.0" && heightInput == "2.8" && faces == WallFaces.BOTH_FACES,
                onClick = { onApplyPreset(6.0, 2.8, true, true) },
                label = { Text("Muro Alto 6.0 x 2.8 m") }
            )
            InputChip(
                selected = lengthInput == "5.0" && heightInput == "2.5" && faces == WallFaces.ONE_FACE,
                onClick = { onApplyPreset(5.0, 2.5, false, false) },
                label = { Text("Revestimiento 5.0 x 2.5 m (1 Cara)") }
            )
        }
    }
}

@Composable
fun DimensionsCard(
    lengthInput: String,
    heightInput: String,
    lengthError: String?,
    heightError: String?,
    onLengthChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onAdjustLength: (Double) -> Unit,
    onAdjustHeight: (Double) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Dimensiones del Muro (Metros)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Largo con Steppers
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Largo del muro (m)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onAdjustLength(-0.5) },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Restar 0.5m")
                    }

                    OutlinedTextField(
                        value = lengthInput,
                        onValueChange = onLengthChange,
                        placeholder = { Text("ej. 4.0") },
                        isError = lengthError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("form_length_field"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    IconButton(
                        onClick = { onAdjustLength(0.5) },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Sumar 0.5m")
                    }
                }

                if (lengthError != null) {
                    Text(
                        text = lengthError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 52.dp)
                    )
                }
            }

            // Alto con Steppers
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Alto del muro (m)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onAdjustHeight(-0.5) },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Restar 0.5m")
                    }

                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = onHeightChange,
                        placeholder = { Text("ej. 2.6") },
                        isError = heightError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("form_height_field"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    IconButton(
                        onClick = { onAdjustHeight(0.5) },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Sumar 0.5m")
                    }
                }

                if (heightError != null) {
                    Text(
                        text = heightError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 52.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicalSpecsCard(
    faces: WallFaces,
    studSpacingMeters: Double,
    boardType: DrywallBoardType,
    hasInsulation: Boolean,
    onFacesChange: (WallFaces) -> Unit,
    onStudSpacingChange: (Double) -> Unit,
    onBoardTypeChange: (DrywallBoardType) -> Unit,
    onInsulationToggle: (Boolean) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
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
            Text(
                text = "Especificaciones Técnicas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Caras emplacadas
            Text(
                text = "Caras emplacadas",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = faces == WallFaces.ONE_FACE,
                    onClick = { onFacesChange(WallFaces.ONE_FACE) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("1 Cara")
                }
                SegmentedButton(
                    selected = faces == WallFaces.BOTH_FACES,
                    onClick = { onFacesChange(WallFaces.BOTH_FACES) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("2 Caras")
                }
            }

            HorizontalDivider()

            // Separación de postes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Separación entre postes",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (studSpacingMeters < 0.5) "40.7 cm (16\") - Muros altos o mayor rigidez" else "61.0 cm (24\") - Muros estándar ligeros",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = studSpacingMeters < 0.5,
                        onClick = { onStudSpacingChange(0.407) },
                        label = { Text("40.7 cm") }
                    )
                    FilterChip(
                        selected = studSpacingMeters >= 0.5,
                        onClick = { onStudSpacingChange(0.61) },
                        label = { Text("61 cm") }
                    )
                }
            }

            HorizontalDivider()

            // Tipo de placa
            Text(
                text = "Tipo de Placa",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DrywallBoardType.entries.forEach { type ->
                    FilterChip(
                        selected = boardType == type,
                        onClick = { onBoardTypeChange(type) },
                        label = { Text(type.displayName) }
                    )
                }
            }

            // Switch de Aislamiento
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Aislamiento Termoacústico",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Lana de vidrio o mineral en rollos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = hasInsulation,
                    onCheckedChange = onInsulationToggle,
                    modifier = Modifier.testTag("form_insulation_switch")
                )
            }
        }
    }
}
