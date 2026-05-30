package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kit_items")
data class KitItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isOwned: Boolean,
    val category: String,
    val quantity: Int = 1
)

@Entity(tableName = "checklist_items")
data class ChecklistItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val checklistType: String, // e.g. "earthquake", "flood", "fire", "storm", "power_outage", "landslide", "family_plan"
    val text: String,
    val isCompleted: Boolean
)

@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val category: String // "Medical", "Family", "Local"
)

@Entity(tableName = "saved_locations")
data class SavedLocation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "preparedness_reminders")
data class PreparednessReminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val dateTimeMs: Long,
    val isCompleted: Boolean = false,
    val category: String // "Kit Inspection", "Medicine Expiration", "Preparedness Training", "Contact Update"
)
