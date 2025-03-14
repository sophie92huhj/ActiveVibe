package fr.isen.activevibe.messages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.foundation.Image
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
        return "${user1}_$user2"
    }

    fun sendMessage(sender: String, receiver: String, text: String) {
        val timestamp = System.currentTimeMillis()
        val message = Message(sender, receiver, text, timestamp)

        // ✅ Créer deux conversations indépendantes sous "messages/"
        val conversationA = database.child("${sender}_$receiver")
        val conversationB = database.child("${receiver}_$sender")

        // ✅ Sauvegarder dans messages/userA_userB
        conversationA.push().setValue(message)

        // ✅ Sauvegarder dans messages/userB_userA
        conversationB.push().setValue(message)
    }

    fun getMessages(conversationId: String, onMessagesLoaded: (List<Message>) -> Unit) {
        database.child(conversationId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messagesList = mutableListOf<Message>()
                for (child in snapshot.children) {
                    val message = child.getValue(Message::class.java)
                    if (message != null) messagesList.add(message)
                }
                messagesList.sortBy { it.timestamp } // Trie les messages par date
                onMessagesLoaded(messagesList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MessagesRepository", "Erreur chargement messages : ${error.message}")
            }
        })
    }

    fun getUserConversations(userName: String, onConversationsLoaded: (List<Pair<String, Long>>) -> Unit) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val conversationList = mutableListOf<Pair<String, Long>>() // ✅ Liste avec ID de conversation + timestamp du dernier message

                for (child in snapshot.children) {
                    val conversationId = child.key ?: continue
                    if (conversationId.startsWith(userName)) { // ✅ Filtrer par utilisateur connecté
                        val lastMessageTimestamp = child.children.mapNotNull {
                            it.child("timestamp").getValue(Long::class.java)
                        }.maxOrNull() ?: 0 // ✅ Récupérer le dernier message (ou 0 si aucun)

                        conversationList.add(Pair(conversationId, lastMessageTimestamp))
                    }
                }

                // ✅ Trier par dernier message (du plus récent au plus ancien)
                val sortedConversations = conversationList.sortedByDescending { it.second }

                onConversationsLoaded(sortedConversations)
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

    fun getProfileImageUrl(userName: String, onResult: (String?) -> Unit) {
        usersDatabase.orderByChild("nomUtilisateur").equalTo(userName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        val imageUrl = child.child("profileImageUrl").getValue(String::class.java)
                        onResult(imageUrl) // ✅ Retourne l'URL de l'image
                        return
                    }
                    onResult(null) // ✅ Si aucune image trouvée, renvoyer `null`
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MessagesRepository", "Erreur récupération photo de profil : ${error.message}")
                    onResult(null)
                }
            })
    }

    fun listenForMessages(currentUser: String, chatPartner: String, onMessagesUpdated: (List<Message>) -> Unit) {
        val conversationId1 = "${currentUser}_$chatPartner"
        val conversationId2 = "${chatPartner}_$currentUser"

        val messagesRef1 = FirebaseDatabase.getInstance().reference.child("messages").child(conversationId1)
        val messagesRef2 = FirebaseDatabase.getInstance().reference.child("messages").child(conversationId2)

        val messagesList = mutableStateListOf<Message>()

        val messageListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)

                if (message != null && messagesList.none { it.timestamp == message.timestamp && it.text == message.text }) {
                    // ✅ Ajout uniquement si le message n'existe pas déjà
                    messagesList.add(message)
                    messagesList.sortBy { it.timestamp } // ✅ Trie du plus ancien au plus récent
                    onMessagesUpdated(messagesList.toList())
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("MessagesRepository", "Erreur récupération messages : ${error.message}")
            }
        }

        messagesRef1.addChildEventListener(messageListener) // ✅ Écoute messages envoyés
        messagesRef2.addChildEventListener(messageListener) // ✅ Écoute messages reçus
    }




}

@Composable
fun Message() {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var currentUsername by remember { mutableStateOf("") }
    var conversations by remember { mutableStateOf(emptyList<String>()) }
    var users by remember { mutableStateOf(emptyList<UserProfile>()) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var selectedChatUser by remember { mutableStateOf<String?>(null) }

    // 🔹 Charger l’utilisateur connecté et ses conversations
    LaunchedEffect(Unit) {
        val userId = currentUser?.uid ?: return@LaunchedEffect
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        userRef.child("nomUtilisateur").get().addOnSuccessListener { snapshot ->
            val fetchedUsername = snapshot.value as? String ?: ""
            currentUsername = fetchedUsername
            Log.d("MessagesDebug", "Utilisateur récupéré : $currentUsername")

            MessagesRepository.getUserConversations(fetchedUsername) { conversationList ->
                conversations = conversationList.map { it.first } // ✅ Extraire uniquement l’ID de conversation
            }

            MessagesRepository.getUsers { userList ->
                users = userList.filter { it.nomUtilisateur != fetchedUsername }
            }
        }
    }


    selectedChatUser?.let { chatUser ->
        ChatScreen(receiverName = chatUser) { selectedChatUser = null }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Messages", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { showSearch = !showSearch },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF433AF1)) // ✅ Couleur bleue
        ) {
            Text(
                text = if (showSearch) "Fermer la recherche" else "Nouvelle discussion",
                color = Color.White // ✅ Texte en blanc pour le contraste
            )
        }


        Spacer(modifier = Modifier.height(10.dp))

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
                    var profileImageUrl by remember { mutableStateOf<String?>(null) }

                    // ✅ Charger l'image de profil pour chaque utilisateur
                    LaunchedEffect(user.nomUtilisateur) {
                        MessagesRepository.getProfileImageUrl(user.nomUtilisateur) { url ->
                            profileImageUrl = url
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { selectedChatUser = user.nomUtilisateur; showSearch = false },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ✅ Afficher la photo de profil si disponible
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!profileImageUrl.isNullOrEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(profileImageUrl),
                                    contentDescription = "Photo de profil",
                                    modifier = Modifier.size(50.dp)
                                )
                            } else {
                                Text(
                                    text = user.nomUtilisateur.first().toString().uppercase(),
                                    color = Color.White,
                                    fontSize = 20.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(text = user.nomUtilisateur, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(10.dp))

        Text("Conversations récentes", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        if (conversations.isEmpty()) {
            Text("Aucune conversation pour le moment", color = Color.Gray, fontSize = 16.sp)
        } else {
            LazyColumn {
                items(conversations) { conversationId ->
                    val chatPartnerName = conversationId.split("_").last()
                    var profileImageUrl by remember { mutableStateOf<String?>(null) }

                    // ✅ Charger l'image de profil pour chaque utilisateur
                    LaunchedEffect(chatPartnerName) {
                        MessagesRepository.getProfileImageUrl(chatPartnerName) { url ->
                            profileImageUrl = url
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { selectedChatUser = chatPartnerName },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ✅ Afficher l'image de profil
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!profileImageUrl.isNullOrEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(profileImageUrl),
                                    contentDescription = "Photo de profil",
                                    modifier = Modifier.size(50.dp)
                                )
                            } else {
                                Text(
                                    text = chatPartnerName.first().toString().uppercase(),
                                    color = Color.White,
                                    fontSize = 20.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(text = chatPartnerName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


