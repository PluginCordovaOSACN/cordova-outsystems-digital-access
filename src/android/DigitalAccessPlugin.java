package com.cordova.plugin.access;
// The native Toast API

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;


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
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.EasyPermissions;
import zucchettiaxess.zreader.zblelib.Lib.AppsUtility.ErrorInfo;

import zucchettiaxess.zreader.zblelib.Lib.HWIntegration.BLEScan;
import zucchettiaxess.zreader.zblelib.Lib.HWIntegration.BLESend;
import zucchettiaxess.zreader.zblelib.Lib.HWIntegration.BLESendListener;
import zucchettiaxess.zreader.zblelib.Lib.HWIntegration.BleScanListener;
import zucchettiaxess.zreader.zblelib.Lib.HWIntegration.DeviceManager;
import zucchettiaxess.zreader.zblelib.Lib.HWIntegration.Dir_Type;
import zucchettiaxess.zreader.zblelib.Lib.HWIntegration.f;
import zucchettiaxess.zreader.zblelib.Lib.Stamp.Badge;

public class DigitalAccessPlugin extends CordovaPlugin {


    private final String INIT = "init";
    private final String SCAN = "scan";
    private final String SEND = "send";
    private final String STOP = "stop";
    private final String SEND_BLE = "sendBle";

    private final String BLUETOOTH = "checkBluetooth";
    private final String ALL_IN_ONE_EVENT = "allInOneEvent";
    private final String GET_RESULT = "getResult";
    private final String STOP_ALL_IN_ONE_EVENT = "stopAllInOneEvent";

    private static boolean isEnableAllInOneEvent = false;

    private static int countStart;
    private static int countEnd;

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


    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1;
    private static final int MY_PERMISSION_ACCESS_BLUETOOTH = 2;
    private static final int MY_PERMISSION_ACCESS_BLUETOOTH_S = 3;


    String[] permissionsLocation = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

    private CallbackContext callback;
    public static final String[] BLUETOOTH_PERMISSIONS_S = {"android.permission.BLUETOOTH_SCAN", "android.permission.BLUETOOTH_CONNECT"};

    private static boolean isEnabled_S = false;
    private static boolean isEnabled_Location = false;

    private static String lastIdExecutionCallback;
    BluetoothDevice bleFounded;

    private Request request;

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        super.onRequestPermissionResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

