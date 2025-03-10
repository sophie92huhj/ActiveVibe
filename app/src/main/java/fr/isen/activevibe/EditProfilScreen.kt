package fr.isen.activevibe

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User


// Modèle User pour correspondre à Firebase
data class UserProfile(
    val nom: String = "",
    val email: String = "",
    val age: String = "",
    val gender: String = "",
    val nationality: String = "",
    val height: String = "",
    val weight: String = "",
    val sport: String = "",
    val role: String = "",
    val level: String = "",
    val team: String = "",
    val achievements: String = "")

@Composable
fun EditProfileScreen(
    modifier: Modifier = Modifier,
    nom: String,
    email: String,
    saveProfile: (UserProfile) -> Unit
) {
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var name by remember { mutableStateOf(nom) }
    var userEmail by remember { mutableStateOf(email) }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var sport by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("") }
    var team by remember { mutableStateOf("") }
    var achievements by remember { mutableStateOf("") }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    val bmi = remember(height, weight) {
        val heightMeters = height.toFloatOrNull()?.div(100) ?: 0f
        val weightKg = weight.toFloatOrNull() ?: 0f
        if (heightMeters > 0) weightKg / (heightMeters * heightMeters) else null
    }

    // Sauvegarder les informations
    val saveUserProfile = {
        val updatedUser = UserProfile(name, userEmail) // Vous pouvez ajouter d'autres champs si nécessaire
        saveProfile(updatedUser) // Appel de la fonction pour sauvegarder les données
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.Gray, shape = CircleShape)
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (profileImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(profileImageUri),
                    contentDescription = "Photo de profil",
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("Ajouter une photo", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Informations générales", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = userEmail, onValueChange = { userEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Âge") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = gender, onValueChange = { gender = it }, label = { Text("Sexe") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = nationality, onValueChange = { nationality = it }, label = { Text("Nationalité") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        Text("Informations physiques", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Taille (cm)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Poids (kg)") }, modifier = Modifier.fillMaxWidth())

        if (bmi != null) {
            Text("IMC : %.2f".format(bmi), style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Discipline sportive", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = sport, onValueChange = { sport = it }, label = { Text("Sport pratiqué") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Poste / Rôle spécifique") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = level, onValueChange = { level = it }, label = { Text("Niveau (débutant, intermédiaire, avancé, professionnel)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = team, onValueChange = { team = it }, label = { Text("Club / Équipe actuelle") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = achievements, onValueChange = { achievements = it }, label = { Text("Palmarès / Titres / Récompenses") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { saveUserProfile() }) {
            Text("Sauvegarder")
        }
    }
}
