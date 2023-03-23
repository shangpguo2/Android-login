package com.fyp.cityulogin;

import static com.fyp.cityulogin.util.BluetoothUUID.CHAR_EID;
import static com.fyp.cityulogin.util.BluetoothUUID.CHAR_PASSWORD;
import static com.fyp.cityulogin.util.StoreInfo.getInfo;
import static com.fyp.cityulogin.util.StoreInfo.storeInfo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGatt;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.fyp.cityulogin.bluetooth.BluetoothController;
import com.fyp.cityulogin.util.StoreInfo;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Get an instance of BluetoothController
        BluetoothController bluetoothController = BluetoothController.getInstance();
        // Set the context of the BluetoothController
        bluetoothController.setContext(this);
        // Initialize the BluetoothController
        bluetoothController.init(this, 999);

        // Get the start advertising button
        Button startAdv = findViewById(R.id.startAdv);
        // Get an instance of SharedPreferences
        SharedPreferences preferences = getSharedPreferences("userPref", MODE_PRIVATE);
        // Get the account and password fields
        EditText accountField = findViewById(R.id.account);
        EditText passwordField = findViewById(R.id.password);
        // Get the remember me checkbox
        CheckBox rememberMe = findViewById(R.id.rememberMe);
        // Create a new StoreInfo object to call constructor
        new StoreInfo();
        // Get the account and password from the SharedPreferences
        accountField.setText(getInfo("eid", preferences));
        passwordField.setText(getInfo("pwd", preferences));
        if (!accountField.getText().toString().equals("") && !passwordField.getText().toString().equals("")) {
            // If the account and password are not empty, check the remember me checkbox
            rememberMe.setChecked(true);
        }
        // Set the click listener for the start advertising button
        startAdv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the account and password text
                String account = String.valueOf((accountField).getText());
                String password = String.valueOf((passwordField).getText());
                // Start advertising
                bluetoothController.startAdvertising();
                // Start GATT service
                bluetoothController.startGattService(account, password);
                if (rememberMe.isChecked()) {
                    // Store the account and password in the SharedPreferences
                    storeInfo("eid", account, preferences);
                    storeInfo("pwd", password, preferences);
                } else {
                    // Clear the account and password in the SharedPreferences
                    storeInfo("eid", "", preferences);
                    storeInfo("pwd", "", preferences);
                }
            }
        });

        // Get the stop advertising button
        Button endAdv = findViewById(R.id.stopAdv);
        // Set the click listener for the stop advertising button
        endAdv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Stop advertising
                bluetoothController.stopAdvertising();
                // Stop GATT service
                bluetoothController.closeGattService();
            }
        });

    }


}