package com.n3v.junwidi;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ServerActivity extends BaseActivity implements MyDirectActionListener {

    public static String TAG = "ServerActivity";

    private WifiP2pManager myManager;
    private WifiP2pManager.Channel myChannel;
    private boolean isWifiP2pEnabled = false;

    private MyBroadCastReceiver myBroadCastReceiver;

    private TextView txt_myDevice_Name;
    private TextView txt_myDevice_Address;
    private TextView txt_myDevice_State;
    private Button btn_File_Select;
    private Button btn_Server_Destroy;
    private ListView listView_Client_List;

    private ArrayList<WifiP2pDevice> wifiP2pDeviceList;
    private MyServerAdapter myServerAdapter;

    private WifiP2pInfo myInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        initView();
        myManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        myChannel = myManager.initialize(this, getMainLooper(), null);
        myBroadCastReceiver = new MyBroadCastReceiver(myManager, myChannel, this);
        registerReceiver(myBroadCastReceiver, MyBroadCastReceiver.getIntentFilter());
    }

    private void initView() {
        txt_myDevice_Name = findViewById(R.id.server_txt_my_device_name);
        txt_myDevice_Address = findViewById(R.id.server_txt_my_device_address);
        txt_myDevice_State = findViewById(R.id.server_txt_my_device_state);
        btn_File_Select = findViewById(R.id.server_btn_file_select);
        btn_Server_Destroy = findViewById(R.id.server_btn_server_destroy);

        listView_Client_List = findViewById(R.id.server_list_client);
        wifiP2pDeviceList = new ArrayList<WifiP2pDevice>();
        myServerAdapter = new MyServerAdapter(this, R.layout.item_client, wifiP2pDeviceList);
        listView_Client_List.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            }
        });
        listView_Client_List.setAdapter(myServerAdapter);
    }


    @Override
    public void setIsWifiP2pEnabled(boolean enabled) {
        this.isWifiP2pEnabled = enabled;
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        wifiP2pDeviceList.clear();
        myServerAdapter.notifyDataSetChanged();
        Log.e(TAG, "onConnectionInfoAvailable");
        Log.e(TAG, "onConnectionInfoAvailable groupFormed: " + wifiP2pInfo.groupFormed);
        Log.e(TAG, "onConnectionInfoAvailable isGroupOwner: " + wifiP2pInfo.isGroupOwner);
        Log.e(TAG, "onConnectionInfoAvailable getHostAddress: " + wifiP2pInfo.groupOwnerAddress.getHostAddress());
        if(wifiP2pInfo.groupFormed && !wifiP2pInfo.isGroupOwner){
            myInfo = wifiP2pInfo;
        }
    }

    @Override
    public void onDisconnection() {
        Log.e(TAG, "onDisconnection");
        wifiP2pDeviceList.clear();
        myServerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSelfDeviceAvailable(WifiP2pDevice wifiP2pDevice) {
        Log.e(TAG, "onSelfDeviceAvailable");
        Log.e(TAG, "DeviceName: " + wifiP2pDevice.deviceName);
        Log.e(TAG, "DeviceAddress: " + wifiP2pDevice.deviceAddress);
        Log.e(TAG, "Status: " + wifiP2pDevice.status);
        txt_myDevice_Name.setText(wifiP2pDevice.deviceName);
        txt_myDevice_Address.setText(wifiP2pDevice.deviceAddress);
        txt_myDevice_State.setText(getDeviceState(wifiP2pDevice.status));
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        Log.e(TAG, "onPeersAvailable :");
        this.wifiP2pDeviceList.clear();
        this.wifiP2pDeviceList.addAll(wifiP2pDeviceList.getDeviceList());
        myServerAdapter.notifyDataSetChanged();
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
