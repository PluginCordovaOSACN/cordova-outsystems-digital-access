import Foundation
import ZAxessCommonObjects
import ZAxessBLELibrarySaipem
import UIKit
@objc(DigitalAccessPlugin) class DigitalAccessPlugin: CDVPlugin {
    public var result: Result?
    private let dbMinDistance = -40
    private let dbMaxDistance = -110
    private let fakeDevice = "FakeDevice"
    private var blemanager: ZBTDeviceManager!
    private var dispatchGroup: DispatchGroup?
    private var resultJson: String {
        let jsonEncoder = JSONEncoder()
            jsonEncoder.dateEncodingStrategy = .iso8601
        let jsonData = (try? jsonEncoder.encode(self.result))!
        return String(data: jsonData, encoding: String.Encoding.utf8) ?? ""
    }
    @objc(init:) func initialize(command : CDVInvokedUrlCommand){
        let timeoutScan = command.argument(at: 0) as! Int64
        let dbDistance = command.argument(at: 1) as! Int
        let buildingDefault = command.argument(at: 2) as! String
        let badgeCode = command.argument(at: 3) as! String
		let distanceMeter = command.argument(at: 4) as! Int

        if blemanager == nil {
            blemanager = ZBTDeviceManager()
            blemanager.delegate = self
            result = Result(method: "init")
        }
        result?.method = "init"
        result?.timeout = timeoutScan
        result?.dbDistance = -dbDistance
        result?.location = buildingDefault
        result?.badgeCode = badgeCode
        result?.success = true
        result?.date = Date()
		result?.distanceMeter = distanceMeter

       // if dbMinDistance > dbDistance || dbDistance > dbMaxDistance {
       //     result?.success = false
       //     result?.message = "db distance is not valid. Dbdistance must be inside this range: \(dbMinDistance) NEAR to \(dbMaxDistance) FAR\nTo set the dbDistance parameter use the method: init(callback, callbackError, timeout, numberOfBadge, dbDistance))"
       //     commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)
       // } else {
            commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: resultJson), callbackId: command.callbackId)
       // }
    }
    @objc(checkBluetooth:) func checkBluetooth(command : CDVInvokedUrlCommand){
        if result != nil {
            result?.method = "checkBluetooth"
            if self.blemanager.bluetoothEnabled ?? false {
                result?.success = true
                result?.date = Date()
                commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: resultJson), callbackId: command.callbackId)
            } else {
                result?.success = false
                result?.date = Date()
                result?.message = "Please turn on the bluetooth"
                commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)
            }
        } else {
            result = Result(method: "checkBluetooth")
            result?.message = "Please use the method init(callback, callbackError, timeout, numberOfBadge, dbDistance) before use the method checkBluetooth"
            commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)
        }
    }
    @objc(scan:) func scan(command : CDVInvokedUrlCommand){
        let badgeCode = command.argument(at: 0) as? String
        let isUsingFakeDevice = command.argument(at: 1) as? Bool // not mandatory default false
        if result != nil {
            guard let badgeCode = badgeCode else {
                result?.message = "Please set a badgeCode in the command"
                commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)
                return
            }
            result?.method = "scan"
            result?.date = Date()
            result?.userDevice = "iOS"
            result?.badgeCode = badgeCode
            if isUsingFakeDevice ?? false == true {
                result?.success = true
                result?.message = "FakeDevice found"
                result?.deviceName = fakeDevice
                commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: resultJson), callbackId: command.callbackId)
            } else {
                DispatchQueue.global(qos: .default).async {
                    self.dispatchGroup = DispatchGroup()
                    self.dispatchGroup?.enter()
                    self.blemanager.refreshScan()
                    self.blemanager.updateDevicesList()
                    let time = Double(self.result?.timeout ?? 0)
                    let elapsed = self.dispatchGroup?.wait(timeout: .now() + time)
                    self.dispatchGroup = nil
                    if elapsed == .success {
                        if self.result?.deviceId != nil {
                            self.result?.isTimeout = false
                            self.result?.success = true
                            self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: self.resultJson), callbackId: command.callbackId)
                            self.result?.deviceId = nil

                        } else {
                            self.result?.success = false
                            self.result?.message = "No device founded"
                            self.result?.isTimeout = true
                            self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: self.resultJson), callbackId: command.callbackId)
                        }
                    } else {
                        self.result?.success = false
                        self.result?.message = "Timeout of scan"
                        self.result?.isTimeout = true
                        self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: self.resultJson), callbackId: command.callbackId)
                    }
                }
            }
        } else {
            result = Result(method: "scan")
            result?.message = "Please use the method init(callback, callbackError, timeout, numberOfBadge, dbDistance) before use the method scan"
            commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)
        }
    }
    @objc(send:) func send(command : CDVInvokedUrlCommand){
        let dir = command.argument(at: 0) as? Bool // not mandatory default false
        let deviceId = command.argument(at: 1) as? String

 
        let direction: ZBLEBadge.Direction = dir ?? false ? .IN : .OUT
        if result != nil {
            result?.userDevice = "iOS"
            result?.method = "send"
            result?.date = Date()
            result?.success = true
            result?.message = "Badge sended"
            result?.deviceId = deviceId
            do {
                let badge =  try ZBLEBadge(badgecode: UInt64(result?.badgeCode ?? "") ?? 0000, direction: direction, dirmode: DirMode.DM_IN_OR_OUT)
                let deviceId = UUID(uuidString: result?.deviceId ?? "")!
                result?.numberOfBadge = "\(badge)"
                blemanager.sendBadge(deviceId, badge: badge)
                commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: resultJson), callbackId: command.callbackId)
            } catch {
                result?.success = false
                result?.message = error.localizedDescription
                commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)
            }
        } else {
            result = Result(method: "send")
            result?.message = "Please use the method init(callback, callbackError, timeout, numberOfBadge, dbDistance) before use the method send"
            commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)
        }
    }
    @objc(stop:) func stop(command : CDVInvokedUrlCommand){
        if result != nil {
            result?.method = "stop"
            result?.date = Date()
            result?.otherMessage = "stop scanning"
            result?.deviceMac = ""
            result?.deviceName = ""
            result?.deviceId = ""
            result?.isTimeout = false
            result?.success = true
            self.blemanager.stopScanning()
            self.blemanager.resetDevicesList()
            self.dispatchGroup?.leave()
            commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: resultJson), callbackId: command.callbackId)
        } else {
            result = Result(method: "stop")
            result?.message = "Please use the method init(callback, callbackError, timeout, numberOfBadge, dbDistance) before use the method stop"
            commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)
        }
    }
}
extension DigitalAccessPlugin: ZBTDeviceManagerProtocol {
    func devicesListUpdated(_ device: ZBluetoothLEDevice) {
        if result != nil && result?.dbDistance ?? 0 >= device.distance.intValue && device.isCloseEnough && device.meterDistance.intValue <= 2 {
            dispatchGroup?.leave()
            result?.otherMessage = "devicesListUpdated"
            result?.sdkVersion = device.distance.intValue
            result?.deviceMac = device.mac
            result?.deviceName = "\(device.deviceInfo) - \(device.description)"
            result?.deviceId = "\(device.id)"
            result?.deviceEnabled = device.isEnabled
            result?.deviceIconsVisible = device.iconsVisible
            result?.deviceIconReversed = device.reverseIcons
            result?.deviceCloseEnough = device.isCloseEnough
            result?.deviceIsAlive = device.isAlive
            result?.meterDistance = device.meterDistance.intValue
            result?.isCloseEnoughBK = device.isCloseEnoughBK

           /* let badge =  try ZBLEBadge(badgecode: UInt64(result?.badgeCode ?? "") ?? 0000, direction: direction, dirmode: DirMode.DM_IN_OR_OUT)
            let deviceId = UUID(uuidString: result?.deviceId ?? "")!
            result?.numberOfBadge = "\(badge)"
             blemanager.sendBadge(deviceId, badge: badge)*/
          //  result?.minDistance = device.minDistance ?? 0 device.minDistance.intValue
            //result?.minDistanceBK = device.minDistanceBK ?? 0 device.minDistanceBK.intValue
        //TODO: SET DIRMODE
        //result?.dirMode = device.dirMode
        }
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
        blemanager?.updateDevicesList()
    }
    func bluetoothBecomeUnavailable() {
        // ---
    }
    func foundDeviceNear(_ device: ZBluetoothLEDevice) {
        //dispatchGroup?.leave()
        //result?.otherMessage = "foundDeviceNear"
        //result?.sdkVersion = device.distance.intValue
        //if result != nil && result?.dbDistance ?? 0 >= device.distance.intValue {
        //    result?.deviceMac = device.mac
         //   result?.deviceName = "\(device.deviceInfo) - \(device.description)"
         //   result?.deviceId = "\(device.id)"
            // TODO: SET DIRMODE
           // result?.dirMode = device.dirMode
        //}
    }
    func validatedBeacon(_ device: ZBluetoothLEDevice) {
        // ---
    }
    func validationErrorBeacon(_device: ZBluetoothLEDevice, error: ZAxessBLEBeaconValidationError) {
        // ---
    }
    func firstDeviceFound(_ device: ZBluetoothLEDevice) {
       // dispatchGroup?.leave()
       // result?.otherMessage = "firstDeviceFound"
       // result?.sdkVersion = device.distance.intValue
       // if result != nil {
       //     result?.deviceMac = device.mac
        //    result?.deviceName = "\(device.deviceInfo) - \(device.description)"
        //    result?.deviceId = "\(device.id)"
        //    result?.sdkVersion = device.distance.intValue
           // TODO: SET DIRMODE
           // result?.dirMode = device.dirMode
       // }
    }
}
