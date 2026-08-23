package dev.nario.syno.activities

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dev.nario.syno.R
import android.widget.Button
import dev.nario.syno.dialogs.ScheduleMatchDialog

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val db = Firebase.firestore

        val scheduleMatch = findViewById<Button>(R.id.btnScheduleMatch)

        scheduleMatch.setOnClickListener {
            ScheduleMatchDialog(this).show()
        }
    }
}