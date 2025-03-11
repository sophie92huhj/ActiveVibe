package fr.isen.activevibe.publications

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.res.painterResource
import com.google.firebase.auth.FirebaseAuth
import fr.isen.activevibe.Publication
import fr.isen.activevibe.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(modifier: Modifier = Modifier) {
    val database = FirebaseDatabase.getInstance().getReference("publications")
    val publications = remember { mutableStateListOf<Publication>() }

    // ✅ DEBUG : Vérifier si Firebase récupère bien les publications
    LaunchedEffect(Unit) {
        database.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetchedPublications = mutableListOf<Publication>()
                for (child in snapshot.children.reversed()) {
                    val pub = child.getValue(Publication::class.java)
                    Log.d("FirebaseDebug", "Publication récupérée: $pub") // 🔥 DEBUG
                    if (pub != null) fetchedPublications.add(pub)
                }
                publications.clear()
                publications.addAll(fetchedPublications)

                // Vérifier si on a bien des publications
                Log.d("UI", "Nombre de publications affichées: ${publications.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FeedScreen", "Erreur chargement publications : ${error.message}")
            }
        })
    }

    Column(modifier = Modifier.fillMaxSize()) {

        if (publications.isEmpty()) {
            Log.d("UI", "Aucune publication affichée.") // 🔥 DEBUG
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

    var isLiked by remember { mutableStateOf(false) }
    var showCommentInput by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf(listOf<Pair<String, String>>()) } // Liste des commentaires (Nom, Message)

    // Vérifier si la publication est déjà likée
    LaunchedEffect(Unit) {
        userLikesRef.child(publication.id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLiked = snapshot.exists()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // Charger les commentaires
        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetchedComments = mutableListOf<Pair<String, String>>()
                for (child in snapshot.children) {
                    val username = child.child("nomUtilisateur").getValue(String::class.java) ?: "Utilisateur inconnu"
                    val message = child.child("message").getValue(String::class.java) ?: ""
                    fetchedComments.add(username to message)
                }
                comments = fetchedComments
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FeedScreen", "Erreur récupération des commentaires : ${error.message}")
            }
        })
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.background(Color.White)) {
            // ✅ En-tête
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = publication.username ?: "Utilisateur inconnu",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // ✅ Image de la publication
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

            // ✅ Description de la publication
            Text(
                text = publication.description,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )

            // ✅ Barre d'actions (Like, Commentaire)
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
                        imageVector = if (isLiked) Icons.Filled.FavoriteBorder else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.Black
                    )
                }
                IconButton(onClick = { showCommentInput = !showCommentInput }) {
                    Icon(Icons.Default.ModeComment, contentDescription = "Commenter", tint = Color.Black)
                }
            }

            // ✅ Affichage du champ de commentaire
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
                                        "uid" to userId,
                                        "nomUtilisateur" to username,
                                        "message" to commentText
                                    )
                                    newCommentRef.setValue(commentData)
                                    commentText = ""
                                    showCommentInput = false
                                }
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Envoyer")
                    }
                }
            }

            // ✅ Affichage des commentaires sous la publication
            if (comments.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    comments.forEach { (username, message) ->
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
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DisplayImage(imagePath: String) {
    val context = LocalContext.current
    val imageFile = File(imagePath)

    if (imageFile.exists()) {
        val bitmap = BitmapFactory.decodeFile(imagePath)
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Image de la publication",
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp) // Taille d'image similaire à Instagram
                .clip(RoundedCornerShape(0.dp))
        )
    }
}

fun formatTimestamp(timestamp: Long?): String {
    return timestamp?.let {
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(it))
    } ?: "Date inconnue"
}