# cordova-outsystems-digital-access
Cordova Plugin for integrate OutSystems with digital access



If you integrate this plugin with android remove check if there are this rows:

  <uses-permission android:name="android.permission.BLUETOOTH"
                     android:maxSdkVersion="30" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"
                     android:maxSdkVersion="30" />

if it exists should be replaced with:
  <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
