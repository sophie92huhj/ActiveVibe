package fr.isen.activevibe

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            UserScreen(nom.value, email.value)
        }
    }
}

// Modèle User pour correspondre à Firebase
data class User(val nom: String = "", val email: String = "")

@Composable
fun UserScreen(nom: String, email: String) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "Nom: $nom", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Email: $email", style = MaterialTheme.typography.bodyLarge)
    }
}
