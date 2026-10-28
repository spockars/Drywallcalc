package com.example.drywall.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.drywall.data.local.entity.ProjectWithCalculations
import com.example.drywall.data.local.entity.WallCalculationEntity
import com.example.drywall.data.local.entity.effectiveNetAreaM2
import com.example.drywall.data.local.entity.totalPaintUnits
import com.example.drywall.data.mapper.ProjectBudgetView
import com.example.drywall.ui.components.BudgetSummary
import com.example.drywall.ui.components.LaborDialog
import com.example.drywall.ui.viewmodel.AppScreen
import com.example.drywall.ui.viewmodel.DrywallCalculatorViewModel
import com.example.drywall.util.DrywallReportExporter
import com.example.drywall.util.DrywallPdfExporter
import com.example.drywall.util.NumberFormatUtils
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsListScreen(
    viewModel: DrywallCalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val context = LocalContext.current

    val filteredProjects = remember(uiState.savedProjects, uiState.searchQuery) {
        if (uiState.searchQuery.isBlank()) {
            uiState.savedProjects
        } else {
            val q = uiState.searchQuery.trim().lowercase(Locale.getDefault())
            uiState.savedProjects.filter {
                it.project.name.lowercase(Locale.getDefault()).contains(q) ||
                        it.project.clientOrLocation.lowercase(Locale.getDefault()).contains(q) ||
                        it.project.notes.lowercase(Locale.getDefault()).contains(q)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.navigateTo(AppScreen.CALCULATOR) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo Cómputo") },
                modifier = Modifier.testTag("new_computation_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                    ProjectsHeaderCard(
                        totalProjectsCount = uiState.savedProjects.size,
                        searchQuery = uiState.searchQuery,
                        onSearchChange = viewModel::onSearchQueryChanged
                    )
                }
            }

            if (filteredProjects.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                        EmptyProjectsState(
                            hasSearch = uiState.searchQuery.isNotBlank(),
                            onCreateNew = { viewModel.navigateTo(AppScreen.CALCULATOR) }
                        )
                    }
                }
            } else {
                items(filteredProjects, key = { it.project.id }) { projectWithCalcs ->
                    Box(modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp)) {
                        ProjectCard(
                            projectWithCalcs = projectWithCalcs,
                            dateFormat = dateFormat,
                            onLoadCalculation = { calc ->
                                viewModel.loadSavedCalculation(projectWithCalcs.toWallWithOpenings(calc))
                            },
                            onDeleteCalculation = { wall ->
                                viewModel.setCalculationPendingDelete(wall)
                            },
                            budgetView = uiState.projectBudgets[projectWithCalcs.project.id],
                            onConfigureLabor = {
                                viewModel.openLaborDialog(projectWithCalcs.project.id)
                            },
                            onSharePdf = {
                                try {
                                    val file = DrywallPdfExporter.generateProjectPdf(
                                        context = context,
                                        project = projectWithCalcs.project,
                                        walls = projectWithCalcs.calculations.map {
                                            projectWithCalcs.toWallWithOpenings(it)
                                        },
                                        budgetView = uiState.projectBudgets[projectWithCalcs.project.id]
                                    )
                                    DrywallPdfExporter.sharePdf(
                                        context = context,
                                        file = file,
                                        subject = "Presupuesto ${projectWithCalcs.project.name}"
                                    )
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "No se pudo generar el PDF: ${e.localizedMessage}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            onDeleteClick = {
                                viewModel.setProjectPendingDelete(projectWithCalcs)
                            }
                        )
                    }
                }
            }
        }
    }

    uiState.laborProjectId?.let { projectId ->
        val projectName = uiState.savedProjects
            .firstOrNull { it.project.id == projectId }
            ?.project?.name ?: "Obra"
        LaborDialog(
            projectName = projectName,
            enabled = uiState.laborEnabledDraft,
            mode = uiState.laborModeDraft,
            amountInput = uiState.laborAmountDraft,
            onEnabledChange = viewModel::onLaborEnabledChanged,
            onModeChange = viewModel::onLaborModeChanged,
            onAmountChange = viewModel::onLaborAmountChanged,
            onDismiss = viewModel::dismissLaborDialog,
            onConfirm = viewModel::saveLabor
        )
    }

    uiState.calculationPendingDelete?.let { wallToDelete ->
        AlertDialog(
            onDismissRequest = { viewModel.setCalculationPendingDelete(null) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("¿Eliminar Muro? ") },
            text = {
                Text(
                    "Se eliminará el muro \"${wallToDelete.wallName}\" de la obra, con sus vanos. " +
                        "Los demás muros no se tocan."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCalculation(wallToDelete.id)
                        viewModel.setCalculationPendingDelete(null)
                    }
                ) {
                    Text("Eliminar muro")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setCalculationPendingDelete(null) }) {
                    Text("Cancelar")
                }
            }
        )
    }

    uiState.projectPendingDelete?.let { projectToDelete ->
        AlertDialog(
            onDismissRequest = { viewModel.setProjectPendingDelete(null) },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("¿Eliminar Proyecto?") },
            text = {
                Text("Se eliminará la obra \"${projectToDelete.project.name}\" y todos sus cómputos métricos asociados de la base de datos local.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProject(projectToDelete.project.id)
                        viewModel.setProjectPendingDelete(null)
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setProjectPendingDelete(null) }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ProjectsHeaderCard(
    totalProjectsCount: Int,
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Obras y Proyectos Guardados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Base de datos Room SQLite en dispositivo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "$totalProjectsCount ${if (totalProjectsCount == 1) "obra" else "obras"}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Buscar por obra, cliente o notas...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("projects_search_input")
            )
        }
    }
}

