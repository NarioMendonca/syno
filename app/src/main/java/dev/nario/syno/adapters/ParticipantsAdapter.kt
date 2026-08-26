package dev.nario.syno.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.nario.syno.R
import dev.nario.syno.entities.User

class ParticipantsAdapter(
    private var participants: List<User>,
    private val onParticipantClick: (User) -> Unit
) : RecyclerView.Adapter<ParticipantsAdapter.ParticipantsViewHolder>() {

    class ParticipantsViewHolder(participantView: View): RecyclerView.ViewHolder(participantView) {
        val profilePhoto = participantView.findViewById<ImageView>(R.id.ivProfilePhoto)
        val participantName = participantView.findViewById<TextView>(R.id.tvParticipantName)
        val participantDescription = participantView.findViewById<TextView>(R.id.tvParticipantDescription)
        val participantFavoriteGame = participantView.findViewById<TextView>(R.id.tvParticipantFavoriteGame)
        val participantRating = participantView.findViewById<TextView>(R.id.tvParticipantRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match_participant, parent, false)
        return ParticipantsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParticipantsViewHolder, position: Int) {
        val participant = participants[position]
        // TODO: add user photo pic
        holder.participantName.text = participant.name
        holder.participantDescription.text = participant.profileDescription
        holder.participantFavoriteGame.text = participant.favoriteGame
        holder.participantRating.text = participant.rating.toString()

        holder.itemView.setOnClickListener {
            onParticipantClick(participant)
        }
    }

    override fun getItemCount(): Int {
        return participants.size
    }

    fun updateParticipants(newParticipants: List<User>) {
        participants = newParticipants
        notifyDataSetChanged()
    }
}