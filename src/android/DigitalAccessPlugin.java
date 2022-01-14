package com.cordova.plugin.access;
// The native Toast API
import static java.security.AccessController.getContext;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.widget.Toast;
// Cordova-required packages
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import zucchettiaxess.zreader.zblelib.Lib.AppsUtility.ErrorInfo;
import zucchettiaxess.zreader.zblelib.Lib.HWIntegration.BLEScan;
import zucchettiaxess.zreader.zblelib.Lib.HWIntegration.BleScanListener;

public class DigitalAccessPlugin extends CordovaPlugin {
 
  private final String SHOW = "show";
  private final String INIT = "init";
  private final String SCAN = "scan";

  private ErrorInfo mainErrorInfo;
  private BLEScan bleScan;
  static Context mainContext;
  private long timeoutScan = 10000;

  private static final String DURATION_LONG = "long";
  @Override
  public boolean execute(String action, JSONArray args,
    final CallbackContext callbackContext) {

    PluginResult pluginResult;
      switch (action){
        case INIT:

          try {
            JSONObject options = args.getJSONObject(0);
            timeoutScan = options.getLong("timeoutScan");
          } catch (JSONException e) {
            callbackContext.error("Error encountered: " + e.getMessage());
            return false;
          }
          pluginResult = new PluginResult(PluginResult.Status.OK);
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
          //scan the badge
          mainErrorInfo = new ErrorInfo();
          mainErrorInfo.ClearAll();

          mainContext = cordova.getContext();
          bleScan = new BLEScan(mainContext, mainErrorInfo);

          bleScan.startBLEScanning(mainErrorInfo, 110, bleScan,timeoutScan);

          bleCallBack(bleScan, callbackContext);


          break;

        default:
          throw new IllegalStateException("Unexpected value: " + action);
      }
      // Verify that the user sent a 'show' action
     
      return true;
  }





  private void bleCallBack(final BLEScan bleScan,final CallbackContext callbackContext) {
    bleScan.setScanBleListener(new BleScanListener() {
      @Override
      public void onBleScanningTerminated() {
        //no devices found?
        if(BLEScan.getDevicesArray().size() == 0)
        {

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


}