package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ChecklistItem
import com.example.data.KitItem
import com.example.viewmodel.SurvivalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitsChecklistScreen(
    viewModel: SurvivalViewModel,
    modifier: Modifier = Modifier
) {
    var outerTabState by remember { mutableIntStateOf(0) } // 0 = Kit Builder, 1 = Checklists
    val kitItems by viewModel.kitItems.collectAsStateWithLifecycle()
    val checklistItems by viewModel.checklistItems.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Upper switcher
        TabRow(
            selectedTabIndex = outerTabState,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = outerTabState == 0,
                onClick = { outerTabState = 0 },
                modifier = Modifier.testTag("tab_kit_builder")
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Backpack, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Emergency Kit", fontWeight = FontWeight.Bold)
                }
            }
            Tab(
                selected = outerTabState == 1,
                onClick = { outerTabState = 1 },
                modifier = Modifier.testTag("tab_incident_checklists")
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Checklists", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (outerTabState == 0) {
            KitBuilderTabContent(
                kitItems = kitItems,
                onAddItem = { name, cat, qty -> viewModel.addKitItem(name, cat, qty) },
                onToggleOwned = { viewModel.toggleKitItemOwned(it) },
                onDeleteItem = { viewModel.deleteKitItem(it) }
            )
        } else {
            IncidentChecklistsTabContent(
                allItems = checklistItems,
                onToggleCheck = { viewModel.toggleChecklistItem(it) }
            )
        }
    }
}

// ============== EMERGENCY KIT BUILDER LAYOUT ==============

@Composable
fun KitBuilderTabContent(
    kitItems: List<KitItem>,
    onAddItem: (String, String, Int) -> Unit,
    onToggleOwned: (KitItem) -> Unit,
    onDeleteItem: (KitItem) -> Unit
) {
    var showAddItemDialog by remember { mutableStateOf(false) }

    // Aggregate statistics
    val totalMatched = kitItems.size
    val ownedCount = kitItems.count { it.isOwned }
    val progressPercent = if (totalMatched > 0) (ownedCount.toFloat() / totalMatched.toFloat() * 100).toInt() else 0

    Column(modifier = Modifier.fillMaxSize()) {
        // Progress Summary Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "KIT COMPLETENESS RATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$ownedCount of $totalMatched items owned",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { progressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (progressPercent > 80) Color(0xFF4CD964) else if (progressPercent > 50) Color(0xFFFFCC00) else Color(0xFFFF3B30),
                    trackColor = MaterialTheme.colorScheme.background
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Preparedness level: $progressPercent%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showAddItemDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("add_item_trigger"),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Manual Item", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // List of supply items matching groups
        if (kitItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Your tactical pack is empty. Touch 'Add Manual Item' model.")
            }
        } else {
            val categories = listOf("Water", "Food", "Medical", "Tools", "Shelter", "Other")

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                categories.forEach { category ->
                    val filteredItems = kitItems.filter { it.category == category }
                    if (filteredItems.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                ) {
                                    Column {
                                        filteredItems.forEachIndexed { idx, item ->
                                            KitSupplyRow(
                                                item = item,
                                                onToggle = { onToggleOwned(item) },
                                                onDelete = { onDeleteItem(item) }
                                            )
                                            if (idx < filteredItems.size - 1) {
                                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal adding drawer dialog
    if (showAddItemDialog) {
        var nameInput by remember { mutableStateOf("") }
        var categorySelected by remember { mutableStateOf("Water") }
        var qtyInput by remember { mutableIntStateOf(1) }
        val categoryOptions = listOf("Water", "Food", "Medical", "Tools", "Shelter", "Other")
        var isDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text("Add Custom Gear Supply", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_item_name"),
                        label = { Text("Item Name") },
                        placeholder = { Text("e.g. Canned Tuna, Duct Tape") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )

                    // Custom category picker dropdown menu
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { isDropdownExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_dropdown_trigger")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Category: $categorySelected", color = MaterialTheme.colorScheme.onSurface)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            categoryOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        categorySelected = opt
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Stepper for quantity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Target Quantity:")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (qtyInput > 1) qtyInput-- }) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease Quantity")
                            }
                            Text(
                                text = qtyInput.toString(),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            IconButton(onClick = { if (qtyInput < 99) qtyInput++ }) {
                                Icon(Icons.Default.Add, contentDescription = "Increase Quantity")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            onAddItem(nameInput.trim(), categorySelected, qtyInput)
                            showAddItemDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_item_btn")
                ) {
                    Text("Add Supply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun KitSupplyRow(
    item: KitItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kit_item_row_${item.id}")
            .clickable { onToggle() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isOwned,
            onCheckedChange = { onToggle() },
            modifier = Modifier.testTag("kit_item_checkbox_${item.id}")
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (item.isOwned) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Target Quantity: ${item.quantity}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.testTag("kit_item_delete_${item.id}")
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove supply",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}


// ============== EMERGENCY INCIDENT CHECKLISTS ==============

@Composable
fun IncidentChecklistsTabContent(
    allItems: List<ChecklistItem>,
    onToggleCheck: (ChecklistItem) -> Unit
) {
    var selectedType by remember { mutableStateOf("earthquake") }

    val chips = listOf(
        "earthquake" to "🏠 Earthquake",
        "flood" to "🌊 Flood",
        "fire" to "🔥 Fire",
        "storm" to "🌪️ Storm",
        "power_outage" to "🔌 Outage",
        "landslide" to "🏔️ Landslide",
        "family_plan" to "👨‍👩‍👧 Family Plan"
    )

    val currentList = allItems.filter { it.checklistType == selectedType }
    val totalInList = currentList.size
    val checkedInList = currentList.count { it.isCompleted }
    val checkProgress = if (totalInList > 0) checkedInList.toFloat() / totalInList.toFloat() else 0f

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal list selection chip row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chips) { (type, label) ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("chip_checklist_$type")
                )
            }
        }

        // Selected Checklist Score Indicator
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DISASTER CONFORMANCE RATIO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$checkedInList of $totalInList drills validated",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(54.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { checkProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.background,
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = "${(checkProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Actions listing scroll view
        if (currentList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Prepopulating items...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentList) { item ->
                    ChecklistItemRow(
                        item = item,
                        onClick = { onToggleCheck(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChecklistItemRow(
    item: ChecklistItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("checklist_row_${item.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (item.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onClick() },
                modifier = Modifier.testTag("checklist_check_${item.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (item.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
