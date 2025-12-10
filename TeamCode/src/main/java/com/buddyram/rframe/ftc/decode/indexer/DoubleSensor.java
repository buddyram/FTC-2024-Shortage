package com.buddyram.rframe.ftc.decode.indexer;

import com.buddyram.rframe.ftc.decode.DecodeBot;
import com.qualcomm.hardware.rev.RevColorSensorV3;

public class DoubleSensor extends ColorSensor {
    public final ColorSensor sensor2;
    public DoubleSensor(DecodeBot robot, RevColorSensorV3 colorSensor, double activationDistance, int[] colorMatches, ColorSensor sensor2) {
        super(robot, colorSensor, activationDistance, colorMatches);
        this.sensor2 = sensor2;
    }

    @Override
    public ColorMatch indexerBall() {
        ColorMatch res = super.indexerBall();
        ColorMatch res2 = sensor2.indexerBall();
        if (res == ColorMatch.NONE && res2 == ColorMatch.NONE) {
            return ColorMatch.NONE;
        }
        if (res == ColorMatch.MATCH_PURPLE || res2 == ColorMatch.MATCH_PURPLE) {
            return ColorMatch.MATCH_PURPLE;
        }
        return ColorMatch.MATCH_GREEN;
    }
}
