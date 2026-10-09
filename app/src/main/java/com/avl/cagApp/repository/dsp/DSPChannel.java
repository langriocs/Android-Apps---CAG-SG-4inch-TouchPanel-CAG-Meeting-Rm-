package com.avl.cagApp.repository.dsp;

public enum DSPChannel {
    CH_1(0),
    CH_2(1),
    CH_3(2),
    CH_4(3),
    CH_5(4),
    CH_6(5),
    CH_7(6),
    CH_8(7),
    CH_9(8),
    CH_10(9);

    private final int value;

    DSPChannel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
