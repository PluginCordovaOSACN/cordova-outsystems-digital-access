
  // Empty constructor
  function DigitalAccessPlugin() {}
  
  // The function that passes work along to native shells
  // Message is a string, duration may be 'long' or 'short'
  DigitalAccessPlugin.prototype.show = function(message, duration, successCallback, errorCallback) {
    var options = {};
    options.message = message;
    options.duration = duration;
    cordova.exec(successCallback, errorCallback, 'DigitalAccessPlugin', 'show', [options]);
  }
  
  
  
  DigitalAccessPlugin.prototype.scan = function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'DigitalAccessPlugin', 'scan', null);
  }
  
  DigitalAccessPlugin.prototype.send = function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'DigitalAccessPlugin', 'send', null);
  }
  
  DigitalAccessPlugin.prototype.stop = function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'DigitalAccessPlugin', 'stop', null);
  }
  
  
  DigitalAccessPlugin.prototype.init = function(successCallback, errorCallback, timeoutScan, numberOfBadge, dbDistance) {
    cordova.exec(successCallback, errorCallback, 'DigitalAccessPlugin', 'init', [timeoutScan, numberOfBadge, dbDistance]);
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
