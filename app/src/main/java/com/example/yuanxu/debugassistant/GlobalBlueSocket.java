package com.example.yuanxu.debugassistant;

import android.app.Application;
import android.bluetooth.BluetoothSocket;

public class GlobalBlueSocket extends Application {

    BluetoothSocket globalBluetoothSocket = null;

    public void setGlobalBluetoothSocket(BluetoothSocket bluetoothSocket)
    {
        globalBluetoothSocket = bluetoothSocket;
    }

    public BluetoothSocket getGlobalBluetoothSocket()
    {
        return globalBluetoothSocket;
    }
}

