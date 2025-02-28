package com.buddyram.rosebot;

import com.buddyram.rframe.Broadcaster;
import com.buddyram.rframe.JsonSerde;
import com.buddyram.rframe.Message;
import com.buddyram.rframe.MessageListener;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.actions.RobotAction;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class NetworkCommunicator implements MessageListener<BotMessage> {
    public static final String MOTOR_CONTROL_IN_CHANNEL = "motor.in";
    public static final String MOTOR_CONTROL_OUT_CHANNEL = "motor.out";

    private final String address;
    private final NetworkRemoteController.ActionHandler actionHandler;
    private boolean isRunning = true;
    private Jedis jedis;
    private final Broadcaster<BotMessage> broadcaster;
    private final ArrayList<MessageListener<BotMessage>> listenersAdded = new ArrayList<>();

    public NetworkCommunicator(String address, Broadcaster<BotMessage> broadcaster, NetworkRemoteController.ActionHandler actionHandler) {
        this.broadcaster = broadcaster;
        this.address = address;
        this.actionHandler = actionHandler;
    }

    public synchronized void cleanup() {
        this.broadcaster.removeListener(this);
        if (jedis != null) {
            this.jedis.close();
        }

    }

    public void stop() {
        this.isRunning = false;
    }

    public void start() {
        this.broadcaster.addListener(this);
        new Thread(() -> {
            try {
                JedisPubSub pubSub = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        JsonSerde serde = new JsonSerde();
                        Object obj = serde.parseJson(message);
                        if (obj instanceof RobotAction) {
                            try {
                                actionHandler.handle((RobotAction<Robot>) obj);
                            } catch (RobotException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                };
                jedis.subscribe(pubSub);
//                System.out.println("thread start!");
//                while (this.isRunning) {
//                    try {
//                        while (this.jedis.isConnected()) {
//                        }
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    } finally {
//                        this.reset(2000);
//                    }
//                }
            } finally {
                this.cleanup();
            }
        }).start();
    }

    @Override
    public void handleMessage(Message<BotMessage> message) {
        this.jedis.publish(MOTOR_CONTROL_OUT_CHANNEL, message.data.toJson());
    }
}
