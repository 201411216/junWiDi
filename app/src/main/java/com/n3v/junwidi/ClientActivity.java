package com.n3v.junwidi;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;

import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.n3v.junwidi.Adapter.MyClientAdapter;
import com.n3v.junwidi.BroadcastReceiver.MyBroadCastReceiver;
import com.n3v.junwidi.Datas.DeviceInfo;
import com.n3v.junwidi.Dialogs.GuideLineDialog;
import com.n3v.junwidi.Dialogs.ReceiveDialog;
import com.n3v.junwidi.Listener.MyClientTaskListener;
import com.n3v.junwidi.Listener.MyDialogListener;
import com.n3v.junwidi.Listener.MyDirectActionListener;
import com.n3v.junwidi.Services.MyClientTask;
import com.n3v.junwidi.Utils.RealPathUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Enumeration;

public class ClientActivity extends BaseActivity implements MyDirectActionListener, MyDialogListener, MyClientTaskListener {

    private static final String TAG = "ClientActivity";

    private WifiP2pManager myManager;
    private WifiP2pManager.Channel myChannel;
    private boolean isWifiP2pEnabled = false;

    private MyBroadCastReceiver myBroadCastReceiver;

    private TextView txt_myDevice_Name;
    private TextView txt_myDevice_Address;

    private TextView text_server_activity_Connected_Server;
    private TextView text_server_activity_able_list;

    private SwipeRefreshLayout layout_Client_Pull_To_Refresh;
    private ListView listView_Server_List;

    private ArrayList<WifiP2pDevice> myWifiP2pDeviceList;
    private MyClientAdapter myClientAdapter;

    private WifiP2pInfo myWifiP2pInfo;
    private WifiP2pDevice myWifiP2pDevice = null;
    private DeviceInfo myDeviceInfo = null;

    private ReceiveDialog receiveDialog = null;
    private GuideLineDialog guideLineDialog = null;

    InetAddress host_Address = null;

    private String fileName = "";
    private long fileSize = 0;

    AsyncTask nowTask = null;

    boolean handshaked = false;
    boolean downloading = false;
    boolean first_wifi_check = false;

    private static final int PLAYER_RESULT_CODE = 19283;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        myManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        myChannel = myManager.initialize(this, getMainLooper(), null);
        myBroadCastReceiver = new MyBroadCastReceiver(myManager, myChannel, this);
        //registerReceiver(myBroadCastReceiver, MyBroadCastReceiver.getIntentFilter());
        receiveDialog = new ReceiveDialog(this, fileName, this);
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        guideLineDialog = new GuideLineDialog(this, dm, GuideLineDialog.GLD_CLIENT, null);
        initView();

        permissionCheck(0);

        if (!first_wifi_check) {
            int checkWifiState = checkWifiOnAndConnected();
            if (checkWifiState == 2) {
                WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                if (wifiManager != null) {
                    wifiManager.setWifiEnabled(true);
                    showToast("Wifi 기능을 사용합니다");
                }
            } else if (checkWifiState == 3) {
                WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                if (wifiManager != null) {
                    wifiManager.disconnect();
                    //showToast("Wifi 연결을 해제하였습니다");
                }
            }
            first_wifi_check = true;
        }

