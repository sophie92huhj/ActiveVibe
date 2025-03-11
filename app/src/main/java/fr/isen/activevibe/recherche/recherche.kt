package fr.isen.activevibe.recherche

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*

@Composable
fun RechercheScreen() {
    var searchText by remember { mutableStateOf("") }
    var users by remember { mutableStateOf(listOf<String>()) }

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

        // ✅ Affichage des utilisateurs filtrés
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
            .padding(16.dp),
        singleLine = true
    )
}

@Composable
fun UserList(users: List<String>, searchText: String) {
    val filteredUsers = users.filter { it.contains(searchText, ignoreCase = true) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(filteredUsers) { user ->
            Text(text = user, modifier = Modifier.padding(8.dp))
        }
    }
}
