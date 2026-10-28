package com.example.drywall.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.drywall.data.local.DrywallDatabase
import com.example.drywall.data.local.entity.ProjectWithCalculations
import com.example.drywall.data.local.entity.WallCalculationEntity
import com.example.drywall.data.local.entity.WallWithOpenings
import com.example.drywall.data.mapper.ProjectBudgetView
import com.example.drywall.data.mapper.toBudgetView
import com.example.drywall.data.mapper.toEntity
import com.example.drywall.data.mapper.toLabor
import com.example.drywall.data.repository.DrywallRepository
import com.example.drywall.data.repository.DrywallRepositoryImpl
import com.example.drywall.domain.engine.ALL_DRYWALL_MATERIALS
import com.example.drywall.domain.engine.CalculatorEngine
import com.example.drywall.domain.engine.CostCalculator
import com.example.drywall.domain.engine.PaintCalculator
import com.example.drywall.domain.model.ConstructionElementType
import com.example.drywall.domain.model.DrywallBoardType
import com.example.drywall.domain.model.DrywallCeilingInput
import com.example.drywall.domain.model.FurnitureCalculationInput
import com.example.drywall.domain.model.LaborMode
import com.example.drywall.domain.model.MaterialCategory
import com.example.drywall.domain.model.OpeningType
import com.example.drywall.domain.model.PaintProduct
import com.example.drywall.domain.model.PaintRole
import com.example.drywall.domain.model.PaintSpec
import com.example.drywall.domain.model.PaintUnit
import com.example.drywall.domain.model.PriceCatalogItem
import com.example.drywall.domain.model.PriceEntry
import com.example.drywall.domain.model.PriceKeys
import com.example.drywall.domain.model.ProjectLabor
import com.example.drywall.domain.model.PvcCeilingInput
import com.example.drywall.domain.model.WallCalculationInput
import com.example.drywall.domain.model.WallCalculationResult
import com.example.drywall.domain.model.WallCost
import com.example.drywall.domain.model.WallFaces
import com.example.drywall.domain.model.WallOpening
import com.example.drywall.util.NumberFormatUtils
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
    RESULTS("Resultados"),
    PRICES("Precios")
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
    val selectedElementType: ConstructionElementType = ConstructionElementType.MURO_DRYWALL,
    val elementNameInput: String = "Elemento 1",
    // Form Inputs - Muro
    val lengthInput: String = "4.0",
    val heightInput: String = "2.6",
    val faces: WallFaces = WallFaces.BOTH_FACES,
    val hasInsulation: Boolean = true,
    val studSpacingMeters: Double = 0.407,
    val boardType: DrywallBoardType = DrywallBoardType.ESTANDAR_ST,
    val wastePercentage: Double = 8.0,
    val openingDrafts: List<WallOpeningDraft> = emptyList(),
    val openingsValidationErrors: List<String> = emptyList(),
    // Form Inputs - Cielo Raso Drywall
    val ceilingWidthInput: String = "3.0",
    val suspensionHeightInput: String = "0.30",
    val mainBeamSpacingMeters: Double = 0.80,
    // Form Inputs - Cielo Raso PVC
    val pvcSlatWidthInput: String = "0.25",
    val pvcCommercialSlatLengthInput: String = "5.95",
    val pvcSupportSpacingMeters: Double = 0.50,
    // Form Inputs - Mueble Drywall
    val furnitureWidthInput: String = "2.0",
    val furnitureHeightInput: String = "1.8",
    val furnitureDepthInput: String = "0.35",
    val horizontalShelvesCountInput: String = "2",
    val verticalDividersCountInput: String = "1",
    // Pintura (calculador separado del motor de drywall)
    val paintEnabled: Boolean = false,
    val paintProductNameInput: String = "",
    val paintUnit: PaintUnit = PaintUnit.GALON,
    val paintCoverageInput: String = "",
    val paintFinishCoatsInput: String = "2",
    val paintWastePercentInput: String = "10",
    val paintValidationErrors: List<String> = emptyList(),
    // Costos y presupuesto
    val priceCatalog: List<PriceCatalogItem> = emptyList(),
    val prices: Map<String, PriceEntry> = emptyMap(),
    val priceDrafts: Map<String, String> = emptyMap(),
    val currentWallCost: WallCost? = null,
    val projectBudgets: Map<Long, ProjectBudgetView> = emptyMap(),
    val laborProjectId: Long? = null,
    val laborEnabledDraft: Boolean = false,
    val laborModeDraft: LaborMode = LaborMode.PER_M2,
    val laborAmountDraft: String = "",
    val calculationResult: WallCalculationResult? = null,
    val selectedCategoryFilter: MaterialCategory? = null,
    val errorMessage: String? = null,
    val isCalculating: Boolean = false,
    // Persistencia Room
    val savedProjects: List<ProjectWithCalculations> = emptyList(),
    val searchQuery: String = "",
    val showSaveDialog: Boolean = false,
    val showCreateProjectDialog: Boolean = false,
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
        _uiState.update { it.copy(priceCatalog = buildPriceCatalog()) }
        calculateMaterials()

        // Observa proyectos guardados desde Room si el repositorio está provisto
        observeSavedProjects()
        observePrices()
    }

    /** Catálogo de insumos cotizables, con su unidad comercial. */
    private fun buildPriceCatalog(): List<PriceCatalogItem> {
        val materials = ALL_DRYWALL_MATERIALS.map { material ->
            PriceCatalogItem(
                itemKey = PriceKeys.material(material.id),
                name = material.name,
                unitLabel = material.defaultUnit.singularName,
                categoryLabel = material.category.displayName
            )
        }
        val paint = PaintRole.entries.flatMap { role ->
            PaintUnit.entries.map { unit ->
                PriceCatalogItem(
                    itemKey = PriceKeys.paint(role, unit),
                    name = "Pintura ${role.label.lowercase()}",
                    unitLabel = unit.label.lowercase(),
                    categoryLabel = "Pintura"
                )
            }
        }
        return materials + paint
    }

    private fun observePrices() {
        val repo = repository ?: return
        viewModelScope.launch {
            repo.materialPrices
                .catch { error ->
                    _uiState.update {
                        it.copy(userNotification = "Error al leer precios: ${error.localizedMessage}")
                    }
                }
                .collect { entries ->
                    val map = entries.associateBy { it.itemKey }
                    _uiState.update { state ->
                        val seeded = state.priceDrafts.toMutableMap()
                        map.forEach { (key, entry) ->
                            if (!seeded.containsKey(key)) {
                                seeded[key] = if (entry.unitPrice > 0.0) {
                                    NumberFormatUtils.formatDecimal(entry.unitPrice, 0)
                                } else {
                                    ""
                                }
                            }
                        }
                        state.copy(prices = map, priceDrafts = seeded)
                    }
                    recomputeCosts()
                }
        }
    }

    /** Recalcula costo del muro en pantalla y presupuesto de cada obra con los precios vigentes. */
    private fun recomputeCosts() {
        val state = _uiState.value
        val wallCost = state.calculationResult?.let { result ->
            CostCalculator.buildWallCost(result, state.prices)
        }
        val budgets = state.savedProjects.associate { project ->
            project.project.id to project.toBudgetView(engine, state.prices)
        }
        _uiState.update { it.copy(currentWallCost = wallCost, projectBudgets = budgets) }
    }

    /** Actualiza el texto de un precio sin guardarlo todavía. */
    fun onPriceDraftChanged(itemKey: String, value: String) {
        _uiState.update { it.copy(priceDrafts = it.priceDrafts + (itemKey to value)) }
    }

    /** Limpia un precio (lo deja sin cotizar). */
    fun onPriceCleared(itemKey: String) {
        _uiState.update { it.copy(priceDrafts = it.priceDrafts + (itemKey to "")) }
    }

    /** Guarda en Room todo el catálogo de precios. */
    fun savePrices() {
        val repo = repository
        if (repo == null) {
            _uiState.update { it.copy(userNotification = "Persistencia no configurada en este entorno.") }
            return
        }

        val state = _uiState.value
        val entries = state.priceCatalog.map { item ->
            val raw = state.priceDrafts[item.itemKey].orEmpty().replace(',', '.').trim()
            val price = raw.toDoubleOrNull() ?: 0.0
            PriceEntry(
                itemKey = item.itemKey,
                unitPrice = if (price > 0.0) price else 0.0,
                unitLabel = item.unitLabel
            )
        }

        viewModelScope.launch {
            try {
                repo.saveMaterialPrices(entries)
                _uiState.update {
                    it.copy(userNotification = "Precios unitarios guardados")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(userNotification = "Error al guardar precios: ${e.localizedMessage}")
                }
            }
        }
    }

    /** Abre la edición de mano de obra de una obra. */
    fun openLaborDialog(projectId: Long) {
        val project = _uiState.value.savedProjects.firstOrNull { it.project.id == projectId }
            ?: return
        val labor = project.toLabor()
        _uiState.update {
            it.copy(
                laborProjectId = projectId,
                laborEnabledDraft = labor.enabled,
                laborModeDraft = labor.mode,
                laborAmountDraft = if (labor.amount > 0.0) {
                    NumberFormatUtils.formatDecimal(labor.amount, 0)
                } else {
                    ""
                }
            )
        }
    }

    fun dismissLaborDialog() {
        _uiState.update { it.copy(laborProjectId = null) }
    }

    fun onLaborEnabledChanged(enabled: Boolean) {
        _uiState.update { it.copy(laborEnabledDraft = enabled) }
    }

    fun onLaborModeChanged(mode: LaborMode) {
        _uiState.update { it.copy(laborModeDraft = mode) }
    }

    fun onLaborAmountChanged(value: String) {
        _uiState.update { it.copy(laborAmountDraft = value) }
    }

    /** Guarda la mano de obra de la obra abierta en el diálogo. */
    fun saveLabor() {
        val projectId = _uiState.value.laborProjectId ?: return
        val repo = repository
        if (repo == null) {
            _uiState.update { it.copy(userNotification = "Persistencia no configurada en este entorno.") }
            return
        }

        val state = _uiState.value
        val amount = state.laborAmountDraft.replace(',', '.').trim().toDoubleOrNull() ?: 0.0
        val labor = ProjectLabor(
            enabled = state.laborEnabledDraft,
            mode = state.laborModeDraft,
            amount = if (amount > 0.0) amount else 0.0
        )

        viewModelScope.launch {
            try {
                repo.updateProjectLabor(projectId, labor)
                _uiState.update {
                    it.copy(
                        laborProjectId = null,
                        userNotification = "Mano de obra actualizada"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(userNotification = "Error al guardar mano de obra: ${e.localizedMessage}")
                }
            }
        }
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
                    recomputeCosts()
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
     * Agrega el cómputo actual como un elemento adicional de una obra ya existente.
     */
    fun addCurrentCalculationToProject(projectId: Long, wallName: String) {
        val state = _uiState.value
        val currentResult = state.calculationResult
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
                val entity = buildElementEntity(projectId, wallName, state, currentResult)
                repo.addCalculationEntityToProject(entity)
                _uiState.update {
                    it.copy(
                        showSaveDialog = false,
                        userNotification = "Elemento \"${entity.wallName}\" agregado a la obra"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(userNotification = "Error al agregar el elemento: ${e.localizedMessage}")
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
        val state = _uiState.value
        val currentResult = state.calculationResult
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
                val projectId = repo.createEmptyProject(projectName, clientOrLocation, notes)
                val entity = buildElementEntity(projectId, wallName, state, currentResult)
                repo.addCalculationEntityToProject(entity)
                _uiState.update {
                    it.copy(
                        showSaveDialog = false,
                        userNotification = "Obra \"$projectName\" guardada en el dispositivo"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(userNotification = "Error al guardar proyecto: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun buildElementEntity(
        projectId: Long,
        elementName: String,
        state: DrywallCalculatorUiState,
        result: WallCalculationResult
    ): WallCalculationEntity {
        val now = System.currentTimeMillis()
        val name = elementName.ifBlank {
            when (state.selectedElementType) {
                ConstructionElementType.MURO_DRYWALL -> "Muro 1"
                ConstructionElementType.CIELO_RASO_DRYWALL -> "Cielo Raso Drywall 1"
                ConstructionElementType.CIELO_RASO_PVC -> "Cielo Raso PVC 1"
                ConstructionElementType.MUEBLE_DRYWALL -> "Mueble Drywall 1"
            }
        }

        return when (state.selectedElementType) {
            ConstructionElementType.CIELO_RASO_DRYWALL -> {
                val len = state.lengthInput.parseDimension() ?: 4.0
                val wid = state.ceilingWidthInput.parseDimension() ?: 3.0
                val susp = state.suspensionHeightInput.parseDimension() ?: 0.30
                WallCalculationEntity(
                    projectId = projectId,
                    wallName = name,
                    lengthMeters = len,
                    heightMeters = wid,
                    faces = "ONE_FACE",
                    hasInsulation = state.hasInsulation,
                    studSpacingMeters = state.studSpacingMeters,
                    boardType = state.boardType.name,
                    wasteFactor = state.wastePercentage / 100.0,
                    totalDrywallAreaM2 = len * wid,
                    netAreaM2 = len * wid,
                    totalBoardsCount = result.totalBoardsRequired,
                    totalProfiles3mCount = result.totalProfiles3mRequired,
                    summaryNotes = "Cielo Raso Drywall: ${(len * wid).format(1)} m²",
                    createdAt = now,
                    elementType = ConstructionElementType.CIELO_RASO_DRYWALL.name,
                    suspensionHeightMeters = susp,
                    mainBeamSpacingMeters = state.mainBeamSpacingMeters,
                    paintEnabled = state.paintEnabled,
                    paintFinishCoats = state.paintFinishCoatsInput.trim().toIntOrNull() ?: 2,
                    paintWasteFactor = (state.paintWastePercentInput.parseDimension() ?: 10.0) / 100.0,
                    paintProductName = state.paintProductNameInput,
                    paintUnit = state.paintUnit.name,
                    paintCoverageM2PerUnit = state.paintCoverageInput.parseDimension() ?: 0.0,
                    paintAreaM2 = result.paint?.areaM2 ?: 0.0,
                    paintSealerUnits = result.paint?.sealerUnits ?: 0,
                    paintFinishUnits = result.paint?.finishUnits ?: 0
                )
            }
            ConstructionElementType.CIELO_RASO_PVC -> {
                val len = state.lengthInput.parseDimension() ?: 4.0
                val wid = state.ceilingWidthInput.parseDimension() ?: 3.0
                val slatW = state.pvcSlatWidthInput.parseDimension() ?: 0.25
                val slatL = state.pvcCommercialSlatLengthInput.parseDimension() ?: 5.95
                WallCalculationEntity(
                    projectId = projectId,
                    wallName = name,
                    lengthMeters = len,
                    heightMeters = wid,
                    faces = "ONE_FACE",
                    hasInsulation = false,
                    studSpacingMeters = state.pvcSupportSpacingMeters,
                    boardType = "PVC",
                    wasteFactor = state.wastePercentage / 100.0,
                    totalDrywallAreaM2 = len * wid,
                    netAreaM2 = len * wid,
                    totalBoardsCount = result.totalBoardsRequired,
                    totalProfiles3mCount = result.totalProfiles3mRequired,
                    summaryNotes = "Cielo Raso PVC: ${(len * wid).format(1)} m²",
                    createdAt = now,
                    elementType = ConstructionElementType.CIELO_RASO_PVC.name,
                    slatWidthMeters = slatW,
                    commercialSlatLengthMeters = slatL
                )
            }
            ConstructionElementType.MUEBLE_DRYWALL -> {
                val w = state.furnitureWidthInput.parseDimension() ?: 2.0
                val h = state.furnitureHeightInput.parseDimension() ?: 1.8
                val d = state.furnitureDepthInput.parseDimension() ?: 0.35
                val shelves = state.horizontalShelvesCountInput.trim().toIntOrNull() ?: 2
                val dividers = state.verticalDividersCountInput.trim().toIntOrNull() ?: 1
                WallCalculationEntity(
                    projectId = projectId,
                    wallName = name,
                    lengthMeters = w,
                    heightMeters = h,
                    faces = "ONE_FACE",
                    hasInsulation = false,
                    studSpacingMeters = 0.407,
                    boardType = state.boardType.name,
                    wasteFactor = state.wastePercentage / 100.0,
                    totalDrywallAreaM2 = w * h,
                    netAreaM2 = w * h,
                    totalBoardsCount = result.totalBoardsRequired,
                    totalProfiles3mCount = result.totalProfiles3mRequired,
                    summaryNotes = "Mueble Drywall: ${w.format(1)}x${h.format(1)}x${d.format(1)} m",
                    createdAt = now,
                    elementType = ConstructionElementType.MUEBLE_DRYWALL.name,
                    depthMeters = d,
                    horizontalShelvesCount = shelves,
                    verticalDividersCount = dividers
                )
            }
            else -> {
                result.toEntity(
                    projectId = projectId,
                    wallName = name,
                    createdAt = now
                )
            }
        }
    }

    private fun Double.format(decimals: Int): String = NumberFormatUtils.formatDecimal(this, decimals)

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

    fun onElementTypeSelected(type: ConstructionElementType) {
        _uiState.update { it.copy(selectedElementType = type, errorMessage = null) }
        calculateMaterials()
    }

    fun onCeilingWidthChanged(value: String) {
        _uiState.update { it.copy(ceilingWidthInput = value, errorMessage = null) }
        calculateMaterials()
    }

    fun onSuspensionHeightChanged(value: String) {
        _uiState.update { it.copy(suspensionHeightInput = value, errorMessage = null) }
        calculateMaterials()
    }

    fun onPvcSlatWidthChanged(value: String) {
        _uiState.update { it.copy(pvcSlatWidthInput = value, errorMessage = null) }
        calculateMaterials()
    }

    fun onPvcCommercialSlatLengthChanged(value: String) {
        _uiState.update { it.copy(pvcCommercialSlatLengthInput = value, errorMessage = null) }
        calculateMaterials()
    }

    fun onFurnitureWidthChanged(value: String) {
        _uiState.update { it.copy(furnitureWidthInput = value, errorMessage = null) }
        calculateMaterials()
    }

    fun onFurnitureHeightChanged(value: String) {
        _uiState.update { it.copy(furnitureHeightInput = value, errorMessage = null) }
        calculateMaterials()
    }

    fun onFurnitureDepthChanged(value: String) {
        _uiState.update { it.copy(furnitureDepthInput = value, errorMessage = null) }
        calculateMaterials()
    }

    fun onHorizontalShelvesCountChanged(value: String) {
        _uiState.update { it.copy(horizontalShelvesCountInput = value, errorMessage = null) }
        calculateMaterials()
    }

    fun onVerticalDividersCountChanged(value: String) {
        _uiState.update { it.copy(verticalDividersCountInput = value, errorMessage = null) }
        calculateMaterials()
    }

    fun onElementNameChanged(value: String) {
        _uiState.update { it.copy(elementNameInput = value) }
    }

    fun setShowCreateProjectDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateProjectDialog = show) }
    }

    fun createEmptyProject(projectName: String, clientOrLocation: String, notes: String) {
        val repo = repository ?: return
        viewModelScope.launch {
            try {
                repo.createEmptyProject(projectName, clientOrLocation, notes)
                _uiState.update {
                    it.copy(
                        showCreateProjectDialog = false,
                        userNotification = "Obra \"$projectName\" creada"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(userNotification = "Error al crear obra: ${e.localizedMessage}")
                }
            }
        }
    }

    fun calculateMaterials() {
        val current = _uiState.value
        val wasteFactor = current.wastePercentage / 100.0

        try {
            when (current.selectedElementType) {
                ConstructionElementType.CIELO_RASO_DRYWALL -> {
                    val length = current.lengthInput.parseDimension()
                    val width = current.ceilingWidthInput.parseDimension()
                    val suspHeight = current.suspensionHeightInput.parseDimension() ?: 0.30

                    if (length == null || length <= 0.0 || width == null || width <= 0.0) {
                        _uiState.update {
                            it.copy(
                                errorMessage = "Ingresa dimensiones válidas para el techo (largo y ancho mayores a 0 m).",
                                calculationResult = null
                            )
                        }
                        return
                    }

                    val ceilingInput = DrywallCeilingInput(
                        lengthMeters = length,
                        widthMeters = width,
                        suspensionHeightMeters = suspHeight,
                        omegaSpacingMeters = current.studSpacingMeters,
                        mainBeamSpacingMeters = current.mainBeamSpacingMeters,
                        boardType = current.boardType,
                        hasInsulation = current.hasInsulation,
                        wasteFactor = wasteFactor
                    )

                    val reqs = engine.calculateDrywallCeiling(ceilingInput)
                    val dummyWallInput = WallCalculationInput(
                        lengthMeters = length,
                        heightMeters = width,
                        wasteFactor = wasteFactor
                    )

                    val paintSpec = buildPaintSpec(current)
                    val paintEstimate = if (current.paintEnabled) {
                        runCatching { PaintCalculator.estimate(ceilingInput.areaM2, paintSpec) }.getOrNull()
                    } else null

                    _uiState.update {
                        it.copy(
                            calculationResult = WallCalculationResult(
                                input = dummyWallInput,
                                requirements = reqs,
                                paint = paintEstimate
                            ),
                            errorMessage = null
                        )
                    }
                }
                ConstructionElementType.CIELO_RASO_PVC -> {
                    val length = current.lengthInput.parseDimension()
                    val width = current.ceilingWidthInput.parseDimension()
                    val slatWidth = current.pvcSlatWidthInput.parseDimension() ?: 0.25
                    val slatLength = current.pvcCommercialSlatLengthInput.parseDimension() ?: 5.95

                    if (length == null || length <= 0.0 || width == null || width <= 0.0) {
                        _uiState.update {
                            it.copy(
                                errorMessage = "Ingresa dimensiones válidas para el techo PVC.",
                                calculationResult = null
                            )
                        }
                        return
                    }

                    val pvcInput = PvcCeilingInput(
                        lengthMeters = length,
                        widthMeters = width,
                        slatWidthMeters = slatWidth,
                        commercialSlatLengthMeters = slatLength,
                        supportSpacingMeters = current.pvcSupportSpacingMeters,
                        wasteFactor = wasteFactor
                    )

                    val reqs = engine.calculatePvcCeiling(pvcInput)
                    val dummyWallInput = WallCalculationInput(
                        lengthMeters = length,
                        heightMeters = width,
                        wasteFactor = wasteFactor
                    )

                    _uiState.update {
                        it.copy(
                            calculationResult = WallCalculationResult(
                                input = dummyWallInput,
                                requirements = reqs
                            ),
                            errorMessage = null
                        )
                    }
                }
                ConstructionElementType.MUEBLE_DRYWALL -> {
                    val width = current.furnitureWidthInput.parseDimension()
                    val height = current.furnitureHeightInput.parseDimension()
                    val depth = current.furnitureDepthInput.parseDimension() ?: 0.35
                    val shelves = current.horizontalShelvesCountInput.trim().toIntOrNull() ?: 2
                    val dividers = current.verticalDividersCountInput.trim().toIntOrNull() ?: 1

                    if (width == null || width <= 0.0 || height == null || height <= 0.0) {
                        _uiState.update {
                            it.copy(
                                errorMessage = "Ingresa dimensiones válidas para el mueble.",
                                calculationResult = null
                            )
                        }
                        return
                    }

                    val furnitureInput = FurnitureCalculationInput(
                        widthMeters = width,
                        heightMeters = height,
                        depthMeters = depth,
                        horizontalShelvesCount = shelves,
                        verticalDividersCount = dividers,
                        boardType = current.boardType,
                        wasteFactor = wasteFactor
                    )

                    val reqs = engine.calculateFurniture(furnitureInput)
                    val dummyWallInput = WallCalculationInput(
                        lengthMeters = width,
                        heightMeters = height,
                        wasteFactor = wasteFactor
                    )

                    _uiState.update {
                        it.copy(
                            calculationResult = WallCalculationResult(
                                input = dummyWallInput,
                                requirements = reqs
                            ),
                            errorMessage = null
                        )
                    }
                }
                ConstructionElementType.MURO_DRYWALL -> {
                    val length = current.lengthInput.parseDimension()
                    val height = current.heightInput.parseDimension()

                    if (length == null || length <= 0.0 || height == null || height <= 0.0) {
                        _uiState.update {
                            it.copy(
                                errorMessage = "Por favor ingresa un largo y alto válidos mayores a 0 metros.",
                                calculationResult = null
                            )
                        }
                        return
                    }

                    val openingsResolution = resolveOpenings(current.openingDrafts)
                    val openings = openingsResolution.openings
                    val draftErrors = openingsResolution.errors

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
                    } else emptyList()

                    val paintEstimate = if (current.paintEnabled && paintErrors.isEmpty()) {
                        runCatching {
                            PaintCalculator.estimate(baseResult.netTotalDrywallArea, paintSpec)
                        }.getOrNull()
                    } else null

                    _uiState.update {
                        it.copy(
                            calculationResult = baseResult.copy(paint = paintEstimate),
                            errorMessage = null,
                            openingsValidationErrors = emptyList(),
                            paintValidationErrors = paintErrors
                        )
                    }
                }
            }
            recomputeCosts()
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
        calculateMaterials()
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
