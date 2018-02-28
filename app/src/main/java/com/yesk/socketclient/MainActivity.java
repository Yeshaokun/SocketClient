package com.yesk.socketclient;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.listViewData)
    ListView listViewData;
    @BindView(R.id.ip)
    EditText ip;
    @BindView(R.id.port)
    EditText port;
    @BindView(R.id.connect)
    Button connect;
    @BindView(R.id.edit_ssid)
    EditText editSsid;
    @BindView(R.id.edit_pwd)
    EditText editPwd;
    @BindView(R.id.iv_showPassword)
    ImageView ivShowPassword;
    @BindView(R.id.send)
    Button send;
    @BindView(R.id.edit_ip)
    EditText editIp;
    @BindView(R.id.edit_port)
    EditText editPort;
    @BindView(R.id.setSendServer)
    Button setSendServer;
    @BindView(R.id.setTCP)
    Button setTCP;
    @BindView(R.id.setUDP)
    Button setUDP;
    @BindView(R.id.setOk)
    Button setOk;
    @BindView(R.id.editText2)
    EditText editText2;

    private Context mContext;
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        mContext = this;
        ButterKnife.bind(this);
        initEvent();
    }

    @Override
    protected void onStop() {
        tcpClient.disConnected();
        super.onStop();
    }

    private Boolean showPassword = true;//是否显示密码

    @OnClick({R.id.connect, R.id.send, R.id.iv_showPassword, R.id.setSendServer, R.id.setTCP, R.id.setUDP, R.id.setOk})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.connect:
                if (tcpClient.isConnected()) {
                    tcpClient.disConnected();
                    ShowToast("断开连接！");
                } else {
                    String IP = ip.getText().toString().trim();
                    String strPort = port.getText().toString();
                    int port = Integer.parseInt(strPort);
                    tcpClient.connect(IP, port);
                }
                break;
            case R.id.send:
                if (tcpClient.isConnected()) {
                    StringBuffer msg = new StringBuffer("");
                    msg.append("AT+J=");
                    msg.append("\"");
                    msg.append(editSsid.getText().toString().trim());
                    msg.append("\",\"");
                    msg.append(editPwd.getText().toString().trim());
                    msg.append("\"");
//                    SendMessageThread thread = new SendMessageThread();
//                    thread.SendMessage(tcpClient.getTransceiver(),msg.toString());
                    tcpClient.getTransceiver().sendMSG(msg.toString());
                } else {
                    ShowToast("请连接");
                }

                break;
            case R.id.iv_showPassword:
                if (showPassword) {// 显示密码
                    ivShowPassword.setImageResource(R.drawable.login_see);
                    editPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    editPwd.setSelection(editPwd.getText().toString().length());
                    showPassword = !showPassword;
                } else {// 隐藏密码
                    ivShowPassword.setImageResource(R.drawable.login_hide);
                    editPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    editPwd.setSelection(editPwd.getText().toString().length());
                    showPassword = !showPassword;
                }
                break;
            case R.id.setSendServer:
                if (tcpClient.isConnected()) {
                    StringBuffer msg = new StringBuffer("");
                    msg.append("AT+I=");
                    msg.append("\"");
                    msg.append(editIp.getText().toString().trim());
                    msg.append("\",\"");
                    msg.append(editPort.getText().toString().trim());
                    msg.append("\"");
//                    SendMessageThread thread = new SendMessageThread();
//                    thread.SendMessage(tcpClient.getTransceiver(),msg.toString());
                    tcpClient.getTransceiver().sendMSG(msg.toString());
                } else {
                    ShowToast("请连接");
                }

                break;
            case R.id.setTCP:
                tcpClient.getTransceiver().sendMSG("AT+T=T");
                break;
            case R.id.setUDP:
                tcpClient.getTransceiver().sendMSG("AT+T=U");
                break;
            case R.id.setOk:
                tcpClient.getTransceiver().sendMSG("AT+R");
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("提示！");
                builder.setMessage("配置完成,将断开连接！");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        boolean enable = false;
                        connectView(enable);
                        tcpClient.disConnected();
                        ShowToast("断开连接！");

                    }
                });
                builder.create().show();
                break;
        }
    }

    private TcpClient tcpClient = new TcpClient() {
        @Override
        public void onConnectSuccess() {
            handler.sendEmptyMessage(0);
        }

        @Override
        public void onConnectBreak() {
            handler.sendEmptyMessage(1);

        }

        @Override
        public void onReceive(String s) {
            Message msg = new Message();
            msg.what = 4;
            if (s.contains("AT+J=OK")) {
                msg.obj = "WIFI配置成功";
            } else if (s.contains("AT+T=OK")){
                msg.obj = "服务器配置成功";
            } else if (s.contains("AT+I=OK")){
                msg.obj = "协议切换成功";
            } else{
                msg.obj = "配置失败！";
            }
            handler.sendMessage(msg);
        }

        @Override
        public void onConnectFalied() {
            handler.sendEmptyMessage(3);
        }

        @Override
        public void onSendSuccess(String s) {
            handler.sendEmptyMessage(5);
        }
    };
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            listAdapter.add("我：" + msg.obj.toString());
//            listViewData.setSelection(listAdapter.getCount() - 1);
            switch (msg.what) {
                case 0:
                    ShowToast("连接成功！");
                    //可点击
                    boolean enable = true;
                    connectView(enable);
                    break;
                case 1:
                    ShowToast("断开连接！");
                    break;
                case 2:
                    ShowToast("发送成功！");
                    break;
                case 3:
                    ShowToast("连接失败！");
                    break;
                case 4:
                    ShowToast(msg.obj.toString());
                    break;
                case 5:
                    ShowToast("正在发送！");
                    break;
                default:
                    break;
            }
        }
    };
    Toast toast;

    private void ShowToast(String str) {
        if (null != toast) {
            toast.setText(str);
        } else {
            toast = Toast.makeText(mContext, str, Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    /**
     * 连接状态改变View
     * @param enable
     */
    private void connectView(boolean enable) {
        setOk.setEnabled(enable);
        setSendServer.setEnabled(enable);
        send.setEnabled(enable);
        setTCP.setEnabled(enable);
        setUDP.setEnabled(enable);

        editIp.setEnabled(enable);
        editPort.setEnabled(enable);
        editSsid.setEnabled(enable);
        editPwd.setEnabled(enable);

        ip.setEnabled(!enable);
        port.setEnabled(!enable);
    }
    /**
     * 初始化事件
     */
    private void initEvent() {

        editIp.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                Pattern pattern = Pattern.compile("^(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[0-9]{1,2})(\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[0-9]{1,2})){3}$");
                Matcher matcher = pattern.matcher(editIp.getText().toString());
                if (matcher.matches()) {

                } else {
                    editIp.setError("IP地址格式不正确");
                }

            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }
        });
        editPort.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
//                Pattern pattern = Pattern.compile("\b(([01]?\d?\d|2[0-4]\d|25[0-5])\.){3}([01]?\d?\d|2[0-4]\d|25[0-5])\b");
//                Matcher matcher = pattern.matcher(mEmailView.getText().toString());
//                if (matcher.matches()&&mPasswordView.getText().length()>6) {
//                    rlButton.setClickable(true);
//                    rlButton.setBackgroundResource(R.drawable.btn_item_bule_selector);
//                } else {
//                    rlButton.setClickable(false);
//                    rlButton.setBackgroundResource(R.drawable.corners_bg_gray);
//                }
                //不合法时给与提示
                if (editPort.getText().length()>0&&Integer.parseInt(editPort.getText().toString())>65535) {
                    editPort.setError("端口号最大为65535");
                }
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }
        });

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long secondTime = System.currentTimeMillis();

            if (secondTime - firstTime > 2000) {                                         //如果两次按键时间间隔大于2秒，则不退出
                firstTime = secondTime;//更新firstTime
                return true;
            } else {                                                    //两次按键小于2秒时，退出应用
                finish();
            }
        }
        return true;
    }
    long firstTime = 0;
}
