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

    public boolean init() {

        this.sensor.setLinearUnit(DistanceUnit.INCH);
        this.sensor.setAngularUnit(AngleUnit.DEGREES);
        // TODO: allow user to pass in offsets
//        SparkFunOTOS.Pose2D offset = new SparkFunOTOS.Pose2D(0.0781, -1.4418, 0);
//        this.sensor.setOffset(offset);
//        this.sensor.setLinearScalar((84 - (16 + 3.0 / 32.0)) / 70.11); // odometry reading after 84 inches forward
//        this.sensor.setAngularScalar(3600.0 / 3623.0); // odometry reading after 10 rotations.

//        SparkFunOTOS.Pose2D offset = new SparkFunOTOS.Pose2D(-5.508 / 2, 1.2976 / 2, 0);
//        this.sensor.setOffset(offset);
//        this.sensor.setLinearScalar(96 / 91.1082 * 48 / 50.8346);
//        this.sensor.setAngularScalar(3600 / 3608.5);

        SparkFunOTOS.Pose2D offset = new SparkFunOTOS.Pose2D(-2.33, -5.62, 90);
        this.sensor.setOffset(offset);
        this.sensor.setLinearScalar(48 / 49.2052);
        this.sensor.setAngularScalar(3600 / 3566.0);
//        this.sensor.setLinearScalar(0.8883); // use this -> 0.94752
//        this.sensor.setAngularScalar(1.0101010101);
//        x:118.34106253075788
//
//        y:78.68022576279527


//        this.sensor.setLinearScalar(60 / 72.5);
//        this.sensor.setAngularScalar(3565.4 / 3600);
//        SparkFunOTOS.Pose2D offset = new SparkFunOTOS.Pose2D(0, 0, 0);
//        this.sensor.setOffset(offset);
//        this.sensor.setLinearScalar(48.0 / 42.0); // 48.0 / 42.0
//        this.sensor.setAngularScalar(1);
//        this.sensor.calibrateImu(255, false);

        SparkFunOTOS.Pose2D currentPosition = new SparkFunOTOS.Pose2D(this.offset.position.x, this.offset.position.y, this.offset.rotation.z);
        this.sensor.resetTracking();
        this.sensor.setPosition(currentPosition);
        SparkFunOTOS.Version hwVersion = new SparkFunOTOS.Version();
        SparkFunOTOS.Version fwVersion = new SparkFunOTOS.Version();
        this.sensor.getVersionInfo(hwVersion, fwVersion);


        return true;
    }

    public void setPosition(Pose3D pos) {
        this.sensor.setPosition(new SparkFunOTOS.Pose2D(pos.position.x, pos.position.y, pos.rotation.z));
    }

    @Override
    public void cleanup() {
    }

    public Pose3D get() {
        SparkFunOTOS.Pose2D pose = sensor.getPosition();
        SparkFunOTOS.Pose2D velocityPose = sensor.getVelocity();
        Vector3D position = new Vector3D(pose.x, pose.y, 0);
        Vector3D rotation = new Vector3D(0, 0, pose.h);
        Vector3D positionVelocity = new Vector3D(velocityPose.x, velocityPose.y, 0);
        Vector3D rotationVelocity = new Vector3D(0, 0, velocityPose.h);
        return new Pose3D(position, rotation, positionVelocity, rotationVelocity);
    }
}
