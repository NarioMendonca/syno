package dev.nario.syno.activities

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Path
import android.graphics.PathMeasure
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import dev.nario.syno.BaseActivity
import dev.nario.syno.R

class SearchPlayersActivity : BaseActivity() {
    private lateinit var searchingIcon: ImageView
    private lateinit var searchConfigContainer: LinearLayout
    private lateinit var searchingContainer: LinearLayout
    private lateinit var playerFoundContainer: LinearLayout
    private lateinit var noPlayerContainer: LinearLayout
    private lateinit var visibleContainer: LinearLayout

    private lateinit var searchAnimation: ValueAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search_player)

        searchConfigContainer = findViewById(R.id.searchConfigContainer)
        searchingContainer = findViewById(R.id.searchingContainer)
        playerFoundContainer = findViewById(R.id.playerFoundContainer)
        noPlayerContainer = findViewById(R.id.noPlayerContainer)
        visibleContainer = findViewById(R.id.visibleContainer)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val seekBarDistance = findViewById<SeekBar>(R.id.seekBarDistance)
        val distanceValue = findViewById<TextView>(R.id.tvDistanceValue)
        searchingIcon = findViewById(R.id.ivSearchingIcon)

        btnBack.setOnClickListener {
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
    }

    private fun showSearchConfig() {
        searchConfigContainer.visibility = View.VISIBLE
        searchingContainer.visibility = View.GONE
        playerFoundContainer.visibility = View.GONE
        noPlayerContainer.visibility = View.GONE
    }

    private fun showSearching() {
        searchConfigContainer.visibility = View.GONE
        searchingContainer.visibility = View.VISIBLE
        playerFoundContainer.visibility = View.GONE
        noPlayerContainer.visibility = View.GONE
    }

    private fun showPlayerFound() {
        searchConfigContainer.visibility = View.GONE
        searchingContainer.visibility = View.GONE
        playerFoundContainer.visibility = View.VISIBLE
        noPlayerContainer.visibility = View.GONE
    }

    private fun showNoPlayer() {
        searchConfigContainer.visibility = View.GONE
        searchingContainer.visibility = View.GONE
        playerFoundContainer.visibility = View.GONE
        noPlayerContainer.visibility = View.VISIBLE
    }

    private fun showVisible() {
        searchConfigContainer.visibility = View.GONE
        searchingContainer.visibility = View.GONE
        playerFoundContainer.visibility = View.GONE
        noPlayerContainer.visibility = View.GONE
        visibleContainer.visibility = View.VISIBLE
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
}