package com.avl.cagApp.repository.dsp;

import com.avl.cagApp.libs.TCPClient;
import com.avl.cagApp.model.vo.RoomDevice;
import com.avl.cagApp.repository.IRoomDevice;

public class DSPRepository implements IDSPRepository, IRoomDevice {

    private volatile IAudioListener listener;
    private final TCPClient tcpClient;
    private RoomDevice roomDevice;
    private DSPChannel channel;


    public DSPRepository() {
        tcpClient = createClient();
    }

    private TCPClient createClient() {
        return new TCPClient("cag", new TCPClient.OnMessageReceived() {
            @Override
            public void onMessageReceived(String message) {
                if (message == null || message.trim().isEmpty()) {
                    return;
                }

                handleMessage(message);

            }
        }, new TCPClient.OnConnectionStatusChanged() {
            @Override
            public void onConnected() {
                IAudioListener currentListener = listener;
                if (currentListener != null) {
                    currentListener.onConnected();
                }
            }

            @Override
            public void onDisconnected() {
                IAudioListener currentListener = listener;
                if (currentListener != null) {
                    currentListener.onDisconnected();
                }
            }
        });
    }

    private void handleMessage(String message) {

    }

    @Override
    public void connect(String ip, int port) {
        tcpClient.connect(ip, port);
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void setRoomDevice(RoomDevice roomDevice) {

    }

    @Override
    public RoomDevice getRoomDevice() {
        return null;
    }

    @Override
    public void setChannel(DSPChannel channel) {
        this.channel = channel;
    }

    @Override
    public void setVolume(int volume) {
        int faderValue = 311 + (volume * 2);
        tcpClient.sendMessage("SICL S 0000 00 NC "+ channel.getValue() +","+ faderValue +" \r");
    }

    @Override
    public void setMute(int value) {
//        tcpClient.sendMessage("SICM S 0000 00 NC "+ channel +","+ value +" \r");
        tcpClient.sendMessage("SOCM S 0000 00 NC "+ channel.getValue() +","+value+" \r");
    }

    @Override
    public void setListener(IAudioListener listener) {
        this.listener = listener;
    }

    @Override
    public void cleanup() {

    }

}
