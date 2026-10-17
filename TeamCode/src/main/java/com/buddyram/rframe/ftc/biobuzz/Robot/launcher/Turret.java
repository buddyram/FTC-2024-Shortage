package com.buddyram.rframe.ftc.biobuzz.Robot.launcher;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

public class Turret extends BaseComponent<Robot> {
    public static final double SERVO_RANGE_DEG = 355.0; // servo travel across 500-2500 us
    public static final double GEAR_RATIO = 64.0 / 70.0; // turret deg per servo deg
    public static final double CENTER_OFFSET_DEG = 0; // trim so 0 = turret pointing straight ahead
    // Auto-aim needs positive angles to turn the turret counter-clockwise seen from above (robot's left).
    // Flip this if "Turret Class Test" D-pad right (+90) swings it to the robot's right.
    public static final boolean REVERSED = true;
    private static final double DIRECTION = REVERSED ? -1 : 1;
    // Wiring / hard stop limits, shrunk to what the servo can reach once the center offset is applied
    private static final double SERVO_REACH_DEG = SERVO_RANGE_DEG * GEAR_RATIO / 2;
    public static final double MIN_ANGLE = Math.max(-90, Math.min(
            DIRECTION * (-SERVO_REACH_DEG - CENTER_OFFSET_DEG), DIRECTION * (SERVO_REACH_DEG - CENTER_OFFSET_DEG)));
    public static final double MAX_ANGLE = Math.min(90, Math.max(
            DIRECTION * (-SERVO_REACH_DEG - CENTER_OFFSET_DEG), DIRECTION * (SERVO_REACH_DEG - CENTER_OFFSET_DEG)));

    private static final double MAX_SPEED_DEG_PER_S = 180.0;
    private static final double MAX_UPDATE_DT_S = 0.1; // caps the slew step after a gap between updates
    private static final double READY_TOLERANCE_DEG = 0.5;

    private final ServoImplEx servo;
    private final ElapsedTime timer = new ElapsedTime();
    private double targetAngle = 0;
    private double commandAngle = 0;

    public Turret(Robot robot, ServoImplEx servo) {
        super(robot);
        this.servo = servo;
        this.servo.setPwmRange(new PwmControl.PwmRange(500, 2500));
        this.servo.setPosition(angleToPosition(commandAngle));
    }

    private double angleToPosition(double angle) {
        double servoDeg = (DIRECTION * angle + CENTER_OFFSET_DEG) / GEAR_RATIO;
        return Range.clip(0.5 + servoDeg / SERVO_RANGE_DEG, 0.0, 1.0);
    }

    // The servo is absolute but has no readback, so the slewed command doubles as the current angle.
    private synchronized void update() {
        double maxStep = MAX_SPEED_DEG_PER_S * Math.min(timer.seconds(), MAX_UPDATE_DT_S);
        timer.reset();
        if (commandAngle == targetAngle) {
            return;
        }
        commandAngle += Range.clip(targetAngle - commandAngle, -maxStep, maxStep);
        servo.setPosition(angleToPosition(commandAngle));
    }

    public synchronized double getCurrentAngle() {
        update();
        return commandAngle;
    }

    public synchronized boolean isReady() {
        update();
        return Math.abs(targetAngle - commandAngle) < READY_TOLERANCE_DEG;
    }

    public synchronized void setAngle(double newAngle) {
        update();
        targetAngle = Range.clip(newAngle, MIN_ANGLE, MAX_ANGLE);
        update();
    }
}
