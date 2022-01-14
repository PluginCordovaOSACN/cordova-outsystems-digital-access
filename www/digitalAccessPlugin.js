cordova.define("cordova-plugin-access.DigitalAccessPlugin", function(require, exports, module) {
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
  
  
  DigitalAccessPlugin.prototype.init = function(successCallback, errorCallback, timeoutScan) {
    cordova.exec(successCallback, errorCallback, 'DigitalAccessPlugin', 'init', [timeoutScan]);
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
  });
  