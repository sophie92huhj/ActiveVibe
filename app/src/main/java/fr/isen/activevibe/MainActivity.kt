package fr.isen.activevibe

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*

class MainActivity : ComponentActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Référence à Firebase Database
        database = FirebaseDatabase.getInstance().getReference("utilisateurs")

        // States pour stocker les données
        val nom = mutableStateOf("Chargement...")
        val email = mutableStateOf("Chargement...")

        // Lire en temps réel les données Firebase
        database.child("001").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        nom.value = user.nom
                        email.value = user.email
                        Log.d("Firebase", "Données récupérées : Nom=${user.nom}, Email=${user.email}")
                    }
                } else {
                    Log.e("Firebase", "Aucune donnée trouvée")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Erreur Firebase : ${error.message}")
            }
        })

        // Affichage de l'interface
        setContent {
            var selectedItem by remember { mutableStateOf(0) }
            var isDarkMode by remember { mutableStateOf(false) }
            val items = listOf("Fil d'actualité", "Recherche", "Ajouter", "Posts Likés", "Profil")
            val icons = listOf(Icons.Filled.Home, Icons.Filled.Search, Icons.Filled.Add, Icons.Filled.Favorite, Icons.Filled.Person)

            MaterialTheme(
                colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()
            ) {
                Scaffold(
                    topBar = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .statusBarsPadding()  // Pour éviter que l'icône touche la barre de notification
                        ) {
                            // Positionner l'icône Send
                            IconButton(
                                onClick = {
                                    Log.d("Send Button", "Send button clicked")
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)  // Aligner l'icône en haut à droite
                                    .offset(y = 10.dp)  // Ajuste la position de l'icône
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Send,
                                    contentDescription = "Envoyer",
                                    modifier = Modifier.size(40.dp)  // Taille de l'icône
                                )
                            }
                        }
                    },
                    bottomBar = {
                        NavigationBar {
                            items.forEachIndexed { index, _ ->
                                if (index == 2) {
                                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                        FloatingActionButton(onClick = { selectedItem = index }) {
                                            Icon(Icons.Filled.Add, contentDescription = "Ajouter")
                                        }
                                    }
                                } else {
                                    NavigationBarItem(
                                        icon = { Icon(icons[index], contentDescription = null) },
                                        selected = selectedItem == index,
                                        onClick = { selectedItem = index }
                                    )
                                }
                            }
                        }
                    },
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
                            0 -> Text("Page: Fil d'actualité", fontSize = 24.sp)
                            1 -> Text("Page: Recherche", fontSize = 24.sp)
                            2 -> Text("Page: Ajouter un post", fontSize = 24.sp)
                            3 -> Text("Page: Posts Likés", fontSize = 24.sp)
                            4 -> Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Page: Profil", fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = "Nom: ${nom.value}", style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Email: ${email.value}", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class User(val nom: String = "", val email: String = "")
