package com.fyp.cityulogin.util;



import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.AdvertiseData;
import android.os.ParcelUuid;
import android.util.Log;
import android.bluetooth.BluetoothGattCharacteristic;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BluetoothUtil {
    private static final String TAG = "BluetoothUtil.java";

    public static AdvertiseData createScanAdvertiseData() {
        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.setIncludeDeviceName(true);
//        byte[] serverData = new byte[5];
//        ByteBuffer bb = ByteBuffer.wrap(serverData);
//        bb.order(ByteOrder.BIG_ENDIAN);
//        bb.put((byte) 0x02);
//        bb.put((byte) 0x15);
//        bb.putShort(major);
//        bb.putShort(minor);
//        bb.put(txPower);

        builder.addServiceUuid(ParcelUuid.fromString(BluetoothUUID.BLE_USER_DATA.toString()));

        builder.setIncludeTxPowerLevel(true);
        AdvertiseData adv = builder.build();
        return adv;
    }


    public static AdvertiseData createIBeaconAdvertiseData(UUID proximityUuid, short major, short minor, byte txPower) {
        if (proximityUuid == null) {
            throw new IllegalArgumentException("createIBeaconAdvertiseData - proximityUuid null");
        }
        /*
             Byte 0:        Length :  0x02
             Byte 1:        Type: 0x01 (Flags)
             Byte 2:        Value: 0x06 (Typical Flags)
             Byte 3:        Length: 0x1a
             Byte 4:        Type: 0xff (Custom Manufacturer Packet)
             Byte 5-6:      Manufacturer ID : 0x4c00 (Apple)
             Byte 7:        SubType: 0x2 (iBeacon)
             Byte 8:        SubType Length: 0x15
             Byte 9-24:     Proximity UUID
             Byte 25-26:    Major
             Byte 27-28:    Minor
             Byte 29:       Signal Power
        */
        // UUID to byte[]
        String[] uuidstr = proximityUuid.toString().replaceAll("-", "").toLowerCase().split("");
        byte[] uuidBytes = new byte[16];
        for (int i = 0, x = 0; i < uuidstr.length - 1; x++) {
            uuidBytes[x] = (byte) ((Integer.parseInt(uuidstr[i++], 16) << 4) | Integer.parseInt(uuidstr[i++], 16));
        }
        byte[] majorBytes = {(byte) (major >> 8), (byte) (major & 0xff)};
        byte[] minorBytes = {(byte) (minor >> 8), (byte) (minor & 0xff)};
        byte[] mPowerBytes = {txPower};
        byte[] manufacturerData = new byte[0x17];
        byte[] flagibeacon = {0x02, 0x15};

        System.arraycopy(flagibeacon, 0x0, manufacturerData, 0x0, 0x2);
        System.arraycopy(uuidBytes, 0x0, manufacturerData, 0x2, 0x10);
        System.arraycopy(majorBytes, 0x0, manufacturerData, 0x12, 0x2);
        System.arraycopy(minorBytes, 0x0, manufacturerData, 0x14, 0x2);
        System.arraycopy(mPowerBytes, 0x0, manufacturerData, 0x16, 0x1);

        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.addManufacturerData(0x004c, manufacturerData);

        AdvertiseData adv = builder.build();
        return adv;
    }

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
