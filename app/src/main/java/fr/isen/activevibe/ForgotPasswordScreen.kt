package fr.isen.activevibe

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

@Composable
fun ForgotPasswordScreen(onBackToLogin: () -> Unit) {
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

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
            color = Color(0xFF433AF1),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Entrez votre email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        message?.let {
            Text(text = it, color = Color.Red, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

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
