package fr.isen.activevibe.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import fr.isen.activevibe.publications.FeedScreen
import fr.isen.activevibe.publications.PublicationScreen
import fr.isen.activevibe.profil.ProfileScreen


@Composable
fun HomeScreen() {
    FeedScreen()
}

@Composable
fun SearchScreen() {
    Text("Page: Recherche", fontSize = 24.sp)
}

@Composable
fun AddPostScreen() {
    PublicationScreen()
}

@Composable
fun LikedPostsScreen() {
    Text("Page: Posts Likés", fontSize = 24.sp)
}

@Composable
fun ProfileScreen1() {
   ProfileScreen()
}

@Composable
fun MessagesScreen() {
    Text("Page: Messages", fontSize = 24.sp)
}
