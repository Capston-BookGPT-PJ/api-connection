package com.example.meltingbooks.network;

public class TokenRequestBody {
    private String token;
    private String deviceInfo;

    public TokenRequestBody(String token, String deviceInfo) {
        this.token = token;
        this.deviceInfo = deviceInfo;
    }
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}

