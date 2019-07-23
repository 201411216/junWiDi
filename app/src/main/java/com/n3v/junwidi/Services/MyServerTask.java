package com.n3v.junwidi.Services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import com.n3v.junwidi.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MyServerTask extends AsyncTask<Void, Integer, String> {

    private static final String TAG = "MyServerService";

    private Context myContext;

    public MyServerTask(Context context) {
        myContext = context;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            DatagramSocket socket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
            socket.setBroadcast(true);
            DatagramPacket packet = null;

            byte[] buffer = "Hello".getBytes();

            packet = new DatagramPacket(buffer, buffer.length, getBroadcastAddress(), Constants.FILE_SERVICE_PORT);

            socket.send(packet);

            byte[] buf = new byte[1024];
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            Log.e(TAG, buf.toString());

            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
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
