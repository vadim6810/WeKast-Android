package com.wekast.wekastandroidclient.model;

import android.os.Build;
import android.util.Log;

import com.wekast.wekastandroidclient.activity.ListActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by ELAD on 7/27/2016.
 */
public class Server {
    private static final String TAG = "wekastClient";
    ListActivity activity;
    ServerSocket serverSocket;
    public String message = "";
    static final int socketServerPORT = 8888;
    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_DONGLE_IP = "dongleIp";

    public Server(ListActivity activity) {
        this.activity = activity;
        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.setName("AppSocketServerThread");
        socketServerThread.start();
        Log.d(TAG, "socketServerThread.getName(): thread " + socketServerThread.getName() + " started");
    }

    public int getPort() {
        return socketServerPORT;
    }

    public void onDestroy() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SocketServerThread extends Thread {
        int count = 0;

        @Override
        public void run() {


            try {
                serverSocket = new ServerSocket(socketServerPORT);
                while (true) {
                    Socket socket = serverSocket.accept();
                    count++;
                    String ipDongle = socket.getInetAddress().toString();
//                    addIpDongleToSharedPref(ipDongle);
                    message += "#" + count + " from "
                            + ipDongle + ":"
                            + socket.getPort() + "\n";

//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            activity.msg.setText(message);
//                        }
//                    });

                    SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(
                            socket, count);
                    socketServerReplyThread.setName("socketServerReplyThread");
                    socketServerReplyThread.run();
                    Log.d(TAG, "socketServerThread.getName(): thread " + socketServerReplyThread.getName() + " started");
                }
            } catch (IOException e) {
                e.printStackTrace();
                message += "Something wrong! " + e.toString() + "\n";
            }


        }
    }

    private class SocketServerReplyThread extends Thread {
        private Socket hostThreadSocket;
        int cnt;

        SocketServerReplyThread(Socket socket, int c) {
            hostThreadSocket = socket;
            cnt = c;
        }

        @Override
        public void run() {
            OutputStream outputStream;
            String msgReply = "Hello from APP, you are #" + cnt;
            try {
                outputStream = hostThreadSocket.getOutputStream();
                PrintStream printStream = new PrintStream(outputStream);
                printStream.print(msgReply);
                printStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                message += "Something wrong! " + e.toString() + "\n";
            } finally {

            }

//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    activity.msg.setText(message);
//                }
//            });
        }
    }

    public String getIpAddress() {
        String curInterface = "ap0";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            curInterface = "wlan0";
        }

        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                if(networkInterface.getName().equals(curInterface)){
                    Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                    while (enumInetAddress.hasMoreElements()) {
                        InetAddress inetAddress = enumInetAddress.nextElement();
                        if (inetAddress instanceof Inet4Address) {
                            ip += "Server running at : " + inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }
}
