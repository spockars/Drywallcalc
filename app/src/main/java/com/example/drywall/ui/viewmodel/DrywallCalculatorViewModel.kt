package com.example.drywall.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.drywall.data.local.DrywallDatabase
import com.example.drywall.data.local.entity.ProjectWithCalculations
import com.example.drywall.data.local.entity.WallCalculationEntity
import com.example.drywall.data.repository.DrywallRepository
import com.example.drywall.data.repository.DrywallRepositoryImpl
import com.example.drywall.domain.engine.CalculatorEngine
import com.example.drywall.domain.model.DrywallBoardType
import com.example.drywall.domain.model.MaterialCategory
import com.example.drywall.domain.model.WallCalculationInput
import com.example.drywall.domain.model.WallCalculationResult
import com.example.drywall.domain.model.WallFaces
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Pantallas principales de la aplicación.
 */
enum class AppScreen(val label: String) {
    PROJECTS("Proyectos"),
    CALCULATOR("Calculadora"),
    RESULTS("Resultados")
}

/**
 * Estado completo de la interfaz de usuario para la calculadora y persistencia de Drywall.
 */
data class DrywallCalculatorUiState(
    val currentScreen: AppScreen = AppScreen.CALCULATOR,
    val lengthInput: String = "4.0",
    val heightInput: String = "2.6",
    val faces: WallFaces = WallFaces.BOTH_FACES,
    val hasInsulation: Boolean = true,
    val studSpacingMeters: Double = 0.407,
    val boardType: DrywallBoardType = DrywallBoardType.ESTANDAR_ST,
    val wastePercentage: Double = 8.0,
    val calculationResult: WallCalculationResult? = null,
    val selectedCategoryFilter: MaterialCategory? = null,
    val errorMessage: String? = null,
    val isCalculating: Boolean = false,
    // Persistencia Room
    val savedProjects: List<ProjectWithCalculations> = emptyList(),
    val searchQuery: String = "",
    val showSaveDialog: Boolean = false,
    val showProjectsSheet: Boolean = false,
    val projectPendingDelete: ProjectWithCalculations? = null,
    val userNotification: String? = null
)


/**
 * ViewModel que expone el estado y los eventos para el cálculo de materiales y la persistencia local con Room.
 */
