package dev.nario.syno.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dev.nario.syno.R
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.nario.syno.BaseActivity
import dev.nario.syno.adapters.MatchAdapter
import dev.nario.syno.dialogs.ScheduleMatchDialog
import dev.nario.syno.entities.Match

class HomeActivity : BaseActivity() {
    val TAG = "HomeActivity"
    val db = Firebase.firestore
    private lateinit var matchAdapter: MatchAdapter
    private lateinit var matchesMessageTitle: TextView
    private lateinit var matchesMessageDescription: TextView
    private lateinit var matchesMessageLogo: TextView
    val matches = ArrayList<Match>()
    private lateinit var matchesListRv: RecyclerView
    private lateinit var emptyMatchesMessage: LinearLayout
    private lateinit var matchesLoadingProgressBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        matchesListRv = findViewById(R.id.rvMatches)
        emptyMatchesMessage = findViewById(R.id.emptyMatches)
        matchesLoadingProgressBar = findViewById(R.id.matchesProgressBar)

        matchAdapter = MatchAdapter(emptyList()) {
            match ->
            //TODO: open match details
        }
        matchesListRv.adapter = matchAdapter
        matchesListRv.layoutManager = LinearLayoutManager(this)

        matchesMessageTitle = findViewById<TextView>(R.id.matchesDefaultMessageTitle)
        matchesMessageDescription = findViewById<TextView>(R.id.matchesDefaultMessageDescription)
        matchesMessageLogo = findViewById<TextView>(R.id.matchesDefaultMessageLogo)

        val scheduleMatch = findViewById<Button>(R.id.btnScheduleMatch)

        loadMatches()

        scheduleMatch.setOnClickListener {
            ScheduleMatchDialog(this).show()
        }
    }

    fun addMatchToList(match: Match) {
        matches.add(match)
        matchAdapter.updateMatches(matches)
    }

    private fun loadMatches() {
        db.collection("matches")
            .whereEqualTo("creatorId", currentUserId)
            .get()
            .addOnSuccessListener { matchesRequest ->
                for (match in matchesRequest) {
                    matches.add(match.toObject(Match::class.java))
                }

                if (matches.size != 0) {
                    matchesListRv.visibility = View.VISIBLE
                } else {
                    emptyMatchesMessage.visibility = View.GONE
                }

                matchesLoadingProgressBar.visibility = View.GONE

                matchAdapter.updateMatches(matches)
            }
            .addOnFailureListener {
                matchesMessageTitle.text = "Falha ao buscar partidas"
                matchesMessageDescription.text = "Verifique sua conexão com a internet e reinicie o App"
                matchesMessageLogo.visibility = View.GONE
            }

    }
}