 // Empty constructor
 function DigitalAccessPlugin() {}
  
 // The function that passes work along to native shells
 // Message is a string, duration may be 'long' or 'short'

 
 
 DigitalAccessPlugin.prototype.scan = function(successCallback, errorCallback, badgeCode, isUsingFakeDevice) {
   cordova.exec(successCallback, errorCallback, 'DigitalAccessPlugin', 'scan', [badgeCode, isUsingFakeDevice]);
 }
 
 DigitalAccessPlugin.prototype.send = function(successCallback, errorCallback, isComing, deviceId) {
   cordova.exec(successCallback, errorCallback, 'DigitalAccessPlugin', 'send', [isComing, deviceId]);
 }
 
 DigitalAccessPlugin.prototype.sendBle = function(successCallback, errorCallback, MACAddress) {
  cordova.exec(successCallback, errorCallback, 'DigitalAccessPlugin', 'sendBle', [MACAddress]);
}

 DigitalAccessPlugin.prototype.stop = function(successCallback, errorCallback) {
   cordova.exec(successCallback, errorCallback, 'DigitalAccessPlugin', 'stop', null);
 }
 
 
 DigitalAccessPlugin.prototype.init = function(successCallback, errorCallback, timeoutScan, dbDistance, building, badgeCode, distanceIosMeter) {
  cordova.exec(successCallback, errorCallback, 'DigitalAccessPlugin', 'init', [timeoutScan, dbDistance, building, badgeCode,distanceIosMeter ]);    
 }

 DigitalAccessPlugin.prototype.checkBluetooth = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'DigitalAccessPlugin', 'checkBluetooth',[]);
}

 DigitalAccessPlugin.prototype.allInOneEvent = function(successCallback, errorCallback, requestJSON) {
   cordova.exec(successCallback, errorCallback, 'DigitalAccessPlugin', 'allInOneEvent', [requestJSON]);
 }

 DigitalAccessPlugin.prototype.getResult = function(successCallback, errorCallback) {
	cordova.exec(successCallback, errorCallback, 'DigitalAccessPlugin', 'getResult');
 }

 DigitalAccessPlugin.prototype.stopAllInOneEvent = function(successCallback, errorCallback) {
     cordova.exec(successCallback, errorCallback, 'DigitalAccessPlugin', 'stopAllInOneEvent');
 }
 
 
 // Installation constructor that binds DigitalAccessPlugin to window
 DigitalAccessPlugin.install = function() {
   if (!window.plugins) {
     window.plugins = {};
   }
   window.plugins.digitalAccessPlugin = new DigitalAccessPlugin();
   return window.plugins.digitalAccessPlugin;
 };
 cordova.addConstructor(DigitalAccessPlugin.install);