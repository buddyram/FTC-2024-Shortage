package org.firstinspires.ftc.teamcode.opmodes;

import com.buddyram.rframe.drive.HolonomicPositionDriveAdapter;
import com.buddyram.rframe.drive.MecanumDriveTrain;
import com.buddyram.rframe.ftc.Motor;
import com.buddyram.rframe.ftc.SparkFunOTOSOdometry;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public abstract class BaseOpmode extends LinearOpMode {
    public void initializeHardware() throws InterruptedException {
        DcMotor motorFR = hardwareMap.get(DcMotor.class, "DFR");
        DcMotor motorFL = hardwareMap.get(DcMotor.class, "DFL");
        DcMotor motorBR = hardwareMap.get(DcMotor.class, "DBR");
        DcMotor motorBL = hardwareMap.get(DcMotor.class, "DBL");
        SparkFunOTOSOdometry odometry = new SparkFunOTOSOdometry(
                hardwareMap.get(SparkFunOTOS.class, "TO"),
                org.firstinspires.ftc.teamcode.opmodes_into_the_deep.BaseOpmode.currentPosition
        );
        odometry.init();

        MecanumDriveTrain drive = new MecanumDriveTrain(
                new Motor(motorFL, -1),
                new Motor(motorFR, -1),
                new Motor(motorBL, -1),
                new Motor(motorBR, -1),
                1
        );
        HolonomicPositionDriveAdapter adapter = new HolonomicPositionDriveAdapter(drive, odometry);
        adapter.init();

        DcMotorEx motorFly = hardwareMap.get(DcMotorEx.class, "LFly");
        motorFly.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFly.setDirection(DcMotorSimple.Direction.REVERSE);
        motorFly.setVelocity(0);

        Servo servoFeed = hardwareMap.get(Servo.class, "LFeed");
        servoFeed.setPosition(0);
        CRServo servoInt = hardwareMap.get(CRServo.class, "IntS");
        servoInt.setPower(0);
    }
}