        //first check permission location
        if (requestCode == MY_PERMISSION_ACCESS_FINE_LOCATION) {

            int granted_permission = 0;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    granted_permission++;
                }
            }
            if (granted_permission == 0 && grantResults.length > 0) {
                result.setSuccess(false);
                result.setMessage("Location Permission is required for access! ");
                result.setPermissionDenied("Location " + MY_PERMISSION_ACCESS_FINE_LOCATION);
                isEnabled_Location = false;
                callback.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));

            } else {
                isEnabled_Location = true;
                if (Build.VERSION.SDK_INT > 30) {
                    //required permission bluetooth scan
                    PermissionHelper.requestPermissions(this, MY_PERMISSION_ACCESS_BLUETOOTH_S, BLUETOOTH_PERMISSIONS_S);
                } else {
                    checkBluetoothStep2();
                }
            }

        } else if (requestCode == MY_PERMISSION_ACCESS_BLUETOOTH_S) {
            int granted_permission = 0;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    granted_permission++;
                }
            }
            if (granted_permission == 0) {
                result.setSuccess(false);
                result.setMessage("Bluetooth Scan Permission is required for access! ");
                result.setPermissionDenied("Bluetooth Scan " + MY_PERMISSION_ACCESS_BLUETOOTH_S);
                isEnabled_S = false;
                callback.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));

            } else {
                isEnabled_S = true;
                //required
                if (!isEnabled_Location) { //call permission location
                    PermissionHelper.requestPermissions(this, MY_PERMISSION_ACCESS_FINE_LOCATION, permissionsLocation);
                } else {
                    checkBluetoothStep2();
                }
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);


        if (resultCode == RESULT_OK) {

            if (!isEnableAllInOneEvent) {
                Toast.makeText(webView.getContext(), "Bluetooth is ON", Toast.LENGTH_SHORT).show();
                result.setMessage("Bluetooth is ON");
                result.setPermissionDenied("");
                callback.sendPluginResult(getPluginResult(PluginResult.Status.OK));
            } else {
                if (result.isShowToast())
                    Toast.makeText(webView.getContext(), "Bluetooth is ON", Toast.LENGTH_SHORT).show();
                scanAndSendBadge();
            }

        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(webView.getContext(), "Bluetooth operation is cancelled",
                    Toast.LENGTH_SHORT).show();
            result.setMessage("Bluetooth operation is cancelled");
            callback.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
        }

    }

    private boolean checkBluetoothStep2() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            result.setSuccess(false);
            result.setMessage("This device doesn't support Bluetooth");

            callback.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
            return false;
        }


        if (!bluetoothAdapter.isEnabled()) {
            Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            cordova.startActivityForResult(this, bluetoothIntent, MY_PERMISSION_ACCESS_BLUETOOTH);
        } else {
            if (!isEnableAllInOneEvent) {
                result.setMessage("Bluetooth Enabled");
                callback.sendPluginResult(getPluginResult(PluginResult.Status.OK));
            } else {
                // start scanning and send badge
                scanAndSendBadge();
            }

            return true;
        }
        return false;
    }


    private void scanAndSendBadge() {
        isEnableAllInOneEvent = true;
        scanning();
    }

    private void sendBadge() {
        try {
            if (!result.isSending()) {
               // bleScanTMP = new BLEScan(mainContext, mainErrorInfo);


                BluetoothDevice ble = bleFounded;
                BLESend sender;
                result.setSending(true);
                result.setScanning(false);

                result.setOtherMessage(result.getOtherMessage() + "__SendBadge");

               // bleScan.stopBLEScanning();

                sender = new BLESend(cordova.getContext());

                sender.setBleListener(new BLESendListener() {
                    @Override
                    public void onBLEReaderDisconnected() {
                        result.setOtherMessage(result.getOtherMessage() + "__onBLEReaderDisconnected");

                        if (!result.getMessage().equals("Badge sended")) {
                            result.setSuccess(false);
                            result.setMessage("Badge not send");
                            callback.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                        } else {
                            callback.sendPluginResult(getPluginResult(PluginResult.Status.OK));
                        }

                    }

                    @Override
                    public void onSendBadgeCompleted() {
                        result.setOtherMessage(result.getOtherMessage() + "__onSendBadgeCompleted");

                        result.setSuccess(true);
                        result.setMessage("Badge sended");

                        callback.sendPluginResult(getPluginResult(PluginResult.Status.OK));
                        //bleScan=null;
                    }

                    @Override
                    public void onNewActionInSend(f f) {
                        // result.setOtherMessage(result.getOtherMessage() + "__onNewActionInSend: " + f.name());
                    }
                });
                sender.sendBadgeToBle(ble, mainErrorInfo, Dir_Type.DIR_IN);
                result.setStartDateSending(new Date());
                if (result.isShowToast())
                    Toast.makeText(webView.getContext(), "Sending badge...", Toast.LENGTH_SHORT).show();

                result.setOtherMessage(result.getOtherMessage() + "__SendBadgeBLE");
            }

        } catch (Exception e) {
            if (result == null) {
                initSecondVersion(ALL_IN_ONE_EVENT);
            }
            result.setOtherMessage(result.getOtherMessage() + "__SendBadge Generic Exception" + e.getMessage());
            result.setSuccess(false);
            result.setTimeout(true);
            callback.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));

        }

    }


    private void scanning() {
        if (!cordova.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            if (result.isShowToast())
                Toast.makeText(webView.getContext(), "Error = ble NOT supported",
                        Toast.LENGTH_LONG).show();

            result.setSuccess(false);

            result.setMessage("ble NOT supported");
            callback.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));

        }

        if (BLEScan.getBleScan()!=null){
            result.setOtherMessage(result.getOtherMessage() + " __mScanning:" +  BLEScan.getmScanning() + " --- " );
            //BLEScan.setmScanning(false);


        }
       // boolean needStopped = false;
      /* if (bleScan!=null) {
           try{
               if (result.isShowToast())
                   Toast.makeText(webView.getContext(), "BleScan retry to scan after 1 seconds", Toast.LENGTH_SHORT).show();

               TimeUnit.SECONDS.sleep(1);
           } catch (InterruptedException e) {
               result.setScanning(false);

               result.setMessage(e.getMessage());
               callback.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
           }
       }*/
            bleScan = new BLEScan(mainContext, mainErrorInfo);

            //bleScan.setScanBleListener(null);

            bleScan.setScanBleListener(new BleScanListener() {
                String idSession = result.getIdExecution();

                boolean needstopped = false;
                @Override
                public void onBleScanningTerminated() {
                    countEnd++;
                    result.setOtherMessage(result.getOtherMessage() + " __mScanningTerminated:" +  BLEScan.getmScanning() + " --- " );

                    result.setOtherMessage(result.getOtherMessage() + "__terminated__" + " start " + countStart + " end " + countEnd + " ----");
                    result.setTerminated(true);
                    if (!result.isSending()) {
                       if (idSession.equals(result.getIdExecution())) {
                            if (BLEScan.getDevicesArray().size() == 0 || (request.getDevicesEnabled() != null && !request.getDevicesEnabled().isEmpty())) {
                                result.setTimeout(true);
                                //result.setScanning(false);
                                result.setSuccess(false);
                                result.setMessage("No devices found");
                                //callback.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                            //}
                       if ((new Date().getTime() - result.getStartDateScanning().getTime() > result.getTimeout())) {
                            if (result.isShowToast())
                                Toast.makeText(webView.getContext(), "No devices found", Toast.LENGTH_SHORT).show();
                            result.setScanning(false);
                            callback.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                        } else {
                           if (result.isShowToast())
                                Toast.makeText(webView.getContext(), "Scanning terminated old scanning", Toast.LENGTH_SHORT).show();
                           result.setOtherMessage(result.getOtherMessage() + "--- old scanning terminated ----" + (new Date().getTime() - result.getStartDateScanning().getTime()));

                           /*
                            result.setOtherMessage(result.getOtherMessage() + " _-_ " + "onBleScanningTerminated");

                            if (!result.isAlreadyScan()) {
                                result.setOtherMessage(result.getOtherMessage() + " _-_ " + "Try restart" + (new Date().getTime() - result.getStartDateScanning().getTime()));

                                try {
                                    int secondsWait = (int) (new Date().getTime() - result.getStartDateScanning().getTime() - result.getTimeout())/1000;
                                   // bleScan.stopBLEScanning();
                                    if (result.isShowToast())
                                        Toast.makeText(webView.getContext(), "Scanning terminated but not timeout, retry to scan after " + secondsWait + " seconds", Toast.LENGTH_SHORT).show();
                                    TimeUnit.SECONDS.sleep(secondsWait);
                                    result.setAlreadyScan(true);

                                    result.setOtherMessage(result.getOtherMessage() + " _-_ " + "restarted" + (new Date().getTime() - result.getStartDateScanning().getTime()));
                                    //result.setStartDateScanning(new Date());
                                    bleScan.startBLEScanning(mainErrorInfo, result.getDbDistance(), bleScan, result.getTimeout());
                                } catch (InterruptedException e) {
                                    result.setScanning(false);

                                    result.setMessage(e.getMessage());
                                    callback.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                                }
                           } else {
                                result.setScanning(false);

                                result.setOtherMessage(result.getOtherMessage() + " _-_ " + "Is already restarted" + (new Date().getTime() - result.getStartDateScanning().getTime()));
                                callback.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));

                            }*/
                        }

                            } else {
                                result.setOtherMessage(result.getOtherMessage() + " _-_device found");

                            }
                       } else {
                            result.setOtherMessage(result.getOtherMessage() + " _-_IDSESSION different");

                        }
                    } else {
                        result.setSending(false);

                        result.setOtherMessage(result.getOtherMessage() + " _-_is sending");

                    }

                }

                @Override
                public void onBleDeviceFound(final BluetoothDevice device) {
                    result.setOtherMessage(result.getOtherMessage() + " _-_ " + "onBleDeviceFound");

                    if (idSession.equals(result.getIdExecution())) {
                        if (!result.isSending()) {
                            if ((request.getDevicesEnabled() == null || request.getDevicesEnabled().isEmpty() || request.getDevicesEnabled().contains(device.getAddress()) || request.getDevicesEnabled().contains(device.getName())
                                && (request.getDevicesDisabled() == null || request.getDevicesDisabled().isEmpty() || (!request.getDevicesDisabled().contains(device.getAddress()) && !request.getDevicesDisabled().contains(device.getName()))))) {

                                    if (result.getDeviceMac().isEmpty()) {

                                    result.setDeviceMac(device.getAddress());
                                    result.setDeviceName(device.getName());
                                    result.setScanning(false);
                                    bleFounded = device;
                                    result.setBleFounded(device);

                                    sendBadge();


                                }
                            }
                            else{
                                result.setOtherMessage(result.getOtherMessage() + " _-_Device found but not enabled");

                            }
                        }
                    } else {
                        result.setOtherMessage(result.getOtherMessage() + " _-_IDSESSION different founded");
                        //   bleScanTMP.stopBLEScanning();
                    }

                }


            });

        /*}else{
        /*    try{
                if (result.isShowToast())
                    Toast.makeText(webView.getContext(), "BleScan retry to scan after 1 seconds", Toast.LENGTH_SHORT).show();

                TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            result.setScanning(false);

            result.setMessage(e.getMessage());
            callback.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
        }
        }*/

        result.setStartDateScanning(new Date());
        countStart++;

        bleScan.startBLEScanning(mainErrorInfo, result.getDbDistance(), bleScan, result.getTimeout());
        result.setOtherMessage(result.getOtherMessage() + "__Start Scanning");
        result.setScanning(true);
        if (result.isShowToast())
            Toast.makeText(webView.getContext(), "Scanning...", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean execute(String action, JSONArray args,
                           final CallbackContext callbackContext) {

        PluginResult pluginResult;
        callback = callbackContext;
        isEnableAllInOneEvent = false;
        switch (action) {
            case GET_RESULT:

                if (result == null) {
                    initSecondVersion(GET_RESULT);
                }
                callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.OK));


                break;

            case STOP_ALL_IN_ONE_EVENT:
                try {
                    if (result != null) {
                        initSecondVersion(STOP_ALL_IN_ONE_EVENT);
                    }

                    if (bleScan != null) {
                        bleScan.stopBLEScanning();
                    }
                    callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.OK));

                } catch (Exception e) {
                    callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));

                }


                break;
            case ALL_IN_ONE_EVENT:
                try {
                    isEnableAllInOneEvent = true;
                    //long timeEndOld = 0;

                    /*if (result != null) {
                        timeEndOld = ((result.getEndDate().getTime() + result.getTimeOfExecution()) - new Date().getTime());
                    }
                    */



                    //inizialize
                    if (args != null && args.length() > 0) {
                        try {
                            request = new Gson().fromJson(args.getString(0), Request.class);

                            //timeoutOfScanSeconds
                            timeoutScan = request.getTimeoutScanSeconds() * 1000;

                            dbDistance = request.getDbDistance();
                            badgeCode = request.getBadgeCode();

                           /* while (result!=null && !result.isTerminated()){
                                if(request.isShowToast()){
                                    Toast.makeText(webView.getContext(), "Waiting finishing previous istance", Toast.LENGTH_SHORT).show();
                                }
                                TimeUnit.SECONDS.sleep(1);

                            }*/
                            initSecondVersion(ALL_IN_ONE_EVENT);

                            DeviceManager devManager = new DeviceManager(mainContext, mainErrorInfo);
                            if (mainErrorInfo.IsAllOk(true)) {
                                devManager.activateBluetooth(true);
                            }
                            result.setShowToast(request.isShowToast());

                            badge = new Badge(mainContext, mainErrorInfo, badgeCode);
                            result.setNumberOfBadge(badgeCode);
                            result.setTimeout(timeoutScan);
                            result.setDbDistance(dbDistance);
                            result.setBadgeCode(badge.getCodeStringOfBadge(false));
                            result.setIdExecution(result.getBadgeCode() + result.getDate().getTime());
                           /* cordova.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String idSession = result.getIdExecution();
                                    try {
                                        TimeUnit.SECONDS.sleep(((timeoutScan) / 1000)+20);
                                        if(idSession.equals(result.getIdExecution())){
                                            result.setMessage("Timeout generic");
                                            result.setSuccess(false);
                                            callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));

                                        }
                                    } catch (InterruptedException e) {

                                        result.setMessage(e.getMessage());
                                        result.setSuccess(false);
                                        callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));


                                    }
                                   // Toast.makeText(cordova.getContext(), "Error = ble NOT supported", Toast.LENGTH_LONG).show();
                                }
                            });*/
                            /*if (timeEndOld > 0 && timeEndOld < timeoutScan) {
                                result.setOtherMessage("Wait until the other finished");
                                TimeUnit.SECONDS.sleep((timeoutScan - timeEndOld) / 1000);
                                bleScan = null;
                                result.setOtherMessage(result.getOtherMessage() + "__Started__" + (new Date()) + " timewait " + (timeoutScan - timeEndOld));
                            }
                            */

                            //check Permission
                            PermissionHelper.requestPermissions(this, MY_PERMISSION_ACCESS_FINE_LOCATION, permissionsLocation);


                        } catch (JSONException e) {
                            result.setSuccess(false);
                            result.setMessage(e.getMessage());
                            callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                        }
                    } else {
                        result.setSuccess(false);
                        result.setMessage("one or more input parameters are missed: timeoutSeconds, dbDistance, badgeCode");
                        callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                    }


                } catch (Exception e) {
                    result.setOtherMessage(result.getOtherMessage() + "__ Generic Exception");
                    result.setSuccess(false);
                    result.setTimeout(true);
                    callback.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));

                }


                break;
            case BLUETOOTH:
                init(BLUETOOTH);
                try {
                    // checkBluetoothAndLocation();

                    /*ActivityCompat.requestPermissions(cordova.getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSION_ACCESS_FINE_LOCATION);*/
                    PermissionHelper.requestPermissions(this, MY_PERMISSION_ACCESS_FINE_LOCATION, permissionsLocation);


                } catch (Exception e) {
                    if (result == null) {
                        result = new Result();
                        result.setMethod(BLUETOOTH);
                    }
                    result.setSuccess(false);
                    result.setMessage(e.getMessage());
                    callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));


                }


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

                        // if (checkPermission(true)) {

                        //if (bleScan==null){
                        bleScan = new BLEScan(mainContext, mainErrorInfo);
                        // }
                        bleScan.startBLEScanning(mainErrorInfo, result.getDbDistance(), bleScan, result.getTimeout());
                        bleCallBack(bleScan, callbackContext);
                        /*} else {
                            result.setSuccess(false);

                            result.setMessage("checkPermission not allowed");
                            callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                        }*/
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
                    bleScan.stopBLEScanning();

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

                    for (int i = 0; i < BLEScan.getDevicesArray().size(); i++) {
                        result.setOtherMessage(result.getOtherMessage() + " Device Founded: " + BLEScan.getDevicesArray().get(i).getAddress());
                    }

                    result.setOtherMessage(result.getOtherMessage() + " send Badge to " + BLEScan.getDevicesArray().get(0).getAddress());
                    sender.sendBadgeToBle(BLEScan.getDevicesArray().get(0), mainErrorInfo, Dir_Type.DIR_IN);
                    sender.setBleListener(new BLESendListener() {
                        @Override
                        public void onBLEReaderDisconnected() {
                            result.setSuccess(false);
                            result.setMessage("Badge not send");
                            callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));

                        }

                        @Override
                        public void onSendBadgeCompleted() {
                            result.setSuccess(true);
                            result.setMessage("Badge sended");
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
            
            case SEND_BLE:
                try {
                    String bleMACAddress = null;
                    if (args != null && args.length() > 0) {
                        try {
                            bleMACAddress = args.getString(0);
                           
                        } catch (JSONException e) {
                            result = new Result();
                            result.setMethod(SEND_BLE);
                            result.setSuccess(false);
                            result.setMessage(e.getMessage());
                            callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                        }
                    }
                    if (result == null || bleMACAddress == null) {
                        if (!init(SEND_BLE)) {
                            callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                        }
                        
                    }
                  
                    bleScan.stopBLEScanning();

                    result.setMethod(SEND_BLE);
                    result.setDeviceMac("");
                    result.setDeviceName("");

                    if (BLEScan.getBleScan() == null || BLEScan.getDevicesArray() == null || BLEScan.getDevicesArray().size() == 0) {
                        result.setSuccess(false);
                        result.setMessage("No reader found");
                        callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                    }

                    BLESend sender;
                    sender = new BLESend(cordova.getContext());
                    int indexBle = -1; 
                    for (int i = 0; i < BLEScan.getDevicesArray().size(); i++) {
                        result.setOtherMessage(result.getOtherMessage() + " Device Founded: " + BLEScan.getDevicesArray().get(i).getAddress());
                        if(BLEScan.getDevicesArray().get(i).getAddress().equals(bleMACAddress)){
                            indexBle = i;
                        }
                    }
                    if(indexBle == -1){
                        result.setSuccess(false);
                        result.setMessage("No reader found with MACAddress: " + bleMACAddress );
                        callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                    }else{
                        result.setDeviceMac(BLEScan.getDevicesArray().get(indexBle).getAddress());
                        result.setDeviceName(BLEScan.getDevicesArray().get(indexBle).getName());
                        result.setOtherMessage(result.getOtherMessage() + " send Badge to " + BLEScan.getDevicesArray().get(indexBle).getAddress());
                        sender.sendBadgeToBle(BLEScan.getDevicesArray().get(indexBle), mainErrorInfo, Dir_Type.DIR_IN);
                        sender.setBleListener(new BLESendListener() {
                            @Override
                            public void onBLEReaderDisconnected() {
                                result.setSuccess(false);
                                result.setMessage("Badge not send");
                                callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));

                            }

                            @Override
                            public void onSendBadgeCompleted() {
                                result.setSuccess(true);
                                result.setMessage("Badge sended");
                                callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.OK));

                            }

                            @Override
                            public void onNewActionInSend(f f) {

                            }
                        });

                    }
                   

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

                try {
                    if (result == null) {
                        if (!init(STOP)) {
                            callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                        }

                    }
                    result.setMethod(STOP);
                    bleScan.stopBLEScanning();
                    bleScan.onBleScanningTerminated();

                    result.setMessage("Scan stopped");
                    callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.OK));

                } catch (Exception e) {
                    if (result == null) {
                        result = new Result();
                        result.setMethod(STOP);
                    }
                    result.setSuccess(false);
                    result.setMessage(e.getMessage());
                    callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));


                }

                break;
            default:
                throw new IllegalStateException("Unexpected value: " + action);
        }
        // Verify that the user sent a 'show' action

        return true;
    }


    private void initSecondVersion(String method) {

        result = new Result();

        mainContext = cordova.getContext();
        mainErrorInfo = new ErrorInfo();
        mainErrorInfo.ClearAll();

        badge = new Badge(mainContext, mainErrorInfo, badgeCode);
        result.setPermissionDenied("");
        result.setDeviceName("");
        result.setDeviceMac("");
        result.setSdkVersion(cordova.getContext().getApplicationContext().getApplicationInfo().targetSdkVersion);
        result.setMessage("");
        result.setOtherMessage("");
        result.setDate(new Date());
        result.setAlreadyScan(false);


        result.setMethod(method);
        result.setTimeout(false);
        result.setUserDevice("Android");
        result.setSuccess(true);
        result.setScanning(false);
        result.setSending(false);
result.setTerminated(false);
        result.setShowToast(false);

    }


    private boolean init(String method) {

        result = new Result();

        mainContext = cordova.getContext();
        mainErrorInfo = new ErrorInfo();
        mainErrorInfo.ClearAll();

        badge = new Badge(mainContext, mainErrorInfo, badgeCode);
        result.setPermissionDenied("");
        result.setDeviceName("");
        result.setDeviceMac("");
        result.setSdkVersion(cordova.getContext().getApplicationContext().getApplicationInfo().targetSdkVersion);
        result.setMessage("");
        result.setOtherMessage("");
        result.setTimeout(timeoutScan);
        result.setNumberOfBadge(badgeCode);
        result.setBadgeCode(badge.getCodeStringOfBadge(false));
        result.setDbDistance(dbDistance);
        result.setDate(new Date());
        result.setLocation(buildingDefault);
        result.setMethod(method);
        result.setTimeout(false);
        result.setUserDevice("Android");

        if (dbDistance < dbMinDistance || dbDistance > dbMaxDistance) {
            result.setSuccess(false);
            result.setMessage("db distance is not valid. Dbdistance must be inside this range: " + dbMinDistance + " NEAR to " + dbMaxDistance + " FAR\n" +
                    "To set the dbDistance parameter use the method: init(callback, callbackError, timeout, numberOfBadge, dbDistance))");
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

                if (!isEnableAllInOneEvent) {

                    if (BLEScan.getDevicesArray().size() == 0) {
                        result.setSuccess(false);
                        result.setMessage("No devices found");
                        result.setTimeout(true);

                        callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                    }
                } else {
                    if (result.isScanning()) {
                        if (BLEScan.getDevicesArray().size() == 0 || (request.getDevicesEnabled() != null && !request.getDevicesEnabled().isEmpty())) {
                            result.setOtherMessage(result.getOtherMessage() + " _-_ " + "onBleScanningTerminated");

                            result.setTimeout(true);

                            result.setSuccess(false);
                            result.setMessage("No devices found");
                            //callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));

                            if (lastIdExecutionCallback == null || !lastIdExecutionCallback.equals(result.getIdExecution())) {
                                if ((new Date().getTime() - result.getDate().getTime() - 1000 > result.getTimeout())) {
                                    result.setScanning(false);
                                    callback.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                                } else {
                                    result.setOtherMessage(result.getOtherMessage() + " _-_ " + "onBleScanningTerminated");

                                    if (!result.isAlreadyScan()) {
                                        result.setOtherMessage(result.getOtherMessage() + " _-_ " + "Try First restart" + (new Date().getTime() - result.getDate().getTime()));

                                        result.setAlreadyScan(true);
                                        try {
                                            TimeUnit.SECONDS.sleep(1);
                                            result.setOtherMessage(result.getOtherMessage() + " _-_ " + "First restarted" + (new Date().getTime() - result.getDate().getTime()));

                                            bleScan.startBLEScanning(mainErrorInfo, result.getDbDistance(), bleScan, result.getTimeout());
                                        } catch (InterruptedException e) {
                                            result.setScanning(false);

                                            result.setMessage(e.getMessage());
                                            callback.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));

                                        }
                                    } else {
                                        //  callback.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
                                        result.setScanning(false);

                                        result.setOtherMessage(result.getOtherMessage() + " _-_ " + "Is already restarted" + (new Date().getTime() - result.getDate().getTime()));
                                        callback.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));

                                    }

                                }
                            } else {
                                result.setOtherMessage(result.getOtherMessage() + " _-_ " + "same idExecution");

                            }

                        } else {
                            result.setOtherMessage(result.getOtherMessage() + " _-_ " + "found device");

                        }
                    } else {
                        result.setOtherMessage(result.getOtherMessage() + " _-_ " + "Finished scanning" + (new Date().getTime() - result.getDate().getTime()));

                        callback.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));

                    }
                }
            }

            @Override
            public void onBleDeviceFound(final BluetoothDevice device) {
                result.setOtherMessage(result.getOtherMessage() + " _-_ " + "onBleDeviceFound");
                if (!isEnableAllInOneEvent) {
                    result.setSuccess(true);
                    result.setTimeout(false);
                    result.setDeviceMac(device.getAddress());
                    result.setDeviceName(device.getName());
                    result.setMessage("Device found");

                    callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.OK));
                } else {
                    if (result.isScanning()) {
                        if (request.getDevicesEnabled() == null || request.getDevicesEnabled().isEmpty() || request.getDevicesEnabled().contains(device.getAddress())) {
                            if (result.getDeviceMac().isEmpty()) {
                                result.setDeviceMac(device.getAddress());
                                result.setDeviceName(device.getName());
                                //sendBadge(device);

                            }
                        }
                    }


                }

            }
        });


    }


    private PluginResult getPluginResult(PluginResult.Status status) {
        lastIdExecutionCallback = result.getIdExecution();
        result.setEndDate(new Date());
        result.setTimeOfExecution(result.getEndDate().getTime() - result.getDate().getTime());
        result.setOtherMessage(result.getOtherMessage() + "__start " + countStart + " end " + countEnd +" ----" + BLESend.isBadgeInSending() +" --");

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();
           /* if(result.getBleFounded()!=null){
                gson.toJson(result.getBleFounded());
                return new PluginResult(status,   gson.toJson(result.getBleFounded()));

            }*/
        return new PluginResult(status, gson.toJson(result));


    }


}
