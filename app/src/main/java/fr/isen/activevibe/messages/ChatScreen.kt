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
    var currentUsername by remember { mutableStateOf("") } // ✅ Variable mutable
    var messages by remember { mutableStateOf(emptyList<Message>()) }
    var messageText by remember { mutableStateOf("") }

    // 🔹 Charger le `nomUtilisateur` de l'utilisateur connecté
    LaunchedEffect(Unit) {
        val userId = currentUser?.uid ?: return@LaunchedEffect
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        userRef.child("nomUtilisateur").get().addOnSuccessListener { snapshot ->
            currentUsername = snapshot.value as? String ?: ""
            Log.d("ChatDebug", "Nom utilisateur actuel récupéré : $currentUsername")

            // ✅ Une fois récupéré, on peut écouter les messages en direct
            val conversationId = MessagesRepository.getConversationId(currentUsername, receiverName)
            MessagesRepository.listenForMessages(conversationId) { messagesList ->
                messages = messagesList
            }
        }
    }

    // 🔹 UI du chat
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack) { Text("Retour") }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Chat avec $receiverName", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 🔹 Liste des messages
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            reverseLayout = true // ✅ Affiche le dernier message en bas
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
                        val conversationId = MessagesRepository.getConversationId(currentUsername, receiverName)
                        MessagesRepository.sendMessage(currentUsername, receiverName, messageText)
                        messageText = "" // ✅ Réinitialiser le champ après l'envoi
                    }
                }
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
            .padding(8.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isMine) Color(0xFF433AF1) else Color.LightGray
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = message.text,
                color = if (isMine) Color.White else Color.Black,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

