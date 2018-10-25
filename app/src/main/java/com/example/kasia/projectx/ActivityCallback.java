package com.example.kasia.projectx;

/**
 * Interfejs zawierający metody potrzebne do komunikacji z główną aktywnością
 *
 * @author kfojcik
 * @version 1.0
 */

public interface ActivityCallback {

    /**
     * Metoda ustawiająca bajty;
     * @param data tablica bajtów
     */
    void setReceivedBytes(byte[] data);

    /**
     * Metoda ustawiająca instancję BluetoothConnectionService
     * @param instance obiekt klasy BluetoothConnectionService
     */
    void setBlueToothConnectionInstance(BluetoothConnectionService instance);

    /**
     * Metoda zamykjąca okno dialogowe
     */
    void dismissConnectionDialog();

    /**
     * Metoda ustawiająca status połączenia
     * @param status enum z możliwymi statusami połączenia
     */
    void setConnectionStatus(BluetoothConnectionService.ConnectionStatus status);
}
