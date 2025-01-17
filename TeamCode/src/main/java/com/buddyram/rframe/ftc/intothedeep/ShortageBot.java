package com.buddyram.rframe.ftc.intothedeep;

import com.buddyram.rframe.PrintLogger;
import com.buddyram.rframe.drive.Driveable;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.drive.HolonomicDriveTrain;
import com.buddyram.rframe.Logger;
import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.Utils;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.drive.VirtualHolonomicDriveTrain;
import com.buddyram.rframe.drive.VirtualOdometrySensor;
import com.buddyram.rframe.actions.ConditionalWrapperAction;
import com.buddyram.rframe.ftc.DriveTowardsAction;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.ftc.intothedeep.arm.RobotArm;
import com.buddyram.rframe.ftc.RobotActions;
import com.buddyram.rframe.RobotException;
import com.qualcomm.robotcore.hardware.DigitalChannel;

import java.util.ArrayList;

public class ShortageBot implements Driveable<HolonomicDriveTrain> {
    public static final double TARGET_POSITION_THRESHOLD = 1;
    public static final double TARGET_ROTATION_THRESHOLD = 1;
    public static final double SLOW_DISTANCE_THRESHOLD = 7;
    private final Logger logger;
    private final DigitalChannel frontBumper;

    public DigitalChannel getFrontBumper() {
        return frontBumper;
    }

    private final HolonomicDriveTrain drive;

    public HolonomicDriveTrain getDrive() {
        return drive;
    }


    public Odometry<Pose3D> getOdometry() {
        return odometry;
    }

    private final Odometry<Pose3D> odometry;
    public final RobotArm arm;

    public static void main(String[] args) {
        System.out.println("Starting autonomous simulator v1.1.1 ...");
        PrintLogger logger = new PrintLogger(System.out);
        VirtualHolonomicDriveTrain drive = new VirtualHolonomicDriveTrain(24, 10 / 27.1);
        VirtualOdometrySensor odometry = new VirtualOdometrySensor(
                new Pose3D( // pose
                    new Vector3D(96, 8.5, 0), // position
                    new Vector3D(0, 0, 0), // rotation
                    new Vector3D(0, 0, 0), // position velocity
                    new Vector3D(0, 0, 0)
                ),
                drive
        );
        //AutonomousDrive main = new AutonomousDrive(odometry, logger, drive);
//        System.out.println("Initializing...");
//        main.init();
//        System.out.println("Initialized! Starting main program...");
//        main.run();
    }

    public ShortageBot(Logger logger, HolonomicDriveTrain drive, Odometry<Pose3D> odometry, RobotArm arm, DigitalChannel frontBumper) {
        this.odometry = odometry;
        this.logger = logger;
        this.drive = drive;
        this.arm = arm;
        this.frontBumper = frontBumper;
    }

    public void run() throws RobotException, InterruptedException {
        ArrayList<RobotAction<ShortageBot>> actions = new ArrayList<>();

        // ACTIONS START HERE

        actions.add(new ConditionalWrapperAction<>(new DriveTowardsAction(new Vector3D(72, 12, 0), false), (drive) -> drive.getOdometry().get().position.x < 75));
        actions.add(RobotActions.COLLISION_HANG_RELEASE);

        // ACTIONS END HERE

        while (this.isActive() && !actions.isEmpty()) {
            if (actions.get(0).run(this)) {
                actions.remove(0);
                System.out.println("next!! " + actions.size());
            }
        }
    }

    public boolean isActive() {
        return true;
    }

    public void init() {
    }

    public HolonomicDriveInstruction calculateRelativeDriveInstruction(Vector3D relativeTarget) {
        return this.calculateDriveInstruction(relativeTarget.add(this.odometry.get().position));
    }

    public HolonomicDriveInstruction calculateDriveInstruction(Vector3D target) {
        double rotationInstruction = 0, driveSpeedInstruction = 0, driveAngleInstruction = 0;
        Pose3D pos = this.odometry.get();
        double distanceToTarget = pos.position.distance(target);
        driveSpeedInstruction = distanceToTarget > SLOW_DISTANCE_THRESHOLD ? 1 : 0.6;
        driveAngleInstruction = pos.position.calculateRotation(target).z;

        return new HolonomicDriveInstruction(rotationInstruction, driveSpeedInstruction, driveAngleInstruction);
    }

    public boolean navigate(double[] position) {
        this.logger.log("active", position);
        this.logger.flush();
        boolean reachedPosition = false;
        Vector3D target = new Vector3D(position[0], position[1], 0);

        while (!reachedPosition && isActive()) {
            double rotationInstruction = 0, driveSpeedInstruction = 0, driveAngleInstruction = 0;
            Pose3D pos = this.odometry.get();
            double distanceToTarget = pos.position.distance(target);
            double rotationDiff = Utils.angleDifference(pos.rotation.z, position[2]);
            reachedPosition = (distanceToTarget < TARGET_POSITION_THRESHOLD) && (Math.abs(rotationDiff) <= TARGET_ROTATION_THRESHOLD);
            if (Math.abs(rotationDiff) >= TARGET_ROTATION_THRESHOLD) {
                if (rotationDiff > 0) {
                    rotationInstruction = Math.min(0.5, rotationDiff / 20);
                } else {
                    rotationInstruction = -Math.min(0.5, Math.abs(rotationDiff) / 20);
                }
            }

            if (distanceToTarget >= TARGET_POSITION_THRESHOLD) {
                driveSpeedInstruction = distanceToTarget > SLOW_DISTANCE_THRESHOLD ? 1 : 0.6;
                driveAngleInstruction = pos.position.calculateRotation(target).z;
            }

            drive.drive(new HolonomicDriveInstruction(rotationInstruction, driveSpeedInstruction, driveAngleInstruction));
            this.logger.log("pose", pos);
            this.logger.log("target", "(" + position[0] + ", " + position[1] + ", " + position[2] + ")");
            this.logger.log("distance to target", distanceToTarget);
            this.logger.log("drive angle instruction", driveAngleInstruction);
            this.logger.log("rotation instruction", rotationInstruction);
            this.logger.log("diff instruction", rotationDiff);
            this.logger.flush();
        }
        return reachedPosition;
    }
}

