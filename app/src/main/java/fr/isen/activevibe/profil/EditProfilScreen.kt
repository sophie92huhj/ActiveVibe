package fr.isen.activevibe.profil

import android.net.Uri
import android.util.Log
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
import androidx.compose.material.icons.filled.ArrowBack
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
import fr.isen.activevibe.UserProfile


@Composable
fun EditProfilScreen(
    userProfile: UserProfile,
    saveProfile: (UserProfile) -> Unit,
    onBackClick: () -> Unit
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

    var isEditing by remember { mutableStateOf(false) } // Mode édition activé/désactivé

    val genderOptions = listOf("Homme", "Femme")
    val levelOptions = listOf("Débutant", "Intermédiaire", "Avancé")
    val sportOptions = listOf("Natation", "Course à pied", "Vélo")

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
            profileImageUri = profileImageUri?.toString() ?: "",
            nomUtilisateur = userProfile.nomUtilisateur,
            nom = name,
            email = userEmail,
            age = age,
            gender = gender,
            nationality = nationality,
            height = height,
            weight = weight,
            sport = sport,
            level = level,
            team = team
        )
        saveProfile(updatedUser) // Sauvegarde du profil
        onBackClick() // Retour au profil après sauvegarde
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🔹 Bouton retour
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Retour")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Photo de profil
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
                Text("photo", color = Color.White)
            }
        }

        // 🔹 Bouton pour activer l'édition
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { isEditing = !isEditing }) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Modifier le profil")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Champs de saisie des informations personnelles
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nom") },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEditing
        )

        OutlinedTextField(
            value = userEmail,
            onValueChange = { userEmail = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEditing
        )

        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Âge") },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEditing
        )

        DropdownField(
            label = "Sexe",
            options = genderOptions,
            selectedOption = gender,
            onOptionSelected = { gender = it },
            enabled = isEditing
        )

        OutlinedTextField(
            value = nationality,
            onValueChange = { nationality = it },
            label = { Text("Nationalité") },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEditing
        )

        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Taille (cm)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEditing
        )

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Poids (kg)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEditing
        )

        // 🔹 Affichage de l'IMC calculé
        if (bmi != null) {
            Text("IMC : %.2f".format(bmi), style = MaterialTheme.typography.bodyMedium)
        }

        DropdownField(
            label = "Sport pratiqué",
            options = sportOptions,
            selectedOption = sport,
            onOptionSelected = { sport = it },
            enabled = isEditing
        )

        DropdownField(
            label = "Niveau",
            options = levelOptions,
            selectedOption = level,
            onOptionSelected = { level = it },
            enabled = isEditing
        )

        OutlinedTextField(
            value = team,
            onValueChange = { team = it },
            label = { Text("Club / Équipe actuelle") },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEditing
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Bouton Enregistrer
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
fun DropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            readOnly = true,
            enabled = enabled // Le champ est activé uniquement en mode édition
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


