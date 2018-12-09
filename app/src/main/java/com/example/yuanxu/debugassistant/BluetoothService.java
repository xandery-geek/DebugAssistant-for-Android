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
    private static boolean run_state = false;
    private BluetoothSocket bluetoothSocket;
    private String send_string = "";
    private Thread thread;
    private static String device_name = null;

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

                if(bluetoothSocket != null && bluetoothSocket.isConnected())
                {
                    run_state = true;
                }
                else
                {
                    run_state = false;
                    return;
                }

                InputStream inputStream;
                byte[] read_byte = new byte[1024];

                //蓝牙串口Socket一直连接
                while (bluetoothSocket != null && bluetoothSocket.isConnected())
                {
                    if (!send_string.isEmpty())
                    {
                        synchronized (this) {
                            //发送信息, 加锁
                            try {
                                OutputStream outputStream = bluetoothSocket.getOutputStream();
                                outputStream.write(send_string.getBytes());
                                outputStream.flush();
                                send_string = "";   //清空
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    //接收信息
                    try {
                        inputStream = bluetoothSocket.getInputStream();
                        if(inputStream.read(read_byte) > 0) {
                            String str = new String(read_byte); //字节数组转字符串
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
        //开启线程
        thread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //关闭Socket
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        bluetoothSocket = null;
        device_name = null;
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
        intent.setAction("ServiceBoardCast");
        intent.putExtra("info", s);
        sendBroadcast(intent);
    }

    //静态方法
    public static boolean isRunning()
    {
        return run_state;
    }

    public static String getName(){
        return device_name;
    }

    public static void setName(String name){
        device_name = name;
    }
}
