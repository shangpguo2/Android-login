package com.fyp.cityulogin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.fyp.cityulogin.bluetooth.BluetoothController;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothController bluetoothController = BluetoothController.getInstance();
        bluetoothController.setContext(this.getApplicationContext());
        bluetoothController.init(this, 999);

        View rootView = findViewById(android.R.id.content);
        Button startAdv = (Button) rootView.findViewById(R.id.startAdv);

        startAdv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothController.startAdvertising();
                bluetoothController.startGattService(String.valueOf(((EditText) rootView.findViewById(R.id.account)).getText()),
                        String.valueOf(((EditText) rootView.findViewById(R.id.password)).getText()));
            }
        });
        Button endAdv = (Button) rootView.findViewById(R.id.stopAdv);
        endAdv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothController.stopAdvertising();
                bluetoothController.closeGattService();
            }

        });
    }
}