import Foundation


@objc(DigitalAccessPlugin) class DigitalAccessPlugin: CDVPlugin{

    public static var result:Result

    private static var dbMinDistance = 40
    private static var dbMaxDistance = 110

    private static var fakeDevice = "FakeDevice"



    @objc(init:) func init(command : CDVInvokedUrlCommand){
        let timeoutScan = command.argument(at: 0) as! Double
        let dbDistance = command.argument(at: 1) as! Int
        let buildingDefault = command.argument(at: 2) as! String
        let badgeCode = command.argument(at: 3) as! String

        result = Result(method: 'init')
        result.timeout = timeoutScan
        /*TODO check dbDistance should be > dbMinDistance and < dbMaxDistance and set success to false and add this message: 
        "db distance is not valid. Dbdistance must be inside this range: " + dbMinDistance + " NEAR to " + dbMaxDistance + " FAR\n" +
                    "To set the dbDistance parameter use the method: init(callback, callbackError, timeout, numberOfBadge, dbDistance))"

        */
        result.dbDistance = dbDistance
        result.location = buildingDefault
        result.badgeCode = badgeCode
     /* TODO 
     check bluetooth actived
     otherwise ask to the user to activated  
*/

        result.success = true
        result.date = Date()

//TODO encode the result to json
        let jsonEncoder = JSONEncoder()
        let jsonData = try jsonEncoder.encode(result)
        let resultJson = String(data: jsonData, encoding: String.Encoding.utf16)

        //if result.success
        commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: resultJson), callbackId: command.callbackId)
        //else
        //commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)



    }

    @objc(checkBluetooth:) func checkBluetooth(command : CDVInvokedUrlCommand){
    
    //if exist result
    result.method = 'checkBluetooth'
    //otherwise -> create result and set init

    //TODO return Result.success=true if bluetooth is actived otherwise ask to the user to active and after return the result 


        result.success = true
        result.date = Date()
        //TODO encode the result to json
        let jsonEncoder = JSONEncoder()
        let jsonData = try jsonEncoder.encode(result)
        let resultJson = String(data: jsonData, encoding: String.Encoding.utf16)

        //if result.success
        commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: resultJson), callbackId: command.callbackId)
        //else
        //commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)



    }

    @objc(scan:) func scan(command : CDVInvokedUrlCommand){
    
        let badgeCode = command.argument(at: 0) as! String
        let isUsingFakeDevice = command.argument(at: 1) as! Bool // not mandatory default false

        result.setMethod = "scan"
        result.date = Date()

        //if result nill or result.timeout or result.dbdistance are nill or badgeCode nil then Result.success=false

        //otherwise
        result.badgeCode = badgeCode

        //if isUsingFakeDevice then simulate fakeDevice
            result.setSuccess = true
            result.setMessage = "FakeDevice found";
            result.setDeviceName = fakeDevice;
        //otherwise TODO use the SDK to check the device


        //TODO encode the result to json
        let jsonEncoder = JSONEncoder()
        let jsonData = try jsonEncoder.encode(result)
        let resultJson = String(data: jsonData, encoding: String.Encoding.utf16)

        //if result.success
        commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: resultJson), callbackId: command.callbackId)
        //else
        //commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)



    }


    @objc(send:) func send(command : CDVInvokedUrlCommand){
    
        result.setMethod="send";
        result.date = Date()

        //if result nill or result.timeout or result.dbdistance are nill or badgeCode nil then Result.success=false


        //simulate access with fake reader
        //if fakeDevice = result.deviceName {
            result.setSuccess(true);
            result.setMessage("Badge sended");

            callbackContext.sendPluginResult(getPluginResult(PluginResult.Status.OK));
        //  }
        //otherwise TODO use the SDK to send the badge to the device
 
        
        //TODO encode the result to json
        let jsonEncoder = JSONEncoder()
        let jsonData = try jsonEncoder.encode(result)
        let resultJson = String(data: jsonData, encoding: String.Encoding.utf16)

        //if result.success
        commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: resultJson), callbackId: command.callbackId)
        //else
        //commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)

   
    }


    @objc(stop:) func stop(command : CDVInvokedUrlCommand){
        result.setMethod="stop";
        result.date = Date()

        //TODO use the SDK to stop the scanning



        result.success = true
     //TODO encode the result to json
        let jsonEncoder = JSONEncoder()
        let jsonData = try jsonEncoder.encode(result)
        let resultJson = String(data: jsonData, encoding: String.Encoding.utf16)

        //if result.success
        commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: resultJson), callbackId: command.callbackId)
        //else
        //commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: resultJson), callbackId: command.callbackId)

   

    } 

 

}
