/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.pinlock.fingerprint;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;

/**
 * Fingerprint helper class
 */

public class FingerprintHelper {

    private static final String TAG = "FingerprintHelper";
    private static final String KEY_NAME = "calendula_pin_lock_key";

    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private Cipher cipher;
    private Context context;

    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintHandler handler;


    public FingerprintHelper(Context context) {
        this.context = context.getApplicationContext();
        keyguardManager = (KeyguardManager) this.context.getSystemService(Context.KEYGUARD_SERVICE);
        fingerprintManager = (FingerprintManager) this.context.getSystemService(Context.FINGERPRINT_SERVICE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startAuthentication(FingerprintCallbackAdapter adapter) {
        if (!canUseFingerPrint()) {
            LogUtil.e(TAG, "Cannot use fingerprint on this device");
            throw new IllegalStateException("Can't use fingerprint on this device!");
        }
        generateKey();
        if (cipherInit()) {
            cryptoObject = new FingerprintManager.CryptoObject(cipher);
            handler = new FingerprintHandler(context, adapter);
            handler.startAuth(fingerprintManager, cryptoObject);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void stop() {
        if (handler != null) {
            handler.stopAuth();
            handler = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean hasPermissions() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            LogUtil.d(TAG, "hasPermissions: Fingerprint authentication permission not enabled");
            return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean canUseFingerPrint() {
        return hasPermissions() && checkKeyguard() && fingerprintManager.hasEnrolledFingerprints();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean fingerPrintEnabled() {
        return PreferenceUtils.getBoolean(PreferenceKeys.FINGERPRINT_ENABLED, false);
    }

    private boolean checkKeyguard() {
        if (!keyguardManager.isKeyguardSecure()) {
            LogUtil.d(TAG, "checkKeyguard: Lock screen security not enabled in Settings");
            return false;
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get KeyGenerator instance", e);
        }

        try {
            keyStore.load(null);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public interface FingerprintCallbackAdapter {
        void onAuthenticationFailed();

        void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

        private CancellationSignal cancellationSignal;
        private Context appContext;
        private FingerprintCallbackAdapter adapter;

        public FingerprintHandler(Context context, FingerprintCallbackAdapter adapter) {
            appContext = context;
            this.adapter = adapter;
        }

        public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {

            cancellationSignal = new CancellationSignal();

            if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        }

        public void stopAuth() {
            if (cancellationSignal != null && !cancellationSignal.isCanceled()) {
                cancellationSignal.cancel();
            }
        }

        @Override
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
            Toast.makeText(appContext, errString, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
            Toast.makeText(appContext, helpString, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAuthenticationFailed() {
            adapter.onAuthenticationFailed();
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            adapter.onAuthenticationSucceeded(result);
        }
    }

}
