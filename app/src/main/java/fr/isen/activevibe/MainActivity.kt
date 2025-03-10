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
import com.google.firebase.database.*
import fr.isen.activevibe.ui.theme.ActiveVibeTheme

class MainActivity : ComponentActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Référence à Firebase Database
        database = FirebaseDatabase.getInstance().getReference("utilisateurs")

        setContent {
            ActiveVibeTheme {
                val userProfile = remember { mutableStateOf(UserProfile()) }

                // Lire en temps réel les données Firebase
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

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EditProfileScreen(
                        modifier = Modifier.padding(innerPadding),
                        userProfile = userProfile.value,
                        saveProfile = { updatedProfile ->
                            saveProfileToFirebase(updatedProfile)
                        }
                    )
                }
            }
        }
    }

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
