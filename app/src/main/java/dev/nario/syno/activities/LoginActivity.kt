package dev.nario.syno.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import dev.nario.syno.R
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    private lateinit var emailErrorMsg: TextView
    private lateinit var pwdErrorMsg: TextView
    private lateinit var auth: FirebaseAuth
    private val TAG = "LoginActivityLog"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_activity)

        val homeActivityIntent = Intent(this, HomeActivity::class.java)

        auth = Firebase.auth

        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.w(TAG,"user already exists, email: ${currentUser.email}")
            startActivity(homeActivityIntent)
        }

        val credentialManager = CredentialManager.create(this)

        //declare variables
        val emailField = findViewById<EditText>(R.id.etEmail)
        val emailContainer = findViewById<LinearLayout>(R.id.emailContainer)

        val pwdField = findViewById<EditText>(R.id.etPassword)
        val pwdContainer = findViewById<LinearLayout>(R.id.passwordContainer)

        val googleAccountBtn = findViewById<Button>(R.id.btnGoogle)

        googleAccountBtn.setOnClickListener {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            lifecycleScope.launch {
                try {
                    val result = credentialManager.getCredential(this@LoginActivity, request)
                    handleSignInWithGoogle(result.credential)
                    startActivity(homeActivityIntent)
                } catch (e: GetCredentialException) {
                    Log.e("RegistrationActivity", e.errorMessage.toString())
                    Toast.makeText(
                        this@LoginActivity,
                        "Falha ao fazer login com google, verifique se há uma conta google neste dispositivo",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun isEmailValid(email: String): Boolean {
        if (email.isEmailInvalid()) {
            emailErrorMsg.text = "O email inserido é inválido!"
            emailErrorMsg.visibility = View.VISIBLE
            return false
        }

        return true;
    }

    fun isPasswordsValid(pwd: String, confirmPwd: String): Boolean {
        if (pwd.isEmpty()) {
            pwdErrorMsg.text = "Defina uma senha no campo abaixo!"
            pwdErrorMsg.visibility = View.VISIBLE
            return false;
        }

        if (pwd != confirmPwd) {
            pwdErrorMsg.text = "Senha e confirmar senha não são iguais!"
            pwdErrorMsg.visibility = View.VISIBLE
            return false;
        }

        if (pwd.length < 6) {
            pwdErrorMsg.text = "Sua senha deve ter pelo menos 6 caracteres!"
            pwdErrorMsg.visibility = View.VISIBLE
            return false;
        }

        return true;
    }
    fun CharSequence?.isEmailInvalid(): Boolean {
        val isInvalid = this.isNullOrEmpty() || !Patterns.EMAIL_ADDRESS.matcher(this).matches()
        return isInvalid
    }

    private fun handleSignInWithGoogle(credential: Credential) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

        firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }
}
