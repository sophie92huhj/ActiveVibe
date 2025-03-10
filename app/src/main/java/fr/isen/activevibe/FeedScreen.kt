package fr.isen.activevibe

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*
import java.io.File
import androidx.compose.ui.text.font.FontWeight

@Composable
fun FeedScreen(modifier: Modifier = Modifier) {
    val database = FirebaseDatabase.getInstance().getReference("publications")
    val publications = remember { mutableStateListOf<Publication>() }

    database.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            publications.clear()
            for (child in snapshot.children) {
                val pub = child.getValue(Publication::class.java)
                if (pub != null) publications.add(pub)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("FeedScreen", "Erreur de chargement des publications : ${error.message}")
        }
    })

    Column(modifier = modifier.fillMaxSize()) {
        // ✅ Barre de navigation en haut (comme Instagram)
        TopAppBar(
            title = { Text("ActiveVibe", fontSize = 22.sp) },
            backgroundColor = Color.White,
            elevation = 0.dp
        )

        // ✅ Feed avec les publications
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(publications) { publication ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = 0.dp
                ) {
                    Column {
                        // 🔹 Barre du haut (nom + icône menu)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profil",
                                tint = Color.Gray,
                                modifier = Modifier
                                    .size(40.dp)
                                   // .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Utilisateur", fontSize = 14.sp)
                        }

                        // 🔹 Affichage de l’image
                        publication.imageUrl?.let { imagePath ->
                            DisplayImage(imagePath)
                        }

                        // 🔹 Description & Détails
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = publication.sportType, fontWeight = FontWeight.Bold)
                            Text(text = publication.description)

                            // Affichage conditionnel des stats
                            publication.duration?.let { Text(text = "⏳ Durée : $it min") }
                            publication.distance?.let { Text(text = "📏 Distance : $it km") }
                            publication.speed?.let { Text(text = "⚡ Vitesse : $it km/h") }
                        }

                        // 🔹 Ligne des actions (Like, Commentaire, Partage)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { /* Action Like */ }) {
                                Icon(Icons.Default.FavoriteBorder, contentDescription = "Like", tint = Color.Black)
                            }
                            IconButton(onClick = { /* Action Commentaire */ }) {
                                Icon(Icons.Default.Comment, contentDescription = "Comment", tint = Color.Black)
                            }
                            IconButton(onClick = { /* Action Partage */ }) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
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
            contentDescription = "Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentScale = ContentScale.Crop
        )
    }
}