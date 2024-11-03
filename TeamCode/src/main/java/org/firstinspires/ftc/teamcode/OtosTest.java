/*
    SPDX-License-Identifier: MIT

    Copyright (c) 2024 SparkFun Electronics
*/
package org.firstinspires.ftc.robotcontroller.external.samples;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.MecanumBaseChassis;

import java.util.ArrayList;

@Autonomous(name = "Sensor: SparkFun OTOS", group = "Sensor")
public class OtosTest extends LinearOpMode {
    // Create an instance of the sensor
    SparkFunOTOS myOtos;

    @Override
    public void runOpMode() throws InterruptedException {
        // Get a reference to the sensor
        DcMotor motorFR = hardwareMap.get(DcMotor.class, "motorFR");
        DcMotor motorFL = hardwareMap.get(DcMotor.class, "motorFL");
        DcMotor motorBR = hardwareMap.get(DcMotor.class, "motorBR");
        DcMotor motorBL = hardwareMap.get(DcMotor.class, "motorBL");
        MecanumBaseChassis robot = new MecanumBaseChassis(motorFL, motorFR, motorBL, motorBR);
        robot.setErrorCorrectionMultipliers(new double[]{1, -1, 1, -1});
        myOtos = hardwareMap.get(SparkFunOTOS.class, "otos");

        // All the configuration for the OTOS is done in this helper method, check it out!
        configureOtos();
        int startOffsetX = 24;
        int startOffsetY = 9;
        // Wait for the start button to be pressed
        waitForStart();
        ArrayList<double[]> positions = new ArrayList<double[]>();
        positions.add(new double[]{48 - startOffsetX, 9 - startOffsetY, 0, 1});
        positions.add(new double[]{36 - startOffsetX, 12 - startOffsetY, 0, 1});
        positions.add(new double[]{36 - startOffsetX, 68 - startOffsetY, 0, 1});
        positions.add(new double[]{24 - startOffsetX, 68 - startOffsetY, 0, 1});
        positions.add(new double[]{24 - startOffsetX, 68 - startOffsetY, -90, 0});
        positions.add(new double[]{24 - startOffsetX, 24 - startOffsetY, -90, 1});
        positions.add(new double[]{24 - startOffsetX, 24 - startOffsetY, -135, 0});
        positions.add(new double[]{20 - startOffsetX, 20 - startOffsetY, -135, 1});
        positions.add(new double[]{24 - startOffsetX, 24 - startOffsetY, -135, 1});
        positions.add(new double[]{24 - startOffsetX, 24 - startOffsetY, 0, 0});
        positions.add(new double[]{24 - startOffsetX, 68 - startOffsetY, 0, 1});
        positions.add(new double[]{12 - startOffsetX, 68 - startOffsetY, 0, 1});
        positions.add(new double[]{12 - startOffsetX, 68 - startOffsetY, -90, 0});
        positions.add(new double[]{12 - startOffsetX, 24 - startOffsetY, -90, 1});
        positions.add(new double[]{12 - startOffsetX, 24 - startOffsetY, -135, 0});
        positions.add(new double[]{20 - startOffsetX, 20 - startOffsetY, -135, 1});
        // Loop until the OpMode ends
        double targetX = 0;
        double targetY = 0;
        double targetAngle = 0;
        double angle;
        double mode = 1;
        boolean reachedPosition = false;
        while (opModeIsActive() && positions.isEmpty()) {
            // Get the latest position, which includes the x and y coordinates, plus the
            // heading angle
            SparkFunOTOS.Pose2D pos = myOtos.getPosition();
            // Log the position to the telemetry
            telemetry.addData("X coordinate", pos.x);
            telemetry.addData("Y coordinate", pos.y);
            telemetry.addData("Heading angle", pos.h);
            if (mode == 1) {
                angle = Math.toDegrees(Math.atan2(pos.y - targetY, pos.x - targetX)) - pos.h;
                robot.setDirection(angle);
                robot.setSpeed(1);
            } else {
                robot.setSpeed(0);
            }
            telemetry.addData("targetx", targetX);
            telemetry.addData("targety", targetY);
            telemetry.addData("targetang", targetAngle);
            // Update the telemetry on the driver station
            telemetry.update();

            reachedPosition = (Math.sqrt(Math.pow(targetX - pos.x, 2) + Math.pow(targetY - pos.y, 2)) < 0.2 || mode == 0) && Math.abs(pos.h - targetAngle) < 5;
            if (reachedPosition) {
                positions.remove(0);
                targetX = positions.get(0)[0];
                targetY = positions.get(0)[1];
                targetAngle = positions.get(0)[2];
                mode = (positions.get(0)[3]);
            }
            if (Math.abs(pos.h - targetAngle) >= 5) {
                if (targetAngle - pos.h > 0) {
                    robot.setRobotDirection(1);
                } else {
                    robot.setRobotDirection(-1);
                }
            } else {
                robot.setRobotDirection(0);
            }
            robot.update();
        }
    }

    private void configureOtos() {
        telemetry.addLine("Configuring OTOS...");
        telemetry.update();

        // myOtos.setLinearUnit(DistanceUnit.METER);
        myOtos.setLinearUnit(DistanceUnit.INCH);
        // myOtos.setAngularUnit(AnguleUnit.RADIANS);
        myOtos.setAngularUnit(AngleUnit.DEGREES);

        SparkFunOTOS.Pose2D offset = new SparkFunOTOS.Pose2D(3, 5, 0);
        myOtos.setOffset(offset);

        myOtos.setLinearScalar(1.9538461538);
        myOtos.setAngularScalar(1.0);

        myOtos.calibrateImu();

        // Reset the tracking algorithm - this resets the position to the origin,
        // but can also be used to recover from some rare tracking errors
        myOtos.resetTracking();

        // After resetting the tracking, the OTOS will report that the robot is at
        // the origin. If your robot does not start at the origin, or you have
        // another source of location information (eg. vision odometry), you can set
        // the OTOS location to match and it will continue to track from there.
        SparkFunOTOS.Pose2D currentPosition = new SparkFunOTOS.Pose2D(0, 0, 0);
        myOtos.setPosition(currentPosition);

        // Get the hardware and firmware version
        SparkFunOTOS.Version hwVersion = new SparkFunOTOS.Version();
        SparkFunOTOS.Version fwVersion = new SparkFunOTOS.Version();
        myOtos.getVersionInfo(hwVersion, fwVersion);

        telemetry.addLine("OTOS configured! Press start to get position data!");
        telemetry.addLine();
        telemetry.addLine(String.format("OTOS Hardware Version: v%d.%d", hwVersion.major, hwVersion.minor));
        telemetry.addLine(String.format("OTOS Firmware Version: v%d.%d", fwVersion.major, fwVersion.minor));
        telemetry.update();
    }
}