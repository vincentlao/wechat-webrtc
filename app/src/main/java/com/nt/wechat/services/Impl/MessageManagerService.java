package com.nt.wechat.services.Impl;

import android.content.Intent;

import com.nt.wechat.entity.Account;
import com.nt.wechat.entity.Conversation;
import com.nt.wechat.entity.Message;
import com.nt.wechat.services.WeChatService;
import com.nt.wechat.services.interfaces.IAccountManagerService;
import com.nt.wechat.services.interfaces.IMessageManagerService;
import com.nt.wechat.ui.activity.ChatActivity;
import com.nt.wechat.ui.activity.VideoChatActivity;
import com.nt.wechat.util.CommonDefine;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by vincentlao on 2015/12/13.
 */
public class MessageManagerService implements IMessageManagerService, WeChatService.IAccountEvent {
    private WeChatService mService;
    private Map<String, ArrayList<Message>> mMapChatMessage = new HashMap<>();
    private HashSet<IMessageListener> mMessageListeners = new HashSet<>();
    private ArrayList<Conversation> mConversations = new ArrayList<>();

    private ChatMessageListener mChatMessageListener = new ChatMessageListener() {
        @Override
        public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
            saveMessage(chat.getParticipant(), chat.getParticipant(), mService.getAccountService().getAccount().getJid(), message.getBody());
            mService.getCoversationManagerService().addConversation(new Conversation(chat.getParticipant(), chat.getParticipant()));
            onReceiveMessage(chat.getParticipant(), message);
        }
    };

    public MessageManagerService(WeChatService service) {
        this.mService = service;
    }

    public void addMessageListener(IMessageManagerService.IMessageListener listener) {
        mMessageListeners.add(listener);
    }

    public void removeMessageListener(IMessageManagerService.IMessageListener listener) {
        mMessageListeners.remove(listener);
    }

    public void sendMessage(String toJid, String messageContent) throws SmackException.NotConnectedException {
        AbstractXMPPConnection connection = mService.getAccountService().getConnection();
        ChatManager chatmanager = ChatManager.getInstanceFor(connection);
        Chat newChat = chatmanager.createChat(toJid, mChatMessageListener);

        newChat.sendMessage(messageContent);
        saveMessage(toJid, mService.getAccountService().getAccount().getJid(), toJid, messageContent);

        mService.getCoversationManagerService().addConversation(new Conversation(toJid, toJid));
    }

    public ArrayList<Message> getHistoryMessage(String participantJid) {
        return mMapChatMessage.get(participantJid);
    }

    protected void saveMessage(String participantJid, String fromJid, String toJid, String message) {
        if (message == null ||  message.isEmpty()) {
            return;
        }

        try {
            JSONObject jsonMessage = new JSONObject(message);

            if (jsonMessage.has(CommonDefine.JSON_MESSAGE_TYPE) && jsonMessage.getInt(CommonDefine.JSON_MESSAGE_TYPE) == CommonDefine.MessageType_Text) {
                String msg_text = jsonMessage.getString(CommonDefine.JSON_MESSAGE_TEXT);
                if (mMapChatMessage.containsKey(participantJid)) {
                    mMapChatMessage.get(participantJid).add(new Message(fromJid, toJid, msg_text));
                } else {
                    ArrayList<Message> listMessage = new ArrayList<>();

                    listMessage.add(new Message(fromJid, toJid, msg_text));
                    mMapChatMessage.put(participantJid, listMessage);
                }
        }
        } catch (JSONException e){
            e.printStackTrace();
        }

    }

    protected void onReceiveMessage(String participantJid, org.jivesoftware.smack.packet.Message message) {
        try {
            String body = message.getBody();
            JSONObject jsonMessage = new JSONObject(body);

            if (jsonMessage.has(CommonDefine.JSON_MESSAGE_TYPE)) {
                if (jsonMessage.getInt(CommonDefine.JSON_MESSAGE_TYPE) == CommonDefine.MessageType_Video) {
                    if (jsonMessage.getInt(CommonDefine.JSON_MESSAGE_SUB_TYPE) == CommonDefine.MessageSubType_Offer) {
                        Intent intent = new Intent(mService, VideoChatActivity.class);
                        intent.putExtra(CommonDefine.JSON_MESSAGE_SUB_TYPE, jsonMessage.getInt(CommonDefine.JSON_MESSAGE_SUB_TYPE));
                        intent.putExtra(CommonDefine.JSON_JID, jsonMessage.getString(CommonDefine.JSON_JID));
                        intent.putExtra(CommonDefine.JSON_TYPE, jsonMessage.getString(CommonDefine.JSON_TYPE));
                        intent.putExtra(CommonDefine.JSON_SDP, jsonMessage.getString(CommonDefine.JSON_SDP));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mService.startActivity(intent);
                        return;
                    }
                }
            }
        } catch (JSONException e){
            e.printStackTrace();
        }


        for (IMessageListener listener : mMessageListeners) {
            listener.processMessage(participantJid, message);
        }
    }

    @Override
    public void onLogin(Account account) {
        AbstractXMPPConnection connection = mService.getAccountService().getConnection();
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addChatListener(new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean createdLocally) {
                if (!createdLocally) {
                    chat.addMessageListener(mChatMessageListener);
                }

                mService.getCoversationManagerService().addConversation(new Conversation(chat.getParticipant(), chat.getParticipant()));
            }
        });
    }

    @Override
    public void onLogout(Account account) {

    }
}
