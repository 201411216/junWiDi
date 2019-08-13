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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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

    private static final String TAG = "MyServerTask";

    private Context myContext;
    private String ACT_MODE = "";
    private String host_addr = null;
    private DeviceInfo myDeviceInfo;
    private ArrayList<DeviceInfo> myDeviceInfoList;
    private ArrayAdapter<DeviceInfo> myServerAdapter;

    String videoPath = "";

    public MyServerTask(Context context, String mode, String host, DeviceInfo deviceInfo, ArrayList<DeviceInfo> deviceInfoList, ArrayAdapter serverAdapter) {
        myContext = context;
        ACT_MODE = mode;
        host_addr = host;
        myDeviceInfo = deviceInfo;
        myDeviceInfoList = deviceInfoList;
        myServerAdapter = serverAdapter;
    }

    public MyServerTask(Context context, String mode, String host, DeviceInfo deviceInfo, ArrayList<DeviceInfo> deviceInfoList, ArrayAdapter serverAdapter, String videoPath) {
        myContext = context;
        ACT_MODE = mode;
        host_addr = host;
        myDeviceInfo = deviceInfo;
        myDeviceInfoList = deviceInfoList;
        myServerAdapter = serverAdapter;
        this.videoPath = videoPath;
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
                if (socket != null) {
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
                socket.setSoTimeout(Constants.HANDSHAKE_TIMEOUT);
                byte[] receivebuf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(receivebuf, receivebuf.length);
                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());
                Log.v(TAG, "Receive message : " + msg);
                StringTokenizer st;
                for (int i = 0; i < myDeviceInfoList.size(); i++) {
                    st = new StringTokenizer(msg, "+=+");
                    if (myDeviceInfoList.get(i).getWifiP2pDevice().deviceAddress.equals(st.nextToken())) {
                        myDeviceInfoList.get(i).setStr_address(st.nextToken());
                        myDeviceInfoList.get(i).setPx_width(Integer.parseInt(st.nextToken()));
                        myDeviceInfoList.get(i).setPx_height(Integer.parseInt(st.nextToken()));
                        myDeviceInfoList.get(i).setDpi(Integer.parseInt(st.nextToken()));
                        myDeviceInfoList.get(i).setDensity(Float.parseFloat(st.nextToken()));
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
                if (socket != null) {
                    socket.close();
                }
            }
        } else if (ACT_MODE.equals(SERVER_MESSAGE_SERVICE)) {
            Log.v(TAG, "ACT : SERVER_MESSAGE_SERVICE");
            DatagramSocket socket = null;
            try {
                InetAddress addr = InetAddress.getByName("192.168.49.255");
                socket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
                //socket.setSoTimeout(Constants.COMMON_TIMEOUT);
                socket.setReuseAddress(true);
                socket.setBroadcast(true);
                String time_msg = "time_test+=+" + getStrNow() + "+=+";
                byte[] buf = time_msg.getBytes();
                Log.v(TAG, "Handshake Info : " + time_msg);
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
        } else if (ACT_MODE.equals(SERVER_FILE_TRANSFER_SERVICE)) {
            if (videoPath.equals("")) {
                Log.v(TAG, "ERROR : SERVER_FILE_TRANSFER_SERVICE : null path");
                return "";
            }
            Log.v(TAG, "ACT : SERVER_FILE_TRANSFER_SERVICE");
            DatagramSocket socket = null;
            try {
                InetAddress addr = InetAddress.getByName("192.168.49.255");
                socket = new DatagramSocket(Constants.FILE_SERVICE_PORT);
                socket.setReuseAddress(true);
                socket.setBroadcast(true);
                File myVideo = new File(videoPath);
                DatagramPacket packet;

                long file_Size = myVideo.length();
                double startTime = 0;
                double endTime = 0;
                double timeDiff = 0;
                double avgTransferSpeed = 0;
                int count = 0;

                byte[] buf;

                startTime = System.currentTimeMillis();

                StringTokenizer st = new StringTokenizer(videoPath, "/");
                String videoName = "";
                while (st.hasMoreTokens()) {
                    videoName = st.nextToken();
                }
                String startMsg = "START+=+" + videoName + "+=+" + file_Size + "+=+";
                buf = startMsg.getBytes();
                packet = new DatagramPacket(buf, buf.length, addr, Constants.FILE_SERVICE_PORT);
                socket.send(packet);

                buf = new byte[Constants.FILE_BUFFER_SIZE];

                FileInputStream fis = new FileInputStream(myVideo);
                DataInputStream dis = new DataInputStream(new BufferedInputStream(fis));

                while (true) {
                    String head = "DATA+=+" + String.format("%010d", count) + "+=+"; // 20Bytes

                    for (int i = 0; i < Constants.FILE_HEADER_SIZE; i++){
                        buf[i] = head.getBytes()[i];
                    }

                    int check = dis.read(buf, Constants.FILE_HEADER_SIZE, Constants.FILE_BUFFER_SIZE - Constants.FILE_HEADER_SIZE);
                    if (check == -1) {
                        break;
                    }

                    packet = new DatagramPacket(buf, check, addr, Constants.FILE_SERVICE_PORT);
                    socket.send(packet);
                    count++;
                    Log.v(TAG, "Count : " + count);
                }

                String endMsg = "END+=+" + videoName + "+=+" + file_Size + "+=+";
                buf = endMsg.getBytes();
                packet = new DatagramPacket(buf, buf.length, addr, Constants.FILE_SERVICE_PORT);
                socket.send(packet);

                endTime = System.currentTimeMillis();
                timeDiff = (endTime - startTime) / 1000;
                avgTransferSpeed = ((double) file_Size / 1000) / timeDiff;

                Log.v(TAG, "Total Time : " + timeDiff + "(sec)" + "\n" + "Average Transfer Speed : " + avgTransferSpeed + "(KBps)");

                String toastMsg = "";
                Toaster.get().showToast(myContext, toastMsg + "\n" + getStrNow(), Toast.LENGTH_LONG);

                dis.close();
                fis.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : SERVER_FILE_TRANSFER_SERVICE : UnknownHostException");
            } catch (SocketException e) {
                e.printStackTrace();
                Log.e(TAG, "ERROR : SERVER_FILE_TRANSFER_SERVICE : SocketException");
            } catch (FileNotFoundException e) {
                Log.e(TAG, "ERROR : SERVER_FILE_TRANSFER_SERVICE : FileNotFoundException");
            } catch (IOException e) {
                Log.e(TAG, "ERROR : SERVER_FILE_TRANSFER_SERVICE : IOException");
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        if (ACT_MODE.equals(SERVER_HANDSHAKE_SERVICE)) {
            myServerAdapter.notifyDataSetChanged();
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
