package com.n3v.junwidi.Services;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.n3v.junwidi.Constants;
import com.n3v.junwidi.DeviceInfo;
import com.n3v.junwidi.Listener.MyServerTaskListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

public class MyServerTask extends AsyncTask<Void, Integer, String> {

    public static final String SERVER_TEST_SERVICE = "action.SERVER_TEST_SERVICE";
    public static final String SERVER_HANDSHAKE_SERVICE = "action.SERVER_HANDSHAKE_SERVICE";
    public static final String SERVER_MESSAGE_SERVICE = "action.SERVER_MESSAGE_SERVICE";
    public static final String SERVER_FILE_TRANSFER_SERVICE = "action.SERVER_FILE_TRANSFER_SERVICE";
    public static final String SERVER_TCP_FILE_TRANSFER_SERVICE = "action.SERVER_TCP_FILE_TRANSFER_SERVICE";

    private static final String TAG = "MyServerTask";

    private Context myContext;
    private String ACT_MODE = "";
    private String host_addr = null;
    private DeviceInfo myDeviceInfo;
    private ArrayList<DeviceInfo> myDeviceInfoList;
    private ArrayAdapter<DeviceInfo> myServerAdapter;

    private Socket socket = null;
    private ServerSocket serverSocket = null;
    private DatagramSocket datagramSocket = null;
    private FileInputStream fis = null;

    private MyServerTaskListener serverTaskListener = null;

    String videoPath = "";

    private boolean handshaked = false;

    public MyServerTask(Context context, String mode, String host, DeviceInfo deviceInfo, ArrayList<DeviceInfo> deviceInfoList, ArrayAdapter serverAdapter, MyServerTaskListener serverTaskListener) {
        this.myContext = context;
        this.ACT_MODE = mode;
        this.host_addr = host;
        this.myDeviceInfo = deviceInfo;
        this.myDeviceInfoList = deviceInfoList;
        this.myServerAdapter = serverAdapter;
        this.serverTaskListener = serverTaskListener;
    }

    public MyServerTask(Context context, String mode, String host, DeviceInfo deviceInfo, ArrayList<DeviceInfo> deviceInfoList, ArrayAdapter serverAdapter, String videoPath, MyServerTaskListener serverTaskListener) {
        this.myContext = context;
        this.ACT_MODE = mode;
        this.host_addr = host;
        this.myDeviceInfo = deviceInfo;
        this.myDeviceInfoList = deviceInfoList;
        this.myServerAdapter = serverAdapter;
        this.videoPath = videoPath;
        this.serverTaskListener = serverTaskListener;
    }

