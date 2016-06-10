package org.apache.cordova.filemanager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import java.util.HashMap;

/**
 * Created by manojkumar on 5/30/16.
 */
public class PendingRequests {

    private static HashMap<String, CallbackContext> callbackQueue = new HashMap<String, CallbackContext>();

    public static void put(String key, CallbackContext callbackContext) {
        put(key, callbackContext, true);
    }

    public static void put(String key, CallbackContext callbackContext, boolean send) {
        // remove if any
        remove(key);
        // add to map
        callbackQueue.put(key, callbackContext);
        // send no result if true
        if (send) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
        }
    }

    public static CallbackContext remove(String key) {
        return remove(key, true);
    }

    public static CallbackContext remove(String key, boolean send) {
        CallbackContext callbackContext = callbackQueue.remove(key);
        if (send && callbackContext != null) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            callbackContext.sendPluginResult(pluginResult);
        }
        return callbackContext;
    }

    public static void fireCallback(String key, PluginResult pluginResult) {
        fireCallback(key, pluginResult, false);
    }

    public static void fireCallback(String key, PluginResult pluginResult, boolean remove) {
        // fire and remove
        if (remove) {
            callbackQueue.remove(key).sendPluginResult(pluginResult);
        } else {
            pluginResult.setKeepCallback(true);
            callbackQueue.get(key).sendPluginResult(pluginResult);
        }
    }
}
