package com.nt.wechat.ui.activity;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.support.v7.app.AppCompatActivity;

import com.nt.wechat.Config;
import com.nt.wechat.services.interfaces.IAccountManagerService;
import com.nt.wechat.services.WeChatService;

/**
 * Created by laoni on 2015/12/18.
 */
public abstract class BaseActivity extends AppCompatActivity {
    private WeChatService.WeChatBinder mBinder;
    public boolean xmppConnectionServiceBound = false;
    protected boolean registeredListeners = false;

    private long mLastUiRefresh = 0;
    private Handler mRefreshUiHandler = new Handler();
    private Runnable mRefreshUiRunnable = new Runnable() {
        @Override
        public void run() {
            mLastUiRefresh = SystemClock.elapsedRealtime();
            refreshUiReal();
        }
    };

    protected ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBinder = (WeChatService.WeChatBinder) service;
            xmppConnectionServiceBound = true;
            if (!registeredListeners && shouldRegisterListeners()) {
                registerListeners();
                registeredListeners = true;
            }
            onBackendConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            xmppConnectionServiceBound = false;
        }
    };

    abstract void onBackendConnected();

    public WeChatService.WeChatBinder getBinder() {
        return mBinder;
    }

    protected void registerListeners() {
    }

    protected void unregisterListeners() {
    }

    protected final void refreshUi() {
        final long diff = SystemClock.elapsedRealtime() - mLastUiRefresh;
        if (diff > Config.REFRESH_UI_INTERVAL) {
            mRefreshUiHandler.removeCallbacks(mRefreshUiRunnable);
            runOnUiThread(mRefreshUiRunnable);
        } else {
            final long next = Config.REFRESH_UI_INTERVAL - diff;
            mRefreshUiHandler.removeCallbacks(mRefreshUiRunnable);
            mRefreshUiHandler.postDelayed(mRefreshUiRunnable, next);
        }
    }

    abstract protected void refreshUiReal();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.mTheme = findTheme();
        //setTheme(this.mTheme);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!xmppConnectionServiceBound) {
            connectToBackend();
        } else {
            if (!registeredListeners) {
                this.registerListeners();
                this.registeredListeners = true;
            }
            this.onBackendConnected();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    protected boolean shouldRegisterListeners() {
        if  (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return !isDestroyed() && !isFinishing();
        } else {
            return !isFinishing();
        }
    }

    public void connectToBackend() {
        try {
            Intent intent = new Intent(this, WeChatService.class);
            intent.setAction("ui");
            startService(intent);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (xmppConnectionServiceBound) {
            if (registeredListeners) {
                this.unregisterListeners();
                this.registeredListeners = false;
            }
            unbindService(mConnection);
            xmppConnectionServiceBound = false;
        }
    }

    protected void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focus = getCurrentFocus();
        if (focus != null) {
            inputManager.hideSoftInputFromWindow(focus.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        }
        return super.onOptionsItemSelected(item);
    }

    protected SharedPreferences getPreferences() {
        return PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
    }

    public int getPixel(int dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return ((int) (dp * metrics.density));
    }

    public boolean copyTextToClipboard(String text, int labelResId) {
        ClipboardManager mClipBoardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        String label = getResources().getString(labelResId);
        if (mClipBoardManager != null) {
            ClipData mClipData = ClipData.newPlainText(label, text);
            mClipBoardManager.setPrimaryClip(mClipData);
            return true;
        }
        return false;
    }

}
