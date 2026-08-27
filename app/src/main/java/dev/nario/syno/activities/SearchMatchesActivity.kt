package dev.nario.syno.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import dev.nario.syno.BaseActivity
import dev.nario.syno.R

class SearchMatchesActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search_matches)

        val backBtn = findViewById<ImageButton>(R.id.btnBack)
        val homeBtn = findViewById<LinearLayout>(R.id.navHome)
        val searchPlayersBtn = findViewById<LinearLayout>(R.id.navPlayers)

        backBtn.setOnClickListener {
            finish()
        }

        homeBtn.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        searchPlayersBtn.setOnClickListener {
            startActivity(Intent(this, SearchPlayersActivity::class.java))
        }
    }
}