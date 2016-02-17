package com.nt.wechat.services.Impl;

import android.widget.CalendarView;

import com.nt.wechat.entity.Account;
import com.nt.wechat.entity.Contact;
import com.nt.wechat.entity.Conversation;
import com.nt.wechat.persistance.DatabaseBackend;
import com.nt.wechat.services.WeChatService;
import com.nt.wechat.services.interfaces.IConversationManagerService;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by laoni on 2015/12/22.
 */
public class ConversationManagerService implements IConversationManagerService, WeChatService.IAccountEvent {
    private ArrayList<Conversation> conversations = new ArrayList<>();
    private boolean hasLoadConversation = false;
    private WeChatService mService;

    public ConversationManagerService(WeChatService service) {
        this.mService = service;
    }

    @Override
    public ArrayList<Conversation> getConversation() {
        loadConversations(false);
        return conversations;
    }

    @Override
    public void addConversation(Conversation conversation) {
        loadConversations(false);

        DatabaseBackend db = mService.getDatabase();
        if (db == null) {
            return;
        }

        if (hasConversation(conversation.getContact())) {
            return;
        }

        db.createConversation(conversation);
        conversations.add(conversation);
    }

    @Override
    public void removeConversation(String jid) {
        loadConversations(false);

        DatabaseBackend db = mService.getDatabase();
        if (db == null) {
            return;
        }

        //remove from db

        for (Conversation c : conversations) {
            if (c.getContact().equals(jid)) {
                conversations.remove(c);
                return;
            }
        }
    }

    @Override
    public boolean hasConversation(String jid) {
        for (Conversation c : conversations) {
            if (c.getContact().equals(jid)) {
                return true;
            }
        }
        return false;
    }

    public void loadConversations(boolean forces) {
        if (hasLoadConversation && !forces) {
            return;
        }

        DatabaseBackend db = mService.getDatabase();
        if (db == null) {
            return;
        }
        CopyOnWriteArrayList<Conversation> convers = db.getConversations();
        if (convers != null) {
            conversations.addAll(convers);
        }

        hasLoadConversation = true;
    }

    @Override
    public void onLogin(Account account) {
        loadConversations(true);
    }

    @Override
    public void onLogout(Account account) {
        conversations.clear();
    }
}
