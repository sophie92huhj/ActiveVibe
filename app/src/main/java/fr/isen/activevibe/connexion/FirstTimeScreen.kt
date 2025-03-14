package fr.isen.activevibe.connexion

import android.content.Context
import android.content.Intent
import android.util.Log
import fr.isen.activevibe.R
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import fr.isen.activevibe.MainActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale


@Composable
fun FirstTimeScreen() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference.child("users")

    var nomUtilisateur by remember { mutableStateOf("") }
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var isChecking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // ✅ Image en arrière-plan
        Image(
            painter = painterResource(id = R.drawable.sport),  // Assure-toi que l’image est bien dans `res/drawable`
            contentDescription = "Fond d'écran",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Recouvre tout l'écran
        )

        // ✅ Contenu de l'écran superposé
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
                color = Color.Black // ✅ Texte en blanc pour un meilleur contraste
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nomUtilisateur,
                onValueChange = { nomUtilisateur = it },
                label = { Text("Nom d'utilisateur", color = Color.Black) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,  // Texte en noir
                    cursorColor = Color.Black,  // Curseur noir
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Gray,
                    focusedLabelColor = Color.Black
                )


            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nom,
                onValueChange = { nom = it },
                label = { Text("Nom", color = Color.Black) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,  // Texte en noir
                    cursorColor = Color.Black,  // Curseur noir
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Gray,
                    focusedLabelColor = Color.Black
                )


            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = prenom,
                onValueChange = { prenom = it },
                label = { Text("Prénom", color = Color.Black) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,  // Texte en noir
                    cursorColor = Color.Black,  // Curseur noir
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Gray,
                    focusedLabelColor = Color.Black
                )

            )

            Spacer(modifier = Modifier.height(16.dp))

            errorMessage?.let {
                Text(text = it, color = Color.Red, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ✅ Bouton Valider avec fond bleu
            Button(
                onClick = {
                    if (nomUtilisateur.isNotEmpty() && nom.isNotEmpty() && prenom.isNotEmpty()) {
                        isChecking = true
                        checkAndRegisterUser(nomUtilisateur, nom, prenom, database, auth, context) { success, message ->
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
}


/** ✅ Vérifier si le Nom d'utilisateur existe déjà et enregistrer les infos **/
private fun checkAndRegisterUser(
    username: String,
    nom: String,
    prenom: String,
    database: DatabaseReference,
    auth: FirebaseAuth,
    context: Context, // ✅ Ajout du contexte pour la navigation
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
                            callback(true, null)

                            // ✅ Redirection vers MainActivity après validation
                            val intent = Intent(context, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
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