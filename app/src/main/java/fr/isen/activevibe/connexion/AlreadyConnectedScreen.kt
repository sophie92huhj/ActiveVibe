package fr.isen.activevibe.connexion

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.activevibe.MainActivity

@Composable
fun AlreadyConnectedScreen() {
    val context = LocalContext.current

    // ✅ Démarre `MainActivity` dès l'affichage de cet écran
    LaunchedEffect(Unit) {
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
    }

    // ✅ Affichage temporaire pendant la redirection
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Connexion réussie, redirection...",
            fontSize = 20.sp,
            color = Color(0xFF433AF1)
        )
        Spacer(modifier = Modifier.height(16.dp))
        CircularProgressIndicator(color = Color(0xFF433AF1)) // ✅ Animation de chargement
    }
}
