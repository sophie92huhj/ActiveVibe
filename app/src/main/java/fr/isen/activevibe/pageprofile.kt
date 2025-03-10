package fr.isen.activevibe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun App() {
    var showEditProfile by remember { mutableStateOf(false) }

    if (showEditProfile) {
        EditProfilScreen { showEditProfile = false }
    } else {
        ProfileScreen { showEditProfile = true }
    }
}

@Composable
fun ProfileScreen(onEditClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Image de profil (cercle noir avec bords fins)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .border(2.dp, Color.Black, CircleShape) // Bord fin noir
                    .background(Color.Transparent, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Nom d'Utilisateur", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "📍 Paris, France", fontSize = 13.sp, color = Color.Gray)
            }
        }


        Spacer(modifier = Modifier.height(25.dp))

        // Bio alignée à gauche
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "À propos de moi", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Ajouter une biographie",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(10.dp))


        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onEditClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE0E0E0), // Fond doux gris clair pour l'élégance
                contentColor = Color(0xFF424242) // Texte sombre mais subtil
            ),
            shape = RoundedCornerShape(12.dp), // Bordures arrondies mais douces
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp), // Ombre légère pour du relief
            border = BorderStroke(1.dp, Color(0xFFB0B0B0)), // Bordure fine et discrète
            modifier = Modifier
                .wrapContentWidth()
                .height(40.dp) // Taille plus petite mais toujours équilibrée
        ) {
            Text(
                text = "Éditer le profil",
                fontSize = 14.sp, // Taille de texte modérée pour garder un style raffiné
                fontWeight = FontWeight.Medium, // Poids du texte équilibré
                letterSpacing = 0.5.sp // Espacement léger des lettres
            )
        }



        Spacer(modifier = Modifier.height(20.dp))

        // Statistiques du profil
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ProfileStat("15", "Posts")
            ProfileStat("1.2K", "Abonnés")
            ProfileStat("200", "Abonnements")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Grille minimaliste (3x3)
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

@Composable
fun EditProfilScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Édition du Profil", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(text = "Retour")
        }
    }
}



