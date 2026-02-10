package com.buddyram.rframe.ftc.v2;

import com.buddyram.rframe.GroundingOdometry;
import com.buddyram.rframe.Logger;
import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.actions.MultiAction;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.actions.TimeoutWrapperAction;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.drive.HolonomicDriveTrain;
import com.buddyram.rframe.drive.Navigatable;
import com.buddyram.rframe.ftc.DriveToAction;
import com.buddyram.rframe.ftc.decode.DecodeBot;
import com.buddyram.rframe.ftc.decode.Globals;
import com.buddyram.rframe.ftc.v2.Robot.intake.Intake;
import com.buddyram.rframe.ftc.v2.Robot.launcher.Launcher;

public class NewDecodeBot implements Navigatable<HolonomicDriveTrain> {
    public enum AutoStep { NEAR, MIDDLE, FAR, OPEN_GATE, SHOOT }

    public static class AutoSequenceConfig {
        public final Vector3D shootPos;
        public final double shootHeading;
        public final AutoStep[] steps;
        public final Vector3D parkPos;
        public final int firstTurretWaitMs;
        public final int turretWaitMs;

        public AutoSequenceConfig(Vector3D shootPos, double shootHeading,
                                  AutoStep[] steps, Vector3D parkPos,
                                  int firstTurretWaitMs, int turretWaitMs) {
            this.shootPos = shootPos;
            this.shootHeading = shootHeading;
            this.steps = steps;
            this.parkPos = parkPos;
            this.firstTurretWaitMs = firstTurretWaitMs;
            this.turretWaitMs = turretWaitMs;
        }

        public AutoSequenceConfig(Vector3D shootPos, double shootHeading,
                                  AutoStep[] steps, Vector3D parkPos) {
            this(shootPos, shootHeading, steps, parkPos, 1000, 1000);
        }
    }
    public boolean block = true;
    public Logger logger;
    public Odometry<Pose3D> odometry;
    private static final Vector3D BLUE_GOAL = new Vector3D(0, 137, 0);
    private static final Vector3D RED_GOAL = new Vector3D(144, 137, 0);
    public boolean isRed;
    public double turretOffset = 0;
    public boolean jamFix = false;

    public Odometry<Pose3D> getApriltagOdometry() {
        return apriltagOdometry;
    }

    public Odometry<Pose3D> apriltagOdometry;
    public HolonomicDriveTrain drive;


    public Intake getIntake() {
        return intake;
    }

    public Intake intake;

    public boolean aimOn = false;

    public Launcher getLauncher() {
        return launcher;
    }

    public Launcher launcher;

    public Vector3D targetGoal;

    public NewDecodeBot() {
        this(null, null, null, null, null, null, false);
    }
    public NewDecodeBot(Logger logger, GroundingOdometry<Pose3D> odometry, HolonomicDriveTrain drive, Launcher launcher, Intake intake, Odometry<Pose3D> apriltagOdometry, boolean isRed) {
        this.init(logger, odometry, drive, launcher, intake, apriltagOdometry, isRed);
    }

    public void init(Logger logger, Odometry<Pose3D> odometry, HolonomicDriveTrain drive, Launcher launcher, Intake intake, Odometry<Pose3D> apriltagOdometry, boolean isRed) {
        this.logger = logger;
        this.odometry = odometry;
        this.drive = drive;
        this.launcher = launcher;
        this.intake = intake;
        this.apriltagOdometry = apriltagOdometry;
        this.isRed = isRed;
        this.targetGoal = isRed ? RED_GOAL : BLUE_GOAL;
    }

    @Override
    public Odometry<Pose3D> getOdometry() {
        return this.odometry;
    }

