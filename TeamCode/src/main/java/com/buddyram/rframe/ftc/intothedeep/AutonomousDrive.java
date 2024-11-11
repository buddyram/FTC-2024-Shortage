package com.buddyram.rframe.ftc.intothedeep;

import com.buddyram.rframe.HolonomicDriveInstruction;
import com.buddyram.rframe.HolonomicDriveTrain;
import com.buddyram.rframe.Logger;
import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.Utils;
import com.buddyram.rframe.Vector3D;

import java.util.ArrayList;

public class AutonomousDrive {
    public static final double TARGET_POSITION_THRESHOLD = 1;
    public static final double TARGET_ROTATION_THRESHOLD = 2;
    private final Odometry<Pose3D> odometry;
    private final Logger logger;
    private final HolonomicDriveTrain drive;

    public static void main(String[] args) {
        System.out.println("Starting autonomous simulator v1.1.1 ...");
        PrintLogger logger = new PrintLogger();
        VirtualOdometrySensor odometry = new VirtualOdometrySensor(new Pose3D( // pose
                new Vector3D(48, 9, 0), // position
                new Vector3D(0, 0, 0), // rotation
                new Vector3D(0, 0, 0), // position velocity
                new Vector3D(0, 0, 0))
        );
        VirtualHolonomicDriveTrain drive = new VirtualHolonomicDriveTrain(odometry, 2, 10 / 27.1);
        AutonomousDrive main = new AutonomousDrive(odometry, logger, drive);
        Vector3D a = new Vector3D(0, 0, 0);
        Vector3D b = new Vector3D(3, 4, 0);
        Vector3D c = new Vector3D(2, 5, 0);
        Vector3D d = new Vector3D(1, 1, 0);
        System.out.println(a.calculateRotation(b));
        System.out.println(b.calculateRotation(c));
        System.out.println(c.calculateRotation(d));
        System.out.println("Initializing...");
        System.out.println("Initializing...");
        main.init();
        System.out.println("Initialized! Starting main program...");
        main.run();
    }

    public AutonomousDrive(Odometry<Pose3D> odometry, Logger logger, HolonomicDriveTrain drive) {
        this.odometry = odometry;
        this.logger = logger;
        this.drive = drive;
    }

    public void run() {
        ArrayList<double[]> positions = new ArrayList<double[]>();
        positions.add(new double[]{48, 9, 0});
        positions.add(new double[]{36, 12, 0});
        positions.add(new double[]{36, 68, 0});
        positions.add(new double[]{24, 68, 0});
        positions.add(new double[]{24, 24, -90});
        positions.add(new double[]{20, 20, -135});
        positions.add(new double[]{24, 24, -135});
        positions.add(new double[]{24, 68, 0});
        positions.add(new double[]{12, 68, 0});
        positions.add(new double[]{12, 24, -90});
        positions.add(new double[]{20, 20, -135});
        while (this.isActive() && !positions.isEmpty()) {
            if (this.navigate(positions.get(0))) {
                positions.remove(0);
            }

        }
    }

    public boolean isActive() {
        return true;
    }

    public void init() {
        if (odometry.init()) {
            logger.log("initialized", "true");
            this.logger.flush();
        }
    }

    public boolean navigate(double[] position) {
        this.logger.log("active", position);
        this.logger.flush();
        boolean reachedPosition = false;
        double rotationInstruction = 0;
        double driveAngleInstruction = 0;
        double driveSpeedInstruction = 0;
        double distanceToTarget;
        Vector3D target = new Vector3D(position[0], position[1], 0);

        while (!reachedPosition && isActive()) {
            Pose3D pos = odometry.get();
            distanceToTarget = pos.position.distance(target);
            reachedPosition = distanceToTarget < TARGET_POSITION_THRESHOLD;
            //(Utils.angleDifference(pos.rotation.z, position[2]) >= TARGET_ROTATION_THRESHOLD)
            if (Utils.angleDifference(pos.rotation.z, position[2]) >= TARGET_ROTATION_THRESHOLD) {

            }

            distanceToTarget = pos.position.distance(target);

            if (distanceToTarget >= TARGET_POSITION_THRESHOLD) {
                driveSpeedInstruction = Math.min(0.5, distanceToTarget);
                driveAngleInstruction = pos.position.calculateRotation(target).z;
            }
            drive.drive(new HolonomicDriveInstruction(rotationInstruction, driveSpeedInstruction, driveAngleInstruction));
            this.logger.log("x", pos.position.x);
            this.logger.log("y", pos.position.y);
            this.logger.log("h", pos.rotation.z);
            this.logger.log("target x", position[0]);
            this.logger.log("target y", position[1]);
            this.logger.log("target h", position[2]);
            this.logger.log("distance to target", distanceToTarget);
            this.logger.log("drive angle instruction", driveAngleInstruction);
            this.logger.flush();
        }
        return reachedPosition;
    }
}

class PrintLogger implements Logger {

    public void log(String caption, Object value) {
        System.out.println(caption + ": " + value.toString());
    }

    public void flush() {

    }
}

class VirtualHolonomicDriveTrain implements HolonomicDriveTrain {
    public final VirtualOdometrySensor odometry;
    public final double speedFeetPerSecond;
    public final double rotationsPerSecond;
    public VirtualHolonomicDriveTrain(VirtualOdometrySensor odometry, double speedFeetPerSecond, double rotationsPerSecond) {
        this.odometry = odometry;
        this.speedFeetPerSecond = speedFeetPerSecond;
        this.rotationsPerSecond = rotationsPerSecond;
    }
    public void drive(HolonomicDriveInstruction instruction) {

    }
}

class VirtualOdometrySensor implements Odometry<Pose3D> {
    private final Pose3D pos;
    public VirtualOdometrySensor(Pose3D initialPosition) {
        this.pos = initialPosition;

    }

    public Pose3D get() {
        return this.pos;
    }

    public boolean init() {
        return false;
    }
}
