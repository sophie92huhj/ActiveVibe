package fr.isen.activevibe

import android.os.Bundle
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import fr.isen.activevibe.connexion.AlreadyConnectedScreen
import fr.isen.activevibe.connexion.FirstTimeScreen
import fr.isen.activevibe.connexion.ForgotPasswordScreen


class ConnexionActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // ✅ Variable pour stocker la mise à jour de l'écran
    private var screenUpdater: ((String) -> Unit)? = null

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
            var screen by remember { mutableStateOf("login") }

            when (screen) {
                "login" -> CustomAuthScreen(
                    onEmailLoginClicked = { email, password -> registerOrLoginWithEmail(email, password) { screen = it } },
                    onGoogleLoginClicked = { signInWithGoogle() },
                    onResetPasswordClicked = { screen = "forgotPassword" } // ✅ Affiche l'écran de mot de passe oublié
                )
                "forgotPassword" -> ForgotPasswordScreen(
                    onBackToLogin = { screen = "login" } // ✅ Retour à la connexion
                )
                "firstTime" -> FirstTimeScreen()
                "alreadyConnected" -> AlreadyConnectedScreen()
            }
        }

    }

    /** ✅ Vérifier si l'utilisateur existe et le rediriger **/
    private fun registerOrLoginWithEmail(email: String, password: String, updateScreen: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Bienvenue ${auth.currentUser?.email}", Toast.LENGTH_SHORT).show()
                    updateScreen("alreadyConnected") // ✅ Mise à jour directe de `screen`
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { registerTask ->
                            if (registerTask.isSuccessful) {
                                Toast.makeText(this, "Compte créé : ${auth.currentUser?.email}", Toast.LENGTH_SHORT).show()
                                updateScreen("firstTime") // ✅ Mise à jour directe de `screen`
                            } else {
                                Toast.makeText(this, "Erreur : ${registerTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
    }




    /** ✅ Connexion avec Google et redirection **/
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Bienvenue ${auth.currentUser?.email}", Toast.LENGTH_SHORT).show()
                        val isNewUser = task.result.additionalUserInfo?.isNewUser ?: false
                        screenUpdater?.invoke(if (isNewUser) "firstTime" else "alreadyConnected") // ✅ Correction ici
                    } else {
                        Toast.makeText(this, "Erreur connexion Google", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Échec connexion Google", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetPassword(email: String) {
        if (email.isNotEmpty()) {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Email de réinitialisation envoyé.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Erreur : ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Veuillez entrer votre email.", Toast.LENGTH_SHORT).show()
        }
    }


}


@Composable
fun CustomAuthScreen(
    onEmailLoginClicked: (String, String) -> Unit,
    onGoogleLoginClicked: () -> Unit,
    onResetPasswordClicked: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) } // 🔹 Gère l'affichage du mot de passe

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
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // ✅ Logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo ActiveVibe",
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = 8.dp)
        )

        // ✅ Texte "Connexion"
        Text(
            text = "Connexion / Inscription",
            fontSize = 26.sp,
            color = Color(0xFF433AF1),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 🔹 Champ de mot de passe avec visibilité activable
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Masquer le mot de passe" else "Afficher le mot de passe"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Bouton Connexion Email
        Button(
            onClick = { onEmailLoginClicked(email, password) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF433AF1)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            Text(text = "Se connecter avec une adresse e-mail", color = Color.White)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ✅ Bouton Connexion Google
        Button(
            onClick = { onGoogleLoginClicked() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A5A5A)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            Text(text = "Se connecter avec Google", color = Color.White)
        }

        // ✅ Bouton "Mot de passe oublié ?"
        TextButton(
            onClick = { onResetPasswordClicked(email) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(text = "Mot de passe oublié ?", color = Color(0xFF433AF1))
        }
    }
}

