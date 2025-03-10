package fr.isen.activevibe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Initialisation de Firebase Auth
        auth = Firebase.auth

        // ✅ Configuration Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            CustomAuthScreen(
                onEmailLoginClicked = { email, password -> loginWithEmail(email, password) },
                onGoogleLoginClicked = { signInWithGoogle() }
            )
        }
    }

    /** ✅ Connexion avec Email et Mot de Passe **/
    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Bienvenue ${user?.email}", Toast.LENGTH_SHORT).show()
                    Log.d("Firebase", "Connexion réussie : ${user?.email}")
                } else {
                    Toast.makeText(this, "Échec de connexion : ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Firebase", "Erreur connexion : ${task.exception?.message}")
                }
            }
    }

    /** ✅ Lancer Google Sign-In **/
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    /** ✅ Récupérer le Résultat Google Sign-In **/
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        Toast.makeText(this, "Bienvenue ${user?.email}", Toast.LENGTH_SHORT).show()
                        Log.d("Firebase", "Connexion Google réussie : ${user?.email}")
                    } else {
                        Toast.makeText(this, "Erreur connexion Google", Toast.LENGTH_SHORT).show()
                        Log.e("Firebase", "Erreur Google : ${task.exception?.message}")
                    }
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Échec connexion Google", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun CustomAuthScreen(
    onEmailLoginClicked: (String, String) -> Unit,
    onGoogleLoginClicked: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ✅ Texte "Bienvenue sur" au-dessus du logo
        Text(
            text = "Bienvenue sur",
            fontSize = 22.sp,
            color = Color(0xFF433AF1),
            modifier = Modifier.padding(bottom = 8.dp) // Ajout d’un espacement avant le logo
        )

        // ✅ Logo plus grand
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo ActiveVibe",
            modifier = Modifier
                .size(250.dp) // Taille du logo ajustée
                .padding(bottom = 8.dp) // Espacement après le logo
        )

        // ✅ Texte "Connexion" sous le logo
        Text(
            text = "Connexion",
            fontSize = 26.sp,
            color = Color(0xFF433AF1),
            modifier = Modifier.padding(bottom = 16.dp) // Espacement avant les champs
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Bouton Connexion Email avec couleur #433AF1
        Button(
            onClick = { onEmailLoginClicked(email, password) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF433AF1)), // Couleur mise à jour
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            Text(text = "Se connecter avec une adresse e-mail", color = Color.White)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ✅ Bouton Connexion Google avec un gris foncé
        Button(
            onClick = { onGoogleLoginClicked() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A5A5A)), // Gris foncé pour contraster
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            Text(text = "Se connecter avec Google", color = Color.White)
        }
    }
}

