package com.br.authutil.helper.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

import com.br.authutil.provider.AuthCallback;
import com.br.commonutils.base.CUBasedActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;

public class SMSRetriever {

    private Activity activity;
    private AuthCallback authCallback;
    private String messageContains;
    private boolean requestHint;

    private SMSRetriever(@NonNull Activity activity, @NonNull AuthCallback authCallback) {
        this.activity = activity;
        this.authCallback = authCallback;
        this.requestHint = true;
    }

    public static <T extends CUBasedActivity> SMSRetriever with(@NonNull T activity, @NonNull AuthCallback authCallback) {
        return new SMSRetriever(activity, authCallback);
    }

    public SMSRetriever messageContains(@NonNull String messageContains) {
        this.messageContains = messageContains;
        return this;
    }

    public SMSRetriever requestHint(@NonNull boolean requestHint) {
        this.requestHint = requestHint;
        return this;
    }

    public void verify() {
        if (requestHint) {
            try {
                GoogleApiClient googleApiClient = new GoogleApiClient.Builder(activity).addApi(Auth.CREDENTIALS_API).build();
                HintRequest hintRequest = new HintRequest.Builder().setPhoneNumberIdentifierSupported(true).build();

                PendingIntent pendingIntent = Auth.CredentialsApi.getHintPickerIntent(googleApiClient, hintRequest);
                activity.startIntentSenderForResult(pendingIntent.getIntentSender(), CUBasedActivity.REQUEST_CODE_SMS_RETRIEVER, null, 0, 0, 0);
            } catch (Exception e) {
                authCallback.authFailed(e.getMessage());
            }
        }

        SmsRetrieverClient smsRetrieverClient = SmsRetriever.getClient(activity);
        Task<Void> task = smsRetrieverClient.startSmsRetriever();
        task.addOnSuccessListener(aVoid -> {
            SMSRetrieverBroadcast smsRetrieverBroadcast = new SMSRetrieverBroadcast();
            smsRetrieverBroadcast.setAuthCallback(authCallback);
            smsRetrieverBroadcast.setMessageContains(messageContains);

            IntentFilter intentFilter = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
            intentFilter.setPriority(999);
            activity.registerReceiver(smsRetrieverBroadcast, intentFilter);
        });

        task.addOnFailureListener(e -> authCallback.authFailed(e.getMessage()));
    }
}