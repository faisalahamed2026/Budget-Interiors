package com.interiordesign3d.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

// ─── Furniture Item ────────────────────────────────────────────────────────────

@Parcelize
@Serializable
data class FurnitureItem(
    val id: String,
    val name: String,
    val category: FurnitureCategory,
    val brand: String,
    val price: Double,
    val thumbnailUrl: String,
    val modelUrl: String,               // Path to .glb / .gltf 3D model
    val widthCm: Float,
    val depthCm: Float,
    val heightCm: Float,
    val availableColors: List<String>,  // Hex color strings
    val description: String,
    val tags: List<String> = emptyList()
) : Parcelable

enum class FurnitureCategory(val displayName: String, val icon: String) {
    SOFA("Sofas", "🛋️"),
    CHAIR("Chairs", "🪑"),
    TABLE("Tables", "🪞"),
    BED("Beds", "🛏️"),
    WARDROBE("Wardrobes", "🚪"),
    BOOKSHELF("Bookshelves", "📚"),
    LAMP("Lamps", "💡"),
    RUG("Rugs", "🟫"),
    PLANT("Plants", "🪴"),
    DECOR("Décor", "🎨")
}

// ─── DesignRoom ──────────────────────────────────────────────────────────────────────

@Entity(tableName = "rooms")
@Parcelize
@Serializable
data class DesignRoom(
    @PrimaryKey val id: String,
    val name: String,
    val widthCm: Float,
    val lengthCm: Float,
    val heightCm: Float,
    val wallColor: String = "#F5F0EB",
    val floorMaterial: FloorMaterial = FloorMaterial.HARDWOOD,
    val floorColor: String = "#C4A882",
    val thumbnailPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

enum class FloorMaterial(val displayName: String) {
    HARDWOOD("Hardwood"),
    MARBLE("Marble"),
    TILE("Tile"),
    CARPET("Carpet"),
    CONCRETE("Concrete"),
    LAMINATE("Laminate")
}

// ─── Placed Furniture (in a room) ──────────────────────────────────────────────

@Entity(tableName = "placed_furniture")
@Parcelize
@Serializable
data class PlacedFurniture(
    @PrimaryKey val id: String,
    val roomId: String,
    val furnitureId: String,
    val furnitureName: String,
    val modelUrl: String,
    // Position in the room (cm from bottom-left corner)
    val posX: Float = 0f,
    val posY: Float = 0f,       // height (usually 0 = on floor)
    val posZ: Float = 0f,
    // Rotation around Y axis (degrees)
    val rotationY: Float = 0f,
    // Scale multiplier
    val scale: Float = 1.0f,
    // Applied color override (hex) or null = default
    val colorOverride: String? = null
) : Parcelable

// ─── Design Project ────────────────────────────────────────────────────────────

@Entity(tableName = "design_projects")
@Parcelize
@Serializable
data class DesignProject(
    @PrimaryKey val id: String,
    val name: String,
    val roomId: String,
    val thumbnailPath: String? = null,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isShared: Boolean = false,
    val shareUrl: String? = null
) : Parcelable

// ─── AI Suggestion ────────────────────────────────────────────────────────────

data class AiDesignSuggestion(
    val id: String,
    val title: String,
    val description: String,
    val style: DesignStyle,
    val suggestedFurnitureIds: List<String>,
    val suggestedWallColor: String,
    val suggestedFloorMaterial: FloorMaterial,
    val moodBoardImages: List<String>
)

enum class DesignStyle(val displayName: String) {
    MINIMALIST("Minimalist"),
    SCANDINAVIAN("Scandinavian"),
    INDUSTRIAL("Industrial"),
    BOHEMIAN("Bohemian"),
    MODERN("Modern"),
    CLASSIC("Classic"),
    JAPANDI("Japandi"),
    COASTAL("Coastal"),
    MAXIMALIST("Maximalist"),
    MID_CENTURY("Mid-Century Modern")
}

// ─── DesignRoom Measurement ─────────────────────────────────────────────────────────

data class RoomMeasurement(
    val widthCm: Float,
    val lengthCm: Float,
    val heightCm: Float,
    val measuredViaAR: Boolean = false,
    val confidence: Float = 1.0f // 0–1
)

// ─── Color Palette ────────────────────────────────────────────────────────────

data class ColorPalette(
    val id: String,
    val name: String,
    val primary: String,
    val secondary: String,
    val accent: String,
    val background: String,
    val style: DesignStyle
)

// ─── Material ─────────────────────────────────────────────────────────────────

data class Material(
    val id: String,
    val name: String,
    val type: MaterialType,
    val colorHex: String,
    val textureUrl: String? = null,
    val roughness: Float = 0.5f,
    val metallic: Float = 0f
)

enum class MaterialType(val displayName: String) {
    PAINT("Paint"),
    WALLPAPER("Wallpaper"),
    WOOD("Wood"),
    STONE("Stone"),
    FABRIC("Fabric"),
    METAL("Metal"),
    GLASS("Glass")
}
