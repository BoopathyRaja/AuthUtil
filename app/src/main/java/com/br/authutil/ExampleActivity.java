package com.br.authutil;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.br.authutil.data.AuthData;
import com.br.authutil.data.AuthType;
import com.br.authutil.helper.fingerprint.Fingerprint;
import com.br.authutil.helper.sms.SMSReceiver;
import com.br.authutil.helper.sms.SMSRetriever;
import com.br.authutil.provider.AuthCallback;
import com.br.commonutils.base.CUBasedActivity;
import com.br.commonutils.base.permission.PermissionHandler;
import com.br.commonutils.data.permission.DangerousPermission;
import com.br.commonutils.helper.toaster.Toaster;
import com.br.commonutils.util.CommonUtil;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ExampleActivity extends CUBasedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);

        init();
    }

    @Override
    public void init() {
        authenticateWithFingerprint();
    }

    @Override
    public void extra(Object data, Type type, String... extras) {

    }

    /*********************************************** BIO-METRIC ***********************************************/
    private void authenticateWithFingerprint() {
        try {
            Fingerprint.with(this, new AuthCallback() {
                @Override
                public void authSucceeded(AuthType authType, AuthData authData, Object... extras) {
                    Toaster.with(getApplicationContext())
                            .message("Authenticated " + authType.name())
                            .show();

                    checkForSMSPermission();
                }

                @Override
                public void authFailed(String error) {
                    Toaster.with(getApplicationContext())
                            .message(error)
                            .show();
                }

                @Override
                public void authCancelled() {
                    Toaster.with(getApplicationContext())
                            .message("Cancelled")
                            .show();
                }

                @Override
                public void authTimeout() {
                    Toaster.with(getApplicationContext())
                            .message("Timeout")
                            .show();
                }
            }).showDialog(true).authenticate();
        } catch (Exception e) {
            Toaster.with(getApplicationContext())
                    .message(e.getMessage())
                    .show();

            checkForSMSPermission();
        }
    }

    private void checkForSMSPermission() {
        requestPermission(CommonUtil.asList(DangerousPermission.RECEIVE_SMS), new PermissionHandler() {
            @Override
            public void result(List<DangerousPermission> granted, List<DangerousPermission> denied) {
                if (granted.contains(DangerousPermission.RECEIVE_SMS))
                    smsReceiver();
                else
                    smsRetriever();
            }

            @Override
            public boolean permissionRationale() {
                return false;
            }

            @Override
            public void permissionRationaleFor(List<DangerousPermission> dangerousPermissions) {

            }

            @Override
            public void info(String message) {

            }
        });
    }

    private void smsReceiver() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED)
            return;

        SMSReceiver.with(this, new AuthCallback() {
            @Override
            public void authSucceeded(AuthType authType, AuthData authData, Object... extras) {
                switch (authType) {
                    case SMS_RECEIVER: {
                        Map<String, Object> data = authData.getData();
                        String from = (String) data.get("from");
                        String when = (String) data.get("when");
                        String message = (String) data.get("message");
                    }
                    break;
                }
            }

            @Override
            public void authFailed(String error) {

            }

            @Override
            public void authCancelled() {

            }

            @Override
            public void authTimeout() {

            }
        }).messageContains("Your activation code is ").verify();
    }

    private void smsRetriever() {
        SMSRetriever.with(this, new AuthCallback() {
            @Override
            public void authSucceeded(AuthType authType, AuthData authData, Object... extras) {
                switch (authType) {
                    case SMS_RETRIEVER: {
                        Map<String, Object> data = authData.getData();
                        String from = (String) data.get("from");
                        String when = (String) data.get("when");
                        String message = (String) data.get("message");
                    }
                    break;
                }
            }

            @Override
            public void authFailed(String error) {

            }

            @Override
            public void authCancelled() {

            }

            @Override
            public void authTimeout() {

            }
        }).requestHint(true).messageContains("Your activation code is ").verify();
    }
}
