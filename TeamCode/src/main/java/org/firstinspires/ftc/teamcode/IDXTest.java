package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name="IDXTEST")
public class IDXTest extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        DcMotor indexMotor = hardwareMap.get(DcMotor.class, "idx");
        indexMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        indexMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        indexMotor.setPower(0);
        waitForStart();
        while (opModeIsActive()) {
            telemetry.addData("p", indexMotor.getCurrentPosition());
            telemetry.update();
        }
    }
}
