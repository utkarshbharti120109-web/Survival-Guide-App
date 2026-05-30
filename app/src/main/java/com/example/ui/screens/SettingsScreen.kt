package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.EmergencyContact
import com.example.viewmodel.SurvivalViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SurvivalViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()

    var showAddContactDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // --- 1. EMERGENCY CONTACTS ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SECURE PHONE CARD REGISTRY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Button(
                    onClick = { showAddContactDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier
                        .height(26.dp)
                        .testTag("add_contact_trigger"),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Contact", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // List emergency directory files
        if (contacts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No emergency contacts stored.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Save details of nearby medical centers or relatives so you can dial with a single tap.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(contacts) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_row_${item.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = if (item.category == "Medical") Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (item.category == "Medical") Icons.Default.LocalHospital else Icons.Default.FamilyRestroom,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(item.phoneNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // One-tap dialing trigger
                        IconButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.phoneNumber}"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Dialer unavailable", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("call_contact_${item.id}")
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "One-tap direct dial", tint = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(
                            onClick = { viewModel.deleteContact(item) },
                            modifier = Modifier.testTag("delete_contact_${item.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove contact", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // --- 2. PREPAREDNESS REMINDERS ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TACTICAL EXPIRY ALERTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Button(
                    onClick = { showAddReminderDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier
                        .height(26.dp)
                        .testTag("add_reminder_trigger"),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Alert", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (reminders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No safety reminders scheduled.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Schedule custom alarms for medicine expiration limits or backpack inventory checks.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(reminders) { alarm ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reminder_row_${alarm.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (alarm.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = alarm.isCompleted,
                            onCheckedChange = { viewModel.toggleReminderCompleted(alarm) },
                            modifier = Modifier.testTag("reminder_check_${alarm.id}")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = alarm.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (alarm.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Due: ${formatDateMs(alarm.dateTimeMs)} (${alarm.category})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.deleteReminder(alarm) },
                            modifier = Modifier.testTag("delete_reminder_${alarm.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove alert", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // --- 3. SYSTEM PREFERENCES & UTILITY ---
        item {
            Text(
                text = "COMMUNICATION CONFIGS",
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
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Theme Switcher row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Full UI Tactical Dark Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { onToggleTheme(it) },
                            modifier = Modifier.testTag("dark_mode_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Multiple Language selector row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Active Language Profile", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }
                        Button(
                            onClick = {
                                Toast.makeText(context, "Using offline English manual. Secondary packages downloaded.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("lang_switch_btn"),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("English (US)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Data backup model simulator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Data Backup & Recovery", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }
                        Button(
                            onClick = {
                                Toast.makeText(context, "Full offline cache file written to safe system partition.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("data_backup_btn"),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Backup Local Cache", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // --- 4. THE JURIDICAL DISCLAIMER (regulatory standard) ---
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = "Legal disclaimer", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REGULATORY SAFETY DECREE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "The materials and instructions displayed in Survival Guide are strictly compiled for offline instructional convenience. Sourcing wild water, signaling rescue parties, and administering emergency CPR should always conform to local disaster regulations and direct professional guidelines.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    // Modal dialog for adding emergency contact
    if (showAddContactDialog) {
        var contactName by remember { mutableStateOf("") }
        var contactPhone by remember { mutableStateOf("") }
        var contactCatSelected by remember { mutableStateOf("Family") }
        val catOptions = listOf("Family", "Medical")

        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = { Text("Save Contact Details", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = contactName,
                        onValueChange = { contactName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contact_name_input"),
                        label = { Text("Full Name / Station") },
                        placeholder = { Text("e.g., Mom cell, Red Cross Office") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )

                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contact_phone_input"),
                        label = { Text("Phone Number") },
                        placeholder = { Text("e.g., +1 234 567 890") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    // Simple radio selector category
                    Text("Directory Classification:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        catOptions.forEach { option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { contactCatSelected = option }
                            ) {
                                RadioButton(
                                    selected = contactCatSelected == option,
                                    onClick = { contactCatSelected = option }
                                )
                                Text(option, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (contactName.isNotBlank() && contactPhone.isNotBlank()) {
                            viewModel.addContact(contactName.trim(), contactPhone.trim(), contactCatSelected)
                            showAddContactDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_save_contact_btn")
                ) {
                    Text("Save Card")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal dialog to schedule alert timers
    if (showAddReminderDialog) {
        var alertTitle by remember { mutableStateOf("") }
        var typeAlertSelected by remember { mutableStateOf("Kit Inspection") }
        var triggerHoursDelay by remember { mutableStateOf("24") } // Default to 24 hours
        val reminderCategories = listOf("Kit Inspection", "Medicine Expiration", "Preparedness Training")

        AlertDialog(
            onDismissRequest = { showAddReminderDialog = false },
            title = { Text("Program Inspector Alert", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = alertTitle,
                        onValueChange = { alertTitle = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reminder_title_input"),
                        label = { Text("Alert Alert Label") },
                        placeholder = { Text("e.g. Replenish saline bottle, inspect generator fuel") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )

                    OutlinedTextField(
                        value = triggerHoursDelay,
                        onValueChange = { triggerHoursDelay = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reminder_time_input"),
                        label = { Text("Simulation Alert Delay (Hours)") },
                        placeholder = { Text("e.g., 24, 72, 168") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Text("Classification:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        reminderCategories.forEach { cat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { typeAlertSelected = cat }
                            ) {
                                RadioButton(
                                    selected = typeAlertSelected == cat,
                                    onClick = { typeAlertSelected = cat }
                                )
                                Text(cat, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val hours = triggerHoursDelay.trim().toIntOrNull() ?: 24
                        if (alertTitle.isNotBlank()) {
                            viewModel.addReminder(alertTitle.trim(), typeAlertSelected, hours)
                            showAddReminderDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_reminder_btn")
                ) {
                    Text("Establish Alert")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReminderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatDateMs(timeMs: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy (HH:mm)", Locale.getDefault())
    return formatter.format(Date(timeMs))
}
