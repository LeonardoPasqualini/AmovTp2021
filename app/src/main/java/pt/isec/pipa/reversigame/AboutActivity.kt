package pt.isec.pipa.reversigame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
    }

    fun creditos(view: android.view.View) {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }
}