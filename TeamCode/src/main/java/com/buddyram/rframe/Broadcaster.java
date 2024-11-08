package com.buddyram.rframe;

public interface Broadcaster<DataType> {
    public void addListener(MessageListener<DataType> listener);
    public void removeListener(MessageListener<DataType> listener);
    public void broadcast(DataType data);
}
