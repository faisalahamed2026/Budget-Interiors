package com.interiordesign3d.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.interiordesign3d.data.models.*
import com.interiordesign3d.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

// ─── UI State ─────────────────────────────────────────────────────────────────

data class HomeUiState(
    val recentProjects: List<DesignProject> = emptyList(),
    val savedRooms: List<DesignRoom> = emptyList(),
    val aiSuggestions: List<AiDesignSuggestion> = emptyList(),
    val isLoading: Boolean = false
)

data class CatalogUiState(
    val furniture: List<FurnitureItem> = emptyList(),
    val selectedCategory: FurnitureCategory? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

data class RoomDesignerUiState(
    val room: DesignRoom? = null,
    val placedFurniture: List<PlacedFurniture> = emptyList(),
    val selectedFurnitureId: String? = null,
    val isARMode: Boolean = false,
    val isMeasuring: Boolean = false,
    val cameraAzimuth: Float = 45f,
    val cameraElevation: Float = 30f,
    val cameraDistance: Float = 500f
)

data class MeasurementUiState(
    val currentMeasurement: RoomMeasurement? = null,
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val arSupported: Boolean = false
)

// ─── ViewModels ───────────────────────────────────────────────────────────────

class HomeViewModel(
    private val projectDao: DesignProjectDao,
    private val roomDao: RoomDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                projectDao.getAllProjects(),
                roomDao.getAllRooms()
            ) { projects, rooms ->
                HomeUiState(
                    recentProjects = projects.take(6),
                    savedRooms = rooms,
                    aiSuggestions = generateAiSuggestions(),
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }

    private fun generateAiSuggestions(): List<AiDesignSuggestion> = listOf(
        AiDesignSuggestion("s1","Japandi Sanctuary",
            "Blend Japanese minimalism with Scandinavian warmth. Neutral tones, natural materials, intentional negative space.",
            DesignStyle.JAPANDI,
            listOf("sofa_01","chair_01","lamp_02","plant_01"),
            "#F5F0EB", FloorMaterial.HARDWOOD,
            listOf("moodboard/japandi1.jpg","moodboard/japandi2.jpg")),
        AiDesignSuggestion("s2","Urban Industrial Loft",
            "Raw concrete, exposed metal, leather textures. Bold and unapologetic character.",
            DesignStyle.INDUSTRIAL,
            listOf("sofa_03","table_01","lamp_01"),
            "#3A3A3A", FloorMaterial.CONCRETE,
            listOf("moodboard/industrial1.jpg","moodboard/industrial2.jpg")),
        AiDesignSuggestion("s3","Coastal Escape",
            "Soft blues, natural rattan, linen textures. The ocean brought indoors.",
            DesignStyle.COASTAL,
            listOf("chair_02","rug_01","plant_02","lamp_02"),
            "#EBF5FB", FloorMaterial.TILE,
            listOf("moodboard/coastal1.jpg")),
        AiDesignSuggestion("s4","Mid-Century Revival",
            "Warm walnut, bold teal accents, iconic silhouettes celebrating 1950s design.",
            DesignStyle.MID_CENTURY,
            listOf("chair_03","table_03","lamp_01","rug_02"),
            "#FFF8DC", FloorMaterial.HARDWOOD,
            listOf("moodboard/midcentury1.jpg"))
    )
}

class CatalogViewModel(
    private val furnitureRepository: FurnitureRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState(isLoading = true))
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init { loadFurniture() }

    fun selectCategory(category: FurnitureCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadFurniture()
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            (if (query.isBlank()) furnitureRepository.getAllFurniture()
             else furnitureRepository.searchFurniture(query))
                .collect { items ->
                    _uiState.update { it.copy(furniture = items, isLoading = false) }
                }
        }
    }

    private fun loadFurniture() {
        viewModelScope.launch {
            val category = _uiState.value.selectedCategory
            (if (category == null) furnitureRepository.getAllFurniture()
             else furnitureRepository.getFurnitureByCategory(category))
                .collect { items ->
                    _uiState.update { it.copy(furniture = items, isLoading = false) }
                }
        }
    }
}

