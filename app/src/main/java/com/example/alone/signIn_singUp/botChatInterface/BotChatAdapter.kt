package com.example.alone.signIn_singUp.botChatInterface

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alone.R

private const val VIEW_TYPE_BOT = 1
private const val VIEW_TYPE_USER = 2
private const val VIEW_TYPE_TYPING = 3

class ChatAdapter(
    private val messages: MutableList<BotChatMessage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        val msg = messages[position]
        return when {
            msg.isTyping -> VIEW_TYPE_TYPING
            msg.isBot -> VIEW_TYPE_BOT
            else -> VIEW_TYPE_USER
        }
    }

    fun updateMessageText(position: Int, newText: String) {
        if (position < 0 || position >= messages.size) return
        val old = messages[position]
        messages[position] = old.copy(text = newText)
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_BOT -> {
                val v = inflater.inflate(R.layout.item_message_bot, parent, false)
                BotViewHolder(v)
            }
            VIEW_TYPE_USER -> {
                val v = inflater.inflate(R.layout.item_message_user, parent, false)
                UserViewHolder(v)
            }
            else -> {
                val v = inflater.inflate(R.layout.item_typing_indicator, parent, false)
                TypingViewHolder(v)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        when (holder) {
            is BotViewHolder -> holder.bind(msg)
            is UserViewHolder -> holder.bind(msg)
            is TypingViewHolder -> holder.bind(msg)
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(msg: BotChatMessage) {
        messages.add(msg)
        notifyItemInserted(messages.size - 1)
    }

    fun removeTypingIndicator() {
        val index = messages.indexOfLast { it.isTyping }
        if (index != -1) {
            messages.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    class BotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv: TextView = itemView.findViewById(R.id.tvMessageBot)
        fun bind(message: BotChatMessage) {
            tv.text = message.text
        }
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv: TextView = itemView.findViewById(R.id.tvMessageUser)
        fun bind(message: BotChatMessage) {
            tv.text = message.text
        }
    }

    // ✅ UPDATED: No TextView needed - animations are automatic
    class TypingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(@Suppress("UNUSED_PARAMETER") message: BotChatMessage) {
            // Animations start automatically from XML
            // No code needed!
        }
    }
}
