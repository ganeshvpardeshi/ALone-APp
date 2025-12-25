package com.example.alone.chatInterface

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alone.R
import com.example.alone.beConnection.ApiClient
import com.example.alone.model.ModerateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var editMessage: EditText
    private lateinit var buttonSend: Button
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)


        editMessage = findViewById(R.id.editMessage)
        buttonSend = findViewById(R.id.buttonSend)

        recycler = findViewById(R.id.recyclerMessages)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = ChatAdapter(mutableListOf())
        recycler.adapter = adapter


        buttonSend.setOnClickListener {
            val text = editMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                checkAndSendMessage(text)
            }
        }
    }


    private fun checkAndSendMessage(text: String) {
        buttonSend.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.moderationApi.moderate(
                    ModerateRequest(text = text)
                )

                withContext(Dispatchers.Main) {
                    buttonSend.isEnabled = true
                    if (response.status == "ALLOW") {
                        adapter.addMessage(ChatMessage(text))
                        recycler.scrollToPosition(adapter.itemCount - 1)
                        editMessage.text.clear()
                    } else {
                        showBlockedToast(response.reason, "")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    buttonSend.isEnabled = true

                    showBlockedToast("connection_error : ", e.message)
                }
            }
        }
    }

    private fun showBlockedToast(reason: String?, errMsg: String?) {
        val msg = when (reason) {
            "personal_info"   -> "For your safety, contact and social IDs are blocked."
            "toxic_content"   -> "Please keep the conversation respectful."
            "connection_error"-> "Could not check your message, try again."
            else              -> "This message cannot be sent."
        }
        Log.d( " Error Message : " , errMsg.toString())
        Toast.makeText(this, msg , Toast.LENGTH_SHORT).show()
    }


}
