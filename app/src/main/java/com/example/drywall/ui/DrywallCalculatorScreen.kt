package com.example.drywall.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.drywall.ui.screens.CalculationFormScreen
import com.example.drywall.ui.screens.CalculationResultsScreen
import com.example.drywall.ui.screens.ElementDashboardScreen
import com.example.drywall.ui.screens.PricesScreen
import com.example.drywall.ui.screens.ProjectsListScreen
import com.example.drywall.ui.viewmodel.AppScreen
import com.example.drywall.ui.viewmodel.DrywallCalculatorViewModel

/**
 * Contenedor principal de la aplicación DrywallCalc que gestiona la navegación entre
 * la Lista de Proyectos, el Formulario de Cálculo y la Pantalla de Resultados.
 */
@Composable
fun DrywallCalculatorScreen(
    viewModel: DrywallCalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userNotification) {
        uiState.userNotification?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                // Tab 1: Inicio / Dashboard (Tarjetas Visuales)
                NavigationBarItem(
                    selected = uiState.currentScreen == AppScreen.DASHBOARD || uiState.currentScreen == AppScreen.CALCULATOR,
                    onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentScreen == AppScreen.DASHBOARD || uiState.currentScreen == AppScreen.CALCULATOR) Icons.Filled.GridView else Icons.Outlined.GridView,
                            contentDescription = "Inicio"
                        )
                    },
                    label = { Text("Inicio") },
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )

                // Tab 2: Proyectos guardados
                NavigationBarItem(
                    selected = uiState.currentScreen == AppScreen.PROJECTS,
                    onClick = { viewModel.navigateTo(AppScreen.PROJECTS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (uiState.savedProjects.isNotEmpty()) {
                                    Badge { Text("${uiState.savedProjects.size}") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (uiState.currentScreen == AppScreen.PROJECTS) Icons.Filled.Folder else Icons.Outlined.Folder,
                                contentDescription = "Proyectos Guardados"
                            )
                        }
                    },
                    label = { Text("Proyectos") },
                    modifier = Modifier.testTag("nav_tab_projects")
                )

                // Tab 3: Catálogo de precios unitarios
                NavigationBarItem(
                    selected = uiState.currentScreen == AppScreen.PRICES,
                    onClick = { viewModel.navigateTo(AppScreen.PRICES) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentScreen == AppScreen.PRICES) {
                                Icons.Filled.Payments
                            } else {
                                Icons.Outlined.Payments
                            },
                            contentDescription = "Precios unitarios"
                        )
                    },
                    label = { Text("Precios") },
                    modifier = Modifier.testTag("nav_tab_prices")
                )

                // Tab 4: Desglose de resultados
                NavigationBarItem(
                    selected = uiState.currentScreen == AppScreen.RESULTS,
                    onClick = { viewModel.navigateTo(AppScreen.RESULTS) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentScreen == AppScreen.RESULTS) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                            contentDescription = "Resultados"
                        )
                    },
                    label = { Text("Resultados") },
                    modifier = Modifier.testTag("nav_tab_results")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = uiState.currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    AppScreen.PROJECTS -> {
                        ProjectsListScreen(viewModel = viewModel)
                    }
                    AppScreen.DASHBOARD -> {
                        ElementDashboardScreen(viewModel = viewModel)
                    }
                    AppScreen.CALCULATOR -> {
                        CalculationFormScreen(viewModel = viewModel)
                    }
                    AppScreen.RESULTS -> {
                        CalculationResultsScreen(viewModel = viewModel)
                    }
                    AppScreen.PRICES -> {
                        PricesScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