    @Override
    protected String doInBackground(Void... voids) {
        if (ACT_MODE.equals(SERVER_TEST_SERVICE)) {
            Log.v(TAG, "ACT : SERVER_TEST_SERVICE");
            datagramSocket = null;
            try {
                datagramSocket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
                datagramSocket.setReuseAddress(true);
                datagramSocket.setSoTimeout(Constants.COMMON_TIMEOUT);
                byte[] receivebuf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(receivebuf, receivebuf.length);
                Log.v(TAG, "Before Receive");

                datagramSocket.receive(packet);
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
                if (datagramSocket != null) {
                    datagramSocket.close();
                }
            }
            return "";
        } else if (ACT_MODE.equals(SERVER_HANDSHAKE_SERVICE)) {
            Log.v(TAG, "ACT : SERVER_HANDSHAKE_SERVICE");
            datagramSocket = null;
            try {
                datagramSocket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
                datagramSocket.setReuseAddress(true);
                datagramSocket.setSoTimeout(Constants.HANDSHAKE_TIMEOUT);
                byte[] receivebuf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(receivebuf, receivebuf.length);
                datagramSocket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());
                Log.v(TAG, "Receive message : " + msg);
                StringTokenizer st;
                for (int i = 0; i < myDeviceInfoList.size(); i++) {
                    st = new StringTokenizer(msg, Constants.DELIMITER);
                    if (myDeviceInfoList.get(i).getWifiP2pDevice().deviceAddress.equals(st.nextToken())) {
                        myDeviceInfoList.get(i).setStr_address(st.nextToken());
                        myDeviceInfoList.get(i).setPx_width(Integer.parseInt(st.nextToken()));
                        myDeviceInfoList.get(i).setPx_height(Integer.parseInt(st.nextToken()));
                        myDeviceInfoList.get(i).setDpi(Integer.parseInt(st.nextToken()));
                        myDeviceInfoList.get(i).setDensity(Float.parseFloat(st.nextToken()));
                        myDeviceInfoList.get(i).setGroupOwner(Boolean.parseBoolean(st.nextToken()));
                        Log.v(TAG, "GET ADDRESS " + myDeviceInfoList.get(i).getStr_address());
                    }
                }
                publishProgress();
            } catch (SocketTimeoutException e) {
                Log.v(TAG, "SERVER_HANDSHAKE_SERVICE : Socket Time out");
            } catch (SocketException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : DatagramSocket socket = new DatagramSocket();");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : socket.send(packet);");
            } finally {
                if (datagramSocket != null) {
                    datagramSocket.close();
                }
            }
        } else if (ACT_MODE.equals(SERVER_MESSAGE_SERVICE)) {
            Log.v(TAG, "ACT : SERVER_MESSAGE_SERVICE");
            datagramSocket = null;
            try {
                InetAddress addr = InetAddress.getByName("192.168.49.255");
                datagramSocket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
                //socket.setSoTimeout(Constants.COMMON_TIMEOUT);
                datagramSocket.setReuseAddress(true);
                datagramSocket.setBroadcast(true);
                String time_msg = "time_test" + Constants.DELIMITER + getStrNow();
                byte[] buf = time_msg.getBytes();
                Log.v(TAG, "Handshake Info : " + time_msg);
                DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, Constants.FILE_SERVICE_PORT);
                Log.v(TAG, "Send message complete");
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
                if (datagramSocket != null) {
                    datagramSocket.close();
                }
            }
        } else if (ACT_MODE.equals(SERVER_TCP_FILE_TRANSFER_SERVICE)) {
            if (videoPath.equals("")) {
                Log.v(TAG, "ERROR : SERVER_TCP_FILE_TRANSFER_SERVICE : null video path");
                return "";
            }
            Log.v(TAG, "ACT : SERVER_TCP_FILE_TRANSFER_SERVICE");

            socket = null;
            fis = null;

            File myFile = new File(videoPath);

            double totalStartTime = 0;
            double totalEndTime = 0;
            double totalDiffTime = 0;
            double totalAvgTransferSpeed = 0;

            double startTime = 0;
            double endTime = 0;
            double diffTime = 0;
            double avgTransferSpeed = 0;

            int readByte;
            long totalReadByte;
            long fileSize = myFile.length();

            StringTokenizer st = new StringTokenizer(videoPath, "/");
            String videoName = "";
            while (st.hasMoreTokens()) {
                videoName = st.nextToken();
            }

            byte[] buffer = new byte[Constants.FILE_BUFFER_SIZE];

            if (!myFile.exists()) {
                Log.e(TAG, "ERROR : SERVER_TCP_TRANSFER_SERVICE : File doesn't exists");
                return "";
            }

            try {
                totalStartTime = System.currentTimeMillis();
                int deviceCount = 0;
                for (DeviceInfo di : myDeviceInfoList) {
                    socket = new Socket(di.getStr_address(), Constants.FILE_TRANSFER_PORT);
                    while (!socket.isConnected()) {
                        socket.close();
                        socket = new Socket(di.getStr_address(), Constants.FILE_TRANSFER_PORT);
                    }
 //                   if (!socket.isConnected()) {
 //                       Log.e(TAG, "ERROR : SERVER_TCP_TRANSFER_SERVICE : Socket connecting error");
 //                       socket.close();
 //                       continue;
 //                   }
                    Toaster.get().showToast(myContext, "Send File to " + di.getWifiP2pDevice().deviceName, Toast.LENGTH_SHORT);
                    startTime = System.currentTimeMillis();

                    Log.v(TAG, "post dos open");

                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    dos.writeUTF(Constants.TRANSFER_START + Constants.DELIMITER + videoName + Constants.DELIMITER + fileSize);

                    dos.close();
                    socket.close();

                    Log.v(TAG, "post server open");

                    serverSocket = new ServerSocket(Constants.FILE_TRANSFER_PORT);
                    socket = serverSocket.accept();
                    DataInputStream dis = new DataInputStream(socket.getInputStream());

                    boolean receiveOK = false;

                    String okMessage = dis.readUTF();
                    st = new StringTokenizer(okMessage, Constants.DELIMITER);
                    if (st.hasMoreTokens()) {
                        String receiverState = st.nextToken();
                        if (receiverState.equals(Constants.RECEIVE_WAIT)) {
                            receiveOK = true;
                        } else if (receiverState.equals(Constants.RECEIVE_DENY)) {
                            Toaster.get().showToast(this.myContext, di.getWifiP2pDevice().deviceName + " 수신이 거절되었습니다.", Toast.LENGTH_LONG);
                        }
                    }

                    if (!receiveOK) {
                        dos.close();
                        dis.close();
                        if (!serverSocket.isClosed()) {
                            serverSocket.close();
                        }
                        if (!socket.isClosed()) {
                            socket.close();
                        }
                        continue;
                    }

                    handshaked = true;
                    publishProgress();

                    fis = new FileInputStream(myFile);

                    OutputStream os = socket.getOutputStream();
                    totalReadByte = 0;
                    while ((readByte = fis.read(buffer)) > 0) {
                        os.write(buffer, 0, readByte);
                        totalReadByte += readByte;
                    }
                    endTime = System.currentTimeMillis();
                    diffTime = (endTime - startTime) / 1000;
                    avgTransferSpeed = ((double) fileSize / 1000) / diffTime;

                    Log.v(TAG, "Send " + deviceCount + " complete");
                    Log.v(TAG, "Time : " + diffTime + "(sec)");
                    Log.v(TAG, "AVG Transfer Speed : " + avgTransferSpeed + "(KB/s)");

                    String toastMessage = "#" + deviceCount + " " + di.getWifiP2pDevice().deviceName + " transfer complete";
                    Toaster.get().showToast(myContext, toastMessage, Toast.LENGTH_SHORT);

                    deviceCount++;
                    dos.close();
                    fis.close();
                    os.close();
                    if (!socket.isClosed()) {
                        socket.close();
                    }
                    if (!serverSocket.isClosed()) {
                        serverSocket.close();
                    }
                }
                totalEndTime = System.currentTimeMillis();
                totalDiffTime = (totalEndTime - totalStartTime) / 1000;
                totalAvgTransferSpeed = (((double) fileSize * deviceCount) / 1000) / totalDiffTime;
                Log.v(TAG, "Send to " + deviceCount + "device(s) complete");
                Log.v(TAG, "Total Time : " + totalDiffTime + "(sec)");
                Log.v(TAG, "Total AVG Transfer Speed : " + totalAvgTransferSpeed + "(KB/s)");
                String toastMessage = "Send to " + deviceCount + "device(s) complete";
                Toaster.get().showToast(myContext, toastMessage, Toast.LENGTH_LONG);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : SERVER_TCP_TRANSFER_SERVICE : IOException");
                String toastMessage = ("ERROR : SERVER_TCP_TRANSFER_SERVICE : IOException");
                Toaster.get().showToast(myContext, toastMessage, Toast.LENGTH_LONG);
            }

        }
        return "";
    }

    @Override
    protected void onProgressUpdate(Integer... values){
        super.onProgressUpdate(values);
        if (ACT_MODE.equals(SERVER_TCP_FILE_TRANSFER_SERVICE)) {
            if (handshaked == true) {
                serverTaskListener.onHandshaked();
            }
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (ACT_MODE.equals(SERVER_HANDSHAKE_SERVICE)) {
            myServerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCancelled(){
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (datagramSocket != null && !datagramSocket.isClosed()) {
                datagramSocket.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getStrNow() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return sdfNow.format(date);
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

        public static MyServerTask.Toaster get() {
            return INSTANCE;
        }
    }
}
