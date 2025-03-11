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
        // ✅ Barre supérieure bleue
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.activevibe), // Ton logo dans res/drawable
                        contentDescription = "Logo ActiveVibe",
                        modifier = Modifier
                            .size(64.dp) // Taille réduite du logo
                            .padding(end = 8.dp) // Espacement entre le logo et le texte
                    )
                    Text(
                        "Active Vibe",
                        color = Color(0xFF433AF1), // Bleu de l'application
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            },
            modifier = Modifier.background(Color.White), // Fond blanc comme sur l'image
        )

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
    var commentText by remember { mutableStateOf("") }
    var showCommentInput by remember { mutableStateOf(false) }
    var comments by remember { mutableStateOf(listOf<String>()) }
    var showMenu by remember { mutableStateOf(false) } // ✅ Gérer le menu déroulant

    var commentToDelete by remember { mutableStateOf<String?>(null) } // ✅ Stocker le commentaire sélectionné
    var showDeleteDialog by remember { mutableStateOf(false) } // ✅ Gérer la boîte de confirmation

    val database = FirebaseDatabase.getInstance().getReference("publications")
    val publicationRef = database.child(publication.id) // Référence pour supprimer la publication
    val commentsRef = publicationRef.child("comments")

    // ✅ Charger les commentaires depuis Firebase
    LaunchedEffect(Unit) {
        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetchedComments = mutableListOf<String>()
                for (child in snapshot.children) {
                    val comment = child.getValue(String::class.java)
                    Log.d("FirebaseDebug", "Comment récupéré: $comment") // 🔥 DEBUG
                    comment?.let { fetchedComments.add(it) }
                }
                comments = fetchedComments
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FeedScreen", "Erreur récupération des commentaires : ${error.message}")
            }
        })
    }

    // ✅ Boîte de dialogue de confirmation pour supprimer un commentaire
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer le commentaire") },
            text = { Text("Êtes-vous sûr de vouloir supprimer ce commentaire ? Cette action est irréversible.") },
            confirmButton = {
                Button(onClick = {
                    commentToDelete?.let { comment ->
                        commentsRef.orderByValue().equalTo(comment)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (child in snapshot.children) {
                                        child.ref.removeValue() // ✅ Supprime le commentaire
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("FeedScreen", "Erreur suppression commentaire : ${error.message}")
                                }
                            })
                    }
                    showDeleteDialog = false
                }) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // ✅ Affichage de la publication
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.background(Color.White)) {

            // ✅ En-tête (Nom d'utilisateur, options)
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
                ) {
                    // Placeholder pour la photo de profil
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = publication.username ?: "Utilisateur inconnu",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.weight(1f))

                // ✅ Bouton "..." pour le menu (suppression de publication)
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(text = {
                            Text("Supprimer la publication")
                        }, onClick = {
                            publicationRef.removeValue() // ✅ Supprime la publication
                            showMenu = false
                        })
                    }
                }
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

            // ✅ Description
            Text(
                text = publication.description,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )

            // ✅ Statistiques (temps, distance, vitesse si renseignés)
            if (!publication.duration.isNullOrEmpty() ||
                !publication.distance.isNullOrEmpty() ||
                !publication.speed.isNullOrEmpty()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
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
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { /* Ajouter un like */ }) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "Like", tint = Color.Black)
                }
                IconButton(onClick = { showCommentInput = !showCommentInput }) {
                    Icon(Icons.Default.ModeComment, contentDescription = "Commenter", tint = Color.Black)
                }
                IconButton(onClick = { /* Partager la publication */ }) {
                    Icon(Icons.Default.Send, contentDescription = "Partager", tint = Color.Black)
                }
            }

            // ✅ Champ de commentaire
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
                                val newCommentRef = commentsRef.push()
                                newCommentRef.setValue(commentText)
                                commentText = ""
                                showCommentInput = false
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Envoyer")
                    }
                }
            }

            // ✅ Affichage des commentaires
            if (comments.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    comments.forEach { comment ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = comment,
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.weight(1f)
                            )

                            // ✅ Bouton "..." pour supprimer un commentaire
                            IconButton(
                                onClick = {
                                    commentToDelete = comment
                                    showDeleteDialog = true
                                }
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Supprimer", tint = Color.Gray)
                            }
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