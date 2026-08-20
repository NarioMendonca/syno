package dev.nario.syno.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import dev.nario.syno.utils.isEmailInvalid
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

        val homeActivityIntent = Intent(this, HomeActivity::class.java)
        val credentialManager = CredentialManager.create(this)

        //declare variables
        val emailField = findViewById<EditText>(R.id.etEmail)
        val emailContainer = findViewById<LinearLayout>(R.id.emailContainer)
        emailErrorMsg = findViewById<TextView>(R.id.emailErrorMessage)

        //password
        val pwdField = findViewById<EditText>(R.id.etPassword)
        val pwdContainer = findViewById<LinearLayout>(R.id.passwordContainer)
        pwdErrorMsg = findViewById<TextView>(R.id.passwordErrorMessage)

        //confirm password
        val confirmPwdField = findViewById<EditText>(R.id.etConfirmPassword)
        val confirmPwdContainer = findViewById<LinearLayout>(R.id.confirmPasswordContainer)

        // registration buttons
        val createAccountBtn = findViewById<Button>(R.id.btnCreateAccount)
        val googleAccountBtn = findViewById<Button>(R.id.btnGoogle)

        // click to log in with email and password
        createAccountBtn.setOnClickListener {
            //validate user email and change UI to show if something is wrong
            if (emailField.text.toString().isEmailInvalid()) {
                emailErrorMsg.text = "O email inserido é inválido!"
                emailErrorMsg.visibility = View.VISIBLE
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
                        startActivity(homeActivityIntent)
                    } else {
                        if (task.exception is FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "Um usuário com este email já existe!", Toast.LENGTH_LONG).show()
                        }

                        Log.w(TAG, "Fail to create user with email and password", task.exception)
                    }
                }

        }

        // create login with google
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
                    startActivity(homeActivityIntent)
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

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        val homeActivityIntent = Intent(this, HomeActivity::class.java)
        if (currentUser != null) {
            Log.w(TAG,"user already logged in, email: ${currentUser.email}")
            startActivity(homeActivityIntent)
        }
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
