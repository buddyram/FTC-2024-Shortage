package com.buddyram.rosebot;

import com.buddyram.rframe.Broadcaster;
import com.buddyram.rframe.JsonSerde;
import com.buddyram.rframe.Message;
import com.buddyram.rframe.MessageListener;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.actions.RobotAction;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.JedisPubSub;

public class NetworkCommunicator implements MessageListener<BotMessage> {
    public static final String MOTOR_CONTROL_IN_CHANNEL = "motor.in";
    public static final String MOTOR_CONTROL_OUT_CHANNEL = "motor.out";

    private final NetworkRemoteController.ActionHandler actionHandler;
    private final JedisPubSub pubSub;
    private boolean isRunning = true;
    private final Jedis jedisIn;
    private final Jedis jedisOut;
    private final Broadcaster<BotMessage> broadcaster;
    private final ArrayList<MessageListener<BotMessage>> listenersAdded = new ArrayList<>();
    private ExecutorService subscriber;

    public NetworkCommunicator(String address, Broadcaster<BotMessage> broadcaster, NetworkRemoteController.ActionHandler actionHandler) {
        this.broadcaster = broadcaster;
        this.actionHandler = actionHandler;
        this.jedisIn = new Jedis(address);
        this.jedisOut = new Jedis(address);
        this.pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                JsonSerde serde = new JsonSerde();
                Object obj = serde.parseJson(message);
                System.out.println(message);
                if (obj instanceof RobotAction) {
                    System.out.println("found ROBOTACTION");
                    try {
                        actionHandler.handle((RobotAction<Robot>) obj);
                    } catch (RobotException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
    }

    public synchronized void cleanup() {
        this.broadcaster.removeListener(this);
        System.out.println("cleanup");
        this.pubSub.unsubscribe();
        System.out.println("unsubscribed");
        if(this.subscriber != null) subscriber.shutdown();

        this.jedisIn.close();
        this.jedisOut.close();
        System.out.println("closed");
    }

    public void stop() {
        System.out.println("stop");
        this.cleanup();
        System.out.println("stop:done");
        this.isRunning = false;
    }

    public void start() {
        this.broadcaster.addListener(this);
        this.subscriber = Executors.newFixedThreadPool(1);
        this.subscriber.execute(() -> jedisIn.subscribe(pubSub, MOTOR_CONTROL_IN_CHANNEL));
        this.isRunning = true;
    }

    @Override
    public void handleMessage(Message<BotMessage> message) {
        this.jedisOut.publish(MOTOR_CONTROL_OUT_CHANNEL, message.data.toJson());
    }
}
