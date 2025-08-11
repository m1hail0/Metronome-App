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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogInActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginBtn: Button
    private lateinit var guestBtn: Button
    private lateinit var register: TextView


    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_layout)


        register = findViewById(R.id.go_register_btn)

        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        loginBtn = findViewById(R.id.login_btn)
        guestBtn = findViewById(R.id.guest_btn)

        auth = FirebaseAuth.getInstance()

        // Goes automatically to the MainActivity if the user is already logged in
        if (auth.currentUser != null) {
            val i = Intent(this@LogInActivity, MetronomeActivity::class.java)
            startActivity(i)
            Toast.makeText(this@LogInActivity, "You are already logged in!", Toast.LENGTH_SHORT).show()
            finish()
        }

        loginBtn.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            Log.i("Test Credentials", "Username: $email and Password: $password")

            loginUser(email, password)
        }

        register.setOnClickListener {
            val i = Intent(this@LogInActivity, RegisterActivity::class.java)
            startActivity(i)
        }

        guestBtn.setOnClickListener{
            Toast.makeText(this@LogInActivity, "Logged in as a GUEST", Toast.LENGTH_SHORT).show()
            val i = Intent(this@LogInActivity, MetronomeActivity::class.java)
            startActivity(i)
        }

    }

    private fun loginUser(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val i = Intent(this@LogInActivity, MetronomeActivity::class.java)
                                startActivity(i)
                                finish()
                            }
                        }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.d("TESTIRANJE", e.message.toString())
                        Toast.makeText(this@LogInActivity, "Sign in not successful", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


}