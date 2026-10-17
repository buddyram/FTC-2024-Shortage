package com.buddyram.rframe.ftc.biobuzz.Robot.launcher;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.qualcomm.robotcore.hardware.Servo;

public class Blocker extends BaseComponent<Robot> {
        public final double CLOSED = 0.59;
        public final double OPEN = 0.55;
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
