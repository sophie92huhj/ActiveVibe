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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material3.ExposedDropdownMenuBox
//import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.text.font.FontWeight
import fr.isen.activevibe.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.ExperimentalMaterial3Api
import fr.isen.activevibe.API.ImgurUploader
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext



@Composable
fun EditProfilScreen(
    userProfile: UserProfile,
    saveProfile: (UserProfile) -> Unit,
    onBackClick: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) } // Mode édition activé/désactivé

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
    val context = LocalContext.current // ✅ Récupérer le contexte ici
    // 🔹 Récupérer le nom et prénom depuis Firebase Firestore

    // 🔹 Déclaration des variables d'état pour l'UIv
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var nomUtilisateur by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    val user = FirebaseAuth.getInstance().currentUser
    var userEmail by remember { mutableStateOf(user?.email ?: "Email non disponible") }
    var age by remember { mutableStateOf(userProfile.age) }
    var gender by remember { mutableStateOf(userProfile.gender) }
    var nationality by remember { mutableStateOf(userProfile.nationality) }
    var height by remember { mutableStateOf(userProfile.height) }
    var weight by remember { mutableStateOf(userProfile.weight) }
    var sport by remember { mutableStateOf(userProfile.sport) }
    var level by remember { mutableStateOf(userProfile.level) }
    var team by remember { mutableStateOf(userProfile.team) }

