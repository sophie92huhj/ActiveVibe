package fr.isen.activevibe

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.MoreVert
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

@Composable
fun FeedScreen(modifier: Modifier = Modifier) {
    val database = FirebaseDatabase.getInstance().getReference("publications")
    val publications = remember { mutableStateListOf<Publication>() }

    database.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            publications.clear()
            for (child in snapshot.children.reversed()) {
                val pub = child.getValue(Publication::class.java)
                if (pub != null) publications.add(pub)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("FeedScreen", "Erreur de chargement des publications : ${error.message}")
        }
    })

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(publications) { publication ->
            PublicationCard(publication)
        }
    }
}

@Composable
fun PublicationCard(publication: Publication) {
    var commentText by remember { mutableStateOf("") }  // Pour le texte du commentaire
    var showCommentInput by remember { mutableStateOf(false) }  // Afficher ou non le champ de texte
    var comments by remember { mutableStateOf(listOf<String>()) }  // Liste des commentaires

    // Charger les commentaires depuis Firebase (si nécessaire)
    val database = FirebaseDatabase.getInstance().getReference("publications")
    val commentsRef = database.child(publication.id).child("comments")

    // Récupérer les commentaires de la publication
    commentsRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val fetchedComments = mutableListOf<String>()
            for (child in snapshot.children) {
                child.getValue(String::class.java)?.let {
                    fetchedComments.add(it)
                }
            }
            comments = fetchedComments
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("FeedScreen", "Erreur lors de la récupération des commentaires : ${error.message}")
        }
    })

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            // ✅ En-tête (Sport + Timestamp + Auteur + Options)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = publication.sportType,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatTimestamp(publication.timestamp),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                IconButton(onClick = { /* Options */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
            }

            // ✅ Image si disponible
            publication.imageUrl?.let { imagePath ->
                if (imagePath.isNotEmpty()) {
                    DisplayImage(imagePath)
                }
            }

            // ✅ Description
            Text(
                text = publication.description,
                fontSize = 14.sp,
                modifier = Modifier.padding(12.dp)
            )

            // ✅ Statistiques (Si renseignées)
            if (!publication.duration.isNullOrEmpty() ||
                !publication.distance.isNullOrEmpty() ||
                !publication.speed.isNullOrEmpty()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    publication.duration?.let {
                        Text(text = "Durée : $it min", fontSize = 12.sp, color = Color.Gray)
                    }
                    publication.distance?.let {
                        Text(text = "Distance : $it km", fontSize = 12.sp, color = Color.Gray)
                    }
                    publication.speed?.let {
                        Text(text = "Vitesse : $it km/h", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            // ✅ Barre d'actions (Like, Commentaire, Partage)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    IconButton(onClick = { /* Ajouter un like */ }) {
                        Icon(Icons.Default.Favorite, contentDescription = "Like", tint = Color.Black)
                    }
                    IconButton(onClick = {
                        // Afficher ou cacher le champ de commentaire
                        showCommentInput = !showCommentInput
                    }) {
                        Icon(Icons.Default.ModeComment, contentDescription = "Commenter", tint = Color.Black)
                    }
                    IconButton(onClick = { /* Partager la publication */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Partager", tint = Color.Black)
                    }
                }
            }

            // ✅ Afficher le champ de commentaire si showCommentInput est true
            if (showCommentInput) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Écrire un commentaire") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )

                Button(
                    onClick = {
                        if (commentText.isNotEmpty()) {
                            // Sauvegarder le commentaire dans Firebase
                            val newCommentRef = commentsRef.push()
                            newCommentRef.setValue(commentText)

                            // Réinitialiser le champ de texte
                            commentText = ""
                            showCommentInput = false
                        }
                    },
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text("Envoyer")
                }
            }

            // ✅ Affichage des commentaires
            if (comments.isNotEmpty()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    comments.forEach { comment ->
                        Text(text = comment, fontSize = 14.sp, color = Color.Gray)
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
                .height(250.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

fun formatTimestamp(timestamp: Long?): String {
    return timestamp?.let {
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(it))
    } ?: "Date inconnue"
}