class RoomDesignerViewModel(
    private val roomDao: RoomDao,
    private val placedFurnitureDao: PlacedFurnitureDao,
    private val furnitureRepository: FurnitureRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomDesignerUiState())
    val uiState: StateFlow<RoomDesignerUiState> = _uiState.asStateFlow()

    fun loadRoom(roomId: String) {
        viewModelScope.launch {
            val room = roomDao.getRoomById(roomId) ?: return@launch
            _uiState.update { it.copy(room = room) }

            placedFurnitureDao.getFurnitureForRoom(roomId).collect { furniture ->
                _uiState.update { it.copy(placedFurniture = furniture) }
            }
        }
    }

    fun createNewRoom(name: String, widthCm: Float, lengthCm: Float, heightCm: Float) {
        viewModelScope.launch {
            val room = DesignRoom(
                id = UUID.randomUUID().toString(),
                name = name,
                widthCm = widthCm,
                lengthCm = lengthCm,
                heightCm = heightCm
            )
            roomDao.insertRoom(room)
            _uiState.update { it.copy(room = room) }
        }
    }

    fun addFurnitureToRoom(furnitureItem: FurnitureItem) {
        val room = _uiState.value.room ?: return
        viewModelScope.launch {
            val placed = PlacedFurniture(
                id = UUID.randomUUID().toString(),
                roomId = room.id,
                furnitureId = furnitureItem.id,
                furnitureName = furnitureItem.name,
                modelUrl = furnitureItem.modelUrl,
                posX = room.widthCm / 2,
                posZ = room.lengthCm / 2
            )
            placedFurnitureDao.insertPlacedFurniture(placed)
        }
    }

    fun moveFurniture(id: String, posX: Float, posZ: Float) {
        viewModelScope.launch {
            val item = _uiState.value.placedFurniture.find { it.id == id } ?: return@launch
            placedFurnitureDao.updatePlacedFurniture(item.copy(posX = posX, posZ = posZ))
        }
    }

    fun rotateFurniture(id: String, degrees: Float) {
        viewModelScope.launch {
            val item = _uiState.value.placedFurniture.find { it.id == id } ?: return@launch
            placedFurnitureDao.updatePlacedFurniture(item.copy(rotationY = degrees))
        }
    }

    fun applyColorToFurniture(id: String, colorHex: String) {
        viewModelScope.launch {
            val item = _uiState.value.placedFurniture.find { it.id == id } ?: return@launch
            placedFurnitureDao.updatePlacedFurniture(item.copy(colorOverride = colorHex))
        }
    }

    fun removeFurniture(id: String) {
        viewModelScope.launch {
            val item = _uiState.value.placedFurniture.find { it.id == id } ?: return@launch
            placedFurnitureDao.deletePlacedFurniture(item)
        }
    }

    fun updateRoomColors(wallColor: String? = null, floorMaterial: FloorMaterial? = null) {
        val room = _uiState.value.room ?: return
        viewModelScope.launch {
            val updated = room.copy(
                wallColor = wallColor ?: room.wallColor,
                floorMaterial = floorMaterial ?: room.floorMaterial,
                updatedAt = System.currentTimeMillis()
            )
            roomDao.updateRoom(updated)
            _uiState.update { it.copy(room = updated) }
        }
    }

    fun selectFurniture(id: String?) {
        _uiState.update { it.copy(selectedFurnitureId = id) }
    }

    fun toggleARMode(enabled: Boolean) {
        _uiState.update { it.copy(isARMode = enabled) }
    }

    fun updateCamera(azimuth: Float? = null, elevation: Float? = null, distance: Float? = null) {
        _uiState.update { state ->
            state.copy(
                cameraAzimuth = azimuth ?: state.cameraAzimuth,
                cameraElevation = elevation ?: state.cameraElevation,
                cameraDistance = distance ?: state.cameraDistance
            )
        }
    }
}

class MeasurementViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MeasurementUiState())
    val uiState: StateFlow<MeasurementUiState> = _uiState.asStateFlow()

    fun startARScan() {
        _uiState.update { it.copy(isScanning = true, scanProgress = 0f) }
        viewModelScope.launch {
            // Simulate AR scanning progress
            for (i in 1..10) {
                kotlinx.coroutines.delay(400)
                _uiState.update { it.copy(scanProgress = i / 10f) }
            }
            _uiState.update {
                it.copy(
                    isScanning = false,
                    scanProgress = 1f,
                    currentMeasurement = RoomMeasurement(380f, 480f, 260f, true, 0.94f)
                )
            }
        }
    }

    fun setManualMeasurement(width: Float, length: Float, height: Float) {
        _uiState.update {
            it.copy(currentMeasurement = RoomMeasurement(width, length, height, false))
        }
    }

    fun checkARSupport(context: android.content.Context) {
        val availability = com.google.ar.core.ArCoreApk.getInstance()
            .checkAvailability(context)
        _uiState.update {
            it.copy(arSupported = availability.isSupported)
        }
    }
}
