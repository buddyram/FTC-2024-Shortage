package com.buddyram.rframe.ftc.biobuzz.opmodes;

import com.buddyram.rframe.ftc.biobuzz.Robot.launcher.Turret;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name = "Turret Class Test", group = "Diagnostic")
public class TurretClassTest extends LinearOpMode {

    private static final double NUDGE_DEG_PER_S = 120.0;

    @Override
    public void runOpMode() {
        // Turret centers itself when constructed
        Turret turret = new Turret(null, hardwareMap.get(ServoImplEx.class, "turret"));

        telemetry.addLine("=== Turret Class Test ===");
        telemetry.addLine("D-pad up/left/right: 0 / -90 / 90");
        telemetry.addLine("D-pad down: sweep to MAX, Y: sweep to MIN");
        telemetry.addLine("Left stick X: manual nudge");
        telemetry.update();
        waitForStart();

        double targetAngle = 0;
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive()) {
            double dt = timer.seconds();
            timer.reset();

            if (gamepad1.dpad_up) targetAngle = 0;
            if (gamepad1.dpad_left) targetAngle = -90;
            if (gamepad1.dpad_right) targetAngle = 90;
            if (gamepad1.dpad_down) targetAngle = Turret.MAX_ANGLE;
            if (gamepad1.y) targetAngle = Turret.MIN_ANGLE;

            targetAngle += -gamepad1.left_stick_x * NUDGE_DEG_PER_S * dt;
            targetAngle = Range.clip(targetAngle, Turret.MIN_ANGLE, Turret.MAX_ANGLE);

            turret.setAngle(targetAngle);

            telemetry.addData("target (deg)", "%.1f", targetAngle);
            telemetry.addData("current (deg)", "%.1f", turret.getCurrentAngle());
            telemetry.addData("ready", turret.isReady());
            telemetry.update();
        }
    }
}
