package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.data.KitItem
import com.example.data.ChecklistItem
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.SurvivalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: SurvivalViewModel,
    onNavigateToTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val kitItems by viewModel.kitItems.collectAsStateWithLifecycle()
    val checklistItems by viewModel.checklistItems.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    val readinessScore by viewModel.readinessScoreFlow.collectAsStateWithLifecycle(initialValue = 0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // Red Emergency Disclaimer Bar
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Guidance Alert",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "DISCLAIMER: Informational guidance only. Not a medical or official emergency substitute.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Tactical Score Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("readiness_score_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "OVERALL READINESS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$readinessScore%",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 42.sp
                            ),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Score is dynamically calculated based on completed checklists (${checklistItems.checklistItemCompletedOfTotal}) and owned supplies items (${kitItems.kitItemCompletedOfTotal}).",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Circular Readiness Gauge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(84.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.08f),
                                shape = CircleShape
                            )
                    ) {
                        CircularProgressIndicator(
                            progress = { readinessScore / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 8.dp,
                            trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f),
                        )
                        Text(
                            text = "Lv. ${if (readinessScore > 90) 5 else if (readinessScore > 70) 4 else if (readinessScore > 50) 3 else if (readinessScore > 30) 2 else 1}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // Quick Tactical Utility Controls
        item {
            Text(
                text = "STANDBY UTILITIES",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SOS Flare
                val isSosOn = viewModel.isFlashlightOn && viewModel.isAlarmPlaying
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (isSosOn) {
                                if (viewModel.isFlashlightOn) viewModel.toggleFlashlight(context)
                                if (viewModel.isAlarmPlaying) viewModel.toggleAlarm()
                            } else {
                                if (!viewModel.isFlashlightOn) viewModel.toggleFlashlight(context)
                                if (!viewModel.isAlarmPlaying) viewModel.toggleAlarm()
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(
                                color = if (isSosOn) Color(0xFFFFCC00) else if (isSystemInDarkTheme()) Color(0xFF3B1E1D) else Color(0xFFF9DEDC),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🚨", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "SOS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Flash Button
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.toggleFlashlight(context) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(
                                color = if (viewModel.isFlashlightOn) MaterialTheme.colorScheme.primary else if (isSystemInDarkTheme()) Color(0xFF2E2431) else Color(0xFFE7E0EB),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🔦", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (viewModel.isFlashlightOn) "Flash ON" else "Flash",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Map Button
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToTab(3) }, // Navigates to Contacts & Maps Tab
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(
                                color = if (isSystemInDarkTheme()) Color(0xFF2E2431) else Color(0xFFE7E0EB),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📍", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Map",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Alarm Button
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.toggleAlarm() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(
                                color = if (viewModel.isAlarmPlaying) Color(0xFFFFB4AB) else if (isSystemInDarkTheme()) Color(0xFF2E2431) else Color(0xFFE7E0EB),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📢", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (viewModel.isAlarmPlaying) "Alarm ON" else "Alarm",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Navigation shortcuts
        item {
            Text(
                text = "SURVIVAL MISSIONS",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DashboardShortcutRow(
                        icon = Icons.Default.LocalHospital,
                        title = "Read First Aid Instructions",
                        description = "Immediate guides for burns, bleeding, choking & more.",
                        onClick = { onNavigateToTab(1) } // First Aid Tab
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    DashboardShortcutRow(
                        icon = Icons.Default.BusinessCenter,
                        title = "Build Emergency kit",
                        description = "Track survival water, non-perishables, and tools matches.",
                        onClick = { onNavigateToTab(2) } // Bags & checklists Tab
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    DashboardShortcutRow(
                        icon = Icons.Default.Map,
                        title = "Contacts & Maps",
                        description = "Save emergency family cards and secure safe checkpoint nodes.",
                        onClick = { onNavigateToTab(3) } // Contacts Location Tab
                    )
                }
            }
        }

        // Monthly Preparedness Challenge
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Challenge Award Icon",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MONTHLY DEFENDER CHALLENGE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Home Expiry Inspection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Check the seals of your home fire extinguishers and replace any medications that expire this month.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // Earned Badges Showcase
        item {
            Text(
                text = "EARNED MILITARY BADGES",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val packMasterEarned = kitItems.count { it.isOwned } >= 5
                val responderEarned = checklistItems.count { it.isCompleted } >= 8
                val guardianEarned = contacts.isNotEmpty()
                val signalmanEarned = locations.isNotEmpty()

                BadgeWidget(
                    title = "Pack Master",
                    icon = Icons.Default.Backpack,
                    isEarned = packMasterEarned,
                    modifier = Modifier.weight(1f)
                )

                BadgeWidget(
                    title = "First Core",
                    icon = Icons.Default.HealthAndSafety,
                    isEarned = responderEarned,
                    modifier = Modifier.weight(1f)
                )

                BadgeWidget(
                    title = "Guardian",
                    icon = Icons.Default.ContactPhone,
                    isEarned = guardianEarned,
                    modifier = Modifier.weight(1f)
                )

                BadgeWidget(
                    title = "Signalman",
                    icon = Icons.Default.Signpost,
                    isEarned = signalmanEarned,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DashboardShortcutRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun BadgeWidget(
    title: String,
    icon: ImageVector,
    isEarned: Boolean,
    modifier: Modifier = Modifier
) {
    val alpha = if (isEarned) 1.0f else 0.4f
    val containerBg = if (isEarned) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
    val borderStroke = if (isEarned) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(containerColor = containerBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (isEarned) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "$title Badge",
                    tint = if (isEarned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
                color = if (isEarned) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Inline dynamic helper strings to avoid compiler dependencies calculation errors
private val Dashboard_kitItemsDummy = emptyList<KitItem>()
private inline val List<KitItem>.kitItemCompletedOfTotal: String
    get() {
        val total = this.size
        val completed = this.count { it.isOwned }
        return "$completed/$total"
    }

private inline val List<ChecklistItem>.checklistItemCompletedOfTotal: String
    get() {
        val total = this.size
        val completed = this.count { it.isCompleted }
        return "$completed/$total"
    }
