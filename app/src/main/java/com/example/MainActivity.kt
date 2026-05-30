package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AppDatabase
import com.example.data.SurvivalRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.SurvivalViewModel
import com.example.viewmodel.SurvivalViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup SQLite Room Database, Dao & Repository loop
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = SurvivalRepository(database.survivalDao())
        
        // Instantiate ViewModel
        val viewModel: SurvivalViewModel by viewModels {
            SurvivalViewModelFactory(repository)
        }

        enableEdgeToEdge()

        setContent {
            val systemDark = isSystemInDarkTheme()
            var localDarkThemeState by remember { mutableStateOf(systemDark) }

            MyApplicationTheme(darkTheme = localDarkThemeState) {
                var currentSelectedTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "S",
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    Text(
                                        text = when (currentSelectedTab) {
                                            0 -> "Guardian Monitor"
                                            1 -> "First Aid Manuals"
                                            2 -> "Gear Checklist"
                                            3 -> "Rescue Utilities"
                                            else -> "Directory Books"
                                        },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = (-0.5).sp
                                        ),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.testTag("app_navigation_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentSelectedTab == 0,
                                onClick = { currentSelectedTab = 0 },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Monitor Screen", modifier = Modifier.size(24.dp)) },
                                label = { Text("Monitor", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("nav_dashboard")
                            )
                            NavigationBarItem(
                                selected = currentSelectedTab == 1,
                                onClick = { currentSelectedTab = 1 },
                                icon = { Icon(Icons.Default.LocalHospital, contentDescription = "Manuals Screen", modifier = Modifier.size(24.dp)) },
                                label = { Text("Manuals", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("nav_first_aid")
                            )
                            NavigationBarItem(
                                selected = currentSelectedTab == 2,
                                onClick = { currentSelectedTab = 2 },
                                icon = { Icon(Icons.Default.Backpack, contentDescription = "Gear Screen", modifier = Modifier.size(24.dp)) },
                                label = { Text("Gear Pack", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("nav_gear")
                            )
                            NavigationBarItem(
                                selected = currentSelectedTab == 3,
                                onClick = { currentSelectedTab = 3 },
                                icon = { Icon(Icons.Default.FlashlightOn, contentDescription = "Hardware Tools Screen", modifier = Modifier.size(24.dp)) },
                                label = { Text("Utility Tools", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("nav_tools")
                            )
                            NavigationBarItem(
                                selected = currentSelectedTab == 4,
                                onClick = { currentSelectedTab = 4 },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Configs Screen", modifier = Modifier.size(24.dp)) },
                                label = { Text("Directory", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("nav_settings")
                            )
                        }
                    }
                ) { innerPadding ->
                    val contentModifier = Modifier.padding(innerPadding)
                    
                    when (currentSelectedTab) {
                        0 -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToTab = { currentSelectedTab = it },
                            modifier = contentModifier
                        )
                        1 -> FirstAidScreen(
                            modifier = contentModifier
                        )
                        2 -> KitsChecklistScreen(
                            viewModel = viewModel,
                            modifier = contentModifier
                        )
                        3 -> ToolsScreen(
                            viewModel = viewModel,
                            modifier = contentModifier
                        )
                        4 -> SettingsScreen(
                            viewModel = viewModel,
                            isDarkTheme = localDarkThemeState,
                            onToggleTheme = { localDarkThemeState = it },
                            modifier = contentModifier
                        )
                    }
                }
            }
        }
    }
}
