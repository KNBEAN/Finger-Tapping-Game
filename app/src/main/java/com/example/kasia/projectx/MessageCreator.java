package com.example.kasia.projectx;

import java.nio.ByteBuffer;

/**
 * Created by Łukasz on 2018-03-18.
 */

public class MessageCreator {

    private static class MessageCreatorThread extends Thread {

        private int mType = -1;
        private BluetoothConnectionService mBluetoothConnectionService;
        private float mToSend;

        public MessageCreatorThread(int type, float toSend, BluetoothConnectionService bluetoothConnectionService) {
            mBluetoothConnectionService = bluetoothConnectionService;
            mType = type;
            mToSend = toSend;
        }
        @Override
        public void run() {
            switch(mType) {
                case 1:
                    mBluetoothConnectionService.write(new byte[] {1, 0, 0, 0, 0});
                    break;
                case 2:
                    mBluetoothConnectionService.write(new byte[] {2, MainActivity.LEFT_BYTE_VALUE, 0, 0, 0});
                    break;
                case 3:
                    mBluetoothConnectionService.write(new byte[] {2, MainActivity.RIGHT_BYTE_VALUE, 0, 0, 0});
                    break;
                case 4:
                    byte[] parsedData = ByteBuffer.allocate(4).putFloat(mToSend).array();
                    byte[] output = new byte[5];
                    output[0] = 3;
                    int i = 1;
                    for (byte temp : parsedData) {
                        output[i++] = temp;
                    }
                    mBluetoothConnectionService.write(output);
                    break;
            }

        }
    }

    public static void createFrequencyMessage(float frequency, BluetoothConnectionService bluetoothConnectionService) {
        new MessageCreatorThread(4, frequency, bluetoothConnectionService).start();
    }
}
