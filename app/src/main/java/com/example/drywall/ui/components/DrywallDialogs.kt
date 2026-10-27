package com.example.drywall.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.drywall.data.local.entity.ProjectWithCalculations

/**
 * Diálogo para guardar el cómputo actual.
 *
 * Permite crear una obra nueva o agregar el muro a una obra existente, que es el flujo
 * real de una obra con varios muros.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveProjectDialog(
    defaultWallName: String,
    existingProjects: List<ProjectWithCalculations>,
    onDismiss: () -> Unit,
    onSaveAsNewProject: (projectName: String, client: String, notes: String, wallName: String) -> Unit,
    onAddToExistingProject: (projectId: Long, wallName: String) -> Unit
) {
    var projectName by remember { mutableStateOf("Obra Drywall") }
    var client by remember { mutableStateOf("") }
    var wallName by remember { mutableStateOf(defaultWallName) }
    var notes by remember { mutableStateOf("") }
    var addToExisting by remember { mutableStateOf(false) }
    var selectedProjectId by remember { mutableStateOf<Long?>(null) }

    val canAddToExisting = existingProjects.isNotEmpty()
    val existingMode = addToExisting && canAddToExisting
    val confirmEnabled = if (existingMode) selectedProjectId != null else projectName.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (existingMode) "Agregar Muro a una Obra" else "Guardar Obra en el Dispositivo")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (canAddToExisting) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !existingMode,
                            onClick = { addToExisting = false },
                            label = { Text("Nueva obra") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = existingMode,
                            onClick = { addToExisting = true },
                            label = { Text("Obra existente") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_mode_existing_chip")
                        )
                    }
                }

                if (existingMode) {
                    Text(
                        text = "Elige la obra que recibirá este muro:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        existingProjects.forEach { project ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedProjectId == project.project.id,
                                    onClick = { selectedProjectId = project.project.id }
                                )
                                Column {
                                    Text(
                                        text = project.project.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${project.calculations.size} muro(s) · " +
                                            "${project.totalBoards} placas",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = { projectName = it },
                        label = { Text("Nombre del Proyecto / Obra *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = client,
                        onValueChange = { client = it },
                        label = { Text("Cliente o Ubicación (opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = wallName,
                    onValueChange = { wallName = it },
                    label = { Text("Identificador del Muro (ej. Muro 1)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!existingMode) {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notas técnicas o recordatorios") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (existingMode) {
                        selectedProjectId?.let { projectId ->
                            onAddToExistingProject(projectId, wallName)
                        }
                    } else {
                        onSaveAsNewProject(projectName, client, notes, wallName)
                    }
                },
                enabled = confirmEnabled,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (existingMode) "Agregar muro" else "Guardar en SQLite")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
