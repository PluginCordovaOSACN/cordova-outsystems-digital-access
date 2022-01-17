package com.cordova.plugin.access;
// The native Toast API
import static java.security.AccessController.getContext;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.widget.Toast;
// Cordova-required packages
import com.google.gson.Gson;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
 
  private final String SHOW = "show";
  private final String INIT = "init";
  private final String SCAN = "scan";
  private final String SEND = "send";
  private final String STOP = "stop";

  private ErrorInfo mainErrorInfo;
  private BLEScan bleScan;
  static Context mainContext;
  private Badge badge;

  private Result result;
  private static String customBadge = "123456";

  private long timeoutScan = 10000;
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
                customBadge = args.getString(1);
              }
            } catch (JSONException e) {
              pluginResult = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
              callbackContext.sendPluginResult(pluginResult);
            }
          }

          init();
          result.setMethod(INIT);
          Gson gson = new Gson();

          pluginResult = new PluginResult(PluginResult.Status.OK, gson.toJson(result));
          callbackContext.sendPluginResult(pluginResult);
          break;
        case SHOW:
            /*if (!action.equals("show")) {
            callbackContext.error("\"" + action + "\" is not a recognized action.");
            return false;
          }*/
          String message;
          String duration;
          try {
            JSONObject options = args.getJSONObject(0);
            message = options.getString("message");
            duration = options.getString("duration");
          } catch (JSONException e) {
            callbackContext.error("Error encountered: " + e.getMessage());
            return false;
          }
          // Create the toast
          Toast toast = Toast.makeText(cordova.getActivity(), message,
            DURATION_LONG.equals(duration) ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
          // Display toast
          toast.show();
          // Send a positive result to the callbackContext
          pluginResult = new PluginResult(PluginResult.Status.OK);
          callbackContext.sendPluginResult(pluginResult);
          break;
        case SCAN:


          if(result==null){
            init();
          }
          result.setMethod(SCAN);

          bleScan = new BLEScan(mainContext, mainErrorInfo);
          bleScan.startBLEScanning(mainErrorInfo, 110, bleScan,timeoutScan);

          bleCallBack(bleScan, callbackContext);

          break;
        case SEND:
          if(BLEScan.getBleScan() == null || BLEScan.getDevicesArray() == null || BLEScan.getDevicesArray().size() == 0){
            pluginResult = new PluginResult(PluginResult.Status.ERROR, "No reader found");
            callbackContext.sendPluginResult(pluginResult);
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
              PluginResult pluginResult;

              pluginResult = new PluginResult(PluginResult.Status.OK, "Badge sended");
              callbackContext.sendPluginResult(pluginResult);



              sendPD(false,"BLE Send in progress");
            }

            @Override
            public void onNewActionInSend(f f) {

            }
          });
          break;

        case STOP:
          bleScan.stopBLEScanning();

          break;
        default:
          throw new IllegalStateException("Unexpected value: " + action);
      }
      // Verify that the user sent a 'show' action
     
      return true;
  }


private void init(){

  result = new Result();

  mainContext = cordova.getContext();
  mainErrorInfo = new ErrorInfo();
  mainErrorInfo.ClearAll();

  badge = new Badge(mainContext, mainErrorInfo, customBadge );

  result.setTimeout(timeoutScan);
  result.setCustomBadge(customBadge);
  result.setBadgeCode(badge.getCodeStringOfBadge(false));

}


  private void bleCallBack(final BLEScan bleScan,final CallbackContext callbackContext) {
    bleScan.setScanBleListener(new BleScanListener() {
      @Override
      public void onBleScanningTerminated() {
        //no devices found?
        if(BLEScan.getDevicesArray().size() == 0)
        {
          result.setStatus("error");
          result.setMessage("No devices found");

          PluginResult pluginResult2 = new PluginResult(PluginResult.Status.OK, "No devices found" +timeoutScan);
          callbackContext.sendPluginResult(pluginResult2);
        }
      }

      @Override
      public void onBleDeviceFound(final BluetoothDevice device) {
        //add to textview
        PluginResult pluginResult2 = new PluginResult(PluginResult.Status.OK, "Name: " + device.getName() +"\n" + "MAC:" + device.getAddress() +"\n\n" + timeoutScan);
        callbackContext.sendPluginResult(pluginResult2);
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

}