package fr.isen.activevibe.recherche

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*
import androidx.compose.ui.Alignment

@Composable
fun RechercheScreen() {
    var searchText by remember { mutableStateOf("") }
    var users by remember { mutableStateOf(listOf<String>()) }
    var selectedUser by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance().getReference("users")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users = snapshot.children.mapNotNull { it.child("nomUtilisateur").getValue(String::class.java) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    if (selectedUser == null) {
        Column(
            modifier = Modifier.padding(16.dp).verticalScroll(scrollState)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Rechercher un utilisateur...") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true
            )
            users.filter { it.contains(searchText, ignoreCase = true) }.forEach { user ->
                Button(
                    onClick = { selectedUser = user },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    shape = RoundedCornerShape(0.dp) // Coins carrés
                ) {
                    Text(
                        user,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth() // Texte prend toute la largeur
                            //.align(Alignment.Start), // Alignement gauche
                       // textAlign = TextAlign.Start // Texte bien calé à gauche
                    )
                }
            }
        }
    } else {
        AutreProfilScreen(selectedUser!!) { selectedUser = null }
    }
}


@Composable
fun AutreProfilScreen(username: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Retour",
            modifier = Modifier.size(32.dp).clickable { onBack() },
            tint = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Nom d'utilisateur : $username", fontSize = 20.sp)
    }
}

