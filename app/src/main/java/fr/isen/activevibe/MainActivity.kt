package fr.isen.activevibe

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge




class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Démarrage automatique de ProfilActivity
        val intent = Intent(this@MainActivity, ProfilActivity::class.java)
        startActivity(intent)
        //finish()  // Ferme MainActivity après avoir démarré ProfilActivity
    }
}
