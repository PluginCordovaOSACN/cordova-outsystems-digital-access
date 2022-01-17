package com.cordova.plugin.access;

public class Result {
    private long timeout;
    private String numberOfBadge;
    private String method;
    private String status;
    private String message;
    private String badgeCode;
    private String deviceMac;
    private String deviceName;
    private Integer dbDistance;



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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
