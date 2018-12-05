package com.br.authutil.helper.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.br.authutil.data.AuthData;
import com.br.authutil.data.AuthType;
import com.br.authutil.provider.AuthCallback;
import com.br.commonutils.validator.Validator;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

class SMSRetrieverBroadcast extends BroadcastReceiver {

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
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (Validator.isValid(bundle)) {
                Status status = (Status) bundle.get(SmsRetriever.EXTRA_STATUS);
                switch (status.getStatusCode()) {
                    case CommonStatusCodes.SUCCESS: {
                        String message = (String) bundle.get(SmsRetriever.EXTRA_SMS_MESSAGE);

                        if (Validator.isValid(authCallback)) {
                            if (message.contains(messageContains)) {
                                AuthData authData = new AuthData();
                                authData.add("message", message);

                                authCallback.authSucceeded(AuthType.SMS_RETRIEVER, authData, SMSRetrieverBroadcast.this);
                            } else {
                                authCallback.authFailed("Message content does not match");
                            }
                        }
                    }
                    break;

                    case CommonStatusCodes.TIMEOUT: {
                        if (Validator.isValid(authCallback))
                            authCallback.authTimeout();
                    }
                    break;
                }
            }
        }

        context.unregisterReceiver(this);
    }
}