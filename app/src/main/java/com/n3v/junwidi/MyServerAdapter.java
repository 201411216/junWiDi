package com.n3v.junwidi;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MyServerAdapter extends ArrayAdapter<WifiP2pDevice> {

    private ArrayList<WifiP2pDevice> myDeviceArrayList;
    private Context myContext;
    private int myResource;

    public MyServerAdapter(Context context, int resource, ArrayList<WifiP2pDevice> deviceArrayList) {
        super(context, resource, deviceArrayList);
        myContext = context;
        myResource = resource;
        myDeviceArrayList = deviceArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater li = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = li.inflate(this.myResource, null);
        }
        WifiP2pDevice tempDevice = myDeviceArrayList.get(position);
        if (tempDevice != null) {
            TextView item_Client_Device_Name = (TextView) v.findViewById(R.id.item_client_device_name);
            TextView item_Client_Device_Model = (TextView) v.findViewById(R.id.item_client_device_model);
            TextView item_Client_Device_State = (TextView) v.findViewById(R.id.item_client_device_state);
            TextView item_Client_Device_Address = (TextView) v.findViewById(R.id.item_client_device_address);
            TextView item_Client_Device_Width_Px = (TextView) v.findViewById(R.id.item_client_device_width_px);
            TextView item_Client_Device_Height_Px = (TextView) v.findViewById(R.id.item_client_device_height_px);
            TextView item_Client_Device_Dpi = (TextView) v.findViewById(R.id.item_client_device_dpi);
            if (tempDevice.deviceName != null) {
                item_Client_Device_Name.setText(tempDevice.deviceName);
            }
            item_Client_Device_State.setText(getDeviceState(tempDevice.status));
            if (tempDevice.deviceAddress != null) {
                item_Client_Device_Address.setText(tempDevice.deviceAddress);
            }
        }
        return v;
    }

    @Override
    public void notifyDataSetInvalidated(){
        myDeviceArrayList.clear();
        super.notifyDataSetInvalidated();
    }

    @Override
    public void add(WifiP2pDevice device){
        myDeviceArrayList.add(device);
    }

    @Override
    public void addAll(Collection c){
        myDeviceArrayList.clear();
        myDeviceArrayList.addAll(c);
    }

    @Override
    public void clear(){
        myDeviceArrayList.clear();
    }

    @Override
    public int getCount() {
        if (myDeviceArrayList == null) {
            return -1;
        }
        return myDeviceArrayList.size();
    }

    @Override
    public WifiP2pDevice getItem(int position) {
        return myDeviceArrayList.get(position);
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
