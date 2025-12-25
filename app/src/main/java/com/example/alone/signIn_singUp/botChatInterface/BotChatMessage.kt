package com.example.alone.signIn_singUp.botChatInterface

data class BotChatMessage(
    val text: String,
    val isBot: Boolean,
    val isTyping: Boolean = false   // true only for typing indicator
)
