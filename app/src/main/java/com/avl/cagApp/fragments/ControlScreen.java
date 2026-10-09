package com.avl.cagApp.fragments;

import android.graphics.PorterDuff;
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

    private TextView tvRoomName;
    private ConstraintLayout btnSourceUsbC;
    private ConstraintLayout btnSourceWireless;
    private ConstraintLayout btnSourceMS;
    private MaterialButton btnMute;
    private MaterialButton btnPower;
    private ImageView switchLedIcon;
    private ImageView tvLedIcon;
    private TextView txtSystemOnline;
    private TextView tvLedTVDesc;
    private SeekBar seekBarVolume;
    private ImageView imgVolDown;
    private ImageView imgVolUp;

    private View layoutWarmup;
    private TextView txtWarmupCountdown;



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

        tvRoomName = view.findViewById(R.id.txtRoomName);
        btnPower = view.findViewById(R.id.btnPower);
        btnPower.setOnClickListener(v -> {
            mViewModel.turnOffTV();
            performShutdown();
            Navigation.findNavController(v).navigate(R.id.action_controlScreen_to_splashScreen);
        });

        // Warmup UI components
        layoutWarmup = view.findViewById(R.id.layoutWarmup);
        txtWarmupCountdown = view.findViewById(R.id.txtWarmupCountdown);

        setupSource(view);
        setupAudioControl(view);
        setupIndicator(view);
        fetchControlDevicesData();
        observeViewModel();



    }

    private void setupSource(View view) {
        btnSourceUsbC = view.findViewById(R.id.btnSourceUsbC);
        btnSourceWireless = view.findViewById(R.id.btnSourceWireless);
        btnSourceMS = view.findViewById(R.id.btnSourceMS);

        btnSourceUsbC.setOnClickListener(v -> {
            mViewModel.routeInputSourceToUSB();
        });

        btnSourceWireless.setOnClickListener(v -> {
            mViewModel.routeInputSourceToWireless();
        } );

        btnSourceMS.setOnClickListener(v -> {
            mViewModel.routeInputSourceToMS();
        });
    }

    private void setupAudioControl(View view) {
        seekBarVolume = view.findViewById(R.id.seekBarVolume);
        imgVolDown = view.findViewById(R.id.imgVolDown);
        imgVolUp = view.findViewById(R.id.imgVolUp);
        btnMute = view.findViewById(R.id.btnMute);

        btnMute.setOnClickListener(v -> {
            Boolean current = mViewModel.getDSPMuted().getValue();
            mViewModel.changeMute(current == null || !current);
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
    }

    private void setupIndicator(View view) {
        txtSystemOnline = view.findViewById(R.id.txtSystemOnline);
        switchLedIcon = view.findViewById(R.id.imgSystemOnlineLed);
        tvLedIcon = view.findViewById(R.id.imgDisplayConnectedLed);
        tvLedTVDesc = view.findViewById(R.id.txtDisplayConnected);
    }

    private void fetchControlDevicesData() {
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
                        } else if (roomDevice.getDeviceName().equals("DSP")) {
                            mViewModel.connectDSP(roomDevice.getDeviceIpAddress(), roomDevice.getDevicePort());
                        }
                    });
                }
            }
        });
    }



    private void observeViewModel() {
        mViewModel.getTvPowerState().observe(getViewLifecycleOwner(), state -> {


        });

        mViewModel.getIsUsbCSelected().observe(getViewLifecycleOwner(), isSelected -> {
            btnSourceUsbC.setBackgroundResource(isSelected ? R.drawable.bg_rounded_card_selected : R.drawable.bg_rounded_card);
        });

        mViewModel.getIsWirelessSelected().observe(getViewLifecycleOwner(), isSelected -> {
            btnSourceWireless.setBackgroundResource(isSelected ? R.drawable.bg_rounded_card_selected : R.drawable.bg_rounded_card);
        });

        mViewModel.getIsMSSelected().observe(getViewLifecycleOwner(), isSelected -> {
            btnSourceMS.setBackgroundResource(isSelected ? R.drawable.bg_rounded_card_selected : R.drawable.bg_rounded_card);
        });


        mViewModel.getDSPMuted().observe(getViewLifecycleOwner(), isMuted -> {
            btnMute.setBackgroundResource(isMuted ? R.drawable.bg_rounded_card_selected : R.drawable.bg_rounded_card);
            btnMute.setIconResource(isMuted ? R.drawable.ic_volume_down : R.drawable.ic_volume_mute );
        });

        mViewModel.getDSPVolume().observe(getViewLifecycleOwner(), volume -> {
            if (volume != null) {
                seekBarVolume.setProgress(volume);
                volNum = volume;
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

    private void performShutdown() {
        if (warmupTimer != null) {
            warmupTimer.cancel();
        }

        // Execute the shutdown sequence in the ViewModel (includes recursive TV checks)

        // Final reset logic
        mViewModel.setSystemInitialized(false);
    }


}