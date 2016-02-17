package com.nt.wechat.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.nt.wechat.entity.Account;
import com.nt.wechat.persistance.DatabaseBackend;
import com.nt.wechat.receiver.SystemEventReceiver;
import com.nt.wechat.services.Impl.AccountManagerService;
import com.nt.wechat.services.Impl.ContactManagerService;
import com.nt.wechat.services.Impl.ConversationManagerService;
import com.nt.wechat.services.Impl.MessageManagerService;
import com.nt.wechat.services.interfaces.IAccountManagerService;
import com.nt.wechat.services.interfaces.IContactManagerService;
import com.nt.wechat.services.interfaces.IConversationManagerService;
import com.nt.wechat.services.interfaces.IMessageManagerService;

import java.util.ArrayList;

public class WeChatService extends Service {
    public interface IAccountEvent {
        public void onLogin(Account account);
        public void onLogout(Account account);
    }

    private final IBinder mBinder = new WeChatBinder();
    private SystemEventReceiver mEventReceiver = new SystemEventReceiver();

    private AccountManagerService mAccountService;
    private ContactManagerService mContactManagerService;
    private MessageManagerService mMessageManagerService;
    private ConversationManagerService mConversationManagerService;
    private DatabaseBackend mDatabase;
    private ArrayList mServices = new ArrayList();

    public WeChatService() {
        mAccountService = new AccountManagerService(this);
        mContactManagerService = new ContactManagerService(this);
        mMessageManagerService = new MessageManagerService(this);
        mConversationManagerService = new ConversationManagerService(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String action = intent == null ? null : intent.getAction();
        boolean interactive = false;
        if (action != null) {
            switch (action) {
            }
        }

        return START_STICKY;
    }

    @SuppressLint("TrulyRandom")
    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(this.mEventReceiver);
        } catch (IllegalArgumentException e) {
            //ignored
        }
        super.onDestroy();
    }

    public SharedPreferences getPreferences() {
        return PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
    }

    private boolean awayWhenScreenOff() {
        return getPreferences().getBoolean("away_when_screen_off", false);
    }

    public void toggleScreenEventReceiver() {
        if (awayWhenScreenOff()) {
            final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(this.mEventReceiver, filter);
        } else {
            try {
                unregisterReceiver(this.mEventReceiver);
            } catch (IllegalArgumentException e) {
                //ignored
            }
        }
    }

    public void onLogin(Account account) {
        if(mDatabase != null) {
            mDatabase.close();
        }

        mDatabase = new DatabaseBackend(getApplicationContext(), account.getJid());

        mContactManagerService.onLogin(account);
        mMessageManagerService.onLogin(account);
        mConversationManagerService.onLogin(account);
    }

    public void onLogout(Account account) {
        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }

        mContactManagerService.onLogout(account);
        mMessageManagerService.onLogout(account);
        mConversationManagerService.onLogout(account);
    }

    public DatabaseBackend getDatabase() {
        return mDatabase;
    }

    public AccountManagerService getAccountService() {
        return mAccountService;
    }

    public ContactManagerService getContactManagerService() {
        return mContactManagerService;
    }

    public MessageManagerService getMessageManagerService() {
        return mMessageManagerService;
    }

    public ConversationManagerService getCoversationManagerService() {
        return mConversationManagerService;
    }

    @SuppressWarnings("unchecked")
    public class WeChatBinder extends Binder {
        public IAccountManagerService getAccountManagerService() {
            return WeChatService.this.getAccountService();
        }

        public IContactManagerService getContactManagerService() {
            return WeChatService.this.getContactManagerService();
        }

        public IMessageManagerService getMessageManagerService() {
            return WeChatService.this.getMessageManagerService();
        }

        public IConversationManagerService getConversationManagerService() {
            return WeChatService.this.getCoversationManagerService();
        }
    }
}
