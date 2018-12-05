package com.br.authutil.helper.sms;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;

import com.br.authutil.provider.AuthCallback;

public class SMSReceiver {

    private Context context;
    private AuthCallback authCallback;
    private String messageContains;
    
    private SMSReceiver(@NonNull Context context, @NonNull AuthCallback authCallback) {
        this.context = context;
        this.authCallback = authCallback;
    }

    @RequiresPermission(Manifest.permission.RECEIVE_SMS)
    public static SMSReceiver with(@NonNull Context context, @NonNull AuthCallback authCallback) {
        return new SMSReceiver(context, authCallback);
    }

    public SMSReceiver messageContains(@NonNull String messageContains) {
        this.messageContains = messageContains;
        return this;
    }

    public void verify() {
        SMSReceiverBroadcast smsReceiverBroadcast = new SMSReceiverBroadcast();
        smsReceiverBroadcast.setAuthCallback(authCallback);
        smsReceiverBroadcast.setMessageContains(messageContains);

        IntentFilter intentFilter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        intentFilter.setPriority(999);
        context.registerReceiver(smsReceiverBroadcast, intentFilter);
    }
}