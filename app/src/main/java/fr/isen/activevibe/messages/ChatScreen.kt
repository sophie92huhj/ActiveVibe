package fr.isen.activevibe.messages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import fr.isen.activevibe.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(receiverName: String, onBack: () -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var currentUsername by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Message>() }
    var messageText by remember { mutableStateOf("") }

    // 🔹 Récupérer `nomUtilisateur`
    LaunchedEffect(Unit) {
        val userId = currentUser?.uid ?: return@LaunchedEffect
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        userRef.child("nomUtilisateur").get().addOnSuccessListener { snapshot ->
            val fetchedUsername = snapshot.value as? String ?: ""
            currentUsername = fetchedUsername
            Log.d("ChatDebug", "Utilisateur actuel récupéré : $currentUsername")

            // ✅ Charger les messages en temps réel sans doublons
            MessagesRepository.listenForMessages(fetchedUsername, receiverName) { messagesList ->
                messages.clear()
                messages.addAll(messagesList)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF433AF1), // Gris foncé
                    contentColor = Color.White // Texte en blanc
                )
            ) {
                Text("Retour")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Chat avec $receiverName", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ✅ Messages bien affichés avec alignement dynamique
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            reverseLayout = false // ✅ Les messages récents en bas
        ) {
            items(messages) { message ->
                MessageBubble(
                    message = message,
                    isMine = message.senderId == currentUsername
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 🔹 Champ pour écrire un message
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = { Text("Écrire un message...") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // ✅ Bouton pour envoyer un message
            Button(
                onClick = {
                    if (messageText.isNotEmpty()) {
                        MessagesRepository.sendMessage(currentUsername, receiverName, messageText)
                        messageText = "" // ✅ Réinitialiser le champ après l'envoi
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF433AF1), // Bleu clair
                    contentColor = Color.White // Texte en blanc
                )
            ) {
                Text("Envoyer")
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isMine: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isMine) Color(0xFF64B5F6) else Color(0xFFE0E0E0) // ✅ Bleu clair pour l'envoi, gris clair pour la réception
            ),
            shape = RoundedCornerShape(24.dp), // ✅ Arrondi plus prononcé
            modifier = Modifier
                .widthIn(min = 50.dp, max = 300.dp) // ✅ Largeur dynamique
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = message.text,
                color = if (isMine) Color.White else Color.Black,
                fontSize = 16.sp, // ✅ Texte plus lisible
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp)
            )
        }
    }
}
