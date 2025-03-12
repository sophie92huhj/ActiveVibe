package fr.isen.activevibe.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import fr.isen.activevibe.likes.LikeScreen
import fr.isen.activevibe.publications.FeedScreen
import fr.isen.activevibe.publications.PublicationScreen
import fr.isen.activevibe.profil.ProfileScreen
import fr.isen.activevibe.recherche.RechercheScreen


@Composable
fun HomeScreen() {
    FeedScreen()
}

@Composable
fun SearchScreen() {
    RechercheScreen()
}

@Composable
fun AddPostScreen() {
    PublicationScreen()
}

@Composable
fun LikedPostsScreen(navController: NavController) {
    LikeScreen(navController)
}

@Composable
fun ProfileScreen1(onEditClick: () -> Unit) {
    ProfileScreen(onEditClick = onEditClick)
}

@Composable
fun MessagesScreen() {
    Text("Page: Messages", fontSize = 24.sp)
}
