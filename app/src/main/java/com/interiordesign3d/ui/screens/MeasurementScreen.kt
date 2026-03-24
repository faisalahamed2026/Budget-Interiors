package com.interiordesign3d.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementScreen(
    onMeasurementComplete: (Float, Float, Float) -> Unit,
    onBack: () -> Unit
) {
    var mode by remember { mutableStateOf(MeasureMode.CHOOSE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Room Measurement") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)
            .background(MaterialTheme.colorScheme.background)) {
            when (mode) {
                MeasureMode.CHOOSE -> MeasureModeChooser(
                    onARMode = { mode = MeasureMode.AR },
                    onManualMode = { mode = MeasureMode.MANUAL }
                )
                MeasureMode.AR -> ARMeasurementView(
                    onComplete = { w, l, h -> onMeasurementComplete(w, l, h) },
                    onBack = { mode = MeasureMode.CHOOSE }
                )
                MeasureMode.MANUAL -> ManualMeasurementForm(
                    onComplete = { w, l, h -> onMeasurementComplete(w, l, h) },
                    onBack = { mode = MeasureMode.CHOOSE }
                )
            }
        }
    }
}

enum class MeasureMode { CHOOSE, AR, MANUAL }

// ─── Mode Chooser ─────────────────────────────────────────────────────────────

