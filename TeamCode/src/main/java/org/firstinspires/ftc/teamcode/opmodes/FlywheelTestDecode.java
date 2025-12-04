package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "Flywheel Test Decode", group = "Flywheel")
public class FlywheelTestDecode extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        DcMotorEx motor = hardwareMap.get(DcMotorEx.class, "LFly");
        Servo feed = hardwareMap.get(Servo.class, "LFeed");
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motor.setVelocity(0);
        int s = 0;
        waitForStart();
        while (opModeIsActive()) {
            motor.setVelocity(s * 28 / 60.0);
            s += (int) (gamepad1.right_trigger - gamepad1.left_trigger);
            feed.setPosition(gamepad1.right_bumper ? 1 : 0.4);
            telemetry.addData("s", s);
            telemetry.addData("as", motor.getVelocity());
            telemetry.update();
        }
    }
}
