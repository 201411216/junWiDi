package com.n3v.junwidi.Services;

import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import com.n3v.junwidi.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MyClientTask extends AsyncTask<Void, Integer, String> {

    public static final String CLIENT_DOWNLOAD_SERVICE = "action.CLIENT_DOWNLOAD_SERVICE";

    private static final String TAG = "MyClientService";

    private Context myContext;

    public MyClientTask(Context context) {
        myContext = context;
    }

//    @Override
//    protected String doInBackground(Void... voids) {
//        WifiManager myWifiManager = (WifiManager) myContext.getSystemService(Context.WIFI_SERVICE);
//        WifiManager.MulticastLock multiCastLock = myWifiManager.createMulticastLock("multicastLock");
//        multiCastLock.setReferenceCounted(true);
//        multiCastLock.acquire();
//
//        InetAddress address = null;
//        MulticastSocket clientSocket = null;
//
//        try {
//            clientSocket = new MulticastSocket(Constants.FILE_SERVICE_PORT);
//            address = InetAddress.getByName("224.0.0.1");
//            clientSocket.joinGroup(address);
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        DatagramPacket packet = null;
//        byte[] buffer = new byte[1024];
//        packet = new DatagramPacket(buffer, buffer.length);
//        try {
//            clientSocket.receive(packet);
//            byte[] data = packet.getData();
//            Log.d(TAG, "Data : " + data.toString());
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//
//        multiCastLock.release();
//
//        try{
//            clientSocket.leaveGroup(address);
//        } catch (IOException e){
//            e.printStackTrace();
//        }
//        clientSocket.close();
//        return null;
//    }
//
//
    @Override
    protected void onPostExecute(String result) {
        Log.v(TAG, "onPostExecute");
    }

    public InetAddress getBroadcastAddress() throws IOException {
        WifiManager myWifiManager = (WifiManager) myContext.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = myWifiManager.getDhcpInfo();

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        }

        return InetAddress.getByAddress(quads);
    }

    @Override
    protected String doInBackground(Void... voids) {
        Log.v(TAG, "doInBackground");
        WifiManager wifi = (WifiManager) myContext.getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock lock = wifi.createMulticastLock("jun.widi");
        lock.acquire();

        DatagramSocket clientSocket = null;

        try {
            clientSocket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
        } catch (SocketException e) {
            Log.e(TAG, "Error : clientSocket = new DatagramSocket(Constants.FILE_SERVICE_PORT);");
            e.printStackTrace();
        }

        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        try{
            clientSocket.receive(packet);
        } catch (IOException e){
            Log.e(TAG, "Error : clientSocket.receive(packet);");
            e.printStackTrace();
        }
        String message = new String(packet.getData());
        Log.v(TAG, "Message : " + message);

        lock.release();
        clientSocket.close();
        return null;
    }
}
