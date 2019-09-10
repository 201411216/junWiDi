package com.n3v.junwidi.Utils;

import android.media.MediaMetadataRetriever;
import android.util.DisplayMetrics;
import android.util.Log;

import com.n3v.junwidi.Datas.DeviceInfo;
import com.n3v.junwidi.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class DeviceInfoListUtil {

    private String TAG = "DeviceInfoListUtil";

    ArrayList<DeviceInfo> deviceInfoArrayList = null;
    ArrayList<DeviceInfo> newDIAL = null;

    public String videoPath = "";

    private int px_video_width = 0;
    private int px_video_height = 0;

    private int video_width_rate = 0;
    private int video_height_rate = 0;

    private int mm_videoview_width = 0;
    private int mm_videoview_height = 0;

    private int mm_min_height = 0;
    private int mm_sum_width = 0;

    //private boolean

    public DeviceInfoListUtil(final ArrayList<DeviceInfo> deviceInfoArrayList, String videoPath) {
        if (deviceInfoArrayList.size() > 0) {
            this.deviceInfoArrayList = deviceInfoArrayList;
        } else {
            Log.e(TAG, "deviceInfoArrayList is empty");
        }
        if (!videoPath.equals("")) {
            this.videoPath = videoPath;
        } else {
            Log.e(TAG, "VideoPath is empty");
        }
    }

    public DeviceInfoListUtil(final ArrayList<DeviceInfo> dial, final DeviceInfo myDeviceInfo, final String videoPath) {
        if (dial.size() > 0) {
            ArrayList<DeviceInfo> tmpDial = new ArrayList<>(dial);
            tmpDial.add(myDeviceInfo);
            newDIAL = tmpDial;
            Log.v(TAG, "newDIAL.size = " + newDIAL.size());
        } else {
            Log.e(TAG, "deviceInfoArrayList is empty");
        }

        if (!videoPath.equals("")) {
            this.videoPath = videoPath;
            StringTokenizer st = new StringTokenizer(videoPath, "/");
            String videoName = "";
            while (st.hasMoreTokens()) {
                videoName = st.nextToken();
            }

            boolean videoCorrect = true;
            for (DeviceInfo di : newDIAL) {
                if (di.isGroupOwner()) {
                    Log.v(TAG, "GroupOwner setPath() : " + videoPath);
                    di.setVideoName(videoPath);
                }
                if (!di.getVideoName().equals(videoName)) {
                    if (di.getVideoName() == "") {
                        di.setVideoName(videoName);
                    } else {
                        if (!di.isGroupOwner()) {
                            videoCorrect = false;
                            Log.v(TAG, "videoName != videoPath");
                            Log.v(TAG, "videoName = " + videoName);
                            Log.v(TAG, "di.getDeviceName = " + di.getWifiP2pDevice().deviceName);
                            Log.v(TAG, "di.getVideoName() = " + di.getVideoName());
                            break;
                        }
                    }
                }
            }
            if (videoCorrect) {
                //this.videoPath = videoPath;
            } else {
                Log.e(TAG, "videoName != videoPath");
            }
        } else {
            Log.e(TAG, "VideoPath is null");
        }
    }

    public void processList() {
        this.makeAndSortPosition();
        this.getVideoSize();
        this.calcSetXYValue();
    }

    public void makeAndSortPosition() {
        List<Integer> wPlusH = new ArrayList<>();
        List<Integer> positionArray = new ArrayList<>();
        int arrIndex = 0;
        for (DeviceInfo di : newDIAL) {
            if (mm_min_height == 0) {
                mm_min_height = di.getMm_height();
                Log.v(TAG, "-1 : di.getMm_height() = " + di.getMm_height());
            } else {
                if (di.getMm_height() < mm_min_height) {
                    mm_min_height = di.getMm_height();
                    Log.v(TAG, "< mmmh : di.getMm_height() = " + di.getMm_height());
                }
            }
            mm_sum_width += di.getMm_width();
            Log.v(TAG, "< mmmh : di.getMm_width() = " + di.getMm_width());
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
            for (DeviceInfo di : newDIAL) {
                if (di.getMm_height() == i && !di.isGroupOwner()) {
                    Log.v(TAG, "setPosition to Client : " + di.getWifiP2pDevice().deviceName + " / " + tmp_position);
                    di.setPosition(tmp_position);
                    tmp_position++;
                }
                if (i == -1 && di.isGroupOwner()) {
                    Log.v(TAG, "setPosition to server : " + di.getWifiP2pDevice().deviceName + " / " + tmp_position);
                    di.setPosition(tmp_position);
                    tmp_position++;
                }
            }
        }

        ArrayList<DeviceInfo> newDeviceInfoList = new ArrayList<>();
        for (int positionIndex = 0; positionIndex < newDIAL.size(); positionIndex++) {
            for (DeviceInfo di : newDIAL) {
                if (di.getPosition() == positionIndex + 1) {
                    Log.v(TAG, "add to newDeviceInfoList : " + di.getWifiP2pDevice().deviceName);
                    newDeviceInfoList.add(di);
                }
            }
        }
        this.newDIAL = newDeviceInfoList;
    }

    public void getVideoSize() {
        int videoWidth = 0;
        int videoHeight = 0;

        int gcd;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Log.v(TAG, videoPath);
        retriever.setDataSource(videoPath);
        videoWidth = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        videoHeight = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        retriever.release();

        Log.v(TAG, "videoWidth = " + videoWidth + " / videoHeight = " + videoHeight);

        this.px_video_width = videoWidth;
        this.px_video_height = videoHeight;

        gcd = getGcd(videoWidth, videoHeight);

        Log.v(TAG, "gcd = " + gcd);

        this.video_width_rate = this.px_video_width / gcd;
        this.video_height_rate = this.px_video_height / gcd;

        Log.v(TAG, "vwrate = " + video_width_rate + " / vhrate = " + video_height_rate);

        Log.v(TAG, "mm_sum_width = " + mm_sum_width + " / mm_min_height = " + mm_min_height);

        if ((mm_sum_width / video_width_rate) * video_height_rate > mm_min_height) {
            mm_videoview_height = mm_min_height;
            mm_videoview_width = (mm_min_height / video_height_rate) * video_width_rate;
        } else {
            mm_videoview_width = mm_sum_width;
            mm_videoview_height = (mm_sum_width / video_width_rate) * video_height_rate;
        }

        Log.v(TAG, "mmvw = " + mm_videoview_width + " / mmvh = " +mm_videoview_height);

        for (DeviceInfo di : newDIAL) {
            di.setMm_videoview_width(this.mm_videoview_width);
            di.setMm_videoview_height(this.mm_videoview_height);
        }
    }

    public void calcSetXYValue() {
        int positionIndex = 1;
        int sumMmWidth = 0;
        for (DeviceInfo di : newDIAL) {
            if (di.getPosition() == positionIndex) {
                di.setSetXValue(di.mmToPx(sumMmWidth));
                sumMmWidth -= di.getMm_width();
                di.setSetYValue(di.mmToPx(di.getMm_height() - mm_videoview_height));
            }
            positionIndex++;
        }
    }

    public int getGcd(final int _a, final int _b) {

        int a = _a;
        int b = _b;

        if (a < b) {
            int tmp = a;
            a = b;
            b = tmp;
        }
        while (b > 0) {
            int tmp = b;
            b = a % b;
            a = tmp;
        }

        return a;
    }

    public ArrayList<DeviceInfo> getResultList() {
        Log.v(TAG, "getResultList. size() = " + newDIAL.size());
        return this.newDIAL;
    }

}
