package dev.nario.syno.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.firestore
import dev.nario.syno.BaseActivity
import dev.nario.syno.R
import dev.nario.syno.entities.User

class ProfileActivity: BaseActivity() {
    private val TAG = "ProfileActivity"
    private lateinit var user: User
    private lateinit var profileContentContainer: View
    private lateinit var loadingContainer: View
    private lateinit var userNotFoundContainer: View
    private lateinit var userNotFoundMessage: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.profile_activity)

        profileContentContainer = findViewById<LinearLayout>(R.id.contentContainer)
        loadingContainer = findViewById<LinearLayout>(R.id.loadingContainer)
        userNotFoundContainer = findViewById<LinearLayout>(R.id.userNotFoundContainer)
        userNotFoundMessage = findViewById<TextView>(R.id.userNotFoundMessage)

        showLoading()

        val userIdToFind = intent.getStringExtra("userId") ?: currentUserId

        val db = Firebase.firestore

        db.collection("users")
            .document(userIdToFind)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userData = document.toObject(User::class.java)
                    if (userData != null) {
                        user = userData
                        fillUserFields(user)
                        showProfile()
                    } else {
                        showUserNotFound("Erro ao processar dados do usuário")
                    }
                } else {
                    showUserNotFound()
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Falha ao buscar informações do usuário: ${userIdToFind}", exception)
                showUserNotFound("Falha ao buscar informações do usuário")
            }

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

    fun fillUserFields(user: User) {
        val onlineIndicator = findViewById<View>(R.id.onlineIndicator)
        val profileName = findViewById<TextView>(R.id.tvProfileName)
        val onlineStatus = findViewById<TextView>(R.id.tvOnlineStatus)
        val profileDescription = findViewById<TextView>(R.id.tvProfileDescription)
        val favoriteGame = findViewById<TextView>(R.id.tvFavoriteGame)
        val city = findViewById<TextView>(R.id.tvCity)

        val rating = findViewById<TextView>(R.id.tvRating)
        val votesCount = findViewById<TextView>(R.id.tvVotesCount)

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

        rating.text = "${user.rating}"
        votesCount.text = "${user.votesCount} avaliações"
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
}