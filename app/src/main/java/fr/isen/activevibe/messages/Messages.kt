package fr.isen.activevibe.messages

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    var users by remember { mutableStateOf(emptyList<UserProfile>()) }
    var searchQuery by remember { mutableStateOf("") }
    var filteredUsers by remember { mutableStateOf(emptyList<UserProfile>()) }
    var conversations by remember { mutableStateOf(emptyList<Conversation>()) }
    var selectedChatUser by remember { mutableStateOf<String?>(null) } // ✅ Permet d'afficher `ChatScreen()`

    // 🔹 Charger les utilisateurs et conversations
    LaunchedEffect(Unit) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val userId = firebaseAuth.currentUser?.uid ?: return@LaunchedEffect
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        userRef.child("nomUtilisateur").get().addOnSuccessListener { snapshot ->
            val fetchedUsername = snapshot.value as? String ?: ""
            currentUsername = fetchedUsername
            Log.d("MessagesDebug", "Nom utilisateur récupéré : $currentUsername")

            MessagesRepository.getUsers { userList ->
                users = userList
                filteredUsers = userList
            }

            MessagesRepository.getUserConversations(fetchedUsername) { conversationList ->
                conversations = conversationList
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (selectedChatUser == null) {
            // ✅ Écran principal des messages
            Text("Messages", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(10.dp))

            // 🔹 Champ de recherche
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    filteredUsers = users.filter { it.nomUtilisateur.contains(query, ignoreCase = true) }
                },
                label = { Text("Rechercher un utilisateur...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔹 Liste des utilisateurs
            LazyColumn {
                items(filteredUsers) { user ->
                    if (user.nomUtilisateur != currentUsername) {
                        Button(
                            onClick = { selectedChatUser = user.nomUtilisateur }, // ✅ Sélectionne l'utilisateur
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        ) {
                            Text("Ouvrir la discussion avec ${user.nomUtilisateur}")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 🔹 Liste des conversations récentes
            Text("Conversations récentes", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            LazyColumn {
                items(conversations) { conversation ->
                    val chatPartnerName = if (conversation.user1 == currentUsername) conversation.user2 else conversation.user1

                    Button(
                        onClick = { selectedChatUser = chatPartnerName }, // ✅ Ouvre la conversation
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Text("Reprendre la discussion avec $chatPartnerName")
                    }
                }
            }
        } else {
            // ✅ Lorsque `selectedChatUser` est défini, on affiche `ChatScreen()`
            ChatScreen(receiverName = selectedChatUser!!) {
                selectedChatUser = null // ✅ Retour à la liste des messages quand on appuie sur "Retour"
            }
        }
    }
}

