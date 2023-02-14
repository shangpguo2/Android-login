package com.fyp.cityulogin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.fyp.cityulogin.databinding.FragmentFirstBinding;
import com.fyp.cityulogin.bluetooth.BluetoothController;
import com.fyp.cityulogin.utils.BluetoothUUID;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);

        BluetoothController bluetoothController = BluetoothController.getInstance();
        bluetoothController.setContext(this);
        bluetoothController.init(getActivity(), 999);

        View rootView = binding.getRoot();
        Button startAdv = (Button) rootView.findViewById(R.id.startAdv);
        startAdv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothController.startAdvertising();
                bluetoothController.startGattService(String.valueOf(((EditText) rootView.findViewById(R.id.account)).getText()),
                        String.valueOf(((EditText) rootView.findViewById(R.id.password)).getText()));
            }
        });
        Button endAdv = (Button) rootView.findViewById(R.id.endAdv);
        endAdv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothController.stopAdvertising();
                bluetoothController.closeGattService();
            }

        });
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}