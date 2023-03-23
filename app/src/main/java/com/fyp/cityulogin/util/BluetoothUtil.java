package com.fyp.cityulogin.util;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.AdvertiseData;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.UUID;

public class BluetoothUtil {
    private static final String TAG = "BluetoothUtil.java";



    public static AdvertiseData createAdvertiseData(UUID proximityUuid) {
        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.setIncludeDeviceName(true);
    //        builder.addManufacturerData(0x01AC, new byte[]{0x34, 0x56});
        builder.addServiceUuid(ParcelUuid.fromString(proximityUuid.toString()));

        AdvertiseData adv = builder.build();
        Log.i(TAG, adv.toString());
        return adv;
    }

    public static BluetoothGattService createGattTable(@NonNull String account, @NonNull String password) {
//        BluetoothGattDescriptor accountDescriptor =
//                new BluetoothGattDescriptor(BluetoothUUID.DESC_ACCOUNT, BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor.PERMISSION_READ);
//        BluetoothGattDescriptor passwordDescriptor =
//                new BluetoothGattDescriptor(BluetoothUUID.DESC_PASSWORD, BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor.PERMISSION_READ);
        // get eid
        BluetoothGattCharacteristic eidCharacteristic =
                new BluetoothGattCharacteristic(BluetoothUUID.CHAR_EID
                        , BluetoothGattCharacteristic.PROPERTY_READ
                        , BluetoothGattCharacteristic.PERMISSION_READ);
        eidCharacteristic.setValue(account);
        // get password
        BluetoothGattCharacteristic pwdCharacteristic =
                new BluetoothGattCharacteristic(BluetoothUUID.CHAR_PASSWORD
                        , BluetoothGattCharacteristic.PROPERTY_READ
                        , BluetoothGattCharacteristic.PERMISSION_READ);
        pwdCharacteristic.setValue(password);

        BluetoothGattService loginService =
                new BluetoothGattService(BluetoothUUID.BLE_USER_DATA, BluetoothGattService.SERVICE_TYPE_PRIMARY);

//        accountDescriptor.setValue(account.getBytes(StandardCharsets.UTF_8));
//        passwordDescriptor.setValue(password.getBytes(StandardCharsets.UTF_8));
//        eidCharacteristic.addDescriptor(accountDescriptor);
//        eidCharacteristic.addDescriptor(passwordDescriptor);
        loginService.addCharacteristic(eidCharacteristic);
        loginService.addCharacteristic(pwdCharacteristic);

        Log.i(TAG, "GattService Finished.");
        return loginService;
    }
}
