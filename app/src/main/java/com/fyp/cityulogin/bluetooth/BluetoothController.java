package com.fyp.cityulogin.bluetooth;

import static com.fyp.cityulogin.util.BluetoothUUID.CHAR_EID;
import static com.fyp.cityulogin.util.BluetoothUUID.CHAR_PASSWORD;
import static com.fyp.cityulogin.util.BluetoothUtil.createGattTable;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.fyp.cityulogin.MainActivity;
import com.fyp.cityulogin.R;
import com.fyp.cityulogin.util.BluetoothUUID;
import com.fyp.cityulogin.util.BluetoothUtil;

import java.util.concurrent.CountDownLatch;

public class BluetoothController {
    private Context context;
    private Activity activity;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeAdvertiser bluetoothAdvertiser;
    private BluetoothGattServer bluetoothGattServer;
    private static final BluetoothController bluetoothController = new BluetoothController();
    private boolean allowConnection = false;

    private static final String TAG = "BluetoothController - ";


    // private constructor and singleton
    public static BluetoothController getInstance() {
        return bluetoothController;
    }

    // set context and initialize bluetooth
    public void setContext(@NonNull Context context) {
        this.context = context;
        // initialise manager
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) this.context.getSystemService(Context.BLUETOOTH_SERVICE);
        }
        // initialise adapter
        if (bluetoothManager != null && bluetoothAdapter == null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
    }

    // initialize bluetooth
    public void init(@NonNull Activity activity, int requestCode) {
        this.activity = activity;
        Log.d(TAG, "Start to init");
        // check bluetooth
        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(activity, "This device does not support BLE", Toast.LENGTH_LONG).show();
            activity.finish();
            return;
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // check adapter
        if (bluetoothAdapter == null) {
            Toast.makeText(activity, "This device does not support BLE", Toast.LENGTH_LONG).show();
            activity.finish();
            return;
        }

        // initial advertiser
        bluetoothAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        if (bluetoothAdapter == null) {
            Toast.makeText(activity, "the device not support peripheral", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "This device not support peripheral");
            activity.finish();
            return;
        }

        // to start bluetooth
        if ((bluetoothAdapter == null) || (!bluetoothAdapter.isEnabled())) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            activity.startActivityForResult(enableBtIntent, requestCode);
        }
        Log.d(TAG, "INIT finish");
    }

    // Set config of the advertiser
    public AdvertiseSettings createAdvertiseSettings(boolean connectable, int timeout) {
        // create advertise settings
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        // set advertise mode
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        // AdvertiseSettings.ADVERTISE_MODE_LOW_POWER  ,ADVERTISE_MODE_BALANCED ,ADVERTISE_MODE_LOW_LATENCY
        builder.setConnectable(connectable);
        builder.setTimeout(timeout);
        // timeout=0 means advertise all the time, MAX = 180 * 1000
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        /*
            ＊ AdvertiseSettings.ADVERTISE_TX_POWER_HIGH -56 dBm @ 1 meter with Nexus 5

            ＊ AdvertiseSettings.ADVERTISE_TX_POWER_LOW -75 dBm @ 1 meter with Nexus 5

            ＊ AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM -66 dBm @ 1 meter with Nexus 5

            ＊AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW not detected with Nexus 5
         */
        // create advertise settings
        AdvertiseSettings advertiseSettings = builder.build();
        if (advertiseSettings == null) {
            Log.e(TAG, "No advertise setting!");
        }
        return advertiseSettings;
    }

    // callback function of advertising, including success and failure
    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        // success
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Toast.makeText(context, "Advertise Start Success", Toast.LENGTH_SHORT).show();
            if (settingsInEffect != null) {
                Log.d(TAG, "advertiseCallback - onStartSuccess - TxPowerLv=" + settingsInEffect.getTxPowerLevel() + " mode=" + settingsInEffect.getMode() + " timeout=" + settingsInEffect.getTimeout());
            } else {
                Log.d(TAG, "advertiseCallback - onStartSuccess - settingInEffect is null");
            }
        }
        // failure
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.d(TAG, "advertiseCallback - onStartFailure - errorCode=" + errorCode);

            if (errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE) {
                Toast.makeText(context, "advertise_failed_data_too_large", Toast.LENGTH_LONG).show();
                Log.e(TAG, "advertiseCallback - onStartFailure - Failed, advertise data > 31 bytes.");
            } else if (errorCode == ADVERTISE_FAILED_TOO_MANY_ADVERTISERS) {
                Toast.makeText(context, "advertise_failed_too_many_advertises", Toast.LENGTH_LONG).show();
                Log.e(TAG, "advertiseCallback - onStartFailure - Failed, no advertising instance.");

            } else if (errorCode == ADVERTISE_FAILED_ALREADY_STARTED) {
                Toast.makeText(context, "advertise_failed_already_started", Toast.LENGTH_LONG).show();
                Log.e(TAG, "advertiseCallback - onStartFailure - Failed, advertising already started");

            } else if (errorCode == ADVERTISE_FAILED_INTERNAL_ERROR) {
                Toast.makeText(context, "advertise_failed_internal_error", Toast.LENGTH_LONG).show();
                Log.e(TAG, "advertiseCallback - onStartFailure - Failed, internal error");

            } else if (errorCode == ADVERTISE_FAILED_FEATURE_UNSUPPORTED) {
                Toast.makeText(context, "advertise_failed_feature_unsupported", Toast.LENGTH_LONG).show();
                Log.e(TAG, "advertiseCallback - onStartFailure - Failed, not supported");
            }
        }
    };

    // function to start advertising
    @SuppressLint("MissingPermission")
    public void startAdvertising() {
        // if advertiser does not exist, get a new one
        if (bluetoothAdvertiser == null) {
            bluetoothAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        }
        try {
            Log.d(TAG, "Start to adv");
            // start advertising
            bluetoothAdvertiser.startAdvertising(createAdvertiseSettings(true, 0)
//                    , BluetoothUtil.createIBeaconAdvertiseData(BluetoothUUID.bleServerUUID, major, minor, (byte) -0x3b)
                    , BluetoothUtil.createAdvertiseData(BluetoothUUID.BLE_USER_DATA)
                    , advertiseCallback);
            Log.d(TAG, "Success");
        } catch (Exception e) {
            Log.v(TAG, "Fail to setup BLE Service");
        }
    }

    // function to stop advertising
    @SuppressLint("MissingPermission")
    public void stopAdvertising() {
        // close BluetoothLeAdvertiser
        if (bluetoothAdvertiser != null) {
            bluetoothAdvertiser.stopAdvertising(advertiseCallback);
//            bluetoothAdvertiser = null;
        }
//        if (bluetoothAdapter != null) {
//            bluetoothAdapter = null;
//        }

        Toast.makeText(context, "Advertise Stopped", Toast.LENGTH_SHORT).show();
    }



    // create GATT table and start GATT service
    @SuppressLint("MissingPermission")
    public void startGattService(String account, String password) {
        BluetoothGattServerCallback serverCallback = new BluetoothGattServerCallback() {
            // read request from client and send characteristics data
            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
                // check if the device is allowed to connect
                if (!allowConnection) {
                    // because the dialog is not in the main thread, so we need to use runOnUiThread
                    activity.runOnUiThread(new Runnable() {
                        // show dialog to ask user to pair
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setTitle("Pair with device " + device.getName() + "?");
                            builder.setPositiveButton("Pair", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    allowConnection = true;
                                    if (characteristic.getUuid().equals(CHAR_EID) || characteristic.getUuid().equals(CHAR_PASSWORD)) {
                                        bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                                                characteristic.getValue());
                                    } else {
                                        bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, "Connection not allowed".getBytes());
                                    }
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    allowConnection = false;
                                    bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, "Connection not allowed".getBytes());
                                    dialog.dismiss();
                                }
                            });
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            });
//                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//                    builder.setTitle("Pair with " + device.getName() + " ?");
//                    builder.setPositiveButton("Pair", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            allowConnection = true;
//                            if (characteristic.getUuid().equals(CHAR_EID) || characteristic.getUuid().equals(CHAR_PASSWORD)) {
//                                bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
//                                        characteristic.getValue());
//                            } else {
//                                bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, "Connection not allowed".getBytes());
//                            }
//                            latch.countDown();
//                        }
//                    });
//                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            allowConnection = false;
//                            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, "Connection not allowed".getBytes());
//                            latch.countDown();
//                            dialog.dismiss();
//                        }
//                    });
//                    builder.setCancelable(false);
//                    AlertDialog dialog = builder.create();
//                    dialog.show();
//                    dialog.getWindow().getDecorView().bringToFront();
//                    try {
//                        latch.await();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                // if the device is allowed to connect
                } else {
                    // send response to client when the request is for EID or password
                    if (characteristic.getUuid().equals(CHAR_EID) || characteristic.getUuid().equals(CHAR_PASSWORD)) {
                        bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                                characteristic.getValue());
                    // reject the request if the request is not for EID or password
                    } else {
                        bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, "Connection not allowed".getBytes());
                    }
                }

            }

            // when connection state changes, set allowConnection to false
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                super.onConnectionStateChange(device, status, newState);
                allowConnection = false;
            }

        };
        // if GATT service does not exist, get a new one
        if (bluetoothGattServer == null) {
            bluetoothGattServer = bluetoothManager.openGattServer(this.context, serverCallback);
        }
        // add GATT table
        bluetoothGattServer.addService(createGattTable(account, password));
    }

    // close GATT service
    @SuppressLint("MissingPermission")
    public void closeGattService() {
        if (bluetoothGattServer == null) {
            return;
        }
        bluetoothGattServer.close();
        bluetoothGattServer = null;
    }


    // show pairing dialog for user to check
    @SuppressLint("MissingPermission")
    private void showPairingDialog(final BluetoothDevice device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Pair with " + device.getName() + " ?");
        builder.setMessage("OK to pair with " + device.getName() + " ");
        builder.setPositiveButton("Pair", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                allowConnection = true;
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                allowConnection = false;
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
//        dialog.getWindow().getDecorView().bringToFront();
    }
}
