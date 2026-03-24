package com.interiordesign3d.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.interiordesign3d.data.models.*
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDesignerScreen(
    roomId: String,
    onNavigateToAR: () -> Unit,
    onNavigateToColorPicker: () -> Unit,
    onBack: () -> Unit
) {
    var room by remember { mutableStateOf(
        Room("r1","Living Room", 380f, 520f, 260f, "#F5F0EB")
    )}
    var placedFurniture by remember { mutableStateOf(mutableListOf<PlacedFurniture>()) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var showFurniturePicker by remember { mutableStateOf(false) }
    var showRoomSetup by remember { mutableStateOf(roomId.length > 10) } // new room
    var viewMode by remember { mutableStateOf(ViewMode.PERSPECTIVE) }

    // Room setup dialog for new rooms
    if (showRoomSetup) {
        RoomSetupDialog(
            onConfirm = { name, w, l, h ->
                room = Room(roomId, name, w, l, h)
                showRoomSetup = false
            },
            onDismiss = { showRoomSetup = false; onBack() }
        )
    }

    Scaffold(
        topBar = {
            DesignerTopBar(
                roomName   = room.name,
                viewMode   = viewMode,
                onViewChange = { viewMode = it },
                onAR       = onNavigateToAR,
                onColors   = onNavigateToColorPicker,
                onSave     = { /* save project */ },
                onBack     = onBack
            )
        },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallFloatingActionButton(
                    onClick = onNavigateToAR,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Icon(Icons.Filled.ViewInAr, "AR View")
                }
                FloatingActionButton(
                    onClick = { showFurniturePicker = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.AddCircle, "Add Furniture")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── 3D Viewport ──────────────────────────────────────────────────
            RoomViewport3D(
                room = room,
                placedFurniture = placedFurniture,
                selectedId = selectedId,
                viewMode = viewMode,
                onSelectFurniture = { selectedId = it },
                onMoveFurniture = { id, x, z ->
                    placedFurniture = placedFurniture.map {
                        if (it.id == id) it.copy(posX = x, posZ = z) else it
                    }.toMutableList()
                },
                modifier = Modifier.weight(1f)
            )

            // ── Selected Furniture Controls ──────────────────────────────────
            AnimatedVisibility(
                visible = selectedId != null,
                enter = slideInVertically { it },
                exit  = slideOutVertically { it }
            ) {
                val selected = placedFurniture.find { it.id == selectedId }
                if (selected != null) {
                    FurnitureControlPanel(
                        item = selected,
                        onRotate  = { deg ->
                            placedFurniture = placedFurniture.map {
                                if (it.id == selectedId) it.copy(rotationY = deg) else it
                            }.toMutableList()
                        },
                        onScale   = { scale ->
                            placedFurniture = placedFurniture.map {
                                if (it.id == selectedId) it.copy(scale = scale) else it
                            }.toMutableList()
                        },
                        onDelete  = {
                            placedFurniture.removeIf { it.id == selectedId }
                            placedFurniture = placedFurniture.toMutableList()
                            selectedId = null
                        },
                        onDeselect = { selectedId = null }
                    )
                }
            }
        }
    }

    // ── Furniture Picker Sheet ───────────────────────────────────────────────
    if (showFurniturePicker) {
        ModalBottomSheet(onDismissRequest = { showFurniturePicker = false }) {
            FurniturePickerSheet(
                onSelect = { item ->
                    val placed = PlacedFurniture(
                        id = java.util.UUID.randomUUID().toString(),
                        roomId = room.id,
                        furnitureId = item.id,
                        furnitureName = item.name,
                        modelUrl = item.modelUrl,
                        posX = room.widthCm / 2,
                        posZ = room.lengthCm / 2
                    )
                    placedFurniture = (placedFurniture + placed).toMutableList()
                    selectedId = placed.id
                    showFurniturePicker = false
                }
            )
        }
    }
}

// ─── View Modes ───────────────────────────────────────────────────────────────

enum class ViewMode(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    TOP_DOWN("Top View", Icons.Filled.GridOn),
    PERSPECTIVE("3D View", Icons.Filled.ViewInAr),
    ISOMETRIC("Isometric", Icons.Filled.Layers)
}

// ─── Top Bar ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DesignerTopBar(
    roomName: String,
    viewMode: ViewMode,
    onViewChange: (ViewMode) -> Unit,
    onAR: () -> Unit,
    onColors: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    TopAppBar(
        title = { Text(roomName, style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            // View mode toggle
            ViewMode.entries.forEach { mode ->
                IconButton(onClick = { onViewChange(mode) }) {
                    Icon(mode.icon, contentDescription = mode.label,
                        tint = if (viewMode == mode) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onColors) {
                Icon(Icons.Filled.Palette, contentDescription = "Colors")
            }
            IconButton(onClick = onSave) {
                Icon(Icons.Filled.Save, contentDescription = "Save")
            }
        }
    )
}

// ─── 3D Room Viewport (Canvas-based renderer) ──────────────────────────────────

@Composable
fun RoomViewport3D(
    room: Room,
    placedFurniture: List<PlacedFurniture>,
    selectedId: String?,
    viewMode: ViewMode,
    onSelectFurniture: (String?) -> Unit,
    onMoveFurniture: (String, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var cameraAzimuth by remember { mutableStateOf(35f) }
    var cameraElevation by remember { mutableStateOf(50f) }
    var zoom by remember { mutableStateOf(1f) }
    var dragStartAzimuth by remember { mutableStateOf(0f) }
    var dragStartElevation by remember { mutableStateOf(0f) }

    Box(modifier = modifier
        .fillMaxSize()
        .background(Color(0xFFE8E4DF))
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, pinchZoom, _ ->
                cameraAzimuth = (cameraAzimuth + pan.x * 0.3f) % 360f
                cameraElevation = (cameraElevation - pan.y * 0.3f).coerceIn(10f, 80f)
                zoom = (zoom * pinchZoom).coerceIn(0.5f, 3f)
            }
        }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoom3D(
                room = room,
                furniture = placedFurniture,
                selectedId = selectedId,
                azimuth = cameraAzimuth,
                elevation = cameraElevation,
                zoom = zoom,
                viewMode = viewMode,
                size = size
            )
        }

        // Camera controls overlay
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallFloatingActionButton(onClick = { zoom = (zoom * 1.2f).coerceAtMost(3f) },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)) {
                Icon(Icons.Filled.ZoomIn, "Zoom In", modifier = Modifier.size(18.dp))
            }
            SmallFloatingActionButton(onClick = { zoom = (zoom * 0.8f).coerceAtLeast(0.5f) },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)) {
                Icon(Icons.Filled.ZoomOut, "Zoom Out", modifier = Modifier.size(18.dp))
            }
            SmallFloatingActionButton(onClick = { cameraAzimuth = 35f; cameraElevation = 50f; zoom = 1f },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)) {
                Icon(Icons.Filled.CenterFocusWeak, "Reset", modifier = Modifier.size(18.dp))
            }
        }

        // Room dimensions overlay
        Surface(
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text("${room.widthCm.toInt()} × ${room.lengthCm.toInt()} cm",
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Text("${placedFurniture.size} items placed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ─── Canvas 3D Drawing ────────────────────────────────────────────────────────

fun DrawScope.drawRoom3D(
    room: Room,
    furniture: List<PlacedFurniture>,
    selectedId: String?,
    azimuth: Float,
    elevation: Float,
    zoom: Float,
    viewMode: ViewMode,
    size: Size
) {
    val cx = size.width / 2
    val cy = size.height / 2
    val scale = minOf(size.width, size.height) / maxOf(room.widthCm, room.lengthCm) * 0.6f * zoom

    fun project(x: Float, y: Float, z: Float): Offset {
        val azRad = Math.toRadians(azimuth.toDouble())
        val elRad = Math.toRadians(elevation.toDouble())
        val rx = x * cos(azRad) - z * sin(azRad)
        val ry = y
        val rz = x * sin(azRad) + z * cos(azRad)
        val px = rx * cos(0.0) - ry * sin(0.0)  // no tilt in XY
        val screenX = cx + (rx * scale).toFloat()
        val screenY = cy - ((rz * sin(elRad) + y * cos(elRad)) * scale).toFloat()
        return Offset(screenX, screenY)
    }

    val w = room.widthCm
    val l = room.lengthCm
    val h = room.heightCm
    val hw = w / 2
    val hl = l / 2

    // Floor
    val floorColor = try {
        Color(android.graphics.Color.parseColor(room.floorColor))
    } catch (e: Exception) { Color(0xFFC4A882) }

    val floorPath = Path().apply {
        val tl = project(-hw, 0f, -hl)
        val tr = project(hw, 0f, -hl)
        val br = project(hw, 0f, hl)
        val bl = project(-hw, 0f, hl)
        moveTo(tl.x, tl.y); lineTo(tr.x, tr.y); lineTo(br.x, br.y); lineTo(bl.x, bl.y); close()
    }
    drawPath(floorPath, color = floorColor.copy(alpha = 0.7f))
    drawPath(floorPath, color = Color.Black.copy(alpha = 0.15f), style = Stroke(1f))

    // Floor grid
    val gridLines = 5
    for (i in 0..gridLines) {
        val t = i / gridLines.toFloat()
        val x = -hw + w * t
        val p1 = project(x, 0f, -hl)
        val p2 = project(x, 0f, hl)
        drawLine(Color.Black.copy(alpha = 0.07f), p1, p2, strokeWidth = 0.8f)
    }
    for (i in 0..gridLines) {
        val t = i / gridLines.toFloat()
        val z = -hl + l * t
        val p1 = project(-hw, 0f, z)
        val p2 = project(hw, 0f, z)
        drawLine(Color.Black.copy(alpha = 0.07f), p1, p2, strokeWidth = 0.8f)
    }

    // Back wall (left-back)
    val wallColor = try {
        Color(android.graphics.Color.parseColor(room.wallColor))
    } catch (e: Exception) { Color(0xFFF5F0EB) }

    val backWallPath = Path().apply {
        val tl = project(-hw, h, -hl)
        val tr = project(hw, h, -hl)
        val br = project(hw, 0f, -hl)
        val bl = project(-hw, 0f, -hl)
        moveTo(tl.x, tl.y); lineTo(tr.x, tr.y); lineTo(br.x, br.y); lineTo(bl.x, bl.y); close()
    }
    drawPath(backWallPath, color = wallColor.copy(alpha = 0.85f))
    drawPath(backWallPath, color = Color.Black.copy(alpha = 0.12f), style = Stroke(1f))

    // Side wall (right-back)
    val sideWallPath = Path().apply {
        val tl = project(hw, h, -hl)
        val tr = project(hw, h, hl)
        val br = project(hw, 0f, hl)
        val bl = project(hw, 0f, -hl)
        moveTo(tl.x, tl.y); lineTo(tr.x, tr.y); lineTo(br.x, br.y); lineTo(bl.x, bl.y); close()
    }
    drawPath(sideWallPath, color = wallColor.copy(alpha = 0.65f))
    drawPath(sideWallPath, color = Color.Black.copy(alpha = 0.1f), style = Stroke(1f))

    // Draw furniture
    furniture.forEach { item ->
        drawFurnitureBox(item, selectedId, ::project, scale)
    }
}

fun DrawScope.drawFurnitureBox(
    item: PlacedFurniture,
    selectedId: String?,
    project: (Float, Float, Float) -> Offset,
    scale: Float
) {
    val isSelected = item.id == selectedId
    val baseColor = if (item.colorOverride != null) {
        try { Color(android.graphics.Color.parseColor(item.colorOverride)) }
        catch (e: Exception) { Color(0xFF8B7355) }
    } else Color(0xFF8B7355)

    // Approximate furniture as a box using typical ratios
    val bw = 60f * item.scale / 2
    val bh = 40f * item.scale
    val bl = 60f * item.scale / 2

    val x = item.posX - 190f  // offset from center
    val z = item.posZ - 260f

    // Top face
    val top = Path().apply {
        val tl = project(x - bw, bh, z - bl)
        val tr = project(x + bw, bh, z - bl)
        val br = project(x + bw, bh, z + bl)
        val bll = project(x - bw, bh, z + bl)
        moveTo(tl.x, tl.y); lineTo(tr.x, tr.y); lineTo(br.x, br.y); lineTo(bll.x, bll.y); close()
    }
    drawPath(top, color = baseColor.copy(alpha = 0.9f))

    // Front face
    val front = Path().apply {
        val tl = project(x - bw, bh, z + bl)
        val tr = project(x + bw, bh, z + bl)
        val br = project(x + bw, 0f, z + bl)
        val bll = project(x - bw, 0f, z + bl)
        moveTo(tl.x, tl.y); lineTo(tr.x, tr.y); lineTo(br.x, br.y); lineTo(bll.x, bll.y); close()
    }
    drawPath(front, color = baseColor.copy(alpha = 0.7f))

    // Side face
    val side = Path().apply {
        val tl = project(x + bw, bh, z - bl)
        val tr = project(x + bw, bh, z + bl)
        val br = project(x + bw, 0f, z + bl)
        val bll = project(x + bw, 0f, z - bl)
        moveTo(tl.x, tl.y); lineTo(tr.x, tr.y); lineTo(br.x, br.y); lineTo(bll.x, bll.y); close()
    }
    drawPath(side, color = baseColor.copy(alpha = 0.55f))

    // Selection highlight
    if (isSelected) {
        drawPath(top, color = Color.Transparent, style = Stroke(2f / scale))
        val outline = Path().apply {
            val tl = project(x - bw, bh, z - bl)
            val tr = project(x + bw, bh, z - bl)
            val br = project(x + bw, bh, z + bl)
            val bll = project(x - bw, bh, z + bl)
            moveTo(tl.x, tl.y); lineTo(tr.x, tr.y); lineTo(br.x, br.y); lineTo(bll.x, bll.y); close()
        }
        drawPath(outline, color = Color(0xFFFFB74D), style = Stroke(2.5f))
    }
}

// ─── Furniture Control Panel ──────────────────────────────────────────────────

@Composable
private fun FurnitureControlPanel(
    item: PlacedFurniture,
    onRotate: (Float) -> Unit,
    onScale: (Float) -> Unit,
    onDelete: () -> Unit,
    onDeselect: () -> Unit
) {
    Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(item.furnitureName, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Row {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = onDeselect) {
                        Icon(Icons.Filled.Close, "Deselect")
                    }
                }
            }

            // Rotation
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Rotation", style = MaterialTheme.typography.labelMedium)
                    Text("${item.rotationY.toInt()}°", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                }
                Slider(value = item.rotationY, onValueChange = onRotate,
                    valueRange = 0f..360f, modifier = Modifier.fillMaxWidth())
            }

            // Scale
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Scale", style = MaterialTheme.typography.labelMedium)
                    Text("${(item.scale * 100).toInt()}%", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                }
                Slider(value = item.scale, onValueChange = onScale,
                    valueRange = 0.3f..2.0f, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

// ─── Room Setup Dialog ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomSetupDialog(
    onConfirm: (String, Float, Float, Float) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("Living Room") }
    var width by remember { mutableStateOf("380") }
    var length by remember { mutableStateOf("520") }
    var height by remember { mutableStateOf("260") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Weekend, contentDescription = null) },
        title = { Text("Set Up Your Room") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Room name") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = width, onValueChange = { width = it },
                        label = { Text("Width (cm)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = length, onValueChange = { length = it },
                        label = { Text("Length (cm)") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = height, onValueChange = { height = it },
                    label = { Text("Height (cm)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    name.ifBlank { "Room" },
                    width.toFloatOrNull() ?: 380f,
                    length.toFloatOrNull() ?: 520f,
                    height.toFloatOrNull() ?: 260f
                )
            }) { Text("Create Room") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ─── Furniture Picker Sheet ───────────────────────────────────────────────────

@Composable
private fun FurniturePickerSheet(onSelect: (FurnitureItem) -> Unit) {
    val repo = remember { com.interiordesign3d.data.repository.FurnitureRepository() }
    var items by remember { mutableStateOf<List<FurnitureItem>>(emptyList()) }
    var selectedCat by remember { mutableStateOf<FurnitureCategory?>(null) }

    LaunchedEffect(selectedCat) {
        (if (selectedCat == null) repo.getAllFurniture()
         else repo.getFurnitureByCategory(selectedCat!!))
            .collect { items = it }
    }

    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
        Text("Add Furniture", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))

        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(FurnitureCategory.entries) { cat ->
                FilterChip(selected = selectedCat == cat,
                    onClick = { selectedCat = if (selectedCat == cat) null else cat },
                    label = { Text("${cat.icon} ${cat.displayName}") })
            }
        }

        LazyRow(contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items, key = { it.id }) { item ->
                Surface(onClick = { onSelect(item) },
                    modifier = Modifier.width(120.dp).height(140.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column(modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween) {
                        Text(item.category.icon, fontSize = 36.sp)
                        Column {
                            Text(item.name, style = MaterialTheme.typography.labelMedium,
                                maxLines = 2, fontWeight = FontWeight.SemiBold)
                            Text("$${item.price.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}
