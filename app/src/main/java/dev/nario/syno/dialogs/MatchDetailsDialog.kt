package dev.nario.syno.dialogs

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dev.nario.syno.R
import dev.nario.syno.activities.HomeActivity
import dev.nario.syno.activities.ProfileActivity
import dev.nario.syno.adapters.ParticipantsAdapter
import dev.nario.syno.entities.Match
import dev.nario.syno.entities.User
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MatchDetailsDialog(
    private val activity: HomeActivity,
    private val TAG: String = "MatchDetailsDialog",
    private val db: FirebaseFirestore = Firebase.firestore,
) {
    private lateinit var btnJoin: Button

    fun show(match: Match) {
        // dialog configs
        val dialog = Dialog(activity)

        dialog.requestWindowFeature(
            Window.FEATURE_NO_TITLE
        )

        dialog.setContentView(
            R.layout.dialog_match_details
        )

        dialog.window?.setBackgroundDrawable(
            Color.TRANSPARENT.toDrawable()
        )

        // views
        val tvGame = dialog.findViewById<TextView>(
            R.id.tvDetailsGame
        )

        val tvMatchName = dialog.findViewById<TextView>(
            R.id.tvDetailsMatchName
        )

        val tvDate = dialog.findViewById<TextView>(
            R.id.tvDetailsDate
        )

        val tvPlayers = dialog.findViewById<TextView>(
            R.id.tvDetailsPlayers
        )

        val tvGameType = dialog.findViewById<TextView>(
            R.id.tvDetailsGameType
        )

        val tvLocation = dialog.findViewById<TextView>(
            R.id.tvDetailsLocation
        )

        val tvAverageAge = dialog.findViewById<TextView>(
            R.id.tvDetailsAverageAge
        )

        val tvParticipantsCount =
            dialog.findViewById<TextView>(
                R.id.tvParticipantsCount
            )

        val btnClose =
            dialog.findViewById<ImageButton>(
                R.id.btnCloseDetails
            )

        btnJoin =
            dialog.findViewById<Button>(
                R.id.btnJoinMatch
            )

        // match info to views
        tvGame.text = match.game
        tvMatchName.text = match.name

        // formatting date
        val matchScheduleAt = match.scheduledAt.toDate().toInstant()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm", Locale.getDefault())
        val dateFormatted = matchScheduleAt.atZone(ZoneId.systemDefault()).format(formatter)
        tvDate.text = "📅 ${dateFormatted}"

        tvPlayers.text = "👥 ${match.participants.size} / ${match.maxPlayers} jogadores"
        tvLocation.text = "🔗 ${match.meetingLocation}"

        // game type (maybe not exists)
        if (match.gameType != null) {
            tvGameType.visibility =
                View.VISIBLE

            tvGameType.text =
                "🎮 ${match.gameType}"

        } else {
            tvGameType.visibility =
                View.GONE
        }

        // average age of Match (maybe not exists)
        if (match.averageAge != null) {
            tvAverageAge.visibility =
                View.VISIBLE

            tvAverageAge.text =
                "👤 Idade média: ${match.averageAge} anos"
        } else {
            tvAverageAge.visibility =
                View.GONE
        }

        // participants count
        tvParticipantsCount.text = "${match.participants.size} jogadores"

        // match participants recycler view
        val participantsRecyclerView = dialog.findViewById<RecyclerView>(R.id.rvParticipantsList)
        val participantsAdapter = ParticipantsAdapter(emptyList()) { participant ->
            val profileActivityIntent = Intent(activity, ProfileActivity::class.java)
            profileActivityIntent.putExtra("userId", participant.id)
            activity.startActivity(profileActivityIntent)

        }
        participantsRecyclerView.adapter = participantsAdapter
        participantsRecyclerView.layoutManager = LinearLayoutManager(dialog.context)

        // get match participans
        db.collection("users")
            .whereIn(FieldPath.documentId(), match.participants)
            .get()
            .addOnSuccessListener { documents ->
                val users = ArrayList<User>()
                for (user in documents) {
                    users.add(user.toObject(User::class.java))
                }

                participantsAdapter.updateParticipants(users)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "falha ao buscar participantes da partida ${match.id}", exception)
                // TODO: show in UI the fail in find match participants
            }

        // close match
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        if (match.creatorId == activity.currentUserId) {
            btnJoin.visibility = View.GONE
        }

        // verify if user is already in this match
        userIsAlreadyOnMatch(match)

        // join in match
        btnJoin.setOnClickListener {
            // verify if the user is the match creator
            if (match.creatorId == activity.currentUserId) {
                Toast.makeText(
                    dialog.context,
                    "Você não pode tentar entrar em sua propria partida!",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            userIsAlreadyOnMatch(match)
            // just ensures that match id is not null to avoid crashes
            if (match.id == null) {
                Toast.makeText(
                    dialog.context,
                    "Erro ao tentar entrar na partida",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            } else {
                // subscribe user in the match
                match.id?.let { matchIdSafe ->
                    db.collection("matches")
                        .document(matchIdSafe)
                        .update(
                            "participants",
                            FieldValue.arrayUnion(activity.currentUserId)
                        )
                        .addOnSuccessListener {
                            btnJoin.isClickable = false
                            btnJoin.text = "Você já está nessa partida!"
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                dialog.context,
                                "Erro ao tentar entrar na partida, verifique sua internet",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
        }

        dialog.show()

        dialog.window?.setLayout(
            (activity.resources.displayMetrics.widthPixels * 0.92)
                .toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    fun userIsAlreadyOnMatch(match: Match) {
        match.id?.let { matchIdSafe ->
            db.collection("matches")
                .document(matchIdSafe)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val requestedMatch = document.toObject(Match::class.java)
                        if (activity.currentUserId in requestedMatch!!.participants) {
                            btnJoin.isClickable = false
                            btnJoin.text = "Você já está nessa partida!"
                        }
                    }
                }
        }
    }
}