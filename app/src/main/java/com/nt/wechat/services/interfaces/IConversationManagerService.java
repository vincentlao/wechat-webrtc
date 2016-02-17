package com.nt.wechat.services.interfaces;

import com.nt.wechat.entity.Conversation;

import java.util.ArrayList;

/**
 * Created by laoni on 2015/12/22.
 */
public interface IConversationManagerService {
    public ArrayList<Conversation> getConversation();
    public void addConversation(Conversation conversation);
    public void removeConversation(String jid);
    public boolean hasConversation(String jid);
}
