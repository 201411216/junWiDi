package com.n3v.junwidi.Services;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.n3v.junwidi.Utils.Constants;
import com.n3v.junwidi.Datas.DeviceInfo;
import com.n3v.junwidi.Listener.MyClientTaskListener;

import java.io.DataInputStream;
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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;

public class MyClientTask extends AsyncTask<Void, Integer, String> {

    public static final String CLIENT_DOWNLOAD_SERVICE = "tt.client.DOWNLOAD_SERVICE";
    public static final String CLIENT_HANDSHAKE_SERVICE = "tt.client.HANDSHAKE_SERVICE";
    public static final String CLIENT_TEST_SERVICE = "tt.client.TEST_SERVICE";
    public static final String CLIENT_MESSAGE_SERVICE = "tt.client.MESSAGE_SERVICE";
    public static final String CLIENT_FILE_RECEIVE_SERVICE = "tt.client.FILE_RECEIVE_SERVICE";
    public static final String CLIENT_TCP_FILE_RECEIVE_SERVICE = "tt.client.TCP_FILE_RECEIVE_SERVICE";
    public static final String CLIENT_CONTROL_SERVICE = "tt.client.CONTROL_SERVICE";
    public static final String CLIENT_TCP_FILE_RECEIVE_WAITING_SERVICE = "tt.client.TCP_FILE_RECEIVE_WAITING_SERVICE";
    public static final String CLIENT_TCP_CANCEL_WAITING_SERVICE = "tt.client.TCP_CANCEL_WAITING_SERVICE";
    public static final String CLIENT_TCP_GO_SIGNAL_SERVICE = "tt.client.TCP_GO_SIGNAL_SERVICE";

    public String ACT_MODE = "";

    public String host_addr = "";

    private String time_test = "";

    private static final String TAG = "MyClientService";

    private Context myContext;

    private DeviceInfo myDeviceInfo;

    private String fileName = "";
    private long fileSize = 0;

    private boolean end_wait = false;

    private int progress = 0;

    private MyClientTaskListener clientTaskListener = null;

    private boolean waitingCancelled = false;
    private boolean needDelete = false;
    private boolean receiveComplete = false;

    private Socket socket = null;
    private DatagramSocket datagramSocket = null;
    private ServerSocket serverSocket = null;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;
    private FileOutputStream fos = null;
    private InputStream is = null;

    private String result = "";

