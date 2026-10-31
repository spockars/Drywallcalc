package com.example.drywall.ui.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Initial Dashboard (Home Cards)", showBackground = true, widthDp = 390, heightDp = 800)
@Composable
fun InitialDashboardPreview() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF2563EB),
            secondary = Color(0xFFF59E0B),
            surface = Color(0xFFFAFAFA),
            background = Color(0xFFF1F5F9)
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Architecture, contentDescription = null, tint = Color.White)
                            }
                            Text("DrywallCalc Pro", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) { Icon(Icons.Default.Notifications, contentDescription = null) }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = true,
                        onClick = {},
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Inicio") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
                        label = { Text("Proyectos") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = { Icon(Icons.Default.Build, contentDescription = null) },
                        label = { Text("Precios") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = { Icon(Icons.Default.Share, contentDescription = null) },
                        label = { Text("Reportes") }
                    )
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Greeting Banner
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("¡Hola, Constructor! 👋", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Selecciona el tipo de elemento que deseas calcular hoy para iniciar el presupuesto.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        }
                    }
                }

                item {
                    Text("Elementos de Construcción", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                // Grid or Large Cards for elements
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Card 1: Muro Drywall
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(160.dp),
                            shape = RoundedCornerShape(16.dp),
                            onClick = {}
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ViewAgenda, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                                Column {
                                    Text("Muro Drywall", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Placas, studs y aislamientos", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }

                        // Card 2: Cielo Raso Drywall
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(160.dp),
                            shape = RoundedCornerShape(16.dp),
                            onClick = {}
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                }
                                Column {
                                    Text("Cielo Raso", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Suspensión y placas", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Card 3: Cielo PVC
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(160.dp),
                            shape = RoundedCornerShape(16.dp),
                            onClick = {}
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF10B981).copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.GridOn, contentDescription = null, tint = Color(0xFF10B981))
                                }
                                Column {
                                    Text("Cielo PVC", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Llamas y tablillas PVC", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }

                        // Card 4: Mueble Drywall
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(160.dp),
                            shape = RoundedCornerShape(16.dp),
                            onClick = {}
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF8B5CF6).copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Kitchen, contentDescription = null, tint = Color(0xFF8B5CF6))
                                }
                                Column {
                                    Text("Mueble / Repisa", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Divisiones y nichos", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
