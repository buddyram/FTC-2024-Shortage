package com.buddyram.rframe;


import com.buddyram.rframe.drive.RotateAction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.util.Map;
import java.util.TreeMap;

public class JsonSerde {
    private final Gson gson;

    public JsonSerde() {
        this.gson = new Gson();
    }

    public RotateAction parseRotateAction(Map<String, Object> map) throws JsonParseException {
        double speed = (Double) map.get("speed");
        RotateAction.Direction direction = ((Boolean) map.get("isClockwise")) ? RotateAction.Direction.CLOCKWISE : RotateAction.Direction.COUNTER_CLOCKWISE;
        return new RotateAction(direction, speed);
    }

    public Object parseJson(String json) {
        Map<String, Object> map = this.gson.fromJson(json, TreeMap.class);
        String action = (String) map.get("action");
        switch (action) {
            case "rotate":
                return this.parseRotateAction(map);
            default:
                return null;

        }
    }
}