    public MyClientTask(Context context, String mode, String addr, DeviceInfo deviceInfo, MyClientTaskListener clientTaskListener, String fileName, long fileSize) {
        myContext = context;
        ACT_MODE = mode;
        host_addr = addr;
        myDeviceInfo = deviceInfo;
        this.clientTaskListener = clientTaskListener;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    @Override
    protected void onPostExecute(String result) {
        //Log.v(TAG, "onPostExecute");
    }

    @Override
    protected String doInBackground(Void... voids) {
        if (ACT_MODE.equals(CLIENT_TEST_SERVICE)) {
            Log.v(TAG, "ACT : SERVER_TEST_SERVICE");
            datagramSocket = null;
            try {
                InetAddress addr = InetAddress.getByName(host_addr);
                datagramSocket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
                datagramSocket.setReuseAddress(true);
                datagramSocket.setSoTimeout(Constants.COMMON_TIMEOUT);
                byte[] buf = getDottedDecimalIP(getLocalIPAddress()).getBytes();
                Log.v(TAG, getDottedDecimalIP(getLocalIPAddress()));
                DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, Constants.FILE_SERVICE_PORT);
                Log.v(TAG, "Sending message");
                datagramSocket.send(packet);
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
                if (datagramSocket != null && !datagramSocket.isClosed()) {
                    datagramSocket.close();
                }
            }
        } else if (ACT_MODE.equals(CLIENT_HANDSHAKE_SERVICE)) {
            Log.v(TAG, "ACT : SERVER_HANDSHAKE_SERVICE");
            try {
                /*
                InetAddress addr = InetAddress.getByName(host_addr);
                datagramSocket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
                datagramSocket.setSoTimeout(Constants.COMMON_TIMEOUT);
                datagramSocket.setReuseAddress(true);
                byte[] buf = myDeviceInfo.getString().getBytes();
                Log.v(TAG, "Handshake Info : " + myDeviceInfo.getString());
                DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, Constants.FILE_SERVICE_PORT);
                datagramSocket.send(packet);
                Log.v(TAG, "Send message complete");
                publishProgress();
                */
                socket = new Socket(host_addr, Constants.FILE_SERVICE_PORT);

                /*
                if (!socket.isConnected()) {
                    Log.e(TAG, "ERROR : CLIENT_TCP_FILE_RECEIVE_SERVICE : Socket connecting error");
                    socket.close();
                    return "";
                }
                */

                Log.v(TAG, "socket accepted");

                String okMessage = myDeviceInfo.getString();

                while (!socket.isConnected()) {
                }

                dos = new DataOutputStream(socket.getOutputStream());

                dos.writeUTF(okMessage);
                Log.v(TAG, "HANDSHAKE message : " + okMessage);
                publishProgress();
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
                try {
                    if (socket != null && !socket.isClosed()) {
                        Log.v(TAG, "handshake socket closing");
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (ACT_MODE.equals(CLIENT_MESSAGE_SERVICE)) {
            Log.v(TAG, "ACT : CLIENT_MESSAGE_SERVICE");
            datagramSocket = null;
            WifiManager.MulticastLock multicastLock = null;
            try {
                WifiManager wifiManager = (WifiManager) myContext.getSystemService(Context.WIFI_SERVICE);
                multicastLock = wifiManager.createMulticastLock("n3v.junwidi");
                multicastLock.acquire();
                datagramSocket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
                datagramSocket.setReuseAddress(true);
                datagramSocket.setSoTimeout(Constants.LONG_TIMEOUT);
                datagramSocket.setBroadcast(true);
                byte[] receivebuf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(receivebuf, receivebuf.length);
                Log.v(TAG, "before : receive time_test");
                datagramSocket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());
                Log.v(TAG, "Receive message : " + msg);
                StringTokenizer st = new StringTokenizer(msg, Constants.DELIMITER);
                if (st.hasMoreTokens()) {
                    if (st.nextToken().equals("time_test")) {
                        time_test = st.nextToken();
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
                if (datagramSocket != null && !datagramSocket.isClosed()) {
                    datagramSocket.close();
                    multicastLock.release();
                }
            }
        } else if (ACT_MODE.equals(CLIENT_TCP_FILE_RECEIVE_WAITING_SERVICE)) {
            Log.v(TAG, "CLIENT_TCP_FILE_RECEIVE_WAITING_SERVICE act");
            serverSocket = null;
            socket = null;

            byte[] buffer = new byte[Constants.FILE_BUFFER_SIZE];

            try {
                try {


                    Log.v(TAG, Boolean.toString(isCancelled()));
                    serverSocket = new ServerSocket(Constants.FILE_TRANSFER_PORT);
                    socket = serverSocket.accept();
                    socket.setSoTimeout(1000);
                    Log.v(TAG, Boolean.toString(socket.isConnected()));

                    dis = new DataInputStream(socket.getInputStream());

                    String receiveMessage = dis.readUTF();
                    if (receiveMessage.startsWith(Constants.CANCEL_WAITING)) {
                        Log.v(TAG, "Waiting loop is cancelled");
                        waitingCancelled = true;
                    } else {
                        StringTokenizer st = new StringTokenizer(receiveMessage, Constants.DELIMITER);
                        if (st.hasMoreTokens()) {
                            if (st.nextToken().equals(Constants.TRANSFER_START)) {
                                if (st.hasMoreTokens()) {
                                    fileName = st.nextToken();
                                    if (st.hasMoreTokens()) {
                                        fileSize = Long.valueOf(st.nextToken());
                                        publishProgress();
                                        end_wait = true;
                                        //break;
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } finally {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                        socket = null;
                    }
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                        socket = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else if (ACT_MODE.equals(CLIENT_TCP_CANCEL_WAITING_SERVICE)) {
            Log.v(TAG, "ACT : CLIENT_TCP_CANCEL_WAITING_SERVICE");
            try {
                socket = new Socket(myDeviceInfo.getStr_address(), Constants.FILE_TRANSFER_PORT);

                dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF(Constants.CANCEL_WAITING);

                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (ACT_MODE.equals(CLIENT_TCP_FILE_RECEIVE_SERVICE)) {
            socket = null;

            File newVideo = null;

            double startTime = 0;
            double endTime = 0;
            double diffTime = 0;
            double avgReceiveSpeed = 0;

            int readByte = 0;
            long sumReadByte = 0;
            long lastPublishedReadByte = 0;

            byte[] buffer = new byte[Constants.FILE_BUFFER_SIZE];

            try {
                socket = new Socket(host_addr, Constants.FILE_TRANSFER_PORT);

                if (!socket.isConnected()) {
                    Log.e(TAG, "ERROR : CLIENT_TCP_FILE_RECEIVE_SERVICE : Socket connecting error");
                    socket.close();
                    return "";
                }

                String okMessage = Constants.RECEIVE_WAIT;

                dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF(okMessage);

                dis = new DataInputStream(socket.getInputStream());

                File newDir = new File(myContext.getExternalFilesDir(null), "TogetherTheater");
                if (!newDir.exists()) {
                    Log.v(TAG, "mkdir1");
                    newDir.mkdir();
                }
                Log.v(TAG, fileName + "!");

                newVideo = new File(newDir, fileName);
                Log.v(TAG, newVideo.getAbsolutePath());
                if (!newVideo.createNewFile()) {
                    Log.v(TAG, "mkdir2 already exists");
                }

                this.needDelete = true;

                Toaster.get().showToast(myContext, "File " + fileName + " receive start", Toast.LENGTH_SHORT);

                startTime = System.currentTimeMillis();

                fos = new FileOutputStream(newVideo);
                is = socket.getInputStream();

                while ((readByte = is.read(buffer)) > 0 && !isCancelled()) {
                    fos.write(buffer, 0, readByte);
                    sumReadByte += readByte;
                    if (sumReadByte - lastPublishedReadByte > (fileSize / 100)) {
                        this.progress++;
                        lastPublishedReadByte = sumReadByte;
                        publishProgress();
                    }
                }

                if (!isCancelled()) {
                    this.needDelete = false;
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
                if (!needDelete) {
                    receiveComplete = true;
                }
                publishProgress();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : CLIENT_TCP_FILE_RECEIVE_SERVICE : IOException");
            } finally {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (ACT_MODE.equals(CLIENT_CONTROL_SERVICE)) {
            Log.v(TAG, "ACT : CLIENT_CONTROL_SERVICE");
            datagramSocket = null;
            dos = null;
            WifiManager.MulticastLock multicastLock = null;

            String file_name = "";

            try {
                WifiManager wifiManager = (WifiManager) myContext.getSystemService(Context.WIFI_SERVICE);
                multicastLock = wifiManager.createMulticastLock("n3v.junwidi");
                multicastLock.acquire();
                datagramSocket = new DatagramSocket(Constants.CONTROL_SERVICE_PORT);
                datagramSocket.setReuseAddress(true);
                datagramSocket.setSoTimeout(Constants.LONG_TIMEOUT);
                datagramSocket.setBroadcast(true);

                byte[] receivebuf;

                while (!this.isCancelled()) {

                    receivebuf = new byte[Constants.CONTROL_BUFFER_SIZE];
                    DatagramPacket packet = new DatagramPacket(receivebuf, receivebuf.length);
                    datagramSocket.receive(packet);
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
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (!socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    @Override
    protected void onCancelled() {
        Log.v(TAG, "normal cancel");
        handleOnCancelled(this.result);
        super.onCancelled();
    }

    @Override
    protected void onCancelled(String result) {
        Log.v(TAG, "result cancel");
        handleOnCancelled(result);
        super.onCancelled(result);
    }

    private void handleOnCancelled(String result) {
        Log.v(TAG, "onCancel");
        if (needDelete) {
            File newVideo = new File(myContext.getExternalFilesDir(null) + "/TogetherTheater", fileName);
            if (newVideo.exists()) {
                newVideo.delete();
                Toaster.get().showToast(myContext, "영상 수신이 취소되어 작성중이던 파일을 삭제합니다", Toast.LENGTH_SHORT);
            }
        }
        try {
            if (socket != null && !socket.isClosed()) {
                Log.v(TAG, "socket closed by cancel");
                socket.close();
            }
            if (datagramSocket != null && !datagramSocket.isClosed()) {
                Log.v(TAG, "datagram socket closed by cancel");
                datagramSocket.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                Log.v(TAG, "server socket closed by cancel");
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "MyClientTask : onCancelled");
        super.onCancelled();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (ACT_MODE.equals(CLIENT_MESSAGE_SERVICE)) {
            Toaster.get().showToast(myContext, time_test + "\n" + getStrNow(), Toast.LENGTH_LONG);
        } else if (ACT_MODE.equals(CLIENT_TCP_FILE_RECEIVE_SERVICE)) {
            if (receiveComplete) {
                clientTaskListener.onReceiveFinished();
            } else {
                clientTaskListener.progressUpdate(this.progress);
            }
        } else if (ACT_MODE.equals(CLIENT_HANDSHAKE_SERVICE)) {
            clientTaskListener.onHandshaked();
        } else if (ACT_MODE.equals(CLIENT_TCP_FILE_RECEIVE_WAITING_SERVICE)) {
            clientTaskListener.setFile(fileName, fileSize);
            clientTaskListener.onEndWait();
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
