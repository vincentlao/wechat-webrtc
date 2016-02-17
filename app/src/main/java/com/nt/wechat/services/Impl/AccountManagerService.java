package com.nt.wechat.services.Impl;

import com.nt.wechat.entity.Account;
import com.nt.wechat.services.WeChatService;
import com.nt.wechat.services.interfaces.IAccountManagerService;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.io.IOException;
import java.util.HashSet;

/**
 * Created by laoni on 2015/12/18.
 */
public class AccountManagerService implements IAccountManagerService {
    private AbstractXMPPConnection mConnection;
    private WeChatService mService;
    private Account mAccount;

    public AccountManagerService(WeChatService service) {
        this.mService = service;
    }

    public AbstractXMPPConnection getConnection() {
        return mConnection;
    }

    public boolean isConnected() {
        return mConnection != null;
    }

    public void login(String jid,String password) throws IOException, XMPPException, SmackException {
        logout();
        mAccount = new Account(jid, password);
        try {
            mConnection = new XMPPTCPConnection(jid, password);
            mConnection.setPacketReplyTimeout(10 * 1000);
            mConnection.connect();
            mConnection.login();
            mAccount.setState(Account.State.ONLINE);
            mService.onLogin(mAccount);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void logout() {
        if (mConnection != null) {
            mConnection.disconnect();
            mConnection = null;
        }

        if (mAccount != null) {
            mAccount.setState(Account.State.OFFLINE);
        }

        mService.onLogout(mAccount);
    }

    public Account getAccount() {
        return mAccount;
    }
}
