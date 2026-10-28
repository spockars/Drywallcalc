package com.example.drywall.ui.screens

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
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Straighten
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
import com.example.drywall.domain.model.ConstructionElementType
import com.example.drywall.domain.model.DrywallBoardType
import com.example.drywall.domain.model.WallFaces
import com.example.drywall.ui.components.OpeningsCard
import com.example.drywall.ui.components.PaintCard
import com.example.drywall.ui.viewmodel.DrywallCalculatorViewModel

/**
 * Pantalla de Formulario de Cálculo de Construcción en Seco (Muro, Cielo Raso Drywall, PVC, Muebles).
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
                num == null -> "Ingresa un número válido"
                num <= 0.0 -> "El largo debe ser mayor a 0 m"
                num > 100.0 -> "Verifica el largo"
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
                num == null -> "Ingresa un número válido"
                num <= 0.0 -> "El alto debe ser mayor a 0 m"
                num > 15.0 -> "Verifica la altura"
                else -> null
            }
        }
    }

    val isFormValid by remember(lengthValidation, heightValidation) {
        derivedStateOf { lengthValidation == null && heightValidation == null }
    }

    val openingsAreaM2 by remember(uiState.openingDrafts) {
        derivedStateOf {
            uiState.openingDrafts.sumOf { draft ->
                val width = draft.widthInput.replace(',', '.').trim().toDoubleOrNull() ?: 0.0
                val height = draft.heightInput.replace(',', '.').trim().toDoubleOrNull() ?: 0.0
                if (width > 0.0 && height > 0.0) width * height else 0.0
            }
        }
    }

    val netAreaM2 by remember(uiState.openingDrafts, uiState.lengthInput, uiState.heightInput) {
        derivedStateOf {
            val length = uiState.lengthInput.replace(',', '.').trim().toDoubleOrNull() ?: 0.0
            val height = uiState.heightInput.replace(',', '.').trim().toDoubleOrNull() ?: 0.0
            (length * height - openingsAreaM2).coerceAtLeast(0.0)
        }
    }

    val paintAreaM2 by remember(netAreaM2, uiState.faces) {
        derivedStateOf { netAreaM2 * uiState.faces.factor }
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
                    FormBannerCard(selectedType = uiState.selectedElementType)
                }
            }

            // Selector de Tipo de Elemento
            item {
                Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                    ElementTypeSelectorCard(
                        selectedType = uiState.selectedElementType,
                        elementName = uiState.elementNameInput,
                        onTypeSelected = viewModel::onElementTypeSelected,
                        onElementNameChange = viewModel::onElementNameChanged
                    )
                }
            }

            when (uiState.selectedElementType) {
                ConstructionElementType.MURO_DRYWALL -> {
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

                    item {
                        Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                            OpeningsCard(
                                drafts = uiState.openingDrafts,
                                errors = uiState.openingsValidationErrors,
                                openingsAreaM2 = openingsAreaM2,
                                netAreaM2 = netAreaM2,
                                onAddOpening = viewModel::addOpening,
                                onClearOpenings = viewModel::clearOpenings,
                                onRemoveOpening = viewModel::removeOpening,
                                onTypeChange = viewModel::updateOpeningType,
                                onWidthChange = viewModel::updateOpeningWidth,
                                onHeightChange = viewModel::updateOpeningHeight
                            )
                        }
                    }

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

                    item {
                        Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                            PaintCard(
                                enabled = uiState.paintEnabled,
                                productName = uiState.paintProductNameInput,
                                unit = uiState.paintUnit,
                                coverageInput = uiState.paintCoverageInput,
                                finishCoatsInput = uiState.paintFinishCoatsInput,
                                wastePercentInput = uiState.paintWastePercentInput,
                                errors = uiState.paintValidationErrors,
                                paintAreaM2 = paintAreaM2,
                                onEnabledChange = viewModel::onPaintEnabledToggled,
                                onProductNameChange = viewModel::onPaintProductNameChanged,
                                onUnitChange = viewModel::onPaintUnitChanged,
                                onCoverageChange = viewModel::onPaintCoverageChanged,
                                onFinishCoatsChange = viewModel::onPaintFinishCoatsChanged,
                                onWasteChange = viewModel::onPaintWasteChanged
                            )
                        }
                    }
                }

                ConstructionElementType.CIELO_RASO_DRYWALL -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                            CeilingDimensionsCard(
                                lengthInput = uiState.lengthInput,
                                widthInput = uiState.ceilingWidthInput,
                                suspensionInput = uiState.suspensionHeightInput,
                                onLengthChange = viewModel::onLengthChanged,
                                onWidthChange = viewModel::onCeilingWidthChanged,
                                onSuspensionChange = viewModel::onSuspensionHeightChanged
                            )
                        }
                    }

                    item {
                        Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                            TechnicalSpecsCard(
                                faces = WallFaces.ONE_FACE,
                                studSpacingMeters = uiState.studSpacingMeters,
                                boardType = uiState.boardType,
                                hasInsulation = uiState.hasInsulation,
                                onFacesChange = {},
                                onStudSpacingChange = viewModel::onStudSpacingChanged,
                                onBoardTypeChange = viewModel::onBoardTypeChanged,
                                onInsulationToggle = viewModel::onInsulationToggled
                            )
                        }
                    }

                    item {
                        Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                            PaintCard(
                                enabled = uiState.paintEnabled,
                                productName = uiState.paintProductNameInput,
                                unit = uiState.paintUnit,
                                coverageInput = uiState.paintCoverageInput,
                                finishCoatsInput = uiState.paintFinishCoatsInput,
                                wastePercentInput = uiState.paintWastePercentInput,
                                errors = uiState.paintValidationErrors,
                                paintAreaM2 = paintAreaM2,
                                onEnabledChange = viewModel::onPaintEnabledToggled,
                                onProductNameChange = viewModel::onPaintProductNameChanged,
                                onUnitChange = viewModel::onPaintUnitChanged,
                                onCoverageChange = viewModel::onPaintCoverageChanged,
                                onFinishCoatsChange = viewModel::onPaintFinishCoatsChanged,
                                onWasteChange = viewModel::onPaintWasteChanged
                            )
                        }
                    }
                }

                ConstructionElementType.CIELO_RASO_PVC -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                            PvcCeilingDimensionsCard(
                                lengthInput = uiState.lengthInput,
                                widthInput = uiState.ceilingWidthInput,
                                slatWidthInput = uiState.pvcSlatWidthInput,
                                slatLengthInput = uiState.pvcCommercialSlatLengthInput,
                                onLengthChange = viewModel::onLengthChanged,
                                onWidthChange = viewModel::onCeilingWidthChanged,
                                onSlatWidthChange = viewModel::onPvcSlatWidthChanged,
                                onSlatLengthChange = viewModel::onPvcCommercialSlatLengthChanged
                            )
                        }
                    }
                }

                ConstructionElementType.MUEBLE_DRYWALL -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                            FurnitureDimensionsCard(
                                widthInput = uiState.furnitureWidthInput,
                                heightInput = uiState.furnitureHeightInput,
                                depthInput = uiState.furnitureDepthInput,
                                shelvesInput = uiState.horizontalShelvesCountInput,
                                dividersInput = uiState.verticalDividersCountInput,
                                onWidthChange = viewModel::onFurnitureWidthChanged,
                                onHeightChange = viewModel::onFurnitureHeightChanged,
                                onDepthChange = viewModel::onFurnitureDepthChanged,
                                onShelvesChange = viewModel::onHorizontalShelvesCountChanged,
                                onDividersChange = viewModel::onVerticalDividersCountChanged
                            )
                        }
                    }
                }
            }

            // Botón de Acción Principal para Calcular
            item {
                Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                    Button(
                        onClick = { viewModel.calculateAndGoToResults() },
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
                            text = "Calcular y Ver Desglose",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElementTypeSelectorCard(
    selectedType: ConstructionElementType,
    elementName: String,
    onTypeSelected: (ConstructionElementType) -> Unit,
    onElementNameChange: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Tipo de Elemento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConstructionElementType.entries.forEach { type ->
                    val icon = when (type) {
                        ConstructionElementType.MURO_DRYWALL -> Icons.Default.Layers
                        ConstructionElementType.CIELO_RASO_DRYWALL -> Icons.Default.Straighten
                        ConstructionElementType.CIELO_RASO_PVC -> Icons.Default.GridView
                        ConstructionElementType.MUEBLE_DRYWALL -> Icons.Default.Layers
                    }
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { onTypeSelected(type) },
                        label = { Text(type.displayName) },
                        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            OutlinedTextField(
                value = elementName,
                onValueChange = onElementNameChange,
                label = { Text("Nombre o Ubicación del Elemento") },
                placeholder = { Text("ej. Pared Principal / Techo Baño / Mueble TV") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun CeilingDimensionsCard(
    lengthInput: String,
    widthInput: String,
    suspensionInput: String,
    onLengthChange: (String) -> Unit,
    onWidthChange: (String) -> Unit,
    onSuspensionChange: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Dimensiones del Cielo Raso (m)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = lengthInput,
                onValueChange = onLengthChange,
                label = { Text("Largo del Techo (m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = widthInput,
                onValueChange = onWidthChange,
                label = { Text("Ancho del Techo (m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = suspensionInput,
                onValueChange = onSuspensionChange,
                label = { Text("Altura de Descolgado / Suspensión (m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun PvcCeilingDimensionsCard(
    lengthInput: String,
    widthInput: String,
    slatWidthInput: String,
    slatLengthInput: String,
    onLengthChange: (String) -> Unit,
    onWidthChange: (String) -> Unit,
    onSlatWidthChange: (String) -> Unit,
    onSlatLengthChange: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Dimensiones Cielo Raso PVC (m)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = lengthInput,
                onValueChange = onLengthChange,
                label = { Text("Largo del Techo (m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = widthInput,
                onValueChange = onWidthChange,
                label = { Text("Ancho del Techo (m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = slatWidthInput,
                    onValueChange = onSlatWidthChange,
                    label = { Text("Ancho Tablilla (m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = slatLengthInput,
                    onValueChange = onSlatLengthChange,
                    label = { Text("Largo Comercial (m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
fun FurnitureDimensionsCard(
    widthInput: String,
    heightInput: String,
    depthInput: String,
    shelvesInput: String,
    dividersInput: String,
    onWidthChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onDepthChange: (String) -> Unit,
    onShelvesChange: (String) -> Unit,
    onDividersChange: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Dimensiones del Mueble / Repisa (m)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = widthInput,
                    onValueChange = onWidthChange,
                    label = { Text("Ancho (m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = heightInput,
                    onValueChange = onHeightChange,
                    label = { Text("Alto (m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = depthInput,
                onValueChange = onDepthChange,
                label = { Text("Profundidad (m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = shelvesInput,
                    onValueChange = onShelvesChange,
                    label = { Text("N° Repisas Horiz.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = dividersInput,
                    onValueChange = onDividersChange,
                    label = { Text("N° Divisores Vert.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
fun FormBannerCard(selectedType: ConstructionElementType) {
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
                            imageVector = when (selectedType) {
                                ConstructionElementType.MURO_DRYWALL -> Icons.Default.Layers
                                ConstructionElementType.CIELO_RASO_DRYWALL -> Icons.Default.Straighten
                                ConstructionElementType.CIELO_RASO_PVC -> Icons.Default.GridView
                                ConstructionElementType.MUEBLE_DRYWALL -> Icons.Default.Layers
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = selectedType.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedType.description,
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
