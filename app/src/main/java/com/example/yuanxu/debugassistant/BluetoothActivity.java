package com.example.yuanxu.debugassistant;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.bluetooth.BluetoothAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends Activity {

    //权限请求标识代码
    private final int REQUEST_CODE = 1;
    //蓝牙串口通用的UUID
    private static final UUID BLUETOOTH_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //Widget
    private Switch bluetooth_switch;
    private TextView device_name;
    private ListView device_list;

    private ListAdapter listAdapter;

    //Bluetooth
    private BluetoothAdapter local_bluetooth = null;
    private List<BluetoothDevice> bonded_device = new ArrayList<>();
    private BluetoothSocket bluetoothSocket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);    //设置布局

        initWidget();
        setListening();
    }

    private void initWidget()
    {
        bluetooth_switch = findViewById(R.id.bluetooth_switch);
        device_name = findViewById(R.id.bluetooth_text);
        device_list = findViewById(R.id.device_list);
    }

    private void setListening()
    {
        bluetooth_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked)
                {
                    if(startBluetooth())
                    {
                        discoverDevice();   //查看已配对的蓝牙设备
                    }
                }
                else
                {
                    closeBluetooth();
                }
            }
        });

        device_list.setOnItemClickListener(new OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                BluetoothDevice device = bonded_device.get(position);
                connectDevice(device.getName(), device.getAddress());
            }
        });
    }

    private boolean startBluetooth()
    {
        //没有蓝牙权限, 申请权限(蓝牙和位置信息)
        if(ContextCompat.checkSelfPermission(BluetoothActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(BluetoothActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }

        //获得蓝牙适配器
        local_bluetooth = BluetoothAdapter.getDefaultAdapter();

        if(local_bluetooth == null)
        {
            Toast.makeText(BluetoothActivity.this, "获取蓝牙失败,可能该设备不支持蓝牙", Toast.LENGTH_LONG).show();
            return false;
        }

        //打开蓝牙
        if(!local_bluetooth.isEnabled())
        {
            if(!local_bluetooth.enable())
            {
                Toast.makeText(BluetoothActivity.this, "打开蓝牙失败, 请打开蓝牙后重试", Toast.LENGTH_LONG).show();
                bluetooth_switch.setChecked(false); //自动关闭开关
                return false;
            }
        }
        return true;
    }

    private void discoverDevice()
    {
        Log.i("BlueActivity", "扫描已绑定的蓝牙");
        //获得本地蓝牙已绑定的蓝牙设备
        Set<BluetoothDevice> devices = local_bluetooth.getBondedDevices();
        if(devices.isEmpty())
        {
            Toast.makeText(BluetoothActivity.this, "没有已配对的蓝牙设备", Toast.LENGTH_LONG).show();
            return;
        }

        //初始化device_list
        bonded_device.addAll(devices);
        listAdapter = new ListAdapter(BluetoothActivity.this, R.layout.list_view_content, bonded_device);
        device_list.setAdapter(listAdapter);
    }

    private void connectDevice(String name, String address)
    {
        GlobalBlueSocket globalBlueSocket = (GlobalBlueSocket)getApplication();
        bluetoothSocket = globalBlueSocket.getGlobalBluetoothSocket();

        //获得远程蓝牙设备
        BluetoothDevice  device = local_bluetooth.getRemoteDevice(address);

        try {
            //建立Socket连接
            bluetoothSocket = device.createRfcommSocketToServiceRecord(BLUETOOTH_UUID);
        }
        catch (IOException e)
        {
            Toast.makeText(BluetoothActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
            globalBlueSocket.setGlobalBluetoothSocket(null);
        }
        //开启服务
        if (bluetoothSocket != null)
        {
            Intent intent = new Intent(BluetoothActivity.this, BluetoothService.class);
            startService(intent);
            //无法保证线程确实开启,待改进
            device_name.setText(name);
            Toast.makeText(BluetoothActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(BluetoothActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void closeBluetooth()
    {
        device_name.setText("");
        bonded_device.clear();
        //清空ListView适配器
        listAdapter.clear();
        listAdapter.notifyDataSetChanged();
        local_bluetooth = null;

        //关闭蓝牙Socket
        if (bluetoothSocket != null) {
            try {
                if (bluetoothSocket.isConnected()) {
                    bluetoothSocket.close();
                    bluetoothSocket = null;
                }
            } catch (IOException e) {
                Log.i("Bluetooth Socket", "断开蓝牙Socket错误");
            }
        }
        Intent intent = new Intent(BluetoothActivity.this, BluetoothService.class);
        stopService(intent);

        Toast.makeText(BluetoothActivity.this, "已断开连接", Toast.LENGTH_SHORT).show();
    }

    //重写权限结果返回函数
    @Override
    public void onRequestPermissionsResult
    (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean granted = true;
        //查看申请权限结果
        if(requestCode == REQUEST_CODE)
        {
            for(int i: grantResults)
            {
                if(i != PackageManager.PERMISSION_GRANTED)
                {
                    granted = false;
                }
            }
        }
        //申请失败
        if(!granted)
        {
            Toast.makeText(BluetoothActivity.this, "申请蓝牙权限失败", Toast.LENGTH_SHORT).show();
        }
    }
}

