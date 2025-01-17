package com.buddyram.rframe.drive;

import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.Utils;
import com.buddyram.rframe.Vector3D;

public class VirtualOdometrySensor implements Odometry<Pose3D> {
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
