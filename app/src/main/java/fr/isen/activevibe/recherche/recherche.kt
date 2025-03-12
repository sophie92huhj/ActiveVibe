package fr.isen.activevibe.recherche

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
        Column(
            modifier = Modifier.padding(16.dp).verticalScroll(scrollState)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Rechercher un utilisateur...") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true
            )
            users.filter { it.contains(searchText, ignoreCase = true) }.forEach { user ->
                Button(
                    onClick = { selectedUser = user },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    shape = RoundedCornerShape(0.dp) // Coins carrés
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween // Aligne l'icône à droite et le texte à gauche
                    ) {
                        Text(
                            user,
                            color = Color.Black,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start // Texte aligné à gauche
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward, // Icône flèche à droite
                            contentDescription = "Flèche",
                            tint = Color.Red, // Change la couleur de l'icône en rouge
                            modifier = Modifier.padding(start = 8.dp) // Espacement entre le texte et la flèche
                        )
                    }
                }
            }
        }
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
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = userProfile.nomUtilisateur, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = userProfile.nom, fontSize = 14.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "À propos de moi", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Ajouter une biographie",
                fontSize = 14.sp,
                color = Color.Gray
            )
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


/*fun AutreProfilScreen(username: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Retour",
            modifier = Modifier.size(32.dp).clickable { onBack() },
            tint = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Nom d'utilisateur : $username", fontSize = 20.sp)
    }
}
*/
