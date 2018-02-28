package com.yesk.socketclient;


import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by yeshaokun on 2018/1/13.
 */

public abstract class SocketTransceiver implements Runnable {

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean runFlag = false;
    private BufferedReader br;
    private PrintWriter pw;
    private String msg;

    public void start(Socket socket) {
        this.socket = socket;
        runFlag = true;
        new Thread(this).start();
    }

    public void stop() {
        runFlag = false;

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void run() {
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            runFlag = false;
            e.printStackTrace();
        }


        while (runFlag) {
            br = new BufferedReader(new InputStreamReader(inputStream));
            char[] buf = new char[1024];
            int len = 2;
            try {
                while ((len = br.read(buf)) > 0) { //单个读取计数，直到结束返回-1
                    this.onReceive(String.valueOf(buf));
                    Log.i("sssssss", len + ":" + String.valueOf(buf));

                }
//                String content;
//                while ((content = br.readLine()) !=null) {
//                    // 每当读到来自服务器的数据之后，发送消息通知程序界面显示该数据，使用此循环读取数据时如果后台发的数据没有\n等就会读取失败
//                    this.onReceive(content);
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    Thread oneThread;

    /**
     * 封装内部发送数据的处理方式，供外部调用
     * @param s
     */
    public void sendMSG(String s) {
        msg = s;
        //耗时操作必须放在子线程里，继承Thread的线程只能被启动一次，重复启动会报错
        oneThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (outputStream != null) {
                    try {
                        pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream)));
                        pw.println(msg);// 发送参数
                        pw.flush();
                        //调用发送成功
                        onSendSuccess(msg);
                    } catch (Exception e) {
                        //出现异常时，调用连接失败
                        onConnectBreak();
                        runFlag = false;
                        e.printStackTrace();
                        Log.e("11111", "sendMSG: 异常:" + e.toString());
                    }
                }
            }
        });
        //启动线程
        oneThread.start();


    }

    public abstract void onReceive(String s);

    public abstract void onConnectBreak();

    public abstract void onSendSuccess(String s);

}

