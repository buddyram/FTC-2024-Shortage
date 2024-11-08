package com.buddyram.rframe;

public class Message<DataType> {
    public final DataType data;
    public final long at;
    public final Broadcaster<DataType> sender;

    public Message(Broadcaster<DataType> sender, DataType data, long at) {
        this.data = data;
        this.sender = sender;
        this.at = at;
    }
}
