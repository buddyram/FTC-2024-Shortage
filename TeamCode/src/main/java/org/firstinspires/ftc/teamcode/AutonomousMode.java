package org.firstinspires.ftc.teamcode;

import com.buddyram.rframe.HolonomicDriveInstruction;
import com.buddyram.rframe.MecanumDriveTrain;
import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.Utils;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.ftc.Motor;
import com.buddyram.rframe.ftc.SparkFunOTOSOdometry;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.hardware.DcMotor;

import java.util.ArrayList;

@Autonomous(name = "Autonomous - v1", group = "Sensor")
public class AutonomousMode extends LinearOpMode {
    Odometry<Pose3D> odometry;
    public static final double TARGET_POSITION_THRESHOLD = 1;
    public static final double TARGET_ROTATION_THRESHOLD = 2;
    MecanumDriveTrain drive;

    @Override
    public void runOpMode() throws InterruptedException {

        // get a reference to our touchSensor object.
        DcMotor motorFR = hardwareMap.get(DcMotor.class, "motorFR");
        DcMotor motorFL = hardwareMap.get(DcMotor.class, "motorFL");
        DcMotor motorBR = hardwareMap.get(DcMotor.class, "motorBR");
        DcMotor motorBL = hardwareMap.get(DcMotor.class, "motorBL");
        odometry = new SparkFunOTOSOdometry(
                hardwareMap.get(SparkFunOTOS.class, "otos"),
                new Pose3D( // pose
                        new Vector3D(48, 9, 0), // position
                        new Vector3D(0, 0, 0), // rotation
                        new Vector3D(0, 0, 0), // position velocity
                        new Vector3D(0, 0, 0)) // rotation velocity
        );
        if (odometry.init()) {
            telemetry.addData("initialized", "true");
            telemetry.update();
        }

        this.drive = new MecanumDriveTrain(
                new Motor(motorFL, -1),
                new Motor(motorFR, -1),
                new Motor(motorBL, -1),
                new Motor(motorBR),
                0.7
        );

        waitForStart();
        ArrayList<double[]> positions = new ArrayList<double[]>();
        positions.add(new double[]{48, 9, 0});
        positions.add(new double[]{36, 12, 0});
        positions.add(new double[]{36, 50, 0});
        positions.add(new double[]{24, 50, 0});
        positions.add(new double[]{24, 24, -90});
        positions.add(new double[]{20, 20, -135});
        positions.add(new double[]{24, 24, -135});
        positions.add(new double[]{24, 50, 0});
        positions.add(new double[]{12, 50, 0});
        positions.add(new double[]{12, 24, -90});
        positions.add(new double[]{20, 20, -135});
        while (opModeIsActive() && !positions.isEmpty()) {
            if (this.navigate(positions.get(0))) {
                positions.remove(0);
            }

        }
    }

    public boolean navigate(double[] position) {
        telemetry.addData("active", position);
        telemetry.update();
        boolean reachedPosition = false;
        double rotationInstruction = 0;
        double driveAngleInstruction = 0;
        double driveSpeedInstruction = 0;
        double distanceToTarget;
        double angleDiff;
        Vector3D target = new Vector3D(position[0], position[1], 0);

        while (!reachedPosition && opModeIsActive()) {
            Pose3D pos = odometry.get();
            distanceToTarget = pos.position.distance(target);
            reachedPosition = distanceToTarget < TARGET_POSITION_THRESHOLD;
            //(Utils.angleDifference(pos.rotation.z, position[2]) >= TARGET_ROTATION_THRESHOLD)
            angleDiff = Utils.angleDifference(pos.rotation.z, position[2]);
            if (Utils.angleDifference(pos.rotation.z, position[2]) >= TARGET_ROTATION_THRESHOLD) {
                rotationInstruction = TARGET_ROTATION_THRESHOLD;
            }

            distanceToTarget = pos.position.distance(target);

            if (distanceToTarget >= TARGET_POSITION_THRESHOLD) {
                driveSpeedInstruction = Math.min(0.5, distanceToTarget);
                driveAngleInstruction = 90 - pos.position.calculateRotation(target).z;
            }
            drive.drive(new HolonomicDriveInstruction(rotationInstruction, driveSpeedInstruction, driveAngleInstruction));
            telemetry.addData("x", pos.position.x);
            telemetry.addData("y", pos.position.y);
            telemetry.addData("h", pos.rotation.z);
            telemetry.addData("target x", position[0]);
            telemetry.addData("target y", position[1]);
            telemetry.addData("target h", position[2]);
            telemetry.addData("distance to target", distanceToTarget);
            telemetry.addData("drive angle instruction", driveAngleInstruction);
            telemetry.update();
        }
        return reachedPosition;
    }
}
//1/2.71