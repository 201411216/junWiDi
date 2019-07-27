package com.n3v.junwidi;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/*
Server Activity 의 ListView 가 사용할 ArrayAdapter.
DeviceInfoList 의 정보를 ListView 에 띄워줌.

view.setText() 의 괄호 안의 내용은 String 만 받으므로 다른 type 의 데이터는 형변환을 해줘야함
 */
public class MyServerAdapter extends ArrayAdapter<DeviceInfo> {

    private static final String TAG = "MyServerAdapter";

    private ArrayList<DeviceInfo> myDeviceInfoList;
    private Context myContext;
    private int myResource;

    public MyServerAdapter(Context context, int resource, ArrayList<DeviceInfo> deviceInfoList) {
        super(context, resource, deviceInfoList);
        myContext = context;
        myResource = resource;
        myDeviceInfoList = deviceInfoList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater li = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = li.inflate(this.myResource, null);
        }
        DeviceInfo tempDevice = myDeviceInfoList.get(position);
        if (tempDevice != null) {
            TextView item_Client_Device_Name = (TextView) v.findViewById(R.id.item_client_device_name);
            TextView item_Client_Device_Model = (TextView) v.findViewById(R.id.item_client_device_model);
            TextView item_Client_Device_State = (TextView) v.findViewById(R.id.item_client_device_state);
            TextView item_Client_Device_Address = (TextView) v.findViewById(R.id.item_client_device_address);
            TextView item_Client_Device_Width_Px = (TextView) v.findViewById(R.id.item_client_device_width_px);
            TextView item_Client_Device_Height_Px = (TextView) v.findViewById(R.id.item_client_device_height_px);
            TextView item_Client_Device_Dpi = (TextView) v.findViewById(R.id.item_client_device_dpi);
            if (tempDevice.getWifiP2pDevice().deviceName != null) {
                item_Client_Device_Name.setText(tempDevice.getWifiP2pDevice().deviceName);
            }
            item_Client_Device_State.setText(getDeviceState(tempDevice.getWifiP2pDevice().status));
            if (tempDevice.getStr_address() != null) {
                item_Client_Device_Address.setText(tempDevice.getStr_address());
            }
            if (tempDevice.getPx_width() != -1){
                item_Client_Device_Width_Px.setText(Integer.toString(tempDevice.getPx_width()));
            }
            if (tempDevice.getPx_height() != -1){
                item_Client_Device_Height_Px.setText(Integer.toString(tempDevice.getPx_height()));
            }
            if (tempDevice.getDpi() != -1){
                item_Client_Device_Dpi.setText(Float.toString(tempDevice.getDpi()));
            }

        }
        return v;
    }

    @Override
    public void notifyDataSetChanged(){
        //Log.v(TAG, "notifyDataSetChanged() in act");
        super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated(){
        myDeviceInfoList.clear();
        super.notifyDataSetInvalidated();
    }

    @Override
    public void add(DeviceInfo device){
        myDeviceInfoList.add(device);
    }

    @Override
    public void addAll(Collection c){
        myDeviceInfoList.clear();
        myDeviceInfoList.addAll(c);
    }

    @Override
    public void clear(){
        myDeviceInfoList.clear();
    }

    @Override
    public int getCount() {
        if (myDeviceInfoList == null) {
            return -1;
        }
        return myDeviceInfoList.size();
    }

    @Override
    public DeviceInfo getItem(int position) {
        return myDeviceInfoList.get(position);
    }

    public static String getDeviceState(int deviceState) {
        switch (deviceState) {
            case WifiP2pDevice.AVAILABLE:
                return "Avaialbe";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Error";
        }
    }

}
