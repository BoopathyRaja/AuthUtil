package com.br.authutil;

import android.os.Bundle;

import com.br.commonutils.base.CUBasedActivity;
import com.br.commonutils.helper.toaster.Toaster;

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

    /*********************************************** BIO-METRIC ***********************************************/
    private void authenticateWithFingerprint() {
        try {
            FingerprintAuth.with(this, new AuthCallback() {
                @Override
                public void authSucceeded() {
                    Toaster.with(getApplicationContext())
                            .message("Authenticated")
                            .show();
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
            }).showDialog(true).authenticate();
        } catch (Exception e) {
            Toaster.with(getApplicationContext())
                    .message(e.getMessage())
                    .show();
        }
    }
}
