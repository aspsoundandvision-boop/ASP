package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.playback.PlaybackManager
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {

  private val musicViewModel: MusicViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val songs by musicViewModel.allSongs.collectAsStateWithLifecycle()
      val latestSongs by musicViewModel.latestReleases.collectAsStateWithLifecycle()
      val branding by musicViewModel.branding.collectAsStateWithLifecycle()

      // Dynamically load primary accent color from DB branding configurations!
      val primaryAccentColor = parseColor(branding.primaryColorHex)

      MyApplicationTheme {
        val navController = rememberNavController()
        val currentBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = currentBackStackEntry?.destination?.route

        // Control flags for continuous player overlay visibility
        val isSplash = currentRoute == "splash"
        val isAdmin = currentRoute == "admin"
        val showPlayerAndNav = !isSplash && !isAdmin

        var showFullPlayerSheet by remember { mutableStateOf(false) }

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          bottomBar = {
            if (showPlayerAndNav) {
              NavigationBar(
                containerColor = Color(0xFF0C0C10),
                contentColor = Color.White
              ) {
                NavigationBarItem(
                  selected = currentRoute == "home",
                  onClick = {
                    navController.navigate("home") {
                      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                      launchSingleTop = true
                      restoreState = true
                    }
                  },
                  icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                  label = { Text("Home", fontSize = 11.sp) },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = primaryAccentColor,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = primaryAccentColor
                  )
                )

                NavigationBarItem(
                  selected = currentRoute == "library",
                  onClick = {
                    navController.navigate("library") {
                      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                      launchSingleTop = true
                      restoreState = true
                    }
                  },
                  icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
                  label = { Text("Library", fontSize = 11.sp) },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = primaryAccentColor,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = primaryAccentColor
                  )
                )

                NavigationBarItem(
                  selected = currentRoute == "experience",
                  onClick = {
                    navController.navigate("experience") {
                      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                      launchSingleTop = true
                      restoreState = true
                    }
                  },
                  icon = { Icon(Icons.Default.Audiotrack, contentDescription = "Sound & Vision") },
                  label = { Text("Experience", fontSize = 11.sp) },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = primaryAccentColor,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = primaryAccentColor
                  )
                )
              }
            }
          }
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color(0xFF08080C))
              .padding(
                bottom = if (showPlayerAndNav) innerPadding.calculateBottomPadding() else 0.dp
              )
          ) {
            NavHost(
              navController = navController,
              startDestination = "splash",
              modifier = Modifier.fillMaxSize()
            ) {
              composable("splash") {
                IntroSplashScreen(
                  branding = branding,
                  onFinished = {
                    PlaybackManager.stop() // Clear launch synths
                    navController.navigate("home") {
                      popUpTo("splash") { inclusive = true }
                    }
                  }
                )
              }

              composable("home") {
                HomeScreen(
                  songs = songs,
                  latestSongs = latestSongs,
                  branding = branding,
                  onNavigateToLibrary = { navController.navigate("library") },
                  onNavigateToExperience = { navController.navigate("experience") },
                  onOpenAdmin = { navController.navigate("admin") }
                )
              }

              composable("library") {
                LibraryScreen(
                  songs = songs,
                  branding = branding
                )
              }

              composable("experience") {
                CinematicScreen(
                  branding = branding
                )
              }

              composable("admin") {
                AdminBackendPanel(
                  viewModel = musicViewModel,
                  branding = branding,
                  songs = songs,
                  onClose = { navController.popBackStack() }
                )
              }
            }

            // Fixed bottom soundscape player matching Spotify floating bar
            if (showPlayerAndNav) {
              Box(
                modifier = Modifier
                  .align(androidx.compose.ui.Alignment.BottomCenter)
                  .padding(bottom = 8.dp)
              ) {
                FixedBottomPlayer(
                  branding = branding,
                  onExpandedClick = { showFullPlayerSheet = true }
                )
              }
            }

            // Expanded floating cover overlays containing detailed 16-band spectrums
            if (showFullPlayerSheet) {
              FullPlayerSheet(
                branding = branding,
                onDismiss = { showFullPlayerSheet = false }
              )
            }
          }
        }
      }
    }
  }
}
