package com.avl.cagApp.repository.dsp;

public interface IAudioListener {

    void onConnected();
    void onDisconnected();
    void onVolumeChanged(int volume);
    void onMuteChanged(boolean isMuted);
    void setListener(IAudioListener listener);
    void onError(String message);

}
