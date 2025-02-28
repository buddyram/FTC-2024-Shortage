package com.buddyram.rosebot;

import com.buddyram.rframe.JsonSerde;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.actions.RobotAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class NetworkRemoteController implements Runnable {
    private final ActionHandler handler;
    Socket client;
    public NetworkRemoteController(Socket client, ActionHandler handler) {
        this.client = client;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            BufferedReader scanner = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
            JsonSerde serde = new JsonSerde();
            String line;
            while ((line = scanner.readLine()) != null) {
                System.out.println(line);


                Object obj = serde.parseJson(line);
                if (obj instanceof RobotAction) {
                    handler.handle((RobotAction<Robot>) obj);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public interface ActionHandler {
        void handle(RobotAction<Robot> action) throws RobotException;
    }
}
