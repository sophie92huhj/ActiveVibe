package fr.isen.activevibe.recherche

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import fr.isen.activevibe.R
import fr.isen.activevibe.UserProfile
import fr.isen.activevibe.profil.GridPlaceholder
import fr.isen.activevibe.profil.ProfileStat


@Composable
fun RechercheScreen() {
    var searchText by remember { mutableStateOf("") }
    var users by remember { mutableStateOf(listOf<String>()) }
    var selectedUser by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance().getReference("users")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users = snapshot.children.mapNotNull { it.child("nomUtilisateur").getValue(String::class.java) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    if (selectedUser == null) {
        Scaffold(
            topBar = {
                // Barre de recherche fixe en haut
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Rechercher un utilisateur...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp), // Espacement autour de la barre
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            content = { paddingValues ->
                // Liste des utilisateurs qui peut défiler
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                ) {
                    users.filter { it.contains(searchText, ignoreCase = true) }.forEach { user ->
                        Button(
                            onClick = { selectedUser = user },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp) // Espacement vertical plus important entre les boutons
                                .height(48.dp), // Hauteur des boutons un peu plus grande
                            shape = RoundedCornerShape(16.dp), // Coins arrondis pour les boutons
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFECECEC)), // Couleur de fond plus douce
                            elevation = ButtonDefaults.elevatedButtonElevation(4.dp) // Légère élévation pour un effet de profondeur
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Box(
                                    modifier = Modifier
                                        .size(45.dp) // Taille du cercle
                                        .padding(end = 12.dp) // Espacement avec le texte
                                        .border(1.dp, Color.LightGray, CircleShape) // Bordure noire fine
                                        .clip(CircleShape) // Pour arrondir l'image dans un cercle
                                ) {
                                    // Image PNG à l'intérieur du cercle
                                    Image(
                                        painter = painterResource(id = R.drawable.profile), // Remplacez ic_profile par le nom de votre image
                                        contentDescription = "Avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        //contentScale = ContentScale.Crop // Coupe l'image pour qu'elle remplisse le cercle
                                    )
                                }
                                // Texte à gauche
                                Text(
                                    text = user,
                                    color = Color.Black,
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Start
                                )

                                // Icône à droite
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Flèche",
                                    tint = Color.Blue, // Icône plus discrète
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

    LaunchedEffect(username) {
        database.orderByChild("nomUtilisateur").equalTo(username).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val userSnapshot = snapshot.children.first()
                userProfile = UserProfile(
                    nomUtilisateur = userSnapshot.child("nomUtilisateur").getValue(String::class.java) ?: "Non trouvé",
                    nom = userSnapshot.child("nom").getValue(String::class.java) ?: "Nom inconnu",
                    email = userSnapshot.child("email").getValue(String::class.java) ?: "Email non disponible"
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Retour",
            modifier = Modifier.size(32.dp).clickable { onBack() },
            tint = Color.Black
        )
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
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Photo de profil",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = userProfile.nomUtilisateur, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                //Text(text = userProfile.nom, fontSize = 14.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "À propos de moi", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            /*Text(
                text = "Ajouter une biographie",
                fontSize = 14.sp,
                color = Color.Gray
            )*/
        }
        Spacer(modifier = Modifier.height(15.dp))

        // 🔹 Statistiques du profil
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ProfileStat("9", "Posts")
            ProfileStat("673", "Abonnés")
            ProfileStat("710", "Abonnements")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🔹 Grille minimaliste (3x3)
        GridPlaceholder()
    }
    }



