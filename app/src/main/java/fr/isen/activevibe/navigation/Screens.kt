package fr.isen.activevibe.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen() {
    Text("Page: Fil d'actualité", fontSize = 24.sp)
}

@Composable
fun SearchScreen() {
    Text("Page: Recherche", fontSize = 24.sp)
}

@Composable
fun AddPostScreen() {
    Text("Page: Ajouter un post", fontSize = 24.sp)
}

@Composable
fun LikedPostsScreen() {
    Text("Page: Posts Likés", fontSize = 24.sp)
}

@Composable
fun ProfileScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Page: Profil", fontSize = 24.sp)
    }
}

@Composable
fun MessagesScreen() {
    Text("Page: Messages", fontSize = 24.sp)
}
