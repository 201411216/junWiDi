package com.n3v.junwidi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.ArrayList;

public class MyBroadCastReceiver extends BroadcastReceiver {

    public static final String TAG = "MyBroadCastReceiver";

    public static WifiP2pManager mManager;
    public static WifiP2pManager.Channel mChannel;
    public MyDirectActionListener mListener;
    public ArrayList<WifiP2pDevice> peers;

    public MyBroadCastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MyDirectActionListener listener){
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mListener = listener;
        peers = new ArrayList<WifiP2pDevice>();
    }

    @Override
    public void onReceive(Context context, Intent intent){
        String action = intent.getAction();
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                Log.d(TAG, "Wi-Fi enabled");
                mListener.setIsWifiP2pEnabled(true);
            }else{
                Log.d(TAG, "Wi-Fi failed");
                mListener.setIsWifiP2pEnabled(false);
            }
        }else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener(){
                @Override
                public void onPeersAvailable(WifiP2pDeviceList peers){
                    mListener.onPeersAvailable(peers);
                }
            });
        }else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if(networkInfo.isConnected()) {
                mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        mListener.onConnectionInfoAvailable(info);
                    }
                });
                Log.e(TAG, "P2P Device is already connected");
            }else{
                mListener.onDisconnection();
                Log.e(TAG, "P2P Device is disconnected");
            }
        }else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            mListener.onSelfDeviceAvailable((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
    }

    public static IntentFilter getIntentFilter(){
        IntentFilter intentFilter = new IntentFilter();
        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        return intentFilter;
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            peers.clear();
            peers.addAll(wifiP2pDeviceList.getDeviceList());
            if(peers.size() == 0){
                Log.d(TAG, "No Peer");
                return;
            }
        }
    };

}
