package com.n3v.junwidi.Services;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.n3v.junwidi.Constants;
import com.n3v.junwidi.DeviceInfo;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;

import static android.content.Context.WIFI_SERVICE;

public class MyClientTask extends AsyncTask<Void, Integer, String> {

    public static final String CLIENT_DOWNLOAD_SERVICE = "action.CLIENT_DOWNLOAD_SERVICE";
    public static final String CLIENT_HANDSHAKE_SERVICE = "action.CLIENT_HANDSHAKE_SERVICE";
    public static final String CLIENT_TEST_SERVICE = "action.CLIENT_TEST_SERVICE";
    public static final String CLIENT_MESSAGE_SERVICE = "action.CLIENT_MESSAGE_SERVICE";
    public static final String CLIENT_FILE_RECEIVE_SERVICE = "action.CLIENT_FILE_RECEIVE_SERVICE";

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
        //Log.v(TAG, "onPostExecute");
    }

    public InetAddress getBroadcastAddress() throws IOException {
        WifiManager myWifiManager = (WifiManager) myContext.getSystemService(WIFI_SERVICE);
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
            WifiManager.MulticastLock multicastLock = null;
            try {
                WifiManager wifiManager = (WifiManager) myContext.getSystemService(Context.WIFI_SERVICE);
                multicastLock = wifiManager.createMulticastLock("n3v.junwidi");
                multicastLock.acquire();
                socket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
                socket.setReuseAddress(true);
                socket.setSoTimeout(Constants.LONG_TIMEOUT);
                socket.setBroadcast(true);
                byte[] receivebuf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(receivebuf, receivebuf.length);
                Log.v(TAG, "before : receive time_test");
                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());
                Log.v(TAG, "Receive message : " + msg);
                StringTokenizer st = new StringTokenizer(msg, "+=+");
                if (st.hasMoreTokens()) {
                    if (st.nextToken().equals("time_test")) {
                        time_test = st.nextToken();
                        publishProgress();
                    }
                }
            } catch (SocketTimeoutException e) {
                Log.v(TAG, "CLIENT_MESSAGE_SERVICE : Socket Time out");
            } catch (SocketException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : DatagramSocket socket = new DatagramSocket();");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : socket.send(packet);");
            } finally {
                if (socket != null) {
                    socket.close();
                    multicastLock.release();
                }
            }
        } else if (ACT_MODE.equals(CLIENT_FILE_RECEIVE_SERVICE)) {
            Log.v(TAG, "ACT : CLIENT_FILE_RECEIVE_SERVICE");
            DatagramSocket socket = null;
            DataOutputStream dos = null;
            WifiManager.MulticastLock multicastLock = null;
            String fileName = "";
            long fileSize = 0;
            String filePath = "";
            File newVideo;
            int count = 0;
            int nextPRE = 0;
            try {
                WifiManager wifiManager = (WifiManager) myContext.getSystemService(Context.WIFI_SERVICE);
                multicastLock = wifiManager.createMulticastLock("n3v.junwidi");
                multicastLock.acquire();
                socket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
                socket.setReuseAddress(true);
                socket.setSoTimeout(Constants.LONG_TIMEOUT);
                socket.setBroadcast(true);
                byte[] receivebuf = new byte[Constants.FILE_BUFFER_SIZE];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(receivebuf, receivebuf.length);
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength());
                    if (msg.startsWith("START")) {
                        StringTokenizer st = new StringTokenizer(msg, "+=+");
                        if (st.hasMoreTokens()) {
                            if (st.nextToken().equals("START")) {
                                fileName = st.nextToken();
                                fileSize = Long.valueOf(st.nextToken());
                            }
                        }
                        File newDir = new File(myContext.getExternalFilesDir(null), "TogetherTheater");
                        if (!newDir.exists()) {
                            Log.v(TAG, "mkdir1");
                            newDir.mkdir();
                        }
                        newVideo = new File(myContext.getExternalFilesDir(null) + "/TogetherTheater", fileName);
                        if (!newVideo.createNewFile()) {
                            Log.v(TAG, "mkdir2 already exists");
                        }
                        dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(newVideo)));
                    } else if (msg.startsWith("END")) {
                        Log.v(TAG, "CLIENT_FILE_RECEIVE_SERVICE : Receiving complete");
                        dos.close();
                        break;
                    } else if (msg.startsWith("PRE")) {
                        StringTokenizer st = new StringTokenizer(msg, "+=+");
                        int curPRE = -1;
                        if (st.hasMoreTokens()) {
                            if (st.nextToken().equals("PRE")){
                                curPRE = Integer.valueOf(st.nextToken());
                            }
                        }
                        if (curPRE == nextPRE){

                        }
                        dos.write(receivebuf, 0, receivebuf.length);
                    } else {
                        dos.write(receivebuf, 0, receivebuf.length);
                    }
                    count++;
                    Log.v(TAG, "Count : " + count);
                }
            } catch (SocketTimeoutException e) {
                Log.v(TAG, "CLIENT_MESSAGE_SERVICE : Socket Time out");
            } catch (SocketException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : DatagramSocket socket = new DatagramSocket();");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : socket.send(packet);");
            } finally {
                if (socket != null) {
                    socket.close();
                    multicastLock.release();
                }
            }

        }
        return "";
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (ACT_MODE.equals(CLIENT_MESSAGE_SERVICE)) {
            Toaster.get().showToast(myContext, time_test + "\n" + getStrNow(), Toast.LENGTH_LONG);
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

    public String getStrNow() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return sdfNow.format(date);
    }
}
