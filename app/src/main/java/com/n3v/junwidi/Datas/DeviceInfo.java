package com.n3v.junwidi.Datas;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.TypedValue;

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

    private boolean guidelineReady = false;

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

    public DeviceInfo(DeviceInfo di) {
        this.wifiP2pDevice = di.getWifiP2pDevice();
        this.brand_Name = di.getBrand_Name();
        this.model_Name = di.getModel_Name();
        this.str_address = di.getStr_address();
        this.px_width = di.getPx_width();
        this.px_height = di.getPx_height();
        this.densityDpi = di.getDensityDpi();
        this.isGroupOwner = di.isGroupOwner();
        this.mm_width = di.getMm_width();
        this.mm_height = di.getMm_height();
        this.position = di.getPosition();
        this.mm_videoview_width = di.getMm_videoview_width();
        this.mm_videoview_height = di.getMm_videoview_height();
        this.setXValue = di.getSetXValue();
        this.setYValue = di.getSetYValue();
        this.videoName = getVideoName();
        this.hasVideo = di.isHasVideo();
        this.guidelineReady = di.isGuidelineReady();
    }

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
                + Constants.DELIMITER + hasVideo + Constants.DELIMITER + guidelineReady;
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

    public boolean isGuidelineReady() {
        return guidelineReady;
    }

    public void setGuidelineReady(boolean guidelineReady) {
        this.guidelineReady = guidelineReady;
    }

    public void convertPx() {
        mm_width = pxToMm(px_width);
        mm_height = pxToMm(px_height);
    }

    public int pxToMm(int value) {
        return (int) (value * 25.4 / this.densityDpi);
        //return (int)(value / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1, dm));
    }

    public int mmToPx(int value) {
        return (int) (value * this.densityDpi / 25.4);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.wifiP2pDevice, flags);
        dest.writeString(this.brand_Name);
        dest.writeString(this.model_Name);
        dest.writeString(this.str_address);
        dest.writeInt(this.px_width);
        dest.writeInt(this.px_height);
        dest.writeInt(this.densityDpi);
        dest.writeByte(this.isGroupOwner ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mm_width);
        dest.writeInt(this.mm_height);
        dest.writeInt(this.position);
        dest.writeInt(this.mm_videoview_width);
        dest.writeInt(this.mm_videoview_height);
        dest.writeInt(this.setXValue);
        dest.writeInt(this.setYValue);
        dest.writeString(this.videoName);
        dest.writeByte(this.hasVideo ? (byte) 1 : (byte) 0);
        dest.writeByte(this.guidelineReady ? (byte) 1 : (byte) 0);
    }

    protected DeviceInfo(Parcel in) {
        this.wifiP2pDevice = in.readParcelable(WifiP2pDevice.class.getClassLoader());
        this.brand_Name = in.readString();
        this.model_Name = in.readString();
        this.str_address = in.readString();
        this.px_width = in.readInt();
        this.px_height = in.readInt();
        this.densityDpi = in.readInt();
        this.isGroupOwner = in.readByte() != 0;
        this.mm_width = in.readInt();
        this.mm_height = in.readInt();
        this.position = in.readInt();
        this.mm_videoview_width = in.readInt();
        this.mm_videoview_height = in.readInt();
        this.setXValue = in.readInt();
        this.setYValue = in.readInt();
        this.videoName = in.readString();
        this.hasVideo = in.readByte() != 0;
        this.guidelineReady = in.readByte() != 0;
    }

    public static final Creator<DeviceInfo> CREATOR = new Creator<DeviceInfo>() {
        @Override
        public DeviceInfo createFromParcel(Parcel source) {
            return new DeviceInfo(source);
        }

        @Override
        public DeviceInfo[] newArray(int size) {
            return new DeviceInfo[size];
        }
    };
}
