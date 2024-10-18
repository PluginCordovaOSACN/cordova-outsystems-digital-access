package com.cordova.plugin.access;

import java.util.List;

public class Request {
    private int timeoutScanSeconds;
    private int dbDistance;
    private String badgeCode;
    private boolean showToast;

    //list of mac address or other identificatives of ble device
    private List<String> devicesEnabled;
    private List<String> devicesDisabled;


    public int getTimeoutScanSeconds() {
        return timeoutScanSeconds;
    }

    public void setTimeoutScanSeconds(int timeoutScanSeconds) {
        this.timeoutScanSeconds = timeoutScanSeconds;
    }

    public int getDbDistance() {
        return dbDistance;
    }

    public void setDbDistance(int dbDistance) {
        this.dbDistance = dbDistance;
    }

    public String getBadgeCode() {
        return badgeCode;
    }

    public void setBadgeCode(String badgeCode) {
        this.badgeCode = badgeCode;
    }

    public boolean isShowToast() {
        return showToast;
    }

    public void setShowToast(boolean showToast) {
        this.showToast = showToast;
    }

    public List<String> getDevicesEnabled() {
        return devicesEnabled;
    }

    public void setDevicesEnabled(List<String> devicesEnabled) {
        this.devicesEnabled = devicesEnabled;
    }

    public List<String> getDevicesDisabled() {
        return devicesDisabled;
    }

    public void setDevicesDisabled(List<String> devicesDisabled) {
        this.devicesDisabled = devicesDisabled;
    }
}
