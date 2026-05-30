package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SurvivalDao {

    // Kit Items
    @Query("SELECT * FROM kit_items ORDER BY category ASC, name ASC")
    fun getAllKitItems(): Flow<List<KitItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKitItem(item: KitItem): Long

    @Update
    suspend fun updateKitItem(item: KitItem)

    @Delete
    suspend fun deleteKitItem(item: KitItem)

    @Query("DELETE FROM kit_items WHERE id = :id")
    suspend fun deleteKitItemById(id: Int)


    // Checklist Items
    @Query("SELECT * FROM checklist_items WHERE checklistType = :type")
    fun getChecklistByType(type: String): Flow<List<ChecklistItem>>

    @Query("SELECT * FROM checklist_items")
    fun getAllChecklistItems(): Flow<List<ChecklistItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertChecklistItems(items: List<ChecklistItem>)

    @Update
    suspend fun updateChecklistItem(item: ChecklistItem)


    // Emergency Contacts
    @Query("SELECT * FROM emergency_contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<EmergencyContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContact): Long

    @Delete
    suspend fun deleteContact(contact: EmergencyContact)


    // Saved Locations
    @Query("SELECT * FROM saved_locations ORDER BY timestamp DESC")
    fun getAllLocations(): Flow<List<SavedLocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: SavedLocation): Long

    @Delete
    suspend fun deleteLocation(location: SavedLocation)


    // Reminders
    @Query("SELECT * FROM preparedness_reminders ORDER BY dateTimeMs ASC")
    fun getAllReminders(): Flow<List<PreparednessReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: PreparednessReminder): Long

    @Update
    suspend fun updateReminder(reminder: PreparednessReminder)

    @Delete
    suspend fun deleteReminder(reminder: PreparednessReminder)
}
