package fr.isen.activevibe

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import androidx.compose.foundation.shape.RoundedCornerShape


@Composable
fun FirstTimeScreen() {
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference.child("users")

    var nomUtilisateur by remember { mutableStateOf("") }
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var isChecking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Première Utilisation",
            fontSize = 26.sp,
            color = Color(0xFF433AF1)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Champ Nom d'utilisateur
        OutlinedTextField(
            value = nomUtilisateur,
            onValueChange = { nomUtilisateur = it },
            label = { Text("Nom d'utilisateur") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Champ Nom
        OutlinedTextField(
            value = nom,
            onValueChange = { nom = it },
            label = { Text("Nom") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Champ Prénom
        OutlinedTextField(
            value = prenom,
            onValueChange = { prenom = it },
            label = { Text("Prénom") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Afficher un message d'erreur si besoin
        errorMessage?.let {
            Text(text = it, color = Color.Red, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Bouton Valider
        Button(
            onClick = {
                if (nomUtilisateur.isNotEmpty() && nom.isNotEmpty() && prenom.isNotEmpty()) {
                    isChecking = true
                    checkAndRegisterUser(nomUtilisateur, nom, prenom, database, auth) { success, message ->
                        isChecking = false
                        if (!success) {
                            errorMessage = message
                        }
                    }
                } else {
                    errorMessage = "Tous les champs doivent être remplis."
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF433AF1)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isChecking) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(text = "Valider", color = Color.White)
            }
        }
    }
}

/** ✅ Vérifier si le Nom d'utilisateur existe déjà et enregistrer les infos **/
private fun checkAndRegisterUser(
    username: String,
    nom: String,
    prenom: String,
    database: DatabaseReference,
    auth: FirebaseAuth,
    callback: (Boolean, String?) -> Unit
) {
    database.orderByChild("nomUtilisateur").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                callback(false, "Nom d'utilisateur déjà pris. Choisissez-en un autre.")
            } else {
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    val userData = mapOf(
                        "nomUtilisateur" to username,
                        "nom" to nom,
                        "prenom" to prenom
                    )

                    database.child(uid).setValue(userData)
                        .addOnSuccessListener {
                            Log.d("Firebase", "Utilisateur ajouté avec succès")
                            callback(true, null) // Succès
                        }
                        .addOnFailureListener { e ->
                            callback(false, "Erreur lors de l'enregistrement : ${e.message}")
                        }
                } else {
                    callback(false, "Erreur : UID introuvable.")
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            callback(false, "Erreur Firebase : ${error.message}")
        }
    })
}
