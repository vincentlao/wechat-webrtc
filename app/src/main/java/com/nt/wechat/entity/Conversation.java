package com.nt.wechat.entity;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by laoni on 2015/12/12.
 */
public class Conversation extends AbstractEntity{
    public static final String TABLENAME = "conversations";
    public static final String JID = "jid";
    public static final String NAME = "name";

    private String contactJid;
    private String name;

    public Conversation(String contactJid, String contactNmae) {
        setContact(contactJid);
        setContactName(contactNmae);
    }

    public String getContact() {
        return contactJid;
    }

    public void setContactName(String contactName) {
        this.name = contactName;
    }

    public String getContactName() {
        return name;
    }

    public void setContact(String contact) {
        this.contactJid = contact;
    }

    @Override
    public ContentValues getContentValues() {
        final ContentValues values = new ContentValues();
        values.put(Conversation.JID, contactJid);
        values.put(Conversation.NAME, name);
        return values;
    }

    public static Conversation fromCursor(Cursor cursor) {
        return new Conversation(cursor.getString(cursor.getColumnIndex(JID)), cursor.getString(cursor.getColumnIndex(NAME)));
    }
}
