package com.example.drywall.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.drywall.domain.model.MaterialCategory
import com.example.drywall.domain.model.MaterialRequirement
import com.example.drywall.domain.model.WallCalculationResult
import com.example.drywall.ui.components.SaveProjectDialog
import com.example.drywall.ui.components.ShareReportDialog
import com.example.drywall.ui.viewmodel.AppScreen
import com.example.drywall.ui.viewmodel.DrywallCalculatorViewModel

/**
 * Pantalla de Resultados con desglose completo de insumos estilo tarjeta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationResultsScreen(
    viewModel: DrywallCalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val result = uiState.calculationResult
    var showShareDialog by remember { mutableStateOf(false) }

    // Manejador del botón atrás para retornar al formulario
    BackHandler {
        viewModel.navigateTo(AppScreen.CALCULATOR)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Lista de Materiales",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        result?.let {
                            Text(
                                text = "${it.input.lengthMeters}m x ${it.input.heightMeters}m (${it.input.faces.label})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.CALCULATOR) },
                        modifier = Modifier.testTag("back_to_form_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver al formulario")
                    }
                },
                actions = {
                    result?.let { res ->
                        IconButton(
                            onClick = { showShareDialog = true },
                            modifier = Modifier.testTag("share_whatsapp_top_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir por WhatsApp")
                        }
                        IconButton(
                            onClick = { copyResultToClipboard(context, res) },
                            modifier = Modifier.testTag("copy_materials_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar lista")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (result == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No hay resultados de cálculo disponibles.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { viewModel.navigateTo(AppScreen.CALCULATOR) }) {
                        Text("Ir al Formulario")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Banner Hero con métricas ejecutivas
                item {
                    Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                        ResultsSummaryBanner(
                            result = result,
                            onSaveClick = { viewModel.setShowSaveDialog(true) },
                            onEditClick = { viewModel.navigateTo(AppScreen.CALCULATOR) },
                            onShareWhatsAppClick = { showShareDialog = true }
                        )
                    }
                }

                // Filtro por Categoría de Materiales
                item {
                    Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                        CategoryFilterChips(
                            selectedCategory = uiState.selectedCategoryFilter,
                            onSelectCategory = viewModel::onCategoryFilterSelected
                        )
                    }
                }

                // Lista de Tarjetas de Material
                val filteredItems = if (uiState.selectedCategoryFilter == null) {
                    result.requirements
                } else {
                    result.requirements.filter { it.material.category == uiState.selectedCategoryFilter }
                }

                items(filteredItems, key = { it.material.id }) { item ->
                    Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                        MaterialDetailCard(requirement = item)
                    }
                }
            }
        }
    }

    // Diálogo de exportación y reporte para WhatsApp
    if (showShareDialog && result != null) {
        ShareReportDialog(
            result = result,
            onDismiss = { showShareDialog = false }
        )
    }

    // Diálogo de guardado en Room
    if (uiState.showSaveDialog) {
        SaveProjectDialog(
            defaultWallName = "Muro ${uiState.lengthInput}x${uiState.heightInput}m",
            onDismiss = { viewModel.setShowSaveDialog(false) },
            onConfirm = { projectName, client, notes, wallName ->
                viewModel.saveCurrentProject(
                    projectName = projectName,
                    clientOrLocation = client,
                    notes = notes,
                    wallName = wallName
                )
            }
        )
    }
}


@Composable
fun ResultsSummaryBanner(
    result: WallCalculationResult,
    onSaveClick: () -> Unit,
    onEditClick: () -> Unit,
    onShareWhatsAppClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Resumen del Cómputo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Muro de ${result.input.lengthMeters} m x ${result.input.heightMeters} m",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "+8% Desperdicio",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Grid de 3 Métricas Destacadas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryMetricItem(
                    label = "Superficie a Emplacar",
                    value = "%.2f m²".format(result.input.totalDrywallArea),
                    subtext = result.input.faces.label
                )
                SummaryMetricItem(
                    label = "Placas (1.22x2.44)",
                    value = "${result.totalBoardsRequired} unid",
                    subtext = result.input.boardType.displayName.substringBefore(" (")
                )
                SummaryMetricItem(
                    label = "Perfiles 3m",
                    value = "${result.totalProfiles3mRequired} piezas",
                    subtext = "Canales + Postes"
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

            // Botón destacado de exportar reporte a WhatsApp
            Button(
                onClick = onShareWhatsAppClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("export_whatsapp_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1EBE5D),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Exportar Reporte a WhatsApp",
                    fontWeight = FontWeight.Bold
                )
            }

            // Acciones secundarias: Guardar Obra / Modificar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onSaveClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("results_save_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Guardar Obra")
                }

                OutlinedButton(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Modificar")
                }
            }
        }
    }
}


@Composable
fun SummaryMetricItem(label: String, value: String, subtext: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = subtext,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun CategoryFilterChips(
    selectedCategory: MaterialCategory?,
    onSelectCategory: (MaterialCategory?) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onSelectCategory(null) },
            label = { Text("Todos") }
        )
        MaterialCategory.entries.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onSelectCategory(category) },
                label = { Text(category.displayName) },
                leadingIcon = {
                    Icon(
                        imageVector = getCategoryIcon(category),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

/**
 * Tarjeta individual detallada para cada insumo con desglose matemático y presentación comercial.
 */
@Composable
fun MaterialDetailCard(
    requirement: MaterialRequirement,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
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
            // Header de la tarjeta: Código, Categoría y Cantidad Grande
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Text(
                                text = requirement.material.code,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = requirement.material.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = requirement.material.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = requirement.material.commercialSpec,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Contenedor grande de cantidad comercial
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${requirement.commercialQuantity}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = requirement.unit.pluralName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Desglose Cuantitativo: Teórico vs Con 8% Desperdicio vs Piezas Comerciales
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Base: %.2f".format(requirement.rawQuantity),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "+8% Desp: %.2f".format(requirement.quantityWithWaste),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Comercial: ${requirement.commercialQuantity} ${requirement.unit.symbol}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Nota técnica explicativa con ícono
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = requirement.formulaExplanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun getCategoryIcon(category: MaterialCategory): ImageVector = when (category) {
    MaterialCategory.ESTRUCTURA -> Icons.Default.Architecture
    MaterialCategory.PLACAS -> Icons.Default.Layers
    MaterialCategory.FIJACIONES -> Icons.Default.Build
    MaterialCategory.ACABADOS -> Icons.Default.Brush
    MaterialCategory.AISLAMIENTO -> Icons.Default.Security
}

fun copyResultToClipboard(context: Context, result: WallCalculationResult) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val sb = StringBuilder()
    sb.append("=== LISTA DE MATERIALES DRYWALL ===\n")
    sb.append("Dimensiones: ${result.input.lengthMeters} m x ${result.input.heightMeters} m (${result.input.faces.label})\n")
    sb.append("Superficie total: %.2f m²\n".format(result.input.totalDrywallArea))
    sb.append("Desperdicio aplicado: 8%\n\n")

    for ((category, items) in result.requirementsByCategory) {
        sb.append("[${category.displayName.uppercase()}]\n")
        for (req in items) {
            sb.append("- ${req.material.name}: ${req.commercialQuantity} ${req.unit.pluralName}\n")
        }
        sb.append("\n")
    }

    val clip = ClipData.newPlainText("Lista Materiales Drywall", sb.toString())
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Lista copiada al portapapeles", Toast.LENGTH_SHORT).show()
}
