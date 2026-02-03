package com.buddyram.rframe.ftc.v2;

import com.buddyram.rframe.GroundingOdometry;
import com.buddyram.rframe.Logger;
import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.actions.MultiAction;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.drive.HolonomicDriveTrain;
import com.buddyram.rframe.drive.Navigatable;
import com.buddyram.rframe.ftc.DriveToAction;
import com.buddyram.rframe.ftc.decode.DecodeBot;
import com.buddyram.rframe.ftc.decode.Globals;
import com.buddyram.rframe.ftc.v2.Robot.intake.Intake;
import com.buddyram.rframe.ftc.v2.Robot.launcher.Launcher;

public class NewDecodeBot implements Navigatable<HolonomicDriveTrain> {
    public boolean block = true;
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
    public int speed = 0;
    public void adjustFlywheelSpeed() {
        double dist = this.odometry.get().position.distance(this.targetGoal);
        this.getLauncher().wheel.setRPM(2350.50621 * Math.pow(1.00529, dist) + speed);
        if (dist > 78) {
            this.launcher.hood.setAngle(0.55);
        } else {
            this.launcher.hood.setAngle(1);
        }
    }

    public double autoAim() {
        Vector3D posToGoal = this.targetGoal.sub(this.odometry.get().position.add(this.odometry.get().positionVelocity.mul(1)));
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
        this.launcher.blocker.setAngle(this.block ? this.launcher.blocker.OPEN : this.launcher.blocker.CLOSED);
        if (jamFix) {
            this.intake.enableMode(Intake.Modes.INTAKING);
            return;
        }
        else {
            this.intake.enableMode(Intake.Modes.IDLE);
        }
    }



    public void updateGlobals() {
        Globals.POSITION = this.odometry.get();
    }

    public void runAuto() throws RobotException {
        Vector3D shoot_close = new Vector3D(60, 84, 0);
        //19.5,72.25
        //12.8,118.5
        //15.2,74.8
        //8.5,75.2
        BotUtilsNew.driveTo(shoot_close, false).run(this);
        this.aimOn = true;
        BotUtilsNew.rotateTo(45).run(this);
        BotUtilsNew.wait(500).run(this);
        this.jamFix = true;
        this.block = false;
        BotUtilsNew.wait(1000).run(this);
        this.aimOn = false;
        this.block = true;
        BotUtilsNew.driveTo(new Vector3D(18, 84, 0), false).run(this);
        this.aimOn = true;
        BotUtilsNew.driveTo(shoot_close, false).run(this);
        BotUtilsNew.wait(500).run(this);
        this.jamFix = true;
        this.block = false;
        BotUtilsNew.wait(1000).run(this);
        this.jamFix = false;

    }
}
