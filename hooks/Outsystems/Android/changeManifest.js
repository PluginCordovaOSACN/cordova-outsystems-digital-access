var fs = require('fs');
var path = require('path');

var constants = {
    manifestPath:"platforms/android/app/src/main/AndroidManifest.xml"
}

module.exports = function (context) {
    
    console.log("Start changing Manifest!");
    var Q = require("q");
    var deferral = new Q.defer();


    var manifestContent = fs.readFileSync(constants.manifestPath, {encoding:'utf8'});

    
    manifestContent = manifestContent.replaceAll("android:maxSdkVersion=\"30","");


    fs.writeFileSync(constants.manifestPath, manifestContent);
    
    console.log("Finished changing Manifest!");

    deferral.resolve();
 
    return deferral.promise;
}