package com.buddyram.rframe.ftc.intothedeep;

import com.buddyram.rframe.HolonomicDriveInstruction;
import com.buddyram.rframe.HolonomicDriveTrain;
import com.buddyram.rframe.Logger;
import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.Utils;
import com.buddyram.rframe.Vector3D;

import java.io.PrintStream;
import java.util.ArrayList;

public class AutonomousDrive {
    public static final double TARGET_POSITION_THRESHOLD = 1;
    public static final double TARGET_ROTATION_THRESHOLD = 0.5;
    public static final double SLOW_DISTANCE_THRESHOLD = 5;
    private final Logger logger;
    private final HolonomicDriveTrain drive;
    private final Odometry<Pose3D> odometry;

    public static void main(String[] args) {
        System.out.println("Starting autonomous simulator v1.1.1 ...");
        PrintLogger logger = new PrintLogger(System.out);
        VirtualHolonomicDriveTrain drive = new VirtualHolonomicDriveTrain(24, 10 / 27.1);
        VirtualOdometrySensor odometry = new VirtualOdometrySensor(
                new Pose3D( // pose
                    new Vector3D(36, 9, 0), // position
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

    public AutonomousDrive(Logger logger, HolonomicDriveTrain drive, Odometry<Pose3D> odometry) {
        this.odometry = odometry;
        this.logger = logger;
        this.drive = drive;
    }

    public void run() {
        ArrayList<double[]> positions = new ArrayList<double[]>();
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
        while (this.isActive() && !positions.isEmpty()) {
            if (this.navigate(positions.get(0))) {
                positions.remove(0);
                System.out.println("next!! " + positions.size());
            }
        }
    }

    public boolean isActive() {
        return true;
    }

    public void init() {
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
                    rotationInstruction = Math.min(1, rotationDiff / 5);
                } else {
                    rotationInstruction = -Math.min(1, Math.abs(rotationDiff) / 5);
                }
            }

            if (distanceToTarget >= TARGET_POSITION_THRESHOLD) {
                driveSpeedInstruction = distanceToTarget > SLOW_DISTANCE_THRESHOLD ? 1 : 0.5;
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
