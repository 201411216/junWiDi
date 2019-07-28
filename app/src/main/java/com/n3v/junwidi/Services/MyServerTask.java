package com.n3v.junwidi.Services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.n3v.junwidi.Constants;
import com.n3v.junwidi.DeviceInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class MyServerTask extends AsyncTask<Void, Integer, String> {

    public static final String SERVER_TEST_SERVICE = "action.SERVER_TEST_SERVICE";
    public static final String SERVER_HANDSHAKE_SERVICE = "action.SERVER_HANDSHAKE_SERVICE";

    private static final String TAG = "MyServerTask";

    private Context myContext;
    private String ACT_MODE = "";
    private String host_addr = null;
    private DeviceInfo myDeviceInfo;
    private ArrayList<DeviceInfo> myDeviceInfoList;
    private ArrayAdapter<DeviceInfo> myServerAdapter;

    public MyServerTask(Context context, String mode, String host, DeviceInfo deviceInfo, ArrayList<DeviceInfo> deviceInfoList, ArrayAdapter serverAdapter) {
        myContext = context;
        ACT_MODE = mode;
        host_addr = host;
        myDeviceInfo = deviceInfo;
        myDeviceInfoList = deviceInfoList;
        myServerAdapter = serverAdapter;
    }

    @Override
    protected String doInBackground(Void... voids) {
        if (ACT_MODE.equals(SERVER_TEST_SERVICE)) {
            Log.v(TAG, "ACT : SERVER_TEST_SERVICE");
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
                socket.setReuseAddress(true);
                socket.setSoTimeout(Constants.COMMON_TIMEOUT);
                byte[] receivebuf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(receivebuf, receivebuf.length);
                Log.v(TAG, "Before Receive");
                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());
                Log.v(TAG, "Receive message : " + msg);
                socket.close();
            } catch (SocketException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : DatagramSocket socket = new DatagramSocket();");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : socket.send(packet);");
            } finally {
            if(socket != null){
                socket.close();
            }
        }
            return "";
        } else if (ACT_MODE.equals(SERVER_HANDSHAKE_SERVICE)) {
            Log.v(TAG, "ACT : SERVER_HANDSHAKE_SERVICE");
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
                socket.setReuseAddress(true);
                socket.setSoTimeout(Constants.COMMON_TIMEOUT);
                byte[] receivebuf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(receivebuf, receivebuf.length);
                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());
                Log.v(TAG, "Receive message : " + msg);
                StringTokenizer st = new StringTokenizer(msg, "//");
                for (int i = 0; i < myDeviceInfoList.size(); i++) {
                    st = new StringTokenizer(msg, "//");
                    if (myDeviceInfoList.get(i).getWifiP2pDevice().deviceName.equals(st.nextToken())) {
                        myDeviceInfoList.get(i).setStr_address(st.nextToken());
                        myDeviceInfoList.get(i).setPx_width(Integer.parseInt(st.nextToken()));
                        myDeviceInfoList.get(i).setPx_height(Integer.parseInt(st.nextToken()));
                        myDeviceInfoList.get(i).setDpi(Integer.parseInt(st.nextToken()));
                        myDeviceInfoList.get(i).setDensity(Float.parseFloat(st.nextToken()));
                    }
                }
                publishProgress();
            } catch (SocketTimeoutException e){
                Log.v(TAG, "SERVER_HANDSHAKE_SERVICE : Socket Time out");
            } catch (SocketException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : DatagramSocket socket = new DatagramSocket();");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : socket.send(packet);");
            } finally {
                if(socket != null){
                    socket.close();
                }
            }
        } else {
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result){
        if(ACT_MODE.equals(SERVER_HANDSHAKE_SERVICE)) {
            myServerAdapter.notifyDataSetChanged();
        }
    }
}
