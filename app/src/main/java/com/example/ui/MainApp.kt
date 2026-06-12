package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.screens.AIScreen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.GuideScreen
import com.example.ui.screens.SurveyScreen
import com.example.ui.viewmodel.SurveyViewModel

enum class MainTab(val title: String, val icon: ImageVector, val tag: String) {
    SURVEY("Survei", Icons.Default.Terrain, "tab_survey"),
    CALCULATOR("Kalkulator", Icons.Default.Calculate, "tab_calculator"),
    AI_INTERPRET("AI Asisten", Icons.Default.AutoAwesome, "tab_ai"),
    GUIDE("Panduan", Icons.Default.Book, "tab_guide")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: SurveyViewModel) {
    var selectedTab by remember { mutableStateOf(MainTab.SURVEY) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Geophysics Survey Toolbox",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.testTag("app_top_bar")
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("app_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                MainTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                        label = { Text(text = tab.title, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                MainTab.SURVEY -> SurveyScreen(viewModel = viewModel)
                MainTab.CALCULATOR -> CalculatorScreen()
                MainTab.AI_INTERPRET -> AIScreen()
                MainTab.GUIDE -> GuideScreen()
            }
        }
    }
}
