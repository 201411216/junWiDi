package com.n3v.junwidi.Utils;

import android.media.MediaMetadataRetriever;
import android.util.DisplayMetrics;

import com.n3v.junwidi.Datas.DeviceInfo;
import com.n3v.junwidi.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeviceInfoListUtil {

    ArrayList<DeviceInfo> deviceInfoArrayList = null;

    public String videoPath = "";

    public DeviceInfoListUtil(final ArrayList<DeviceInfo> deviceInfoArrayList) {
        this.deviceInfoArrayList = deviceInfoArrayList;
    }

    public DeviceInfoListUtil(final ArrayList<DeviceInfo> dial, final DeviceInfo myDeviceInfo, final String videoPath) {
        ArrayList<DeviceInfo> tmpDial = dial;
        tmpDial.add(myDeviceInfo);
        this.deviceInfoArrayList = tmpDial;
        this.videoPath = videoPath;
    }

    public void getVideoSize() {
        int videoWidth = 0;
        int videoHeight = 0;

        DisplayMetrics dm = new DisplayMetrics();

        int temp;
        //해상도 넓이,높이값 최대공약수
        int gcd;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //retriever.setDataSource("android.resource://" + getPackageName() + "/" + R.raw.test2);
        videoWidth = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        videoHeight = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        retriever.release();
        int a = videoWidth;
        int b = videoHeight;
        while (a != 0) {
            if (a < b) {
                temp = a;
                a = b;
                b = temp;
            }
            a = a - b;
        }
        gcd = b;
    }



    public void makePosition() {

    }

    public ArrayList<DeviceInfo> calcDeviceList(){
        ArrayList<DeviceInfo> tempArr = new ArrayList<>();
//        tempArr.addAll(myDeviceInfoList);
//        myDeviceInfo.convertPx();
//        tempArr.add(myDeviceInfo);

        List<Integer> wPlusH = new ArrayList<>();
        List<Integer> positionArray = new ArrayList<>();

        int arrIndex = 0;
        for (DeviceInfo di : tempArr) {
            if (!di.isGroupOwner()) {
                wPlusH.add(di.getMm_height());
            } else {
                wPlusH.add(-1);
            }
            arrIndex++;
        }
        Collections.sort(wPlusH, Collections.<Integer>reverseOrder());
        int tmp_position = 1;
        for (int i : wPlusH) {
            for (DeviceInfo di : tempArr) {
                if (di.getMm_height() == i) {
                    di.setPosition(tmp_position);
                    tmp_position++;
                }
                if (i == -1 && di.isGroupOwner()) {
                    di.setPosition(tmp_position);
                }
            }
        }


        return tempArr;
    }

    public int PxToMm(int value, DisplayMetrics dm){
        return value * dm.densityDpi;
    }

    //public int

}
