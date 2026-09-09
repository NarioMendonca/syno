package dev.nario.syno.activities

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Path
import android.graphics.PathMeasure
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.gson.Gson
import dev.nario.syno.BaseActivity
import dev.nario.syno.R
import dev.nario.syno.http.ApiErrorResponse
import dev.nario.syno.http.FindNearbyPlayerResponse
import dev.nario.syno.http.MatchedPlayer
import dev.nario.syno.http.PlayerMatchmakingData
import dev.nario.syno.http.RegisterUserResponse
import dev.nario.syno.http.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchPlayersActivity : BaseActivity() {
    private lateinit var searchingIcon: ImageView
    private lateinit var searchConfigContainer: LinearLayout
    private lateinit var searchingContainer: LinearLayout
    private lateinit var playerFoundContainer: LinearLayout
    private lateinit var noPlayerContainer: LinearLayout
    private lateinit var visibleContainer: LinearLayout

    private lateinit var etContact: EditText
    private lateinit var switchVisible: Switch
    private lateinit var seekBarDistance: SeekBar
    private lateinit var btnSearchPlayer: Button

    private lateinit var tvFoundPlayerName: TextView
    private lateinit var tvFoundPlayerContact: TextView
    private lateinit var tvFoundPlayerDistance: TextView

    private lateinit var tvVisibleContact: TextView
    private lateinit var tvVisibleDistance: TextView

    private lateinit var searchAnimation: ValueAnimator

    private var activeSearchCall: Call<FindNearbyPlayerResponse>? = null
    private var pendingLocationAction: (() -> Unit)? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val action = pendingLocationAction
        pendingLocationAction = null

        if (granted && action != null) {
            action()
        } else if (!granted) {
            Toast.makeText(this, "É necessário permitir o acesso à localização para buscar jogadores.", Toast.LENGTH_LONG).show()
        }
    }

    private val ignoreResponseCallback = object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {}
        override fun onFailure(call: Call<Void>, t: Throwable) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search_player)

        searchConfigContainer = findViewById(R.id.searchConfigContainer)
        searchingContainer = findViewById(R.id.searchingContainer)
        playerFoundContainer = findViewById(R.id.playerFoundContainer)
        noPlayerContainer = findViewById(R.id.noPlayerContainer)
        visibleContainer = findViewById(R.id.visibleContainer)

        etContact = findViewById(R.id.etContact)
        switchVisible = findViewById(R.id.switchVisible)
        seekBarDistance = findViewById(R.id.seekBarDistance)
        btnSearchPlayer = findViewById(R.id.btnSearchPlayer)
        val distanceValue = findViewById<TextView>(R.id.tvDistanceValue)
        searchingIcon = findViewById(R.id.ivSearchingIcon)

        tvFoundPlayerName = findViewById(R.id.tvFoundPlayerName)
        tvFoundPlayerContact = findViewById(R.id.tvFoundPlayerContact)
        tvFoundPlayerDistance = findViewById(R.id.tvFoundPlayerDistance)

        tvVisibleContact = findViewById(R.id.tvVisibleContact)
        tvVisibleDistance = findViewById(R.id.tvVisibleDistance)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnBackFound = findViewById<ImageButton>(R.id.btnBackFound)
        val btnBackVisible = findViewById<ImageButton>(R.id.btnBackVisible)
        val btnCancelSearch = findViewById<Button>(R.id.btnCancelSearch)
        val btnTryAgain = findViewById<Button>(R.id.btnTryAgain)
        val btnCancelVisibility = findViewById<Button>(R.id.btnCancelVisibility)

        btnBack.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        btnBackFound.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        btnBackVisible.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        seekBarDistance.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    val distance = progress + 1

                    distanceValue.text =
                        "$distance km"
                }

                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {}

                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {}
            }
        )

        switchVisible.setOnCheckedChangeListener { _, isChecked ->
            btnSearchPlayer.text = if (isChecked) {
                "Ficar encontrável a outros jogadores"
            } else {
                "Buscar jogador"
            }
        }

        btnSearchPlayer.setOnClickListener {
            onSearchPlayerClicked()
        }

        btnCancelSearch.setOnClickListener {
            activeSearchCall?.cancel()
            stopSearchAnimation()
            RetrofitClient.matchmakingApi.cancelMatchmaking(currentUserId).enqueue(ignoreResponseCallback)
            showSearchConfig()
        }

        btnTryAgain.setOnClickListener {
            showSearchConfig()
        }

        btnCancelVisibility.setOnClickListener {
            RetrofitClient.matchmakingApi.cancelMatchmaking(currentUserId).enqueue(ignoreResponseCallback)
            showSearchConfig()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activeSearchCall?.cancel()
    }

    private fun onSearchPlayerClicked() {
        val contact = etContact.text?.toString()?.trim().orEmpty()
        if (contact.isEmpty()) {
            Toast.makeText(this, "Informe um contato para que outros jogadores possam falar com você.", Toast.LENGTH_LONG).show()
            return
        }

        val distanceLimitToSearch = seekBarDistance.progress + 1

        withLocationPermission {
            getCurrentLocation(
                onSuccess = { location ->
                    val data = PlayerMatchmakingData(
                        firebase_id = currentUserId,
                        contact = contact,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        distanceLimitToSearch = distanceLimitToSearch,
                        preferences = emptyList()
                    )

                    if (switchVisible.isChecked) {
                        registerInMatchmaking(data)
                    } else {
                        searchNearbyPlayer(data)
                    }
                },
                onError = {
                    Toast.makeText(this, "Não foi possível obter sua localização.", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun searchNearbyPlayer(data: PlayerMatchmakingData) {
        showSearching()

        val call = RetrofitClient.matchmakingApi.findNearbyPlayer(data)
        activeSearchCall = call

        call.enqueue(object : Callback<FindNearbyPlayerResponse> {
            override fun onResponse(call: Call<FindNearbyPlayerResponse>, response: Response<FindNearbyPlayerResponse>) {
                when {
                    response.isSuccessful -> response.body()?.let { showPlayerFound(it.findedUser) }
                    response.code() == 404 -> showNoPlayer()
                    else -> {
                        Toast.makeText(this@SearchPlayersActivity, parseErrorMessage(response), Toast.LENGTH_LONG).show()
                        showSearchConfig()
                    }
                }
            }

            override fun onFailure(call: Call<FindNearbyPlayerResponse>, t: Throwable) {
                if (call.isCanceled) return
                Log.e(TAG, "Falha ao buscar jogador em ${call.request().url()}", t)
                Toast.makeText(this@SearchPlayersActivity, connectionErrorMessage(t), Toast.LENGTH_LONG).show()
                showSearchConfig()
            }
        })
    }

    private fun registerInMatchmaking(data: PlayerMatchmakingData) {
        RetrofitClient.matchmakingApi.registerInMatchmaking(data).enqueue(object : Callback<RegisterUserResponse> {
            override fun onResponse(call: Call<RegisterUserResponse>, response: Response<RegisterUserResponse>) {
                if (response.isSuccessful) {
                    showVisible(data)
                } else {
                    Toast.makeText(this@SearchPlayersActivity, parseErrorMessage(response), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<RegisterUserResponse>, t: Throwable) {
                Log.e(TAG, "Falha ao registrar no matchmaking em ${call.request().url()}", t)
                Toast.makeText(this@SearchPlayersActivity, connectionErrorMessage(t), Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun withLocationPermission(action: () -> Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            action()
        } else {
            pendingLocationAction = action
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(onSuccess: (Location) -> Unit, onError: () -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { lastLocation ->
                if (lastLocation != null) {
                    onSuccess(lastLocation)
                    return@addOnSuccessListener
                }

                fusedLocationClient
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            onSuccess(location)
                        } else {
                            Log.e(TAG, "getCurrentLocation retornou null")
                            onError()
                        }
                    }
                    .addOnFailureListener { error ->
                        Log.e(TAG, "Falha ao obter a localização atual", error)
                        onError()
                    }
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Falha ao obter a última localização conhecida", error)
                onError()
            }
    }

    private fun connectionErrorMessage(t: Throwable): String {
        return if (t is java.net.SocketTimeoutException) {
            "O servidor demorou demais para responder."
        } else {
            "Falha de conexão com o servidor. Verifique se a API está rodando."
        }
    }

    private fun <T> parseErrorMessage(response: Response<T>): String {
        return try {
            val errorJson = response.errorBody()?.string()
            if (errorJson.isNullOrBlank()) {
                "Ocorreu um erro inesperado."
            } else {
                Gson().fromJson(errorJson, ApiErrorResponse::class.java)?.message ?: "Ocorreu um erro inesperado."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao interpretar o erro da API", e)
            "Ocorreu um erro inesperado."
        }
    }

    private fun hideAllContainers() {
        searchConfigContainer.visibility = View.GONE
        searchingContainer.visibility = View.GONE
        playerFoundContainer.visibility = View.GONE
        noPlayerContainer.visibility = View.GONE
        visibleContainer.visibility = View.GONE
    }

    private fun showSearchConfig() {
        stopSearchAnimation()
        hideAllContainers()
        searchConfigContainer.visibility = View.VISIBLE
    }

    private fun showSearching() {
        hideAllContainers()
        searchingContainer.visibility = View.VISIBLE
        startSearchAnimation()
    }

    private fun showPlayerFound(matchedPlayer: MatchedPlayer) {
        stopSearchAnimation()
        hideAllContainers()
        playerFoundContainer.visibility = View.VISIBLE

        tvFoundPlayerName.visibility = View.GONE
        tvFoundPlayerContact.text = matchedPlayer.contact
        tvFoundPlayerDistance.text = "📍 %.1f km de distância".format(matchedPlayer.distancia_km)
    }

    private fun showNoPlayer() {
        stopSearchAnimation()
        hideAllContainers()
        noPlayerContainer.visibility = View.VISIBLE
    }

    private fun showVisible(data: PlayerMatchmakingData) {
        stopSearchAnimation()
        hideAllContainers()
        visibleContainer.visibility = View.VISIBLE

        tvVisibleContact.text = data.contact
        tvVisibleDistance.text = "${data.distanceLimitToSearch} km"
    }

    private fun startSearchAnimation() {

        val path = Path()

        path.moveTo(0f, 0f)

        // Lado esquerdo do infinito
        path.cubicTo(
            -80f, -70f,
            -80f, 70f,
            0f, 0f
        )

        // Lado direito do infinito
        path.cubicTo(
            80f, -70f,
            80f, 70f,
            0f, 0f
        )

        val pathMeasure = PathMeasure(path, false)

        val position = FloatArray(2)

        searchAnimation = ValueAnimator.ofFloat(
            0f,
            pathMeasure.length
        )

        searchAnimation.duration = 3000
        searchAnimation.repeatCount = ValueAnimator.INFINITE
        searchAnimation.interpolator = LinearInterpolator()

        searchAnimation.addUpdateListener { animation ->

            val distance =
                animation.animatedValue as Float

            pathMeasure.getPosTan(
                distance,
                position,
                null
            )

            searchingIcon.translationX =
                position[0]

            searchingIcon.translationY =
                position[1]
        }

        searchAnimation.start()
    }

    private fun stopSearchAnimation() {

        if (::searchAnimation.isInitialized) {
            searchAnimation.cancel()
        }

        searchingIcon.translationX = 0f
        searchingIcon.translationY = 0f
    }

    private companion object {
        const val TAG = "SearchPlayers"
    }
}
