package com.nt.wechat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nt.wechat.services.WeChatService;

public class SystemEventReceiver extends BroadcastReceiver {
    public SystemEventReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentForService = new Intent(context, WeChatService.class);
        intentForService.setAction(intent.getAction());
        context.startService(intentForService);
    }
}
