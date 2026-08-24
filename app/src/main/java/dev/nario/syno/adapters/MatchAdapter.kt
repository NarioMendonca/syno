package dev.nario.syno.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.nario.syno.R
import dev.nario.syno.entities.Match
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MatchAdapter(
    private var matches: List<Match>,
    private val onMatchClick: (Match) -> Unit
) : RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    private val dateFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm")

    class MatchViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val tvGame: TextView =
            itemView.findViewById(R.id.tvGame)

        val tvMatchName: TextView =
            itemView.findViewById(R.id.tvMatchName)

        val tvDate: TextView =
            itemView.findViewById(R.id.tvDate)

        val tvPlayers: TextView =
            itemView.findViewById(R.id.tvPlayers)

        val tvGameType: TextView =
            itemView.findViewById(R.id.tvGameType)

        val tvAverageAge: TextView =
            itemView.findViewById(R.id.tvAverageAge)

        val tvMeetingLocation: TextView =
            itemView.findViewById(R.id.tvMeetingLocation)

        val btnViewMatch: Button =
            itemView.findViewById(R.id.btnViewMatch)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MatchViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_match,
                parent,
                false
            )

        return MatchViewHolder(view)
    }


    override fun onBindViewHolder(
        holder: MatchViewHolder,
        position: Int
    ) {

        val match = matches[position]

        // match name
        holder.tvMatchName.text = match.name

        // game name
        holder.tvGame.text = match.game

        // date
        val matchScheduleTime = match.scheduledAt.toDate().toInstant()
        val fomatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm" ,Locale.getDefault())
        val formattedDate = matchScheduleTime.atZone(ZoneId.systemDefault()).format(fomatter)
        holder.tvDate.text = formattedDate


        // participants
        holder.tvPlayers.text =
            "👥 ${match.participants.size} / ${match.maxPlayers} jogadores"

        // game local
        holder.tvMeetingLocation.text =
            "🔗 ${match.meetingLocation}"

        // game type
        if (match.gameType != null) {
            holder.tvGameType.visibility =
                View.VISIBLE

            holder.tvGameType.text =
                "🎮 ${match.gameType}"
        } else {
            holder.tvGameType.visibility =
                View.GONE
        }


       // average players age
        if (match.averageAge != null) {
            holder.tvAverageAge.visibility =
                View.VISIBLE

            holder.tvAverageAge.text =
                "👤 Idade média: ${match.averageAge} anos"
        } else {
            holder.tvAverageAge.visibility =
                View.GONE
        }

        holder.itemView.setOnClickListener {

            onMatchClick(match)

        }

        holder.btnViewMatch.setOnClickListener {

            onMatchClick(match)

        }
    }


    override fun getItemCount(): Int {

        return matches.size

    }


    fun updateMatches(newMatches: List<Match>) {

        matches = newMatches

        notifyDataSetChanged()

    }
}