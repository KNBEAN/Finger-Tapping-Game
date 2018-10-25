package com.example.kasia.projectx;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;


public class BluetoothConnectionService {

    public enum ConnectionStatus{
        CONNECTED,
        DISCONNECTED
    }

    private static final String TAG = "BCS";
    private static final String APP_NAME = "MYAPP";

    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter mBluetoothAdapter;
    private Context mContext;

    private AcceptThread mInsecureAcceptThread;

    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID mDeviceUUID;
    private ProgressDialog mProgressDialog;
    private ActivityCallback mCallback;

    private ConnectedThread mConnectedThread;

    public  BluetoothConnectionService(Context context, ActivityCallback callback) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mCallback = callback;
        start();
    }


    /**
     Ten wątek jest uruchamiany podczas nasłuchiwania połączeń przychodzących.
     Działa, dopóki połączenie nie zostanie zaakceptowane (lub do momentu anulowania).
     */
    private class AcceptThread extends Thread {

        /**
         * Gniazdo lokalnego serwera
         */
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            /**
             * Tworzenie nowego nasłuchującego gniazda serwera
             */
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }

            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;

            try {
                /**
                 * Wyywołanie blokujące; zostanie zwrócone tylko w przypadku
                 * udanego połączenia lub wyjątku
                 */

                Log.d(TAG, "run: RFCOM server socket start.....");

                socket = mmServerSocket.accept();

                Log.d(TAG, "run: RFCOM server socket accepted connection.");

            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }

            if (socket != null) {
                connected(socket, mmDevice);
            }

            Log.i(TAG, "END mAcceptThread ");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage());
            }
        }

    }

    /**
     Ten wątek jest uruchamiany podczas próby nawiązania połączenia wychodzącego z urządzenia.
     Biegnie prosto; połączenie się udaje lub nie.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            mDeviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread ");

            /**
             * Uzyskanie gniazda Bluetooth dla połączenia z danym urządzeniem Bluetooth
             */
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        + MY_UUID_INSECURE);
                tmp = mmDevice.createRfcommSocketToServiceRecord(mDeviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            mmSocket = tmp;

            /**
             * Anulowanie wykrywania, ponieważ spowolni to połączenie
             */
            mBluetoothAdapter.cancelDiscovery();

            /**
             * Nawiązanie połączenia z gniazdem Bluetooth
             */

            try {
                /**
                 * Jest to wywołanie blokujące i zostanie zwrócone tylko w przypadku udanego połączenia lub wyjątku
                 */
                mmSocket.connect();

                Log.d(TAG, "run: ConnectThread connected.");
            } catch (IOException e) {
                /**
                 * Zamknięcie gniazda
                 */
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE);
            }


            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }


    /**
     * Uruchamianie "usługi". W szczególności uruchamianie AcceptThread, aby rozpocząć sesję
     * w trybie nasłuchu (serwer). Wywoływane przez działanie onResume()
     **/
    public synchronized void start() {
        Log.d(TAG, "start");

        /**
         Anulowanie dowolnego wątku próbującego nawiązać połączenie
         */
        //
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    /**
     * Uruchomienie AcceptThread i czekanie na połączenie.
     * Następnie uruchomienie ConnectThread i próba nawiązania połączenia
     * z innymi urządzeniami AcceptThread.
     **/

    public void startClient(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startClient: Started.");

        /**
         * okno dialogowe postępu inicjalizacji
         **/
        mProgressDialog = ProgressDialog.show(mContext, "Connecting Bluetooth"
                , "Please Wait...", true);

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    /**
     * ConnectedThread odpowiedzialny jest za utrzymywanie połączenia Bluetooth,
     * wysyłanie danych i odbieranie przychodzących danych odpowiednio przez
     * strumienie wejściowe / wyjściowe.
     **/
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            /**
             * odrzucanie dialogu postępu po ustanowieniu połączenia
             **/
            try {
                mProgressDialog.dismiss();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }


            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[5];  // buffer store for the stream

            /**
             * bajty zwrócone z read()
             **/
            int bytes;

            /**
             * Nasłuchiwanie strumienia wejściowego (InputStream) do momentu wystąpienia wyjątku
             **/
            while (true) {
                /**
                 * Czytanie ze strumienia wejściowego
                 **/
                try {
                    bytes = mmInStream.read(buffer);
                    Log.d(TAG, "Bytes received " + bytes +" bytes: " +buffer);
                    mCallback.setReceivedBytes(buffer); // wysyłanie bajtów do callbacka w main activity
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage());
                    mCallback.setConnectionStatus(ConnectionStatus.DISCONNECTED);
                    break;
                }
            }
        }
        /**
         * Wywołanie z głównej aktywności, aby wysłać dane do zdalnego urządzenia
         **/
        public void write(byte[] bytes) {
            Log.d(TAG, "write: Writing to outputstream: " + bytes);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage());
                mCallback.setConnectionStatus(ConnectionStatus.DISCONNECTED);
            }
        }

        /**
         * Wywołanie z głównej aktywności, aby zamknąć połączenie
         **/
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: Starting.");
        mCallback.setConnectionStatus(ConnectionStatus.CONNECTED);
        mCallback.setBlueToothConnectionInstance(this);
        mCallback.dismissConnectionDialog();
        /**
         * Uruchamianie wątku, aby zarządzać połączeniem i wykonywać transmisje
         **/
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        Log.d(TAG, "write: Write Called.");
        /**
         * wykonywanie zapisu
         **/
        mConnectedThread.write(out);
    }

}


