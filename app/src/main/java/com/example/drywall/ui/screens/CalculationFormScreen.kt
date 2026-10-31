package com.example.drywall.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Kitchen
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.drywall.domain.model.ConstructionElementType
import com.example.drywall.domain.model.DrywallBoardType
import com.example.drywall.domain.model.ProfileGauge
import com.example.drywall.domain.model.ProfileLength
import com.example.drywall.domain.model.ProfileWidth
import com.example.drywall.domain.model.WallFaces
import com.example.drywall.ui.components.OpeningsCard
import com.example.drywall.ui.components.PaintCard
import com.example.drywall.ui.viewmodel.DrywallCalculatorViewModel

/**
 * Pantalla de Formulario de Cálculo con Tarjetas Modulares y Acordeones Colapsables.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationFormScreen(
    viewModel: DrywallCalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Estados de acordeón para las tarjetas modulares (Dimensiones expandida por defecto)
    var isDimensionsExpanded by remember { mutableStateOf(true) }
    var isOpeningsExpanded by remember { mutableStateOf(false) }
    var isSpecsExpanded by remember { mutableStateOf(false) }
    var isPaintExpanded by remember { mutableStateOf(false) }

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            // Sticky Bottom Summary Bar en tiempo real
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Área Neta: ${String.format(java.util.Locale.getDefault(), "%.2f", netAreaM2)} m²",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (uiState.selectedElementType == ConstructionElementType.MURO_DRYWALL) 
                                "Pintura: ${String.format(java.util.Locale.getDefault(), "%.2f", paintAreaM2)} m²" 
                            else "Cálculo activo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = { viewModel.calculateAndGoToResults() },
                        shape = RoundedCornerShape(14.dp),
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("calculate_and_view_results_button")
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Calcular", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
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
                                onApplyPreset = viewModel::applyPreset
                            )
                        }
                    }

                    // Acordeón 1: Dimensiones (Expandido por defecto)
                    item {
                        Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isDimensionsExpanded = !isDimensionsExpanded },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Straighten, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Column {
                                                Text("1. Dimensiones Principales", fontWeight = FontWeight.Bold)
                                                if (!isDimensionsExpanded) {
                                                    Text(
                                                        "Largo: ${uiState.lengthInput}m • Alto: ${uiState.heightInput}m (${uiState.faces.label})",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                        Icon(
                                            imageVector = if (isDimensionsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null
                                        )
                                    }

                                    if (isDimensionsExpanded) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        HorizontalDivider()
                                        Spacer(modifier = Modifier.height(16.dp))
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
                            }
                        }
                    }

                    // Acordeón 2: Aberturas (Colapsado por defecto)
                    item {
                        Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isOpeningsExpanded = !isOpeningsExpanded },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Column {
                                                Text("2. Aberturas y Vanos", fontWeight = FontWeight.Bold)
                                                Text(
                                                    if (uiState.openingDrafts.isEmpty()) "Sin aberturas registradas" 
                                                    else "${uiState.openingDrafts.size} abertura(s) (-${String.format(java.util.Locale.getDefault(), "%.2f", openingsAreaM2)} m²)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = if (isOpeningsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null
                                        )
                                    }

                                    if (isOpeningsExpanded) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        HorizontalDivider()
                                        Spacer(modifier = Modifier.height(16.dp))
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
                            }
                        }
                    }

                    // Acordeón 3: Especificaciones Técnicas (Colapsado por defecto)
                    item {
                        Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isSpecsExpanded = !isSpecsExpanded },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Column {
                                                Text("3. Especificaciones Técnicas", fontWeight = FontWeight.Bold)
                                                Text(
                                                    "${uiState.boardType.displayName} • ${uiState.profileLength.displayName} • Stud ${uiState.profileWidth.displayName} • ${uiState.profileGauge.displayName}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = if (isSpecsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null
                                        )
                                    }

                                    if (isSpecsExpanded) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        HorizontalDivider()
                                        Spacer(modifier = Modifier.height(16.dp))
                                        TechnicalSpecsCard(
                                            faces = uiState.faces,
                                            boardTypeFaceA = uiState.boardTypeFaceA,
                                            boardTypeFaceB = uiState.boardTypeFaceB,
                                            hasInsulation = uiState.hasInsulation,
                                            profileWidth = uiState.profileWidth,
                                            profileGauge = uiState.profileGauge,
                                            profileLength = uiState.profileLength,
                                            onFacesChange = viewModel::onFacesChanged,
                                            onBoardTypeFaceAChange = viewModel::onBoardTypeFaceAChanged,
                                            onBoardTypeFaceBChange = viewModel::onBoardTypeFaceBChanged,
                                            onInsulationToggle = viewModel::onInsulationToggled,
                                            onProfileWidthChange = viewModel::onProfileWidthChanged,
                                            onProfileGaugeChange = viewModel::onProfileGaugeChanged,
                                            onProfileLengthChange = viewModel::onProfileLengthChanged
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Acordeón 4: Pintura y Acabados (Colapsado por defecto)
                    item {
                        Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isPaintExpanded = !isPaintExpanded },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Column {
                                                Text("4. Acabados y Pintura", fontWeight = FontWeight.Bold)
                                                Text(
                                                    if (uiState.paintEnabled) "Habilitado (${String.format(java.util.Locale.getDefault(), "%.2f", paintAreaM2)} m² a pintar)" else "Deshabilitado",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = if (isPaintExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null
                                        )
                                    }

                                    if (isPaintExpanded) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        HorizontalDivider()
                                        Spacer(modifier = Modifier.height(16.dp))
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
        }
    }
}

// -----------------------------------------------------------------------------
// Componentes Auxiliares de Formulario
// -----------------------------------------------------------------------------

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
                        ConstructionElementType.MUEBLE_DRYWALL -> Icons.Default.Kitchen
                    }
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { onTypeSelected(type) },
                        label = { Text(type.displayName) },
                        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
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
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "🏗️ Cómputo: ${selectedType.displayName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Configura las medidas y especificaciones técnicas para generar el presupuesto detallado de materiales.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun PresetsRow(
    onApplyPreset: (Double, Double, Boolean, Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = false,
            onClick = { onApplyPreset(3.0, 2.6, true, true) },
            label = { Text("3.0m x 2.6m (Estándar)") }
        )
        FilterChip(
            selected = false,
            onClick = { onApplyPreset(4.0, 2.6, true, true) },
            label = { Text("4.0m x 2.6m") }
        )
        FilterChip(
            selected = false,
            onClick = { onApplyPreset(5.0, 2.8, true, true) },
            label = { Text("5.0m x 2.8m (Grande)") }
        )
        FilterChip(
            selected = false,
            onClick = { onApplyPreset(3.0, 2.6, false, true) },
            label = { Text("Trasdosado 1 Cara") }
        )
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = lengthInput,
            onValueChange = onLengthChange,
            label = { Text("Largo del Muro (metros)") },
            isError = lengthError != null,
            supportingText = { lengthError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onAdjustLength(-0.5) }, modifier = Modifier.weight(1f)) { Text("- 0.5m") }
            Button(onClick = { onAdjustLength(0.5) }, modifier = Modifier.weight(1f)) { Text("+ 0.5m") }
        }

        OutlinedTextField(
            value = heightInput,
            onValueChange = onHeightChange,
            label = { Text("Alto del Muro (metros)") },
            isError = heightError != null,
            supportingText = { heightError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onAdjustHeight(-0.1) }, modifier = Modifier.weight(1f)) { Text("- 0.1m") }
            Button(onClick = { onAdjustHeight(0.1) }, modifier = Modifier.weight(1f)) { Text("+ 0.1m") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicalSpecsCard(
    faces: WallFaces,
    boardTypeFaceA: DrywallBoardType,
    boardTypeFaceB: DrywallBoardType,
    hasInsulation: Boolean,
    profileWidth: ProfileWidth,
    profileGauge: ProfileGauge,
    profileLength: ProfileLength,
    onFacesChange: (WallFaces) -> Unit,
    onBoardTypeFaceAChange: (DrywallBoardType) -> Unit,
    onBoardTypeFaceBChange: (DrywallBoardType) -> Unit,
    onInsulationToggle: (Boolean) -> Unit,
    onProfileWidthChange: (ProfileWidth) -> Unit,
    onProfileGaugeChange: (ProfileGauge) -> Unit,
    onProfileLengthChange: (ProfileLength) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Caras a Revestir", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WallFaces.entries.forEach { f ->
                FilterChip(
                    selected = faces == f,
                    onClick = { onFacesChange(f) },
                    label = { Text(f.label) }
                )
            }
        }

        Text(if (faces == WallFaces.BOTH_FACES) "Tipo de Placa — Cara A (Interior)" else "Tipo de Placa", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            DrywallBoardType.entries.forEach { board ->
                FilterChip(
                    selected = boardTypeFaceA == board,
                    onClick = { onBoardTypeFaceAChange(board) },
                    label = { Text(board.displayName) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (faces == WallFaces.BOTH_FACES) {
            Text("Tipo de Placa — Cara B (Exterior / 2da Cara)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DrywallBoardType.entries.forEach { board ->
                    FilterChip(
                        selected = boardTypeFaceB == board,
                        onClick = { onBoardTypeFaceBChange(board) },
                        label = { Text(board.displayName) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Text("Longitud Comercial del Perfil", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ProfileLength.entries.forEach { len ->
                FilterChip(
                    selected = profileLength == len,
                    onClick = { onProfileLengthChange(len) },
                    label = { Text(len.displayName) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Text("Ancho del Perfil (Stud / Canal)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ProfileWidth.entries.forEach { width ->
                FilterChip(
                    selected = profileWidth == width,
                    onClick = { onProfileWidthChange(width) },
                    label = { Text(width.displayName) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Text("Calibre del Acero Galvanizado", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ProfileGauge.entries.forEach { gauge ->
                FilterChip(
                    selected = profileGauge == gauge,
                    onClick = { onProfileGaugeChange(gauge) },
                    label = { Text(gauge.displayName) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Aislamiento Térmico / Acústico", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text("Lana de vidrio o mineral en cavidad", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = hasInsulation,
                onCheckedChange = onInsulationToggle
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
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Dimensiones Cielo Raso Drywall", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = lengthInput,
                onValueChange = onLengthChange,
                label = { Text("Largo (m)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = widthInput,
                onValueChange = onWidthChange,
                label = { Text("Ancho (m)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = suspensionInput,
                onValueChange = onSuspensionChange,
                label = { Text("Altura de Suspensión (m)") },
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
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Dimensiones Cielo Raso PVC", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = lengthInput,
                onValueChange = onLengthChange,
                label = { Text("Largo del Área (m)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = widthInput,
                onValueChange = onWidthChange,
                label = { Text("Ancho del Área (m)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = slatWidthInput,
                onValueChange = onSlatWidthChange,
                label = { Text("Ancho de Tablilla PVC (m)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = slatLengthInput,
                onValueChange = onSlatLengthChange,
                label = { Text("Largo Comercial Tablilla (m)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
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
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Dimensiones Mueble / Repisa Drywall", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = widthInput,
                onValueChange = onWidthChange,
                label = { Text("Ancho Total (m)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = heightInput,
                onValueChange = onHeightChange,
                label = { Text("Alto Total (m)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = depthInput,
                onValueChange = onDepthChange,
                label = { Text("Profundidad (m)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = shelvesInput,
                onValueChange = onShelvesChange,
                label = { Text("Cantidad de Repisas Horizontales") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = dividersInput,
                onValueChange = onDividersChange,
                label = { Text("Cantidad de Divisiones Verticales") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