class DrywallCalculatorViewModel(
    private val engine: CalculatorEngine = CalculatorEngine(),
    private val repository: DrywallRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(DrywallCalculatorUiState())
    val uiState: StateFlow<DrywallCalculatorUiState> = _uiState.asStateFlow()

    init {
        // Cálculo inicial reactivo
        calculateMaterials()

        // Observa proyectos guardados desde Room si el repositorio está provisto
        observeSavedProjects()
    }

    private fun observeSavedProjects() {
        if (repository == null) return

        viewModelScope.launch {
            repository.allProjectsWithCalculations
                .catch { error ->
                    _uiState.update { it.copy(userNotification = "Error al leer base de datos: ${error.localizedMessage}") }
                }
                .collect { projects ->
                    _uiState.update { it.copy(savedProjects = projects) }
                }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setProjectPendingDelete(project: ProjectWithCalculations?) {
        _uiState.update { it.copy(projectPendingDelete = project) }
    }

    private fun String.parseDimension(): Double? =
        this.replace(',', '.').trim().toDoubleOrNull()

    fun adjustLengthBy(delta: Double) {
        val current = _uiState.value.lengthInput.parseDimension() ?: 4.0
        val next = (current + delta).coerceAtLeast(0.5)
        onLengthChanged(String.format(java.util.Locale.US, "%.1f", next))
    }

    fun adjustHeightBy(delta: Double) {
        val current = _uiState.value.heightInput.parseDimension() ?: 2.6
        val next = (current + delta).coerceAtLeast(0.5)
        onHeightChanged(String.format(java.util.Locale.US, "%.1f", next))
    }


    fun calculateAndGoToResults(): Boolean {
        calculateMaterials()
        val isValid = _uiState.value.calculationResult != null && _uiState.value.errorMessage == null
        if (isValid) {
            _uiState.update { it.copy(currentScreen = AppScreen.RESULTS) }
        }
        return isValid
    }

    fun onLengthChanged(newLength: String) {
        _uiState.update { it.copy(lengthInput = newLength, errorMessage = null) }
        autoCalculateIfValid()
    }

    fun onHeightChanged(newHeight: String) {
        _uiState.update { it.copy(heightInput = newHeight, errorMessage = null) }
        autoCalculateIfValid()
    }

    fun onFacesChanged(faces: WallFaces) {
        _uiState.update { it.copy(faces = faces) }
        calculateMaterials()
    }

    fun onInsulationToggled(hasInsulation: Boolean) {
        _uiState.update { it.copy(hasInsulation = hasInsulation) }
        calculateMaterials()
    }

    fun onStudSpacingChanged(spacingMeters: Double) {
        _uiState.update { it.copy(studSpacingMeters = spacingMeters) }
        calculateMaterials()
    }

    fun onBoardTypeChanged(type: DrywallBoardType) {
        _uiState.update { it.copy(boardType = type) }
        calculateMaterials()
    }

    fun onCategoryFilterSelected(category: MaterialCategory?) {
        _uiState.update { it.copy(selectedCategoryFilter = category) }
    }

    fun applyPreset(length: Double, height: Double, isTwoFaces: Boolean, withInsulation: Boolean) {
        _uiState.update {
            it.copy(
                lengthInput = length.toString(),
                heightInput = height.toString(),
                faces = if (isTwoFaces) WallFaces.BOTH_FACES else WallFaces.ONE_FACE,
                hasInsulation = withInsulation,
                errorMessage = null
            )
        }
        calculateMaterials()
    }

    fun setShowSaveDialog(show: Boolean) {
        _uiState.update { it.copy(showSaveDialog = show) }
    }

    fun setShowProjectsSheet(show: Boolean) {
        _uiState.update { it.copy(showProjectsSheet = show) }
    }

    fun clearNotification() {
        _uiState.update { it.copy(userNotification = null) }
    }

    /**
     * Guarda el cálculo actual en la base de datos Room.
     */
    fun saveCurrentProject(
        projectName: String,
        clientOrLocation: String,
        notes: String,
        wallName: String
    ) {
        val currentResult = _uiState.value.calculationResult
        if (currentResult == null) {
            _uiState.update { it.copy(errorMessage = "No hay un cálculo válido para guardar.") }
            return
        }

        val repo = repository
        if (repo == null) {
            _uiState.update { it.copy(userNotification = "Persistencia no configurada en este entorno.") }
            return
        }

        viewModelScope.launch {
            try {
                repo.saveProjectWithCalculation(
                    projectName = projectName,
                    clientOrLocation = clientOrLocation,
                    notes = notes,
                    calculationResult = currentResult,
                    wallName = wallName
                )
                _uiState.update {
                    it.copy(
                        showSaveDialog = false,
                        userNotification = "Proyecto \"$projectName\" guardado en el dispositivo"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(userNotification = "Error al guardar proyecto: ${e.localizedMessage}")
                }
            }
        }
    }

    /**
     * Carga un cálculo previamente guardado en el formulario y recalcula en vivo.
     */
    fun loadSavedCalculation(calc: WallCalculationEntity) {
        val faces = try {
            WallFaces.valueOf(calc.faces)
        } catch (_: Exception) {
            WallFaces.BOTH_FACES
        }

        val boardType = try {
            DrywallBoardType.valueOf(calc.boardType)
        } catch (_: Exception) {
            DrywallBoardType.ESTANDAR_ST
        }

        _uiState.update {
            it.copy(
                lengthInput = calc.lengthMeters.toString(),
                heightInput = calc.heightMeters.toString(),
                faces = faces,
                hasInsulation = calc.hasInsulation,
                studSpacingMeters = calc.studSpacingMeters,
                boardType = boardType,
                showProjectsSheet = false,
                currentScreen = AppScreen.RESULTS,
                userNotification = "Cálculo \"${calc.wallName}\" cargado"
            )
        }
        calculateMaterials()
    }


    /**
     * Elimina un proyecto completo y sus cálculos asociados por ID.
     */
    fun deleteProject(projectId: Long) {
        val repo = repository ?: return
        viewModelScope.launch {
            try {
                repo.deleteProject(projectId)
                _uiState.update { it.copy(userNotification = "Proyecto eliminado") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userNotification = "Error al eliminar: ${e.localizedMessage}") }
            }
        }
    }

    /**
     * Elimina un cálculo individual.
     */
    fun deleteCalculation(calculationId: Long) {
        val repo = repository ?: return
        viewModelScope.launch {
            try {
                repo.deleteCalculation(calculationId)
                _uiState.update { it.copy(userNotification = "Cómputo eliminado") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userNotification = "Error al eliminar: ${e.localizedMessage}") }
            }
        }
    }

    fun calculateMaterials() {
        val current = _uiState.value
        val length = current.lengthInput.parseDimension()
        val height = current.heightInput.parseDimension()

        if (length == null || length <= 0.0) {
            _uiState.update {
                it.copy(
                    errorMessage = "Por favor ingresa un largo válido mayor a 0 metros.",
                    calculationResult = null
                )
            }
            return
        }

        if (height == null || height <= 0.0) {
            _uiState.update {
                it.copy(
                    errorMessage = "Por favor ingresa una altura válida mayor a 0 metros.",
                    calculationResult = null
                )
            }
            return
        }

        val wasteFactor = current.wastePercentage / 100.0

        try {
            val input = WallCalculationInput(
                lengthMeters = length,
                heightMeters = height,
                faces = current.faces,
                hasInsulation = current.hasInsulation,
                studSpacingMeters = current.studSpacingMeters,
                boardType = current.boardType,
                wasteFactor = wasteFactor
            )

            val result = engine.calculate(input)

            _uiState.update {
                it.copy(
                    calculationResult = result,
                    errorMessage = null
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    errorMessage = e.localizedMessage ?: "Error al calcular materiales",
                    calculationResult = null
                )
            }
        }
    }

    private fun autoCalculateIfValid() {
        val length = _uiState.value.lengthInput.parseDimension()
        val height = _uiState.value.heightInput.parseDimension()
        if (length != null && length > 0.0 && height != null && height > 0.0) {
            calculateMaterials()
        } else {
            val error = when {
                _uiState.value.lengthInput.isBlank() -> "Por favor ingresa el largo del muro."
                length == null || length <= 0.0 -> "El largo del muro debe ser un número mayor a 0."
                _uiState.value.heightInput.isBlank() -> "Por favor ingresa la altura del muro."
                height == null || height <= 0.0 -> "La altura del muro debe ser un número mayor a 0."
                else -> null
            }
            _uiState.update {
                it.copy(
                    errorMessage = error,
                    calculationResult = null
                )
            }
        }
    }
}

/**
 * Factory para instanciar el ViewModel inyectando la base de datos Room.
 */
class DrywallCalculatorViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = DrywallDatabase.getInstance(application)
        val repository = DrywallRepositoryImpl(database.projectDao())
        @Suppress("UNCHECKED_CAST")
        return DrywallCalculatorViewModel(
            engine = CalculatorEngine(),
            repository = repository
        ) as T
    }
}
