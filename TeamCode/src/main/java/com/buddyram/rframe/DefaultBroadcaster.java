package com.buddyram.rframe;

import java.util.ArrayList;
import java.util.Collection;

public class DefaultBroadcaster<DataType> implements Broadcaster<DataType> {
    private ArrayList<MessageListener<DataType>> listeners;

    public DefaultBroadcaster() {
        this.listeners = new ArrayList<MessageListener<DataType>>();
    }

    public synchronized void addListener(MessageListener<DataType> listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public synchronized void removeListener(MessageListener<DataType> listener) {
        this.listeners.remove(listener);
    }

    public synchronized void broadcast(DataType data) {
        for (MessageListener<DataType> listener: listeners) {
            listener.handleMessage(new Message<>(this, data, System.currentTimeMillis()));
        }
    }

    @Override
    public void clearListeners() {
        listeners.clear();
    }

    @Override
    public synchronized void removeAll(Collection<MessageListener<DataType>> messageListeners) {
        for (MessageListener<DataType> listener : messageListeners) {
            this.removeListener(listener);
        }
    }
}
