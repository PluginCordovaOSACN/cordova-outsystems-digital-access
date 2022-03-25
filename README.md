# cordova-outsystems-digital-access
Cordova Plugin for integrate OutSystems with digital access



If you integrate this plugin with Android remove check if there are this rows:

	<uses-permission android:name="android.permission.BLUETOOTH"
	android:maxSdkVersion="30" />
	<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"
	android:maxSdkVersion="30" />

if it exists should be replaced with:

	<uses-permission android:name="android.permission.BLUETOOTH"/>
	<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />



Methods

init
initialize the plugin

	window.plugins.digitalAccessPlugin.init(function(jsonResult) {
    			console.log(jsonResult);
		}, function(error) {
    			console.error(msg);
		},<<timeout>>,<<DBDistance>>, <<NameBuilding>>,<<BadgeDefault>>);
	
Note: 	Android timeout millisencods 
	iOS timeout seconds	

checkBluetooth
check if the bluetooth is on
	
	window.plugins.digitalAccessPlugin.checkBluetooth(function(jsonResult) {
	    console.log(jsonResult);
	}, function(error) {
	    console.error(msg);
	});
	
	
scan
Search for the nearby Bluetooth devices
	
	window.plugins.digitalAccessPlugin.scan(function(jsonResult) {
	    console.log(jsonResult);
	}, function(error) {
	    console.error(msg);
	},<<BadgeCode>>);
	
Note: 
badgeCode should be a number of 5 or 6 digits
!the Bluetooth must on

	
	
send
Send the badge code to the Bluetooth device detected with the scan method
	
	window.plugins.digitalAccessPlugin.send(function(jsonResult) {
	    console.log(jsonResult);
	}, function(error) {
	    console.error(msg);
	},<<IsAccessing>>);

Note: 
IsAccessing (true or false) is not mandatory for Android
	
