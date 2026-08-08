package com.buddyram.rframe.ftc.v3.Robot.intake;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.qualcomm.robotcore.hardware.Servo;

public class IntakeServoLift extends BaseComponent<Robot> {
    public double angle;
    private final Servo servoLeft;
    private final Servo servoRight;
    public final double DOWN = 0;
    public final double MIDDLE = 0.5;
    public final double UP = 1;
    public IntakeServoLift(Robot robot, Servo servoLeft, Servo servoRight) {
        super(robot);
        this.servoLeft = servoLeft;
        this.servoRight = servoRight;
    }
    public void setAngle(double newPos) {
        this.angle = newPos;
        this.servoLeft.setPosition(this.angle);
        this.servoRight.setPosition(1-this.angle);
    }
    public void up() {
        this.setAngle(this.UP);
    }
    public void middle() {
        this.setAngle(this.MIDDLE);
    }
    public void down() {
        this.setAngle(this.DOWN);
    }
}
