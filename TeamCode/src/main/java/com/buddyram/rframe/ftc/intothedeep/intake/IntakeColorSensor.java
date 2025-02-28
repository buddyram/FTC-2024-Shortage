package com.buddyram.rframe.ftc.intothedeep.intake;

import android.graphics.Color;

import com.buddyram.rframe.ColorHSV;
import com.buddyram.rframe.ColorRGB;
import com.buddyram.rframe.ftc.intothedeep.ShortageBot;
import com.qualcomm.hardware.rev.RevColorSensorV3;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class IntakeColorSensor {
    private final RevColorSensorV3 color;

    public double getDistance(DistanceUnit distanceUnit) {
        return this.color.getDistance(distanceUnit);
    }

    public ColorHSV getColorHSV() {
        ColorRGB color = this.getColorRGB();
        float[] hsv = new float[]{0F, 0F, 0F};
        Color.RGBToHSV((int) color.r, (int) color.g, (int) color.b, hsv);
        return new ColorHSV(hsv[0], hsv[1], hsv[2]);
    }

    public ColorRGB getColorRGB() {
        return new ColorRGB(this.color.red(), this.color.green(), this.color.blue(), this.color.alpha());
    }

    public IntakeColorSensor(ShortageBot robot, RevColorSensorV3 color) {
        this.color = color;
    }
}
