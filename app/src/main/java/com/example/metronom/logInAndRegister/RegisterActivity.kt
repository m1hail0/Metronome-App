package com.example.metronom.logInAndRegister

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.metronom.MetronomeActivity
import com.example.metronom.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.register_layout)


        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        confirmPasswordInput = findViewById(R.id.confirm_password_input)
        registerButton = findViewById(R.id.register_btn)

        auth = FirebaseAuth.getInstance()



        registerButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()
            Log.d("Parameters", "$email, $password, $confirmPassword")
            registerUser(email, password, confirmPassword)
        }

    }

    private fun registerUser(email: String, password: String, confirmPassword: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords don't match!", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Successfully registered!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MetronomeActivity::class.java))
                    finish()
                } else {
                    when (val exception = task.exception) {
                        is FirebaseAuthUserCollisionException -> {
                            Toast.makeText(this, "An account with this email already exists!", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(this, "Registration unsuccessful: ${exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }



//    private fun checkLoggedInState(textView: TextView){
//        if (auth.currentUser == null){
//            textView.text = "Not loggend in"
//        }else{
//            textView.text = "Logged in"
//        }
//    }
}