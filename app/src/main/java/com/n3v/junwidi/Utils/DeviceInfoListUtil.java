package com.n3v.junwidi.Utils;

import android.util.DisplayMetrics;

import com.n3v.junwidi.Datas.DeviceInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeviceInfoListUtil {

    ArrayList<DeviceInfo> deviceInfoArrayList = null;

    public DeviceInfoListUtil(final ArrayList<DeviceInfo> deviceInfoArrayList) {
        this.deviceInfoArrayList = deviceInfoArrayList;
    }

    public DeviceInfoListUtil(final ArrayList<DeviceInfo> dial, final DeviceInfo myDeviceInfo) {
        ArrayList<DeviceInfo> tmpDial = dial;
        tmpDial.add(myDeviceInfo);
        this.deviceInfoArrayList = tmpDial;
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
