package fr.isen.activevibe.profil

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import fr.isen.activevibe.R
import fr.isen.activevibe.UserProfile
import fr.isen.activevibe.API.ImgurUploader

@Composable
fun App() {
    var showEditProfile by remember { mutableStateOf(false) }
    var userProfile by remember { mutableStateOf(UserProfile()) }

    if (showEditProfile) {
        EditProfilScreen(
            userProfile = userProfile,
            saveProfile = { updatedProfile ->
                userProfile = updatedProfile // ✅ Met à jour les données utilisateur
                showEditProfile = false // ✅ Retour à `ProfileScreen`
            },
            onBackClick = { showEditProfile = false } // ✅ Retour sans mise à jour
        )
    } else {
        ProfileScreen(onEditClick = { showEditProfile = true })
    }
}


@Composable
fun ProfileScreen(onEditClick: () -> Unit) {
    var userProfile by remember { mutableStateOf(UserProfile()) }  // ✅ Stockage du profil utilisateur
    var nomUtilisateur by remember { mutableStateOf("Chargement...") }

    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val database = FirebaseDatabase.getInstance().reference.child("users")

    // 🔹 Chargement des infos Firebase Realtime Database
    LaunchedEffect(userId) {
        if (userId != null) {
            database.child(userId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    nomUtilisateur = snapshot.child("nomUtilisateur").getValue(String::class.java) ?: "Non trouvé"
                    userProfile = UserProfile(
                        nomUtilisateur = nomUtilisateur,
                        nom = snapshot.child("nom").getValue(String::class.java) ?: "Nom inconnu",
                        email = snapshot.child("email").getValue(String::class.java) ?: "Email non disponible"
                    )

                    // ✅ Récupère l'URL de l'image depuis Firebase
                    profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
                }
            }.addOnFailureListener {
                nomUtilisateur = "Erreur lors du chargement"
            }
        }
    }

    val context = LocalContext.current  // ✅ Déclarer en dehors de la lambda

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
        uri?.let {
            ImgurUploader.uploadToImgur(context, imageUri = it, onSuccess = { imageUrl ->
                profileImageUrl = imageUrl
                saveProfileImageToFirebase(imageUrl)
            }, onFailure = {
                println("Erreur lors de l'upload de l'image")
            })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ✅ Alignement de la photo et du nom d'utilisateur (comme Instagram)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 🔹 Photo de profil
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.Gray, shape = CircleShape)
                    .clip(CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                when {
                    profileImageUri != null -> {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground), // ✅ Image existante
                            contentDescription = "Image de profil par défaut",
                            modifier = Modifier.size(120.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    !profileImageUrl.isNullOrEmpty() -> { // ✅ Affiche l'image depuis Firebase
                        Image(
                            painter = rememberAsyncImagePainter(profileImageUrl),
                            contentDescription = "Photo de profil",
                            modifier = Modifier.size(120.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> { // ❌ Aucune image, afficher une icône par défaut
                        Image(
                            painter = painterResource(R.drawable.profile), // Remplace par ton image par défaut
                            contentDescription = "Image de profil par défaut",
                            modifier = Modifier.size(120.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp)) // Espace entre la photo et le texte

            // 🔹 Nom d'utilisateur et nom
            Column {
                Text(text = nomUtilisateur, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        // 🔹 Bio alignée à gauche
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "À propos de moi", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Ajouter une biographie",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Bouton d'édition du profil
        Button(
            onClick = onEditClick, // ✅ Utilisation de `onEditClick` pour afficher `EditProfilScreen`
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE0E0E0),
                contentColor = Color(0xFF424242)
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, Color(0xFFB0B0B0)),
            modifier = Modifier
                .wrapContentWidth()
                .height(40.dp)
        ) {
            Text(
                text = "Éditer le profil",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔹 Statistiques du profil
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ProfileStat("15", "Posts")
            ProfileStat("1.2K", "Abonnés")
            ProfileStat("200", "Abonnements")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🔹 Grille minimaliste (3x3)
        GridPlaceholder()
    }
}




@Composable
fun ProfileStat(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = number, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun GridPlaceholder() {
    Column {
        repeat(3) {
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

fun saveProfileImageToFirebase(imageUrl: String) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId != null) {
        val database = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        database.child("profileImageUrl").setValue(imageUrl)
            .addOnSuccessListener {
                println("Image enregistrée avec succès")
            }
            .addOnFailureListener {
                println("Erreur lors de l'enregistrement de l'image")
            }
    }
}