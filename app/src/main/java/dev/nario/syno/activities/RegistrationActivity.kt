package dev.nario.syno.activities

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

class RegistrationActivity : ComponentActivity() {

    private lateinit var emailErrorMsg: TextView
    private lateinit var pwdErrorMsg: TextView
    private lateinit var auth: FirebaseAuth
    private val TAG = "RegistrationActivityLog"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.registration_activity)

        auth = Firebase.auth

        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.w(TAG,"user already exists, email: ${currentUser.email}")
            //TODO: go to home page
        }

        val credentialManager = CredentialManager.create(this)

        //declare variables
        val emailField = findViewById<EditText>(R.id.etEmail)
        val emailContainer = findViewById<LinearLayout>(R.id.emailContainer)
        emailErrorMsg = findViewById<TextView>(R.id.emailErrorMessage)

        val pwdField = findViewById<EditText>(R.id.etPassword)
        val pwdContainer = findViewById<LinearLayout>(R.id.passwordContainer)
        pwdErrorMsg = findViewById<TextView>(R.id.passwordErrorMessage)

        val confirmPwdField = findViewById<EditText>(R.id.etConfirmPassword)
        val confirmPwdContainer = findViewById<LinearLayout>(R.id.confirmPasswordContainer)

        val createAccountBtn = findViewById<Button>(R.id.btnCreateAccount)
        val googleAccountBtn = findViewById<Button>(R.id.btnGoogle)

        // click to login with email and password
        createAccountBtn.setOnClickListener {
            //validate user email and change UI to show if something is wrong
            if (!isEmailValid(emailField.text.toString())) {
                emailContainer.setBackgroundResource(R.drawable.bg_input_error)
                return@setOnClickListener
            }
            emailContainer.setBackgroundResource(R.drawable.bg_input)
            emailErrorMsg.visibility = View.GONE


            // validate user password and change UI to show if something is wrong
            if (!isPasswordsValid(pwdField.text.toString(), confirmPwdField.text.toString())) {
                pwdContainer.setBackgroundResource(R.drawable.bg_input_error)
                confirmPwdContainer.setBackgroundResource(R.drawable.bg_input_error)
                return@setOnClickListener
            }
            pwdErrorMsg.visibility = View.GONE
            pwdContainer.setBackgroundResource(R.drawable.bg_input)
            confirmPwdContainer.setBackgroundResource(R.drawable.bg_input)

            // create te user account after validate user data
            auth.createUserWithEmailAndPassword(emailField.text.toString(), pwdField.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.w(TAG,"user created: email ${emailField.text}")
                        //TODO redirect to home page
                    } else {
                        if (task.exception is FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "Um usuário com este email já existe!", Toast.LENGTH_LONG).show()
                        }

                        Log.w(TAG, "Fail to create user with email and password", task.exception)
                    }
                }

        }

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
                    val result = credentialManager.getCredential(this@RegistrationActivity, request)
                    handleSignInWithGoogle(result.credential)
                } catch (e: GetCredentialException) {
                    Log.e("RegistrationActivity", e.errorMessage.toString())
                    Toast.makeText(
                        this@RegistrationActivity,
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
