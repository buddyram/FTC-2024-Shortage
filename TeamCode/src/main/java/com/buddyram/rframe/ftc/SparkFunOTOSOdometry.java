package com.buddyram.rframe.ftc;

import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.Vector3D;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class SparkFunOTOSOdometry implements Odometry<Pose3D> {
    private final SparkFunOTOS sensor;
    private final Pose3D offset;

    public SparkFunOTOSOdometry(SparkFunOTOS sensor, Pose3D offset) {
        this.sensor = sensor;
        this.offset = offset;
    }

    public void init() {
        this.sensor.setLinearUnit(DistanceUnit.INCH);
        this.sensor.setAngularUnit(AngleUnit.DEGREES);
        // TODO: allow user to pass in offsets
        SparkFunOTOS.Pose2D offset = new SparkFunOTOS.Pose2D(0.0781, -1.4418, 0);
        this.sensor.setOffset(offset);
        this.sensor.setLinearScalar(1.9538461538);
        this.sensor.setAngularScalar(1.0);
        this.sensor.calibrateImu();
        this.sensor.resetTracking();
        SparkFunOTOS.Pose2D currentPosition = new SparkFunOTOS.Pose2D(0, 0, 0);
        this.sensor.setPosition(currentPosition);
        SparkFunOTOS.Version hwVersion = new SparkFunOTOS.Version();
        SparkFunOTOS.Version fwVersion = new SparkFunOTOS.Version();
        this.sensor.getVersionInfo(hwVersion, fwVersion);
    }

    public Pose3D get() {
        SparkFunOTOS.Pose2D pose = sensor.getPosition();
        SparkFunOTOS.Pose2D velocityPose = sensor.getVelocity();
        Vector3D position = new Vector3D(pose.x, pose.y, 0);
        Vector3D rotation = new Vector3D(0, 0, pose.h);
        Vector3D positionVelocity = new Vector3D(velocityPose.x, velocityPose.y, 0);
        Vector3D rotationVelocity = new Vector3D(0, 0, velocityPose.h);
        return new Pose3D(position, rotation, positionVelocity, rotationVelocity).add(this.offset);
    }
}
