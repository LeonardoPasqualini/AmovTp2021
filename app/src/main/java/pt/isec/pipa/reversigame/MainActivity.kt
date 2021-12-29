package pt.isec.pipa.reversigame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun novoJogo(view: android.view.View) {
        val intent = Intent(this, ModoJogoActivity::class.java)
        startActivity(intent)
    }

    fun perfil(view: android.view.View) {
        val intent = Intent(this, PerfilActivity::class.java)
        startActivity(intent)
    }

    fun creditos(view: android.view.View) {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }

    fun topScore(view: android.view.View) {
        val intent = Intent(this, TopScoreActivity::class.java)
        startActivity(intent)
    }

}