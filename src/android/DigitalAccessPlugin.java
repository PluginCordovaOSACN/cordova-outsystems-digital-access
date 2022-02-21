package com.cordova.plugin.access;
// The native Toast API
import static java.security.AccessController.getContext;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.widget.Toast;
// Cordova-required packages
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
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
  @Override
  public boolean execute(String action, JSONArray args,
    final CallbackContext callbackContext)  {

    PluginResult pluginResult;

    switch (action){
        case INIT:

          pd = new ProgressDialog(cordova.getContext());
          //pd.setTitle(R.string.SendBadgePDTitle);
          pd.setMessage("BLE INIT");

          if(args!= null && args.length()>0){
            try {
              timeoutScan = args.getLong(0);
              if(args.length()>1){
                  dbDistance = args.getInt(1);
              }if(args.length()>2){
                  buildingDefault = args.getString(2);
              }
              if(args.length()>3){
                  badgeCode = args.getString(3);
              }
            } catch (JSONException e) {
              result.setSuccess(false);
              result.setMessage( e.getMessage());
              callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
            }
          }

          init(INIT);

          pluginResult = getPluginResult(PluginResult.Status.OK);
          callbackContext.sendPluginResult(pluginResult);
          break;

        case SCAN:
          boolean isUsingFakeDevice = false;
          if(args!= null && args.length()>0){
            try {
              badgeCode = args.getString(0);
              if(args.length()>1){
                isUsingFakeDevice = args.getBoolean(1);

              }
            } catch (JSONException e) {
              result = new Result();
              result.setMethod(SCAN);
              result.setSuccess(false);
              result.setMessage( e.getMessage());
              callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));            }
          }

          if(result==null || !badgeCode.equals(result.getBadgeCode())){
            if(!init(SCAN)){
              callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
            };
          }
          result.setMethod(SCAN);


         if(!isUsingFakeDevice){
           bleScan = new BLEScan(mainContext, mainErrorInfo);

           bleScan.startBLEScanning(mainErrorInfo, dbDistance, bleScan,timeoutScan);

           bleCallBack(bleScan, callbackContext);
         }else{
           result.setSuccess(true);
           result.setMessage("FakeDevice found");
           result.setDeviceName(fakeDevice);

           callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.OK));
         }


          break;
        case SEND:
          if(result==null){
            if(!init(SEND)){
              callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
            };
          }
          result.setMethod(SEND);

          //simulate access with fake reader
          if(fakeDevice.equals(result.getDeviceName())){
              result.setSuccess(true);
              result.setMessage("Badge sended");

              callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.OK));
          }

          if(BLEScan.getBleScan() == null || BLEScan.getDevicesArray() == null || BLEScan.getDevicesArray().size() == 0){
            result.setSuccess(false);
            result.setMessage("No reader found");
            callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.NO_RESULT));
          }

          BLESend sender;
          sender = new BLESend(cordova.getContext());
          sendPD(true,"BLE Send in progress");

          sender.sendBadgeToBle(BLEScan.getDevicesArray().get(0), mainErrorInfo, Dir_Type.DIR_IN);
          sender.setBleListener(new BLESendListener() {
            @Override
            public void onBLEReaderDisconnected() {

              sendPD(false,"BLE Send in progress");


            }

            @Override
            public void onSendBadgeCompleted() {
              result.setSuccess(true);
              result.setMessage("Badge sended");
              callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.OK));

              sendPD(false,"BLE Send in progress");
            }

            @Override
            public void onNewActionInSend(f f) {

            }
          });
          break;

        case STOP:
          if(result==null){
            if(!init(STOP)){

              callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.ERROR));
            };
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


private boolean init(String method){

  result = new Result();

  mainContext = cordova.getContext();
  mainErrorInfo = new ErrorInfo();
  mainErrorInfo.ClearAll();

  badge = new Badge(mainContext, mainErrorInfo, badgeCode );

  result.setTimeout(timeoutScan);
  result.setNumberOfBadge(badgeCode);
  result.setBadgeCode(badge.getCodeStringOfBadge(false));
  result.setDbDistance(dbDistance);
  result.setDate(new Date());
  result.setLocation(buildingDefault);
  result.setMethod(method);
  result.setTimeout(false);

  if(dbDistance<dbMinDistance || dbDistance>dbMaxDistance) {
    result.setSuccess(false);
    result.setMessage("db distance is not valid. Dbdistance must be inside this range: " + dbMinDistance + " NEAR to " + dbMaxDistance + " FAR\n" +
            "To set the dbDistance parameter use the method: init(callback, callbackError, timeout, numberOfBadge, dbDistance))");
    return false;
  }
  result.setSuccess(true);

  return true;
}


  private void bleCallBack(final BLEScan bleScan,final CallbackContext callbackContext) {
    bleScan.setScanBleListener(new BleScanListener() {
      @Override
      public void onBleScanningTerminated() {
        //no devices found?
        if(BLEScan.getDevicesArray().size() == 0)
        {
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


  private PluginResult getPluginResult(PluginResult.Status status){
    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();
    return new PluginResult(status, gson.toJson(result));
  }
}