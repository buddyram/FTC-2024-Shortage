package com.buddyram.rframe.ftc.decode;

import com.buddyram.rframe.GroundingOdometry;
import com.buddyram.rframe.Logger;
import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Utils;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.actions.MultiAction;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.drive.HolonomicDriveTrain;
import com.buddyram.rframe.drive.Navigatable;
import com.buddyram.rframe.ftc.ApriltagOdometry;
import com.buddyram.rframe.ftc.decode.action.ShootAction;
import com.buddyram.rframe.ftc.decode.indexer.Indexer;
import com.buddyram.rframe.ftc.decode.intake.Intake;
import com.buddyram.rframe.ftc.decode.launcher.Flywheel;
import com.buddyram.rframe.ftc.decode.launcher.Launcher;
import com.buddyram.rframe.ftc.decode.intake.Sweeper;
import com.buddyram.rframe.ftc.intothedeep.ShortageBot;
import com.buddyram.rframe.ftc.intothedeep.actions.RobotActions;

import java.util.ArrayList;

public class DecodeBot implements Navigatable<HolonomicDriveTrain> {
    public Logger logger;
    public Odometry<Pose3D> odometry;
    private static final Vector3D BLUE_GOAL = new Vector3D(12, 132, 0);
    private static final Vector3D RED_GOAL = new Vector3D(132, 132, 0);
    public boolean isRed;

    public Odometry<Pose3D> getApriltagOdometry() {
        return apriltagOdometry;
    }

    public Odometry<Pose3D> apriltagOdometry;
    public HolonomicDriveTrain drive;
    public Indexer indexer;


    public Intake getIntake() {
        return intake;
    }

    public Intake intake;

    public Launcher getLauncher() {
        return launcher;
    }

    public Launcher launcher;

    public Vector3D targetGoal;

    public DecodeBot() {
        this(null, null, null, null, null, null, false, null);
    }
    public DecodeBot(Logger logger, GroundingOdometry<Pose3D> odometry, HolonomicDriveTrain drive, Launcher launcher, Intake intake, Odometry<Pose3D> apriltagOdometry, boolean isRed, Indexer indexer) {
        this.init(logger, odometry, drive, launcher, intake, apriltagOdometry, isRed, indexer);
    }

