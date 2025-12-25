package com.example.alone.signIn_singUp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.alone.R
import com.example.alone.signIn_singUp.botChatInterface.BotChatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        val intent = Intent(this, BotChatActivity::class.java)
        intent.putExtra("AUTH_MODE", "LOGIN")
        startActivity(intent)

    }
}