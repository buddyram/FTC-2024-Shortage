package com.buddyram.rframe.ftc.v3.Robot.launcher;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.qualcomm.robotcore.hardware.Servo;

public class Blocker extends BaseComponent<Robot> {
        public final double CLOSED = 0.6;
        public final double OPEN = 1;
        private final Servo servo;
        public double angle;
        public Blocker(Robot robot, Servo servo) {
            super(robot);
            this.servo = servo;
        }
        public void setAngle(double newPos) {
            this.angle = newPos;
            this.servo.setPosition(newPos);
        }
}
