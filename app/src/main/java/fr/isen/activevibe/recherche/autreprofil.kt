
package fr.isen.activevibe.recherche

import android.os.Bundle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import fr.isen.activevibe.R
import fr.isen.activevibe.UserProfile


/*


@Composable
fun AutreProfilScreen(username: String) {
    // Variable pour stocker les données de l'utilisateur
    var userProfile by remember { mutableStateOf(UserProfile()) }
    val database = FirebaseDatabase.getInstance().reference.child("users")

    // Charger les données Firebase pour l'utilisateur
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
        // ✅ Ajout d'un `Row` pour aligner la photo et le nom d'utilisateur (comme Instagram)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 🔹 Photo de profil (Utilise une image dynamique si disponible)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
            ) {
                // Si une image de profil est disponible, utilise-la, sinon utilise une image par défaut
                Image(
                    painter = painterResource(id = R.drawable.profile), // Tu peux changer l'ID ici si tu récupères l'image de Firebase
                    contentDescription = "Photo de profil",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(16.dp)) // Espace entre la photo et le texte

            // 🔹 Nom d'utilisateur et nom
            Column {
                Text(text = userProfile.nomUtilisateur, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = userProfile.nom, fontSize = 14.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        // 🔹 Bio alignée à gauche
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "À propos de moi", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Ajouter une biographie", // Si tu as une bio, tu peux la récupérer de Firebase aussi
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Statistiques du profil
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ProfileStat("15", "Posts") // À remplacer avec les données de l'utilisateur
            ProfileStat("1.2K", "Abonnés") // À remplacer avec les données de l'utilisateur
            ProfileStat("200", "Abonnements") // À remplacer avec les données de l'utilisateur
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
*/