package fr.isen.activevibe

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*
import com.google.firebase.firestore.auth.User
import fr.isen.activevibe.ui.theme.ActiveVibeTheme


class MainActivity : ComponentActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Référence à Firebase Database
        database = FirebaseDatabase.getInstance().getReference("utilisateurs")

        // States pour stocker les données
        val nom = mutableStateOf("Chargement...")
        val email = mutableStateOf("Chargement...")

        // Lire en temps réel les données Firebase
        database.child("001").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(UserProfile::class.java)
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
            ActiveVibeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EditProfileScreen(
                        modifier = Modifier.padding(innerPadding),
                        nom = nom.value,
                        email = email.value,
                        saveProfile = { updatedProfile ->
                            saveProfileToFirebase(updatedProfile)
                        }
                    )
                }
            }
        }
    }

    // Fonction pour sauvegarder le profil dans Firebase Realtime Database
    private fun saveProfileToFirebase(profile: UserProfile) {
        database.child("001").setValue(profile).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Firebase", "Profil mis à jour avec succès.")
            } else {
                Log.e("Firebase", "Erreur lors de la mise à jour du profil.")
            }
        }
    }
}