        if (!isLocationServiceEnabled()) {
            showToast("위치정보 기능이 필요합니다");
            Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(locationIntent);
        }


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        getSupportActionBar().setTitle("파일 받기");
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "inActivityResult");
        switch (requestCode) {
            case PLAYER_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    nowTask = callClientTask(MyClientTask.CLIENT_TCP_WAITING_SERVICE);
                }
        }
    }

    private void initView() { //Activity의 view item들 초기화
        txt_myDevice_Name = findViewById(R.id.client_txt_my_device_name);
        txt_myDevice_Address = findViewById(R.id.client_txt_my_device_address);
        //txt_myDevice_State = findViewById(R.id.client_txt_my_device_state);
        //txt_Host_Ip_Address = findViewById(R.id.client_txt_host_ip_address);
        text_server_activity_able_list = findViewById(R.id.text_server_activity_able_list);
        text_server_activity_Connected_Server = findViewById(R.id.text_server_activity_Connected_Server);
        layout_Client_Pull_To_Refresh = findViewById(R.id.client_layout_pull_to_refresh);

        text_server_activity_Connected_Server.setVisibility(View.GONE);

        layout_Client_Pull_To_Refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                permissionCheck(0);
                String deviceState = "";

                myManager.discoverPeers(myChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.v(TAG, "Discover Peer Success");
                        //showToast("Discover Peer Success");
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(TAG, "Discover Peer Failed :: " + i);
                        //showToast("Discover Peer Failed");
                    }
                });
                if (myWifiP2pDevice.status == 0) { // Connect 상태일 때
                    text_server_activity_able_list.setVisibility(View.GONE);
                    text_server_activity_Connected_Server.setVisibility(View.VISIBLE);
                }
                layout_Client_Pull_To_Refresh.setRefreshing(false);
            }
        });

        //text_server_activity_Connected_Server.setVisibility(View.VISIBLE);
        listView_Server_List = findViewById(R.id.client_list_server);
        myWifiP2pDeviceList = new ArrayList<>();
        myClientAdapter = new MyClientAdapter(this, R.layout.item_server, myWifiP2pDeviceList);
        listView_Server_List.setAdapter(myClientAdapter);
        listView_Server_List.setOnItemClickListener(onItemClickListener);

    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Log.v(TAG, "onClickItem pos = " + position);
            WifiP2pDevice d = myWifiP2pDeviceList.get(position);
            connect(d);
        }
    };

    private View.OnClickListener myClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) { //일반 버튼 클릭 리스너

        }
    };

    public AsyncTask callClientTask(String mode) {
        if (myWifiP2pInfo != null) {
            return new MyClientTask(this, mode, myWifiP2pInfo.groupOwnerAddress.getHostAddress(), myDeviceInfo, this, this.fileName, this.fileSize).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        return null;
    }

    @Override
    public void onResume() {
        myBroadCastReceiver = new MyBroadCastReceiver(myManager, myChannel, this);
        registerReceiver(myBroadCastReceiver, MyBroadCastReceiver.getIntentFilter());
        super.onResume();
    }

    @Override
    public void onPause() {
        unregisterReceiver(myBroadCastReceiver);
        super.onPause();
    }

    public void finish() {
        super.finish();

        // activity가 사라질 때 transition 효과 지정
        overridePendingTransition(android.R.anim.slide_in_left, R.anim.anim_slide_out_right);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        // && !nowTask.isCancelled()
        disconnect();
        if (nowTask != null && !nowTask.isCancelled()) {
            Log.v(TAG, "onDestroy : call cancel");
            callClientTask(MyClientTask.CLIENT_TCP_CANCEL_WAITING_SERVICE);
            Log.v(TAG, "onDestroy : call cancel complete");
            nowTask.cancel(false);
            nowTask = null;
        }
        super.onDestroy();
    }

    /*
    Wi-Fi P2P 연결을 시도함
    config.groupOwnerIntent = 0 을 선언하면 GroupOwner 가 되지 않음
     */
    public void connect(WifiP2pDevice d) { //Wifi P2P 연결
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = d.deviceAddress;
        config.groupOwnerIntent = 0;
        config.wps.setup = WpsInfo.PBC;
        if (d.status == WifiP2pDevice.CONNECTED) {
            Log.v(TAG, "The Device is already connected");
            text_server_activity_able_list.setVisibility(View.VISIBLE);
            return;
        }
        myManager.connect(myChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "Connect Success");
                //showToast("Connect Success!");

            }

            @Override
            public void onFailure(int i) {
                Log.e(TAG, "Connect Failed");
                //showToast("Connect Failed");

            }
        });
    }

    /*
    Group 에서 연결 해제하는 기능
     */
    public void disconnect() {
        myManager.removeGroup(myChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "Disconnect Success");
                //showToast("Disconnect Success");
                if (nowTask != null && !nowTask.isCancelled()) {
                    callClientTask(MyClientTask.CLIENT_TCP_CANCEL_WAITING_SERVICE);
                    Log.v(TAG, "Disconnect call nowTask Cancel");
                    nowTask.cancel(true);
                    nowTask = null;
                }
                /*
                handshaked = false;
                myWifiP2pDeviceList.clear();
                myClientAdapter.notifyDataSetChanged();
                host_Address = null;
                txt_Host_Ip_Address.setText("-");
                if (nowTask != null  && !nowTask.isCancelled()) {
                    Log.v(TAG, "Disconnect call nowTask Cancel");
                    nowTask.cancel(true);
                    nowTask = null;
                }
                */
            }

            @Override
            public void onFailure(int i) {
                Log.e(TAG, "Disconnect Failed :: " + i);
                //showToast("Disconnect Failed");
            }
        });
    }

    @Override
    public void setIsWifiP2pEnabled(boolean enabled) {
        this.isWifiP2pEnabled = enabled;
    }

    /*
    BroadCastReceiver 가 WIFI_P2P_CONNECTION_CHANGED_ACTION intent 를 받았을 때 호출.
    새롭게 Server 에 연결되거나, 그룹에서 제외되거나 그룹이 제거되었을 때 호출.
    Handshake Process 의 중추
    p1 : myDeviceInfo 가 초기화 되지 않은 경우 초기화함(wifiP2pInfo 를 통해 GroupOwner 인 자신의 IP 주소를 얻을 수 있음)
    p2 : Group 이 생성되있고, 자신이 GroupOwner 가 아닌 경우(Client 는 항상 GroupOwner 가 아님) Handshake process 로 통신 시도
        해당 AsyncTask 를 통해 자신의 IP address 와 Display 정보를 GroupOwner 에게 전송
     */


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) { //Wifi P2P 연결(그룹 생성) 시 호출됨

        myWifiP2pDeviceList.clear();
        myClientAdapter.notifyDataSetChanged();
        Log.e(TAG, "onConnectionInfoAvailable");
        Log.e(TAG, "onConnectionInfoAvailable groupFormed: " + wifiP2pInfo.groupFormed);
        Log.e(TAG, "onConnectionInfoAvailable isGroupOwner: " + wifiP2pInfo.isGroupOwner);
        Log.e(TAG, "onConnectionInfoAvailable getHostAddress: " + wifiP2pInfo.groupOwnerAddress.getHostAddress());
        myWifiP2pInfo = wifiP2pInfo;
        if (wifiP2pInfo.groupFormed) {
            host_Address = wifiP2pInfo.groupOwnerAddress;
            String temp_Addr = String.valueOf(host_Address).replace("/", "");
            //
            // txt_Host_Ip_Address.setText(temp_Addr);
        }

        if (myDeviceInfo == null) { // p1
            setMyDeviceInfo(wifiP2pInfo);
        }

        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            Log.e(TAG, "Client Can't be GroupOwner");
        } else if (wifiP2pInfo.groupFormed) { // p2
//            callClientTask(MyClientTask.CLIENT_TCP_CANCEL_WAITING_SERVICE);
//            if (nowTask != null && !nowTask.isCancelled()) {
//                nowTask.cancel(true);
//                nowTask = null;
//            }
            if (handshaked == false) {
                Log.v(TAG, "Call HANDSHAKE TASK");
                nowTask = callClientTask(MyClientTask.CLIENT_HANDSHAKE_SERVICE);
            }
        }
    }


    /*
    Wi-Fi P2P Connection 이 해제될 때 호출됨
     */
    @Override
    public void onDisconnection() {

        Log.e(TAG, "onDisconnection");
        handshaked = false;
        myWifiP2pDeviceList.clear();
        myClientAdapter.notifyDataSetChanged();
        host_Address = null;
        //txt_Host_Ip_Address.setText("-");
        if (nowTask != null && !nowTask.isCancelled()) {
            callClientTask(MyClientTask.CLIENT_TCP_CANCEL_WAITING_SERVICE);
        }

    }

    /*
    BroadCastReceiver 가 WIFI_P2P_THIS_DEVICE_CHANGED_ACTION intent 를 받았을 때 호출됨
    별 의미는 없고 자신의 기기 정보를 받아 View 에 올려주는 기능을 하고 있음
     */
    @Override
    public void onSelfDeviceAvailable(WifiP2pDevice wifiP2pDevice) {
        Log.e(TAG, "onSelfDeviceAvailable");
        Log.e(TAG, "DeviceName: " + wifiP2pDevice.deviceName);
        Log.e(TAG, "DeviceAddress: " + wifiP2pDevice.deviceAddress);
        Log.e(TAG, "Status: " + wifiP2pDevice.status);
        txt_myDevice_Name.setText(wifiP2pDevice.deviceName);
        txt_myDevice_Address.setText(wifiP2pDevice.deviceAddress);

        myWifiP2pDevice = wifiP2pDevice;
    }

    /*
    BroadCastReceiver 가 WIFI_P2P_PEERS_CHANGED_ACTION intent 를 받았을 때 호출됨
    Client Activity 에서 Server 가 될 그룹을 찾고, 이를 ListView 에 띄우는 중추 역할
    BroadCastReceiver 에서 가용한 peer 들의 WifiP2pDeviceList 를 매개변수로 이 함수를 호출
    이 함수는 WifiP2pDeviceList 를 ArrayAdapter 를 통해 ListView 에 올려주는 기능 수행.
     */
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

    /*
    WifiP2pDevice.status 의 return value 가 int 이므로 String 으로 변환
     */
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

    public boolean isLocationServiceEnabled() {
        LocationManager locationManager = null;
        boolean gps_enabled = false, network_enabled = false;

        if (locationManager == null)
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            //do nothing...
        }

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            //do nothing...
        }

        return gps_enabled || network_enabled;

    }

    private int checkWifiOnAndConnected() {
        WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if (wifiInfo.getNetworkId() == -1) {
                return 3; // Not connected to an access point
            }
            return 1; // Connected to an access point
        } else {
            return 2; // Wi-Fi adapter is OFF
        }
    }

    /*
    Wi-Fi P2P Peerlist를 받아오기 위해 android 일정 버전 이상에서는 ACCESS_FINE_LOCATION 권한을 요구함.
    해당 권한은 Dangerous Permission에 해당되므로 runtime 중에 권한을 요청하여 허가받아야함.
    해당 권한이 필요한 상황마다 확인하는 것을 권장.(현재는 Server, Client Activity의 onCreate 에서 한번씩만 호출하고 있음)
     */
    public void permissionCheck(int permission) {
        int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0; // permission 1 : 정확한 위치 권한
        int MY_PERMISSIONS_REQUEST_CHANGE_WIFI_MULTICAST_STATE = 0; // permission 2 : 멀티캐스트 상태 권한
        int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0; // permission 3 : 외부 저장소 읽기 권한
        int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0; // permission 4 : 외부 저장소 쓰기 권한
        int MY_PERMISSIONS_REQUEST_MULTI = 0;

        ArrayList<String> permissions = new ArrayList<String>();
        if (permission == 0 || permission == 1) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_WIFI_STATE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CHANGE_WIFI_STATE);
            }
        }
        if (permission == 0 || permission == 2) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_MULTICAST_STATE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CHANGE_WIFI_STATE);
            }
        }
        if (permission == 0 || permission == 3) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        if (permission == 0 || permission == 4) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        if (!permissions.isEmpty()) {
            String[] requestPermissionsArray = new String[permissions.size()];
            requestPermissionsArray = permissions.toArray(requestPermissionsArray);
            ActivityCompat.requestPermissions(this, requestPermissionsArray, MY_PERMISSIONS_REQUEST_MULTI);
        }
    }

    /*
    Client 의 IP Address 를 얻어오는 기능.
     */
    private String getLocalIPAddress() {
        try {
            if (true) {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                                //showToast("ipv4Address");
                                if (getDottedDecimalIP(inetAddress.getAddress()).startsWith("192.168.49.")) {
                                    return getDottedDecimalIP(inetAddress.getAddress());
                                }
                            }
                        }
                    }
                }
            }
            try {
                WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
                int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

                // Convert little-endian to big-endianif needed
                if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
                    ipAddress = Integer.reverseBytes(ipAddress);
                }

                byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

                String ipAddressString;
                try {
                    ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
                } catch (UnknownHostException ex) {
                    Log.e("WIFIIP", "Unable to get host address.");
                    ipAddressString = null;
                }

                return ipAddressString;
            } catch (Exception ex) {
            } // for now eat exceptions

        } catch (NullPointerException ex) {
            showToast("getLocalIPAddress() error: NullPointerException"); //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
            ex.printStackTrace();
        } catch (IOException ex) {
            showToast("getLocalIPAddress() error: IOException");
        }
        return null;
    }

    /*
    byte[] type 으로 구해지는 IP 주소를 String 으로 변환해주는 기능.
     */
    private String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        for (int i = 0; i < ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i] & 0xFF;
        }
        return ipAddrStr;
    }

    public void setMyDeviceInfo(WifiP2pInfo wifiP2pInfo) {
        String brandName = Build.BRAND;
        String modelName = Build.MODEL;
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int densityDpi = dm.densityDpi;
        boolean isGroupOwner = false;

        myDeviceInfo = new DeviceInfo(myWifiP2pDevice, brandName, modelName, getLocalIPAddress(), width, height, densityDpi, isGroupOwner, dm);
        myDeviceInfo.convertPx();
        Log.v(TAG, "Local IP : " + getLocalIPAddress());
    }

    @Override
    public void onProgressFinished() {
        receiveDialog.cancel();
    }

    @Override
    public void onRcvClickOK(int state) {
        if (state == ReceiveDialog.RCV_DLG_INIT) {
            if (nowTask != null && !nowTask.isCancelled()) {
                nowTask.cancel(true);
                nowTask = null;
            }
            downloading = true;
            nowTask = callClientTask(MyClientTask.CLIENT_TCP_FILE_RECEIVE_SERVICE);
        } else if (state == ReceiveDialog.RCV_DLG_VIDEO_ALREADY_EXISTS) {
            receiveDialog.cancel();
        }
    }

    @Override
    public void onRcvClickCancel(int state) {
        if (state == ReceiveDialog.RCV_DLG_VIDEO_ALREADY_EXISTS) {
            receiveDialog.initDialog();
            receiveDialog.cancel();
        } else {
            if (nowTask != null && !nowTask.isCancelled()) {
                nowTask.cancel(true);
                nowTask = null;
            }
            receiveDialog.cancel();
        }
    }

    @Override
    public void onSendClickOK(int state) {
    }

    @Override
    public void onSendClickCancel(int state) {

    }

    @Override
    public void onAllProgressFinished() {

    }

    @Override
    public void onEndWait() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                receiveDialog.show();
                receiveDialog.initDialog();
                receiveDialog.setFileName(fileName);
            }
        });
    }

    @Override
    public void progressUpdate(final int progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                receiveDialog.setProgress(progress);
                receiveDialog.show();
            }
        });
    }

    @Override
    public void onHandshaked() {
        handshaked = true;
        if (nowTask != null && !nowTask.isCancelled()) {
            //nowTask.cancel(true);
            //nowTask = null;
        }
        nowTask = callClientTask(MyClientTask.CLIENT_TCP_WAITING_SERVICE);
        Log.v(TAG, "now Task : WAITING SERVICE");
    }

    @Override
    public void setFile(String fileName, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    @Override
    public void onReceiveFinished() {
        receiveDialog.cancel();
        nowTask = callClientTask(MyClientTask.CLIENT_TCP_WAITING_SERVICE);
    }

    @Override
    public void onReceiveCancelled() {
        receiveDialog.cancel();
        showToast("영상 수신이 취소되었습니다");
    }

    @Override
    public void onVideoAlreadyExist() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                receiveDialog.show();
                receiveDialog.initDialog();
                receiveDialog.setFileName(fileName);
                receiveDialog.setAlreadyExists();
                nowTask = callClientTask(MyClientTask.CLIENT_TCP_WAITING_SERVICE);
            }
        });
    }

    @Override
    public void onReceiveShowGuideline() {
        guideLineDialog.show();
        guideLineDialog.setProcessedMyDeviceInfo(myDeviceInfo);
        nowTask = callClientTask(MyClientTask.CLIENT_TCP_WAITING_SERVICE);
    }

    @Override
    public void onEndExcute() {
        if (nowTask != null && !nowTask.isCancelled()) {
            //nowTask = null;
        }
    }

    @Override
    public void onPreparePlayReceived() {
        guideLineDialog.cancel();
        nowTask = null;
        callClientTask(MyClientTask.CLIENT_TCP_CANCEL_WAITING_SERVICE);
        Intent intent = new Intent(this, PlayerClient.class);
        Log.v(TAG, "Before parcel : " + myDeviceInfo.getLongString());
        intent.putExtra("myDeviceInfo", myDeviceInfo);
        intent.putExtra("host_addr", myWifiP2pInfo.groupOwnerAddress.getHostAddress());
        startActivityForResult(intent, PLAYER_RESULT_CODE);
    }

    @Override
    public void onReceiveConPlay() {

    }

    @Override
    public void onReceiveConPause() {

    }

    @Override
    public void onReceiveConResume() {

    }

    @Override
    public void onReceiveConStop() {

    }

    @Override
    public void onReceiveConSeek(int time) {

    }
}
