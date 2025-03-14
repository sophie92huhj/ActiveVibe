package fr.isen.activevibe.publications

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import fr.isen.activevibe.Publication
import fr.isen.activevibe.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(modifier: Modifier = Modifier) {
    val database = FirebaseDatabase.getInstance().getReference("publications")
    val publications = remember { mutableStateListOf<Publication>() }

    LaunchedEffect(Unit) {
        database.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetchedPublications = mutableListOf<Publication>()
                for (child in snapshot.children.reversed()) {
                    val pub = child.getValue(Publication::class.java)
                    if (pub != null) {
                        fetchedPublications.add(pub)
                        Log.d("FirebaseDebug", "Publication récupérée: ${pub.nomUtilisateur}")
                    }
                }
                publications.clear()
                publications.addAll(fetchedPublications)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FeedScreen", "Erreur chargement publications : ${error.message}")
            }
        })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (publications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Aucune publication disponible", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(publications) { publication ->
                    PublicationCard(publication)
                }
            }
        }
    }
}

@Composable
fun PublicationCard(publication: Publication) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val userLikesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("likes")
    val commentsRef = FirebaseDatabase.getInstance().getReference("publications").child(publication.id).child("comments")
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var isLiked by remember { mutableStateOf(false) }
    var showCommentInput by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    //var comments by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var expandedMenu by remember { mutableStateOf(false) }
    var comments by remember { mutableStateOf<List<Triple<String, String, String>>>(listOf()) }


    LaunchedEffect(Unit) {
        userLikesRef.child(publication.id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLiked = snapshot.exists()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetchedComments = mutableListOf<Triple<String, String, String>>() // Stocke (id, username, message)
                for (child in snapshot.children) {
                    val commentId = child.key ?: continue // L'ID du commentaire dans Firebase
                    val username = child.child("nomUtilisateur").getValue(String::class.java) ?: "Utilisateur inconnu"
                    val message = child.child("message").getValue(String::class.java) ?: ""
                    fetchedComments.add(Triple(commentId, username, message)) // Ajoute l'ID Firebase
                }
                comments = fetchedComments
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FeedScreen", "Erreur récupération des commentaires : ${error.message}")
            }
        })
    }

    LaunchedEffect(publication.nomUtilisateur) {
        val userProfileRef = FirebaseDatabase.getInstance().getReference("users").orderByChild("nomUtilisateur").equalTo(publication.nomUtilisateur)
        userProfileRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    profileImageUrl = child.child("profileImageUrl").getValue(String::class.java)
                    break
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Erreur récupération image de profil : ${error.message}")
            }
        })
    }


    fun deleteComment(commentId: String) {
        commentsRef.child(commentId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                comments = comments.filterNot { it.first == commentId } // Supprime localement aussi
                Log.d("FeedScreen", "Commentaire supprimé avec succès.")
            } else {
                Log.e("FeedScreen", "Erreur lors de la suppression du commentaire.")
            }
        }
    }


    fun deleteAllComments() {
        commentsRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FeedScreen", "Tous les commentaires ont été supprimés")
            } else {
                Log.e("FeedScreen", "Erreur lors de la suppression des commentaires")
            }
        }
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.background(Color.White)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(profileImageUrl ?: R.drawable.profile),
                    contentDescription = "Photo de profil",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = publication.nomUtilisateur ?: "Utilisateur inconnu",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )


                IconButton(onClick = { expandedMenu = !expandedMenu }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }


                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Supprimer tous les commentaires") },
                        onClick = {
                            deleteAllComments()
                            expandedMenu = false
                        }
                    )
                }
            }

            // Image de la publication
            publication.imageUrl?.let { imageUrl ->
                if (imageUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Image de la publication",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }

            //  Afficher le type de sport
            publication.sportType?.let { sport ->
                Text(
                    text = "$sport",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Description de la publication
            Text(
                text = publication.description,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )



            // Barre d'actions (Like, Commentaire)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = {
                    if (isLiked) {
                        userLikesRef.child(publication.id).removeValue()
                    } else {
                        userLikesRef.child(publication.id).setValue(true)
                    }
                    isLiked = !isLiked
                }) {
                    Icon(
                        imageVector = Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.Black
                    )
                }
                IconButton(onClick = { showCommentInput = !showCommentInput }) {
                    Icon(Icons.Default.ModeComment, contentDescription = "Commenter", tint = Color.Black)
                }
            }

            // Affichage du champ de commentaire
            if (showCommentInput) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        label = { Text("Ajouter un commentaire...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (commentText.isNotEmpty()) {
                                val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

                                userRef.child("nomUtilisateur").get().addOnSuccessListener { snapshot ->
                                    val username = snapshot.value as? String ?: "Utilisateur inconnu"

                                    val newCommentRef = commentsRef.push()
                                    val commentData = mapOf(
                                        //"uid" to userId,
                                        "nomUtilisateur" to username,
                                        "message" to commentText
                                    )
                                    newCommentRef.setValue(commentData)
                                    commentText = ""
                                    showCommentInput = false
                                }
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("Envoyer")
                    }
                }
            }


           if (comments.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {

                   // val commentsList = comments.toList()
                    comments.forEach { (commentId, username, message) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$username : $message",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { deleteComment(commentId) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Supprimer", tint = Color.Black)
                            }
                        }
                    }

                            }
                        }
                    }
                }
            }



