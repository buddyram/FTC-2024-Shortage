package com.buddyram.rframe.ftc.intothedeep.intake;


import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.ftc.intothedeep.ShortageBot;
import com.qualcomm.hardware.rev.RevColorSensorV3;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Intake extends BaseComponent<Robot> {
    public final Extension extension;
    public final VirtualFourBar virtualFourBar;
    public final Roller roller;
    public final IntakeColorSensor color;

    public Intake(Robot robot, Extension extension, Roller roller, VirtualFourBar virtualFourBar, IntakeColorSensor color) {
        super(robot);
        this.roller = roller;
        this.extension = extension;
        this.virtualFourBar = virtualFourBar;
        this.color = color;
    }

    public boolean hasCapturedSample() {
        return this.color.getDistance(DistanceUnit.INCH) < 2;
    }

    public boolean hasCapturedSampleColor(ShortageBot.SampleColors color) {
        double hue = this.color.getColorHSV().h;
        return Math.abs(hue - (
               color == ShortageBot.SampleColors.BLUE ? 227 :
               color == ShortageBot.SampleColors.YELLOW ? 81 :
               color == ShortageBot.SampleColors.RED ? 18 : -10000)
        ) < 10;
    }

    public ShortageBot.SampleColors getColor() {
        if (hasCapturedSampleColor(ShortageBot.SampleColors.RED)) {
            return ShortageBot.SampleColors.RED;
        } else if (hasCapturedSampleColor(ShortageBot.SampleColors.YELLOW)) {
            return ShortageBot.SampleColors.YELLOW;
        } else if (hasCapturedSampleColor(ShortageBot.SampleColors.BLUE)) {
            return ShortageBot.SampleColors.BLUE;
        }
        return null;
    }
}