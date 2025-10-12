package com.buddyram.rframe;


import com.buddyram.rframe.actions.ConditionalWrapperAction;
import com.buddyram.rframe.actions.MultiAction;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.drive.DriveCondition;
import com.buddyram.rframe.drive.HolonomicDriveTrain;
import com.buddyram.rframe.drive.Navigatable;
import com.buddyram.rframe.drive.RotateAction;
import com.buddyram.rframe.drive.RotateToAction;
import com.buddyram.rosebot.Rosebot;
import com.buddyram.rosebot.head.Extension;
import com.buddyram.rosebot.head.Head;
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

    public RobotAction<Rosebot> parseCameraRaiseAction(Map<String, Object> map) {
        double height = (Double) map.get("height");
        return Extension.moveTo((int) Math.round(height));

    }

    public RotateAction parseRotateAction(Map<String, Object> map) throws JsonParseException {
        double speed = (Double) map.get("speed");
        RotateAction.Direction direction = ((Boolean) map.get("isClockwise")) ? RotateAction.Direction.CLOCKWISE : RotateAction.Direction.COUNTER_CLOCKWISE;
        return new RotateAction(direction, speed);
    }

    public MultiAction<Rosebot> parseScanAction(Map<String, Object> map) throws JsonParseException {
        double speed = 0.5;
        return new MultiAction<>(
                Extension.moveToAndWait(800),
                new RotateToAction<>(180, 4, (anglediff) -> 0.5),
                new RotateToAction<>(0, 1, (anglediff) -> 0.5),
                Extension.moveToAndWait(1800),
                new RotateToAction<>(180, 4, (anglediff) -> 0.5),
                new RotateToAction<>(0, 1, (anglediff) -> 0.5),
                Extension.moveToAndWait(0)
        );
    }

    public Object parseJson(String json) {
        Map<String, Object> map = this.gson.fromJson(json, TreeMap.class);
        String action = (String) map.get("action");
        switch (action) {
            case "rotate":
                return this.parseRotateAction(map);
            case "scan":
                return this.parseScanAction(map);
            case "raise_camera":
                return this.parseCameraRaiseAction(map);
            default:
                return null;

        }
    }
}
