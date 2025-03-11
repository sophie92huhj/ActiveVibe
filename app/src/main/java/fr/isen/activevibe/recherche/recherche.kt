package fr.isen.activevibe.recherche

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*

@Composable
fun RechercheScreen() {
    var searchText by remember { mutableStateOf("") }
    var users by remember { mutableStateOf(listOf<String>()) }
    var selectedUser by remember { mutableStateOf<String?>(null) } // Garde une variable pour l'utilisateur sélectionné

    // Récupérer les utilisateurs depuis Firebase
    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance().getReference("users")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = snapshot.children.mapNotNull { it.child("nomUtilisateur").getValue(String::class.java) }
                users = userList
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Afficher le profil lorsque l'utilisateur est sélectionné
    if (selectedUser != null) {
        AutreProfilScreen(username = selectedUser ?: "")
    }

    // Interface de recherche et des utilisateurs
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Barre de recherche
        SearchBar(searchText) { searchText = it }

        Spacer(modifier = Modifier.height(16.dp))

        // Affichage des utilisateurs avec un joli design
        UserList(users = users, searchText = searchText) { user ->
            selectedUser = user // Lors du clic, sélectionner l'utilisateur
        }
    }
}

@Composable
fun SearchBar(searchText: String, onSearch: (String) -> Unit) {
    OutlinedTextField(
        value = searchText,
        onValueChange = { onSearch(it) },
        label = { Text("Rechercher un utilisateur...") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        singleLine = true
    )
}

@Composable
fun UserList(users: List<String>, searchText: String, onClickUser: (String) -> Unit) {
    val filteredUsers = users.filter { it.contains(searchText, ignoreCase = true) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(filteredUsers) { user ->
            UserCard(username = user, onClick = onClickUser)
        }
    }
}

@Composable
fun UserCard(username: String, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .clickable { onClick(username) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFF433AF1), CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = username,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF433AF1)
            )

            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Voir le profil",
                tint = Color(0xFF433AF1)
            )
        }
    }
}

@Composable
fun AutreProfilScreen(username: String) {
    // Affichage du profil de l'utilisateur sélectionné
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Profil de $username", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        // Ajoute le reste de la UI pour afficher le profil
    }
}