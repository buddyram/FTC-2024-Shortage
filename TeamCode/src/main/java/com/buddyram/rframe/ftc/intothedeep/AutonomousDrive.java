package com.buddyram.rframe.ftc.intothedeep;

import com.buddyram.rframe.HolonomicDriveInstruction;
import com.buddyram.rframe.HolonomicDriveTrain;
import com.buddyram.rframe.Logger;
import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.Utils;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.ftc.ArmPositionalAction;
import com.buddyram.rframe.ftc.ConditionalWrapperAction;
import com.buddyram.rframe.ftc.DriveTowardsAction;
import com.buddyram.rframe.ftc.FrontBumperCondition;
import com.buddyram.rframe.ftc.MultiAction;
import com.buddyram.rframe.ftc.RobotAction;
import com.buddyram.rframe.ftc.RobotArm;
import com.buddyram.rframe.ftc.RobotActions;
import com.buddyram.rframe.ftc.RobotException;
import com.qualcomm.robotcore.hardware.DigitalChannel;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

public class AutonomousDrive {
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

    public AutonomousDrive(Logger logger, HolonomicDriveTrain drive, Odometry<Pose3D> odometry, RobotArm arm, DigitalChannel frontBumper) {
        this.odometry = odometry;
        this.logger = logger;
        this.drive = drive;
        this.arm = arm;
        this.frontBumper = frontBumper;
    }

