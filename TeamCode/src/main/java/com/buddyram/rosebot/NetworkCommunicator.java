package com.buddyram.rosebot;

import com.buddyram.rframe.Broadcaster;
import com.buddyram.rframe.MessageListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class NetworkCommunicator {
    private final InetSocketAddress address;
    private final NetworkRemoteController.ActionHandler actionHandler;
    private boolean isRunning = true;
    private ServerSocket serverSocket;
    private final Broadcaster<BotMessage> broadcaster;
    private final ArrayList<MessageListener<BotMessage>> listenersAdded = new ArrayList<>();

    public NetworkCommunicator(InetSocketAddress address, Broadcaster<BotMessage> broadcaster, NetworkRemoteController.ActionHandler actionHandler) {
        this.broadcaster = broadcaster;
        this.address = address;
        this.actionHandler = actionHandler;
    }

    public void reset(int msWaitTime) {
        this.cleanup();
        try {
            this.serverSocket = new ServerSocket();
            this.serverSocket.bind(this.address);
            Thread.sleep(msWaitTime);
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void cleanup() {
        try {
            this.broadcaster.removeAll(this.listenersAdded);
            this.listenersAdded.clear();
            if (serverSocket != null) {
                this.serverSocket.close();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void stop() {
        this.isRunning = false;
    }

    public void start() {
        new Thread(() -> {
            System.out.println("thread start!");
            this.reset(0);
            while (this.isRunning) {
                try {
                    while (!this.serverSocket.isClosed()) {
                        Socket socket = this.serverSocket.accept();
                        System.out.println("accept!!");

                        RemoteBotMessageListener listener = new RemoteBotMessageListener(socket);
                        new Thread(new NetworkRemoteController(socket, this.actionHandler)).start();
                        this.broadcaster.addListener(listener);
                        this.listenersAdded.add(listener);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    this.reset(2000);
                }
            }
            this.cleanup();
        }).start();
    }
}
