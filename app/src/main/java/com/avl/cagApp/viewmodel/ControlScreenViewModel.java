package com.avl.cagApp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.avl.cagApp.model.vo.ControlDevice;
import com.avl.cagApp.repository.IRoomDevice;
import com.avl.cagApp.repository.dsp.DSPChannel;
import com.avl.cagApp.repository.dsp.DSPRepository;
import com.avl.cagApp.repository.dsp.IAudioListener;
import com.avl.cagApp.repository.dsp.IDSPRepository;
import com.avl.cagApp.repository.switcher.Switch5x1Output;
import com.avl.cagApp.repository.tv.TVPowerState;
import com.avl.cagApp.repository.switcher.ISwitchListener;
import com.avl.cagApp.repository.switcher.Switch5x1Repository;
import com.avl.cagApp.repository.tv.ITVListener;
import com.avl.cagApp.repository.tv.LGTVRepository;

public class ControlScreenViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isSystemInitialized = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSwitcherConnected = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isTVConnected = new MutableLiveData<>(false);
    private final MutableLiveData<TVPowerState> tvState = new MutableLiveData<>(TVPowerState.OFF);
    private final MutableLiveData<Boolean> isUsbCSelected = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isWirelessSelected = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isMSSeleccted = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> dspMuted = new MutableLiveData<>(false);
    private final MutableLiveData<String> tvMessage = new MutableLiveData<>("");
    private final MutableLiveData<Integer> dspVolume = new MutableLiveData<>(50);


    private final IRoomDevice tvRepository;
    private final IRoomDevice switchRepository;
    private final IRoomDevice dspRepository;


    private Switch5x1Output selectedSource;

    public ControlScreenViewModel () {
        tvRepository = new LGTVRepository();
        switchRepository = new Switch5x1Repository();
        dspRepository = new DSPRepository();

        setupTVListener();
        setupSwitchListener();
        setupDSPListener();
    }

    public LiveData<Boolean> getIsSystemInitialized() { return isSystemInitialized; }
    public void setSystemInitialized(boolean initialized) { isSystemInitialized.postValue(initialized); }

    public LiveData<Boolean> getIsSwitcherConnected() {
        return isSwitcherConnected;
    }

    public LiveData<Boolean> getIsTVConnected() {
        return isTVConnected;
    }

    public LiveData<TVPowerState> getTvPowerState() {
        return tvState;
    }

    public LiveData<Boolean> getIsUsbCSelected() {
        return isUsbCSelected;
    }

    public void setUsbCSelected(boolean selected) {
        isUsbCSelected.postValue(selected);
    }

    public LiveData<Boolean> getIsWirelessSelected() {
        return isWirelessSelected;
    }

    public void setWirelessSelected(boolean selected) {
        isWirelessSelected.postValue(selected);
    }

    public LiveData<Boolean> getIsMSSelected() {
        return isMSSeleccted;
    }

    public LiveData<Boolean> getDSPMuted() {
        return dspMuted;
    }

    public LiveData<String> getTVMessage() {
        return tvMessage;
    }

    public LiveData<Integer> getDSPVolume() {
        return dspVolume;
    }

    // TV setup
    private void setupTVListener() {
        ((LGTVRepository) tvRepository).setListener(new ITVListener() {
            @Override
            public void onConnected() {
                isTVConnected.postValue(true);
            }

            @Override
            public void onDisconnected() {
                isTVConnected.postValue(false);
            }

            @Override
            public void onPowerStateChanged(TVPowerState state) {
                tvState.postValue(state);
            }

            @Override
            public void onVolumeChanged(int volume) {
                dspVolume.postValue(volume);
            }

            @Override
            public void onMuteChanged(boolean state) {
                dspMuted.postValue(state);
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    public void connectTV(String ip, int port) {
        tvRepository.connect(ip, port);
    }

    public void connectDSP(String ip, int port) {
        dspRepository.connect(ip, port);
        ((IDSPRepository) dspRepository).setChannel(DSPChannel.CH_1);
        ((IDSPRepository) dspRepository).setVolume(50);
        ((IDSPRepository) dspRepository).setMute(0);
        dspVolume.postValue(50);
        dspMuted.postValue(false);
    }
    public void connectSwitcher(String ip, int port) {
        switchRepository.connect(ip, port);
    }

    public void turnOnTV() {
        ((LGTVRepository) tvRepository).turnOn();
    }

    public void turnOffTV() {
        ((LGTVRepository) tvRepository).turnOff();
    }

    public void changeMute(boolean isMute) {
        ((IDSPRepository) dspRepository).setMute(isMute ? 1 : 0);
        dspMuted.postValue(isMute);
    }

    public void changeVolume(int volume) {
        ((IDSPRepository) dspRepository).setVolume(volume);
        dspVolume.postValue(volume);
    }

    // Switch setup
    private void setupSwitchListener() {
        ((Switch5x1Repository) switchRepository).setListener(new ISwitchListener() {
            @Override
            public void onConnected() {
                isSwitcherConnected.postValue(true);
            }

            @Override
            public void onDisconnected() {
                isSwitcherConnected.postValue(false);
            }
        });
    }

    private void setupDSPListener() {
        ((IDSPRepository) dspRepository).setListener(new IAudioListener() {
            @Override
            public void onConnected() {

                ((IDSPRepository) dspRepository).setChannel(DSPChannel.CH_1);
                ((IDSPRepository) dspRepository).setVolume(50);
                ((IDSPRepository) dspRepository).setMute(0);
                dspVolume.postValue(50);

            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onVolumeChanged(int volume) {

            }

            @Override
            public void onMuteChanged(boolean isMuted) {

            }

            @Override
            public void setListener(IAudioListener listener) {

            }

            @Override
            public void onError(String message) {

            }
        });

        ((IDSPRepository) dspRepository).setChannel(DSPChannel.CH_1);
        ((IDSPRepository) dspRepository).setVolume(50);
        ((IDSPRepository) dspRepository).setMute(0);

        dspVolume.postValue(50);
    }


    public void disconnectSwitcher() {
        switchRepository.disconnect();
    }
    public void routeInputSourceToUSB() {
        isUsbCSelected.postValue(true);
        isWirelessSelected.postValue(false);
        isMSSeleccted.postValue(false);
        ((Switch5x1Repository) switchRepository).routeInputSourceTo(selectedSource);
    }

    public void routeInputSourceToWireless() {
        isUsbCSelected.postValue(false);
        isWirelessSelected.postValue(true);
        isMSSeleccted.postValue(false);
        ((Switch5x1Repository) switchRepository).routeInputSourceTo(Switch5x1Output.HDMI_4);
    }

    public void routeInputSourceToMS() {
        isUsbCSelected.postValue(false);
        isWirelessSelected.postValue(false);
        isMSSeleccted.postValue(true);
        ((Switch5x1Repository) switchRepository).routeInputSourceTo(Switch5x1Output.HDMI_3);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        tvRepository.cleanup();
        switchRepository.cleanup();
    }

    public void setSelectedControlDevice(ControlDevice controlDevice) {
        if (controlDevice.getIpAdd().equals("192.168.1.60")) { // if Room is PMA Holding
            selectedSource = Switch5x1Output.HDMI_3;
        } else {
            selectedSource = Switch5x1Output.USB_1;
        }
    }

}
