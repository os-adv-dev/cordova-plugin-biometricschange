package outsystems.experts.plugin.biometricschange;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.biometrics.BiometricManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class BiometricsChange extends CordovaPlugin {

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("biometricsChanged")) {
            //String message = args.getString(0);
            //this.coolMethod(message, callbackContext);

            checkForBiometricChanges();
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void checkForBiometricChanges() {
        BiometricManager biometricManager = (BiometricManager) cordova.getActivity().getSystemService(Context.BIOMETRIC_SERVICE);
        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);

        SharedPreferences sharedPreferences = cordova.getActivity().getSharedPreferences("biometric_prefs", Context.MODE_PRIVATE);
        int previousState = sharedPreferences.getInt("biometric_state", BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED);

        if (previousState != canAuthenticate) {
            // Biometric state has changed
            onBiometricStateChanged(canAuthenticate);
            sharedPreferences.edit().putInt("biometric_state", canAuthenticate).apply();
        }
    }

    private void onBiometricStateChanged(int newState) {
        switch (newState) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                // Biometrics are now available
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // No biometric features are enrolled
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                // Biometric hardware is unavailable
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                // No biometric hardware
                break;
        }
    }
}
