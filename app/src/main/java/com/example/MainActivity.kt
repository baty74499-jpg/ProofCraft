package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.BlendAndStillScreen
import com.example.ui.screens.DilutionScreen
import com.example.ui.screens.HydrometerScreen
import com.example.ui.screens.LogAndGuideScreen
import com.example.ui.theme.CopperPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.DistillersViewModel

sealed class Screen(
  val routeIndex: Int,
  val title: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String
) {
  data object Dilute : Screen(0, "Dilute", Icons.Filled.WaterDrop, Icons.Outlined.WaterDrop, "nav_dilute")
  data object Hydrometer : Screen(1, "Hydrometer", Icons.Filled.DeviceThermostat, Icons.Outlined.DeviceThermostat, "nav_hydrometer")
  data object BlendAndStill : Screen(2, "Blend & Still", Icons.Filled.Tune, Icons.Outlined.Tune, "nav_blend")
  data object LogAndGuide : Screen(3, "Log & Guide", Icons.Filled.History, Icons.Outlined.History, "nav_log")

  companion object {
    val items = listOf(Dilute, Hydrometer, BlendAndStill, LogAndGuide)
  }
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        ProofCraftApp()
      }
    }
  }
}

@Composable
fun ProofCraftApp(viewModel: DistillersViewModel = viewModel()) {
  var currentTabIndex by remember { mutableIntStateOf(0) }
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(Unit) {
    viewModel.toastMessage.collect { msg ->
      snackbarHostState.showSnackbar(msg)
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.safeDrawing),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      NavigationBar(
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = androidx.compose.ui.unit.Dp(6f)
      ) {
        Screen.items.forEach { screen ->
          val selected = currentTabIndex == screen.routeIndex
          NavigationBarItem(
            selected = selected,
            onClick = { currentTabIndex = screen.routeIndex },
            icon = {
              Icon(
                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                contentDescription = screen.title
              )
            },
            label = { Text(screen.title) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
              indicatorColor = MaterialTheme.colorScheme.primaryContainer,
              selectedTextColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag(screen.testTag)
          )
        }
      }
    }
  ) { innerPadding ->
    val modifier = Modifier.padding(innerPadding)
    when (currentTabIndex) {
      0 -> DilutionScreen(viewModel = viewModel, modifier = modifier)
      1 -> HydrometerScreen(
        viewModel = viewModel,
        onNavigateToDilution = { currentTabIndex = 0 },
        modifier = modifier
      )
      2 -> BlendAndStillScreen(viewModel = viewModel, modifier = modifier)
      3 -> LogAndGuideScreen(viewModel = viewModel, modifier = modifier)
    }
  }
}
