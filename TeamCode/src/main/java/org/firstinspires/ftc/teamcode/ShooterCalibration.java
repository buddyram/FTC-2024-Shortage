package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
@TeleOp(name="Shooter ca", group="Concept")
public class ShooterCalibration extends LinearOpMode {

    // Declare OpMode member.
    private DcMotor motor = null;


    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addData("Status", "Initialized");
        motor = hardwareMap.get(DcMotor.class, "turret");
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);



        waitForStart();

        double k = 0;
        //

        while (opModeIsActive()) {
            telemetry.addData("p", motor.getCurrentPosition());
            // 21 / 56 * 360
            // 70
            // -65.8
            // 65
            // -60

            telemetry.addData("a", (21 / 56.0 * 360)/(-459.0) * motor.getCurrentPosition());
            telemetry.update();
        }
    }
}
