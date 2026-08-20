package dev.nario.syno

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import dev.nario.syno.activities.LoginActivity
import dev.nario.syno.activities.RegistrationActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }
}
