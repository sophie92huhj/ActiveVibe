package fr.isen.activevibe.publications

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import fr.isen.activevibe.API.ImgurUploader
import androidx.compose.ui.res.painterResource
import com.google.firebase.auth.FirebaseAuth
import fr.isen.activevibe.Publication
import fr.isen.activevibe.R

@Composable
fun PublicationScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var sportType by remember { mutableStateOf("Sélectionner un sport") }
    var description by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var speed by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val sports = listOf("Course à pied", "Tennis", "Rugby", "Natation", "Basket", "Volley", "Repas", "Musculation", "Autre")

    val database = FirebaseDatabase.getInstance().getReference("publications")

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.activevibe),
            contentDescription = "Logo ActiveVibe",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 12.dp)
        )

        Text(
            text = "Ajouter une publication",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF433AF1)
        )

        Spacer(modifier = Modifier.height(20.dp))


        Box(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(Color(0xE5E0E8FF))
            ) {
                Text(sportType, color = Color.Black)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                sports.forEach { sport ->
                    DropdownMenuItem(onClick = {
                        sportType = sport
                        expanded = false
                    }, text = {
                        Text(text = sport)
                    })
                }
            }
        }


        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )


        if (sportType == "Course à pied" || sportType == "Natation") {
            OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Durée (min)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = distance, onValueChange = { distance = it }, label = { Text("Distance (km)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = speed, onValueChange = { speed = it }, label = { Text("Vitesse (km/h)") }, modifier = Modifier.fillMaxWidth())
        }


        if (imageUri != null) {
            Card(
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Image sélectionnée",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            colors = ButtonDefaults.buttonColors(Color(0xFF433AF1)),
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Text("Ajouter une photo", color = Color.White)
        }

        FloatingActionButton(
            onClick = {
                if (sportType != "Sélectionner un sport" && description.isNotEmpty()) {
                    imageUri?.let { uri ->
                        ImgurUploader.uploadToImgur(context, uri, { imageUrl ->
                            savePublicationToDatabase(sportType, description, duration, distance, speed, imageUrl, context, database)
                        }) {
                            Toast.makeText(context, "Échec de l'upload de l'image", Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        savePublicationToDatabase(sportType, description, duration, distance, speed, null, context, database)
                    }
                } else {
                    Toast.makeText(context, "Veuillez remplir au moins le sport et la description", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.background(Color.White)
        ) {
            Icon(Icons.Default.Send, contentDescription = "Publier", tint = Color.White)
        }
    }
}


fun savePublicationToDatabase(
    sportType: String,
    description: String,
    duration: String?,
    distance: String?,
    speed: String?,
    imageUrl: String?,
    context: Context,
    database: DatabaseReference
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    if (userId != null) {
        val usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId)


        usersRef.child("nomUtilisateur").get().addOnSuccessListener { snapshot ->
            val username = snapshot.value as? String ?: "Utilisateur inconnu"

            val newPublication = database.push()
            val publication = Publication(
                id = newPublication.key ?: "",
                sportType = sportType,
                description = description,
                duration = duration.takeIf { it?.isNotEmpty() == true },
                distance = distance.takeIf { it?.isNotEmpty() == true },
                speed = speed.takeIf { it?.isNotEmpty() == true },
                imageUrl = imageUrl,
                timestamp = System.currentTimeMillis(),
                nomUtilisateur = username
            )

            newPublication.setValue(publication)
                .addOnSuccessListener {
                    Toast.makeText(context, "Publication enregistrée !", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener {
            Toast.makeText(context, "Impossible de récupérer nomUtilisateur", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "Utilisateur non connecté", Toast.LENGTH_SHORT).show()
    }
}