    public void run() throws RobotException {
        ArrayList<RobotAction> actions = new ArrayList<RobotAction>();
//        actions.add(RobotArmActions.REST);
//        actions.add(new HangSpecimen(81));
//        actions.add(new DrivePositionAction(new double[]{105, 30, 0}));
//        actions.add(new DrivePositionAction(new double[]{105, 60, 0}));
//        actions.add(new DrivePositionAction(new double[]{117, 60, 0}));
//        actions.add(new DrivePositionAction(new double[]{117, 12, 0}));
//        actions.add(new DrivePositionAction(new double[]{96, 24, 0}));
//        actions.add(new DrivePositionAction(new double[]{96, 24, 180}));
//        actions.add(new DrivePositionAction(new double[]{96, 12, 180}));
//        for (int i = 0; i < 5; i++) {
//            actions.add(new GrabSpecimenFromWall(120));
//            actions.add(new HangSpecimen(81 - 2 * i));
//        }
//        actions.add(new DrivePositionAction(new double[]{120, 9, 0}));
        actions.add(RobotActions.COLLISION_HANG_RELEASE);
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

class PrintLogger implements Logger {
    PrintStream out;
    public PrintLogger(PrintStream out) {
        this.out = out;
    }
    public void log(String caption, Object value) {
        this.out.print("\r" + caption + ": " + value.toString() + "                  ");
    }

    public void flush() {
        //this.out.flush();
    }
}

class VirtualHolonomicDriveTrain implements HolonomicDriveTrain {
    final double maxSpeedInchesPerSecond;
    final double maxRotationsPerSecond;
    private HolonomicDriveInstruction lastInstruction;
    public VirtualHolonomicDriveTrain(double maxSpeedInchesPerSecond, double maxRotationsPerSecond) {
        this.maxSpeedInchesPerSecond = maxSpeedInchesPerSecond;
        this.maxRotationsPerSecond = maxRotationsPerSecond;
        this.lastInstruction = new HolonomicDriveInstruction(0, 0, 0);
    }
    public void drive(HolonomicDriveInstruction instruction) {
        this.lastInstruction = instruction;
    }

    public HolonomicDriveInstruction getLastInstruction() {
        return lastInstruction;
    }
}

class VirtualOdometrySensor implements Odometry<Pose3D> {
    private Pose3D pos;
    private long timestampMs;
    private final VirtualHolonomicDriveTrain driveTrain;
    public VirtualOdometrySensor(Pose3D initialPosition, VirtualHolonomicDriveTrain driveTrain) {
        this.pos = initialPosition;
        this.driveTrain = driveTrain;
    }

    public Pose3D get() {
        HolonomicDriveInstruction instruction = this.driveTrain.getLastInstruction();
        double seconds_past = (System.currentTimeMillis() - this.timestampMs) / 1000.0;
        Vector3D deltaPosition = new Vector3D(
                Math.cos(instruction.direction) * (this.driveTrain.maxSpeedInchesPerSecond * seconds_past * instruction.speed),
                Math.sin(instruction.direction) * (this.driveTrain.maxSpeedInchesPerSecond * seconds_past * instruction.speed),
                0
        );
        Vector3D deltaRotation = new Vector3D(
                0,
                0,
                360 * driveTrain.maxRotationsPerSecond * seconds_past * instruction.rotation

        );
        this.pos = new Pose3D(
                this.pos.position.add(deltaPosition),
                new Vector3D(
                        0,
                        0,
                        Utils.normalizeAngle(this.pos.rotation.add(deltaRotation).z)
                ),
                new Vector3D(),
                new Vector3D()
        );
        this.timestampMs = System.currentTimeMillis();
        return this.pos;
    }

    public boolean init() {
        this.timestampMs = System.currentTimeMillis();
        return true;
    }
}

class DrivePositionAction implements RobotAction {
    double[] position;

    public DrivePositionAction(double[] position) {
        this.position = position;
    }

    public boolean run(AutonomousDrive drive) {
        return drive.navigate(this.position);
    }
}

class HangSpecimen extends ArmPositionalAction {
    private final double x;
    public HangSpecimen(double x) {
        super(0, 0, 0, 0, 0);
        this.x = x;
    }

    public boolean run(AutonomousDrive drive) {
        RobotActions.SPECIMEN_HANG.runArm(drive.arm);
        new DrivePositionAction(new double[]{this.x, 39.5, 0}).run(drive);
        RobotActions.RELEASE_CLAW.runArm(drive.arm);
        RobotActions.REST.runArm(drive.arm);
        return true;
    }
}

class GrabSpecimenFromWall extends ArmPositionalAction {
    private final double x;
    public GrabSpecimenFromWall(double x) {
        super(0, 0, 0, 0, 0);
        this.x = x;
    }

    public boolean run(AutonomousDrive drive) {
        new DrivePositionAction(new double[]{this.x, 20, 180}).run(drive);
        RobotActions.GRAB_FROM_WALL.runArm(drive.arm);
        new DrivePositionAction(new double[]{this.x, 12, 180}).run(drive);
        RobotActions.CLOSE_CLAW.runArm(drive.arm);
        return true;
    }
}

/*
positions.add(new double[]{36, 9, 0}); // block 1 start
        positions.add(new double[]{36, 60, 0});
        positions.add(new double[]{24, 60, 0});
        positions.add(new double[]{24, 24, 0});
        positions.add(new double[]{24, 24, -45});
        positions.add(new double[]{15, 15, -45}); // block 1 end
        positions.add(new double[]{24, 24, -45}); // block 2 start
        positions.add(new double[]{24, 24, 0});
        positions.add(new double[]{24, 60, 0});
        positions.add(new double[]{14, 60, 0});
        positions.add(new double[]{14, 24, 0});
        positions.add(new double[]{10, 24, 0});
        positions.add(new double[]{12, 12, 0});
//        positions.add(new double[]{16, 60, 0}); // block 3 start
//        positions.add(new double[]{8, 60, 0});
//        positions.add(new double[]{8, 10, 0}); // block 3 end
        positions.add(new double[]{12, 24, 0});
        positions.add(new double[]{36, 60, 90});

//        positions.add(new double[]{48, 9, 0});
//        positions.add(new double[]{36, 12, 0});
//        positions.add(new double[]{36, 68, 0});
//        positions.add(new double[]{24, 68, 90});
//        positions.add(new double[]{24, 24, 90});
//        positions.add(new double[]{24, 24, 45});
//        positions.add(new double[]{15, 15, 45});
//        positions.add(new double[]{30, 30, 0});
//        positions.add(new double[]{24, 68, 0});
//        positions.add(new double[]{12, 68, 0});
//        positions.add(new double[]{12, 68, 90});
//        positions.add(new double[]{12, 24, 90});
//        positions.add(new double[]{20, 20, 0});
 */
