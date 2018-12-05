package com.br.authutil.helper.fingerprint;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;

import com.br.authutil.R;
import com.br.authutil.data.AuthData;
import com.br.authutil.data.AuthType;
import com.br.authutil.dialog.FingerprintDialog;
import com.br.authutil.provider.AuthCallback;
import com.br.commonutils.util.CommonUtil;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

@TargetApi(Build.VERSION_CODES.M)
public class Fingerprint extends FingerprintManagerCompat.AuthenticationCallback {

    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static FingerprintManagerCompat fingerprintManagerCompat;
    private Context context;
    private AuthCallback authCallback;
    private String key;
    private KeyStore keyStore;
    private Cipher cipher;

    private boolean showDialog;
    private FingerprintDialog fingerprintDialog;

    private Fingerprint(Context context, AuthCallback authCallback) throws Exception {
        super();
        this.context = context;
        this.authCallback = authCallback;
        this.key = CommonUtil.getUniqueKey();
        this.showDialog = true;

        generateKey();
        initCipher();
    }

    @RequiresPermission(anyOf = {Manifest.permission.USE_FINGERPRINT, Manifest.permission.USE_BIOMETRIC})
    public static Fingerprint with(@NonNull Context context, @NonNull AuthCallback authCallback) throws Exception {
        KeyguardManager keyguardManager = context.getSystemService(KeyguardManager.class);
        if (!keyguardManager.isKeyguardSecure())
            throw new Exception("Secure lock screen hasn't set up. Go to 'Settings -> Security -> Fingerprint' to set up a fingerprint");

        fingerprintManagerCompat = FingerprintManagerCompat.from(context);

        if (!fingerprintManagerCompat.isHardwareDetected())
            throw new Exception("Your Device does not have a Fingerprint Sensor");

        if (!fingerprintManagerCompat.hasEnrolledFingerprints())
            throw new Exception("Go to 'Settings -> Security -> Fingerprint' and register at least one fingerprint");

        return new Fingerprint(context, authCallback);
    }

    public Fingerprint showDialog(boolean showDialog) {
        this.showDialog = showDialog;
        return this;
    }

    @RequiresPermission(anyOf = {Manifest.permission.USE_FINGERPRINT, Manifest.permission.USE_BIOMETRIC})
    public void authenticate() {
        FingerprintManagerCompat.CryptoObject cryptoObject = new FingerprintManagerCompat.CryptoObject(cipher);
        CancellationSignal cancellationSignal = new CancellationSignal();

        fingerprintManagerCompat.authenticate(cryptoObject, 0, cancellationSignal, Fingerprint.this, null);
        showDialogIfNeeded();
    }

    private void generateKey() throws Exception {
        keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(key, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
        keyGenerator.init(builder.build());
        keyGenerator.generateKey();
    }

    private void initCipher() throws Exception {
        cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        keyStore.load(null);

        SecretKey secretKey = (SecretKey) keyStore.getKey(key, null);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    }

    private void showDialogIfNeeded() {
        if (showDialog) {
            FingerprintDialog.Builder builder = new FingerprintDialog.Builder(context);
            builder.icon(R.drawable.ic_fingerprint);
            builder.title("One Touch Sign In");
            builder.subTitle("Please place your fingertip on the scanner to verify");
            builder.description("");
            builder.buttonText("Cancel");
            builder.dialogNegativeCallback(() -> {
                authCallback.authCancelled();
                fingerprintDialog.dismiss();
            });

            fingerprintDialog = builder.build();
            fingerprintDialog.show();
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        super.onAuthenticationError(errMsgId, errString);

        fingerprintDialog.setDescription(errString.toString());
        fingerprintDialog.invalidate();

        authCallback.authFailed(errString.toString());
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        super.onAuthenticationHelp(helpMsgId, helpString);

        fingerprintDialog.setDescription(helpString.toString());
        fingerprintDialog.invalidate();

        authCallback.authFailed(helpString.toString());
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);

        fingerprintDialog.dismiss();
        authCallback.authSucceeded(AuthType.FINGER_PRINT, new AuthData());
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();

        fingerprintDialog.setDescription("Authentication Failed");
        fingerprintDialog.invalidate();

        authCallback.authFailed("Authentication Failed");
    }
}
