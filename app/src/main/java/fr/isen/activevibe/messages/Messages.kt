package fr.isen.activevibe.messages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import fr.isen.activevibe.UserProfile

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Conversation(
    val conversationId: String = "",
    val user1: String = "",
    val user2: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0
)

object MessagesRepository {

    private val database = FirebaseDatabase.getInstance().reference.child("messages")
    private val usersDatabase = FirebaseDatabase.getInstance().reference.child("users")

    fun getConversationId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }

    fun sendMessage(sender: String, receiver: String, text: String) {
        val conversationId = getConversationId(sender, receiver)
        val newMessageRef = database.child(conversationId).push()
        val message = Message(sender, receiver, text, System.currentTimeMillis())

        newMessageRef.setValue(message)
            .addOnSuccessListener { Log.d("MessagesRepository", "Message envoyé avec succès.") }
            .addOnFailureListener { Log.e("MessagesRepository", "Erreur envoi message") }

        val conversationRef = database.child("conversations").child(conversationId)
        conversationRef.setValue(
            Conversation(
                conversationId = conversationId,
                user1 = sender,
                user2 = receiver,
                lastMessage = text,
                lastMessageTimestamp = System.currentTimeMillis()
            )
        )
    }

    fun getMessages(conversationId: String, onMessagesLoaded: (List<Message>) -> Unit) {
        database.child(conversationId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messagesList = mutableListOf<Message>()
                for (child in snapshot.children) {
                    val message = child.getValue(Message::class.java)
                    if (message != null) messagesList.add(message)
                }
                onMessagesLoaded(messagesList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MessagesRepository", "Erreur chargement messages : ${error.message}")
            }
        })
    }

    fun getUserConversations(userName: String, onConversationsLoaded: (List<Conversation>) -> Unit) {
        database.child("conversations").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val conversationList = mutableListOf<Conversation>()
                for (child in snapshot.children) {
                    val conversation = child.getValue(Conversation::class.java)
                    if (conversation != null && (conversation.user1 == userName || conversation.user2 == userName)) {
                        conversationList.add(conversation)
                    }
                }
                onConversationsLoaded(conversationList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MessagesRepository", "Erreur récupération conversations : ${error.message}")
            }
        })
    }

    fun getUsers(onUsersLoaded: (List<UserProfile>) -> Unit) {
        usersDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<UserProfile>()
                for (child in snapshot.children) {
                    val user = child.getValue(UserProfile::class.java)
                    if (user != null) userList.add(user)
                }
                onUsersLoaded(userList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MessagesRepository", "Erreur chargement utilisateurs : ${error.message}")
            }
        })
    }

    fun listenForMessages(conversationId: String, onMessagesUpdated: (List<Message>) -> Unit) {
        val messagesRef = database.child(conversationId)

        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messagesList = mutableListOf<Message>()
                for (child in snapshot.children) {
                    val message = child.getValue(Message::class.java)
                    if (message != null) messagesList.add(message)
                }
                onMessagesUpdated(messagesList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MessagesRepository", "Erreur récupération messages : ${error.message}")
            }
        })
    }


}

@Composable
fun Message() {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var currentUsername by remember { mutableStateOf("") }

    var conversations by remember { mutableStateOf(emptyList<Conversation>()) }
    var users by remember { mutableStateOf(emptyList<UserProfile>()) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) } // ✅ Gère l'affichage de la recherche
    var selectedChatUser by remember { mutableStateOf<String?>(null) } // ✅ Stocke l'utilisateur sélectionné

    // 🔹 Charger les conversations de l'utilisateur connecté
    LaunchedEffect(Unit) {
        val userId = currentUser?.uid ?: return@LaunchedEffect
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        userRef.child("nomUtilisateur").get().addOnSuccessListener { snapshot ->
            val fetchedUsername = snapshot.value as? String ?: ""
            currentUsername = fetchedUsername
            Log.d("MessagesDebug", "Nom utilisateur récupéré : $currentUsername")

            // ✅ Charger toutes les conversations
            MessagesRepository.getUserConversations(fetchedUsername) { conversationList ->
                conversations = conversationList.sortedByDescending { it.lastMessageTimestamp }
            }

            // ✅ Charger tous les utilisateurs pour la recherche
            MessagesRepository.getUsers { userList ->
                users = userList.filter { it.nomUtilisateur != fetchedUsername }
            }
        }
    }

    // ✅ Si un chat est sélectionné, on affiche uniquement le chat en **PLEIN ÉCRAN**
    selectedChatUser?.let { chatUser ->
        ChatScreen(receiverName = chatUser) { selectedChatUser = null }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Messages", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(10.dp))

        // 🔹 Bouton pour rechercher un nouvel utilisateur
        Button(
            onClick = { showSearch = !showSearch },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (showSearch) "Fermer la recherche" else "Nouvelle discussion")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ✅ Affichage de la recherche UNIQUEMENT si showSearch est activé
        if (showSearch) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Rechercher un utilisateur...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            val filteredUsers = users.filter { it.nomUtilisateur.contains(searchQuery, ignoreCase = true) }
            LazyColumn {
                items(filteredUsers) { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { selectedChatUser = user.nomUtilisateur; showSearch = false },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.nomUtilisateur.first().toString().uppercase(),
                                color = Color.White,
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(text = user.nomUtilisateur, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ✅ Liste des conversations récentes
        Text("Conversations récentes", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        if (conversations.isEmpty()) {
            Text("Aucune conversation pour le moment", color = Color.Gray, fontSize = 16.sp)
        } else {
            LazyColumn {
                items(conversations) { conversation ->
                    val chatPartnerName = if (conversation.user1 == currentUsername) conversation.user2 else conversation.user1

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { selectedChatUser = chatPartnerName },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chatPartnerName.first().toString().uppercase(),
                                color = Color.White,
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(text = chatPartnerName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(text = conversation.lastMessage, fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}


