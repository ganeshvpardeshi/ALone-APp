package com.example.alone

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.alone.signIn_singUp.LoginActivity
import com.example.alone.signIn_singUp.RegisterActivity

class GetStartedActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var btnSignup: Button

    //    main page start
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_get_started)
        btnLogin = findViewById(R.id.btnLogin)
        btnSignup = findViewById(R.id.btnSignup)

        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("AUTH_MODE", "LOGIN")
            startActivity(intent)
        }

        btnSignup.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("AUTH_MODE", "SIGNUP")
            startActivity(intent)
        }
    }
}