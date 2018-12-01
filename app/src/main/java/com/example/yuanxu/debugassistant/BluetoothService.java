package com.example.yuanxu.debugassistant;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothService extends Service
{
    //输入输出流
    private boolean run_state = true;
    private BluetoothSocket bluetoothSocket;
    private String send_string;
    private Thread thread;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        GlobalBlueSocket globalBlueSocket = (GlobalBlueSocket)getApplication();
        bluetoothSocket = globalBlueSocket.getGlobalBluetoothSocket();

        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                if(bluetoothSocket != null)
                {
                    try {
                        bluetoothSocket.connect();
                        run_state = true;
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    run_state = false;
                    return;
                }

                //蓝牙串口Socket一直连接
                while (bluetoothSocket != null)
                {
                    synchronized (this) {

                        if (!send_string.isEmpty()) {
                            //发送信息, 加锁
                            try {
                                OutputStream outputStream = bluetoothSocket.getOutputStream();
                                outputStream.write(send_string.getBytes());
                                outputStream.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    //接收信息
                    try {

                        InputStream inputStream = bluetoothSocket.getInputStream();
                        String str = inputStream.toString();
                        if(!str.isEmpty())
                        {
                            sendContentBroadcast(str);
                        }
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                run_state = false;
            }
        });
        thread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bluetoothSocket = null;
        try {
            thread.join();  //等待线程退出
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        return new LocalBinder();
    }

    public boolean isRunning()
    {
        return run_state;
    }

    public void setSendStream(String s)
    {
        synchronized(this) {
            send_string = s;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public final class LocalBinder extends Binder
    {
        public BluetoothService getService()
        {
            return BluetoothService.this;
        }
    }

    //发送广播
    private void sendContentBroadcast(String s)
    {
        Intent intent = new Intent();
        intent.putExtra("info", s);
        sendBroadcast(intent);
    }
}

