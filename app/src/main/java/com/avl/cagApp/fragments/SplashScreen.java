package com.avl.cagApp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.avl.cagApp.AppConstant;
import com.avl.cagApp.R;
import com.avl.cagApp.libs.MyLibUtil;
import com.avl.cagApp.viewmodel.ShareViewModel;
import com.google.android.material.button.MaterialButton;

public class SplashScreen extends Fragment {

    private TextView tvRoomName;
    private MaterialButton btnPressStart;
    private int controlDeviceUI;

    public static SplashScreen newInstance() {
        return new SplashScreen();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvRoomName = view.findViewById(R.id.room_name_tv);

        btnPressStart = view.findViewById(R.id.btn_press_start);

        if (btnPressStart == null) {
            return;
        }

        btnPressStart.setOnClickListener(v -> {
            proceedToNextScreen(v);
        });

        view.setOnClickListener(v -> {
            proceedToNextScreen(v);
        });

        ShareViewModel shareViewModel = new ViewModelProvider(requireActivity()).get(ShareViewModel.class);
        shareViewModel.getControlRoomDevices().observe(getViewLifecycleOwner(), controlRoomDevice -> {
            if (controlRoomDevice != null) {
                shareViewModel.setSelectedControlDevice(controlRoomDevice.controlDevice);
                shareViewModel.setSelectedRoomDevices(controlRoomDevice.roomDevices);
                tvRoomName.setText(controlRoomDevice.controlDevice.getRoomName());
                controlDeviceUI = controlRoomDevice.controlDevice.getDeviceUI();
            } else {
                showAlert("Device IP Address not found! Please contact the admin.");
            }
        });

//        final String ipAddress = "192.168.1.10";
//        final String ipAddress = "192.168.1.131";
        final String ipAddress = MyLibUtil.getIPAddress(true);
        shareViewModel.fetchControlDeviceByIpAddress(ipAddress);
    }

    private void proceedToNextScreen(View v) {
        Navigation.findNavController(v).navigate(R.id.action_splashScreen_to_controlScreen);
    }

    private void showAlert(String message ) {
        CustomAlertDialog alertScreenDialog = new CustomAlertDialog();
        alertScreenDialog.setTitle("Changi Airport Group");
        alertScreenDialog.setMessage(message);
        alertScreenDialog.show(getParentFragmentManager(), "alert dialog");
    }
}