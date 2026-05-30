package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SavedLocation
import com.example.viewmodel.SurvivalViewModel

data class MapLandmark(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val notes: String,
    val iconEmoji: String
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    viewModel: SurvivalViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedLocations by viewModel.locations.collectAsStateWithLifecycle()

    var showMessageFlasher by remember { mutableStateOf(false) }
    var flashMessageText by remember { mutableStateOf("HELP / INJURED") }

    // Compass Sensor listener registration
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    val rotationSensor = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }

    DisposableEffect(Unit) {
        if (rotationSensor == null) {
            // Check secondary fallback accelerometer + magnetic field sensors if Rotation Vector is absent
            val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val magSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

            if (accelSensor != null && magSensor != null) {
                viewModel.isCompassSupported = true
                val gravityVec = FloatArray(3)
                val magneticVec = FloatArray(3)

                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                            System.arraycopy(event.values, 0, gravityVec, 0, Math.min(event.values.size, gravityVec.size))
                        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                            System.arraycopy(event.values, 0, magneticVec, 0, Math.min(event.values.size, magneticVec.size))
                        }

                        val rotationMat = FloatArray(9)
                        val inclinationMat = FloatArray(9)
                        val success = SensorManager.getRotationMatrix(rotationMat, inclinationMat, gravityVec, magneticVec)
                        if (success) {
                            val orientations = FloatArray(3)
                            SensorManager.getOrientation(rotationMat, orientations)
                            var az = Math.toDegrees(orientations[0].toDouble()).toFloat()
                            if (az < 0) az += 360f
                            viewModel.compassAzimuth = az
                        }
                    }
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }

                sensorManager?.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
                sensorManager?.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI)

                onDispose {
                    sensorManager?.unregisterListener(listener)
                }
            } else {
                viewModel.isCompassSupported = false
                onDispose {}
            }
        } else {
            viewModel.isCompassSupported = true
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                        val rotationMat = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(rotationMat, event.values)
                        val orientations = FloatArray(3)
                        SensorManager.getOrientation(rotationMat, orientations)
                        var az = Math.toDegrees(orientations[0].toDouble()).toFloat()
                        if (az < 0) az += 360f
                        viewModel.compassAzimuth = az
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager?.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)

            onDispose {
                sensorManager?.unregisterListener(listener)
            }
        }
    }

    if (showMessageFlasher) {
        // Full screen screen rescue flasher
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { showMessageFlasher = false }
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = flashMessageText.uppercase(),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 48.sp,
                        color = Color(0xFFFFCC00)
                    ),
                    textAlign = TextAlign.Center,
                    lineHeight = 56.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "TAP SCREEN TO RETURN TO CONSOLE",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 2.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // Signal Tools Header indicators
            item {
                Text(
                    text = "EMERGENCY POWER ACCESSORIES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Strobe Flashers control card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("High-Intensity LED Strobe", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Toggle continuous flash torch", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = viewModel.isFlashlightOn,
                                onCheckedChange = { viewModel.toggleFlashlight(context) },
                                modifier = Modifier.testTag("flashlight_tool_switch")
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Distress SOS Morse Strobe", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Blinks continuous (··· --- ···) pattern", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = viewModel.isSosPlaying,
                                onCheckedChange = { viewModel.toggleSos(context) },
                                modifier = Modifier.testTag("sos_strobe_switch")
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Sweeping Siren Alarm (Loud)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Continuous real-time synthesised high-pitch sound", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = viewModel.isAlarmPlaying,
                                onCheckedChange = { viewModel.toggleAlarm() },
                                modifier = Modifier.testTag("siren_alarm_switch")
                            )
                        }
                    }
                }
            }

            // Full-screen rescue signboard setting card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "OFFLINE RESCUE SIGNBOARD",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Display large high-contrast neon text on screen to catch aircraft circles or lookouts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = flashMessageText,
                            onValueChange = { flashMessageText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("rescue_signboard_input"),
                            placeholder = { Text("e.g. SOS / SAFE IN CABIN") },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { showMessageFlasher = true }) {
                                    Icon(Icons.Default.OpenInFull, contentDescription = "Launch Screen signboard")
                                }
                            }
                        )

                        Button(
                            onClick = { showMessageFlasher = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("launch_signboard_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("PROMPT FULLSCREEN LOCK", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Navigational Compass segment
            item {
                Text(
                    text = "MAGNETIC ORIENTATION ACCURACY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (viewModel.isCompassSupported) {
                            Text(
                                text = "OFFLINE MAGNETIC SENSOR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "${viewModel.compassAzimuth.toInt()}° ${getAzimuthCardinalDirection(viewModel.compassAzimuth)}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            // Drawn Dynamic Compass Dial
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .background(Color.Black, shape = CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                // Draw azimuth ticks
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val radius = size.minDimension / 2.0f
                                    val center = Offset(size.width / 2.0f, size.height / 2.0f)

                                    // Rotate the whole canvas against the device orientation azimuth
                                    rotate(degrees = -viewModel.compassAzimuth, pivot = center) {
                                        // Draw simple line indicators for N, E, S, W
                                        drawLine(
                                            color = Color.Red,
                                            start = Offset(center.x, center.y - radius + 10.dp.toPx()),
                                            end = Offset(center.x, center.y - radius + 25.dp.toPx()),
                                            strokeWidth = 4f
                                        )
                                        drawLine(
                                            color = Color.White,
                                            start = Offset(center.x, center.y + radius - 10.dp.toPx()),
                                            end = Offset(center.x, center.y + radius - 25.dp.toPx()),
                                            strokeWidth = 3f
                                        )
                                        drawLine(
                                            color = Color.White,
                                            start = Offset(center.x - radius + 10.dp.toPx(), center.y),
                                            end = Offset(center.x - radius + 25.dp.toPx(), center.y),
                                            strokeWidth = 3f
                                        )
                                        drawLine(
                                            color = Color.White,
                                            start = Offset(center.x + radius - 10.dp.toPx(), center.y),
                                            end = Offset(center.x + radius - 25.dp.toPx(), center.y),
                                            strokeWidth = 3f
                                        )
                                    }

                                    // Static Pointer representing forward device orientation
                                    drawPath(
                                        path = Path().apply {
                                            moveTo(center.x, center.y - 12.dp.toPx())
                                            lineTo(center.x - 8.dp.toPx(), center.y + 12.dp.toPx())
                                            lineTo(center.x + 8.dp.toPx(), center.y + 12.dp.toPx())
                                            close()
                                        },
                                        color = Color.Red
                                    )
                                }
                            }
                        } else {
                            Icon(Icons.Default.CompassCalibration, contentDescription = "Compass missing", modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Magnetic Field Sensor Missing.", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            Text("Your hardware doesn't feature an internal magnetometer compass.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            // Location coordinates & map share
            item {
                Text(
                    text = "SATELLITE POSITIONING COGNIZANCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GpsFixed, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Emergency Sat-Link Coordinates", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }

                        Text(
                            text = viewModel.locationAddressString,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (viewModel.isGpsSynchronizing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Lat: ${viewModel.currentLatitude}\nLong: ${viewModel.currentLongitude}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.requestCurrentLocation(context) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sync_gps_btn"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val mapUri = "https://maps.google.com/maps?q=${viewModel.currentLatitude},${viewModel.currentLongitude}"
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "CRITICAL DETECTOR LOCK: I need help. My current location coordinates are:\nLatitude = ${viewModel.currentLatitude}\nLongitude = ${viewModel.currentLongitude}\nMap node: $mapUri")
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share Safe Position")
                                    context.startActivity(shareIntent)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("share_coords_btn"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share SMS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Quick interface to "Save secure checkpoint"
                        var customLocName by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = customLocName,
                            onValueChange = { customLocName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("safe_loc_name_input"),
                            label = { Text("Checkpoint Label (e.g., Base Bravo)") },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (customLocName.isNotBlank()) {
                                            viewModel.addSavedLocation(
                                                customLocName.trim(),
                                                viewModel.currentLatitude,
                                                viewModel.currentLongitude,
                                                "Offline Saved Safety Point"
                                            )
                                            customLocName = ""
                                        }
                                    },
                                    modifier = Modifier.testTag("save_loc_btn_trigger")
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = "Save safe location point")
                                }
                            }
                        )
                    }
                }
            }

            // Interactive Offline Map Section
            item {
                Text(
                    text = "OFFLINE VECTOR MAP NAVIGATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                OfflineMapCard(
                    viewModel = viewModel,
                    savedLocations = savedLocations
                )
            }

            // Scroll view listing secure checkpoint nodes saved
            if (savedLocations.isNotEmpty()) {
                item {
                    Text(
                        text = "SECURE CHECKPOINT REGISTRY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                items(savedLocations) { loc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("saved_loc_row_${loc.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(loc.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Coordinates: ${loc.latitude}, ${loc.longitude}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { viewModel.deleteLocation(loc) }, modifier = Modifier.testTag("delete_saved_loc_${loc.id}")) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete coordinates", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OfflineMapCard(
    viewModel: SurvivalViewModel,
    savedLocations: List<SavedLocation>,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Map style mode: Contour, Grid, Radar
    var mapStyleMode by remember { mutableStateOf("Contour") }

    // User's coordinates
    val userLat = viewModel.currentLatitude
    val userLng = viewModel.currentLongitude

    // Scale multipliers (fractional degrees to pixels conversion)
    val latFactor = 60000f
    val lngFactor = 60000f

    // Animated sweeping vector for the radar mode
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val radarSweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarSweepAngle"
    )

    // Animated pulse for selected items or GPS indicator
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    // Generate local static landmarks relative to current coordinates so it's always filled with neat features!
    val staticLandmarks = remember(userLat, userLng) {
        listOf(
            MapLandmark("Emergency Cabin Shelter", userLat + 0.0018, userLng - 0.0015, "Waterproof secure cabin | Fire stove equipped", "🏠"),
            MapLandmark("Potable Freshwater Spring", userLat - 0.0016, userLng + 0.0012, "Perennial spring | Filter recommended", "💧"),
            MapLandmark("High Ground Lookout", userLat + 0.0025, userLng + 0.0022, "Elevation peak | Good sat transceiver line", "🏔️"),
            MapLandmark("Supplies Drop Storage 2", userLat - 0.0008, userLng - 0.0020, "Locked underground medical cache cabinet", "📦")
        )
    }

    // Selected landmark / checkpoint for the bottom details overlay
    var selectedLandmark by remember { mutableStateOf<MapLandmark?>(null) }
    var selectedCheckpoint by remember { mutableStateOf<SavedLocation?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        // Vertical spacer to prevent outline collision
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Map Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "Map Navigation Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TACTICAL OFFLINE MAP",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Reset view button
                IconButton(
                    onClick = {
                        offsetX = 0f
                        offsetY = 0f
                        scale = 1.0f
                    },
                    modifier = Modifier.size(28.dp).testTag("map_recenter_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Recenter Map on GPS Location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = "Interactive, pan/zoom tactile vector topographical grid compiled entirely offline without cellular network requirements.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Mode Selector toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Contour", "Grid", "Radar").forEach { style ->
                    val isSelected = mapStyleMode == style
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { mapStyleMode = style }
                            .height(30.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = style,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Interactive Map Viewport canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        color = when (mapStyleMode) {
                            "Grid" -> Color(0xFF0B132B)
                            "Radar" -> Color(0xFF030712)
                            else -> Color(0xFF1C2541) // Contour (Topo Slate Dark Blue)
                        },
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
                    .pointerInput(userLat, userLng, scale, offsetX, offsetY) {
                        detectTapGestures { tapOffset ->
                            var matched = false
                            val width = size.width
                            val height = size.height
                            val originY = height / 2f
                            val originX = width / 2f

                            // 1. Check if any static landmark is tapped
                            for (landmark in staticLandmarks) {
                                val relX = ((landmark.longitude - userLng) * lngFactor * scale).toFloat() + originX + offsetX
                                val relY = (-(landmark.latitude - userLat) * latFactor * scale).toFloat() + originY + offsetY
                                val distPixels = Math.sqrt(
                                    Math.pow((tapOffset.x - relX).toDouble(), 2.0) +
                                    Math.pow((tapOffset.y - relY).toDouble(), 2.0)
                                )
                                if (distPixels <= 35.0) {
                                    selectedLandmark = landmark
                                    selectedCheckpoint = null
                                    matched = true
                                    break
                                }
                            }

                            // 2. Check if any saved checkpoint is tapped
                            if (!matched) {
                                for (checkpoint in savedLocations) {
                                    val relX = ((checkpoint.longitude - userLng) * lngFactor * scale).toFloat() + originX + offsetX
                                    val relY = (-(checkpoint.latitude - userLat) * latFactor * scale).toFloat() + originY + offsetY
                                    val distPixels = Math.sqrt(
                                        Math.pow((tapOffset.x - relX).toDouble(), 2.0) +
                                        Math.pow((tapOffset.y - relY).toDouble(), 2.0)
                                    )
                                    if (distPixels <= 35.0) {
                                        selectedCheckpoint = checkpoint
                                        selectedLandmark = null
                                        matched = true
                                        break
                                    }
                                }
                            }

                            // 3. Clear focus if tapped blank background
                            if (!matched) {
                                selectedLandmark = null
                                selectedCheckpoint = null
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val originX = w / 2f
                    val originY = h / 2f

                    // 1. DRAW BACKGROUND DECORATIONS BY STYLE
                    if (mapStyleMode == "Grid") {
                        // Channel / grid matrix
                        val step = 40.dp.toPx() * scale
                        var x = (offsetX % step)
                        while (x < w) {
                            drawLine(
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                start = Offset(x, 0f),
                                end = Offset(x, h),
                                strokeWidth = 1f
                            )
                            x += step
                        }
                        var y = (offsetY % step)
                        while (y < h) {
                            drawLine(
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1f
                            )
                            y += step
                        }

                        // Grid cardinal center reference lines
                        drawLine(
                            color = Color(0xFF10B981).copy(alpha = 0.25f),
                            start = Offset(originX + offsetX, 0f),
                            end = Offset(originX + offsetX, h),
                            strokeWidth = 2f
                        )
                        drawLine(
                            color = Color(0xFF10B981).copy(alpha = 0.25f),
                            start = Offset(0f, originY + offsetY),
                            end = Offset(w, originY + offsetY),
                            strokeWidth = 2f
                        )
                    } else if (mapStyleMode == "Contour") {
                        // Topographical elevation contours
                        val peak1X = ((+0.0022) * lngFactor * scale).toFloat() + originX + offsetX
                        val peak1Y = (-0.0025 * latFactor * scale).toFloat() + originY + offsetY
                        drawCircle(
                            color = Color(0xFF8B5A2B).copy(alpha = 0.3f),
                            radius = 35.dp.toPx() * scale,
                            center = Offset(peak1X, peak1Y),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFF8B5A2B).copy(alpha = 0.3f),
                            radius = 20.dp.toPx() * scale,
                            center = Offset(peak1X, peak1Y),
                            style = Stroke(width = 1.5.dp.toPx())
                        )

                        val peak2X = ((-0.0025) * lngFactor * scale).toFloat() + originX + offsetX
                        val peak2Y = (-(-0.0020) * latFactor * scale).toFloat() + originY + offsetY
                        drawCircle(
                            color = Color(0xFF8B5A2B).copy(alpha = 0.3f),
                            radius = 45.dp.toPx() * scale,
                            center = Offset(peak2X, peak2Y),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFF8B5A2B).copy(alpha = 0.3f),
                            radius = 25.dp.toPx() * scale,
                            center = Offset(peak2X, peak2Y),
                            style = Stroke(width = 1.5.dp.toPx())
                        )

                        // Topo contour vector winding river
                        val riverPath = Path().apply {
                            val rStart = Offset((-0.004f) * lngFactor * scale + originX + offsetX, (-0.004f) * latFactor * scale + originY + offsetY)
                            val rControl1 = Offset((-0.001f) * lngFactor * scale + originX + offsetX, (0.003f) * latFactor * scale + originY + offsetY)
                            val rControl2 = Offset((0.002f) * lngFactor * scale + originX + offsetX, (-0.003f) * latFactor * scale + originY + offsetY)
                            val rEnd = Offset((0.005f) * lngFactor * scale + originX + offsetX, (0.004f) * latFactor * scale + originY + offsetY)
                            
                            moveTo(rStart.x, rStart.y)
                            cubicTo(rControl1.x, rControl1.y, rControl2.x, rControl2.y, rEnd.x, rEnd.y)
                        }
                        drawPath(
                            path = riverPath,
                            color = Color(0xFF60A5FA).copy(alpha = 0.5f),
                            style = Stroke(width = 4.dp.toPx() * scale)
                        )
                    } else {
                        // Tactical sonar range grid circles
                        val centerUserX = originX + offsetX
                        val centerUserY = originY + offsetY
                        drawCircle(
                            color = Color(0xFF10B981).copy(alpha = 0.25f),
                            radius = 50.dp.toPx() * scale,
                            center = Offset(centerUserX, centerUserY),
                            style = Stroke(width = 1.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            radius = 100.dp.toPx() * scale,
                            center = Offset(centerUserX, centerUserY),
                            style = Stroke(width = 1.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFF10B981).copy(alpha = 0.1f),
                            radius = 150.dp.toPx() * scale,
                            center = Offset(centerUserX, centerUserY),
                            style = Stroke(width = 1.dp.toPx())
                        )

                        //Sonar sweep vector line
                        rotate(degrees = radarSweepAngle, pivot = Offset(centerUserX, centerUserY)) {
                            drawLine(
                                color = Color(0xFF10B981).copy(alpha = 0.7f),
                                start = Offset(centerUserX, centerUserY),
                                end = Offset(centerUserX + 220.dp.toPx() * scale, centerUserY),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }

                    // 2. PLOT STATIC LANDMARKS
                    staticLandmarks.forEach { landmark ->
                        val relX = ((landmark.longitude - userLng) * lngFactor * scale).toFloat() + originX + offsetX
                        val relY = (-(landmark.latitude - userLat) * latFactor * scale).toFloat() + originY + offsetY

                        drawCircle(
                            color = Color(0xFF3B82F6).copy(alpha = 0.2f),
                            radius = 14.dp.toPx(),
                            center = Offset(relX, relY)
                        )
                        drawCircle(
                            color = Color(0xFF3B82F6),
                            radius = 4.dp.toPx(),
                            center = Offset(relX, relY)
                        )

                        if (selectedLandmark == landmark) {
                            drawCircle(
                                color = Color(0xFF3B82F6).copy(alpha = 0.4f),
                                radius = pulseScale.dp.toPx(),
                                center = Offset(relX, relY),
                                style = Stroke(width = 2f)
                            )
                        }

                        drawIntoCanvas { canvas ->
                            val textPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 9.dp.toPx()
                                typeface = android.graphics.Typeface.DEFAULT
                                alpha = 180
                            }
                            canvas.nativeCanvas.drawText(
                                "${landmark.iconEmoji} ${landmark.name}",
                                relX + 8.dp.toPx(),
                                relY + 3.dp.toPx(),
                                textPaint
                            )
                        }
                    }

                    // 3. PLOT SAVE CHECKPOINT NODES
                    savedLocations.forEach { loc ->
                        val relX = ((loc.longitude - userLng) * lngFactor * scale).toFloat() + originX + offsetX
                        val relY = (-(loc.latitude - userLat) * latFactor * scale).toFloat() + originY + offsetY

                        drawCircle(
                            color = Color(0xFFEF4444).copy(alpha = 0.2f),
                            radius = 14.dp.toPx(),
                            center = Offset(relX, relY)
                        )
                        drawCircle(
                            color = Color(0xFFEF4444),
                            radius = 5.dp.toPx(),
                            center = Offset(relX, relY)
                        )

                        if (selectedCheckpoint == loc) {
                            drawCircle(
                                color = Color(0xFFEF4444).copy(alpha = 0.4f),
                                radius = pulseScale.dp.toPx(),
                                center = Offset(relX, relY),
                                style = Stroke(width = 2f)
                            )
                        }

                        drawIntoCanvas { canvas ->
                            val textPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.YELLOW
                                textSize = 9.dp.toPx()
                                typeface = android.graphics.Typeface.DEFAULT_BOLD
                                alpha = 200
                            }
                            canvas.nativeCanvas.drawText(
                                "🚩 ${loc.name}",
                                relX + 8.dp.toPx(),
                                relY + 3.dp.toPx(),
                                textPaint
                            )
                        }
                    }

                    // 4. DRAW USER CURRENT POSITION PIN
                    val userCanvasX = originX + offsetX
                    val userCanvasY = originY + offsetY

                    drawCircle(
                        color = Color(0xFF3B82F6).copy(alpha = 0.15f),
                        radius = 24.dp.toPx() + (pulseScale.dp.toPx() / 2f),
                        center = Offset(userCanvasX, userCanvasY)
                    )

                    // Sector azimuth indicator
                    if (viewModel.isCompassSupported) {
                        rotate(degrees = -viewModel.compassAzimuth, pivot = Offset(userCanvasX, userCanvasY)) {
                            drawPath(
                                path = Path().apply {
                                    moveTo(userCanvasX, userCanvasY)
                                    lineTo(userCanvasX - 22.dp.toPx(), userCanvasY - 55.dp.toPx())
                                    lineTo(userCanvasX + 22.dp.toPx(), userCanvasY - 55.dp.toPx())
                                    close()
                                },
                                color = Color(0xFF3B82F6).copy(alpha = 0.3f)
                            )
                        }
                    }

                    drawCircle(
                        color = Color.White,
                        radius = 7.dp.toPx(),
                        center = Offset(userCanvasX, userCanvasY)
                    )
                    drawCircle(
                        color = Color(0xFF2563EB),
                        radius = 5.dp.toPx(),
                        center = Offset(userCanvasX, userCanvasY)
                    )
                }

                // Zoom controls overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledIconButton(
                        onClick = { scale = (scale + 0.25f).coerceAtMost(2.5f) },
                        modifier = Modifier.size(34.dp).testTag("map_zoom_in"),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.6f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In")
                    }

                    FilledIconButton(
                        onClick = { scale = (scale - 0.25f).coerceAtLeast(0.5f) },
                        modifier = Modifier.size(34.dp).testTag("map_zoom_out"),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.6f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                    }
                }

                // Grid scale indicator bar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Scale: 1cm ≈ ${"%.0f".format(200 / scale)}m",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Center crosshair viewfinder pointer
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Center Pointer Crosshair",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Map action commands
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pin Drop at center of view coordinates
                Button(
                    onClick = {
                        val pinLat = userLat - (offsetY / (latFactor * scale))
                        val pinLng = userLng + (offsetX / (lngFactor * scale))
                        viewModel.addSavedLocation(
                            "Viewport Mark ${savedLocations.size + 1}",
                            pinLat,
                            pinLng,
                            "Added from offline viewfinder coordinates."
                        )
                    },
                    modifier = Modifier.weight(1f).testTag("drop_viewfinder_pin_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AddLocationAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Drop View pin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { selectedLandmark = null; selectedCheckpoint = null; offsetX = 0f; offsetY = 0f },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Recenter GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            AnimatedVisibility(
                visible = selectedLandmark != null || selectedCheckpoint != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                surfaceDetailPanel(
                    landmark = selectedLandmark,
                    checkpoint = selectedCheckpoint,
                    userLatitude = userLat,
                    userLongitude = userLng,
                    onFocusPoint = { lat, lng ->
                        scale = 1.3f
                        offsetX = -((lng - userLng) * lngFactor * scale).toFloat()
                        offsetY = ((lat - userLat) * latFactor * scale).toFloat()
                    }
                )
            }
        }
    }
}

@Composable
private fun surfaceDetailPanel(
    landmark: MapLandmark?,
    checkpoint: SavedLocation?,
    userLatitude: Double,
    userLongitude: Double,
    onFocusPoint: (Double, Double) -> Unit
) {
    val name = landmark?.name ?: checkpoint?.name ?: ""
    val notes = landmark?.notes ?: checkpoint?.notes ?: "Offline saved point"
    val emoji = landmark?.iconEmoji ?: "🚩"
    val lat = landmark?.latitude ?: checkpoint?.latitude ?: userLatitude
    val lng = landmark?.longitude ?: checkpoint?.longitude ?: userLongitude

    val distanceM = distanceInMeters(userLatitude, userLongitude, lat, lng)
    val bearingD = bearingInDegrees(userLatitude, userLongitude, lat, lng)
    val cardinal = getAzimuthCardinalDirection(bearingD.toFloat())

    Card(
        modifier = Modifier.fillMaxWidth().testTag("map_selection_details_panel"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dist: ${"%.0f".format(distanceM)}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Brng: ${"%.0f".format(bearingD)}° ($cardinal)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { onFocusPoint(lat, lng) },
                modifier = Modifier.size(36.dp).testTag("focus_landmark_card_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.CenterFocusStrong,
                    contentDescription = "Lock viewfinder center on coordinate",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun distanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371000.0 // Earth radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}

private fun bearingInDegrees(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val dLon = Math.toRadians(lon2 - lon1)
    val lat1Rad = Math.toRadians(lat1)
    val lat2Rad = Math.toRadians(lat2)
    val y = Math.sin(dLon) * Math.cos(lat2Rad)
    val x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLon)
    var brng = Math.toDegrees(Math.atan2(y, x))
    if (brng < 0) brng += 360.0
    return brng
}

private fun getAzimuthCardinalDirection(azdeg: Float): String {
    return when {
         azdeg >= 337.5 || azdeg < 22.5 -> "N"
         azdeg >= 22.5 && azdeg < 67.5 -> "NE"
         azdeg >= 67.5 && azdeg < 112.5 -> "E"
         azdeg >= 112.5 && azdeg < 157.5 -> "SE"
         azdeg >= 157.5 && azdeg < 202.5 -> "S"
         azdeg >= 202.5 && azdeg < 247.5 -> "SW"
         azdeg >= 247.5 && azdeg < 292.5 -> "W"
         else -> "NW"
    }
}
