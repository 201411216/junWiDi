package com.n3v.junwidi;

import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

public class ClientActivity extends BaseActivity implements MyDirectActionListener {

    public static String TAG = "ClientActivity";

    private WifiP2pManager myManager;
    private WifiP2pManager.Channel myChannel;
    private boolean isWifiP2pEnabled = false;

    private MyBroadCastReceiver myBroadCastReceiver;

    private TextView txt_myDevice_Name;
    private TextView txt_myDevice_Address;
    private TextView txt_myDevice_State;
    private TextView btn_Refresh_Peer_List;
    private Button btn_Request_Connect;
    private Button btn_Request_Disconnect;
    private ListView listView_Server_List;

    private ArrayList<WifiP2pDevice> wifiP2pDeviceList;
    private MyClientAdapter myClientAdapter;

    private WifiP2pInfo myInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        myManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        myChannel = myManager.initialize(this, getMainLooper(), null);
        myBroadCastReceiver = new MyBroadCastReceiver(myManager, myChannel, this);
        registerReceiver(myBroadCastReceiver, MyBroadCastReceiver.getIntentFilter());
        initView();
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

    private void initView(){
        txt_myDevice_Name = findViewById(R.id.client_txt_my_device_name);
        txt_myDevice_Address = findViewById(R.id.client_txt_my_device_address);
        txt_myDevice_State = findViewById(R.id.client_txt_my_device_state);

        btn_Refresh_Peer_List = findViewById(R.id.client_btn_refresh_peer_list);
        btn_Request_Connect = findViewById(R.id.client_btn_request_connect);
        btn_Request_Disconnect = findViewById(R.id.client_btn_request_disconnect);

        listView_Server_List = findViewById(R.id.client_list_server);
        wifiP2pDeviceList = new ArrayList<WifiP2pDevice>();
        wifiP2pDeviceList.add(new WifiP2pDevice());
        myClientAdapter = new MyClientAdapter(this, R.layout.item_server, wifiP2pDeviceList);
        listView_Server_List.setAdapter(myClientAdapter);
        btn_Refresh_Peer_List.setOnClickListener(myClickListener);
        listView_Server_List.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                WifiP2pDevice d = wifiP2pDeviceList.get(position);
                connect(d);
            }
        });
    }

    private View.OnClickListener myClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.equals(btn_Refresh_Peer_List)) {
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
            } else if (v.equals(btn_Request_Connect)) {

            } else if (v.equals(btn_Request_Disconnect)) {
                disconnect();
            }
        }
    };

    public void connect(WifiP2pDevice d){
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = d.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        if(d.status == WifiP2pDevice.CONNECTED){
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

    public void disconnect(){
        myManager.removeGroup(myChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "Disconnect Success");
                showToast("Disconnect Success");
                myManager.requestPeers(myChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                        this.onPeersAvailable(wifiP2pDeviceList);
                    }
                });
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
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        wifiP2pDeviceList.clear();
        myClientAdapter.notifyDataSetChanged();
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
        myClientAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSelfDeviceAvailable(WifiP2pDevice wifiP2pDevice) {

    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        Log.v(TAG, "onPeersAvailable");
        myClientAdapter.addAll(wifiP2pDeviceList.getDeviceList());
        myClientAdapter.notifyDataSetChanged();
    }
}
