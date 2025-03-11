package fr.isen.activevibe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.isen.activevibe.navigation.BottomNavigationBar
import fr.isen.activevibe.navigation.TopBar
import fr.isen.activevibe.navigation.* // ✅ Importation de toutes les fonctions de `Screens.kt`

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var selectedItem by remember { mutableStateOf(0) }
            var isDarkMode by remember { mutableStateOf(false) }

            MaterialTheme(
                colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()
            ) {
                Scaffold(
                    topBar = { TopBar(selectedItem, onItemSelected = { selectedItem = it }) },
                    bottomBar = { BottomNavigationBar(selectedItem, onItemSelected = { selectedItem = it }) },
                    floatingActionButton = {
                        FloatingActionButton(onClick = { isDarkMode = !isDarkMode }) {
                            Text(if (isDarkMode) "🌙" else "🌞")
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        when (selectedItem) {
                            0 -> HomeScreen()  // ✅ Toutes les fonctions sont bien appelées
                            1 -> SearchScreen()
                            2 -> AddPostScreen()
                            3 -> LikedPostsScreen()
                            4 -> ProfileScreen()
                            5 -> MessagesScreen()
                        }
                    }
                }
            }
        }
    }
}
