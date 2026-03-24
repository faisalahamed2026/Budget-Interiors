package com.interiordesign3d.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.interiordesign3d.data.models.*
import com.interiordesign3d.data.repository.ColorPaletteRepository

// ═══════════════════════════════════════════════════════════════════════════════
// COLOR PICKER SCREEN
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerScreen(roomId: String, onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedWallColor by remember { mutableStateOf("#F5F0EB") }
    var selectedFloor by remember { mutableStateOf(FloorMaterial.HARDWOOD) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Colors & Materials") },
                navigationIcon = { IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back") } },
                actions = {
                    Button(onClick = onBack, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Apply")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                listOf("Wall Colors", "Floor", "Palettes").forEachIndexed { idx, title ->
                    Tab(selected = selectedTab == idx, onClick = { selectedTab = idx },
                        text = { Text(title) })
                }
            }

            when (selectedTab) {
                0 -> WallColorTab(selectedColor = selectedWallColor,
                    onColorSelect = { selectedWallColor = it })
                1 -> FloorMaterialTab(selectedMaterial = selectedFloor,
                    onSelect = { selectedFloor = it })
                2 -> PaletteTab()
            }
        }
    }
}

@Composable
private fun WallColorTab(selectedColor: String, onColorSelect: (String) -> Unit) {
    val paintColors = listOf(
        // Whites & Neutrals
        "#FFFFFF","#F5F0EB","#F0EBE3","#EDE0D0","#E8D5C4",
        // Warm tones
        "#F2C4A0","#E8A87C","#D4785A","#B5451B","#8A3210",
        // Greens
        "#E8F5E9","#C8E6C9","#81C784","#4A7C59","#2C5F3E",
        // Blues
        "#E3F2FD","#90CAF9","#42A5F5","#1565C0","#0D47A1",
        // Grays
        "#F5F5F5","#E0E0E0","#9E9E9E","#616161","#212121",
        // Accent
        "#FFF9C4","#FFE082","#FFB300","#FF7043","#F4511E"
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Preview
        Surface(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(16.dp),
            color = try { Color(android.graphics.Color.parseColor(selectedColor)) }
                    catch (e: Exception) { Color(0xFFF5F0EB) }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("Wall Preview", color = Color.Black.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.bodyLarge)
            }
        }

        Text("Paint Colors", style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold)

        LazyVerticalGrid(columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(paintColors) { hex ->
                val isSelected = hex == selectedColor
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape)
                        .background(try { Color(android.graphics.Color.parseColor(hex)) }
                                    catch (e: Exception) { Color.Gray })
                        .border(if (isSelected) 3.dp else 1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.15f),
                            CircleShape)
                        .clickable { onColorSelect(hex) }
                )
            }
        }
    }
}

