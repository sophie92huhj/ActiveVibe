package fr.isen.activevibe.recherche

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*

@Composable
fun RechercheScreen() {
    var searchText by remember { mutableStateOf("") }
    var users by remember { mutableStateOf(listOf<String>()) }

    // Récupération des utilisateurs depuis Firebase
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // ✅ Barre de recherche
        SearchBar(searchText) { searchText = it }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Affichage des utilisateurs avec un joli design
        UserList(users = users, searchText = searchText)
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
fun UserList(users: List<String>, searchText: String) {
    val filteredUsers = users.filter { it.contains(searchText, ignoreCase = true) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(filteredUsers) { user ->
            UserCard(username = user)
        }
    }
}

@Composable
fun UserCard(username: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ Rond avec une bordure fine bleue
            Box(
                modifier = Modifier
                    .size(48.dp) // Taille du rond
                    .clip(CircleShape) // Crée un cercle
                    .border(2.dp, Color(0xFF433AF1), CircleShape) // Bordure fine bleue
            )

            Spacer(modifier = Modifier.width(12.dp))

            // ✅ Nom de l'utilisateur
            Text(
                text = username,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF433AF1)
            )
        }
    }
}