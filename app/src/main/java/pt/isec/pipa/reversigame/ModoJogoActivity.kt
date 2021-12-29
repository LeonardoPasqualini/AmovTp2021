package pt.isec.pipa.reversigame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class ModoJogoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modo_jogo)
    }

    fun modo1(view: android.view.View) {
        val intent = Intent(this, Jogos1_2Activity::class.java)
        startActivity(intent)
    }

    fun modo2(view: android.view.View) {
        val intent = Intent(this, Jogos1_2Activity::class.java)
        startActivity(intent)
    }

    fun modo3(view: android.view.View) {
        val intent = Intent(this, Jogo3Activity::class.java)
        startActivity(intent)
    }

}