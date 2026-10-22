package com.buddyram.rframe.ftc.biobuzz.Robot.launcher;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.ftc.RPMMotor;
import com.buddyram.rframe.ftc.decode.DecodeBot;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

public class Flywheel extends BaseComponent<Robot> {
    private final RPMMotor motor;
    private double target;
    public static final int MAX = 5000;
    public static final int MIN = 0;
    // At-speed band (+/- rpm). The hub reports speed in ~43 rpm steps, so keep this above ~22 or some targets never count as ready
    public static final int THRESHOLD = 30;
    private static final int COUNTS_PER_REV = 28;
    public static final double DEFAULT_RPM = 2250; // starting speed for the manual / test opmodes
    // Velocity PIDF for RUN_USING_ENCODER, from the Flywheel PIDF Tuner: F measured by its F calibration,
    // I and D from its best 4-ball shot rounds (I helps the wheel recover between back-to-back shots).
    public static final PIDFCoefficients PIDF = new PIDFCoefficients(254.0, 0.5, 1.0, 14.418);

    // Ball detection, same rule as the Flywheel Trace opmode: encoder speed over the last 30 ms is 150 rpm
    // under target on 2 updates in a row (one alone is often timing jitter), re-armed once back above that.
    // After a ball the feed waits BALL_PAUSE_MS and for the speed check (the trace's TELEOP+PAUSE mode:
    // 24 balls, none came in early, speed at the hit within 19 rpm sd).
    public static final double BALL_PAUSE_MS = 150;
    private static final double BALL_WINDOW_MS = 30;
    private static final double BALL_DROP_RPM = 150;
    private static final double BALL_REARM_MS = 120;
    private static final int HISTORY = 64;
    private final long[] historyNs = new long[HISTORY];
    private final int[] historyPos = new int[HISTORY];
    private int samples;
    private int belowCount;
    private boolean ballArmed;
    private volatile long lastBallNs;
    private volatile int balls;

    public Flywheel(Robot robot, RPMMotor motor) {
        super(robot);
        this.motor = motor;
    }

    /** Call every loop: samples the encoder and watches for balls going through. */
    public void update() {
        long now = System.nanoTime();
        int slot = samples % HISTORY;
        historyNs[slot] = now;
        historyPos[slot] = motor.getPosition();
        samples++;

        // Encoder speed over the newest stretch of at least BALL_WINDOW_MS
        double encRpm = Double.NaN;
        for (int back = 1; back < Math.min(samples, HISTORY); back++) {
            int old = (samples - 1 - back) % HISTORY;
            double dtMs = (now - historyNs[old]) / 1e6;
            if (dtMs >= BALL_WINDOW_MS) {
                encRpm = (historyPos[slot] - historyPos[old]) / dtMs * 1000 / COUNTS_PER_REV * 60;
                break;
            }
        }
        if (Double.isNaN(encRpm)) return;

        boolean below = target > 0 && encRpm < target - BALL_DROP_RPM;
        if (!below) ballArmed = true;
        belowCount = below ? belowCount + 1 : 0;
        if (ballArmed && belowCount >= 2 && (now - lastBallNs) / 1e6 >= BALL_REARM_MS) {
            lastBallNs = now;
            ballArmed = false;
            balls++;
        }
    }

    /** Milliseconds since update() last saw a ball hit the wheel. */
    public double msSinceLastBall() {
        return lastBallNs == 0 ? Double.MAX_VALUE : (System.nanoTime() - lastBallNs) / 1e6;
    }

    /** Balls seen since the opmode started. */
    public int getBallCount() {
        return balls;
    }

    public double getTarget() {
        return this.target;
    }

    public double getRPM() {
        return this.motor.getRPM();
    }

    public boolean isReady() {
        return Math.abs(this.target - this.motor.getRPM()) < THRESHOLD ;
    }

    public void setRPM(double newRPM) {
        if (newRPM > MAX || newRPM < MIN) {
            return;
        }
        this.target = newRPM;
        this.motor.setRPM(this.target);
    }
    public void increaseRPM(double deltaRPM) {
        this.setRPM(target + deltaRPM);
    }
    public static RobotAction<DecodeBot> setRPMTo(int tgt) {
        return (robot) -> {
            robot.getLauncher().wheel.setRPM(tgt);
            return true;
        };
    }
}
