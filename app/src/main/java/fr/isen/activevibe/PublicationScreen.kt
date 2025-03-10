package fr.isen.activevibe

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Send
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
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*

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
    val sports = listOf("Course à pied", "Tennis", "Rugby", "Natation")

    val database = FirebaseDatabase.getInstance().getReference("publications")

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ Titre en haut
        Text(
            text = "Ajouter une publication",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF433AF1)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ✅ Sélection du sport
        Box(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF0F0F0))
            ) {
                Text(sportType, color = Color.Black)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                sports.forEach { sport ->
                    DropdownMenuItem(onClick = {
                        sportType = sport
                        expanded = false
                    }) {
                        Text(text = sport)
                    }
                }
            }
        }

        // ✅ Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        // ✅ Champs spécifiques pour Course à pied et Natation
        if (sportType == "Course à pied" || sportType == "Natation") {
            OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Durée (min)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = distance, onValueChange = { distance = it }, label = { Text("Distance (km)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = speed, onValueChange = { speed = it }, label = { Text("Vitesse (km/h)") }, modifier = Modifier.fillMaxWidth())
        }

        // ✅ Sélection d’image facultative
        Box(
            modifier = Modifier.size(200.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            imageUri?.let {
                Image(painter = rememberAsyncImagePainter(it), contentDescription = null, modifier = Modifier.fillMaxSize())
            } ?: Icon(Icons.Default.AddAPhoto, contentDescription = "Ajouter une image", modifier = Modifier.size(80.dp), tint = Color.Gray)
        }

        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
            Text("Ajouter une photo")
        }

        // ✅ Bouton de publication
        FloatingActionButton(onClick = {
            if (sportType != "Sélectionner un sport" && description.isNotEmpty()) {
                val localImagePath = imageUri?.let { saveImageLocally(context, it) }
                savePublicationToDatabase(sportType, description, duration, distance, speed, localImagePath, context, database)
            } else {
                Toast.makeText(context, "Veuillez remplir au moins le sport et la description", Toast.LENGTH_SHORT).show()
            }
        }, backgroundColor = Color.Black) {
            Icon(Icons.Default.Send, contentDescription = "Publier", tint = Color.White)
        }
    }
}

// ✅ Fonction pour enregistrer une image localement
fun saveImageLocally(context: Context, imageUri: Uri): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
        val file = File(context.filesDir, "image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)

        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        file.absolutePath
    } catch (e: Exception) {
        null
    }
}

// ✅ Fonction pour enregistrer une publication dans Firebase
fun savePublicationToDatabase(
    sportType: String,
    description: String,
    duration: String?,
    distance: String?,
    speed: String?,
    imagePath: String?,
    context: Context,
    database: DatabaseReference
) {
    val newPublication = database.push()
    val publication = Publication(
        id = newPublication.key ?: "",
        sportType = sportType,
        description = description,
        duration = duration.takeIf { it?.isNotEmpty() == true },
        distance = distance.takeIf { it?.isNotEmpty() == true },
        speed = speed.takeIf { it?.isNotEmpty() == true },
        imageUrl = imagePath,
        timestamp = System.currentTimeMillis()
    )

    newPublication.setValue(publication)
        .addOnSuccessListener {
            Toast.makeText(context, "Publication enregistrée !", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show()
        }
}