package com.cordova.plugin.access;
// The native Toast API

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static java.security.AccessController.getContext;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;
// Cordova-required packages
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import zucchettiaxess.zreader.zblelib.Lib.AppsUtility.ErrorInfo;
import zucchettiaxess.zreader.zblelib.Lib.HWIntegration.BLEBeacon;
import zucchettiaxess.zreader.zblelib.Lib.HWIntegration.BLEBeaconListener;
import zucchettiaxess.zreader.zblelib.Lib.HWIntegration.BLEScan;
import zucchettiaxess.zreader.zblelib.Lib.HWIntegration.BLESend;
import zucchettiaxess.zreader.zblelib.Lib.HWIntegration.BLESendListener;
import zucchettiaxess.zreader.zblelib.Lib.HWIntegration.BleScanListener;
import zucchettiaxess.zreader.zblelib.Lib.HWIntegration.Dir_Type;
import zucchettiaxess.zreader.zblelib.Lib.HWIntegration.StateOfRdrVerify;
import zucchettiaxess.zreader.zblelib.Lib.HWIntegration.StateOfVerify;
import zucchettiaxess.zreader.zblelib.Lib.HWIntegration.ZAX_BLE_Devices;
import zucchettiaxess.zreader.zblelib.Lib.HWIntegration.f;
import zucchettiaxess.zreader.zblelib.Lib.Stamp.Badge;

public class DigitalAccessPlugin extends CordovaPlugin {


    private final String INIT = "init";
    private final String SCAN = "scan";
    private final String SEND = "send";
    private final String STOP = "stop";
    private final String BLUETOOTH_ERROR = "bluetoothError";

    private final int dbMinDistance = 40;
    private final int dbMaxDistance = 110;

    private static int dbDistance = 100;
    private static String badgeCode = "123456";
    private long timeoutScan = 10000;
    private String buildingDefault = "Default Spark1";

    private ErrorInfo mainErrorInfo;
    private BLEScan bleScan;
    static Context mainContext;
    private Badge badge;

    private Result result;

    private final String fakeDevice = "FakeDevice";

    private static ProgressDialog pd;

    private static final String DURATION_LONG = "long";

    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 1;
    private static final int MY_PERMISSION_ACCESS_BLUETOOTH = 2;

