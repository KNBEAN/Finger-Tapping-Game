package com.example.kasia.projectx;


import android.bluetooth.BluetoothDevice;
        import android.content.Context;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.TextView;
        import java.util.ArrayList;

/**
 * Created by kfojc on 28.02.2018.
 */

/**
 * Klasa implementująca adapter do Listview, wyświetlający nazwę i MAC adres.
 *
 * @author kfojcik
 * @version 1.0
 */


public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    /**
     * zmienna używana do stworzenia widoku
     */
    private LayoutInflater mLayoutInflater;

    /**
     * lista urządzeń Bluetooth
     */
    private ArrayList<BluetoothDevice> mDevices;

    /**
     * id zasobów
     */
    private int  mViewResourceId;

    /**
     * Konstruktor argumentowy.
     *
     * @param context Obiekt klasy Context do komunikacji ze środowiskiem, w którym została
     *                uruchomiona aplikacja
     * @param tvResourceId id zasobów
     * @param devices lista urządzeń Bluetooth
     */
    public DeviceListAdapter(Context context, int tvResourceId, ArrayList<BluetoothDevice> devices){
        super(context, tvResourceId,devices);
        this.mDevices = devices;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = tvResourceId;
    }

    /**
     * Metoda przysłaniająca metodę getView klasy Adapter;
     * zwracająca widok poszczegółnych urządzeń na liście - nazwa, MAC adres
     * @param position pozycja urzadzenia na liscie
     * @param convertView widok
     * @param parent grupa widoków, do którego przywiązany jest widok
     * @return widok po dodaniu danego urządzenia
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mLayoutInflater.inflate(mViewResourceId, null);

        BluetoothDevice device = mDevices.get(position);

        if (device != null) {
            TextView deviceName = (TextView) convertView.findViewById(R.id.tvDeviceName);
            TextView deviceAdress = (TextView) convertView.findViewById(R.id.tvDeviceAddress);

            if (deviceName != null) {
                deviceName.setText(device.getName());
            }
            if (deviceAdress != null) {
                deviceAdress.setText(device.getAddress());
            }
        }

        return convertView;
    }

}