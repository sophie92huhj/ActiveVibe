package fr.isen.activevibe.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import fr.isen.activevibe.R

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
@Composable
fun TopBar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.activevibe), // Ton logo
                    contentDescription = "Logo ActiveVibe",
                    modifier = Modifier
                        .size(64.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    "Active Vibe",
                    color = Color(0xFF433AF1),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        actions = {
            if (selectedItem == 0) {
                IconButton(
                    onClick = { onItemSelected(5) },
                    modifier = Modifier
                        .offset(y = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Envoyer",
                        modifier = Modifier.size(30.dp),
                        tint = Color.Black
                    )
                }
            }
        },
        modifier = Modifier.background(Color.White) // Fond blanc comme sur l'image
    )
}