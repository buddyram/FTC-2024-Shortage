package com.buddyram.rframe.drive;

import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;

public class HolonomicPositionDriveAdapter implements HolonomicDriveTrain {
    public final HolonomicDriveTrain driveTrain;
    public final Odometry<Pose3D> odometry;
    double heading;
    public HolonomicPositionDriveAdapter(HolonomicDriveTrain driveTrain, Odometry<Pose3D> odometry) {
        this.driveTrain = driveTrain;
        this.odometry = odometry;
    }

    public void drive(HolonomicDriveInstruction instruction) {
        if (instruction.speed == 0) {
            this.driveTrain.drive(instruction);
        } else {
            this.driveTrain.drive(
                    new HolonomicDriveInstruction(
                            instruction.rotation,
                            instruction.speed,
                            instruction.direction - heading
                    )
            );
        }
        this.heading = this.odometry.get().rotation.z;
    }

    public void init() {
        this.heading = this.odometry.get().rotation.z;
    }
}
