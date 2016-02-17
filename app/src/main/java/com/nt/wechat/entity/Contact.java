package com.nt.wechat.entity;

import android.content.ContentValues;

/**
 * Created by laoni on 2015/12/12.
 */
public class Contact extends AbstractEntity{
    private String displayName;
    private String jid;
    private boolean isOnline;

    public Contact(String jid, String displayName, boolean isOnline) {
        this.setJid(jid);
        this.setDisplayName(displayName);
        this.setIsOnline(isOnline);
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof Contact) {
            Contact otherContact = (Contact)o;
            return this.getJid() == otherContact.getJid();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return getJid().hashCode();
    }

    @Override
    public ContentValues getContentValues() {
        return null;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }
}
