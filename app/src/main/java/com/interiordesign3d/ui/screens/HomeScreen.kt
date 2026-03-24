package com.interiordesign3d.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.interiordesign3d.data.models.*
import com.interiordesign3d.ui.theme.InteriorColors
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDesigner: (String) -> Unit,
    onNavigateToAI: () -> Unit,
    onNavigateToMeasure: () -> Unit
) {
    val scrollState = rememberLazyListState()

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ── Hero Banner ──────────────────────────────────────────────────────
        item {
            HeroBanner(
                onStartDesigning = {
                    onNavigateToDesigner(UUID.randomUUID().toString())
                },
                onMeasureRoom = onNavigateToMeasure
            )
        }

        // ── Quick Actions ────────────────────────────────────────────────────
        item {
            QuickActionsRow(
                onMeasure   = onNavigateToMeasure,
                onAI        = onNavigateToAI,
                onNewRoom   = { onNavigateToDesigner(UUID.randomUUID().toString()) }
            )
        }

        // ── AI Suggestions Header ────────────────────────────────────────────
        item {
            SectionHeader(
                title    = "AI Style Picks",
                subtitle = "Curated for your taste",
                icon     = Icons.Filled.AutoAwesome,
                actionText = "View all",
                onAction   = onNavigateToAI
            )
        }

        // ── AI Style Cards ───────────────────────────────────────────────────
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(styleInspiration) { style ->
                    AiStyleCard(style = style, onClick = { onNavigateToAI() })
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // ── Recent Rooms ─────────────────────────────────────────────────────
        item {
            SectionHeader(
                title    = "Your Rooms",
                subtitle = "Continue designing",
                icon     = Icons.Outlined.Weekend,
                actionText = "New room",
                onAction   = { onNavigateToDesigner(UUID.randomUUID().toString()) }
            )
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sampleRooms) { room ->
                    RoomCard(room = room, onClick = { onNavigateToDesigner(room.id) })
                }
                item {
                    NewRoomCard(onClick = { onNavigateToDesigner(UUID.randomUUID().toString()) })
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── Hero Banner ──────────────────────────────────────────────────────────────

@Composable
private fun HeroBanner(onStartDesigning: () -> Unit, onMeasureRoom: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(280.dp)
    ) {
        // Warm gradient background
        Box(
            modifier = Modifier.fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF8A3210),
                            Color(0xFFB5451B),
                            Color(0xFFD4602A)
                        )
                    )
                )
        )

        // Decorative circular shapes
        Box(
            modifier = Modifier.size(300.dp).offset(x = 120.dp, y = (-80).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier.size(200.dp).offset(x = (-40).dp, y = 120.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
        )

        Column(
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Good morning ✨",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f))
                    Text("InteriorAI",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Notifications",
                        tint = Color.White)
                }
            }

            // Hero content
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Design Your\nDream Space",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold, lineHeight = 40.sp),
                    color = Color.White)
                Text("Visualize furniture in 3D & AR before you buy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f))
            }

            // Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onStartDesigning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = InteriorColors.Terracotta
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("New Design", fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onMeasureRoom,
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.7f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Straighten, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Measure")
                }
            }
        }
    }
}

// ─── Quick Actions ────────────────────────────────────────────────────────────

@Composable
private fun QuickActionsRow(onMeasure: () -> Unit, onAI: () -> Unit, onNewRoom: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf(
            Triple("3D Planner", Icons.Filled.ViewInAr, onNewRoom),
            Triple("AR View",    Icons.Filled.CameraAlt, onMeasure),
            Triple("AI Design",  Icons.Filled.AutoAwesome, onAI)
        ).forEach { (label, icon, action) ->
            QuickActionChip(label = label, icon = icon, onClick = action,
                modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp))
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    actionText: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        TextButton(onClick = onAction) {
            Text(actionText, style = MaterialTheme.typography.labelMedium)
        }
    }
}

// ─── AI Style Card ────────────────────────────────────────────────────────────

data class StyleInspo(val name: String, val style: DesignStyle, val color: Color, val emoji: String)

val styleInspiration = listOf(
    StyleInspo("Japandi", DesignStyle.JAPANDI, Color(0xFF4A7C59), "🎋"),
    StyleInspo("Industrial", DesignStyle.INDUSTRIAL, Color(0xFF5C5550), "🏭"),
    StyleInspo("Coastal", DesignStyle.COASTAL, Color(0xFF4A6D8C), "🌊"),
    StyleInspo("Mid-Century", DesignStyle.MID_CENTURY, Color(0xFFB5451B), "🪑"),
    StyleInspo("Bohemian", DesignStyle.BOHEMIAN, Color(0xFFC4A028), "🌿"),
    StyleInspo("Minimalist", DesignStyle.MINIMALIST, Color(0xFF3A3A3A), "◽")
)

@Composable
private fun AiStyleCard(style: StyleInspo, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(130.dp).height(160.dp),
        shape = RoundedCornerShape(16.dp),
        color = style.color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, style.color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(style.emoji, fontSize = 32.sp)
            Column {
                Text(style.name, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Text(style.style.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ─── Room Card ────────────────────────────────────────────────────────────────

val sampleRooms = listOf(
    Room("r1","Living Room",     380f, 520f, 260f, "#F5F0EB"),
    Room("r2","Master Bedroom",  320f, 430f, 260f, "#EBF3F5"),
    Room("r3","Home Office",     280f, 350f, 260f, "#F5EBF0"),
)

@Composable
private fun RoomCard(room: Room, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(160.dp).height(190.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(android.graphics.Color.parseColor(room.wallColor)).copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 3D room icon
            Box(
                modifier = Modifier.fillMaxWidth().height(90.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Weekend, contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(room.name, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Text("${room.widthCm.toInt()} × ${room.lengthCm.toInt()} cm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(room.floorMaterial.displayName,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

@Composable
private fun NewRoomCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(160.dp).height(190.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.AddCircleOutline, contentDescription = "New Room",
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            Text("New Room", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}
