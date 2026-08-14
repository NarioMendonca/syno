package dev.nario.syno.activities

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import dev.nario.syno.R

class RegistrationActivity : ComponentActivity() {

    private lateinit var emailErrorMsg: TextView
    private lateinit var pwdErrorMsg: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.registration_activity)


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

        createAccountBtn.setOnClickListener {
            if (!isEmailValid(emailField.text.toString())) {
                emailContainer.setBackgroundResource(R.drawable.bg_input_error)
                return@setOnClickListener
            }
            emailContainer.setBackgroundResource(R.drawable.bg_input)
            emailErrorMsg.visibility = View.GONE

            if (!isPasswordsValid(pwdField.text.toString(), confirmPwdField.text.toString())) {
                pwdContainer.setBackgroundResource(R.drawable.bg_input_error)
                confirmPwdContainer.setBackgroundResource(R.drawable.bg_input_error)
                return@setOnClickListener
            }
            pwdErrorMsg.visibility = View.GONE
            pwdContainer.setBackgroundResource(R.drawable.bg_input)
            confirmPwdContainer.setBackgroundResource(R.drawable.bg_input)

            print("TODO: criar usuário")
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
}
