package com.cordova.plugin.access;

import android.bluetooth.BluetoothDevice;

import java.util.Date;

public class Result {
    private long timeout;
    private String numberOfBadge;
    private String method;
    private boolean success;
    private String message;
    private String badgeCode;
    private String deviceMac;
    private String deviceName;
    private Integer dbDistance;
    private Date date;
    private Date startDateScanning;
    private Date startDateSending;

    private Date endDate;

    private String location;
    private boolean isTimeout;
    private int sdkVersion;
    private String otherMessage;
    private String userDevice;
    private String permissionDenied;
    private String idExecution; //Badge+timestart
    private long timeOfExecution;


    private boolean isScanning;
    private boolean isSending;
    private boolean isAlreadyScan;
    private boolean showToast;
    private boolean isTerminated;

    BluetoothDevice bleFounded;

    public boolean isTerminated() {
        return isTerminated;
    }

    public void setTerminated(boolean terminated) {
        isTerminated = terminated;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getNumberOfBadge() {
        return numberOfBadge;
    }

    public void setNumberOfBadge(String numberOfBadge) {
        this.numberOfBadge = numberOfBadge;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getBadgeCode() {
        return badgeCode;
    }

    public void setBadgeCode(String badgeCode) {
        this.badgeCode = badgeCode;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Integer getDbDistance() {
        return dbDistance;
    }

    public void setDbDistance(Integer dbDistance) {
        this.dbDistance = dbDistance;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getStartDateScanning() {
        return startDateScanning;
    }

    public void setStartDateScanning(Date startDateScanning) {
        this.startDateScanning = startDateScanning;
    }

    public Date getStartDateSending() {
        return startDateSending;
    }

    public void setStartDateSending(Date startDateSending) {
        this.startDateSending = startDateSending;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isTimeout() {
        return isTimeout;
    }

    public void setTimeout(boolean timeout) {
        isTimeout = timeout;
    }

    public int getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(int sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public String getOtherMessage() {
        return otherMessage;
    }

    public void setOtherMessage(String otherMessage) {
        this.otherMessage = otherMessage;
    }

    public String getUserDevice() {
        return userDevice;
    }

    public void setUserDevice(String userDevice) {
        this.userDevice = userDevice;
    }

    public String getPermissionDenied() {
        return permissionDenied;
    }

    public void setPermissionDenied(String permissionDenied) {
        this.permissionDenied = permissionDenied;
    }

    public String getIdExecution() {
        return idExecution;
    }

    public void setIdExecution(String idExecution) {
        this.idExecution = idExecution;
    }

    public long getTimeOfExecution() {
        return timeOfExecution;
    }

    public void setTimeOfExecution(long timeOfExecution) {
        this.timeOfExecution = timeOfExecution;
    }

    public boolean isScanning() {
        return isScanning;
    }

    public void setScanning(boolean scanning) {
        isScanning = scanning;
    }

    public boolean isSending() {
        return isSending;
    }

    public void setSending(boolean sending) {
        isSending = sending;
    }

    public boolean isAlreadyScan() {
        return isAlreadyScan;
    }

    public void setAlreadyScan(boolean alreadyScan) {
        isAlreadyScan = alreadyScan;
    }

    public boolean isShowToast() {
        return showToast;
    }

    public void setShowToast(boolean showToast) {
        this.showToast = showToast;
    }

    public BluetoothDevice getBleFounded() {
        return bleFounded;
    }

    public void setBleFounded(BluetoothDevice bleFounded) {
        this.bleFounded = bleFounded;
    }
}

