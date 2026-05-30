package com.example.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SurvivalViewModel(private val repository: SurvivalRepository) : ViewModel() {

    // Database state flows
    val kitItems: StateFlow<List<KitItem>> = repository.allKitItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val checklistItems: StateFlow<List<ChecklistItem>> = repository.allChecklistItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contacts: StateFlow<List<EmergencyContact>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val locations: StateFlow<List<SavedLocation>> = repository.allLocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<PreparednessReminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Transient utility states
    var isFlashlightOn by mutableStateOf(false)
    var isSosPlaying by mutableStateOf(false)
    var isAlarmPlaying by mutableStateOf(false)
    var compassAzimuth by mutableStateOf(0f)
    var isCompassSupported by mutableStateOf(true)

    // Location States
    var currentLatitude by mutableStateOf(37.4220)
    var currentLongitude by mutableStateOf(-122.0841)
    var locationAddressString by mutableStateOf("Satellite sensor ready")
    var isGpsSynchronizing by mutableStateOf(false)

    // Alarm synthesis properties
    private var alarmJob: Job? = null
    private var audioTrack: AudioTrack? = null

    // SOS flashlight strobe job
    private var sosJob: Job? = null

    // Prepopulate database upon boot
    init {
        viewModelScope.launch {
            // Wait shortly for flows to emit, then populate defaults if database size is 0
            val sizeKits = kitItems.first().size
            val sizeChecklists = checklistItems.first().size
            repository.populateDefaultsIfEmpty(
                defaultKitItems = getDefaultKitItems(),
                defaultChecklistItems = getDefaultChecklists(),
                currentKitSize = sizeKits,
                currentChecklistSize = sizeChecklists
            )
        }
    }

    // --- DATABASE OPERATIONS ---

    // Kit Items
    fun addKitItem(name: String, category: String, quantity: Int) {
        viewModelScope.launch {
            repository.insertKitItem(KitItem(name = name, isOwned = false, category = category, quantity = quantity))
        }
    }

    fun toggleKitItemOwned(item: KitItem) {
        viewModelScope.launch {
            repository.updateKitItem(item.copy(isOwned = !item.isOwned))
        }
    }

    fun deleteKitItem(item: KitItem) {
        viewModelScope.launch {
            repository.deleteKitItem(item)
        }
    }

    // Checklists
    fun toggleChecklistItem(item: ChecklistItem) {
        viewModelScope.launch {
            repository.updateChecklistItem(item.copy(isCompleted = !item.isCompleted))
        }
    }

    // Contacts
    fun addContact(name: String, phoneNumber: String, category: String) {
        viewModelScope.launch {
            repository.insertContact(EmergencyContact(name = name, phoneNumber = phoneNumber, category = category))
        }
    }

    fun deleteContact(contact: EmergencyContact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }

    // Locations
    fun addSavedLocation(name: String, latitude: Double, longitude: Double, notes: String) {
        viewModelScope.launch {
            repository.insertLocation(SavedLocation(name = name, latitude = latitude, longitude = longitude, notes = notes))
        }
    }

    fun deleteLocation(location: SavedLocation) {
        viewModelScope.launch {
            repository.deleteLocation(location)
        }
    }

    // Reminders
    fun addReminder(title: String, category: String, delayHours: Int) {
        val triggerTimeMs = System.currentTimeMillis() + (delayHours.toLong() * 60 * 60 * 1000)
        viewModelScope.launch {
            repository.insertReminder(PreparednessReminder(title = title, category = category, dateTimeMs = triggerTimeMs, isCompleted = false))
        }
    }

    fun toggleReminderCompleted(reminder: PreparednessReminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder.copy(isCompleted = !reminder.isCompleted))
        }
    }

    fun deleteReminder(reminder: PreparednessReminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    // Dynamic Readiness Score Assessment (Gamification core)
    val readinessScoreFlow: Flow<Int> = combine(kitItems, checklistItems) { kits, checks ->
        val totalKits = kits.size
        val ownedKits = kits.count { it.isOwned }
        val kitScore = if (totalKits > 0) (ownedKits.toFloat() / totalKits.toFloat()) * 50f else 0f

        val totalChecks = checks.size
        val completedChecks = checks.count { it.isCompleted }
        val checkScore = if (totalChecks > 0) (completedChecks.toFloat() / totalChecks.toFloat()) * 50f else 0f

        (kitScore + checkScore).toInt()
    }

    // --- ALARM SYNTHESIS CONTROLLER ---

    fun toggleAlarm() {
        if (isAlarmPlaying) {
            stopAlarm()
        } else {
            startAlarm()
        }
    }

    private fun startAlarm() {
        isAlarmPlaying = true
        alarmJob = viewModelScope.launch(Dispatchers.Default) {
            val sampleRate = 44100
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = Math.max(minBufferSize, 8192)
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
            )

            val buffer = ShortArray(bufferSize)
            var phase = 0.0
            val twoPi = 2.0 * Math.PI

            try {
                audioTrack?.play()
                while (isAlarmPlaying) {
                    for (i in buffer.indices) {
                        // Siren sound sweep: oscillate frequency between 600Hz and 1400Hz
                        val sweepTime = System.currentTimeMillis() / 1000.0
                        val freq = 1000.0 + 400.0 * Math.sin(sweepTime * Math.PI * 2.0)
                        val sample = Math.sin(phase) * Short.MAX_VALUE * 0.8
                        buffer[i] = sample.toInt().toShort()
                        phase += twoPi * freq / sampleRate
                        if (phase > twoPi) {
                            phase -= twoPi
                        }
                    }
                    audioTrack?.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    audioTrack?.stop()
                } catch (e: Exception) {}
                try {
                    audioTrack?.release()
                } catch (e: Exception) {}
                audioTrack = null
            }
        }
    }

    fun stopAlarm() {
        isAlarmPlaying = false
        alarmJob?.cancel()
        alarmJob = null
        try {
            audioTrack?.stop()
        } catch (e: Exception) {}
        try {
            audioTrack?.release()
        } catch (e: Exception) {}
        audioTrack = null
    }

    // --- FLASHLIGHT CONTROLLER ---

    fun toggleFlashlight(context: Context) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        val cameraId = cameraManager?.cameraIdList?.firstOrNull() ?: return
        try {
            if (isSosPlaying) {
                stopSos(context)
            }
            isFlashlightOn = !isFlashlightOn
            cameraManager.setTorchMode(cameraId, isFlashlightOn)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleSos(context: Context) {
        if (isSosPlaying) {
            stopSos(context)
        } else {
            startSos(context)
        }
    }

    private fun startSos(context: Context) {
        if (isFlashlightOn) {
            isFlashlightOn = false
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull()
            if (cameraId != null) {
                try {
                    cameraManager.setTorchMode(cameraId, false)
                } catch (e: Exception) {}
            }
        }
        isSosPlaying = true
        sosJob = viewModelScope.launch(Dispatchers.Default) {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull() ?: return@launch

            // Morse code: S (...) O (---) S (...)
            val sosPattern = listOf(
                true, 150, false, 150, true, 150, false, 150, true, 150, false, 450, // S
                true, 450, false, 150, true, 450, false, 150, true, 450, false, 450, // O
                true, 150, false, 150, true, 150, false, 150, true, 150, false, 1000 // S
            )

            try {
                while (isSosPlaying) {
                    for (step in 0 until sosPattern.size step 2) {
                        if (!isSosPlaying) break
                        val state = sosPattern[step] as Boolean
                        val delayMs = (sosPattern[step + 1] as Int).toLong()

                        cameraManager.setTorchMode(cameraId, state)
                        delay(delayMs)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    cameraManager.setTorchMode(cameraId, false)
                } catch (e: Exception) {}
            }
        }
    }

    private fun stopSos(context: Context) {
        isSosPlaying = false
        sosJob?.cancel()
        sosJob = null
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        val cameraId = cameraManager?.cameraIdList?.firstOrNull() ?: return
        try {
            cameraManager.setTorchMode(cameraId, false)
        } catch (e: Exception) {}
    }

    // --- CODESIDE SYSTEM SHUTDOWN ---

    override fun onCleared() {
        super.onCleared()
        stopAlarm()
        isSosPlaying = false
        sosJob?.cancel()
    }

    // --- LOCATION ACQUISITION ---

    fun requestCurrentLocation(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationAddressString = "GPS permission required"
                return
            }
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
        try {
            isGpsSynchronizing = true
            locationAddressString = "Syncing with navigation satellites..."
            val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            val provider = when {
                gpsEnabled -> LocationManager.GPS_PROVIDER
                networkEnabled -> LocationManager.NETWORK_PROVIDER
                else -> null
            }

            if (provider != null) {
                val lastKnown = locationManager.getLastKnownLocation(provider)
                if (lastKnown != null) {
                    currentLatitude = lastKnown.latitude
                    currentLongitude = lastKnown.longitude
                    locationAddressString = "Altitude and location synchronized offline."
                }

                locationManager.requestSingleUpdate(provider, object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        currentLatitude = location.latitude
                        currentLongitude = location.longitude
                        locationAddressString = "Precision coordinate lock established."
                        isGpsSynchronizing = false
                    }
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }, context.mainLooper)
            } else {
                // If offline and no provider, simulate a safety sat lock
                viewModelScope.launch {
                    delay(1500)
                    currentLatitude = 34.0522 // Safe node coordinates
                    currentLongitude = -118.2437
                    locationAddressString = "Simulated Sat Lock: Offline fallback active."
                    isGpsSynchronizing = false
                }
            }
        } catch (e: Exception) {
            locationAddressString = "Satellite connection error. Using offline mode."
            isGpsSynchronizing = false
        }
    }

    // --- PRIVATE CONSTANTS FOR PREPOPULATION ---

    private fun getDefaultKitItems(): List<KitItem> = listOf(
        KitItem(name = "Fresh drinking water (1 gal per person/day)", isOwned = false, category = "Water", quantity = 3),
        KitItem(name = "High-calorie canned food / energy bars", isOwned = false, category = "Food", quantity = 6),
        KitItem(name = "Sealed can opener & utensils", isOwned = false, category = "Food", quantity = 1),
        KitItem(name = "Battery-powered / hand-crank AM/FM Radio", isOwned = false, category = "Tools", quantity = 1),
        KitItem(name = "Heavy-duty waterproof high-lumen Flashlight", isOwned = false, category = "Tools", quantity = 1),
        KitItem(name = "Full sterile First Aid bandage & gauze set", isOwned = false, category = "Medical", quantity = 1),
        KitItem(name = "Essential personal prescriptions & aspirin", isOwned = false, category = "Medical", quantity = 1),
        KitItem(name = "Thermal thermal emergency Mylar blanket", isOwned = false, category = "Shelter", quantity = 3),
        KitItem(name = "High-pitch safety distress Whistle", isOwned = false, category = "Tools", quantity = 1),
        KitItem(name = "Printed family emergency plans & copy of IDs", isOwned = false, category = "Documents", quantity = 1),
        KitItem(name = "Sturdy pocket multi-tool swiss pliers", isOwned = false, category = "Tools", quantity = 1),
        KitItem(name = "Unscented iodine water purification drops", isOwned = false, category = "Water", quantity = 1)
    )

    private fun getDefaultChecklists(): List<ChecklistItem> = listOf(
        // Earthquake
        ChecklistItem(checklistType = "earthquake", text = "Identify rigid safe zones: structurally reinforced studs, sturdy tables.", isCompleted = false),
        ChecklistItem(checklistType = "earthquake", text = "Secure tall heavy assets: dressers, TV frames, shelf fasteners.", isCompleted = false),
        ChecklistItem(checklistType = "earthquake", text = "Review family 'Drop, Cover, and Hold On' drills every quarter.", isCompleted = false),
        ChecklistItem(checklistType = "earthquake", text = "Store heavy glass jars and chemicals on lower floor-level margins.", isCompleted = false),
        // Flood
        ChecklistItem(checklistType = "flood", text = "Elevate utility boilers, heater tanks, and electrical breaker boards.", isCompleted = false),
        ChecklistItem(checklistType = "flood", text = "Outline high-ground walking exit routes and dry assembly zones.", isCompleted = false),
        ChecklistItem(checklistType = "flood", text = "Test home sump pumps regularly and maintain battery backups.", isCompleted = false),
        ChecklistItem(checklistType = "flood", text = "Place important physical legal papers in heat-sealed dry bags.", isCompleted = false),
        // Fire
        ChecklistItem(checklistType = "fire", text = "Install smoke alarms inside each bedroom and on every stairwell floor.", isCompleted = false),
        ChecklistItem(checklistType = "fire", text = "Test smoke alarms every month; renew primary alkaline cells yearly.", isCompleted = false),
        ChecklistItem(checklistType = "fire", text = "Position approved multi-class ABC fire extinguishers in easy reach.", isCompleted = false),
        ChecklistItem(checklistType = "fire", text = "Establish two distinct quick egress lines from each primary room.", isCompleted = false),
        // Storm
        ChecklistItem(checklistType = "storm", text = "Keep weak tree limbs near structures trimmed to prevent roof falls.", isCompleted = false),
        ChecklistItem(checklistType = "storm", text = "Stock up on board coverings or emergency tarps for structural breaches.", isCompleted = false),
        ChecklistItem(checklistType = "storm", text = "Anchor light garden assets: picnic tables, trash barrels, flower pots.", isCompleted = false),
        ChecklistItem(checklistType = "storm", text = "Keep vehicles topped with fuel before weather anomalies lock pumps.", isCompleted = false),
        // Power Outage
        ChecklistItem(checklistType = "power_outage", text = "Pack multi-pack heavy batteries, rechargeable cells, and lanterns.", isCompleted = false),
        ChecklistItem(checklistType = "power_outage", text = "Know the backup manual pull release trigger of electric garage doors.", isCompleted = false),
        ChecklistItem(checklistType = "power_outage", text = "Keep a fully fueled external portable generator ready outside in yard.", isCompleted = false),
        ChecklistItem(checklistType = "power_outage", text = "Secure ice blocks in plastic coolers to protect critical perishables.", isCompleted = false),
        // Landslide
        ChecklistItem(checklistType = "landslide", text = "Notice and document land fissures, tilt markers, or wall splits.", isCompleted = false),
        ChecklistItem(checklistType = "landslide", text = "Plan evacuation routes towards open meadows instead of deep valleys.", isCompleted = false),
        ChecklistItem(checklistType = "landslide", text = "Stay alert during long multi-day torrential storms for moving rumble noises.", isCompleted = false),
        ChecklistItem(checklistType = "landslide", text = "Identify retaining walls or channel routing to deflect mud flows.", isCompleted = false),
        // Family Plan
        ChecklistItem(checklistType = "family_plan", text = "Assign an off-grid relative outside the city to log text messages.", isCompleted = false),
        ChecklistItem(checklistType = "family_plan", text = "Store written contact cards inside every backpack and wallet card.", isCompleted = false),
        ChecklistItem(checklistType = "family_plan", text = "Designate immediate outdoor tree and distant cross-suburb zones.", isCompleted = false),
        ChecklistItem(checklistType = "family_plan", text = "Run full home dry-run mock evacuation exercises once a year.", isCompleted = false)
    )
}

// Factory for standard ViewModels creation
class SurvivalViewModelFactory(private val repository: SurvivalRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SurvivalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SurvivalViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
