package com.example.qkx.speechtotext;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by qkx on 16/4/8.
 */
public class BusManager {
    private static final EventBus UI_BUS = new EventBus();
    private static final EventBus DEUFAULT_BUS = EventBus.getDefault();

    private BusManager(){}

    public static EventBus getUiBus() {
        return UI_BUS;
    }

    public static EventBus getDefaultBus() {
        return DEUFAULT_BUS;
    }
}
