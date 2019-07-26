package com.n3v.junwidi;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
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

import com.n3v.junwidi.Services.MyClientTask;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

public class ClientActivity extends BaseActivity implements MyDirectActionListener {

    private static final String TAG = "ClientActivity";

    private WifiP2pManager myManager;
    private WifiP2pManager.Channel myChannel;
    private boolean isWifiP2pEnabled = false;

    private MyBroadCastReceiver myBroadCastReceiver;

    private TextView txt_myDevice_Name;
    private TextView txt_myDevice_Address;
    private TextView txt_myDevice_State;
    private Button btn_Refresh_Peer_List;
    private Button btn_Request_Disconnect;
    private Button btn_Request_Multicast;

    private ListView listView_Server_List;

    private ArrayList<WifiP2pDevice> myWifiP2pDeviceList;
    private MyClientAdapter myClientAdapter;

    private WifiP2pInfo myWifiP2pInfo;
    private WifiP2pDevice myWifiP2pDevice = null;
    private DeviceInfo myDeviceInfo = null;

    InetAddress host_Address = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        myManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        myChannel = myManager.initialize(this, getMainLooper(), null);
        myBroadCastReceiver = new MyBroadCastReceiver(myManager, myChannel, this);
        registerReceiver(myBroadCastReceiver, MyBroadCastReceiver.getIntentFilter());
        initView();
        permissionCheck();
        myManager.discoverPeers(myChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "Discover Peer success");
            }

            @Override
            public void onFailure(int i) {
                Log.e(TAG, "Discover Peer failed :: " + i);
            }
        });
    }

    private void initView() { //Activity의 view item들 초기화
        txt_myDevice_Name = findViewById(R.id.client_txt_my_device_name);
        txt_myDevice_Address = findViewById(R.id.client_txt_my_device_address);
        txt_myDevice_State = findViewById(R.id.client_txt_my_device_state);

        btn_Refresh_Peer_List = findViewById(R.id.client_btn_refresh_peer_list);
        btn_Request_Disconnect = findViewById(R.id.client_btn_request_disconnect);
        btn_Request_Disconnect.setEnabled(false);
        btn_Request_Multicast = findViewById(R.id.client_btn_request_multicast);

        listView_Server_List = findViewById(R.id.client_list_server);
        myWifiP2pDeviceList = new ArrayList<>();
        myClientAdapter = new MyClientAdapter(this, R.layout.item_server, myWifiP2pDeviceList);
        listView_Server_List.setAdapter(myClientAdapter);
        btn_Request_Multicast.setOnClickListener(myClickListener);
        btn_Refresh_Peer_List.setOnClickListener(myClickListener);
        btn_Request_Disconnect.setOnClickListener(myClickListener);
        listView_Server_List.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) { //ListView 아이템 클릭 리스너
                WifiP2pDevice d = myWifiP2pDeviceList.get(position);
                connect(d);
            }
        });
    }

    private View.OnClickListener myClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) { //일반 버튼 클릭 리스너
            if (v.equals(btn_Refresh_Peer_List)) {
                myManager.discoverPeers(myChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.v(TAG, "Discover Peer Success");
                        showToast("Discover Peer Success");
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(TAG, "Discover Peer Failed :: " + i);
                        showToast("Discover Peer Failed");
                    }
                });
            } else if (v.equals(btn_Request_Disconnect)) {
                disconnect();
            } else if (v.equals(btn_Request_Multicast)) {
                callClientTask(MyClientTask.CLIENT_TEST_SERVICE);
            }
        }
    };

    public void callClientTask(String mode) {
        new MyClientTask(this, mode, myWifiP2pInfo.groupOwnerAddress.getHostAddress(), myDeviceInfo).execute();
    }

    public void connect(WifiP2pDevice d) { //Wifi P2P 연결
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = d.deviceAddress;
        config.groupOwnerIntent = 0;
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
            }

            @Override
            public void onFailure(int i) {
                Log.e(TAG, "Connect Failed");
                showToast("Connect Failed");
            }
        });
    }

    public void disconnect() {
        myManager.removeGroup(myChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "Disconnect Success");
                showToast("Disconnect Success");
            }

            @Override
            public void onFailure(int i) {
                Log.e(TAG, "Disconnect Failed :: " + i);
                showToast("Disconnect Failed");
            }
        });
    }

    @Override
    public void setIsWifiP2pEnabled(boolean enabled) {
        this.isWifiP2pEnabled = enabled;
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) { //Wifi P2P 연결(그룹 생성) 시 호출됨
        myWifiP2pDeviceList.clear();
        myClientAdapter.notifyDataSetChanged();
        Log.e(TAG, "onConnectionInfoAvailable");
        Log.e(TAG, "onConnectionInfoAvailable groupFormed: " + wifiP2pInfo.groupFormed);
        Log.e(TAG, "onConnectionInfoAvailable isGroupOwner: " + wifiP2pInfo.isGroupOwner);
        Log.e(TAG, "onConnectionInfoAvailable getHostAddress: " + wifiP2pInfo.groupOwnerAddress.getHostAddress());
        myWifiP2pInfo = wifiP2pInfo;
        btn_Request_Disconnect.setEnabled(true);
        host_Address = wifiP2pInfo.groupOwnerAddress;

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int dpi = dm.densityDpi;
        float density = dm.density;
        String addr = getDottedDecimalIP(getLocalIPAddress());
        myDeviceInfo = new DeviceInfo(myWifiP2pDevice, addr, width, height, dpi, density);

        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {

        } else if (wifiP2pInfo.groupFormed) {
            callClientTask(MyClientTask.CLIENT_HANDSHAKE_SERVICE);
        }
    }

    @Override
    public void onDisconnection() { //Wifi P2P가 연결되지 않았을 때 호출됨
        Log.e(TAG, "onDisconnection");
        myWifiP2pDeviceList.clear();
        myClientAdapter.notifyDataSetChanged();
        btn_Request_Disconnect.setEnabled(false);
    }

    @Override
    public void onSelfDeviceAvailable(WifiP2pDevice wifiP2pDevice) { //Wifi가 켜져있을 시 호출됨
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
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) { //BroadCastReceiver에서 PEER_CHANGED 시 호출됨
        Log.e(TAG, "onPeersAvailable : wifiP2pDeviceList.size : " + wifiP2pDeviceList.getDeviceList().size());
        myWifiP2pDeviceList.clear();
        for (WifiP2pDevice d : wifiP2pDeviceList.getDeviceList()) {
            myWifiP2pDeviceList.add(d);
        }
        myClientAdapter.addAll(wifiP2pDeviceList.getDeviceList());
        Log.e(TAG, "myWifiP2pDeviceList.size : " + myWifiP2pDeviceList.size());
        myClientAdapter.notifyDataSetChanged();
        if (wifiP2pDeviceList.getDeviceList().size() == 0) {
            showToast("No peer");
        }
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

    private byte[] getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                            return inetAddress.getAddress();
                        }
                        //return inetAddress.getHostAddress().toString(); // Galaxy Nexus returns IPv6
                    }
                }
            }
        } catch (SocketException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        } catch (NullPointerException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        }
        return null;
    }

    private String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }

}
