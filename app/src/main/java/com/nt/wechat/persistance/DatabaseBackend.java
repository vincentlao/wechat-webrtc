package com.nt.wechat.persistance;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nt.wechat.entity.Conversation;

import java.util.concurrent.CopyOnWriteArrayList;

public class DatabaseBackend extends SQLiteOpenHelper {

	private static String DATABASE_NAME_PREFIX = "history";
	private static final int DATABASE_VERSION = 1;

	private static String CREATE_CONVERSATION_STATEMENT = "create table "
			+ Conversation.TABLENAME + "(" + Conversation.JID +
			" TEXT, " + Conversation.NAME  + " TEXT);";

	public DatabaseBackend(Context context, String jid) {
		super(context, DATABASE_NAME_PREFIX + "-" + jid, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("PRAGMA foreign_keys=ON;");
		db.execSQL(CREATE_CONVERSATION_STATEMENT);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public void createConversation(Conversation conversation) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.insert(Conversation.TABLENAME, null, conversation.getContentValues());
	}

	public int getConversationCount() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("select count(jid) as count from "
				+ Conversation.TABLENAME, null);
		cursor.moveToFirst();
		int count = cursor.getInt(0);
		cursor.close();
		return count;
	}

	public CopyOnWriteArrayList<Conversation> getConversations() {
		CopyOnWriteArrayList<Conversation> list = new CopyOnWriteArrayList<>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from " + Conversation.TABLENAME, null);
		while (cursor.moveToNext()) {
			list.add(Conversation.fromCursor(cursor));
		}
		cursor.close();
		return list;
	}

}
