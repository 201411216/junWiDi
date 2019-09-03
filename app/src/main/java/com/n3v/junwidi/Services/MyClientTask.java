package com.n3v.junwidi.Services;

import android.content.Context;
import android.content.Intent;
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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;

import static android.content.Context.WIFI_SERVICE;

public class MyClientTask extends AsyncTask<Void, Integer, String> {

    public static final String CLIENT_DOWNLOAD_SERVICE = "tt.client.DOWNLOAD_SERVICE";
    public static final String CLIENT_HANDSHAKE_SERVICE = "tt.client.HANDSHAKE_SERVICE";
    public static final String CLIENT_TEST_SERVICE = "tt.client.TEST_SERVICE";
    public static final String CLIENT_MESSAGE_SERVICE = "tt.client.MESSAGE_SERVICE";
    public static final String CLIENT_FILE_RECEIVE_SERVICE = "tt.client.FILE_RECEIVE_SERVICE";
    public static final String CLIENT_TCP_FILE_RECEIVE_SERVICE = "tt.client.TCP_FILE_RECEIVE_SERVICE";
    public static final String CLIENT_CONTROL_SERVICE = "tt.client.CONTROL_SERVICE";

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
                StringTokenizer st = new StringTokenizer(msg, Constants.DELIMITER);
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
        } else if (ACT_MODE.equals(CLIENT_TCP_FILE_RECEIVE_SERVICE)) {
            ServerSocket serverSocket = null;
            Socket socket = null;

            String fileName = "";
            long fileSize = 0;

            File newVideo = null;

            double startTime = 0;
            double endTime = 0;
            double diffTime = 0;
            double avgReceiveSpeed = 0;

            int readByte = 0;
            int totalReadByte = 0;

            byte[] buffer = new byte[Constants.FILE_BUFFER_SIZE];

            try {
                serverSocket = new ServerSocket(Constants.FILE_SERVICE_PORT);
                socket = serverSocket.accept();

                DataInputStream dis = new DataInputStream(socket.getInputStream());
                String receiveMessage = dis.readUTF();
                StringTokenizer st = new StringTokenizer(receiveMessage, Constants.DELIMITER);
                if (st.hasMoreTokens()) {
                    if (st.nextToken().equals(Constants.TRANSFER_START)) {
                        if (st.hasMoreTokens()) {
                            fileName = st.nextToken();
                            if (st.hasMoreTokens()) {
                                fileSize = Long.valueOf(st.nextToken());
                            }
                        }
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

                Toaster.get().showToast(myContext, "File " + fileName + " receive start", Toast.LENGTH_SHORT);

                startTime = System.currentTimeMillis();

                FileOutputStream fos = new FileOutputStream(newVideo);
                InputStream is = socket.getInputStream();

                while ((readByte = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, readByte);
                }

                endTime = System.currentTimeMillis();
                diffTime = (endTime - startTime);
                avgReceiveSpeed = ((double) fileSize / 1000) / diffTime;

                Log.v(TAG, "Receive " + fileName + " complete");
                Log.v(TAG, "Time : " + diffTime + "(sec)");
                Log.v(TAG, "AVG Receive Speed : " + avgReceiveSpeed + "(KB/s)");

                Toaster.get().showToast(myContext, "Receive " + fileName + " complete", Toast.LENGTH_LONG);

                dis.close();
                is.close();
                fos.close();
                socket.close();
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : CLIENT_TCP_FILE_RECEIVE_SERVICE : IOException");
            }
        } else if (ACT_MODE.equals(CLIENT_CONTROL_SERVICE)) {
            Log.v(TAG, "ACT : CLIENT_CONTROL_SERVICE");
            DatagramSocket socket = null;
            DataOutputStream dos = null;
            WifiManager.MulticastLock multicastLock = null;

            String file_name = "";

            try {
                WifiManager wifiManager = (WifiManager) myContext.getSystemService(Context.WIFI_SERVICE);
                multicastLock = wifiManager.createMulticastLock("n3v.junwidi");
                multicastLock.acquire();
                socket = new DatagramSocket(Constants.CONTROL_SERVICE_PORT);
                socket.setReuseAddress(true);
                socket.setSoTimeout(Constants.LONG_TIMEOUT);
                socket.setBroadcast(true);

                byte[] receivebuf;

                while (true) {

                    receivebuf = new byte[Constants.CONTROL_BUFFER_SIZE];
                    DatagramPacket packet = new DatagramPacket(receivebuf, receivebuf.length);
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, Constants.CONTROL_BUFFER_SIZE);

                    if (msg.startsWith(Constants.CONTROL_PREPARE)) {

                    } else if (msg.startsWith(Constants.CONTROL_PLAY)) {

                    } else if (msg.startsWith(Constants.CONTROL_PAUSE)) {

                    } else if (msg.startsWith(Constants.CONTROL_RESUME)) {

                    } else if (msg.startsWith(Constants.CONTROL_STOP)) {
                        break;
                    } else if (msg.startsWith(Constants.CONTROL_MOVE)) {

                    }

                }

            } catch (IOException e){

            } finally {
                if (!socket.isClosed()) {
                    socket.close();
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
