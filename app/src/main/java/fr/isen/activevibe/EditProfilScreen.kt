package fr.isen.activevibe

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material3.ExposedDropdownMenuBox
//import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.DropdownMenuItem

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.material3.icons.Icons
//import androidx.compose.material3.icons.filled.Edit

@Composable
fun EditProfileScreen(
    modifier: Modifier = Modifier,
    userProfile: UserProfile,
    saveProfile: (UserProfile) -> Unit
) {
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var name by remember { mutableStateOf(userProfile.nom) }
    var userEmail by remember { mutableStateOf(userProfile.email) }
    var age by remember { mutableStateOf(userProfile.age) }
    var gender by remember { mutableStateOf(userProfile.gender) }
    var nationality by remember { mutableStateOf(userProfile.nationality) }
    var height by remember { mutableStateOf(userProfile.height) }
    var weight by remember { mutableStateOf(userProfile.weight) }
    var sport by remember { mutableStateOf(userProfile.sport) }
    var level by remember { mutableStateOf(userProfile.level) }
    var team by remember { mutableStateOf(userProfile.team) }

    val genderOptions = listOf("Homme", "Femme")
    val levelOptions = listOf("Débutant", "Intermédiaire", "Avancé")
    val sportOptions = listOf("Natation", "Course à pieds", "Vélo")

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

    val saveUserProfile = {
        val updatedUser = UserProfile(
            name, userEmail, age, gender, nationality,
            height, weight, sport, level, team,
        )
        saveProfile(updatedUser)
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
                Text("Photo", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nom
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom") }, modifier = Modifier.weight(1f))
            IconButton(onClick = { /* Logique de modification */ }) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Modifier le nom")
            }
        }

        // Email
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = userEmail, onValueChange = { userEmail = it }, label = { Text("Email") }, modifier = Modifier.weight(1f))
            IconButton(onClick = { /* Logique de modification */ }) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Modifier l'email")
            }
        }

        // Âge
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Âge") }, modifier = Modifier.weight(1f))
            IconButton(onClick = { /* Logique de modification */ }) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Modifier l'âge")
            }
        }

        // Sexe
        Row(modifier = Modifier.fillMaxWidth()) {
            DropdownField(label = "Sexe", options = genderOptions, selectedOption = gender, onOptionSelected = { gender = it })
            IconButton(onClick = { /* Logique de modification */ }) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Modifier le sexe")
            }
        }

        // Nationalité
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = nationality, onValueChange = { nationality = it }, label = { Text("Nationalité") }, modifier = Modifier.weight(1f))
            IconButton(onClick = { /* Logique de modification */ }) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Modifier la nationalité")
            }
        }

        // Taille
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Taille (cm)") }, modifier = Modifier.weight(1f))
            IconButton(onClick = { /* Logique de modification */ }) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Modifier la taille")
            }
        }

        // Poids
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Poids (kg)") }, modifier = Modifier.weight(1f))
            IconButton(onClick = { /* Logique de modification */ }) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Modifier le poids")
            }
        }

        if (bmi != null) {
            Text("IMC : %.2f".format(bmi), style = MaterialTheme.typography.bodyMedium)
        }

        // Sport
        Row(modifier = Modifier.fillMaxWidth()) {
            DropdownField(label = "Sport pratiqué", options = sportOptions, selectedOption = sport, onOptionSelected = { sport = it })
            IconButton(onClick = { /* Logique de modification */ }) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Modifier le sport")
            }
        }

        // Niveau
        Row(modifier = Modifier.fillMaxWidth()) {
            DropdownField(label = "Niveau", options = levelOptions, selectedOption = level, onOptionSelected = { level = it })
            IconButton(onClick = { /* Logique de modification */ }) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Modifier le niveau")
            }
        }

        // Équipe
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = team, onValueChange = { team = it }, label = { Text("Club / Équipe actuelle") }, modifier = Modifier.weight(1f))
            IconButton(onClick = { /* Logique de modification */ }) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Modifier l'équipe")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { saveUserProfile() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF433AF1))
        ) {
            Text("Enregistrer")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(label: String, options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            readOnly = true
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onOptionSelected(option)
                    expanded = false
                })
            }
        }
    }
}

