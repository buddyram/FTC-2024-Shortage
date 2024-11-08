/*
    SPDX-License-Identifier: MIT

    Copyright (c) 2024 SparkFun Electronics
*/
package org.firstinspires.ftc.robotcontroller.external.samples;

import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.ftc.SparkFunOTOSOdometry;
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

@Autonomous(name = "Sensor: SparkFun OTOSy", group = "Sensor")
public class OtosTesty extends LinearOpMode {
    Odometry<Pose3D> odometry;

    @Override
    public void runOpMode() throws InterruptedException {
        // Get a reference to the sensor
        DcMotor motorFR = hardwareMap.get(DcMotor.class, "motorFR");
        DcMotor motorFL = hardwareMap.get(DcMotor.class, "motorFL");
        DcMotor motorBR = hardwareMap.get(DcMotor.class, "motorBR");
        DcMotor motorBL = hardwareMap.get(DcMotor.class, "motorBL");
        MecanumBaseChassis robot = new MecanumBaseChassis(motorFL, motorFR, motorBL, motorBR);
        robot.setErrorCorrectionMultipliers(new double[]{-1, -1, 1, -1});
        this.odometry = new SparkFunOTOSOdometry(hardwareMap.get(SparkFunOTOS.class, "otos"), new Pose3D(new Vector3D(24, 9, 0), new Vector3D(0, 0, 0), new Vector3D(0, 0, 0), new Vector3D(0, 0, 0)));
        this.odometry.init();

        waitForStart();
        ArrayList<double[]> positions = new ArrayList<double[]>();
        positions.add(new double[]{48, 9, 0, 1});
        positions.add(new double[]{36, 12, 0, 1});
        positions.add(new double[]{36, 68, 0, 1});
        positions.add(new double[]{24, 68, 0, 1});
        positions.add(new double[]{24, 68, -90, 0});
        positions.add(new double[]{24, 24, -90, 1});
        positions.add(new double[]{24, 24, -135, 0});
        positions.add(new double[]{20, 20, -135, 1});
        positions.add(new double[]{24, 24, -135, 1});
        positions.add(new double[]{24, 24, 0, 0});
        positions.add(new double[]{24, 68, 0, 1});
        positions.add(new double[]{12, 68, 0, 1});
        positions.add(new double[]{12, 68, -90, 0});
        positions.add(new double[]{12, 24, -90, 1});
        positions.add(new double[]{12, 24, -135, 0});
        positions.add(new double[]{20, 20, -135, 1});
        // Loop until the OpMode ends
        double targetX = 0;
        double targetY = 0;
        double targetAngle = 0;
        double angle;
        double mode = 1;
        boolean reachedPosition = false;
        while (opModeIsActive() && !positions.isEmpty()) {
            // Get the latest position, which includes the x and y coordinates, plus the
            // heading angle
            Pose3D pos = odometry.get();

            // Log the position to the telemetry
            if (mode == 1) {
                angle = Math.toDegrees(Math.atan2(pos.position.x - targetY, pos.position.x - targetX)) - pos.rotation.z;
                robot.setDirection(angle);
                robot.setSpeed(0.2);
            } else {
                robot.setSpeed(0);
            }
            telemetry.addData("targetx", targetX);
            telemetry.addData("targety", targetY);
            telemetry.addData("targetang", targetAngle);
            telemetry.addData("x", pos.position.x);
            telemetry.addData("y", pos.position.y);
            telemetry.addData("z", pos.rotation.z);
            // Update the telemetry on the driver station
            telemetry.update();

            reachedPosition = (Math.sqrt(Math.pow(targetX - pos.position.x, 2) + Math.pow(targetY - pos.position.y, 2)) < 0.2 || mode == 0) && Math.abs(pos.rotation.z - targetAngle) < 5;
            if (reachedPosition) {
                positions.remove(0);
                targetX = positions.get(0)[0];
                targetY = positions.get(0)[1];
                targetAngle = positions.get(0)[2];
                mode = (positions.get(0)[3]);
            }
            if (Math.abs(pos.rotation.z - targetAngle) >= 5) {
                if (targetAngle - pos.rotation.z > 0) {
                    robot.setRobotDirection(1);
                } else {
                    robot.setRobotDirection(-1);
                }
            } else {
                robot.setRobotDirection(0);
            }
            //robot.update();
        }
    }
}