    String[] permissionsLocation = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    String[] permissionsBluetooth = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN};


    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        super.onRequestPermissionResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSION_ACCESS_COARSE_LOCATION) {

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(cordova.getActivity(),
                            "Location Permission is required for access!",
                            Toast.LENGTH_LONG).show();
                }
            }

        }
        if (requestCode == MY_PERMISSION_ACCESS_BLUETOOTH) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(cordova.getActivity(),
                            "Bluetooth Permission is required for access! " + permissions[i],
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public boolean hasPermisssionBluetooth() {

        for (String p : permissionsBluetooth) {
            if (!PermissionHelper.hasPermission(this, p)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasPermisssionLocation() {
        for (String p : permissionsLocation) {
            if (!PermissionHelper.hasPermission(this, p)) {
                return false;
            }
        }
        return true;
    }

    private boolean isEnableBluetooth() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!bluetoothAdapter.isEnabled()) {
                new AlertDialog.Builder(cordova.getContext())
                        .setTitle("Enable Bluetooth")
                        .setMessage("This feature need Bluetooth! Please enable bluetooth and retry.")
                        .setPositiveButton("enable", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                bluetoothAdapter.enable();

                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }


    @Override
    public boolean execute(String action, JSONArray args,
                           final CallbackContext callbackContext) {

        PluginResult pluginResult;

        switch (action) {
            case BLUETOOTH_ERROR:
                String nameMethod = "";
                if (args != null && args.length() > 0) {
                    try {
                        nameMethod = args.getString(0);
                    } catch (JSONException e) {
                        result.setSuccess(false);
                        result.setMessage(e.getMessage());
                        callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                    }
                }
                result = new Result();
                result.setMethod(nameMethod);
                result.setSuccess(false);
                result.setMessage("Permission Bluetooth non allow");
                callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));

                break;
            case INIT:
                try {

                    if (args != null && args.length() > 0) {
                        try {
                            timeoutScan = args.getLong(0);
                            if (args.length() > 1) {
                                dbDistance = args.getInt(1);
                            }
                            if (args.length() > 2) {
                                buildingDefault = args.getString(2);
                            }
                            if (args.length() > 3) {
                                badgeCode = args.getString(3);
                            }
                        } catch (JSONException e) {
                            result.setSuccess(false);
                            result.setMessage(e.getMessage());
                            callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                        }
                    }

                    init(INIT);


                    pluginResult = getPluginResult(PluginResult.Status.OK);
                    callbackContext.sendPluginResult(pluginResult);
                } catch (Exception e) {
                    if (result == null) {
                        result = new Result();
                        result.setMethod(INIT);
                    }
                    result.setSuccess(false);
                    result.setMessage(e.getMessage());
                    callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));

                }
                break;

            case SCAN:
                try {

                    boolean isUsingFakeDevice = false;
                    if (args != null && args.length() > 0) {
                        try {
                            badgeCode = args.getString(0);
                            if (args.length() > 1) {
                                isUsingFakeDevice = args.getBoolean(1);

                            }
                        } catch (JSONException e) {
                            result = new Result();
                            result.setMethod(SCAN);
                            result.setSuccess(false);
                            result.setMessage(e.getMessage());
                            callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                        }
                    }

                    if (result == null || !badgeCode.equals(result.getBadgeCode())) {
                        if (!init(SCAN)) {
                            callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                        }
                        ;
                    }
                    result.setMethod(SCAN);


                    if (!isUsingFakeDevice) {
                        if (!cordova.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                            cordova.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(cordova.getContext(), "Error = ble NOT supported", Toast.LENGTH_LONG).show();
                                }
                            });
                            result.setSuccess(false);

                            result.setMessage("ble NOT supported");
                            callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));

                        }

                        if (checkPermission(true)) {


                            bleScan = new BLEScan(mainContext, mainErrorInfo);
                            bleScan.startBLEScanning(mainErrorInfo, dbDistance, bleScan, timeoutScan);

                            bleCallBack(bleScan, callbackContext);

                        } else {
                            result.setSuccess(false);

                            result.setMessage("checkPermission not allowed");
                            callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                        }
                    } else {
                        result.setSuccess(true);
                        result.setMessage("FakeDevice found");
                        result.setDeviceName(fakeDevice);

                        callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.OK));
                    }

                } catch (Exception e) {
                    if (result == null) {
                        result = new Result();
                        result.setMethod(SCAN);
                    }
                    result.setSuccess(false);
                    result.setMessage(e.getMessage());
                    callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));

                }
                break;
            case SEND:
                try {
                    if (result == null) {
                        if (!init(SEND)) {
                            callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                        }
                        ;
                    }
                    result.setMethod(SEND);

                    //simulate access with fake reader
                    if (fakeDevice.equals(result.getDeviceName())) {
                        result.setSuccess(true);
                        result.setMessage("Badge sended");

                        callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.OK));
                    }

                    if (BLEScan.getBleScan() == null || BLEScan.getDevicesArray() == null || BLEScan.getDevicesArray().size() == 0) {
                        result.setSuccess(false);
                        result.setMessage("No reader found");
                        callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                    }

                    BLESend sender;
                    sender = new BLESend(cordova.getContext());
                    sendPD(true, "BLE Send in progress");

                    sender.sendBadgeToBle(BLEScan.getDevicesArray().get(0), mainErrorInfo, Dir_Type.DIR_IN);
                    sender.setBleListener(new BLESendListener() {
                        @Override
                        public void onBLEReaderDisconnected() {
                            result.setSuccess(false);
                            result.setMessage("Badge not send");
                            bleScan.stopBLEScanning();
                            callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));

                        }

                        @Override
                        public void onSendBadgeCompleted() {
                            result.setSuccess(true);
                            result.setMessage("Badge sended");
                            bleScan.stopBLEScanning();
                            callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.OK));

                        }

                        @Override
                        public void onNewActionInSend(f f) {

                        }
                    });

                } catch (Exception e) {
                    if (result == null) {
                        result = new Result();
                        result.setMethod(SEND);
                    }
                    result.setSuccess(false);
                    result.setMessage(e.getMessage());
                    callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));

                }
                break;

            case STOP:
                if (result == null) {
                    if (!init(STOP)) {

                        callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                    }
                    ;
                }
                result.setMethod(STOP);
                bleScan.stopBLEScanning();
                result.setMessage("Scan stopped");
                callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.OK));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + action);
        }
        // Verify that the user sent a 'show' action

        return true;
    }


    private boolean init(String method) {

        result = new Result();

        mainContext = cordova.getContext();
        mainErrorInfo = new ErrorInfo();
        mainErrorInfo.ClearAll();

        badge = new Badge(mainContext, mainErrorInfo, badgeCode);

        result.setTimeout(timeoutScan);
        result.setNumberOfBadge(badgeCode);
        result.setBadgeCode(badge.getCodeStringOfBadge(false));
        result.setDbDistance(dbDistance);
        result.setDate(new Date());
        result.setLocation(buildingDefault);
        result.setMethod(method);
        result.setTimeout(false);

        if (dbDistance < dbMinDistance || dbDistance > dbMaxDistance) {
            result.setSuccess(false);
            result.setMessage("db distance is not valid. Dbdistance must be inside this range: " + dbMinDistance + " NEAR to " + dbMaxDistance + " FAR\n" +
                    "To set the dbDistance parameter use the method: init(callback, callbackError, timeout, numberOfBadge, dbDistance))");
            return false;
        }

        if (!isEnableBluetooth()) {
            result.setSuccess(false);
            result.setMessage("Bluetooth disable");
            return false;

        }
        ;

        if (!hasPermisssionBluetooth()) {
            result.setSuccess(false);
            result.setMessage("Permission Bluetooth disable");
            PermissionHelper.requestPermissions(this, MY_PERMISSION_ACCESS_BLUETOOTH, permissionsBluetooth);
            return false;

        }
        if (!hasPermisssionLocation()) {
            result.setSuccess(false);
            result.setMessage("Permission Location disable");
            PermissionHelper.requestPermissions(this, MY_PERMISSION_ACCESS_COARSE_LOCATION, permissionsLocation);
            return false;

        }


        result.setSuccess(true);

        return true;
    }


    private void bleCallBack(final BLEScan bleScan, final CallbackContext callbackContext) {

        bleScan.setScanBleListener(new BleScanListener() {
            @Override
            public void onBleScanningTerminated() {
                //no devices found?
                if (BLEScan.getDevicesArray().size() == 0) {
                    result.setSuccess(false);
                    result.setMessage("No devices found");
                    result.setTimeout(true);

                    callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                }
            }

            @Override
            public void onBleDeviceFound(final BluetoothDevice device) {
                //add to textview
                result.setSuccess(true);
                result.setMessage("Device found");
                result.setDeviceMac(device.getAddress());
                result.setDeviceName(device.getName());
                callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.OK));
            }
        });


    }

    public static void sendPD(boolean start, String string) {
        if (start) {
            pd.show();
            pd.setMessage(string);
            pd.setCancelable(false);
        } else {
            pd.dismiss();
        }
    }


    private PluginResult getPluginResult(PluginResult.Status status) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();
        return new PluginResult(status, gson.toJson(result));
    }


    private boolean checkPermission(boolean withAsk) {
        boolean allPermitted = true;

        Activity activity = cordova.getActivity();
        if (!selfPermissionGranted(ACCESS_FINE_LOCATION)) {
            if (withAsk)
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_ACCESS_COARSE_LOCATION);
            allPermitted = false;
        } else {
            if (!selfPermissionGranted(ACCESS_FINE_LOCATION)) {
                if (withAsk)
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSION_ACCESS_COARSE_LOCATION);
                allPermitted = false;
            }
        }

        return allPermitted;
    }

    private static boolean selfPermissionGranted(String permission) {
        // For Android < Android M, self permissions are always granted.
        boolean result = true;
        int targetSdkVersion = mainContext.getApplicationContext().getApplicationInfo().targetSdkVersion;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (targetSdkVersion >= Build.VERSION_CODES.M) {
                // targetSdkVersion >= Android M, we can
                // use Context#checkSelfPermission
                result = (mainContext.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            } else {
                // targetSdkVersion < Android M, we have to use PermissionChecker
                result = PermissionChecker.checkSelfPermission(mainContext, permission) == PermissionChecker.PERMISSION_GRANTED;
            }
        }

        return result;
    }
}