package dev.nario.syno.dialogs

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
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
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dev.nario.syno.BaseActivity
import dev.nario.syno.R
import dev.nario.syno.activities.ProfileActivity
import dev.nario.syno.adapters.ParticipantsAdapter
import dev.nario.syno.entities.Match
import dev.nario.syno.entities.User
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MatchDetailsDialog(
    private val activity: BaseActivity,
    private val TAG: String = "MatchDetailsDialog",
    private val db: FirebaseFirestore = Firebase.firestore,
) {
    private lateinit var btnJoin: Button
    private lateinit var tvPlayers: TextView
    private lateinit var tvParticipantsCount: TextView
    private lateinit var participantsAdapter: ParticipantsAdapter

    // participantes que a tela está mostrando agora, para a UI refletir
    // as entradas e saídas sem precisar reabrir o dialog
    private val participants = ArrayList<String>()
    private var requestInProgress = false

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

        tvPlayers = dialog.findViewById(
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

        tvParticipantsCount = dialog.findViewById(
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

        // match participants recycler view
        val participantsRecyclerView = dialog.findViewById<RecyclerView>(R.id.rvParticipantsList)
        participantsAdapter = ParticipantsAdapter(emptyList()) { participant ->
            val profileActivityIntent = Intent(activity, ProfileActivity::class.java)
            profileActivityIntent.putExtra("userId", participant.id)
            activity.startActivity(profileActivityIntent)

        }
        participantsRecyclerView.adapter = participantsAdapter
        participantsRecyclerView.layoutManager = LinearLayoutManager(dialog.context)

        participants.clear()
        participants.addAll(match.participants)

        updateParticipantsCount(match)
        renderJoinButton(match)
        loadParticipants(match)

        // o criador da partida não entra nem desiste da própria partida
        if (match.creatorId == activity.currentUserId) {
            btnJoin.visibility = View.GONE
        }

        // garante que a lista mostrada é a do servidor, e não só a que veio na listagem
        refreshParticipantsFromServer(match)

        // close match
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        dialog.window?.setLayout(
            (activity.resources.displayMetrics.widthPixels * 0.92)
                .toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    /**
     * O mesmo botão alterna entre entrar e desistir, de acordo com o usuário
     * já estar ou não na partida.
     */
    private fun renderJoinButton(match: Match) {
        val userIsParticipant = activity.currentUserId in participants

        if (userIsParticipant) {
            btnJoin.text = "Desistir da partida"
            btnJoin.backgroundTintList = ColorStateList.valueOf("#27272A".toColorInt())
            btnJoin.setTextColor("#F87171".toColorInt())
            btnJoin.setOnClickListener { leaveMatch(match) }
        } else {
            btnJoin.text = "Participar da partida"
            btnJoin.backgroundTintList = ColorStateList.valueOf("#8B5CF6".toColorInt())
            btnJoin.setTextColor("#FFFFFF".toColorInt())
            btnJoin.setOnClickListener { joinMatch(match) }
        }

        btnJoin.isEnabled = !requestInProgress
    }

    private fun updateParticipantsCount(match: Match) {
        tvPlayers.text = "👥 ${participants.size} / ${match.maxPlayers} jogadores"
        tvParticipantsCount.text = "${participants.size} jogadores"
    }

    private fun joinMatch(match: Match) {
        // verify if the user is the match creator
        if (match.creatorId == activity.currentUserId) {
            Toast.makeText(
                activity,
                "Você não pode tentar entrar em sua propria partida!",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // just ensures that match id is not null to avoid crashes
        val matchId = match.id
        if (matchId == null) {
            Toast.makeText(
                activity,
                "Erro ao tentar entrar na partida",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (requestInProgress) {
            return
        }

        setRequestInProgress(true, "Entrando...")

        // subscribe user in the match
        db.collection("matches")
            .document(matchId)
            .update(
                "participants",
                FieldValue.arrayUnion(activity.currentUserId)
            )
            .addOnSuccessListener {
                if (activity.currentUserId !in participants) {
                    participants.add(activity.currentUserId)
                }

                setRequestInProgress(false)
                updateParticipantsCount(match)
                renderJoinButton(match)
                loadParticipants(match)

                Toast.makeText(
                    activity,
                    "Você entrou na partida!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "falha ao entrar na partida $matchId", exception)

                setRequestInProgress(false)
                renderJoinButton(match)

                Toast.makeText(
                    activity,
                    "Erro ao tentar entrar na partida, verifique sua internet",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun leaveMatch(match: Match) {
        val matchId = match.id
        if (matchId == null) {
            Toast.makeText(
                activity,
                "Erro ao tentar sair da partida",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (requestInProgress) {
            return
        }

        setRequestInProgress(true, "Saindo...")

        db.collection("matches")
            .document(matchId)
            .update(
                "participants",
                FieldValue.arrayRemove(activity.currentUserId)
            )
            .addOnSuccessListener {
                participants.remove(activity.currentUserId)

                setRequestInProgress(false)
                updateParticipantsCount(match)
                renderJoinButton(match)
                loadParticipants(match)

                Toast.makeText(
                    activity,
                    "Você saiu da partida.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "falha ao sair da partida $matchId", exception)

                setRequestInProgress(false)
                renderJoinButton(match)

                Toast.makeText(
                    activity,
                    "Erro ao tentar sair da partida, verifique sua internet",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    /**
     * Bloqueia o botão enquanto a requisição está no ar para o usuário não
     * entrar e sair várias vezes com toques repetidos.
     */
    private fun setRequestInProgress(inProgress: Boolean, loadingText: String? = null) {
        requestInProgress = inProgress
        btnJoin.isEnabled = !inProgress

        if (inProgress && loadingText != null) {
            btnJoin.text = loadingText
        }
    }

    private fun loadParticipants(match: Match) {
        // whereIn quebra com lista vazia, e sem participantes não há nada para buscar
        if (participants.isEmpty()) {
            participantsAdapter.updateParticipants(emptyList())
            return
        }

        db.collection("users")
            .whereIn(FieldPath.documentId(), participants.toList())
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
    }

    /**
     * A partida da listagem pode estar desatualizada (outro jogador pode ter entrado
     * depois), então relê os participantes no servidor ao abrir o dialog.
     */
    private fun refreshParticipantsFromServer(match: Match) {
        val matchId = match.id ?: return

        db.collection("matches")
            .document(matchId)
            .get()
            .addOnSuccessListener { document ->
                val serverMatch = document.toObject(Match::class.java) ?: return@addOnSuccessListener

                participants.clear()
                participants.addAll(serverMatch.participants)

                updateParticipantsCount(match)
                renderJoinButton(match)
                loadParticipants(match)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "falha ao atualizar os participantes da partida $matchId", exception)
            }
    }
}
