package com.buddyram.rframe;

import java.util.Collection;

public interface Broadcaster<DataType> {
    public void addListener(MessageListener<DataType> listener);
    public void removeListener(MessageListener<DataType> listener);
    public void broadcast(DataType data);
    public void clearListeners();
    public void removeAll(Collection<MessageListener<DataType>> listeners);
}
