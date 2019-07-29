package com.n3v.junwidi.Services;

import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.n3v.junwidi.BaseActivity;
import com.n3v.junwidi.ClientActivity;
import com.n3v.junwidi.Constants;
import com.n3v.junwidi.DeviceInfo;
import com.n3v.junwidi.MainActivity;
import com.n3v.junwidi.ServerActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.StringTokenizer;

public class MyClientTask extends AsyncTask<Void, Integer, String> {

    public static final String CLIENT_DOWNLOAD_SERVICE = "action.CLIENT_DOWNLOAD_SERVICE";
    public static final String CLIENT_HANDSHAKE_SERVICE = "action.CLIENT_HANDSHAKE_SERVICE";
    public static final String CLIENT_TEST_SERVICE = "action.CLIENT_TEST_SERVICE";
    public static final String CLIENT_MESSAGE_SERVICE = "action.CLIENT_MESSAGE_SERVICE";

    public String ACT_MODE = "";

    public String host_addr = "";

    private String time_test = "";

    private static final String TAG = "MyClientService";

    private Context myContext;

    private DeviceInfo myDeviceInfo;

    public MyClientTask(Context context, String mode, String addr, DeviceInfo deviceInfo) {
        myContext = context;
        ACT_MODE = mode;
        host_addr = addr;
        myDeviceInfo = deviceInfo;
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
        if (ACT_MODE.equals(CLIENT_TEST_SERVICE)) {
            Log.v(TAG, "ACT : SERVER_TEST_SERVICE");
            DatagramSocket socket = null;
            try {
                InetAddress addr = InetAddress.getByName(host_addr);
                socket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
                socket.setReuseAddress(true);
                socket.setSoTimeout(Constants.COMMON_TIMEOUT);
                byte[] buf = getDottedDecimalIP(getLocalIPAddress()).getBytes();
                Log.v(TAG, getDottedDecimalIP(getLocalIPAddress()));
                DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, Constants.FILE_SERVICE_PORT);
                Log.v(TAG, "Sending message");
                socket.send(packet);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : InetAddress addr = InetAddress.getByName(\"255.255.255.255\");");
            } catch (SocketException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : DatagramSocket socket = new DatagramSocket();");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : socket.send(packet);");
            } finally {
                socket.close();
            }
        } else if (ACT_MODE.equals(CLIENT_HANDSHAKE_SERVICE)) {
            Log.v(TAG, "ACT : SERVER_HANDSHAKE_SERVICE");
            DatagramSocket socket = null;
            try {
                InetAddress addr = InetAddress.getByName(host_addr);
                socket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
                socket.setSoTimeout(Constants.COMMON_TIMEOUT);
                socket.setReuseAddress(true);
                byte[] buf = myDeviceInfo.getString().getBytes();
                Log.v(TAG, "Handshake Info : " + myDeviceInfo.getString());
                DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, Constants.FILE_SERVICE_PORT);
                Log.v(TAG, "Send message complete");
                socket.send(packet);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : InetAddress addr = InetAddress.getByName(\"255.255.255.255\");");
            } catch (SocketException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : DatagramSocket socket = new DatagramSocket();");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : socket.send(packet);");
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        } else if (ACT_MODE.equals(CLIENT_MESSAGE_SERVICE)) {
            Log.v(TAG, "ACT : CLIENT_MESSAGE_SERVICE");
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
                socket.setReuseAddress(true);
                socket.setSoTimeout(Constants.COMMON_TIMEOUT);
                socket.setBroadcast(true);
                byte[] receivebuf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(receivebuf, receivebuf.length);
                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());
                Log.v(TAG, "Receive message : " + msg);
                StringTokenizer st = new StringTokenizer(msg, "+=+");
                if (st.hasMoreTokens()){
                    if (st.nextToken().equals("time_test")) {
                        time_test = st.nextToken();
                        publishProgress();
                    }
                }
            } catch (SocketTimeoutException e){
                Log.v(TAG, "CLIENT_MESSAGE_SERVICE : Socket Time out");
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
        }
        return "";
    }

    @Override
    protected void onProgressUpdate(Integer... values){
        super.onProgressUpdate(values);
        if (ACT_MODE.equals(CLIENT_MESSAGE_SERVICE)){
            Toaster.get().showToast(myContext, time_test, Toast.LENGTH_LONG);
        }
    }

    private byte[] getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
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
        for (int i = 0; i < ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i] & 0xFF;
        }
        return ipAddrStr;
    }

    public enum Toaster {
        INSTANCE;

        private final Handler handler = new Handler(Looper.getMainLooper());

        public void showToast(final Context context, final String message, final int length) {
            handler.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, message, length).show();
                        }
                    }
            );
        }

        public static Toaster get() {
            return INSTANCE;
        }
    }
}