// 🔹 Récupérer les données depuis Firebase et mettre à jour l'UI
    LaunchedEffect(userId) {
        if (userId != null) {
            database.child(userId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    nomUtilisateur = snapshot.child("nomUtilisateur").getValue(String::class.java) ?: "Non trouvé"
                    name = snapshot.child("nom").getValue(String::class.java) ?: name
                    surname = snapshot.child("prenom").getValue(String::class.java) ?: surname
                    userEmail = snapshot.child("email").getValue(String::class.java) ?: userEmail
                    age = snapshot.child("age").getValue(String::class.java) ?: age
                    gender = snapshot.child("gender").getValue(String::class.java) ?: gender
                    nationality = snapshot.child("nationality").getValue(String::class.java) ?: nationality
                    height = snapshot.child("height").getValue(String::class.java) ?: height
                    weight = snapshot.child("weight").getValue(String::class.java) ?: weight
                    sport = snapshot.child("sport").getValue(String::class.java) ?: sport
                    level = snapshot.child("level").getValue(String::class.java) ?: level
                    team = snapshot.child("team").getValue(String::class.java) ?: team

                    profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
                }
            }.addOnFailureListener {
                println("Erreur lors de la récupération des données Firebase")
            }
        }
    }



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


        if (userId != null) {
            val updates = mutableMapOf<String, Any?>()

            if (name.isNotEmpty()) updates["nom"] = name
            if (surname.isNotEmpty()) updates["prenom"] = surname
            if (age.isNotEmpty()) updates["age"] = age
            if (gender.isNotEmpty()) updates["gender"] = gender
            if (nationality.isNotEmpty()) updates["nationality"] = nationality
            if (height.isNotEmpty()) updates["height"] = height
            if (weight.isNotEmpty()) updates["weight"] = weight
            if (sport.isNotEmpty()) updates["sport"] = sport
            if (level.isNotEmpty()) updates["level"] = level
            if (team.isNotEmpty()) updates["team"] = team

            profileImageUri?.let { uri ->
                ImgurUploader.uploadToImgur(context, uri, { imageUrl ->
                    updates["profileImageUrl"] = imageUrl

                    database.child(userId).updateChildren(updates)
                        .addOnSuccessListener {
                            database.child(userId).get().addOnSuccessListener { snapshot ->
                                if (snapshot.exists()) {
                                    name = snapshot.child("nom").getValue(String::class.java) ?: name
                                    surname = snapshot.child("prenom").getValue(String::class.java) ?: surname
                                    userEmail = snapshot.child("email").getValue(String::class.java) ?: userEmail
                                    age = snapshot.child("age").getValue(String::class.java) ?: age
                                    gender = snapshot.child("gender").getValue(String::class.java) ?: gender
                                    nationality = snapshot.child("nationality").getValue(String::class.java) ?: nationality
                                    height = snapshot.child("height").getValue(String::class.java) ?: height
                                    weight = snapshot.child("weight").getValue(String::class.java) ?: weight
                                    sport = snapshot.child("sport").getValue(String::class.java) ?: sport
                                    level = snapshot.child("level").getValue(String::class.java) ?: level
                                    team = snapshot.child("team").getValue(String::class.java) ?: team
                                }
                            }
                            onBackClick()
                        }
                        .addOnFailureListener {
                            println("Erreur lors de la mise à jour du profil dans Firebase")
                        }
                }) {
                    Toast.makeText(context, "Erreur lors de l'upload de l'image sur Imgur", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                database.child(userId).updateChildren(updates)
                    .addOnSuccessListener {
                        database.child(userId).get().addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                name = snapshot.child("nom").getValue(String::class.java) ?: name
                                surname = snapshot.child("prenom").getValue(String::class.java) ?: surname
                                userEmail = snapshot.child("email").getValue(String::class.java) ?: userEmail
                                age = snapshot.child("age").getValue(String::class.java) ?: age
                                gender = snapshot.child("gender").getValue(String::class.java) ?: gender
                                nationality = snapshot.child("nationality").getValue(String::class.java) ?: nationality
                                height = snapshot.child("height").getValue(String::class.java) ?: height
                                weight = snapshot.child("weight").getValue(String::class.java) ?: weight
                                sport = snapshot.child("sport").getValue(String::class.java) ?: sport
                                level = snapshot.child("level").getValue(String::class.java) ?: level
                                team = snapshot.child("team").getValue(String::class.java) ?: team
                            }
                        }
                        onBackClick()
                    }
                    .addOnFailureListener {
                        println("Erreur lors de la mise à jour du profil dans Firebase")
                    }
            }
        }
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
            when {
                profileImageUri != null -> { // ✅ Une image locale a été sélectionnée
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUri),
                        contentDescription = "Nouvelle photo de profil",
                        modifier = Modifier.size(120.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                !profileImageUrl.isNullOrEmpty() -> { // ✅ Une image existe déjà dans Firebase
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUrl),
                        contentDescription = "Photo de profil",
                        modifier = Modifier.size(120.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> { // ❌ Aucune image, afficher du texte par défaut
                    Text("photo", color = Color.White)
                }
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
            value = surname,
            onValueChange = { surname = it },
            label = { Text("Prénom") },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEditing
        )

        OutlinedTextField(
            value = userEmail,
            onValueChange = { userEmail = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
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
            options = listOf("Homme", "Femme"),
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
            options = listOf("Natation", "Course à pied", "Vélo"),
            selectedOption = sport,
            onOptionSelected = { sport = it },
            enabled = isEditing
        )

        DropdownField(
            label = "Niveau",
            options = listOf("Débutant", "Intermédiaire", "Avancé"),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomOutlinedTextField(
    label: String,
    selectedOption: String,
    enabled: Boolean
) {
    OutlinedTextField(
        value = selectedOption,
        onValueChange = {},
        label = { Text(text = label, color = Color(0xFF433AF1)) },
        modifier = Modifier.fillMaxWidth(), // Si menuAnchor() pose problème, on peut l'enlever
        readOnly = true,
        enabled = enabled,
    )
}

fun saveProfileToDatabase(userId: String, imageUrl: String?, database: DatabaseReference, context: Context) {
    val updates = mutableMapOf<String, Any?>()
    updates["profileImageUrl"] = imageUrl

    database.child(userId).updateChildren(updates)
        .addOnSuccessListener {
            Toast.makeText(context, "Profil mis à jour !", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Erreur lors de la mise à jour du profil", Toast.LENGTH_SHORT).show()
        }
}

