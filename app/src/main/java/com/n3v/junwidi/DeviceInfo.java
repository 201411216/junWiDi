package com.n3v.junwidi;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;

/*
통신 및 영상 재생에 필요한 기기 정보를 저장할 객체
 */
public class DeviceInfo implements Parcelable {

    private WifiP2pDevice wifiP2pDevice = null;
    private String str_address;
    private int px_width;
    private int px_height;
    private int dpi;
    private float density;
    private boolean isGroupOwner;

    private int mm_width;
    private int mm_height;

    private int position;

    public void setMm_width(int mm_width) {
        this.mm_width = mm_width;
    }

    public void setMm_height(int mm_height) {
        this.mm_height = mm_height;
    }

    public DeviceInfo(WifiP2pDevice device) {
        wifiP2pDevice = device;
        str_address = "";
        px_width = -1;
        px_height = -1;
        dpi = -1;
        density = -1;
        isGroupOwner = false;
        mm_width = -1;
        mm_height = -1;
        position = -1;
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

    public DeviceInfo(Parcel in) {
        this.wifiP2pDevice = WifiP2pDevice.CREATOR.createFromParcel(in);
        this.str_address = in.readString();
        this.px_width = in.readInt();
        this.px_height = in.readInt();
        this.dpi = in.readInt();
        this.density = in.readFloat();
        this.isGroupOwner = Boolean.valueOf(in.readString());
        this.mm_width = in.readInt();
        this.mm_height = in.readInt();
    }

    public static final Creator<DeviceInfo> CREATOR = new Creator<DeviceInfo>() {
        @Override
        public DeviceInfo createFromParcel(Parcel in) {
            return new DeviceInfo(in);
        }

        @Override
        public DeviceInfo[] newArray(int size) {
            return new DeviceInfo[size];
        }
    };

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

    public int getMm_width() {
        return mm_width;
    }

    public int getMm_height() {
        return mm_height;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
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
        return wifiP2pDevice.deviceAddress + Constants.DELIMITER + str_address + Constants.DELIMITER + px_width + Constants.DELIMITER + px_height + Constants.DELIMITER + dpi + Constants.DELIMITER + density + Constants.DELIMITER
                + isGroupOwner + Constants.DELIMITER + mm_width + Constants.DELIMITER + mm_height;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(wifiP2pDevice, 0);
        parcel.writeString(str_address);
        parcel.writeInt(px_width);
        parcel.writeInt(px_height);
        parcel.writeInt(dpi);
        parcel.writeFloat(density);
        parcel.writeString(Boolean.toString(isGroupOwner));
        parcel.writeInt(mm_width);
        parcel.writeInt(mm_height);
    }

    public void convertPx(){
        DisplayMetrics dm = new DisplayMetrics();
        mm_width = PxToMm(px_width, dm);
        mm_height = PxToMm(px_height, dm);
    }

    public int PxToMm(int value, DisplayMetrics dm){
        return value * dm.densityDpi;
    }
}
