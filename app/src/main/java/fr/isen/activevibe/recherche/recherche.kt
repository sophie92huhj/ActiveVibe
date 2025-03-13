package fr.isen.activevibe.recherche

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import coil.compose.rememberAsyncImagePainter
import fr.isen.activevibe.R
import fr.isen.activevibe.UserProfile
import fr.isen.activevibe.profil.GridPlaceholder
import fr.isen.activevibe.profil.ProfileStat
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import fr.isen.activevibe.Publication
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale


data class UserItem(
    val nomUtilisateur: String,
    val profileImageUrl: String? // Peut être null si l'utilisateur n'a pas mis de photo
)


@Composable
fun RechercheScreen() {
    var searchText by remember { mutableStateOf("") }
    //var users by remember { mutableStateOf(listOf<String>()) }
    var selectedUser by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    var users by remember { mutableStateOf(listOf<UserItem>()) }


    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance().getReference("users")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users = snapshot.children.mapNotNull { userSnapshot ->
                    val nomUtilisateur = userSnapshot.child("nomUtilisateur").getValue(String::class.java)
                    //var profileImageUrl = userSnapshot.child("profileImageUrl").getValue(String::class.java)
                    var profileImageUrl = userSnapshot.child("profileImageUrl").getValue(String::class.java) ?: ""

                    if (nomUtilisateur != null) {
                        UserItem(nomUtilisateur, profileImageUrl)
                    } else null
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }



    if (selectedUser == null) {
        Scaffold(
            topBar = {

                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Rechercher un utilisateur...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            content = { paddingValues ->

                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                ) {
                    users.filter { it.nomUtilisateur.contains(searchText, ignoreCase = true) }.forEach { user ->
                        Button(
                            onClick = { selectedUser = user.nomUtilisateur },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFECECEC)),
                            elevation = ButtonDefaults.elevatedButtonElevation(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Box(
                                    modifier = Modifier
                                        .size(45.dp)
                                        .padding(end = 12.dp)
                                        .border(1.dp, Color.LightGray, CircleShape)
                                        .clip(CircleShape)
                                ) {
                                    if (user.profileImageUrl != null) {
                                        Image(
                                            painter = rememberAsyncImagePainter(user.profileImageUrl),
                                            contentDescription = "Avatar",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(id = R.drawable.profile),
                                            contentDescription = "Avatar",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }



                                Text(
                                    text = user.nomUtilisateur,  // ✅ Affiche le nom d’utilisateur
                                    color = Color.Black,
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Start
                                )


                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Flèche",
                                    tint = Color.Blue,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        )
    } else {
        AutreProfilScreen(selectedUser!!) { selectedUser = null }
    }
}




@Composable
fun AutreProfilScreen(username: String, onBack: () -> Unit) {
    var userProfile by remember { mutableStateOf(UserProfile()) }
    val database = FirebaseDatabase.getInstance().reference.child("users")
    val publicationsDatabase = FirebaseDatabase.getInstance().getReference("publications")

    val userPublications = remember { mutableStateListOf<Publication>() } // Liste des publications complètes

    // 🔹 Récupérer les informations de l'utilisateur
    LaunchedEffect(username) {
        database.orderByChild("nomUtilisateur").equalTo(username).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val userSnapshot = snapshot.children.first()
                userProfile = UserProfile(
                    profileImageUri = userSnapshot.child("profileImageUrl").getValue(String::class.java) ?: "",
                    nomUtilisateur = userSnapshot.child("nomUtilisateur").getValue(String::class.java) ?: "Non trouvé",
                    nom = userSnapshot.child("nom").getValue(String::class.java) ?: "Nom inconnu"
                )
            }
        }

        // 🔹 Charger toutes les publications de cet utilisateur
        publicationsDatabase.orderByChild("nomUtilisateur").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetchedPublications = mutableListOf<Publication>()
                for (child in snapshot.children) {
                    val publication = child.getValue(Publication::class.java)
                    if (publication != null) {
                        fetchedPublications.add(publication)
                    }
                }
                userPublications.clear()
                userPublications.addAll(fetchedPublications.sortedByDescending { it.timestamp ?: 0 })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AutreProfilScreen", "Erreur chargement publications : ${error.message}")
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 🔙 Icône de retour
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Retour",
            modifier = Modifier
                .size(32.dp)
                .clickable { onBack() },
            tint = Color.Black
        )

        // 🖼️ Profil utilisateur (photo et nom)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
            ) {
                if (!userProfile.profileImageUri.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(userProfile.profileImageUri),
                        contentDescription = "Photo de profil",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.profile),
                        contentDescription = "Photo de profil",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = userProfile.nomUtilisateur, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "À propos de moi", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(15.dp))

        // 📊 Statistiques
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ProfileStat(userPublications.size.toString(), "Posts")
            ProfileStat("673", "Abonnés")
            ProfileStat("710", "Abonnements")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 📌 Affichage des publications sous forme de liste complète
        UserPublicationsList(userPublications)
    }
}

@Composable
fun UserPublicationsList(publications: List<Publication>) {
    if (publications.isEmpty()) {
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
            items(publications) { publication ->
                PublicationCardWithoutDelete(publication)
            }
        }
    }
}

@Composable
fun PublicationCardWithoutDelete(publication: Publication) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.background(Color.White)) {
            // ✅ Affichage du sport en bannière
            publication.sportType?.takeIf { it.isNotEmpty() }?.let { sport ->
                Box(
                    modifier = Modifier
                        .background(Color(0xff433af1), shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
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

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Afficher l'image uniquement si elle existe
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
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ✅ Ajout d'un fond coloré pour les posts sans image (optionnel)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White) // 🌟 Fond violet clair pour un meilleur rendu
                    .padding(12.dp)
            ) {
                Column {
                    // ✅ Description
                    Text(
                        text = publication.description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // ✅ Affichage des champs optionnels
                    publication.duration?.takeIf { it.isNotEmpty() }?.let { duration ->
                        Text(
                            text = "Durée: $duration min",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    publication.distance?.takeIf { it.isNotEmpty() }?.let { distance ->
                        Text(
                            text = "Distance: $distance km",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    publication.speed?.takeIf { it.isNotEmpty() }?.let { speed ->
                        Text(
                            text = "Vitesse: $speed km/h",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
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
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}