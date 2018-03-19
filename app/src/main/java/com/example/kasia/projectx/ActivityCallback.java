package com.example.kasia.projectx;


/**
 * Created by Łukasz on 2018-03-03.
 */

public interface ActivityCallback {
    void setReceivedBytes(byte[] data);
    void setBlueToothConnectionInstance(BluetoothConnectionService instance);
    void dismissConnectionDialog();
    void setConnectionStatus(BluetoothConnectionService.ConnectionStatus status);
}
