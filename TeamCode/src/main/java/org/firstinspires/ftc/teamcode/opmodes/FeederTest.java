package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;

@TeleOp(name="Feeder", group="Concept")
public class FeederTest extends LinearOpMode {


    @Override
    public void runOpMode() throws InterruptedException {
        ServoImplEx feed = hardwareMap.get(ServoImplEx.class, "feed");
        feed.setPwmRange(new PwmControl.PwmRange(500, 2500));
        waitForStart();
        double d = 0;
        while (opModeIsActive()) {
//            if (gamepad1.dpad_up) {
//                d += 0.0001;
//            }
//            if (gamepad1.dpad_down) {
//                d -= 0.0001;
//            }
//            telemetry.addData("d", d);
//            telemetry.update();
            if (gamepad1.circle) {
                feed.setPosition(0.439);
                Thread.sleep(700);
                feed.setPosition(0.403);
                while (gamepad1.circle);
            }
        }
    }
}