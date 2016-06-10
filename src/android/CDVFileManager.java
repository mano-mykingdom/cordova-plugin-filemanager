package org.apache.cordova.filemanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by manojkumar on 5/30/16.
 */
public class CDVFileManager extends CordovaPlugin {

    private static final String LOG_TAG = "CDVFileManager";

    // error codes
    private static final int ERROR_UNKNOWN = 1;
    private static final int ERROR_FILE_NOT_FOUND = 2;
    private static final int ERROR_PERMISSION_DENIED = 3;

    // actions
    private static final String ACTION_HAS_PERMISSION = "hasPermission";
    private static final String ACTION_REQUEST_PERMISSION = "requestPermission";
    private static final String ACTION_GET_DIRECTORY_LISTING = "getDirectoryListing";

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals(ACTION_HAS_PERMISSION)) {
            this.hasPermisssion(callbackContext);
            return true;
        } else if (action.equals(ACTION_REQUEST_PERMISSION)) {
            this.requestPermission(callbackContext);
            return true;
        } else if (action.equals(ACTION_GET_DIRECTORY_LISTING)) {
            final String path = args.getString(0);
            final String filter = args.getString(1);
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        getDirectoryListing(path, filter, callbackContext);
                    } catch (JSONException exp) {
                        exp.printStackTrace();
                    }
                }
            });
            return true;
        }
        return false;
    }

    private void hasPermisssion(CallbackContext callbackContext) throws JSONException {
        if (PermissionHelper.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            callbackContext.success(getSuccessObject());
        } else {
            callbackContext.error(getErrorObject(ERROR_PERMISSION_DENIED, "Permission denied."));
        }
    }

    private void requestPermission(CallbackContext callbackContext) throws JSONException {
        PendingRequests.put(ACTION_REQUEST_PERMISSION, callbackContext);
        PermissionHelper.requestPermission(this, 0, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                PendingRequests.fireCallback(ACTION_REQUEST_PERMISSION, new PluginResult(PluginResult.Status.ERROR, getErrorObject(ERROR_PERMISSION_DENIED, "Permission denied.")), true);
                return;
            }
        }
        PendingRequests.fireCallback(ACTION_REQUEST_PERMISSION, new PluginResult(PluginResult.Status.OK, getSuccessObject()), true);
    }

    private void getDirectoryListing(String path, final String filter, CallbackContext callbackContext) throws JSONException {

        JSONObject objResult = getSuccessObject();
        JSONArray arrFiles = new JSONArray();
        JSONObject objData = null;

        if (path != "null") {
            /**
             * list on given path
             */

            File baseFile = new File(path);
            if (!baseFile.exists()) {
                callbackContext.error(getErrorObject(ERROR_FILE_NOT_FOUND, "File not found."));
                return;
            }

            objData = parseFile(baseFile);

            File[] files;
            if (filter != "null") {
                files = baseFile.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getAbsolutePath().matches(filter);
                    }
                });
            } else {
                files = baseFile.listFiles();
            }

            if (files != null) {

                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File lhs, File rhs) {
                        if (lhs.isDirectory() != rhs.isDirectory()) {
                            return lhs.isDirectory() ? -1 : 1;
                        }
                        return lhs.getName().compareToIgnoreCase(rhs.getName());
                    }
                });

                for (File file : files) {
                    if (file.getName().startsWith(".")) {
                        continue;
                    }
                    arrFiles.put(parseFile(file));
                }
            }

        } else {
            /**
             * list roots
             */
            objData = new JSONObject();

            // Root
            File fileRoot = new File("/");
            if (fileRoot.exists()) {
                JSONObject objRoot = parseFile(fileRoot);
                objRoot.put("name", "Device");
                objRoot.put("isExternalStorage", false);
                appendStorageInfo(fileRoot, objRoot);
                arrFiles.put(objRoot);
            }

            // External Storage
            File fileInternalStorage = Environment.getExternalStorageDirectory();
            if (fileInternalStorage.exists()) {
                JSONObject objExternalStorage = parseFile(fileInternalStorage);
                appendStorageInfo(fileInternalStorage, objExternalStorage);
                objExternalStorage.put("isExternalStorage", true);
                objExternalStorage.put("isEmulated", Environment.isExternalStorageEmulated());
                objExternalStorage.put("isRemovable", Environment.isExternalStorageRemovable());
                arrFiles.put(objExternalStorage);
            }

            // Mounted
            try {
                // To Do: check for mounted devices
            } catch (Exception exp) {
                callbackContext.error(getErrorObject(ERROR_UNKNOWN, exp.getMessage()));
                return;
            }
        }
        objData.put("files", arrFiles);
        objResult.put("data", objData);
        callbackContext.success(objResult);
    }

    private JSONObject parseFile(File file) throws JSONException {
        JSONObject objFile = new JSONObject();
        objFile.put("name", file.getName());
        objFile.put("path", file.getAbsolutePath());
        objFile.put("isDirectory", file.isDirectory());
        objFile.put("isFile", file.isFile());
        return objFile;
    }

    private void appendStorageInfo(File file, JSONObject obj) throws JSONException {
        obj.put("totalSpace", file.getTotalSpace());
        obj.put("freeSpace", file.getFreeSpace());
        obj.put("usableSpace", file.getUsableSpace());
    }

    private JSONObject getSuccessObject() throws JSONException {
        JSONObject objSuccess = new JSONObject();
        objSuccess.put("success", true);
        return objSuccess;
    }

    private JSONObject getErrorObject(int code, String message) throws JSONException {
        JSONObject objError = new JSONObject();
        objError.put("success", false);
        objError.put("code", code);
        objError.put("message", message);
        return objError;
    }
}