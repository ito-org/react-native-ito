package com.reactlibrary.service;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

/**
 * Implementation of the Bluetooth GATT Time Profile.
 * https://www.bluetooth.com/specifications/adopted-specifications
 */
public class BandemicProfile {
    private static final String TAG = BandemicProfile.class.getSimpleName();

    /* Current Bandemic Service UUID */
    public static UUID BANDEMIC_SERVICE = UUID.fromString("cc84f3ec-7d80-4a58-b15e-9192adf7d6e4");
    /* Mandatory Hash of UUID Characteristic */
    public static UUID HASH_OF_UUID    = UUID.fromString("db10ab98-107c-4b7b-a9c5-26fe559d82b4");

    /**
     * Return a configured {@link BluetoothGattService} instance for the
     * Current Time Service.
     */
    public static BluetoothGattService createBandemicService() {
        BluetoothGattService service = new BluetoothGattService(BANDEMIC_SERVICE,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic hashOfUUID = createHashOfUUIDCharacteristic();
        service.addCharacteristic(hashOfUUID);

        return service;
    }

    public static BluetoothGattCharacteristic createHashOfUUIDCharacteristic() {
        return new BluetoothGattCharacteristic(HASH_OF_UUID,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);
    }

}
