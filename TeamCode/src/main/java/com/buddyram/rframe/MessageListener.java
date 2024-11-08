package com.buddyram.rframe;

public interface MessageListener<DataType> {
    public void handleMessage(Message<DataType> message);
}
