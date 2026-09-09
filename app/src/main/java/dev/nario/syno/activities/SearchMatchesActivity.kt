package dev.nario.syno.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dev.nario.syno.BaseActivity
import dev.nario.syno.R
import dev.nario.syno.adapters.MatchAdapter
import dev.nario.syno.dialogs.MatchDetailsDialog
import dev.nario.syno.entities.Match

class SearchMatchesActivity : BaseActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var matchAdapter: MatchAdapter
    private val matches = ArrayList<Match>()
    private lateinit var matchesListRv: RecyclerView
    private lateinit var matchesNotFound: LinearLayout
    private lateinit var loadingMatches: LinearLayout
    private lateinit var matchesError: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search_matches)

        db = Firebase.firestore

        val backBtn = findViewById<ImageButton>(R.id.btnBack)
        val homeBtn = findViewById<LinearLayout>(R.id.navHome)
        val searchPlayersBtn = findViewById<LinearLayout>(R.id.navPlayers)
        val retryBtn = findViewById<Button>(R.id.btnRetryMatches)
        matchesListRv = findViewById(R.id.rvMatches)
        matchesNotFound = findViewById(R.id.emptyMatches)
        loadingMatches = findViewById(R.id.loadingMatches)
        matchesError = findViewById(R.id.errorMatches)

        matchAdapter = MatchAdapter(emptyList()) {
                match ->
            MatchDetailsDialog(this).show(match)
        }
        matchesListRv.adapter = matchAdapter
        matchesListRv.layoutManager = LinearLayoutManager(this)

        loadMatches()
        backBtn.setOnClickListener {
            finish()
        }

        retryBtn.setOnClickListener {
            loadMatches()
        }

        homeBtn.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        searchPlayersBtn.setOnClickListener {
            startActivity(Intent(this, SearchPlayersActivity::class.java))
        }
    }

    // roda sempre quando usuário carrega a tela novamente (não pela primeira vez)
    override fun onRestart() {
        super.onRestart()
        loadMatches()
    }

    private fun loadMatches() {
        showLoading()

        db.collection("matches")
            .whereNotEqualTo("creatorId", currentUserId)
            .get()
            .addOnSuccessListener { matchesRequest ->
                // limpa antes de preencher, senão as partidas se repetem a cada nova busca
                matches.clear()

                for (match in matchesRequest) {
                    matches.add(match.toObject(Match::class.java))
                }

                // as partidas que acontecem primeiro aparecem no topo
                matches.sortBy { match -> match.scheduledAt }

                matchAdapter.updateMatches(matches)

                if (matches.size != 0) {
                    showMatches()
                } else {
                    showMatchesNotFound()
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "falha ao buscar partidas", exception)
                showError()
            }
    }

    private fun hideAllStates() {
        matchesListRv.visibility = View.GONE
        loadingMatches.visibility = View.GONE
        matchesNotFound.visibility = View.GONE
        matchesError.visibility = View.GONE
    }

    private fun showLoading() {
        hideAllStates()
        loadingMatches.visibility = View.VISIBLE
    }

    private fun showMatches() {
        hideAllStates()
        matchesListRv.visibility = View.VISIBLE
    }

    private fun showMatchesNotFound() {
        hideAllStates()
        matchesNotFound.visibility = View.VISIBLE
    }

    private fun showError() {
        hideAllStates()
        matchesError.visibility = View.VISIBLE
    }

    private companion object {
        const val TAG = "SearchMatches"
    }
}
