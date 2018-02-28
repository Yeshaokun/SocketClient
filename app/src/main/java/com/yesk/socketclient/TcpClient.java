package com.yesk.socketclient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by yeshaokun on 2018/1/13.
 */

public abstract class TcpClient implements Runnable {

    private String IP;
    private int port;
    boolean connect = false;
    private SocketTransceiver transceiver;
    private Socket socket;

    public void connect(String IP, int port) {
        this.IP = IP;
        this.port = port;
        new Thread(this).start();
    }

    @Override
    public void run() {

        transceiver = new SocketTransceiver() {
            @Override
            public void onReceive(String s) {
                TcpClient.this.onReceive(s);
            }

            @Override
            public void onConnectBreak() {
                TcpClient.this.onConnectBreak();
            }

            @Override
            public void onSendSuccess(String s) {
                TcpClient.this.onSendSuccess(s);
            }


        };
        try {
//            socket = new Socket(IP, port);
//            socket.setSoTimeout(1000);
            socket = new Socket();
            socket.connect(new InetSocketAddress(IP, port), 1000);
            connect = true;
//
//            try{
//                  socket.sendUrgentData(0xFF); //发送心跳包
//            }catch(Exception ex){
//                this.onConnectFalied();
//                 Log.i("ssssss", ex.getMessage().toString());
//            }

            this.onConnectSuccess();
            transceiver.start(socket);
        } catch (IOException e) {
            this.onConnectFalied();
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return connect;
    }

    public void disConnected() {
        connect = false;
        if (transceiver != null) {
            transceiver.stop();
            transceiver = null;
        }
    }

    public SocketTransceiver getTransceiver() {
        return transceiver;
    }


    public abstract void onConnectSuccess();

    public abstract void onConnectBreak();

    public abstract void onReceive(String s);

    public abstract void onConnectFalied();

    public abstract void onSendSuccess(String s);

}
