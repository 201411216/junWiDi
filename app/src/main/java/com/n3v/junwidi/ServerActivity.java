package com.n3v.junwidi;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.n3v.junwidi.Listener.MyDirectActionListener;
import com.n3v.junwidi.Services.MyServerTask;

import java.util.ArrayList;

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
    private TextView txt_Video_Path;
    private Button btn_File_Select;
    private Button btn_File_Transfer;
    private SwipeRefreshLayout layout_Server_Pull_To_Refresh;
    private ListView listView_Client_List;

    private ArrayList<WifiP2pDevice> myWifiP2pDeviceList = new ArrayList<>();
    private MyServerAdapter myServerAdapter;

    private WifiP2pInfo myWifiP2pInfo = null;
    private WifiP2pDevice myWifiP2pDevice = null;
    private DeviceInfo myDeviceInfo = null;
    private ArrayList<DeviceInfo> myDeviceInfoList = new ArrayList<>();

    private String videoPath = "";
    private boolean isFileSelected = false;
    private static final int PICK_VIDEO_RESULT_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        initView();
        myManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        myChannel = myManager.initialize(this, getMainLooper(), null);
        myBroadCastReceiver = new MyBroadCastReceiver(myManager, myChannel, this);
        permissionCheck(0);

    }

    private void initView() {
        txt_myDevice_Name = findViewById(R.id.server_txt_my_device_name);
        txt_myDevice_Address = findViewById(R.id.server_txt_my_device_address);
        txt_myDevice_State = findViewById(R.id.server_txt_my_device_state);
        txt_Video_Path = findViewById(R.id.server_txt_video_path);
        btn_File_Select = findViewById(R.id.server_btn_file_select);
        btn_File_Transfer = findViewById(R.id.server_btn_file_transfer);

        btn_File_Select.setText("시간 전송");

        if (!isFileSelected) {
            btn_File_Select.setText("비디오 선택");
            btn_File_Transfer.setEnabled(false);
        }

        btn_File_Select.setOnClickListener(myClickListener);
        btn_File_Transfer.setOnClickListener(myClickListener);

        listView_Client_List = findViewById(R.id.server_list_client);
        myServerAdapter = new MyServerAdapter(this, R.layout.item_client, myDeviceInfoList);
        listView_Client_List.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            }
        });
        listView_Client_List.setAdapter(myServerAdapter);

        layout_Server_Pull_To_Refresh = findViewById(R.id.server_layout_pull_to_refresh);
        layout_Server_Pull_To_Refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isWifiP2pEnabled) {
                    Log.v(TAG, "ListView onRefresh");
                    myManager.requestGroupInfo(myChannel, new WifiP2pManager.GroupInfoListener() {
                        @Override
                        public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                            deviceListUpdate(wifiP2pGroup);
                        }
                    });
                    myServerAdapter.notifyDataSetChanged();
                }
                layout_Server_Pull_To_Refresh.setRefreshing(false);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;

    }

    private View.OnClickListener myClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.equals(btn_File_Select)) {
                Log.v(TAG, "btn_File_Select onClick");
                if (!isFileSelected) {
                    permissionCheck(3);
                    Intent fileChooseIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    fileChooseIntent.setType("video/*");
                    startActivityForResult(fileChooseIntent, PICK_VIDEO_RESULT_CODE);
                } else {
                    videoPath = "";
                    txt_Video_Path.setText("-");
                    isFileSelected = false;
                    btn_File_Select.setText("비디오 선택");
                    btn_File_Transfer.setEnabled(false);
                }
                //callServerTask(MyServerTask.SERVER_MESSAGE_SERVICE);
            } else if (v.equals(btn_File_Transfer)) {
                if (isFileSelected) {
                    Log.v(TAG, "btn_File_Transfer onClick");
                    permissionCheck(2);
                    permissionCheck(3);
                    callServerTask(MyServerTask.SERVER_TCP_FILE_TRANSFER_SERVICE);
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_VIDEO_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    //videoPath = data.getData().getPath();
                    Uri videoURI = data.getData();
                    videoPath = RealPathUtil.getRealPath(this, videoURI);
                    Log.v(TAG, videoPath + " selected");
                    txt_Video_Path.setText(videoPath);
                    isFileSelected = true;
                    btn_File_Select.setText("선택 취소");
                    btn_File_Transfer.setEnabled(true);
                    showToast(videoPath + " selected");
                }
        }
    }

    public void callServerTask(String mode) {
        if (myWifiP2pInfo != null && (mode.equals(MyServerTask.SERVER_TCP_FILE_TRANSFER_SERVICE) || mode.equals(MyServerTask.SERVER_FILE_TRANSFER_SERVICE))) {
            Log.v(TAG, "callServerTask : mode.FILE_TRANSFER");
            if (!this.videoPath.equals("")) {
                new MyServerTask(this, mode, myWifiP2pInfo.groupOwnerAddress.getHostAddress(), myDeviceInfo, myDeviceInfoList, myServerAdapter, this.videoPath).execute();
            }
        } else if (myWifiP2pInfo != null) {
            new MyServerTask(this, mode, myWifiP2pInfo.groupOwnerAddress.getHostAddress(), myDeviceInfo, myDeviceInfoList, myServerAdapter).execute();
        }
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
    public void onDestroy(){
        super.onDestroy();
        removeGroup();
    }

    @Override
    public void setIsWifiP2pEnabled(boolean enabled) {
        this.isWifiP2pEnabled = enabled;
    }

    /*
    BroadCastReceiver 가 WIFI_P2P_CONNECTION_CHANGED_ACTION intent 를 받았을 때 호출.
    새롭게 연결된 Client 가 있거나, 기존의 Client 가 제외되거나 그룹이 생성, 제거됐을 때 호출됨.
    Handshake Process 의 중추.
    p1 : myDeviceInfo 가 초기화 되지 않은 경우 초기화함.(wifiP2pInfo 를 통해 GroupOwner 인 자신의 IP 주소를 얻을 수 있음)
    p2 : WifiP2pGroup 을 얻어 Group 의 Client 의 WifiP2pDevice 정보를 얻을 수 있음 -> DeviceInfoList 갱신.
    p3 : Group 이 생성되있고, 자신이 GroupOwner 인 경우(Server 는 항상 GroupOwner) Handshake process 로 통신 시도.
        해당 AsyncTask 에서 수신된 데이터를 통해 새롭게 추가된 Client 의 IP address 와 Display 정보를 DeviceInfoList 에 추가함.
        onPostExecute() 를 통해 adapter 에 직접 notifyDataSetChanged()를 보내 ListView 를 최신화함.
    p4 : 그룹이 생성된 경우와 그렇지 않은 경우 btn 정보 변경.
     */
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        //btn_Server_Control.setEnabled(true);
        btn_File_Select.setEnabled(true);
        Log.e(TAG, "onConnectionInfoAvailable");
        Log.e(TAG, "onConnectionInfoAvailable groupFormed: " + wifiP2pInfo.groupFormed);
        Log.e(TAG, "onConnectionInfoAvailable isGroupOwner: " + wifiP2pInfo.isGroupOwner);
        Log.e(TAG, "onConnectionInfoAvailable getHostAddress: " + wifiP2pInfo.groupOwnerAddress.getHostAddress());
        myWifiP2pInfo = wifiP2pInfo;

        if (myDeviceInfo == null) { // p1
            setMyDeviceInfo(wifiP2pInfo);
        }

        myManager.requestGroupInfo(myChannel, new WifiP2pManager.GroupInfoListener() { // p2
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                Log.v(TAG, "onGroupInfoAvailable()");
                deviceListUpdate(wifiP2pGroup);
            }
        });

        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) { // p3
            callServerTask(MyServerTask.SERVER_HANDSHAKE_SERVICE);
        }
    }

    /*
    Wi-Fi P2P Connection 이 해제될 때 호출됨
     */
    @Override
    public void onDisconnection() {
        Log.e(TAG, "onDisconnection");
        myWifiP2pDeviceList.clear();
        //btn_Server_Control.setEnabled(false);
        isGroupExist = false;
        myDeviceInfoList.clear();
        myServerAdapter.notifyDataSetChanged();
        btn_File_Select.setEnabled(false);
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
        txt_myDevice_State.setText(getDeviceState(wifiP2pDevice.status));
        myWifiP2pDevice = wifiP2pDevice;
        if (!isGroupExist) {
            createGroup();
        }
    }

    /*
    BroadCastReceiver 가 WIFI_P2P_PEERS_CHANGED_ACTION intent 를 받았을 때 호출됨
    Server Activity 에서는 사용되지 않음
     */
    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        //Log.e(TAG, "onPeersAvailable : wifiP2pDeviceList.size : " + wifiP2pDeviceList.getDeviceList().size());
    }

    /*
    새로운 Wi-Fi P2P Group 생성
     */
    public void createGroup() {
        myManager.createGroup(myChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "Create Group Success");
                showToast("Create Group Success");
                isGroupExist = true;
            }

            @Overrideh
            public void onFailure(int i) {
                Log.e(TAG, "Create Group Failed");
                showToast("Create Group Failed :: " + i);
            }
        });
    }

    /*
    Wi-Fi P2P 그룹에서 탈퇴하는 기능
    Server Activity 는 항상 Group Owner로 동작하므로 그룹이 해산됨(예상)
     */
    public void removeGroup() {
        myManager.removeGroup(myChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "Remove Group Success");
                showToast("Remove Group Success");
                isGroupExist = false;
            }

            @Override
            public void onFailure(int i) {
                Log.e(TAG, "Remove Group Failed");
                showToast("Remove Group Failed :: " + i);
            }
        });
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

    /*
    Wi-Fi P2P Peerlist를 받아오기 위해 android 일정 버전 이상에서는 ACCESS_FINE_LOCATION 권한을 요구함.
    해당 권한은 Dangerous Permission에 해당되므로 runtime 중에 권한을 요청하여 허가받아야함.
     */
    public void permissionCheck(int permission) {
        int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0; // permission 1 : 정확한 위치 권한
        int MY_PERMISSIONS_REQUEST_CHANGE_WIFI_MULTICAST_STATE = 0; // permission 2 : 멀티캐스트 상태 권한
        int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0; // permission 3 : 외부 저장소 읽기 권한
        int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0; // permission 4 : 외부 저장소 쓰기 권한
        int permissionChecker;
        if (permission == 0 || permission == 1) {
            permissionChecker = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionChecker == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
        if (permission == 0 || permission == 2) {
            permissionChecker = ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_MULTICAST_STATE);
            if (permissionChecker == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CHANGE_WIFI_MULTICAST_STATE}, MY_PERMISSIONS_REQUEST_CHANGE_WIFI_MULTICAST_STATE);
            }
        }
        if (permission == 0 || permission == 3) {
            permissionChecker = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionChecker == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }
        if (permission == 0 || permission == 4) {
            permissionChecker = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionChecker == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    /*
    Server Activity 에서는 사용하지 않음
     */
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

    /*
    Server Activity 에서 WifiP2pGroup - DeviceInfoList 의 동기화를 위한 함수
    Group 의 ClientList 가 변경될 때마다 호출할 것을 권장
    Case 1 : Group 의 Client 수가 DeviceInfoList.size() 보다 큰 경우(새로운 클라이언트가 추가된 경우)
        새로운 Client 의 WifiP2pDevice 정보를 DeviceInfoList 에 add
    Case 2 : Group 의 Client 수가 DeviceInfoList.size() 보다 작은 경우(기존의 클라이언트가 제외된 경우)
        제외된 Client 의 DeviceInfo 정보를 List 에서 remove
    Case 3 : Group 의 Client 수가 DeviceInfoList.size() 와 같은 경우(그룹 정보가 유지되는 경우)
        현재 DeviceInfoList 의 정보와 Group 의 Client 정보가 일치하는지 확인 -> 불일치 시 Log.e
     */
    public void deviceListUpdate(WifiP2pGroup group) {
        if (group == null) { // nullPointException 방지
            myDeviceInfoList.clear();
            return;
        }
        if (myDeviceInfoList.size() < group.getClientList().size()) { //Case 1
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
                if (!exist) {
                    DeviceInfo di = new DeviceInfo(tempWifiP2pDeviceList.get(i));
                    myDeviceInfoList.add(di);
                    Log.v(TAG, "added : " + tempWifiP2pDeviceList.get(i).deviceName);
                    return;
                }
            }
        } else if (myDeviceInfoList.size() > group.getClientList().size()) { // Case 2
            Log.v(TAG, "deviceListUpdate : Case 2");
            ArrayList<WifiP2pDevice> tempWifiP2pDeviceList = new ArrayList<>(group.getClientList());
            boolean exist = false;
            if (group.getClientList().size() == 0) {
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
        } else if (myDeviceInfoList.size() == group.getClientList().size()) { // Case 3
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

    public void setMyDeviceInfo(WifiP2pInfo wifiP2pInfo) {
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int dpi = dm.densityDpi;
        float density = dm.density;
        boolean isGroupOwner = true;
        myDeviceInfo = new DeviceInfo(myWifiP2pDevice, wifiP2pInfo.groupOwnerAddress.getHostAddress(), width, height, dpi, density, isGroupOwner);
    }

}
