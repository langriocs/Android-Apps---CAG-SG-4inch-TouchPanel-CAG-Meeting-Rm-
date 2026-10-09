package com.avl.cagApp.repository.dsp;

public interface IDSPRepository {
    void setChannel(DSPChannel channel);
    void setVolume( int faderValue);
    void setMute( int value);
    void setListener(IAudioListener listener);
    void cleanup();
}
