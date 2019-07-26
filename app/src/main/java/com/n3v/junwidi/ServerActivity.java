package com.n3v.junwidi;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.n3v.junwidi.Services.MyServerTask;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ServerActivity extends BaseActivity implements MyDirectActionListener {

    private static final String TAG = "ServerActivity";

    private WifiP2pManager myManager;
    private WifiP2pManager.Channel myChannel;
    private boolean isWifiP2pEnabled = false;
    private boolean isGroupExist = false;

    private MyBroadCastReceiver myBroadCastReceiver;

    private TextView txt_myDevice_Name;
    private TextView txt_myDevice_Address;
    private TextView txt_myDevice_State;
    private Button btn_File_Select;
    private Button btn_Refresh_List;
    private Button btn_Server_Control;
    private ListView listView_Client_List;

    private ArrayList<WifiP2pDevice> myWifiP2pDeviceList = new ArrayList<>();
    private MyServerAdapter myServerAdapter;

    private WifiP2pInfo myWifiP2pInfo = null;
    private WifiP2pDevice myWifiP2pDevice = null;
    private DeviceInfo myDeviceInfo = null;
    private ArrayList<DeviceInfo> myDeviceInfoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        initView();
        myManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        myChannel = myManager.initialize(this, getMainLooper(), null);
        myBroadCastReceiver = new MyBroadCastReceiver(myManager, myChannel, this);
        registerReceiver(myBroadCastReceiver, MyBroadCastReceiver.getIntentFilter());
        permissionCheck();
    }

    private void initView() {
        txt_myDevice_Name = findViewById(R.id.server_txt_my_device_name);
        txt_myDevice_Address = findViewById(R.id.server_txt_my_device_address);
        txt_myDevice_State = findViewById(R.id.server_txt_my_device_state);
        btn_File_Select = findViewById(R.id.server_btn_file_select);
        btn_Refresh_List = findViewById(R.id.server_btn_refresh_list);
        btn_Server_Control = findViewById(R.id.server_btn_server_control);

        //test for Broadcast by Multicast

        btn_File_Select.setText("메시지 전송");

        if (!isGroupExist) {
            btn_Server_Control.setText("그룹 생성");
        }

        btn_File_Select.setOnClickListener(myClickListener);
        btn_Server_Control.setOnClickListener(myClickListener);
        btn_Refresh_List.setOnClickListener(myClickListener);

        listView_Client_List = findViewById(R.id.server_list_client);
        myServerAdapter = new MyServerAdapter(this, R.layout.item_client, myDeviceInfoList);
        listView_Client_List.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            }
        });
        listView_Client_List.setAdapter(myServerAdapter);
    }

    private View.OnClickListener myClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.equals(btn_Server_Control)) {
                if (!isGroupExist) {
                    createGroup();
                } else {
                    removeGroup();
                }
            } else if (v.equals(btn_Refresh_List)) {
                if (isWifiP2pEnabled) {
                    Log.v(TAG, "btn_Refresh_List act");
                    myManager.requestGroupInfo(myChannel, new WifiP2pManager.GroupInfoListener() {
                        @Override
                        public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                            deviceListUpdate(wifiP2pGroup);
                        }
                    });
                    myServerAdapter.notifyDataSetChanged();
                }
            } else if (v.equals(btn_File_Select)) {
                Log.v(TAG, "btn_File_Select onClick");
                callServerTask(MyServerTask.SERVER_TEST_SERVICE);
            }
        }
    };

    public void callServerTask(String mode) {
        new MyServerTask(this, mode, myWifiP2pInfo.groupOwnerAddress.getHostAddress(), myDeviceInfo, myDeviceInfoList).execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        myBroadCastReceiver = new MyBroadCastReceiver(myManager, myChannel, this);
        registerReceiver(myBroadCastReceiver, MyBroadCastReceiver.getIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(myBroadCastReceiver);
    }

    @Override
    public void setIsWifiP2pEnabled(boolean enabled) {
        this.isWifiP2pEnabled = enabled;
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        //btn_Server_Control.setEnabled(true);
        btn_File_Select.setEnabled(true);
        Log.e(TAG, "onConnectionInfoAvailable");
        Log.e(TAG, "onConnectionInfoAvailable groupFormed: " + wifiP2pInfo.groupFormed);
        Log.e(TAG, "onConnectionInfoAvailable isGroupOwner: " + wifiP2pInfo.isGroupOwner);
        Log.e(TAG, "onConnectionInfoAvailable getHostAddress: " + wifiP2pInfo.groupOwnerAddress.getHostAddress());
        myWifiP2pInfo = wifiP2pInfo;

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int dpi = dm.densityDpi;
        float density = dm.density;
        myDeviceInfo = new DeviceInfo(myWifiP2pDevice, wifiP2pInfo.groupOwnerAddress.getHostAddress(), width, height, dpi, density);

        myManager.requestGroupInfo(myChannel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                deviceListUpdate(wifiP2pGroup);
            }
        });

        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            callServerTask(MyServerTask.SERVER_HANDSHAKE_SERVICE);
            Log.v(TAG, myDeviceInfo.getString() + "!");
        }

        myServerAdapter.notifyDataSetChanged();

        if (wifiP2pInfo.groupFormed) {
            btn_Server_Control.setText("그룹 해제");
            isGroupExist = true;
        } else {
            btn_Server_Control.setText("그룹 생성");
            isGroupExist = false;
        }
    }

    @Override
    public void onDisconnection() {
        Log.e(TAG, "onDisconnection");
        myWifiP2pDeviceList.clear();
        myServerAdapter.notifyDataSetChanged();
        //btn_Server_Control.setEnabled(false);
        btn_File_Select.setEnabled(false);
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

        myWifiP2pDevice = wifiP2pDevice;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        Log.e(TAG, "onPeersAvailable : wifiP2pDeviceList.size : " + wifiP2pDeviceList.getDeviceList().size());
//        myWifiP2pDeviceList.clear();
//        for (WifiP2pDevice d : wifiP2pDeviceList.getDeviceList()) {
//            myWifiP2pDeviceList.add(d);
//        }
//        myServerAdapter.addAll(wifiP2pDeviceList.getDeviceList());
//        Log.e(TAG, "myWifiP2pDeviceList.size : " + myWifiP2pDeviceList.size());
//        myServerAdapter.notifyDataSetChanged();
//        if (wifiP2pDeviceList.getDeviceList().size() == 0) {
//            showToast("No peer");
//        }
    }

    public void createGroup() {
        myManager.createGroup(myChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "Create Group Success");
                showToast("Create Group Success");
                isGroupExist = true;
            }

            @Override
            public void onFailure(int i) {
                Log.e(TAG, "Create Group Failed");
                showToast("Create Group Failed :: " + i);
//                if(i == 2){
//                    btn_Server_Control.setText("그룹 해제");
//                    isGroupExist = false;
//                }
            }
        });
    }

    public void removeGroup() {
        myManager.removeGroup(myChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "Remove Group Success");
                showToast("Remove Group Success");
                isGroupExist = false;
                btn_Server_Control.setText("그룹 생성");
            }

            @Override
            public void onFailure(int i) {
                Log.e(TAG, "Remove Group Failed");
                showToast("Remove Group Failed :: " + i);
            }
        });
    }

    public static String getDeviceState(int deviceState) {
        switch (deviceState) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
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

    public void permissionCheck() {
        int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
        int permissionChecker = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionChecker == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {

        }
    }

    public void connect(final WifiP2pDevice d) { //Wifi P2P 연결
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = d.deviceAddress;
        config.groupOwnerIntent = 15;
        config.wps.setup = WpsInfo.PBC;
        if (d.status == WifiP2pDevice.CONNECTED) {
            Log.v(TAG, "The Device is already connected");
            return;
        }
        myManager.connect(myChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "Connect Success");
                showToast("Connect Success");
                DeviceInfo di = new DeviceInfo(d);
                myDeviceInfoList.add(di);
            }

            @Override
            public void onFailure(int i) {
                Log.e(TAG, "Connect Failed");
                showToast("Connect Failed");
            }
        });
    }

    public void deviceListUpdate(WifiP2pGroup group) {
        if (myDeviceInfoList.size() < group.getClientList().size()) {
            Log.v(TAG, "deviceListUpdate : Case 1");
            ArrayList<WifiP2pDevice> tempWifiP2pDeviceList = new ArrayList<>(group.getClientList());
            boolean exist = false;
            Log.v(TAG, "tempWifiP2pDeviceList.size() = " + tempWifiP2pDeviceList.size());
            for (int i = 0; i < tempWifiP2pDeviceList.size(); i++) {
                exist = false;
                for (int j = 0; j < myDeviceInfoList.size(); j++) {
                    if (myDeviceInfoList.get(j).getWifiP2pDevice().equals(tempWifiP2pDeviceList.get(i))) {
                        exist = true;
                        break;
                    }
                }
                Log.v(TAG, tempWifiP2pDeviceList.get(i).deviceName);
                if (!exist) {
                    DeviceInfo di = new DeviceInfo(tempWifiP2pDeviceList.get(i));
                    myDeviceInfoList.add(di);
                    Log.v(TAG, "added : " + tempWifiP2pDeviceList.get(i).deviceName);

                }
                return;
            }
        } else if (myDeviceInfoList.size() > group.getClientList().size()) {
            Log.v(TAG, "deviceListUpdate : Case 2");
            ArrayList<WifiP2pDevice> tempWifiP2pDeviceList = new ArrayList<>(group.getClientList());
            boolean exist = false;
            if (group.getClientList().size() == 0){
                myDeviceInfoList.clear();
            }
            for (int i = 0; i < myDeviceInfoList.size(); i++) {
                exist = false;
                for (int j = 0; j < tempWifiP2pDeviceList.size(); j++) {
                    if (myDeviceInfoList.get(i).getWifiP2pDevice().equals(tempWifiP2pDeviceList.get(j))) {
                        exist = true;
                        break;
                    }
                    if (!exist) {
                        Log.v(TAG, myDeviceInfoList.get(i).getWifiP2pDevice().deviceName + " disconnected");
                        myDeviceInfoList.remove(i);
                    }
                }
            }
            return;
        } else if (myDeviceInfoList.size() == group.getClientList().size()) {
            Log.v(TAG, "deviceListUpdate : Case 3 with size : " + myDeviceInfoList.size());
            if (myDeviceInfoList.size() > 0) {
                ArrayList<WifiP2pDevice> tempWifiP2pDeviceList = new ArrayList<>(group.getClientList());
                int test = 0;
                for (int i = 0; i < myDeviceInfoList.size(); i++) {
                    for (int j = 0; j < tempWifiP2pDeviceList.size(); j++) {
                        if (myDeviceInfoList.get(i).getWifiP2pDevice().equals(tempWifiP2pDeviceList.get(j))) {
                            test++;
                            break;
                        }
                    }
                }
                if (myDeviceInfoList.size() != test) {
                    Log.e(TAG, "Device info list doesn't matched");
                }
            }
            return;
        }
    }
}
