package com.example.healthcare

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import android.widget.*
import android.content.Intent
class MainActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val goSignup = findViewById<TextView>(R.id.goSignup)
        goSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        loginBtn.setOnClickListener {
            val userEmail = email.text.toString().trim()
            val userPass = password.text.toString().trim()

            if (userEmail.isEmpty() || userPass.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // UI feedback
            loginBtn.text = "Logging in..."
            loginBtn.isEnabled = false

            auth.signInWithEmailAndPassword(userEmail, userPass)
                .addOnCompleteListener {
                    // restore button
                    loginBtn.text = "Login"
                    loginBtn.isEnabled = true

                    if (it.isSuccessful) {
                        Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show()

                        //  clear back stack
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)

                    } else {
                        Toast.makeText(
                            this,
                            it.exception?.message ?: "Login Failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}