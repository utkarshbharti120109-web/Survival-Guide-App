package com.example.data

import kotlinx.coroutines.flow.Flow

class SurvivalRepository(private val dao: SurvivalDao) {

    val allKitItems: Flow<List<KitItem>> = dao.getAllKitItems()
    val allChecklistItems: Flow<List<ChecklistItem>> = dao.getAllChecklistItems()
    val allContacts: Flow<List<EmergencyContact>> = dao.getAllContacts()
    val allLocations: Flow<List<SavedLocation>> = dao.getAllLocations()
    val allReminders: Flow<List<PreparednessReminder>> = dao.getAllReminders()

    fun getChecklistByType(type: String): Flow<List<ChecklistItem>> = dao.getChecklistByType(type)

    suspend fun insertKitItem(item: KitItem) = dao.insertKitItem(item)
    suspend fun updateKitItem(item: KitItem) = dao.updateKitItem(item)
    suspend fun deleteKitItem(item: KitItem) = dao.deleteKitItem(item)
    suspend fun deleteKitItemById(id: Int) = dao.deleteKitItemById(id)

    suspend fun insertChecklistItems(items: List<ChecklistItem>) = dao.insertChecklistItems(items)
    suspend fun updateChecklistItem(item: ChecklistItem) = dao.updateChecklistItem(item)

    suspend fun insertContact(contact: EmergencyContact) = dao.insertContact(contact)
    suspend fun deleteContact(contact: EmergencyContact) = dao.deleteContact(contact)

    suspend fun insertLocation(location: SavedLocation) = dao.insertLocation(location)
    suspend fun deleteLocation(location: SavedLocation) = dao.deleteLocation(location)

    suspend fun insertReminder(reminder: PreparednessReminder) = dao.insertReminder(reminder)
    suspend fun updateReminder(reminder: PreparednessReminder) = dao.updateReminder(reminder)
    suspend fun deleteReminder(reminder: PreparednessReminder) = dao.deleteReminder(reminder)

    suspend fun populateDefaultsIfEmpty(
        defaultKitItems: List<KitItem>,
        defaultChecklistItems: List<ChecklistItem>,
        currentKitSize: Int,
        currentChecklistSize: Int
    ) {
        if (currentKitSize == 0) {
            for (item in defaultKitItems) {
                dao.insertKitItem(item)
            }
        }
        if (currentChecklistSize == 0) {
            dao.insertChecklistItems(defaultChecklistItems)
        }
    }
}
