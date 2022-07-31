package com.cordova.plugin.access;

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
    private Date Enddate;

    private String location;
    private boolean isTimeout;
    private int sdkVersion;
    private String otherMessage;
    private String userDevice;
    private String permissionDenied;

    public String getPermissionDenied() {
        return permissionDenied;
    }

    public void setPermissionDenied(String permissionDenied) {
        this.permissionDenied = permissionDenied;
    }

    public Date getEnddate() {
        return Enddate;
    }

    public void setEnddate(Date enddate) {
        Enddate = enddate;
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

    public int getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(int sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public boolean isTimeout() {
        return isTimeout;
    }

    public void setTimeout(boolean timeout) {
        isTimeout = timeout;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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


}
