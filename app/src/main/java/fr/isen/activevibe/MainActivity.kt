package fr.isen.activevibe

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Lancer ConnexionActivity
        val intent = Intent(this, ConnexionActivity::class.java)
        startActivity(intent)
        finish() // ✅ Ferme MainActivity après le démarrage de ConnexionActivity
    }
}
