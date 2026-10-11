package com.buddyram.rframe.ftc.biobuzz.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name = "Servo Test", group = "Diagnostic")
public class ServoTest extends LinearOpMode {

    private static final String SERVO_NAME = "blocker";
    private static final double STEP = 0.01;
    private static final double STICK_RATE_PER_S = 0.5;

    @Override
    public void runOpMode() {
        Servo servo = hardwareMap.get(Servo.class, SERVO_NAME);

        telemetry.addLine("=== Servo Test: " + SERVO_NAME + " ===");
        telemetry.addLine("Servo does not move until START (goes to 0.5)");
        telemetry.addLine("A / B / Y: 0.0 / 1.0 / 0.5");
        telemetry.addLine("D-pad up/down: step +/- " + STEP);
        telemetry.addLine("Left stick Y: smooth adjust");
        telemetry.update();
        waitForStart();

        double position = 0.5;
        boolean prevUp = false;
        boolean prevDown = false;
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive()) {
            double dt = timer.seconds();
            timer.reset();

            if (gamepad1.a) position = 0.0;
            if (gamepad1.b) position = 1.0;
            if (gamepad1.y) position = 0.5;

            if (gamepad1.dpad_up && !prevUp) position += STEP;
            if (gamepad1.dpad_down && !prevDown) position -= STEP;
            prevUp = gamepad1.dpad_up;
            prevDown = gamepad1.dpad_down;

            position += -gamepad1.left_stick_y * STICK_RATE_PER_S * dt;
            position = Range.clip(position, 0.0, 1.0);

            servo.setPosition(position);

            telemetry.addData("servo", SERVO_NAME);
            telemetry.addData("position", "%.3f", position);
            telemetry.update();
        }
    }
}