@Composable
private fun MeasureModeChooser(onARMode: () -> Unit, onManualMode: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.Straighten, contentDescription = null,
            modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)

        Spacer(Modifier.height(24.dp))
        Text("Measure Your Room", style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Choose how you want to measure your room dimensions",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(40.dp))

        // AR Scan option
        MeasureOptionCard(
            title = "AR Room Scan",
            subtitle = "Point your camera at room corners for automatic measurement",
            icon = Icons.Filled.ViewInAr,
            badge = "ACCURATE",
            badgeColor = MaterialTheme.colorScheme.primary,
            onClick = onARMode
        )

        Spacer(Modifier.height(16.dp))

        // Manual option
        MeasureOptionCard(
            title = "Manual Entry",
            subtitle = "Enter room dimensions manually using a tape measure",
            icon = Icons.Outlined.Edit,
            badge = "QUICK",
            badgeColor = MaterialTheme.colorScheme.secondary,
            onClick = onManualMode
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasureOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badge: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = badgeColor.copy(alpha = 0.12f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null,
                        tint = badgeColor, modifier = Modifier.size(28.dp))
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Surface(shape = RoundedCornerShape(4.dp),
                        color = badgeColor.copy(alpha = 0.15f)) {
                        Text(badge, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall, color = badgeColor,
                            fontWeight = FontWeight.Bold)
                    }
                }
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Icon(Icons.Filled.ChevronRight, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─── AR Measurement ──────────────────────────────────────────────────────────

@Composable
private fun ARMeasurementView(
    onComplete: (Float, Float, Float) -> Unit,
    onBack: () -> Unit
) {
    var scanState by remember { mutableStateOf(ARScanState.SCANNING) }
    var progress by remember { mutableStateOf(0f) }
    var measuredWidth by remember { mutableStateOf(380f) }
    var measuredLength by remember { mutableStateOf(520f) }
    var measuredHeight by remember { mutableStateOf(260f) }

    val progressAnim by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(400)
    )

    // Simulate AR scan
    LaunchedEffect(scanState) {
        if (scanState == ARScanState.SCANNING) {
            repeat(10) { i ->
                kotlinx.coroutines.delay(500)
                progress = (i + 1) / 10f
            }
            measuredWidth  = (320f..450f).random()
            measuredLength = (400f..600f).random()
            measuredHeight = (240f..280f).random()
            scanState = ARScanState.COMPLETE
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Simulated camera view
        Box(
            modifier = Modifier.fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))
                    )
                )
        )

        // Scanning overlay grid
        if (scanState == ARScanState.SCANNING) {
            ARScanOverlay(progress = progressAnim)
        }

        // Top controls
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = onBack,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.4f))) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
            }

            Surface(shape = RoundedCornerShape(8.dp), color = Color.Black.copy(alpha = 0.4f)) {
                Text(
                    text = when (scanState) {
                        ARScanState.SCANNING -> "Scanning room…"
                        ARScanState.COMPLETE -> "✅ Scan complete"
                    },
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        // Bottom panel
        Surface(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {

                if (scanState == ARScanState.SCANNING) {
                    Text("Detecting room boundaries…",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Walk slowly around the room perimeter",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LinearProgressIndicator(progress = { progressAnim },
                        modifier = Modifier.fillMaxWidth())
                    Text("${(progressAnim * 100).toInt()}% scanned",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Measurement Complete!",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "Width" to "${measuredWidth.toInt()} cm",
                            "Length" to "${measuredLength.toInt()} cm",
                            "Height" to "${measuredHeight.toInt()} cm"
                        ).forEach { (label, value) ->
                            Surface(modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer) {
                                Column(modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(value, style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text(label, style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { scanState = ARScanState.SCANNING; progress = 0f },
                            modifier = Modifier.weight(1f)) { Text("Re-scan") }
                        Button(onClick = { onComplete(measuredWidth, measuredLength, measuredHeight) },
                            modifier = Modifier.weight(1f)) { Text("Use These Measurements") }
                    }
                }
            }
        }
    }
}

enum class ARScanState { SCANNING, COMPLETE }

@Composable
private fun ARScanOverlay(progress: Float) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val color = Color(0xFF4AF0C4)
        val gridSpacing = 40.dp.toPx()
        val cols = (size.width / gridSpacing).toInt() + 1
        val rows = (size.height / gridSpacing).toInt() + 1
        val visibleCols = (cols * progress).toInt()
        val visibleRows = (rows * progress).toInt()

        for (i in 0..visibleCols) {
            drawLine(color.copy(alpha = 0.3f),
                start = androidx.compose.ui.geometry.Offset(i * gridSpacing, 0f),
                end   = androidx.compose.ui.geometry.Offset(i * gridSpacing, size.height), strokeWidth = 1f)
        }
        for (i in 0..visibleRows) {
            drawLine(color.copy(alpha = 0.3f),
                start = androidx.compose.ui.geometry.Offset(0f, i * gridSpacing),
                end   = androidx.compose.ui.geometry.Offset(size.width, i * gridSpacing), strokeWidth = 1f)
        }

        // Corner brackets
        val bracketSize = 30.dp.toPx()
        val bw = 2.5f
        listOf(
            androidx.compose.ui.geometry.Offset(20f, 20f),
            androidx.compose.ui.geometry.Offset(size.width - 20f, 20f),
            androidx.compose.ui.geometry.Offset(20f, size.height - 20f),
            androidx.compose.ui.geometry.Offset(size.width - 20f, size.height - 20f)
        ).forEachIndexed { idx, corner ->
            val dx = if (idx % 2 == 0) 1f else -1f
            val dy = if (idx < 2) 1f else -1f
            drawLine(color, corner, corner.copy(x = corner.x + bracketSize * dx), strokeWidth = bw)
            drawLine(color, corner, corner.copy(y = corner.y + bracketSize * dy), strokeWidth = bw)
        }
    }
}

// ─── Manual Measurement Form ──────────────────────────────────────────────────

@Composable
private fun ManualMeasurementForm(
    onComplete: (Float, Float, Float) -> Unit,
    onBack: () -> Unit
) {
    var width by remember { mutableStateOf("380") }
    var length by remember { mutableStateOf("520") }
    var height by remember { mutableStateOf("260") }

    val isValid = listOf(width, length, height).all { it.toFloatOrNull()?.let { v -> v > 0 } == true }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        .padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {

        Text("Enter Room Dimensions",
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Use a tape measure for best accuracy. All measurements in centimeters.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        // Diagram
        Surface(shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth().height(180.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text("📐  Width × Length × Height",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Inputs
        listOf(
            Triple("Width (cm)", width) { v: String -> width = v },
            Triple("Length (cm)", length) { v: String -> length = v },
            Triple("Ceiling Height (cm)", height) { v: String -> height = v }
        ).forEach { (label, value, onChange) ->
            OutlinedTextField(
                value = value, onValueChange = onChange,
                label = { Text(label) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                isError = value.isNotBlank() && value.toFloatOrNull() == null,
                supportingText = { if (value.isNotBlank() && value.toFloatOrNull() == null)
                    Text("Please enter a valid number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Button(
            onClick = {
                onComplete(
                    width.toFloat(), length.toFloat(), height.toFloat()
                )
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Start Designing", style = MaterialTheme.typography.titleSmall)
        }

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to options")
        }
    }
}

// Helper extension
private fun ClosedFloatingPointRange<Float>.random(): Float =
    start + (endInclusive - start) * kotlin.random.Random.nextFloat()
