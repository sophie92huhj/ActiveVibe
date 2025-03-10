/*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit

@Composable
fun ModifyProfileScreen(
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
                Text("Ajouter une photo", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nom avec icône de crayon
        EditableFieldWithIcon(
            label = "Nom",
            value = name,
            onValueChange = { name = it }
        )

        // Email avec icône de crayon
        EditableFieldWithIcon(
            label = "Email",
            value = userEmail,
            onValueChange = { userEmail = it }
        )

        // Âge avec icône de crayon
        EditableFieldWithIcon(
            label = "Âge",
            value = age,
            onValueChange = { age = it }
        )

        // Sexe avec icône de crayon
        /*EditableDropdownFieldWithIcon(
            label = "Sexe",
            options = genderOptions,
            selectedOption = gender,
            onOptionSelected = { gender = it }
        )
        */

        // Nationalité avec icône de crayon
        EditableFieldWithIcon(
            label = "Nationalité",
            value = nationality,
            onValueChange = { nationality = it }
        )

        // Taille avec icône de crayon
        EditableFieldWithIcon(
            label = "Taille (cm)",
            value = height,
            onValueChange = { height = it }
        )

        // Poids avec icône de crayon
        EditableFieldWithIcon(
            label = "Poids (kg)",
            value = weight,
            onValueChange = { weight = it }
        )

        // Affichage de l'IMC
        if (bmi != null) {
            Text("IMC : %.2f".format(bmi), style = MaterialTheme.typography.bodyMedium)
        }

        // Sport pratiqué avec icône de crayon
        EditableDropdownFieldWithIcon(
            label = "Sport pratiqué",
            options = sportOptions,
            selectedOption = sport,
            onOptionSelected = { sport = it }
        )

        // Niveau avec icône de crayon
        EditableDropdownFieldWithIcon(
            label = "Niveau",
            options = levelOptions,
            selectedOption = level,
            onOptionSelected = { level = it }
        )

        // Club / Équipe actuelle avec icône de crayon
        EditableFieldWithIcon(
            label = "Club / Équipe actuelle",
            value = team,
            onValueChange = { team = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bouton pour enregistrer les modifications
        Button(
            onClick = { saveUserProfile() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF433AF1))
        ) {
            Text("Enregistrer")
        }
    }
}

@Composable
fun EditableFieldWithIcon(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = "Edit",
            modifier = Modifier
                .clickable { /* Logic for editing */ }
                .padding(start = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableDropdownFieldWithIcon(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }  // Ajout de l'état mutable pour l'expand
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }  // Mise à jour de l'état d'expansion
        ) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                label = { Text(label) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                readOnly = true
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false  // Ferme le menu après la sélection
                        }
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = "Edit",
            modifier = Modifier
                .clickable { expanded = !expanded }  // Lorsque l'icône de crayon est cliquée, toggle l'état d'expansion
                .padding(start = 8.dp)
        )
    }
}

*/