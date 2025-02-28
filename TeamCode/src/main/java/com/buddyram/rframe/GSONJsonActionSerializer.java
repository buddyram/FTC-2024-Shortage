package com.buddyram.rframe;

import com.buddyram.rframe.actions.RobotAction;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class GSONJsonActionSerializer implements JsonSerializer<RobotAction<Robot>> {

    @Override
    public JsonElement serialize(RobotAction<Robot> src, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(src.toString());
    }
}
