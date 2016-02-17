package com.nt.wechat.entity;

import android.content.ContentValues;

/**
 * Created by laoni on 2015/12/18.
 */
public class Account extends AbstractEntity {
    public static enum State {
        OFFLINE,
        CONNECTING,
        ONLINE;
    }

    public static final String TABLENAME = "account";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String DISPLAY_NAME = "display_name";
    public static final String STATE = "state";

    private String jid;
    private String password;
    private String displayName;
    private State state;

    public Account(String jid, String password) {
        this.setJid(jid);
        this.setPassword(password);
        this.setDisplayName(null);
        this.setState(State.OFFLINE);
    }

    @Override
    public ContentValues getContentValues() {
        return null;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
