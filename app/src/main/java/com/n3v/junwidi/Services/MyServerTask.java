package com.n3v.junwidi.Services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import com.n3v.junwidi.Constants;
import com.n3v.junwidi.DeviceInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
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

    public MyServerTask(Context context, String mode, String host, DeviceInfo deviceInfo, ArrayList<DeviceInfo> deviceInfoList) {
        myContext = context;
        ACT_MODE = mode;
        host_addr = host;
        myDeviceInfo = deviceInfo;
        myDeviceInfoList = deviceInfoList;
    }

    @Override
    protected String doInBackground(Void... voids) {
        if (ACT_MODE.equals(SERVER_TEST_SERVICE)) {
            Log.v(TAG, "ACT : SERVER_TEST_SERVICE");
            try {
                DatagramSocket socket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
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
            }
            return "";
        } else if (ACT_MODE.equals(SERVER_HANDSHAKE_SERVICE)) {
            Log.v(TAG, "ACT : SERVER_HANDSHAKE_SERVICE");
            try {
                DatagramSocket socket = new DatagramSocket();
                socket.setReuseAddress(true);
                socket.bind(new InetSocketAddress(Constants.FILE_SERVICE_PORT));
                //socket.setSoTimeout(Constants.COMMON_TIMEOUT);
                byte[] receivebuf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(receivebuf, receivebuf.length);
                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());
                StringTokenizer st = new StringTokenizer(msg, "//");
                for (int i = 0; i < myDeviceInfoList.size(); i++) {
                    if (myDeviceInfoList.get(i).getWifiP2pDevice().deviceName.equals(st.nextToken())) {
                        myDeviceInfoList.get(i).setStr_address(st.nextToken());
                        myDeviceInfoList.get(i).setPx_width(Integer.parseInt(st.nextToken()));
                        myDeviceInfoList.get(i).setPx_height(Integer.parseInt(st.nextToken()));
                        myDeviceInfoList.get(i).setDpi(Integer.parseInt(st.nextToken()));
                        myDeviceInfoList.get(i).setDensity(Float.parseFloat(st.nextToken()));
                    }
                }
                Log.v(TAG, "Receive message : " + msg);
                socket.close();
            } catch (SocketException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : DatagramSocket socket = new DatagramSocket();");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : socket.send(packet);");
            }
        } else {
        }

        return "";

//        try {
//            DatagramSocket socket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
//            socket.setBroadcast(true);
//            DatagramPacket packet = null;
//
//            byte[] buffer = "Hello".getBytes();
//
//            packet = new DatagramPacket(buffer, buffer.length, getBroadcastAddress(), Constants.FILE_SERVICE_PORT);
//
//            socket.send(packet);
//
//            byte[] buf = new byte[1024];
//            packet = new DatagramPacket(buf, buf.length);
//            socket.receive(packet);
//
//            Log.e(TAG, buf.toString());
//
//            socket.close();
//        } catch (SocketException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;

    }

    InetAddress getBroadcastAddress() throws IOException {
        WifiManager myWifiManager = (WifiManager) myContext.getSystemService(myContext.WIFI_SERVICE);
        DhcpInfo dhcp = myWifiManager.getDhcpInfo();

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int i = 0; i < 4; i++) {
            quads[i] = (byte) ((broadcast >> i * 8) & 0xFF);
        }
        return InetAddress.getByAddress(quads);
    }

//    @Override
//    protected void onPostExecute(String result){
//        Log.v(TAG, "onPostExecute");
//    }
//
//    @Override
//    protected String doInBackground(Void... voids) {
//        Log.v(TAG, "doInBackground");
//        DatagramSocket socket = null;
//        try {
//            socket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
//        } catch (SocketException e){
//            Log.e(TAG, "Error : socket = new DatagramSocket(Constants.FILE_SERVICE_PORT);");
//            e.printStackTrace();
//        }
//        InetAddress group = null;
//        try {
//            group = InetAddress.getByName("224.0.0.1");
//        } catch (UnknownHostException e){
//            Log.e(TAG, "Error : group = InetAddress.getByName(\"224.0.0.1\");");
//            socket.close();
//            e.printStackTrace();
//        }
//
//        //Multicast Group 에게 전송
//        final String MESSAGE_TO_TEST_SEND = "TEST";
//        byte buffer[] = MESSAGE_TO_TEST_SEND.getBytes();
//        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, Constants.FILE_SERVICE_PORT);
//        try {
//            socket.send(packet);
//            Log.d("Send", "Sending Packet");
//        } catch (IOException e){
//            Log.e(TAG, "Error : socket.send(packet);");
//            socket.close();
//            e.printStackTrace();
//        }
//        socket.close();
//        return null;
//    }
}
