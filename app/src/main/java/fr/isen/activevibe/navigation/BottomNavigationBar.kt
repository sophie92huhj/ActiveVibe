package fr.isen.activevibe.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun BottomNavigationBar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    val items = listOf("Fil d'actualité", "Recherche", "Ajouter", "Posts Likés", "Profil")
    val icons = listOf(
        Icons.Filled.Home,
        Icons.Filled.Search,
        Icons.Filled.Add,
        Icons.Filled.Favorite,
        Icons.Filled.Person
    )

    NavigationBar {
        items.forEachIndexed { index, _ ->
            if (index == 2) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingActionButton(onClick = { onItemSelected(index) }) {
                        Icon(Icons.Filled.Add, contentDescription = "Ajouter")
                    }
                }
            } else {
                NavigationBarItem(
                    icon = { Icon(icons[index], contentDescription = null) },
                    selected = selectedItem == index,
                    onClick = { onItemSelected(index) }
                )
            }
        }
    }
}
