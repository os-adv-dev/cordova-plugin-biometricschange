package outsystems.experts.plugin.biometricschange;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.util.Log;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class BiometricChangeDetector extends CordovaPlugin {

    private static final String KEY_NAME = "biometric_key";
    private static final String KEYSTORE_NAME = "AndroidKeyStore";
    private static final String TAG = "BiometricChangeDetector";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("checkForBiometricChanges")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkForBiometricChanges(callbackContext);
            } else {
                callbackContext.error("Biometric features are not supported on this Android version.");
            }
            return true;
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkForBiometricChanges(CallbackContext callbackContext) {
        try {
            if (!isBiometricHardwareAvailable()) {
                callbackContext.error("Biometric hardware not available.");
                return;
            }

            SecretKey key = getKey();
            if (key == null) {
                generateKey();
                callbackContext.success("Key generated, no previous biometric data.");
                return;
            }

            try {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                cipher.init(Cipher.ENCRYPT_MODE, key);
                callbackContext.success("Biometrics unchanged.");
            } catch (KeyPermanentlyInvalidatedException e) {
                Log.d(TAG, "---  ✅ --- KeyPermanentlyInvalidatedException: " + e.getMessage());
                generateKey(); // Generate a new key to reset the state
                callbackContext.success("Biometrics have changed.");
            }
        } catch (Exception e) {
            Log.e(TAG, "---  ✅ --- Error checking biometric changes: " + e.getMessage(), e);
            callbackContext.error("Error checking biometric changes: " + e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void generateKey() throws Exception {
        Log.d(TAG, "---  ✅ --- Generating new key");
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_NAME);
        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                .setInvalidatedByBiometricEnrollment(true)
                .build();
        keyGenerator.init(keyGenParameterSpec);
        keyGenerator.generateKey();
        Log.d(TAG, "---  ✅ --- New key generated");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private SecretKey getKey() throws Exception {
        Log.d(TAG, "---  ✅ --- Getting key from KeyStore");
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_NAME);
        keyStore.load(null);
        SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
        Log.d(TAG, "---  ✅ --- Key: " + (key != null ? "exists" : "not found"));
        return key;
    }

    private boolean isBiometricHardwareAvailable() {
        BiometricManager biometricManager = BiometricManager.from(cordova.getActivity());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
            Log.d(TAG, "---  ✅ --- BiometricManager.canAuthenticate() result: " + result);
            return result == BiometricManager.BIOMETRIC_SUCCESS;
        } else {
            int result = biometricManager.canAuthenticate();
            Log.d(TAG, "---  ✅ --- BiometricManager.canAuthenticate() result: " + result);
            return result == BiometricManager.BIOMETRIC_SUCCESS;
        }
    }
}