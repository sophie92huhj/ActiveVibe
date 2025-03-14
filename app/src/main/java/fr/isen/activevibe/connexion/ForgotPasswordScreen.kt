package fr.isen.activevibe.connexion

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import fr.isen.activevibe.R // Assure-toi que sport.png est bien placé dans res/drawable

@Composable
fun ForgotPasswordScreen(onBackToLogin: () -> Unit) {
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // ✅ Image en arrière-plan
        Image(
            painter = painterResource(id = R.drawable.sport),  // Assure-toi que l’image est bien dans `res/drawable`
            contentDescription = "Fond d'écran",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Recouvre tout l'écran
        )

        // ✅ Contenu superposé sur l'image
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Mot de passe oublié",
                fontSize = 26.sp,
                color = Color.Black, // ✅ Texte en blanc pour meilleur contraste
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Entrez votre email", color = Color.Black) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black, // ✅ Texte noir
                    cursorColor = Color.Black,
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Gray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            message?.let {
                Text(text = it, color = Color.Red, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ✅ Bouton "Envoyer" avec fond bleu
            Button(
                onClick = {
                    if (email.isNotEmpty()) {
                        isProcessing = true
                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                isProcessing = false
                                if (task.isSuccessful) {
                                    message = "Email de réinitialisation envoyé."
                                } else {
                                    message = "Erreur : ${task.exception?.message}"
                                }
                            }
                    } else {
                        message = "Veuillez entrer votre email."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF433AF1)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Envoyer", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = { onBackToLogin() }
            ) {
                Text(text = "Retour à la connexion", color = Color(0xFF433AF1))
            }
        }
    }
}
