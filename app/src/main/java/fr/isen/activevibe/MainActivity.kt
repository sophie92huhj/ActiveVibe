package fr.isen.activevibe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import fr.isen.activevibe.navigation.BottomNavigationBar
import fr.isen.activevibe.navigation.TopBar
import fr.isen.activevibe.navigation.* // ✅ Importation de toutes les fonctions de `Screens.kt`
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.graphics.Color
import fr.isen.activevibe.profil.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            var selectedItem by remember { mutableStateOf(0) }
            var isDarkMode by remember { mutableStateOf(false) }
            var showEditProfile by remember { mutableStateOf(false) } // ✅ Ajouté ici !

            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid ?: "" // ✅ Récupère l'UID de l'utilisateur connecté

            MaterialTheme(
                colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme().copy(
                    background = Color.White,
                    surface = Color.White
                )
            ) {
                if (showEditProfile) {
                    EditProfilScreen(
                        userProfile = UserProfile(), // Remplace par les données actuelles
                        saveProfile = { updatedProfile ->
                            showEditProfile = false // ✅ Retour au profil après mise à jour
                        },
                        onBackClick = { showEditProfile = false } // ✅ Bouton retour
                    )
                } else {
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
                                0 -> HomeScreen()
                                1 -> SearchScreen()
                                2 -> AddPostScreen()
                                3 -> LikedPostsScreen(navController)
                                4 -> ProfileScreen1(onEditClick = { showEditProfile = true }) // ✅ Correction ici
                                5 -> MessagesScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}
