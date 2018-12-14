package com.example.yuanxu.debugassistant;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.Arrays;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BluetoothService extends Service
{
    //输入输出流
    private static boolean run_state = false;
    private BluetoothSocket bluetoothSocket;
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

                BufferedInputStream bufferedInputStream = null; //Socket 读取缓冲流

                try {
                     bufferedInputStream = new BufferedInputStream(bluetoothSocket.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                byte[] read_byte = new byte[1024]; //用于读取缓冲流的字节数组

                //蓝牙串口Socket一直连接
                while (bluetoothSocket != null && bluetoothSocket.isConnected())
                {
                    //接收信息
                    try {
                        if (bufferedInputStream != null && waitForReadyRead(bufferedInputStream, 100))
                        {
                            int a = bufferedInputStream.read(read_byte);
                            if(a >0)
                            {
                                StringBuilder str = new StringBuilder(new String(read_byte));
                                Arrays.fill(read_byte, (byte) 0);
                                while (waitForReadyRead(bufferedInputStream, 5))
                                {
                                    if(bufferedInputStream.read(read_byte) > 0) {
                                        str.append(new String(read_byte));
                                        Arrays.fill(read_byte, (byte) 0);
                                    }
                                }
                                sendContentBroadcast(str.toString());
                                String s = str.toString();
                                System.out.print(s);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    if (bufferedInputStream != null) {
                        bufferedInputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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
        if(bluetoothSocket != null && bluetoothSocket.isConnected()){

            synchronized(this) {
                try {
                    OutputStream outputStream = bluetoothSocket.getOutputStream();
                    outputStream.write(s.getBytes());
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean waitForReadyRead(BufferedInputStream bis, int timeout)
    {
        int count =0;
        try {
            while(bis.available() == 0 && count < timeout)
            {
                Thread.sleep(10);
                count++;
            }

            /* 缓冲区内有数据,则返回true
             * 缓冲区内没有数据,则返回false
             */
            return count < timeout;

        }catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return false;
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
