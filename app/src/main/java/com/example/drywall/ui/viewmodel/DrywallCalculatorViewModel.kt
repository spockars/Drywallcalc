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
import com.example.drywall.domain.model.OpeningType
import com.example.drywall.data.local.entity.WallWithOpenings
import com.example.drywall.domain.engine.PaintCalculator
import com.example.drywall.domain.model.PaintProduct
import com.example.drywall.domain.model.PaintSpec
import com.example.drywall.domain.model.PaintUnit
import com.example.drywall.domain.model.WallOpening
import com.example.drywall.util.NumberFormatUtils
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
 * Borrador editable de un vano. Los campos se guardan como texto porque el usuario
 * está escribiendo; la conversión a [WallOpening] ocurre al calcular.
 */
data class WallOpeningDraft(
    val id: Long,
    val type: OpeningType = OpeningType.PUERTA,
    val widthInput: String = "",
    val heightInput: String = ""
)

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
    val openingDrafts: List<WallOpeningDraft> = emptyList(),
    val openingsValidationErrors: List<String> = emptyList(),
    // Pintura (calculador separado del motor de drywall)
    val paintEnabled: Boolean = false,
    val paintProductNameInput: String = "",
    val paintUnit: PaintUnit = PaintUnit.GALON,
    val paintCoverageInput: String = "",
    val paintFinishCoatsInput: String = "2",
    val paintWastePercentInput: String = "10",
    val paintValidationErrors: List<String> = emptyList(),
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
    val calculationPendingDelete: WallCalculationEntity? = null,
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
    private var nextOpeningId: Long = 1L
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

    /** Marca un muro suelto para eliminar con confirmación. */
    fun setCalculationPendingDelete(wall: WallCalculationEntity?) {
        _uiState.update { it.copy(calculationPendingDelete = wall) }
    }

    /**
     * Agrega el cómputo actual como un muro adicional de una obra ya existente.
     */
    fun addCurrentCalculationToProject(projectId: Long, wallName: String) {
        val currentResult = _uiState.value.calculationResult
        if (currentResult == null) {
            _uiState.update { it.copy(errorMessage = "No hay un cálculo válido para agregar a la obra.") }
            return
        }

        val repo = repository
        if (repo == null) {
            _uiState.update { it.copy(userNotification = "Persistencia no configurada en este entorno.") }
            return
        }

        viewModelScope.launch {
            try {
                repo.addCalculationToProject(
                    projectId = projectId,
                    wallName = wallName,
                    calculationResult = currentResult
                )
                _uiState.update {
                    it.copy(
                        showSaveDialog = false,
                        userNotification = "Muro \"$wallName\" agregado a la obra"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(userNotification = "Error al agregar el muro: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun String.parseDimension(): Double? =
        this.replace(',', '.').trim().toDoubleOrNull()

    /** Formatea un número para volver a mostrarlo en un campo de texto, sin ceros sobrantes. */
    private fun Double.toInputString(): String {
        val formatted = NumberFormatUtils.formatDecimal(this, 2)
        return formatted.trimEnd('0').trimEnd('.')
    }

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
    fun loadSavedCalculation(wall: WallWithOpenings) {
        val calc = wall.wall
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
                lengthInput = calc.lengthMeters.toInputString(),
                heightInput = calc.heightMeters.toInputString(),
                faces = faces,
                hasInsulation = calc.hasInsulation,
                studSpacingMeters = calc.studSpacingMeters,
                boardType = boardType,
                wastePercentage = calc.wasteFactor * 100.0,
                paintEnabled = calc.paintEnabled,
                paintProductNameInput = calc.paintProductName,
                paintUnit = runCatching { PaintUnit.valueOf(calc.paintUnit) }
                    .getOrDefault(PaintUnit.GALON),
                paintCoverageInput = if (calc.paintCoverageM2PerUnit > 0.0) {
                    NumberFormatUtils.formatDecimal(calc.paintCoverageM2PerUnit)
                } else {
                    ""
                },
                paintFinishCoatsInput = calc.paintFinishCoats.toString(),
                paintWastePercentInput = NumberFormatUtils.formatDecimal(calc.paintWasteFactor * 100.0, 0),
                showProjectsSheet = false,
                openingDrafts = wall.openings.map { opening ->
                    WallOpeningDraft(
                        id = nextOpeningId++,
                        type = runCatching { OpeningType.valueOf(opening.type) }
                            .getOrDefault(OpeningType.PUERTA),
                        widthInput = NumberFormatUtils.formatDecimal(opening.widthMeters),
                        heightInput = NumberFormatUtils.formatDecimal(opening.heightMeters)
                    )
                },
                openingsValidationErrors = emptyList(),
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

    /** Vanos en edición convertidos a dominio, junto con los errores de captura. */
    private data class OpeningsResolution(
        val openings: List<WallOpening>,
        val errors: List<String>
    )

    /**
     * Convierte los borradores de vanos en [WallOpening] y acumula los mensajes de captura.
     */
    private fun resolveOpenings(drafts: List<WallOpeningDraft>): OpeningsResolution {
        val errors = mutableListOf<String>()
        val openings = mutableListOf<WallOpening>()

        drafts.forEachIndexed { index, draft ->
            val position = index + 1
            val width = draft.widthInput.parseDimension()
            val height = draft.heightInput.parseDimension()

            if (width == null || width <= 0.0) {
                errors += "Vano $position (${draft.type.label}): ingresa un ancho mayor a 0 m."
            }
            if (height == null || height <= 0.0) {
                errors += "Vano $position (${draft.type.label}): ingresa un alto mayor a 0 m."
            }
            if (width != null && width > 0.0 && height != null && height > 0.0) {
                openings += WallOpening(
                    type = draft.type,
                    widthMeters = width,
                    heightMeters = height
                )
            }
        }

        return OpeningsResolution(openings = openings, errors = errors)
    }

    /** Agrega un vano vacío al muro en edición. */
    fun addOpening() {
        _uiState.update {
            it.copy(openingDrafts = it.openingDrafts + WallOpeningDraft(id = nextOpeningId++))
        }
        refreshOpeningsValidation()
    }

    /** Quita un vano por su identificador de borrador. */
    fun removeOpening(openingId: Long) {
        _uiState.update { state ->
            state.copy(openingDrafts = state.openingDrafts.filterNot { it.id == openingId })
        }
        refreshOpeningsValidation()
    }

    /** Cambia el tipo (puerta o ventana) de un vano. */
    fun updateOpeningType(openingId: Long, type: OpeningType) {
        updateOpeningDraft(openingId) { it.copy(type = type) }
    }

    /** Actualiza el ancho en metros (texto) de un vano. */
    fun updateOpeningWidth(openingId: Long, width: String) {
        updateOpeningDraft(openingId) { it.copy(widthInput = width) }
    }

    /** Actualiza el alto en metros (texto) de un vano. */
    fun updateOpeningHeight(openingId: Long, height: String) {
        updateOpeningDraft(openingId) { it.copy(heightInput = height) }
    }

    /** Elimina todos los vanos y vuelve al comportamiento de muro ciego. */
    fun clearOpenings() {
        _uiState.update {
            it.copy(openingDrafts = emptyList(), openingsValidationErrors = emptyList())
        }
        calculateMaterials()
    }

    private fun updateOpeningDraft(openingId: Long, transform: (WallOpeningDraft) -> WallOpeningDraft) {
        _uiState.update { state ->
            state.copy(
                openingDrafts = state.openingDrafts.map { draft ->
                    if (draft.id == openingId) transform(draft) else draft
                }
            )
        }
        refreshOpeningsValidation()
    }

    private fun refreshOpeningsValidation() {
        val state = _uiState.value
        val length = state.lengthInput.parseDimension()
        val height = state.heightInput.parseDimension()
        if (length != null && length > 0.0 && height != null && height > 0.0) {
            calculateMaterials()
        } else {
            val resolution = resolveOpenings(state.openingDrafts)
            _uiState.update { it.copy(openingsValidationErrors = resolution.errors) }
        }
    }

    /** Construye la configuración de pintura a partir de los campos de la UI. */
    private fun buildPaintSpec(state: DrywallCalculatorUiState): PaintSpec {
        val coverage = state.paintCoverageInput.parseDimension() ?: 0.0
        val finishCoats = state.paintFinishCoatsInput.trim().toIntOrNull()
            ?: PaintSpec.DEFAULT_FINISH_COATS
        val wastePercent = state.paintWastePercentInput.parseDimension()
            ?: (PaintSpec.DEFAULT_WASTE_FACTOR * 100.0)
        return PaintSpec(
            finishCoats = finishCoats,
            wasteFactor = (wastePercent / 100.0).coerceAtLeast(0.0),
            product = PaintProduct(
                name = state.paintProductNameInput.trim(),
                unit = state.paintUnit,
                coverageM2PerUnit = coverage
            )
        )
    }

    /** Activa o desactiva el cálculo de pintura del muro. */
    fun onPaintEnabledToggled(enabled: Boolean) {
        _uiState.update { it.copy(paintEnabled = enabled, paintValidationErrors = emptyList()) }
        calculateMaterials()
    }

    fun onPaintProductNameChanged(value: String) {
        _uiState.update { it.copy(paintProductNameInput = value) }
        calculateMaterials()
    }

    fun onPaintUnitChanged(unit: PaintUnit) {
        _uiState.update { it.copy(paintUnit = unit) }
        calculateMaterials()
    }

    fun onPaintCoverageChanged(value: String) {
        _uiState.update { it.copy(paintCoverageInput = value) }
        calculateMaterials()
    }

    fun onPaintFinishCoatsChanged(value: String) {
        _uiState.update { it.copy(paintFinishCoatsInput = value) }
        calculateMaterials()
    }

    fun onPaintWasteChanged(value: String) {
        _uiState.update { it.copy(paintWastePercentInput = value) }
        calculateMaterials()
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

        val openingsResolution = resolveOpenings(current.openingDrafts)
        val openings = openingsResolution.openings
        val draftErrors = openingsResolution.errors

        try {
            val input = WallCalculationInput(
                lengthMeters = length,
                heightMeters = height,
                faces = current.faces,
                hasInsulation = current.hasInsulation,
                studSpacingMeters = current.studSpacingMeters,
                boardType = current.boardType,
                wasteFactor = wasteFactor,
                openings = openings
            )

            val openingErrors = draftErrors + input.validateOpenings()
            if (openingErrors.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        calculationResult = null,
                        openingsValidationErrors = openingErrors,
                        errorMessage = openingErrors.first()
                    )
                }
                return
            }

            val baseResult = engine.calculate(input)

            val paintSpec = buildPaintSpec(current)
            val paintErrors = if (current.paintEnabled) {
                PaintCalculator.validate(baseResult.netTotalDrywallArea, paintSpec)
            } else {
                emptyList()
            }
            val paintEstimate = if (current.paintEnabled && paintErrors.isEmpty()) {
                runCatching {
                    PaintCalculator.estimate(baseResult.netTotalDrywallArea, paintSpec)
                }.getOrNull()
            } else {
                null
            }

            _uiState.update {
                it.copy(
                    calculationResult = baseResult.copy(paint = paintEstimate),
                    errorMessage = null,
                    openingsValidationErrors = emptyList(),
                    paintValidationErrors = paintErrors
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
