package com.cometchat.pro.androiduikit.ComponentFragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableArrayList
import androidx.fragment.app.Fragment
import com.cometchat.pro.androiduikit.R
import com.cometchat.pro.androiduikit.databinding.FragmentConversationListBinding
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.core.CometChat.CallbackListener
import com.cometchat.pro.core.ConversationsRequest
import com.cometchat.pro.core.ConversationsRequest.ConversationsRequestBuilder
import com.cometchat.pro.exceptions.CometChatException
import com.cometchat.pro.models.Conversation
import com.cometchat.pro.models.Group
import com.cometchat.pro.models.User
import com.cometchat.pro.uikit.ui_components.messages.message_list.CometChatMessageListActivity
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants
import com.cometchat.pro.uikit.ui_resources.utils.item_clickListener.OnItemClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ConversationListViewFragment : Fragment() {
    private var conversationBinding: FragmentConversationListBinding? = null
    private var conversationlist = ObservableArrayList<Conversation>()
    private var conversationsRequest: ConversationsRequest? = null
    private lateinit var sendAiMessageButton: Button

    // Retrofit instance for Wit.ai API
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.wit.ai/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val witAiApi = retrofit.create(WitAiApi::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        conversationBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_conversation_list, container, false)
        conversations

        // Programmatically create and add the button
        sendAiMessageButton = Button(requireContext())
        sendAiMessageButton.text = "Send AI Message"
        sendAiMessageButton.setOnClickListener { onClickSendAiMessage() }

        // Add the button to the layout (replace the parent layout ID accordingly)
        val aiButtonLayout = LayoutInflater.from(requireContext()).inflate(R.layout.activity_select, null)
        //val parentLayout = conversationBinding?.
       // parentLayout?.addView(sendAiMessageButton)

        conversationBinding!!.setConversationList(conversationlist)
        conversationBinding!!.cometchatConversationList.setItemClickListener(object :
            OnItemClickListener<Conversation>() {
            override fun OnItemClick(t: Any, position: Int) {
                val conversation = t as Conversation
                val intent = Intent(context, CometChatMessageListActivity::class.java)
                intent.putExtra(UIKitConstants.IntentStrings.TYPE, conversation.conversationType)
                if (conversation.conversationType == CometChatConstants.CONVERSATION_TYPE_GROUP) {
                    intent.putExtra(UIKitConstants.IntentStrings.NAME, (conversation.conversationWith as Group).name)
                    intent.putExtra(UIKitConstants.IntentStrings.GUID, (conversation.conversationWith as Group).guid)
                    intent.putExtra(UIKitConstants.IntentStrings.GROUP_OWNER, (conversation.conversationWith as Group).owner)
                    intent.putExtra(UIKitConstants.IntentStrings.AVATAR, (conversation.conversationWith as Group).icon)
                    intent.putExtra(UIKitConstants.IntentStrings.GROUP_TYPE, (conversation.conversationWith as Group).groupType)
                    intent.putExtra(UIKitConstants.IntentStrings.MEMBER_COUNT, (conversation.conversationWith as Group).membersCount)
                    intent.putExtra(UIKitConstants.IntentStrings.GROUP_DESC, (conversation.conversationWith as Group).description)
                    intent.putExtra(UIKitConstants.IntentStrings.GROUP_PASSWORD, (conversation.conversationWith as Group).password)
                } else {
                    intent.putExtra(UIKitConstants.IntentStrings.NAME, (conversation.conversationWith as User).name)
                    intent.putExtra(UIKitConstants.IntentStrings.UID, (conversation.conversationWith as User).uid)
                    intent.putExtra(UIKitConstants.IntentStrings.AVATAR, (conversation.conversationWith as User).avatar)
                    intent.putExtra(UIKitConstants.IntentStrings.STATUS, (conversation.conversationWith as User).status)
                }
                startActivity(intent)
            }
        })
        return conversationBinding!!.root
    }

    private val conversations: Unit
        private get() {
            if (conversationsRequest == null) {
                conversationsRequest = ConversationsRequestBuilder().setLimit(30).build()
            }
            conversationsRequest!!.fetchNext(object : CallbackListener<List<Conversation?>?>() {
                override fun onSuccess(conversations: List<Conversation?>?) {
                    conversationBinding!!.contactShimmer.stopShimmer()
                    conversationBinding!!.contactShimmer.visibility = View.GONE
                    conversationlist.addAll(conversations!!)
                }

                override fun onError(e: CometChatException) {
                    e.message?.let { Log.e("onError: ", it) }
                }
            })
        }

    // Function to send user message to Wit.ai
    private suspend fun sendMessageToWitAi(message: String) {
        if (message.isNotEmpty()) {
            try {
                val response = witAiApi.getMessage(message, getString(R.string.Y7YJQBGHOFX7S2OB64OGJM7G6XMS4YRB))
                processWitAiResponse(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Function to process Wit.ai response
    private fun processWitAiResponse(witAiResponse: WitAiResponse) {
        val intent = witAiResponse.entities?.get("intent")?.get(0)?.value
        val aiReply = when (intent) {
            "greeting" -> "Hello! How can I help you today?"
            "ask_weather" -> "I'm sorry, I don't know the weather right now."
            else -> "I didn't understand that. Can you please rephrase?"
        }

        // Display the AI-based reply in your chat UI
        displayMessage(aiReply, true)
    }

    // Function to display messages in your chat UI
    private fun displayMessage(message: String, isAi: Boolean) {
        // Your existing code to display messages in the chat UI
        // Add logic to differentiate between normal and AI messages
        if (isAi) {
            // Handle AI messages differently if needed
        } else {
            // Handle normal user messages
        }
    }

    private fun onClickSendAiMessage() {
        // Handle the AI message sending logic here
        CoroutineScope(Dispatchers.IO).launch {
            // Replace "Hello, how's the weather?" with the actual user message
            sendMessageToWitAi("Hello, how's the weather?")
        }
    }
}