@Composable
private fun FloorMaterialTab(selectedMaterial: FloorMaterial, onSelect: (FloorMaterial) -> Unit) {
    val materialEmojis = mapOf(
        FloorMaterial.HARDWOOD to "🪵", FloorMaterial.MARBLE to "⬜",
        FloorMaterial.TILE to "🔲", FloorMaterial.CARPET to "🟫",
        FloorMaterial.CONCRETE to "🩶", FloorMaterial.LAMINATE to "📋"
    )

    LazyVerticalGrid(columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(FloorMaterial.entries) { material ->
            val isSelected = material == selectedMaterial
            Surface(
                onClick = { onSelect(material) },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(if (isSelected) 2.dp else 1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
            ) {
                Row(modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(materialEmojis[material] ?: "🏠", fontSize = 32.sp)
                    Column {
                        Text(material.displayName, style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold)
                        if (isSelected) {
                            Text("Selected ✓", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaletteTab() {
    LazyColumn(contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(ColorPaletteRepository.palettes) { palette ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(palette.name, style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold)
                            Text(palette.style.displayName, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = {}) { Text("Apply") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(palette.primary, palette.secondary, palette.accent, palette.background)
                            .forEach { hex ->
                                Box(modifier = Modifier.weight(1f).height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(try { Color(android.graphics.Color.parseColor(hex)) }
                                                catch (e: Exception) { Color.Gray }))
                            }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// AR VIEW SCREEN
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARViewScreen(roomId: String, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Simulated camera background
        Box(modifier = Modifier.fillMaxSize()
            .background(Brush.radialGradient(
                colors = listOf(Color(0xFF1A2A1A), Color(0xFF0A1A0A)),
                radius = 1000f)))

        // AR grid overlay
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val gridColor = Color(0xFF00FF88).copy(alpha = 0.2f)
            val spacing = 60.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(gridColor, androidx.compose.ui.geometry.Offset(x, 0f),
                    androidx.compose.ui.geometry.Offset(x, size.height), 1f)
                x += spacing
            }
            var y = 0f
            while (y < size.height) {
                drawLine(gridColor, androidx.compose.ui.geometry.Offset(0f, y),
                    androidx.compose.ui.geometry.Offset(size.width, y), 1f)
                y += spacing
            }
        }

        // Furniture placement indicators
        Box(modifier = Modifier.align(Alignment.Center)) {
            Text("📦", fontSize = 48.sp, modifier = Modifier.offset((-60).dp, 40.dp))
            Text("🛋️", fontSize = 64.sp)
            Text("🪑", fontSize = 40.sp, modifier = Modifier.offset(80.dp, 20.dp))
        }

        // Crosshair
        androidx.compose.foundation.Canvas(modifier = Modifier.align(Alignment.Center).size(60.dp)) {
            val cx = size.width / 2
            val cy = size.height / 2
            val color = Color.White
            drawLine(color, androidx.compose.ui.geometry.Offset(cx - 30f, cy),
                androidx.compose.ui.geometry.Offset(cx + 30f, cy), 2f)
            drawLine(color, androidx.compose.ui.geometry.Offset(cx, cy - 30f),
                androidx.compose.ui.geometry.Offset(cx, cy + 30f), 2f)
            drawCircle(color, 4f, androidx.compose.ui.geometry.Offset(cx, cy))
        }

        // Top controls
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack,
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Surface(shape = RoundedCornerShape(8.dp), color = Color.Black.copy(alpha = 0.5f)) {
                Text("AR Furniture Placement", color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
            }
            IconButton(onClick = {},
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))) {
                Icon(Icons.Filled.FlashlightOn, "Flash", tint = Color.White)
            }
        }

        // Bottom panel
        Surface(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            tonalElevation = 8.dp) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Tap to place furniture", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Text("Move device slowly to detect surfaces",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("🛋️ Sofa","🪑 Chair","🛏️ Bed","💡 Lamp").forEach { label ->
                        FilterChip(selected = false, onClick = {}, label = { Text(label) })
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// AI SUGGESTIONS SCREEN
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISuggestionsScreen(onApplySuggestion: (String) -> Unit) {
    val suggestions = remember {
        listOf(
            AiSuggestionUi("Japandi Sanctuary", DesignStyle.JAPANDI, "🎋",
                Color(0xFF4A7C59), "#F5F0EB",
                "Blend Japanese wabi-sabi with Scandinavian simplicity. Natural textures, muted greens, and purposeful negative space.",
                listOf("Natural materials","Muted palette","Minimal furniture","Organic shapes")),
            AiSuggestionUi("Urban Industrial Loft", DesignStyle.INDUSTRIAL, "🏭",
                Color(0xFF5C5550), "#3A3A3A",
                "Raw concrete, aged metal, and exposed brick. Bold, unapologetic character that celebrates imperfection.",
                listOf("Exposed materials","Dark tones","Geometric forms","Statement lighting")),
            AiSuggestionUi("Coastal Escape", DesignStyle.COASTAL, "🌊",
                Color(0xFF4A6D8C), "#EBF3F5",
                "Soft ocean blues, rattan textures, and linen fabrics. The serenity of the coast, brought indoors.",
                listOf("Blues & whites","Natural rattan","Linen textures","Driftwood accents")),
            AiSuggestionUi("Mid-Century Revival", DesignStyle.MID_CENTURY, "🪑",
                Color(0xFFB5451B), "#FFF8DC",
                "Warm walnut, bold teal, and iconic silhouettes. A love letter to 1950s optimism.",
                listOf("Walnut wood","Bold accent colors","Iconic shapes","Tapered legs")),
            AiSuggestionUi("Bohemian Dreamscape", DesignStyle.BOHEMIAN, "🌿",
                Color(0xFFC4A028), "#FFF9EE",
                "Layered textiles, global patterns, and lush greenery. Eclectic, warm, and deeply personal.",
                listOf("Layered rugs","Mixed patterns","Lots of plants","Warm lighting"))
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        item {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                    Text("AI Design Suggestions", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                }
                Text("Curated styles tailored to your preferences",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        items(suggestions) { suggestion ->
            AISuggestionCard(suggestion = suggestion,
                onApply = { onApplySuggestion(java.util.UUID.randomUUID().toString()) })
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

data class AiSuggestionUi(
    val name: String, val style: DesignStyle, val emoji: String,
    val color: Color, val wallColor: String, val description: String, val features: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AISuggestionCard(suggestion: AiSuggestionUi, onApply: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp)) {
        Column {
            // Header
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)
                .background(Brush.horizontalGradient(
                    colors = listOf(suggestion.color, suggestion.color.copy(alpha = 0.6f))))) {
                Text(suggestion.emoji, fontSize = 80.sp,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp))
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                    Surface(shape = RoundedCornerShape(4.dp),
                        color = Color.White.copy(alpha = 0.25f)) {
                        Text(suggestion.style.displayName,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                    Text(suggestion.name, style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Body
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(suggestion.description, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                // Feature chips
                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    suggestion.features.forEach { feature ->
                        Surface(shape = RoundedCornerShape(6.dp),
                            color = suggestion.color.copy(alpha = 0.12f)) {
                            Text(feature, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = suggestion.color)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.Visibility, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Preview")
                    }
                    Button(onClick = onApply, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.AutoAwesome, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Apply Style")
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// SAVED DESIGNS SCREEN
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedDesignsScreen(onDesignClick: (String) -> Unit) {
    val designs = sampleRooms

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Saved Designs", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text("${designs.size} projects", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        LazyVerticalGrid(columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(designs) { room ->
                Card(onClick = { onDesignClick(room.id) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f)
                            .background(try { Color(android.graphics.Color.parseColor(room.wallColor)).copy(alpha = 0.5f) }
                                        catch (e: Exception) { Color.Gray }),
                            contentAlignment = Alignment.Center) {
                            Text("🏠", fontSize = 52.sp)
                        }
                        Column(modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(room.name, style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold)
                            Text("${room.widthCm.toInt()} × ${room.lengthCm.toInt()} cm",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.Share, null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Icon(Icons.Outlined.Edit, null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// FURNITURE DETAIL SCREEN
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FurnitureDetailScreen(furnitureId: String, onBack: () -> Unit) {
    val repo = remember { com.interiordesign3d.data.repository.FurnitureRepository() }
    val item = remember { repo.getFurnitureById(furnitureId) }

    if (item == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Item not found")
        }
        return
    }

    var selectedColor by remember { mutableStateOf(item.availableColors.firstOrNull() ?: "#F5F0EB") }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        .background(MaterialTheme.colorScheme.background)) {

        // Hero
        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            Box(modifier = Modifier.fillMaxSize()
                .background(try { Color(android.graphics.Color.parseColor(selectedColor)).copy(alpha = 0.3f) }
                            catch (e: Exception) { Color.Gray }),
                contentAlignment = Alignment.Center) {
                Text(item.category.icon, fontSize = 120.sp)
            }
            IconButton(onClick = onBack,
                modifier = Modifier.padding(16.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))) {
                Icon(Icons.Filled.ArrowBack, "Back")
            }
        }

        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Text(item.brand, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("$${item.price.toInt()}", style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            // Dimensions
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround) {
                    listOf("W" to "${item.widthCm.toInt()}cm",
                           "D" to "${item.depthCm.toInt()}cm",
                           "H" to "${item.heightCm.toInt()}cm").forEach { (label, value) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(value, style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Text(label, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Color picker
            if (item.availableColors.isNotEmpty()) {
                Text("Available Colors", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item.availableColors.forEach { hex ->
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                            .background(try { Color(android.graphics.Color.parseColor(hex)) }
                                        catch (e: Exception) { Color.Gray })
                            .border(if (hex == selectedColor) 3.dp else 1.dp,
                                if (hex == selectedColor) MaterialTheme.colorScheme.primary
                                else Color.Black.copy(alpha = 0.15f), CircleShape)
                            .clickable { selectedColor = hex })
                    }
                }
            }

            // Description
            Text(item.description, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Tags
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item.tags.forEach { tag ->
                    Surface(shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer) {
                        Text("# $tag", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }

            // CTA Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.ViewInAr, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("View in AR")
                }
                Button(onClick = {}, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.AddCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add to Room")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
