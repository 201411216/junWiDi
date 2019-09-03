package com.n3v.junwidi;

import android.net.wifi.p2p.WifiP2pDevice;

/*
통신 및 영상 재생에 필요한 기기 정보를 저장할 객체
 */
public class DeviceInfo {

    private WifiP2pDevice wifiP2pDevice;
    private String str_address;
    private int px_width;
    private int px_height;
    private int dpi;
    private float density;
    private boolean isGroupOwner;

    public DeviceInfo(WifiP2pDevice device) {
        wifiP2pDevice = device;
        str_address = "";
        px_width = -1;
        px_height = -1;
        dpi = -1;
        density = -1;
        isGroupOwner = false;
    }

    public DeviceInfo(WifiP2pDevice device, String addr, int width, int height, int dpi, float density, boolean isGroupOwner) {
        this.wifiP2pDevice = device;
        this.str_address = addr;
        this.px_width = width;
        this.px_height = height;
        this.dpi = dpi;
        this.density = density;
        this.isGroupOwner = isGroupOwner;
    }

    public int getPx_width() {
        return px_width;
    }

    public int getPx_height() {
        return px_height;
    }

    public int getDpi() {
        return dpi;
    }

    public float getDensity() {
        return density;
    }

    public WifiP2pDevice getWifiP2pDevice() {
        return wifiP2pDevice;
    }

    public String getStr_address() {
        return str_address;
    }

    public void setWifiP2pDevice(WifiP2pDevice wifiP2pDevice) {
        this.wifiP2pDevice = wifiP2pDevice;
    }

    public void setStr_address(String str_address) {
        this.str_address = str_address;
    }

    public void setPx_width(int px_width) {
        this.px_width = px_width;
    }

    public void setPx_height(int px_height) {
        this.px_height = px_height;
    }

    public void setDpi(int dpi) {
        this.dpi = dpi;
    }

    public void setDensity(float density) {
        this.density = density;
    }

    public boolean isGroupOwner() {
        return isGroupOwner;
    }

    public void setGroupOwner(boolean groupOwner) {
        isGroupOwner = groupOwner;
    }

    public String getString() {
        return wifiP2pDevice.deviceAddress + "+=+" + str_address + "+=+" + px_width + "+=+" + px_height + "+=+" + dpi + "+=+" + density + "+=+" + isGroupOwner;
    }
}
