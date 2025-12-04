package com.buddyram.rframe.ftc.decode.indexer;

import android.graphics.Color;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.ColorHSV;
import com.buddyram.rframe.ftc.decode.DecodeBot;
import com.qualcomm.hardware.rev.RevColorSensorV3;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.opencv.core.Mat;

public class ColorSensor extends BaseComponent<DecodeBot> {
    public static final ColorHSV GREEN = new ColorHSV(144,67,53);
    public static final ColorHSV PURPLE = new ColorHSV(281,57,88);
    final int BLANK_HUE; // = 123;
    final int GREEN_HUE; // = 145;
    final int PURPLE_HUE; // = 175;
    final double activationDistance;
    public enum ColorMatch {
        MATCH_GREEN,
        MATCH_PURPLE,
        NONE
    }
    public RevColorSensorV3 colorSensor;
    public ColorSensor(DecodeBot robot, RevColorSensorV3 colorSensor, double activationDistance, int[] colorMatches) {
        super(robot);
        BLANK_HUE = colorMatches[0];
        GREEN_HUE = colorMatches[1];
        PURPLE_HUE = colorMatches[2];
        this.activationDistance = activationDistance;
        this.colorSensor = colorSensor;
    }
    public ColorHSV getColor() {
        float[] hsv = new float[3];
        Color.RGBToHSV(
                this.colorSensor.red(),
                this.colorSensor.green(),
                this.colorSensor.blue(),
                hsv
        );
        return new ColorHSV(hsv[0], hsv[1], hsv[2]);
    }
    public ColorMatch indexerBall() {
        ColorSensor.ColorMatch currentColor = null;
        float[] hsv = new float[3];

        Color.RGBToHSV(
                colorSensor.red(),
                colorSensor.green(),
                colorSensor.blue(),
                hsv
        );
        if (colorSensor.getDistance(DistanceUnit.INCH) < this.activationDistance) {
            float best = Float.POSITIVE_INFINITY;
            float score = Math.abs(PURPLE_HUE - hsv[0]);
            if (score < best) {
                currentColor = ColorSensor.ColorMatch.MATCH_PURPLE;
                best = score;
            }
            score = Math.abs(GREEN_HUE - hsv[0]);
            if (Math.abs(GREEN_HUE - hsv[0]) < best) {
                currentColor = ColorSensor.ColorMatch.MATCH_GREEN;
                best = score;
            }
            score = Math.abs(BLANK_HUE - hsv[0]);
            if (Math.abs(BLANK_HUE - hsv[0]) < best) {
                currentColor = ColorSensor.ColorMatch.NONE;
                best = score;
            }
        } else {
            currentColor = ColorSensor.ColorMatch.NONE;
        }
        return currentColor;
    }
    public ColorMatch findHueMatch() {
        ColorHSV color = this.getColor();
        if (this.colorSensor.getDistance(DistanceUnit.INCH) < 3) {
            if (Math.abs(PURPLE.h - color.h) < 50) {
                return ColorMatch.MATCH_PURPLE;
            }
            if (Math.abs(GREEN.h - color.h) < 50) {
                return ColorMatch.MATCH_GREEN;
            }
        }
        return ColorMatch.NONE;
    }
}
