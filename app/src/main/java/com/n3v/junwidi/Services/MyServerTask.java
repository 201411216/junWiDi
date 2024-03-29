package com.n3v.junwidi.Services;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.n3v.junwidi.Utils.Constants;
import com.n3v.junwidi.Datas.DeviceInfo;
import com.n3v.junwidi.Listener.MyServerTaskListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
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
    public static final String SERVER_TCP_SHOW_GUIDELINE_SERVICE = "action.SERVER_TCP_SHOW_GUIDELINE_SERVICE";
    public static final String SERVER_TCP_PREPARE_PLAY_SERVICE = "action.SERVER_TCP_PREPARE_PLAY_SERVICE";
    public static final String SERVER_CONTROL_WAITING_SERVICE = "action.server.CONTROL_WAITING_SERVICE";
    public static final String SERVER_CONTROL_SEND_PLAY_SERVICE = "action.server.CONTROL_SEND_PLAY_SERVICE";
    public static final String SERVER_CONTROL_SEND_PAUSE_SERVICE = "action.server.CONTROL_SEND_PAUSE_SERVICE";
    public static final String SERVER_CONTROL_SEND_SEEKTIME_SERVICE = "action.server.CONTROL_SEND_SEEKTIME_SERVICE";
    public static final String SERVER_CONTROL_SEND_STOP_SERVICE = "action.server.CONTROL_SEND_STOP_SERVICE";
    public static final String SERVER_CONTROL_CANCEL_SERVICE = "action.server.CONTROL_CANCEL_SERVICE";

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

    private String videoPath = "";

    private boolean waiting = false;
    private boolean handshaked = false;
    private boolean sended = false;
    private boolean send_finished = false;
    private boolean sending = false;
    private boolean already_exists = false;
    private boolean guideline_sended = false;
    private boolean isAllDeviceGuideLineReady = false;
    private boolean isAllDevicePreparePlay = false;

    private boolean receivePlay = false;
    private boolean receivePause = false;
    private boolean receiveStop = false;
    private boolean receiveSeektime = false;
    private int seekingTime = 0;

    private long sumReadByte = 0;
    private long lastPublishedReadByte = 0;
    private int progress = 0;

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

    public MyServerTask(Context context, String mode, String host, DeviceInfo deviceInfo, ArrayList<DeviceInfo> deviceInfoList, ArrayAdapter serverAdapter, String videoPath, MyServerTaskListener serverTaskListener, int seekingTime) {
        this.myContext = context;
        this.ACT_MODE = mode;
        this.host_addr = host;
        this.myDeviceInfo = deviceInfo;
        this.myDeviceInfoList = deviceInfoList;
        this.myServerAdapter = serverAdapter;
        this.videoPath = videoPath;
        this.serverTaskListener = serverTaskListener;
        this.seekingTime = seekingTime;
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
                /*
                datagramSocket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
                datagramSocket.setReuseAddress(true);
                datagramSocket.setSoTimeout(Constants.HANDSHAKE_TIMEOUT);
                byte[] receivebuf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(receivebuf, receivebuf.length);
                datagramSocket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());
                Log.v(TAG, "Receive message : " + msg);
                */

                serverSocket = new ServerSocket(Constants.FILE_SERVICE_PORT);
                socket = serverSocket.accept();
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                String msg = dis.readUTF();

                StringTokenizer st;
                for (int i = 0; i < myDeviceInfoList.size(); i++) {
                    st = new StringTokenizer(msg, Constants.DELIMITER);
                    if (myDeviceInfoList.get(i).getWifiP2pDevice().deviceAddress.equals(st.nextToken())) {
                        myDeviceInfoList.get(i).setBrand_Name(st.nextToken());
                        myDeviceInfoList.get(i).setModel_Name(st.nextToken());
                        myDeviceInfoList.get(i).setStr_address(st.nextToken());
                        myDeviceInfoList.get(i).setPx_width(Integer.parseInt(st.nextToken()));
                        myDeviceInfoList.get(i).setPx_height(Integer.parseInt(st.nextToken()));
                        myDeviceInfoList.get(i).setDensityDpi(Integer.parseInt(st.nextToken()));
                        myDeviceInfoList.get(i).setGroupOwner(Boolean.parseBoolean(st.nextToken()));
                        myDeviceInfoList.get(i).setMm_width(Integer.parseInt(st.nextToken()));
                        myDeviceInfoList.get(i).setMm_height(Integer.parseInt(st.nextToken()));
                        Log.v(TAG, "GET ADDRESS " + myDeviceInfoList.get(i).getStr_address());
                    }
                }


                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF(Constants.HANDSHAKE_SERVER_RECEIVE);

                publishProgress();


            } catch (SocketTimeoutException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                        Log.v(TAG, "HANDSHAKE : socket closed");
                    }
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                        Log.v(TAG, "HANDSHAKE : serverSocket closed");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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

            int deviceCount = 0;

            try {
                totalStartTime = System.currentTimeMillis();
                for (DeviceInfo di : myDeviceInfoList) {
                    if (di.isHasVideo()) {
                        Toaster.get().showToast(this.myContext, di.getWifiP2pDevice().deviceName + " 영상을 이미 가지고 있습니다.", Toast.LENGTH_LONG);
                        continue;
                    }
                    if (di.isGroupOwner()) {
                        di.setVideoName(videoPath);
                        di.setHasVideo(true);
                    }
                    Log.v(TAG, di.getStr_address());
                    socket = new Socket(di.getStr_address(), Constants.WAITING_PORT);
                    while (!socket.isConnected()) {
                        socket.close();
                        socket = new Socket(di.getStr_address(), Constants.WAITING_PORT);
                        Log.v(TAG, "connecting loop");
                    }

                    Toaster.get().showToast(myContext, "Send File to " + di.getWifiP2pDevice().deviceName, Toast.LENGTH_SHORT);
                    startTime = System.currentTimeMillis();

                    sumReadByte = 0;
                    lastPublishedReadByte = 0;
                    progress = 0;

                    Log.v(TAG, "post dos open");

                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    dos.writeUTF(Constants.TRANSFER_START + Constants.DELIMITER + videoName + Constants.DELIMITER + fileSize);

                    dos.close();
                    socket.close();

                    waiting = true;
                    publishProgress();

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
                        } else if (receiverState.equals(Constants.VIDEO_ALREADY_EXIST)) {
                            Toaster.get().showToast(this.myContext, di.getWifiP2pDevice().deviceName + " 영상을 이미 가지고 있습니다.", Toast.LENGTH_LONG);
                            di.setHasVideo(true);
                            waiting = false;
                            already_exists = true;
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
                    if (di.isHasVideo()) {
                        dos.close();
                        dis.close();
                        if (!serverSocket.isClosed()) {
                            serverSocket.close();
                        }
                        if (!socket.isClosed()) {
                            socket.close();
                        }
                        di.setVideoName(videoName);
                        publishProgress();
                        continue;
                    }

                    handshaked = true;
                    publishProgress();

                    fis = new FileInputStream(myFile);

                    OutputStream os = socket.getOutputStream();
                    totalReadByte = 0;

                    sending = true;
                    boolean sending_complete = false;

                    while ((readByte = fis.read(buffer)) > 0 && !isCancelled()) {
                        os.write(buffer, 0, readByte);
                        totalReadByte += readByte;
                        sumReadByte += readByte;
                        if (sumReadByte - lastPublishedReadByte > (fileSize / 100)) {
                            progress++;
                            lastPublishedReadByte = sumReadByte;
                            publishProgress();
                        }
                    }

                    if (!isCancelled()) {
                        sending = false;

                        endTime = System.currentTimeMillis();
                        diffTime = (endTime - startTime) / 1000;
                        avgTransferSpeed = ((double) fileSize / 1000) / diffTime;

                        sended = true;
                        publishProgress();

                        Log.v(TAG, "Send " + deviceCount + " complete");
                        Log.v(TAG, "Time : " + diffTime + "(sec)");
                        Log.v(TAG, "AVG Transfer Speed : " + avgTransferSpeed + "(KB/s)");

                        String toastMessage = "#" + deviceCount + " " + di.getWifiP2pDevice().deviceName + " transfer complete";
                        Toaster.get().showToast(myContext, toastMessage, Toast.LENGTH_SHORT);

                        di.setHasVideo(true);
                        di.setVideoName(videoName);
                        Log.v(TAG, "di.setVideoName() = " + videoName);

                        deviceCount++;

                    } else {
                        break;
                    }

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


                if (!isCancelled()) {

                    totalEndTime = System.currentTimeMillis();
                    totalDiffTime = (totalEndTime - totalStartTime) / 1000;
                    totalAvgTransferSpeed = (((double) fileSize * deviceCount) / 1000) / totalDiffTime;


                    send_finished = true;
                    publishProgress();

                    Log.v(TAG, "Send to " + deviceCount + " device(s) complete");
                    Log.v(TAG, "Total Time : " + totalDiffTime + "(sec)");
                    Log.v(TAG, "Total AVG Transfer Speed : " + totalAvgTransferSpeed + "(KB/s)");
                    if (deviceCount > 0) {
                        String toastMessage = "Send to " + deviceCount + " device(s) complete";
                        Toaster.get().showToast(myContext, toastMessage, Toast.LENGTH_LONG);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : SERVER_TCP_TRANSFER_SERVICE : IOException");
                String toastMessage = ("ERROR : SERVER_TCP_TRANSFER_SERVICE : IOException");
                Toaster.get().showToast(myContext, toastMessage, Toast.LENGTH_LONG);
            } finally {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                        Log.v(TAG, "HANDSHAKE : socket closed");
                    }
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                        Log.v(TAG, "HANDSHAKE : serverSocket closed");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else if (ACT_MODE.equals(SERVER_TCP_SHOW_GUIDELINE_SERVICE)) {
            try {
                int ready_device = 0;
                for (DeviceInfo di : myDeviceInfoList) {
                    if (!di.getWifiP2pDevice().deviceAddress.equals(myDeviceInfo.getWifiP2pDevice().deviceAddress)) {
                        socket = new Socket(di.getStr_address(), Constants.WAITING_PORT);
                        while (!socket.isConnected()) {
                            socket.close();
                            socket = new Socket(di.getStr_address(), Constants.WAITING_PORT);
                            Log.v(TAG, "connecting loop");
                        }

                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                        dos.writeUTF(Constants.SHOW_GUIDELINE + Constants.DELIMITER + di.getLongString());

                        DataInputStream dis = new DataInputStream(socket.getInputStream());
                        String receiveMessage = dis.readUTF();

                        if (receiveMessage.startsWith(Constants.READY_GUIDELINE)) {
                            di.setGuidelineReady(true);
                            ready_device++;
                        }

                        dos.close();
                        socket.close();
                    } else {
                        di.setGuidelineReady(true);
                        ready_device++;
                    }
                    guideline_sended = true;
                    publishProgress();
                }
                if (ready_device == myDeviceInfoList.size()) {
                    isAllDeviceGuideLineReady = true;
                }
                publishProgress();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                        Log.v(TAG, "HANDSHAKE : socket closed");
                    }
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                        Log.v(TAG, "HANDSHAKE : serverSocket closed");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (ACT_MODE.equals(SERVER_TCP_PREPARE_PLAY_SERVICE)) {
            Log.v(TAG, "SERVER_TCP_PREPARE_PLAY_SERVICE acts");
            try {
                int prepare_device = 0;
                for (DeviceInfo di : myDeviceInfoList) {
                    if (!di.getWifiP2pDevice().deviceAddress.equals(myDeviceInfo.getWifiP2pDevice().deviceAddress)) {
                        socket = new Socket(di.getStr_address(), Constants.WAITING_PORT);
                        while (!socket.isConnected()) {
                            socket.close();
                            socket = new Socket(di.getStr_address(), Constants.WAITING_PORT);
                            Log.v(TAG, "connecting loop");
                        }

                        StringTokenizer st = new StringTokenizer(videoPath, "/");
                        String videoName = "";
                        while (st.hasMoreTokens()) {
                            videoName = st.nextToken();
                        }

                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                        dos.writeUTF(Constants.PREPARE_PLAY + Constants.DELIMITER + videoName);

                        DataInputStream dis = new DataInputStream(socket.getInputStream());
                        String receiveMessage = dis.readUTF();

                        if (receiveMessage.startsWith(Constants.PREPARE_RECEIVE)) {
                            prepare_device++;
                        }

                        dos.close();
                        socket.close();
                    } else {
                        prepare_device++;
                    }
                }

                if (prepare_device == myDeviceInfoList.size()) {
                    isAllDevicePreparePlay = true;
                }
                publishProgress();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                        Log.v(TAG, "HANDSHAKE : socket closed");
                    }
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                        Log.v(TAG, "HANDSHAKE : serverSocket closed");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (ACT_MODE.equals(SERVER_CONTROL_WAITING_SERVICE)) {
            Log.v(TAG, "ACT : SERVER_CONTROL_WAITING_SERVICE");
            datagramSocket = null;
            DatagramSocket dos = null;
            WifiManager.MulticastLock multicastLock = null;

            String file_name = "";

            try {
                WifiManager wifiManager = (WifiManager) myContext.getSystemService(Context.WIFI_SERVICE);
                multicastLock = wifiManager.createMulticastLock("n3v.junwidi");
                multicastLock.acquire();
                datagramSocket = new DatagramSocket(Constants.CONTROL_WAITING_PORT);
                datagramSocket.setReuseAddress(true);
                //datagramSocket.setSoTimeout(Constants.LONG_TIMEOUT);
                datagramSocket.setBroadcast(true);

                byte[] receivebuf;
                receivebuf = new byte[Constants.CONTROL_BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(receivebuf, receivebuf.length);
                datagramSocket.receive(packet);

                String msg = new String(packet.getData(), 0, Constants.CONTROL_BUFFER_SIZE);
                if (msg.startsWith(Constants.CONTROL_PLAY)) {
                    receivePlay = true;
                    publishProgress();
                } else if (msg.startsWith(Constants.CONTROL_PAUSE)) {
                    receivePause = true;
                    publishProgress();
                } else if (msg.startsWith(Constants.CONTROL_RESUME)) {
                    receivePlay = true;
                    publishProgress();
                } else if (msg.startsWith(Constants.CONTROL_STOP)) {
                    receiveStop = true;
                    publishProgress();
                } else if (msg.startsWith(Constants.CONTROL_SEEK)) {
                    receiveSeektime = true;
                    publishProgress();
                } else if (msg.startsWith(Constants.CONTROL_CANCEL)) {
                }
                multicastLock.release();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (datagramSocket != null && !datagramSocket.isClosed()) {
                    datagramSocket.close();
                }
            }
        } else if (ACT_MODE.equals(SERVER_CONTROL_SEND_PLAY_SERVICE)) {
            Log.v(TAG, "ACT : SERVER_CONTROL_SEND_PLAY_SERVICE");
            datagramSocket = null;
            try {
                InetAddress addr = InetAddress.getByName("192.168.49.255");
                datagramSocket = new DatagramSocket(Constants.CONTROL_SEND_PORT);
                //socket.setSoTimeout(Constants.COMMON_TIMEOUT);
                datagramSocket.setReuseAddress(true);
                datagramSocket.setBroadcast(true);
                String control_msg = Constants.CONTROL_PLAY;
                byte[] buf = control_msg.getBytes();
                Log.v(TAG, "control msg Info : " + control_msg);
                DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, Constants.CONTROL_WAITING_PORT);
                Log.v(TAG, "Send message complete");
                datagramSocket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (datagramSocket != null && !datagramSocket.isClosed()) {
                    datagramSocket.close();
                }
            }
        } else if (ACT_MODE.equals(SERVER_CONTROL_SEND_PAUSE_SERVICE)) {
            Log.v(TAG, "ACT : SERVER_CONTROL_SEND_PAUSE_SERVICE");
            datagramSocket = null;
            try {
                InetAddress addr = InetAddress.getByName("192.168.49.255");
                datagramSocket = new DatagramSocket(Constants.CONTROL_SEND_PORT);
                //socket.setSoTimeout(Constants.COMMON_TIMEOUT);
                datagramSocket.setReuseAddress(true);
                datagramSocket.setBroadcast(true);
                String control_msg = Constants.CONTROL_PAUSE;
                byte[] buf = control_msg.getBytes();
                Log.v(TAG, "control msg Info : " + control_msg);
                DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, Constants.CONTROL_WAITING_PORT);
                Log.v(TAG, "Send message complete");
                datagramSocket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (datagramSocket != null && !datagramSocket.isClosed()) {
                    datagramSocket.close();
                }
            }
        } else if (ACT_MODE.equals(SERVER_CONTROL_SEND_STOP_SERVICE)) {
            Log.v(TAG, "ACT : SERVER_CONTROL_SEND_STOP_SERVICE");
            datagramSocket = null;
            try {
                InetAddress addr = InetAddress.getByName("192.168.49.255");
                datagramSocket = new DatagramSocket(Constants.CONTROL_SEND_PORT);
                //socket.setSoTimeout(Constants.COMMON_TIMEOUT);
                datagramSocket.setReuseAddress(true);
                datagramSocket.setBroadcast(true);
                String control_msg = Constants.CONTROL_STOP;
                byte[] buf = control_msg.getBytes();
                Log.v(TAG, "control msg Info : " + control_msg);
                DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, Constants.CONTROL_WAITING_PORT);
                Log.v(TAG, "Send message complete");
                datagramSocket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (datagramSocket != null && !datagramSocket.isClosed()) {
                    datagramSocket.close();
                }
            }
        } else if (ACT_MODE.equals(SERVER_CONTROL_SEND_SEEKTIME_SERVICE)) {
            Log.v(TAG, "ACT : SERVER_CONTROL_SEND_SEEKTIME_SERVICE");
            datagramSocket = null;
            try {
                InetAddress addr = InetAddress.getByName("192.168.49.255");
                datagramSocket = new DatagramSocket(Constants.CONTROL_SEND_PORT);
                //socket.setSoTimeout(Constants.COMMON_TIMEOUT);
                datagramSocket.setReuseAddress(true);
                datagramSocket.setBroadcast(true);
                String control_msg = Constants.CONTROL_SEEK + Constants.DELIMITER + seekingTime + Constants.DELIMITER;
                byte[] buf = control_msg.getBytes();
                Log.v(TAG, "control msg Info : " + control_msg);
                DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, Constants.CONTROL_WAITING_PORT);
                Log.v(TAG, "Send message complete");
                datagramSocket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (datagramSocket != null && !datagramSocket.isClosed()) {
                    datagramSocket.close();
                }
            }
        } else if (ACT_MODE.equals(SERVER_CONTROL_CANCEL_SERVICE)) {
            Log.v(TAG, "ACT : SERVER_CONTROL_CANCEL_SERVICE");
            datagramSocket = null;
            try {
                InetAddress addr = InetAddress.getByName("192.168.49.1");
                datagramSocket = new DatagramSocket(Constants.CONTROL_SEND_PORT);
                //socket.setSoTimeout(Constants.COMMON_TIMEOUT);
                datagramSocket.setReuseAddress(true);
                datagramSocket.setBroadcast(true);
                String control_msg = Constants.CONTROL_CANCEL;
                byte[] buf = control_msg.getBytes();
                Log.v(TAG, "Handshake Info : " + control_msg);
                DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, Constants.CONTROL_WAITING_PORT);
                Log.v(TAG, "Send message complete");
                datagramSocket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (datagramSocket != null && !datagramSocket.isClosed()) {
                    datagramSocket.close();
                }
            }
        }
        return "";
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (ACT_MODE.equals(SERVER_TCP_FILE_TRANSFER_SERVICE)) {
            if (sending) {
                serverTaskListener.progressUpdate(progress);
            }
            if (waiting) {
                serverTaskListener.onWaiting();
                waiting = false;
            }
            if (handshaked) {
                serverTaskListener.onHandshaked();
                handshaked = false;
            }
            if (already_exists) {
                already_exists = false;
                serverTaskListener.onNotify();
            }
            if (sended) {
                serverTaskListener.onSendFinished();
                serverTaskListener.onNotify();
                sended = false;
            }
            if (send_finished) {
                serverTaskListener.onAllSendFinished();
                send_finished = false;
            }
        } else if (ACT_MODE.equals(SERVER_TCP_SHOW_GUIDELINE_SERVICE)) {
            if (guideline_sended && isAllDeviceGuideLineReady) {
                Log.v(TAG, "invoke onShowGuidelinSended()");
                serverTaskListener.onShowGuidelineSended();
            }
        } else if (ACT_MODE.equals(SERVER_TCP_PREPARE_PLAY_SERVICE)) {
            if (isAllDevicePreparePlay) {
                serverTaskListener.onPreparePlay();
            }
        } else if (ACT_MODE.equals(SERVER_CONTROL_WAITING_SERVICE)) {
            if (receivePlay) {
                serverTaskListener.onReceiveConPlay();
                receivePlay = false;
            } else if (receivePause) {
                serverTaskListener.onReceiveConPause();
                receivePause = false;
            } else if (receiveStop) {
                serverTaskListener.onReceiveConStop();
                receiveStop = false;
            } else if (receiveSeektime) {
                serverTaskListener.onReceiveConSeek();
                receiveSeektime = false;
            }
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (ACT_MODE.equals(SERVER_HANDSHAKE_SERVICE)) {
        }
        if (ACT_MODE.equals(SERVER_CONTROL_WAITING_SERVICE)) {
            Log.v(TAG, "SERVER_CONTROL_WAITING_SERVICE END");
        }
        serverTaskListener.onNotify();
    }

    @Override
    protected void onCancelled() {
        if (ACT_MODE.equals(SERVER_TCP_FILE_TRANSFER_SERVICE)) {
            serverTaskListener.onCancelTransfer();
        }
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
