package fr.isen.activevibe.likes

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import fr.isen.activevibe.Publication
import fr.isen.activevibe.publications.PublicationCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikeScreen(navController: NavController, modifier: Modifier = Modifier) {
    val database = FirebaseDatabase.getInstance().getReference("publications")
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val userLikesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("likes")
    val likedPublications = remember { mutableStateListOf<Publication>() }

    // Charger les publications likées
    LaunchedEffect(Unit) {
        userLikesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val likedIds = snapshot.children.map { it.key!! }
                if (likedIds.isEmpty()) return

                database.orderByKey().addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val fetchedPublications = mutableListOf<Publication>()
                        for (child in snapshot.children) {
                            val pub = child.getValue(Publication::class.java)
                            if (pub != null && likedIds.contains(pub.id)) {
                                fetchedPublications.add(pub)
                            }
                        }
                        likedPublications.clear()
                        likedPublications.addAll(fetchedPublications)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("LikeScreen", "Erreur chargement likes: ${error.message}")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("LikeScreen", "Erreur récupération des likes: ${error.message}")
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Publications Likées",
                        color = Color(0xFF433AF1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(start = 8.dp) // 🔹 Espacement léger
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (likedPublications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Aucune publication likée", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(likedPublications) { publication ->
                        PublicationCard(publication)
                    }
                }
            }
        }
    }
}
