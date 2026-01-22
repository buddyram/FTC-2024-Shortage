package com.buddyram.rframe.ftc.v2;

import com.buddyram.rframe.GroundingOdometry;
import com.buddyram.rframe.Logger;
import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.drive.HolonomicDriveTrain;
import com.buddyram.rframe.drive.Navigatable;
import com.buddyram.rframe.ftc.decode.Globals;
import com.buddyram.rframe.ftc.v2.Robot.intake.Intake;
import com.buddyram.rframe.ftc.v2.Robot.launcher.Launcher;

public class NewDecodeBot implements Navigatable<HolonomicDriveTrain> {
    public Logger logger;
    public Odometry<Pose3D> odometry;
    private static final Vector3D BLUE_GOAL = new Vector3D(12, 132, 0);
    private static final Vector3D RED_GOAL = new Vector3D(132, 132, 0);
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
    public void adjustFlywheelSpeed() {
        double dist = this.odometry.get().position.distance(this.targetGoal);
        this.getLauncher().wheel.setRPM((2800 + Math.pow(dist, 1.44)) / 2.25);
    }

    public double autoAim() {
        Vector3D posToGoal = this.targetGoal.sub(this.odometry.get().position);
        double angle = (Math.toDegrees(Math.atan2(posToGoal.y, posToGoal.x)) - 90 - this.odometry.get().rotation.z + turretOffset) % 360;
        if (angle < 0) angle += 360;
        angle = angle > 180 ? angle - 360 : angle;
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
        if (jamFix) {
            this.intake.enableMode(Intake.Modes.IDLE);
            return;
        }
    }



    public void updateGlobals() {
        Globals.POSITION = this.odometry.get();
    }

}
