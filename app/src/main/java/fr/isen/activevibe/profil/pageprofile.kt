package fr.isen.activevibe.profil

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.isen.activevibe.API.ImgurUploader
import fr.isen.activevibe.Publication
import fr.isen.activevibe.R
import fr.isen.activevibe.UserProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Context
import android.widget.Toast
import com.google.firebase.database.DatabaseReference

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
    val userPublications = remember { mutableStateListOf<Publication>() }
    // 🔹 Chargement des infos Firebase Realtime Database
    LaunchedEffect(userId) {
        database.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetchedPublications = mutableListOf<Publication>()
                for (child in snapshot.children) {
                    val pub = child.getValue(Publication::class.java)
                    if (pub != null) {
                        fetchedPublications.add(pub)
                    }
                }
                userPublications.clear()
                userPublications.addAll(fetchedPublications)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileScreen", "Erreur chargement publications : ${error.message}")
            }
        })
    }

    LaunchedEffect(userId) {
        userId?.let {
            database.child(it).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    nomUtilisateur = snapshot.child("nomUtilisateur").getValue(String::class.java) ?: "Utilisateur inconnu"
                    profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java) ?: "" // ✅ Récupération de l'image
                } else {
                    nomUtilisateur = "Utilisateur introuvable"
                }
            }.addOnFailureListener {
                nomUtilisateur = "Erreur chargement"
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

// 🔹 Afficher les publications de l'utilisateur
        userId?.let { UserPublications(it) }
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

@Composable
fun UserPublications(userId: String) {
    val database = FirebaseDatabase.getInstance().getReference("publications")
    val userDatabase = FirebaseDatabase.getInstance().getReference("users").child(userId)

    val userPublications = remember { mutableStateListOf<Publication>() }
    var nomUtilisateur by remember { mutableStateOf<String?>(null) }

    // 🔹 Étape 1 : Récupérer le nom d'utilisateur de l'utilisateur connecté
    LaunchedEffect(userId) {
        userDatabase.child("nomUtilisateur").get().addOnSuccessListener { snapshot ->
            val fetchedNomUtilisateur = snapshot.getValue(String::class.java)
            if (fetchedNomUtilisateur != null) {
                nomUtilisateur = fetchedNomUtilisateur
                Log.d("UserPublications", "Nom utilisateur récupéré : $nomUtilisateur")

                // 🔹 Étape 2 : Récupérer les publications de cet utilisateur et les trier
                database.orderByChild("timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val fetchedPublications = mutableListOf<Publication>()

                        Log.d("UserPublications", "Nombre de publications trouvées : ${snapshot.childrenCount}")

                        for (child in snapshot.children) {
                            val pub = child.getValue(Publication::class.java)
                            if (pub != null && pub.nomUtilisateur == nomUtilisateur) {
                                fetchedPublications.add(pub)
                            }
                        }

                        // ✅ Trier par ordre décroissant (plus récent en premier)
                        userPublications.clear()
                        userPublications.addAll(fetchedPublications.sortedByDescending { it.timestamp ?: 0 })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("UserPublications", "Erreur chargement publications : ${error.message}")
                    }
                })
            } else {
                Log.e("UserPublications", "Nom utilisateur non trouvé")
            }
        }.addOnFailureListener {
            Log.e("UserPublications", "Erreur récupération nom utilisateur")
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Mes publications",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(12.dp)
        )

        if (userPublications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Aucune publication trouvée", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(userPublications) { publication ->
                    PublicationCard(publication)
                }
            }
        }
    }
}

@Composable
fun PublicationCard(publication: Publication) {
    val database = FirebaseDatabase.getInstance().getReference("publications")
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.background(Color.White)) {
            // ✅ Image de la publication
            Box(modifier = Modifier.fillMaxWidth()) {
                publication.imageUrl?.takeIf { it.isNotEmpty() }?.let { imageUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Image de la publication",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // ✅ Affichage du sport en bannière
                publication.sportType?.takeIf { it.isNotEmpty() }?.let { sport ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(Color(0xff433af1), shape = RoundedCornerShape(bottomEnd = 8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = sport,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Description
            Text(
                text = publication.description,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )

            // ✅ Affichage des champs optionnels
            publication.duration?.takeIf { it.isNotEmpty() }?.let { duration ->
                Text(
                    text = "Durée: $duration min",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            publication.distance?.takeIf { it.isNotEmpty() }?.let { distance ->
                Text(
                    text = "Distance: $distance km",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            publication.speed?.takeIf { it.isNotEmpty() }?.let { speed ->
                Text(
                    text = "Vitesse: $speed km/h",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            publication.timestamp?.let { timestamp ->
                val date = Date(timestamp)
                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val formattedDate = formatter.format(date)
                Text(
                    text = "Publié le : $formattedDate",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Bouton Supprimer (Affiché uniquement pour l'utilisateur qui a posté)
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            //if (userId != null && publication.nomUtilisateur == userId) {
                Button(
                    onClick = {
                        deletePublication(database, publication.id, context)
                    },
                    colors = ButtonDefaults.buttonColors(Color.Red),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Supprimer", color = Color.White)
                }
            }
        }
    }


fun deletePublication(database: DatabaseReference, publicationId: String, context: Context) {
    if (publicationId.isNotEmpty()) {
        database.child(publicationId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Publication supprimée", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show()
            }
    }
}