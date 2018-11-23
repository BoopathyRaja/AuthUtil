package com.br.authutil.helper.sms;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.telephony.SmsMessage;

import com.br.authutil.data.AuthData;
import com.br.authutil.data.AuthType;
import com.br.authutil.provider.AuthCallback;
import com.br.commonutils.validator.Validator;

public class SMSReceiver extends BroadcastReceiver {

    private Context context;
    private AuthCallback authCallback;
    private String messageContains;
    private boolean autoUnregister;

    private SMSReceiver(@NonNull Context context, @NonNull AuthCallback authCallback) {
        this.context = context;
        this.authCallback = authCallback;
        this.autoUnregister = true;
    }

    @RequiresPermission(Manifest.permission.RECEIVE_SMS)
    public static SMSReceiver with(@NonNull Context context, @NonNull AuthCallback authCallback) {
        return new SMSReceiver(context, authCallback);
    }

    public static void unregister(@NonNull Context context, @NonNull SMSReceiver smsReceiver) {
        context.unregisterReceiver(smsReceiver);
    }

    public SMSReceiver messageContains(@NonNull String messageContains) {
        this.messageContains = messageContains;
        return this;
    }

    public SMSReceiver autoUnregister(@NonNull boolean autoUnregister) {
        this.autoUnregister = autoUnregister;
        return this;
    }

    public void register() {
        IntentFilter intentFilter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        intentFilter.setPriority(999);
        context.registerReceiver(this, intentFilter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (Validator.isValid(bundle)) {
            Object[] pdus = (Object[]) bundle.get("pdus");

            SmsMessage[] messages = new SmsMessage[pdus.length];
            for (int i = 0; i < pdus.length; i++)
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

            for (final SmsMessage message : messages) {
                String from = message.getOriginatingAddress();
                long when = message.getTimestampMillis();
                String msg = message.getMessageBody();

                if (msg.contains(messageContains)) {
                    AuthData authData = new AuthData();
                    authData.add("from", from);
                    authData.add("when", when);
                    authData.add("message", msg);

                    authCallback.authSucceeded(AuthType.SMS_RECEIVER, authData, SMSReceiver.this);
                } else {
                    authCallback.authFailed("Message content does not match");
                }
            }
        }

        if (autoUnregister)
            unregister(context, this);
    }
}