package com.nt.wechat.services.interfaces;

import com.nt.wechat.entity.Account;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;

/**
 * Created by laoni on 2015/12/17.
 */
public interface IAccountManagerService {
    public AbstractXMPPConnection getConnection();
    public void login(String jid,String password) throws IOException, XMPPException, SmackException;
    public void logout();
    public boolean isConnected();
    public Account getAccount();
}
