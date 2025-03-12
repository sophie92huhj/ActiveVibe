package fr.isen.activevibe

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.google.firebase.database.*
import fr.isen.activevibe.profil.App
import fr.isen.activevibe.ui.theme.ActiveVibeTheme
import fr.isen.activevibe.profil.EditProfilScreen

class ProfilActivity : ComponentActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Référence Firebase Database
        database = FirebaseDatabase.getInstance().getReference("utilisateurs")

        setContent {
            ActiveVibeTheme {
                val showEditProfile = remember { mutableStateOf(false) }
                val userProfile = remember { mutableStateOf(UserProfile()) }

                // Lire les données Firebase
                LaunchedEffect(Unit) {
                    database.child("001").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val user = snapshot.getValue(UserProfile::class.java)
                                if (user != null) {
                                    userProfile.value = user
                                    Log.d("Firebase", "Données récupérées : $user")
                                }
                            } else {
                                Log.e("Firebase", "Aucune donnée trouvée")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("Firebase", "Erreur Firebase : ${error.message}")
                        }
                    })
                }

                // Affichage conditionnel : soit `App()`, soit `EditProfilScreen()`
                if (showEditProfile.value) {
                    EditProfilScreen(
                        userProfile = userProfile.value,
                        saveProfile = { updatedProfile ->
                            saveProfileToFirebase(updatedProfile)
                            userProfile.value = updatedProfile // Mise à jour locale
                            showEditProfile.value = false // Retour à `App()`
                        },
                        onBackClick = { showEditProfile.value = false } // Retour à `App()`
                    )
                } else {
                    // App prend en paramètre `onEditClick` pour modifier `showEditProfile`
                    App()
                }
            }
        }
    }

    private fun saveProfileToFirebase(profile: UserProfile) {
        Log.d("Firebase", "Mise à jour du profil: $profile")
        database.child("001").setValue(profile).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Firebase", "Profil mis à jour avec succès.")
            } else {
                Log.e("Firebase", "Erreur lors de la mise à jour du profil : ${task.exception?.message}")
            }
        }
    }
}