@Composable
fun ProjectCard(
    projectWithCalcs: ProjectWithCalculations,
    dateFormat: SimpleDateFormat,
    onLoadCalculation: (WallCalculationEntity) -> Unit,
    onDeleteCalculation: (WallCalculationEntity) -> Unit,
    budgetView: ProjectBudgetView?,
    onConfigureLabor: () -> Unit,
    onSharePdf: () -> Unit,
    onDeleteClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = projectWithCalcs.project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (projectWithCalcs.project.clientOrLocation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = projectWithCalcs.project.clientOrLocation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Guardado: ${dateFormat.format(Date(projectWithCalcs.project.updatedAt))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                val context = LocalContext.current
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val report = buildString {
                                append("📋 *PROYECTO DRYWALL: ${projectWithCalcs.project.name}*\n")
                                if (projectWithCalcs.project.clientOrLocation.isNotBlank()) {
                                    append("📍 *Cliente / Ubicación:* ${projectWithCalcs.project.clientOrLocation}\n")
                                }
                                append("📊 *Métricas Totales:*\n")
                                append("• Superficie neta: ${NumberFormatUtils.formatSquareMeters(projectWithCalcs.totalNetAreaM2)}\n")
                                append("• Total placas: ${projectWithCalcs.totalBoards} unid\n")
                                append("• Total perfiles 3m: ${projectWithCalcs.totalProfiles3m} piezas\n")
                                if (projectWithCalcs.totalPaintUnits > 0) {
                                    append("• Pintura total: ${projectWithCalcs.totalPaintUnits} envases\n")
                                }
                                append("\n🧱 *Muros de la obra (${projectWithCalcs.calculations.size}):*\n")
                                projectWithCalcs.calculations.forEach { calc ->
                                    val wallOpenings = projectWithCalcs.openingsFor(calc.id)
                                    append("• *${calc.wallName}*: ${NumberFormatUtils.formatDecimal(calc.lengthMeters, 1)}m × ${NumberFormatUtils.formatDecimal(calc.heightMeters, 1)}m (${if (calc.faces == "BOTH_FACES") "2 Caras" else "1 Cara"})\n")
                                    append("   → ${calc.totalBoardsCount} placas | ${calc.totalProfiles3mCount} perfiles 3m")
                                    append(" | ${NumberFormatUtils.formatSquareMeters(calc.effectiveNetAreaM2)} netos")
                                    if (wallOpenings.isNotEmpty()) {
                                        append(" | ${wallOpenings.size} vano(s)")
                                    }
                                    if (calc.totalPaintUnits > 0) {
                                        append(" | ${calc.totalPaintUnits} envases pintura")
                                    }
                                    append("\n")
                                }
                                append("\nGenerado por *DrywallCalc* 📱")
                            }
                            DrywallReportExporter.shareReport(context, report, "Compartir Proyecto por WhatsApp")
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir obra por WhatsApp",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar obra",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Superficie neta",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = NumberFormatUtils.formatSquareMeters(projectWithCalcs.totalNetAreaM2),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Placas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${projectWithCalcs.totalBoards} unid",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Perfiles 3m",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${projectWithCalcs.totalProfiles3m} unid",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (projectWithCalcs.totalPaintUnits > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Pintura",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${projectWithCalcs.totalPaintUnits} env",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (projectWithCalcs.project.notes.isNotBlank()) {
                Text(
                    text = "Nota: ${projectWithCalcs.project.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            budgetView?.let { view ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                BudgetSummary(
                    budget = view.budget,
                    laborConfigured = view.labor.isConfigured,
                    onConfigureLabor = onConfigureLabor
                )
                OutlinedButton(
                    onClick = onSharePdf,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("project_pdf_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF de la obra")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Text(
                text = "Muros calculados (${projectWithCalcs.calculations.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                projectWithCalcs.calculations.forEach { calc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            val elementBadge = when (calc.elementType) {
                                "CIELO_RASO_DRYWALL" -> "☁️ Cielo Raso Drywall"
                                "CIELO_RASO_PVC" -> "🪟 Cielo Raso PVC"
                                "MUEBLE_DRYWALL" -> "🛋️ Mueble Drywall"
                                else -> "🧱 Muro Drywall"
                            }

                            Text(
                                text = "${calc.wallName} ($elementBadge)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${NumberFormatUtils.formatDecimal(calc.lengthMeters, 1)}m x " +
                                    "${NumberFormatUtils.formatDecimal(calc.heightMeters, 1)}m " +
                                    "(${if (calc.faces == "BOTH_FACES") "2 Caras" else "1 Cara"}) • " +
                                    "${NumberFormatUtils.formatSquareMeters(calc.effectiveNetAreaM2)} netos" +
                                    if (projectWithCalcs.openingsFor(calc.id).isNotEmpty()) {
                                        " • ${projectWithCalcs.openingsFor(calc.id).size} vano(s)"
                                    } else {
                                        ""
                                    },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${calc.totalBoardsCount} placas/tablillas • " +
                                    "${calc.totalProfiles3mCount} perfiles 3m" +
                                    if (calc.totalPaintUnits > 0) " • ${calc.totalPaintUnits} envases pintura" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        IconButton(
                            onClick = { onDeleteCalculation(calc) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar muro",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = { onLoadCalculation(calc) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ver Cómputo")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyProjectsState(
    hasSearch: Boolean,
    onCreateNew: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Text(
                text = if (hasSearch) "No se encontraron obras coincidentes" else "Aún no tienes proyectos guardados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (hasSearch)
                    "Intenta con otro término de búsqueda o limpia el filtro."
                else
                    "Calcula los materiales de tu primer muro y guárdalo en la base de datos local para consultarlo cuando lo necesites.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (!hasSearch) {
                Button(
                    onClick = onCreateNew,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Calcular Primer Muro")
                }
            }
        }
    }
}
