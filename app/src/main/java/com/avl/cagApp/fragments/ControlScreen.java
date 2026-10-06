package com.avl.cagApp.fragments;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.avl.cagApp.R;
import com.avl.cagApp.repository.tv.TVPowerState;
import com.avl.cagApp.viewmodel.ControlScreenViewModel;
import com.avl.cagApp.viewmodel.ShareViewModel;
import com.google.android.material.button.MaterialButton;

public class ControlScreen extends Fragment {

    private ControlScreenViewModel mViewModel;
    private ShareViewModel mShareModel;
    private CountDownTimer warmupTimer;
    private int volNum = 32;
    boolean isTVConnected = false;
    boolean isSwitcherConnected = false;
    TVPowerState powerState = TVPowerState.UNKNOWN;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(ControlScreenViewModel.class);
        mShareModel = new ViewModelProvider(requireActivity()).get(ShareViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_control_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvRoomName = view.findViewById(R.id.txtRoomName);
        ConstraintLayout btnSourceUsbC = view.findViewById(R.id.btnSourceUsbC);
        ConstraintLayout btnSourceWireless = view.findViewById(R.id.btnSourceWireless);
        MaterialButton btnMute = view.findViewById(R.id.btnMute);
        MaterialButton btnPower = view.findViewById(R.id.btnPower);
        TextView txtSystemOnline = view.findViewById(R.id.txtSystemOnline);
        ImageView switchLedIcon = view.findViewById(R.id.imgSystemOnlineLed);
        ImageView tvLedIcon = view.findViewById(R.id.imgDisplayConnectedLed);
        TextView tvLedTVDesc = view.findViewById(R.id.txtDisplayConnected);
        ImageView imgDisplayIndicator = view.findViewById(R.id.imgStatusIndicator);
        TextView txtDisplayStatus = view.findViewById(R.id.txtDisplayStatus);
        SeekBar seekBarVolume = view.findViewById(R.id.seekBarVolume);
        ImageView imgVolDown = view.findViewById(R.id.imgVolDown);
        ImageView imgVolUp = view.findViewById(R.id.imgVolUp);


        btnSourceUsbC.setOnClickListener(v -> {
            mViewModel.routeInputSourceToUSB();
        });

        btnSourceWireless.setOnClickListener(v -> {
            mViewModel.routeInputSourceToWireless();
        } );

        btnPower.setOnClickListener(v -> {

            mViewModel.turnOffTV();
            performShutdown();
            Navigation.findNavController(v).navigate(R.id.action_controlScreen_to_splashScreen);

        });

        btnMute.setOnClickListener(v -> {
            Boolean current = mViewModel.getTVMuted().getValue();
            mViewModel.changeMute(current);
        });

        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                volNum = seekBar.getProgress();
                mViewModel.changeVolume(volNum);
            }
        });

        imgVolDown.setOnClickListener(v -> {
            if (volNum > 0) {
                volNum = seekBarVolume.getProgress();
                volNum--;
                seekBarVolume.setProgress(volNum);
                mViewModel.changeVolume(volNum);
            }
        });

        imgVolUp.setOnClickListener(v -> {
            if (volNum < 100) {
                volNum = seekBarVolume.getProgress();
                volNum++;
                seekBarVolume.setProgress(volNum);
                mViewModel.changeVolume(volNum);
            }
        });

        // Warmup UI components
        View layoutWarmup = view.findViewById(R.id.layoutWarmup);
        TextView txtWarmupCountdown = view.findViewById(R.id.txtWarmupCountdown);

        mShareModel.getControlRoomDevices().observe(getViewLifecycleOwner(), controlRoomDevices -> {
            if (controlRoomDevices != null) {
                tvRoomName.setText(controlRoomDevices.controlDevice.getRoomName());
                mViewModel.setSelectedControlDevice(controlRoomDevices.controlDevice);
                if (controlRoomDevices.roomDevices != null) {
                    controlRoomDevices.roomDevices.forEach(roomDevice -> {
                        if (roomDevice.getDeviceName().equals("Switch")) {
                            mViewModel.connectSwitcher(roomDevice.getDeviceIpAddress(), roomDevice.getDevicePort());
                        } else if (roomDevice.getDeviceName().equals("TV")) {
                            mViewModel.connectTV(roomDevice.getDeviceIpAddress(), roomDevice.getDevicePort());
                        }
                    });
                }
            }
        });

        mViewModel.getTvPowerState().observe(getViewLifecycleOwner(), state -> {


        });

        mViewModel.getIsUsbCSelected().observe(getViewLifecycleOwner(), isSelected -> {
            btnSourceUsbC.setBackgroundResource(isSelected ? R.drawable.bg_rounded_card_selected : R.drawable.bg_rounded_card);
        });

        mViewModel.getIsWirelessSelected().observe(getViewLifecycleOwner(), isSelected -> {
            btnSourceWireless.setBackgroundResource(isSelected ? R.drawable.bg_rounded_card_selected : R.drawable.bg_rounded_card);
        });

        mViewModel.getTVMuted().observe(getViewLifecycleOwner(), isMuted -> {
            btnMute.setBackgroundResource(isMuted ? R.drawable.bg_rounded_card_selected : R.drawable.bg_rounded_card);
            btnMute.setIconResource(isMuted ? R.drawable.ic_volume_down : R.drawable.ic_volume_mute );
        });

        mViewModel.getTVVolume().observe(getViewLifecycleOwner(), volume -> {
            if (volume != null) {
                seekBarVolume.setProgress(volume);
            }
        });

        mViewModel.getIsSystemInitialized().observe(getViewLifecycleOwner(), isInitialized -> {
            if (!isInitialized) {
                startWarmup(layoutWarmup, txtWarmupCountdown);
            } else {
                layoutWarmup.setVisibility(View.GONE);
            }
        });

        mViewModel.getIsSwitcherConnected().observe(getViewLifecycleOwner(), isConnected -> {
            setLEDConnectionStatus(switchLedIcon,  isConnected);
            String statDesc = "System is " + (isConnected ? "online" : "offline");
            txtSystemOnline.setText(statDesc);
            isSwitcherConnected = isConnected;
        });

        mViewModel.getIsTVConnected().observe(getViewLifecycleOwner(), isConnected -> {
            setLEDConnectionStatus(tvLedIcon, isConnected);
            String statDesc = "Display " + (isConnected ? "connected" : "disconnected");
            tvLedTVDesc.setText(statDesc);
            isTVConnected = isConnected;

            // turn on the tv
            if (isConnected) {
                mViewModel.turnOnTV();
            } else {
                // disable and set the display to off

            }

        });
    }

    private void showAlert() {
        CustomAlertDialog alertScreenDialog = new CustomAlertDialog();
        alertScreenDialog.setTitle("Changi Airport Group");
        alertScreenDialog.setMessage("Device IP Address not found! Please contact the admin.");
        alertScreenDialog.show(getParentFragmentManager(), "alert dialog");
    }


    private void startWarmup(View layoutWarmup, TextView txtWarmupCountdown) {
        layoutWarmup.setVisibility(View.VISIBLE);

        if (warmupTimer != null) {
            warmupTimer.cancel();
        }

        warmupTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String secUntilFinished = (millisUntilFinished / 1000) + "s";
                txtWarmupCountdown.setText(secUntilFinished);
            }

            @Override
            public void onFinish() {
                mViewModel.setSystemInitialized(true);
            }
        }.start();
    }

    private void setLEDConnectionStatus(ImageView ledIcon, boolean status) {
//        int color = ContextCompat.getColor(requireContext(), R.color.led_off);
        int color;

        if(status) {
            color = ContextCompat.getColor(requireContext(), R.color.led_connected);
        } else {
            color = ContextCompat.getColor(requireContext(), R.color.led_disconnected);
        }

        ledIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private void setLEDDisplayStatus(ImageView ledIcon, TextView txtDisplayStatus, TVPowerState status) {
        Drawable color = null;
        String desc = "";
        if(status == TVPowerState.ON) {
            color = ContextCompat.getDrawable(requireContext(), R.drawable.ic_led);
            desc = "Display is ON";
        }
        if (status == TVPowerState.OFF) {
            color = ContextCompat.getDrawable(requireContext(), R.drawable.ic_led_red);
            desc = "Display is OFF";
        }

        if (color == null) {
            return;
        }

        ledIcon.setImageDrawable(color);
        txtDisplayStatus.setText(desc);
    }

    private void performShutdown() {
        if (warmupTimer != null) {
            warmupTimer.cancel();
        }

        // Execute the shutdown sequence in the ViewModel (includes recursive TV checks)

        // Final reset logic
        mViewModel.setSystemInitialized(false);
    }


}