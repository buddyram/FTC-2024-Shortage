package com.buddyram.rosebot;

import com.buddyram.rframe.Message;
import com.buddyram.rframe.MessageListener;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class RemoteBotMessageListener implements MessageListener<BotMessage> {
    private final Socket socket;
    private final PrintStream printStream;

    public RemoteBotMessageListener(Socket socket) {
        this.socket = socket;
        try {
            this.printStream = new PrintStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void handleMessage(Message<BotMessage> message) {
        printStream.println(message.data.toJson());
    }
}
