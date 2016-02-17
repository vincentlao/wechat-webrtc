package com.nt.wechat.entity;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by vincentlao on 2015/12/13.
 */
public class Message  extends AbstractEntity{
    public static final String TABLENAME = "messages";
    public static final String FROM_JID = "from_jid";
    public static final String TO_JID = "to_jid";
    public static final String MESSAGE = "message";

    private String mFromJid;
    private String mToJid;
    private String mMessage;

    public Message(String fromJid, String toJid, String message) {
        mFromJid = fromJid;
        mToJid = toJid;
        mMessage = message;
    }

    @Override
    public ContentValues getContentValues() {
        final ContentValues values = new ContentValues();
        values.put(FROM_JID, mFromJid);
        values.put(TO_JID, mToJid);
        values.put(MESSAGE, mMessage);
        return values;
    }

    public static Message fromCursor(Cursor cursor) {
        return new Message(cursor.getString(cursor.getColumnIndex(FROM_JID)),
                cursor.getString(cursor.getColumnIndex(TO_JID)),
                cursor.getString(cursor.getColumnIndex(MESSAGE)));
    }

    public String getFromJid() {
        return mFromJid;
    }

    public Message setParticipantJid(String mSenderJid) {
        this.mFromJid = mSenderJid;
        return this;
    }

    public String getMessage() {
        return mMessage;
    }

    public Message setMessage(String message) {
        this.mMessage = message;
        return this;
    }
}