    public void init(Logger logger, Odometry<Pose3D> odometry, HolonomicDriveTrain drive, Launcher launcher, Intake intake, Odometry<Pose3D> apriltagOdometry, boolean isRed, Indexer indexer) {
        this.logger = logger;
        this.odometry = odometry;
        this.drive = drive;
        this.launcher = launcher;
        this.intake = intake;
        this.apriltagOdometry = apriltagOdometry;
        this.isRed = isRed;
        this.targetGoal = isRed ? RED_GOAL : BLUE_GOAL;
        this.indexer = indexer;
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
    public class AdjustFlywheelSpeedAction implements RobotAction<DecodeBot>  {
        @Override
        public boolean run(DecodeBot drive) throws RobotException {
            drive.adjustFlywheelSpeed();
            return true;
        }
    }
    public void adjustFlywheelSpeed() {
        double dist = this.odometry.get().position.distance(this.targetGoal);
        this.getLauncher().wheel.setRPM((2800 + Math.pow(dist, 1.42)) / 2.15);
    }

    public static class AutoAimAction implements RobotAction<DecodeBot>  {
        @Override
        public boolean run(DecodeBot drive) throws RobotException {
            drive.autoAim();
            return true;
        }
    }
    public void autoAim() throws RobotException {
        Vector3D posToGoal = this.targetGoal.sub(this.odometry.get().position);
        double angle = (Math.toDegrees(Math.atan2(posToGoal.y, posToGoal.x)) - 90 - this.odometry.get().rotation.z) % 360;
        angle = angle > 180 ? angle - 360 : angle;

        this.launcher.turret.setAngle(-angle);
//        BotUtils.rotateTo(Math.toDegrees(Math.atan2(posToGoal.y, posToGoal.x)) - 90).run(this);
    }

    public HolonomicDriveInstruction calculateDriveInstruction(Vector3D target, double speed) {
        double rotationInstruction = 0, driveSpeedInstruction = 0, driveAngleInstruction = 0;
        Pose3D pos = this.odometry.get();
        driveSpeedInstruction = speed;
        driveAngleInstruction = pos.position.calculateRotation(target).z;

        return new HolonomicDriveInstruction(rotationInstruction, driveSpeedInstruction, driveAngleInstruction);
    }


    public void controlIntake() {
        if (indexer.isFull()) {
            this.intake.enableMode(Intake.Modes.IDLE);
        } else {
            if (indexer.isReady()) {
                this.intake.enableMode(Intake.Modes.INTAKING);
            }
            else {
                this.intake.enableMode(Intake.Modes.WAITING);
            }
        }
    }


    public MultiAction<DecodeBot> shootClose() {
        return new MultiAction<>(
                BotUtils.driveTo(
                        BotUtils.mirrorIfRed(new Vector3D(47, 87, 0), isRed),
                        false),
                new AdjustFlywheelSpeedAction(),
                new AutoAimAction(),
                new ShootAction()
        );
    }

    public MultiAction<DecodeBot> shootCloser() {
        return new MultiAction<>(
                BotUtils.driveTo(
                        BotUtils.mirrorIfRed(new Vector3D(36, 95, 0), isRed),
                        false),
                new AdjustFlywheelSpeedAction(),
                new AutoAimAction(),
                new ShootAction()
        );
    }
    public MultiAction<DecodeBot> shootFar() {
        return new MultiAction<>(
                BotUtils.driveTo(
                        BotUtils.mirrorIfRed(new Vector3D(52, 19, 0), isRed),
                        false),
                new AdjustFlywheelSpeedAction(),
                new AutoAimAction(),
                new ShootAction()
        );
    }


    public enum IntakeLocation {
        FAR,
        MEDIUM,
        CLOSE
    }

//    public MultiAction<DecodeBot> intake(IntakeLocation location, boolean intermediate) {
//        Vector3D preLocation;
//        Vector3D postLocation;
//        if (location == IntakeLocation.CLOSE) {
//            preLocation = new Vector3D(45, 84, 0);
//        } else if (location == IntakeLocation.MEDIUM) {
//            preLocation = new Vector3D(45, 60, 0);
//        } else {
//            preLocation = new Vector3D(45, 36, 0);
//        }
//        postLocation = preLocation.add(new Vector3D(-15, 0, 0));
//        preLocation = BotUtils.mirrorIfRed(preLocation, isRed);
//        postLocation = BotUtils.mirrorIfRed(postLocation, isRed);
//        if (intermediate) {
//            return new MultiAction<>(
//                    Sweeper.moveTo(-1),
//                    BotUtils.rotateTo(BotUtils.mirrorIfRed(90, isRed)),
//                    BotUtils.driveTo(preLocation, false),
//                    BotUtils.driveTo(postLocation, false),
//                    BotUtils.wait(150)
//            );
//        } else {
//            return new MultiAction<>(
//                    Sweeper.moveTo(-1),
//                    BotUtils.rotateTo(BotUtils.mirrorIfRed(90, isRed)),
//                    BotUtils.driveTo(postLocation, false),
//                    BotUtils.wait(150)
//            );
//        }
//    }
//
//    public MultiAction<DecodeBot> intake2 (IntakeLocation location) {
//        Vector3D driveLocation;
//        int dir;
//        // 31,59
//        //
//        if (location == IntakeLocation.CLOSE) {
//            dir = 90;
//            driveLocation = new Vector3D(45, 84, 0);
//
//        } else if (location == IntakeLocation.MEDIUM) {
//            dir = 45;
//            driveLocation = new Vector3D(45, 62, 0);
//        } else {
//            dir = 21;
//            driveLocation = new Vector3D(45, 39, 0);
//        }
//         return new MultiAction<>(Sweeper.moveTo(-1),
//                BotUtils.rotateTo(BotUtils.mirrorIfRed(dir, isRed)),
//                BotUtils.driveTo(driveLocation, false),
//                BotUtils.wait(150)
//         );
//    }
//
//
    public void runAutonomous() throws RobotException, InterruptedException {

        ArrayList<RobotAction<DecodeBot>> actions = new ArrayList<>();
//        actions.add(Flywheel.setRPMTo(3400));
//        actions.add(BotUtils.wait(200));
//
//        actions.add(this.shootFar());
//
//        actions.add(BotUtils.wait(200));
//        actions.add(Sweeper.moveTo(-1));
//        actions.add(BotUtils.wait(700));
//        actions.add(new ShootAction());
//        actions.add(this.intake(IntakeLocation.FAR, true));
//        actions.add(this.shootFar());
//        actions.add(this.intake(IntakeLocation.MEDIUM, true));
//        actions.add(this.shootClose());
//        actions.add(this.intake(IntakeLocation.CLOSE, false));
//        actions.add(this.shootClose());


//        actions.add(intake1);
//        actions.add(shootClose);
//        actions.add(intake2);
//        actions.add(shootClose);
//        actions.add(Flywheel.setRPMTo(3800));
//        actions.add(intake3);
//        actions.add(shootFar);
        actions.add(BotUtils.rotateTo(0));

        while (this.isActive() && !actions.isEmpty()) {
            if (actions.get(0).run(this)) {
                actions.remove(0);
                System.out.println("next!! " + actions.size());
            }
        }
    }

}
