package com.nt.wechat.services.interfaces;

import com.nt.wechat.entity.Message;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;

import java.util.ArrayList;

/**
 * Created by laoni on 2015/12/20.
 */
public interface IMessageManagerService {
    public interface IMessageListener {
        public void processMessage(String senderJid, org.jivesoftware.smack.packet.Message message);
    }

    public void addMessageListener(IMessageManagerService.IMessageListener listener);
    public void removeMessageListener(IMessageManagerService.IMessageListener listener);
    public void sendMessage(String toJid, String messageContent) throws SmackException.NotConnectedException;
    public ArrayList<Message> getHistoryMessage(String participantJid);
}
