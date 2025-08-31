package com.example.aichatbot

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.Chat
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    val messageList = mutableStateListOf<MessageModel>()

//    private val generativeModel: GenerativeModel = GenerativeModel(
//        modelName = "gemini-pro",
//        apiKey = Constants.apikey
//    )
val generativeModel: GenerativeModel = GenerativeModel(
    modelName = "gemini-1.5-flash", // ✅ use valid model
    apiKey = Constants.apikey
)


    // Persistent chat session
    private var chat: Chat? = null

    init {
        chat = generativeModel.startChat(history = emptyList())
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun sendMessage(question: String) {
        if (question.isBlank()) return

        viewModelScope.launch {
            try {
                // Add user message
                messageList.add(MessageModel(question, "user"))

                // Show typing indicator
                messageList.add(MessageModel("Typing...", "model"))

                // Ensure chat exists
                if (chat == null) {
                    chat = generativeModel.startChat(
                        history = buildList {
                            messageList.forEach { message ->
                                add(content(message.role) {
                                    text(message.message)
                                })
                            }
                        }
                    )
                }

                // Send question to Gemini
                val response = chat?.sendMessage(question)

                // Remove typing safely
                if (messageList.isNotEmpty() && messageList.last().message == "Typing...") {
                    messageList.removeLast()
                }

                // Add model reply
                val replyText = response?.text ?: "⚠️ No response received."
                messageList.add(MessageModel(replyText, "model"))

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error: ${e.message}", e)

                if (messageList.isNotEmpty() && messageList.last().message == "Typing...") {
                    messageList.removeLast()
                }

                messageList.add(MessageModel("Error: ${e.message}", "model"))
            }
        }
    }
}




































/*
class ChatViewModel : ViewModel() {
    val messageList by lazy {
        mutableStateListOf<MessageModel>()
    }
    val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = Constants.apikey
    )

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun sendMessage(question: String) {
        viewModelScope.launch {
            try {
                val chat = generativeModel.startChat(
                    history = buildList {
                        messageList.forEach { message ->
                            add(content(message.role) {
                                text(message.message)
                            })
                        }
                    }
                )
                messageList.add(MessageModel(question, "user"))
                messageList.add(MessageModel("Typing...", "model"))
                val response = chat.sendMessage(question)
                messageList.removeLast()
                messageList.add(MessageModel(response.text.toString(), "model"))
            }catch (e: Exception){
                messageList.removeLast()
                messageList.add(MessageModel("Error: ${e.message}", "model"))
            }
        }
    }
}

 */