    @Override
    public HolonomicDriveTrain getDrive() {
        return this.drive;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    public HolonomicDriveInstruction calculateRelativeDriveInstruction(Vector3D relativeTarget, double speed) {
        return this.calculateDriveInstruction(relativeTarget.add(this.odometry.get().position), speed);
    }
    public class AdjustFlywheelSpeedAction implements RobotAction<NewDecodeBot>  {
        @Override
        public boolean run(NewDecodeBot drive) throws RobotException {
            drive.adjustFlywheelSpeed();
            return true;
        }
    }
    public int speed = 0;
    public Double overrideDistance = null;
    public void adjustFlywheelSpeed() {
        double dist = overrideDistance == null ? this.odometry.get().position.distance(this.targetGoal) : overrideDistance;
        this.getLauncher().wheel.setRPM(Math.max(2350.50621 * Math.pow(1.00529, dist) + speed, 0));
        if (dist > 78) {
            this.launcher.hood.setAngle(0.55);
        } else if (dist > 68) {
            this.launcher.hood.setAngle(0.5);
        } else {
            this.launcher.hood.setAngle(0.96);
        }
    }
    public Double overrideAngle = null;

    /**
     * Calculates the turret angle needed to hit the goal from a given position and heading.
     * Use this to preload the turret during a drive so it's ready to shoot on arrival.
     */
    public double calculateTurretAngle(Vector3D fromPosition, double heading) {
        Vector3D posToGoal = this.targetGoal.sub(fromPosition);
        double angle = (Math.toDegrees(Math.atan2(posToGoal.y, posToGoal.x)) - 90 - heading + turretOffset) % 360;
        if (angle < 0) angle += 360;
        angle = angle > 180 ? angle - 360 : angle;
        return angle;
    }

    /**
     * Preloads the turret angle for a future shooting position/heading.
     * Sets overrideAngle so the turret starts moving immediately, even while driving.
     */
    public void preloadTurretForPosition(Vector3D shootPosition, double shootHeading) {
        this.aimOn = true;
        this.overrideAngle = calculateTurretAngle(shootPosition, shootHeading);
    }

    /**
     * Waits until the turret motor reaches its target position, with a timeout.
     */
    public void
    waitForTurret(int timeoutMs) throws RobotException {
        long start = System.currentTimeMillis();
        while (!this.launcher.turret.isReady()) {
            if (System.currentTimeMillis() - start > timeoutMs) {
                System.out.println("[AUTO] waitForTurret timed out after " + timeoutMs + "ms");
                break;
            }
            try { Thread.sleep(10); } catch (InterruptedException e) { throw new RuntimeException(e); }
        }
    }

    public double autoAim() {
        Vector3D posToGoal = this.targetGoal.sub(this.odometry.get().position);
        double angle = (Math.toDegrees(Math.atan2(posToGoal.y, posToGoal.x)) - 90 - this.odometry.get().rotation.z + turretOffset) % 360;
        if (angle < 0) angle += 360;
        angle = angle > 180 ? angle - 360 : angle;
        if (overrideAngle != null) {
            angle = overrideAngle;
        }
        if (!aimOn) {
            angle = 0;
        }

        this.launcher.turret.setAngle(angle);
        return angle;
    }

    public HolonomicDriveInstruction calculateDriveInstruction(Vector3D target, double speed) {
        double rotationInstruction = 0, driveSpeedInstruction = 0, driveAngleInstruction = 0;
        Pose3D pos = this.odometry.get();
        driveSpeedInstruction = speed;
        driveAngleInstruction = pos.position.calculateRotation(target).z;

        return new HolonomicDriveInstruction(rotationInstruction, driveSpeedInstruction, driveAngleInstruction);
    }


    public void controlIntake() {
        this.launcher.blocker.setAngle(this.block ? this.launcher.blocker.OPEN : this.launcher.blocker.CLOSED);
        if (jamFix) {
            this.intake.enableMode(Intake.Modes.INTAKING);
            return;
        }
        else {
            this.intake.enableMode(Intake.Modes.IDLE);
        }
    }

    private void logPos(String label) {
        Pose3D pos = this.odometry.get();
        System.out.println("[AUTO] " + label + " pos=(" +
            String.format("%.2f", pos.position.x) + ", " +
            String.format("%.2f", pos.position.y) + ") heading=" +
            String.format("%.2f", pos.rotation.z));
    }

    public void intakeTick(double x, double y) throws RobotException {
        jamFix = true;
        BotUtilsNew.driveAndRotateTo(BotUtilsNew.mirrorIfRed(new Vector3D(45, y, 0), isRed), BotUtilsNew.mirrorIfRed(90 /*+ 20*/, isRed)).run(this);
        logPos("intakeTick approach (45," + y + ")@110");
        BotUtilsNew.driveAndRotateTo(BotUtilsNew.mirrorIfRed(new Vector3D(x, y, 0), isRed), BotUtilsNew.mirrorIfRed(90, isRed)).run(this);
        logPos("intakeTick pickup (" + x + "," + y + ")@90");
    }

    public void intakeTickFar() throws RobotException {
        intakeTick(15, 36);
    }
    public void intakeTickMiddle() throws RobotException {
        intakeTick(15, 57.6);
    }

    public void intakeTickClose() throws RobotException {
        BotUtilsNew.driveAndRotateTo(BotUtilsNew.mirrorIfRed(new Vector3D(19, 84, 0), isRed), BotUtilsNew.mirrorIfRed(90, isRed)).run(this);
        logPos("intakeTickClose (12,84)@90");
    }

    public void runStep(AutoStep step) throws RobotException {
        switch (step) {
            case NEAR: intakeTickClose(); break;
            case MIDDLE: intakeTickMiddle(); break;
            case FAR: intakeTickFar(); break;
            case OPEN_GATE: openGate(); break;
        }
    }

    private static final Vector3D SHOOT_POSITION = new Vector3D(50, 84, 0);
    private static final int SHOOT_HEADING = 90;

    public void shootFrom(Vector3D shootPos, double shootHeading, int turretWaitMs) throws RobotException {
        // Turret should already be preloaded from prior step; drive to shooting position
        this.aimOn = true;
        BotUtilsNew.driveAndRotateTo(BotUtilsNew.mirrorIfRed(shootPos, isRed), BotUtilsNew.mirrorIfRed(shootHeading, isRed), 6).run(this);
        logPos("shootFrom arrived (" + shootPos.x + "," + shootPos.y + ")@" + shootHeading);

        // Switch to live auto-aim and wait for turret/flywheel to settle
        overrideAngle = null;
        waitForTurret(turretWaitMs);

        // Shoot
        this.jamFix = true;
        this.block = false;
        BotUtilsNew.wait(1300).run(this);
        this.block = true;

        // Preload turret angle for next shot while we drive to intake
        preloadTurretForPosition(BotUtilsNew.mirrorIfRed(shootPos, isRed), BotUtilsNew.mirrorIfRed(shootHeading, isRed));
    }

    public void shootClose() throws RobotException {
        shootFrom(SHOOT_POSITION, SHOOT_HEADING, 1000);
    }

    public void openGate() throws RobotException {
        BotUtilsNew.driveAndRotateTo(BotUtilsNew.mirrorIfRed(new Vector3D(30, 66, 0), isRed), BotUtilsNew.mirrorIfRed(90, isRed)).run(this);
        logPos("openGate approach (25,72)@90");
        new TimeoutWrapperAction<>(BotUtilsNew.driveAndRotateTo(BotUtilsNew.mirrorIfRed(new Vector3D(9, 66, 0), isRed), BotUtilsNew.mirrorIfRed(90, isRed)), 500).run(this);
        logPos("openGate push (11,72)@90");
        BotUtilsNew.wait(800).run(this);
        BotUtilsNew.driveAndRotateTo(BotUtilsNew.mirrorIfRed(new Vector3D(40, 66, 0), isRed), BotUtilsNew.mirrorIfRed(90, isRed)).run(this);
    }

    public void updateGlobals() {
        Globals.POSITION = this.odometry.get();
    }

    public void runAutoSequence(AutoSequenceConfig config) throws RobotException {
        this.overrideDistance = BotUtilsNew.mirrorIfRed(config.shootPos, isRed).distance(targetGoal);
        logPos("AUTO START");
        preloadTurretForPosition(BotUtilsNew.mirrorIfRed(config.shootPos, isRed), BotUtilsNew.mirrorIfRed(config.shootHeading, isRed));

        boolean firstShot = true;
        for (AutoStep step : config.steps) {
            logPos("before " + step);
            if (step == AutoStep.SHOOT) {
                shootFrom(config.shootPos, config.shootHeading,
                    firstShot ? config.firstTurretWaitMs : config.turretWaitMs);
                firstShot = false;
            } else {
                runStep(step);
            }
            logPos("after " + step);
        }

        // Park
        BotUtilsNew.driveAndRotateTo(BotUtilsNew.mirrorIfRed(config.parkPos, isRed), 0).run(this);
        logPos("after final park");
        this.jamFix = false;
        this.aimOn = false;
    }

    public void runAuto() throws RobotException {
        runAutoSequence(new AutoSequenceConfig(
            SHOOT_POSITION, SHOOT_HEADING,
            new AutoStep[]{AutoStep.SHOOT, AutoStep.MIDDLE, AutoStep.SHOOT, AutoStep.NEAR, AutoStep.SHOOT, AutoStep.FAR, AutoStep.SHOOT},
            new Vector3D(24, 72, 0)
        ));
    }
}
