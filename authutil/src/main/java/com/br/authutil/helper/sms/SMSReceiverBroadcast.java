package com.br.authutil.helper.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.br.authutil.data.AuthData;
import com.br.authutil.data.AuthType;
import com.br.authutil.provider.AuthCallback;
import com.br.commonutils.validator.Validator;

class SMSReceiverBroadcast extends BroadcastReceiver {

    private AuthCallback authCallback;
    private String messageContains;

    public AuthCallback getAuthCallback() {
        return authCallback;
    }

    public void setAuthCallback(AuthCallback authCallback) {
        this.authCallback = authCallback;
    }

    public String getMessageContains() {
        return messageContains;
    }

    public void setMessageContains(String messageContains) {
        this.messageContains = messageContains;
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

                    authCallback.authSucceeded(AuthType.SMS_RECEIVER, authData);
                } else {
                    authCallback.authFailed("Message content does not match");
                }
            }
        }

        context.unregisterReceiver(this);
    }
}