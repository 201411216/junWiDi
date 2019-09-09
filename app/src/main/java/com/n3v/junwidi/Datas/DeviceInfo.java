package com.n3v.junwidi.Datas;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;

import com.n3v.junwidi.Utils.Constants;

import java.util.StringTokenizer;

/*
통신 및 영상 재생에 필요한 기기 정보를 저장할 객체
 */
public class DeviceInfo implements Parcelable {

    private WifiP2pDevice wifiP2pDevice = null;
    private String brand_Name = "";
    private String model_Name = "";
    private String str_address = "";
    private int px_width = -1;
    private int px_height = -1;
    private int densityDpi = -1;
    private boolean isGroupOwner = false;

    private int mm_width = -1;
    private int mm_height = -1;

    private int position = -1;

    private int mm_videoview_width = -1;
    private int mm_videoview_height = -1;

    private int setXValue = -1;
    private int setYValue = -1;

    private String videoName = "";

    private boolean hasVideo = false;



    public DeviceInfo(WifiP2pDevice device) {
        wifiP2pDevice = device;
    }

    public DeviceInfo(WifiP2pDevice device, String brand_Name, String model_Name, String addr, int width, int height, int densityDpi, boolean isGroupOwner) {
        this.wifiP2pDevice = device;
        this.brand_Name = brand_Name;
        this.model_Name = model_Name;
        this.str_address = addr;
        this.px_width = width;
        this.px_height = height;
        this.densityDpi = densityDpi;
        this.isGroupOwner = isGroupOwner;
        this.mm_width = pxToMm(width);
        this.mm_height = pxToMm(height);
    }

    public DeviceInfo(Parcel in) {
        this.wifiP2pDevice = WifiP2pDevice.CREATOR.createFromParcel(in);
        this.brand_Name = in.readString();
        this.model_Name = in.readString();
        this.str_address = in.readString();
        this.px_width = in.readInt();
        this.px_height = in.readInt();
        this.densityDpi = in.readInt();
        this.isGroupOwner = Boolean.valueOf(in.readString());
        this.mm_width = in.readInt();
        this.mm_height = in.readInt();
        this.position = in.readInt();
        this.mm_videoview_width = in.readInt();
        this.mm_videoview_height = in.readInt();
        this.setXValue = in.readInt();
        this.setYValue = in.readInt();
        this.videoName = in.readString();
        this.hasVideo = Boolean.valueOf(in.readString());
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

    public int getDensityDpi() {
        return densityDpi;
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
        this.mm_width = pxToMm(px_width);
    }

    public void setPx_height(int px_height) {
        this.px_height = px_height;
        this.mm_height = pxToMm(px_height);
    }

    public void setDensityDpi(int densityDpi) {
        this.densityDpi = densityDpi;
    }

    public void setMm_width(int mm_width) {
        this.mm_width = mm_width;
    }

    public void setMm_height(int mm_height) {
        this.mm_height = mm_height;
    }

    public int getMm_videoview_width() {
        return mm_videoview_width;
    }

    public void setMm_videoview_width(int mm_videoview_width) {
        this.mm_videoview_width = mm_videoview_width;
    }

    public int getMm_videoview_height() {
        return mm_videoview_height;
    }

    public void setMm_videoview_height(int mm_videoview_height) {
        this.mm_videoview_height = mm_videoview_height;
    }

    public int getSetXValue() {
        return setXValue;
    }

    public void setSetXValue(int setXValue) {
        this.setXValue = setXValue;
    }

    public int getSetYValue() {
        return setYValue;
    }

    public void setSetYValue(int setYValue) {
        this.setYValue = setYValue;
    }

    public String getBrand_Name() {
        return brand_Name;
    }

    public void setBrand_Name(String brand_Name) {
        this.brand_Name = brand_Name;
    }

    public String getModel_Name() {
        return model_Name;
    }

    public void setModel_Name(String model_Name) {
        this.model_Name = model_Name;
    }

    public boolean isGroupOwner() {
        return isGroupOwner;
    }

    public void setGroupOwner(boolean groupOwner) {
        isGroupOwner = groupOwner;
    }

    public String getString() {
        return wifiP2pDevice.deviceAddress + Constants.DELIMITER + brand_Name + Constants.DELIMITER + model_Name + Constants.DELIMITER + str_address + Constants.DELIMITER + px_width + Constants.DELIMITER + px_height + Constants.DELIMITER + densityDpi + Constants.DELIMITER
                + isGroupOwner + Constants.DELIMITER + mm_width + Constants.DELIMITER + mm_height;
    }

    public String getLongString() {
        return wifiP2pDevice.deviceAddress + Constants.DELIMITER + brand_Name + Constants.DELIMITER + model_Name + Constants.DELIMITER + str_address + Constants.DELIMITER + px_width + Constants.DELIMITER
                + px_height + Constants.DELIMITER + densityDpi + Constants.DELIMITER + isGroupOwner + Constants.DELIMITER + mm_width + Constants.DELIMITER + mm_height + Constants.DELIMITER + position
                + Constants.DELIMITER + mm_videoview_width + Constants.DELIMITER + mm_videoview_height + Constants.DELIMITER + setXValue + Constants.DELIMITER + setYValue + Constants.DELIMITER + videoName
                + Constants.DELIMITER + hasVideo;
    }

    public boolean isHasVideo() {
        return hasVideo;
    }

    public void setHasVideo(boolean hasVideo) {
        this.hasVideo = hasVideo;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(wifiP2pDevice, 0);
        parcel.writeString(brand_Name);
        parcel.writeString(model_Name);
        parcel.writeString(str_address);
        parcel.writeInt(px_width);
        parcel.writeInt(px_height);
        parcel.writeInt(densityDpi);
        parcel.writeString(Boolean.toString(isGroupOwner));
        parcel.writeInt(mm_width);
        parcel.writeInt(mm_height);
        parcel.writeInt(position);
        parcel.writeInt(mm_videoview_width);
        parcel.writeInt(mm_videoview_height);
        parcel.writeInt(setXValue);
        parcel.writeInt(setYValue);
        parcel.writeString(videoName);
        parcel.writeString(Boolean.toString(hasVideo));
    }

    public void convertPx() {
        mm_width = pxToMm(px_width);
        mm_height = pxToMm(px_height);
    }

    public int pxToMm(int value) {
        return value * this.densityDpi;
    }

    public int mmToPx(int value) {
        return value / this.densityDpi;
    }

}
