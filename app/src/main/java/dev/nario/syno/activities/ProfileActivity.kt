package dev.nario.syno.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import dev.nario.syno.BaseActivity
import dev.nario.syno.R
import dev.nario.syno.entities.Rating
import dev.nario.syno.entities.User
import java.util.Locale

class ProfileActivity: BaseActivity() {
    private val TAG = "ProfileActivity"
    private val db = Firebase.firestore
    private lateinit var user: User
    private lateinit var viewedUserId: String
    private var isOwnProfile = false
    private var selectedRating = 0

    private lateinit var profileContentContainer: View
    private lateinit var loadingContainer: View
    private lateinit var userNotFoundContainer: View
    private lateinit var userNotFoundMessage: TextView

    private lateinit var tvRating: TextView
    private lateinit var tvRatingStars: TextView
    private lateinit var tvVotesCount: TextView
    private lateinit var ratingFormContainer: LinearLayout
    private lateinit var tvAlreadyVoted: TextView
    private lateinit var stars: List<ImageView>
    private lateinit var btnSubmitRating: Button
    private lateinit var tvRatingFeedback: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.profile_activity)

        profileContentContainer = findViewById<LinearLayout>(R.id.contentContainer)
        loadingContainer = findViewById<LinearLayout>(R.id.loadingContainer)
        userNotFoundContainer = findViewById<LinearLayout>(R.id.userNotFoundContainer)
        userNotFoundMessage = findViewById<TextView>(R.id.userNotFoundMessage)

        tvRating = findViewById(R.id.tvRating)
        tvRatingStars = findViewById(R.id.tvRatingStars)
        tvVotesCount = findViewById(R.id.tvVotesCount)
        ratingFormContainer = findViewById(R.id.ratingFormContainer)
        tvAlreadyVoted = findViewById(R.id.tvAlreadyVoted)
        tvRatingFeedback = findViewById(R.id.tvRatingFeedback)
        btnSubmitRating = findViewById(R.id.btnSubmitRating)
        stars = listOf(
            findViewById(R.id.star1),
            findViewById(R.id.star2),
            findViewById(R.id.star3),
            findViewById(R.id.star4),
            findViewById(R.id.star5)
        )

        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                selectedRating = index + 1
                renderStars()
            }
        }

        btnSubmitRating.setOnClickListener {
            submitRating()
        }

        showLoading()

        viewedUserId = intent.getStringExtra("userId")?.takeIf { it.isNotBlank() } ?: currentUserId
        isOwnProfile = viewedUserId == currentUserId

        loadUser()

        val backBtn = findViewById<Button>(R.id.btnBackNotFound)
        backBtn.setOnClickListener {
            // shut down this activity and back to previous Activity in activities stack
            finish()
        }
        val btnBack = findViewById<View>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadUser() {
        db.collection("users")
            .document(viewedUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userData = document.toObject(User::class.java)
                    if (userData != null) {
                        user = userData
                        fillUserFields(user)
                        renderRatingSection()
                        showProfile()
                    } else {
                        showUserNotFound("Erro ao processar dados do usuário")
                    }
                } else {
                    showUserNotFound()
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Falha ao buscar informações do usuário: ${viewedUserId}", exception)
                showUserNotFound("Falha ao buscar informações do usuário")
            }
    }

    fun fillUserFields(user: User) {
        val onlineIndicator = findViewById<View>(R.id.onlineIndicator)
        val profileName = findViewById<TextView>(R.id.tvProfileName)
        val onlineStatus = findViewById<TextView>(R.id.tvOnlineStatus)
        val profileDescription = findViewById<TextView>(R.id.tvProfileDescription)
        val favoriteGame = findViewById<TextView>(R.id.tvFavoriteGame)
        val city = findViewById<TextView>(R.id.tvCity)

        if (!user.isOnline) {
            onlineIndicator.visibility = View.GONE
            onlineStatus.text = "Offline"
        }

        profileName.text = user.name
        profileDescription.text = user.profileDescription
        favoriteGame.text = "${favoriteGame.text} ${user.favoriteGame}"

        if (user.city.isEmpty()) {
            city.text = "Localização não informada"
        } else {
            city.text = "${city.text} ${user.city}"
        }
    }

    private fun renderRatingSection() {
        tvRating.text = String.format(Locale.getDefault(), "%.1f", user.rating)
        tvRatingStars.text = filledStars(Math.round(user.rating).toInt())
        tvVotesCount.text = "${user.votesCount} avaliações"

        val alreadyVoted = user.votes.any { vote -> vote.voterId == currentUserId }

        if (isOwnProfile) {
            ratingFormContainer.visibility = View.GONE
            tvAlreadyVoted.visibility = View.GONE
        } else if (alreadyVoted) {
            ratingFormContainer.visibility = View.GONE
            tvAlreadyVoted.visibility = View.VISIBLE
        } else {
            ratingFormContainer.visibility = View.VISIBLE
            tvAlreadyVoted.visibility = View.GONE
        }

        selectedRating = 0
        renderStars()
    }

    private fun showRatingFeedback(message: String, isError: Boolean) {
        tvRatingFeedback.text = message
        tvRatingFeedback.setTextColor(if (isError) ERROR_COLOR else SUCCESS_COLOR)
        tvRatingFeedback.visibility = View.VISIBLE
    }

    private fun renderStars() {
        stars.forEachIndexed { index, star ->
            val filled = index < selectedRating

            star.setImageResource(
                if (filled) R.drawable.ic_star_filled else R.drawable.ic_star_outline
            )

            val scale = if (filled) 1.15f else 1f
            star.scaleX = scale
            star.scaleY = scale
        }
    }

    private fun filledStars(fullStars: Int): String {
        val clamped = fullStars.coerceIn(0, 5)
        return "★".repeat(clamped) + "☆".repeat(5 - clamped)
    }

    private fun submitRating() {
        if (isOwnProfile) {
            return
        }

        if (selectedRating !in 1..5) {
            showRatingFeedback("Toque em uma estrela para escolher a nota", isError = true)
            Toast.makeText(this, "Selecione de 1 a 5 estrelas para avaliar", Toast.LENGTH_LONG).show()
            return
        }

        btnSubmitRating.isEnabled = false
        btnSubmitRating.text = "Enviando..."
        tvRatingFeedback.visibility = View.GONE

        val votedRating = selectedRating
        val userDocRef = db.collection("users").document(viewedUserId)


        db.runTransaction { transaction ->
            val snapshot = transaction.get(userDocRef)
            val serverUser = snapshot.toObject(User::class.java)
                ?: throw IllegalStateException("Usuário não encontrado")

            if (serverUser.votes.any { vote -> vote.voterId == currentUserId }) {
                throw AlreadyVotedException()
            }

            val newVotesCount = serverUser.votesCount + 1
            val newRating = (serverUser.rating * serverUser.votesCount + votedRating) / newVotesCount

            val vote = Rating(
                targetUserId = viewedUserId,
                voterId = currentUserId,
                value = votedRating
            )

            transaction.update(
                userDocRef,
                mapOf(
                    "rating" to newRating,
                    "votesCount" to newVotesCount,
                    "votes" to FieldValue.arrayUnion(vote)
                )
            )

            User(
                id = serverUser.id,
                name = serverUser.name,
                profileDescription = serverUser.profileDescription,
                email = serverUser.email,
                isEmailVerified = serverUser.isEmailVerified,
                photoUrl = serverUser.photoUrl,
                rating = newRating,
                votesCount = newVotesCount,
                votes = serverUser.votes + vote,
                favoriteGame = serverUser.favoriteGame,
                city = serverUser.city,
                latitude = serverUser.latitude,
                longitude = serverUser.longitude,
                isOnline = serverUser.isOnline
            )
        }
            .addOnSuccessListener { updatedUser ->
                user = updatedUser
                renderRatingSection()

                btnSubmitRating.isEnabled = true
                btnSubmitRating.text = "Enviar avaliação"

                val newRating = String.format(Locale.getDefault(), "%.1f", updatedUser.rating)
                showRatingFeedback(
                    "✅ Avaliação de $votedRating ${if (votedRating == 1) "estrela" else "estrelas"} enviada! Nova média: $newRating",
                    isError = false
                )

                Toast.makeText(this, "Avaliação enviada!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                btnSubmitRating.isEnabled = true
                btnSubmitRating.text = "Enviar avaliação"

                if (exception is AlreadyVotedException) {
                    showRatingFeedback("Você já avaliou este jogador", isError = true)
                    Toast.makeText(this, "Você já avaliou este jogador", Toast.LENGTH_LONG).show()
                    loadUser()
                    return@addOnFailureListener
                }

                Log.w(TAG, "Falha ao enviar avaliação para $viewedUserId", exception)
                showRatingFeedback(
                    "❌ Não foi possível enviar sua avaliação. Verifique sua internet e tente de novo.",
                    isError = true
                )
                Toast.makeText(
                    this,
                    "Erro ao enviar avaliação, verifique sua internet",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun showLoading() {
        profileContentContainer.visibility = View.GONE
        loadingContainer.visibility = View.VISIBLE
        userNotFoundContainer.visibility = View.GONE
    }

    fun showProfile() {
        profileContentContainer.visibility = View.VISIBLE
        loadingContainer.visibility = View.GONE
        userNotFoundContainer.visibility = View.GONE
    }

    fun showUserNotFound(errorMessage: String? = "") {
        profileContentContainer.visibility = View.GONE
        loadingContainer.visibility = View.GONE
        userNotFoundContainer.visibility = View.VISIBLE

        if (!errorMessage.isNullOrEmpty()) {
            userNotFoundMessage.text = errorMessage
        }
    }

    private class AlreadyVotedException : Exception()

    private companion object {
        const val SUCCESS_COLOR = 0xFF22C55E.toInt()
        const val ERROR_COLOR = 0xFFF87171.toInt()
    }
}
