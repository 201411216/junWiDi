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

/*
WifiP2pManager 의 intent broadcast 를 받는 BroadcastReceiver.
WifiP2p 의 각종 상태 변화를 받아 적절한 함수 호출.
 */
public class MyBroadCastReceiver extends BroadcastReceiver {

    private static final String TAG = "MyBroadCastReceiver";

    public static WifiP2pManager mManager;
    public static WifiP2pManager.Channel mChannel;
    public MyDirectActionListener mListener;

    public MyBroadCastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MyDirectActionListener listener){
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent){ //아래의 intent filter를 통해 해당 action을 받을 시 호출
        String action = intent.getAction();
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){ //Wifi 상태가 변경시 호출됨
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                Log.d(TAG, "Wi-Fi enabled");
                mListener.setIsWifiP2pEnabled(true);
            }else{
                Log.d(TAG, "Wi-Fi failed");
                mListener.setIsWifiP2pEnabled(false);
            }
        }else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){ //dicoverPeer을 통해 Peer가 변경되었을 시 호출
            Log.v(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION");
            if(mManager != null) {
                mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() { //현재 WifiP2pDeviceList peers가 empty
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        mListener.onPeersAvailable(peers);
                    }
                });
            }
        }else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){ //Wifi P2P 연결의 상태가 변했을 때 호출됨
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
        }else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){ //기기 상태가 변했을 때 호출됨
            mListener.onSelfDeviceAvailable((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
    }

    public static IntentFilter getIntentFilter(){ //intent filter 반환
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
}
