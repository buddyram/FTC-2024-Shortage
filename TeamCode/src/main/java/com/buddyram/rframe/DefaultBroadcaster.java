package com.buddyram.rframe;

import java.util.ArrayList;

public class DefaultBroadcaster<DataType> implements Broadcaster<DataType> {
    private ArrayList<MessageListener<DataType>> listeners;

    public DefaultBroadcaster() {
        this.listeners = new ArrayList<MessageListener<DataType>>();
    }

    public void addListener(MessageListener<DataType> listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public void removeListener(MessageListener<DataType> listener) {
        this.listeners.remove(listener);
    }

    public void broadcast(DataType data) {
        for (MessageListener<DataType> listener: listeners) {
            listener.handleMessage(new Message<DataType>(this, data, System.currentTimeMillis()));
        }
    }
}
