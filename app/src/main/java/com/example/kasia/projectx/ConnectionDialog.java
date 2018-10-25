package com.example.kasia.projectx;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Klasa implementująca okno dialogowe połączenia Bluetooth
 *
 * @author kfojcik
 * @version 1.0
 */

public class ConnectionDialog extends Dialog{


    /**
     * łańcuch znaków wykorzystywany do logów
     */
    private static final String TAG = "ConnectionDialog";

    /**
     * lista urządzeń Bluetooth
     */
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();

    /**
     * ListView do wyświetlania urządzeń Bluetooth
     */
    private ListView devices;

    /**
     * Adapter do Listview devices
     */
    private DeviceListAdapter mDeviceListAdapter;

    /**
     * Obiekt do kotrolowania Bluetooth
     */
    private BluetoothAdapter mBluetoothAdapter;

    /**
     * informacja o środowisku aplikacji
     */
    private Context mContext;

    /**
     * obiekt tworzący połęczenie Bluetooth (między 2 urządzeniami)
     */
    private BluetoothConnectionService mBluetoothConnection;

    /**
     * wybrane urządzenie Bluetooth
     */
    private BluetoothDevice mBTDevice;

    /**
     * łańcuch znaków - klucz, charakteryzujący połączenie dwóch urządzeń
     */
    private static final UUID  MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");


    /**
     * Konstruktor argumentowy.
     *
     * @param context Obiekt klasy Context do komunikacji ze środowiskiem, w którym została
     *                uruchomiona aplikacja
     * @param callback obiekt anonimowej klasy ActivityCallback do przekazywania danych
     *                 do głównej aktywności
     */
    public ConnectionDialog(Context context, final ActivityCallback callback) {
        super(context);
        setContentView(R.layout.connection_dialog);
        mContext = context;
        setTitle("Connection Menu");
        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        // rejestrowanie broadcastReceiver'a w systemie
        mContext.registerReceiver(mBroadcastReceiver, discoverDevicesIntent);
        Button onOffBtn = (Button) findViewById(R.id.btnOnOff);
        // Włączanie/wyłaczanie Bluetooth w urzędzeniu
        onOffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBluetoothAdapter == null){
                    Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
                }
                if(!mBluetoothAdapter.isEnabled()){
                    Log.d(TAG, "enableDisableBT: enabling BT.");
                    Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    mContext.startActivity(enableBTIntent);

                }
                if(mBluetoothAdapter.isEnabled()){
                    Log.d(TAG, "enableDisableBT: disabling BT.");
                    mBluetoothAdapter.disable();


                }
            }
        });

        Button enableDiscoverableBtn = (Button) findViewById(R.id.btnEnableDiscoverable);
        // Włączenie widoczności urządzenia Bluetooth na 300s (value)
        enableDiscoverableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");

                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                mContext.startActivity(discoverableIntent);

              // IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                mBluetoothConnection = new BluetoothConnectionService(mContext, callback);
            }
        });

        Button discoverBtn = (Button) findViewById(R.id.btnDiscover);
        // wyszukiwanie widocznych urzadzeń z włączonym BLuetooth
        discoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "btnDiscover: Looking for unpaired devices.");
                if (mDeviceListAdapter != null) {
                    mDeviceListAdapter.clear();
                }
                if(mBluetoothAdapter.isDiscovering()){
                    mBluetoothAdapter.cancelDiscovery();
                    Log.d(TAG, "btnDiscover: Canceling discovery.");

                    //check BT permissions in manifest
                    checkBTPermissions();

                    mBluetoothAdapter.startDiscovery();

                }
                if(!mBluetoothAdapter.isDiscovering()){

                    //check BT permissions in manifest
                    checkBTPermissions();

                    mBluetoothAdapter.startDiscovery();
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    mContext.registerReceiver(mBroadcastReceiver, discoverDevicesIntent);
                }
            }
        });

        Button startConnectionBtn = (Button) findViewById(R.id.btnStartConnection);
        // łączenie z wybranym urzadzeniem Bluetooth
        startConnectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBluetoothConnection.startClient(mBTDevice,MY_UUID_INSECURE);
            }
        });


        devices = (ListView) findViewById(R.id.devicesListView);
        // parowanie z wybranycm urządzeniem
        devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //first cancel discovery because its very memory intensive.
                mBluetoothAdapter.cancelDiscovery();

                Log.d(TAG, "onItemClick: You Clicked on a device.");
                String deviceName = mBTDevices.get(i).getName();
                String deviceAddress = mBTDevices.get(i).getAddress();

                Log.d(TAG, "onItemClick: deviceName = " + deviceName);
                Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

                //tworzenie połączenia
                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
                    Log.d(TAG, "Trying to pair with " + deviceName);
                    mBTDevices.get(i).createBond();

                    mBTDevice = mBTDevices.get(i);
                }
            }
        });
        // pobieranie referencji do bluetooth adaptera z systemu
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    /**
     * Implementacja BroadcastReceivera;
     * Zwraca urządzenia Bluetooth (pojedynczo!)
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            // jeśli akcja ta to ACTION_FOUND pobieramy urządzenie wraz z Intentem
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                if (!mBTDevices.contains(device)) {
                    mBTDevices.add(device);
                    Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                    mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                    devices.setAdapter(mDeviceListAdapter);
                }
            }
        }
    };

    /**
     * Metoda typu void przysłaniająca metodę dismiss() klasy Dialog
     * odrejestrowuje BroadcastReceivera wraz ze zniknięciem okna connection_dialog
     */
    @Override
    public void dismiss() {
       mContext.unregisterReceiver(mBroadcastReceiver);
        super.dismiss();
    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1){
            int permissionCheck = mContext.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += mContext.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                ((Activity)mContext).requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

}
