package org.apache.cordova;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import android.util.Log;
import android.widget.Toast;
public class ToastPlugin extends CordovaPlugin {
    public static String TOAST = "toast";
    public boolean execute(String action, JSONArray data,
            CallbackContext callbackContext) throws JSONException {
        if (TOAST.equals(action)) {
            Log.i(TOAST, "message:"+data.getString(0)+",length:"+data.getInt(1));
            toast(data.getString(0), data.getInt(1), callbackContext);
        }
        return false;
    }
    public synchronized void toast(final String message, final int length,
            CallbackContext callbackContext) {
        final CordovaInterface cordova = this.cordova;
        Runnable runnable = new Runnable() {
            public void run() {
                Toast.makeText(cordova.getActivity(), message,length).show();
            }
        };
         this.cordova.getActivity().runOnUiThread(runnable);
    }
}