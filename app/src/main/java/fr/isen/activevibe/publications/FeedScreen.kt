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
    var selectedSport by remember { mutableStateOf<String?>(null) }
    var sportList by remember { mutableStateOf<List<String>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }

    // 🔹 Charger les publications et extraire les sports disponibles
    LaunchedEffect(Unit) {
        database.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetchedPublications = mutableListOf<Publication>()
                val sportsSet = mutableSetOf<String>()

                for (child in snapshot.children.reversed()) {
                    val pub = child.getValue(Publication::class.java)
                    if (pub != null) {
                        fetchedPublications.add(pub)
                        pub.sportType?.let { sportsSet.add(it) }
                    }
                }
                publications.clear()
                publications.addAll(fetchedPublications)
                sportList = sportsSet.toList().sorted()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FeedScreen", "Erreur chargement publications : ${error.message}")
            }
        })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        //  Filtre par sport
        if (sportList.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Button(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF433AF1),
                        contentColor = Color.White
                    )
                ) {
                    Text(text = selectedSport ?: "Filtrer par sport")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    //  Option "Tous les sports"
                    DropdownMenuItem(
                        text = { Text("Tous les sports") },
                        onClick = {
                            selectedSport = null
                            expanded = false
                        }
                    )

                    // Ajouter tous les sports disponibles
                    sportList.forEach { sport ->
                        DropdownMenuItem(
                            text = { Text(sport) },
                            onClick = {
                                selectedSport = sport
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Affichage des publications filtrées
        val filteredPublications = if (selectedSport == null) publications else publications.filter { it.sportType == selectedSport }

        if (filteredPublications.isEmpty()) {
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
                items(filteredPublications) { publication ->
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
                val fetchedComments = mutableListOf<Triple<String, String, String>>()
                for (child in snapshot.children) {
                    val commentId = child.key ?: continue
                    val username = child.child("nomUtilisateur").getValue(String::class.java) ?: "Utilisateur inconnu"
                    val message = child.child("message").getValue(String::class.java) ?: ""
                    fetchedComments.add(Triple(commentId, username, message))
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

            // 🔹 HEADER AVEC NOM + PHOTO
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
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

                Spacer(modifier = Modifier.weight(1f))

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
                            commentsRef.removeValue()
                            expandedMenu = false
                        }
                    )
                }
            }

            //  IMAGE + SPORT EN HAUT À DROITE
            Box(modifier = Modifier.fillMaxWidth()) {
                publication.imageUrl?.takeIf { it.isNotEmpty() }?.let { imageUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Image de la publication",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                publication.sportType?.takeIf { it.isNotEmpty() }?.let { sport ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(Color(0xff433af1), shape = RoundedCornerShape(bottomStart = 8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = sport, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            //  DESCRIPTION
            Text(
                text = publication.description,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )

            //  INFOS SUPPLÉMENTAIRES (Distance, Durée, Vitesse)
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                publication.distance?.takeIf { it.isNotEmpty() }?.let { distance ->
                    Text(text = "Distance : $distance km", fontSize = 12.sp, color = Color.Gray)
                }
                publication.duration?.takeIf { it.isNotEmpty() }?.let { duration ->
                    Text(text = "Durée : $duration min", fontSize = 12.sp, color = Color.Gray)
                }
                publication.speed?.takeIf { it.isNotEmpty() }?.let { speed ->
                    Text(text = "Vitesse : $speed km/h", fontSize = 12.sp, color = Color.Gray)
                }
            }

            //  BARRE D'ACTIONS (LIKE, COMMENTAIRE)
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
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

            //  CHAMP DE COMMENTAIRE
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



