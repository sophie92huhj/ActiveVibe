package fr.isen.activevibe

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*
import java.io.File

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

    LazyColumn(modifier = modifier.padding(16.dp)) {
        items(publications) { publication ->
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = "Sport: ${publication.sportType}")
                Text(text = "Description: ${publication.description}")

                // ✅ Afficher les statistiques facultatives si elles existent
                if (!publication.duration.isNullOrEmpty()) {
                    Text(text = "⏱ Durée: ${publication.duration} min")
                }
                if (!publication.distance.isNullOrEmpty()) {
                    Text(text = "📏 Distance: ${publication.distance} km")
                }
                if (!publication.speed.isNullOrEmpty()) {
                    Text(text = "⚡ Vitesse: ${publication.speed} km/h")
                }

                // ✅ Vérifie si une image est disponible avant d'afficher
                if (!publication.imageUrl.isNullOrEmpty()) {
                    DisplayImage(publication.imageUrl!!)
                }

                Spacer(modifier = Modifier.height(16.dp))
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
            contentDescription = "Image locale",
            modifier = Modifier.size(200.dp)
        )
    } else {
        Text("Image non trouvée")
    }
}