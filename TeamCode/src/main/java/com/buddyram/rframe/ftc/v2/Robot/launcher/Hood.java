package com.buddyram.rframe.ftc.v2.Robot.launcher;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.qualcomm.robotcore.hardware.Servo;

public class Hood extends BaseComponent<Robot> {
    public final double MIN = 0;
    public final double MAX = 1;
    private final Servo servo;
    public Hood(Robot robot, Servo servo) {
        super(robot);
        this.servo = servo;
    }
    public void setAngle(double newPos) {
        if (newPos > MAX || newPos < MIN) {
            return;
        }
        this.servo.setPosition(newPos);
    }
}
