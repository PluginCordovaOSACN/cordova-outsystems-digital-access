import Foundation
import ZAxessCommonObjects
import ZAxessBLELibrarySaipem


@objc(DigitalAccessPlugin) class DigitalAccessPlugin: CDVPlugin {

    public var result: Result?

    private let dbMinDistance = 40
    private let dbMaxDistance = 110

    private let fakeDevice = "FakeDevice"
    
    private var blemanager: ZBTDeviceManager!
    
    
    private var resultJson: String {
        let jsonEncoder = JSONEncoder()
        let jsonData = (try? jsonEncoder.encode(self.result))!
        return String(data: jsonData, encoding: String.Encoding.utf16) ?? ""
    }

    @objc(init:) func initialize(command : CDVInvokedUrlCommand){
        let timeoutScan = command.argument(at: 0) as! Int64
        let dbDistance = command.argument(at: 1) as! Int
        let buildingDefault = command.argument(at: 2) as! String
        let badgeCode = command.argument(at: 3) as! String

        result = Result(method: "init")
        result?.timeout = timeoutScan
        /*TODO check dbDistance should be > dbMinDistance and < dbMaxDistance and set success to false and add this message: 
        "db distance is not valid. Dbdistance must be inside this range: " + dbMinDistance + " NEAR to " + dbMaxDistance + " FAR\n" +
                    "To set the dbDistance parameter use the method: init(callback, callbackError, timeout, numberOfBadge, dbDistance))"

        */
        result?.dbDistance = dbDistance
        result?.location = buildingDefault
        result?.badgeCode = badgeCode
     /* TODO 
     check bluetooth actived
     otherwise ask to the user to activated  
*/

        result?.success = true
        result?.date = Date()

        if dbMinDistance < dbDistance && dbDistance < dbMaxDistance {
            result?.success = false
            result?.message = "db distance is not valid. Dbdistance must be inside this range: \(dbMinDistance) NEAR to \(dbMaxDistance) FAR\nTo set the dbDistance parameter use the method: init(callback, callbackError, timeout, numberOfBadge, dbDistance))"

            commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)
        } else {
            commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: resultJson), callbackId: command.callbackId)
        }

    }

    @objc(checkBluetooth:) func checkBluetooth(command : CDVInvokedUrlCommand){
    
    //if exist result
        if result != nil {
            result?.method = "checkBluetooth"
            //otherwise -> create result and set init

            //TODO return Result.success=true if bluetooth is actived otherwise ask to the user to active and after return the result
            blemanager = ZBTDeviceManager()
            blemanager.delegate = self
            if self.blemanager.bluetoothEnabled ?? false {

                result?.success = true
                result?.date = Date()
                //TODO encode the result to json

                //if result.success
                commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: resultJson), callbackId: command.callbackId)
            } else {
                result?.success = false
                result?.date = Date()
                result?.message = "Please turn on the bluetooth"
                
                commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)
            }
        } else {
            result = Result(method: "checkBluetooth")
            commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)
        }
        //else
        //commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)



    }

    @objc(scan:) func scan(command : CDVInvokedUrlCommand){
    
        let badgeCode = command.argument(at: 0) as! String
        let isUsingFakeDevice = command.argument(at: 1) as? Bool // not mandatory default false

        if result != nil {
            result?.method = "scan"
            result?.date = Date()
            
            if isUsingFakeDevice ?? false == true {
                result?.success = true
                result?.message = "FakeDevice found"
                result?.deviceName = fakeDevice
                
                commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: resultJson), callbackId: command.callbackId)
            } else {
            
                blemanager.updateDevicesList()
                if result?.deviceMac != nil {
                    result?.badgeCode = badgeCode
                    result?.success = true
                    result?.message = "Device found"

                    commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: resultJson), callbackId: command.callbackId)
                    
                } else {
                    result?.success = false
                    result?.message = "No device founded"
                    
                    commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: resultJson), callbackId: command.callbackId)
                }
            }
        } else {
            result = Result(method: "scan")
            commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)
        }



    }


    @objc(send:) func send(command : CDVInvokedUrlCommand){
    
        if result != nil {
            result?.method = "send"
            result?.date = Date()

            //if result nill or result.timeout or result.dbdistance are nill or badgeCode nil then Result.success=false


            //simulate access with fake reader
            //if fakeDevice = result.deviceName {
                result?.success = true
                result?.message = "Badge sended"
            // TODO: SET ZBLEBADGE
            //let badge: ZBLEBadge = ZBLEBadge(badgecode: result?.badgeCode, direction: ZBLEBadge.Direction.IN, dirmode: DirMode.DM_IN_OR_OUT)

            //blemanager.sendBadge(<#T##id: UUID##UUID#>, badge: <#T##ZBLEBadge#>)
            // callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.OK));
            //  }
            //otherwise TODO use the SDK to send the badge to the device
     
            

            //if result.success
            commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: resultJson), callbackId: command.callbackId)
        } else {
            result = Result(method: "send")
            commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)
        }
        //else
        //commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)

   
    }


    @objc(stop:) func stop(command : CDVInvokedUrlCommand){
        
        if result != nil {
            result?.method = "stop"
            result?.date = Date()

            //TODO use the SDK to stop the scanning



            result?.success = true

            //if result.success
            commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: resultJson), callbackId: command.callbackId)
            //else
            //commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)

        } else {
            result = Result(method: "stop")
            commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)
        }
    } 

 

}

extension DigitalAccessPlugin: ZBTDeviceManagerProtocol {
    
    func devicesListUpdated(_ device: ZBluetoothLEDevice) {
        // ---
    }
    
    func deviceRemoved(_ device: ZBluetoothLEDevice) {
        // ---
    }
    
    func firstDeviceFavFound(_ device: ZBluetoothLEDevice) {
        // ---
    }
    
    func notifyMainViewSendingBadgeOk(_ badge: ZBLEBadge, device: ZBluetoothLEDevice) {
        // ---
    }
    
    func notifyMainViewSendingBadgeKO(_ badge: ZBLEBadge?, device: ZBluetoothLEDevice?) {
        // ---
    }
    
    func startEditDeviceName(_ device: ZBluetoothLEDevice) {
        // ---
    }
    
    func startEditDeviceDirMode(_ device: ZBluetoothLEDevice) {
        // ---
    }
    
    func bluetoothBecomeAvailable() {
        // ---
    }
    
    func bluetoothBecomeUnavailable() {
        // ---
    }
    
    func foundDeviceNear(_ device: ZBluetoothLEDevice) {
        // ---
    }
    
    func validatedBeacon(_ device: ZBluetoothLEDevice) {
        // ---
    }
    
    func validationErrorBeacon(_device: ZBluetoothLEDevice, error: ZAxessBLEBeaconValidationError) {
        // ---
    }
    
    func firstDeviceFound(_ device: ZBluetoothLEDevice) {
        if result != nil {
            result?.deviceMac = device.mac
            result?.deviceName = "\(device.deviceInfo) - \(device.description)"
            // TODO: SET DIRMODE
           // result?.dirMode = device.dirMode
        }
    }
}
