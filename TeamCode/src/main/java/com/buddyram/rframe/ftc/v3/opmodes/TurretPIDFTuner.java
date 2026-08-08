package com.buddyram.rframe.ftc.v3.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

@TeleOp(name = "Turret PIDF Tuner", group = "Diagnostic")
public class TurretPIDFTuner extends LinearOpMode {

    // Turret: 28 CPR motor * 363/20 gear ratio = 508.2 ticks per revolution
    private static final double TICKS_PER_DEG = 28.0 * 363.0 / 20.0 / 360.0;
    private static final double MAX_POWER = 0.6;

    // PIDF adjustment increments
    private static final double P_STEP = 0.5;
    private static final double I_STEP = 0.01;
    private static final double D_STEP = 0.1;
    private static final double F_STEP = 0.5;

    // Target angle presets to cycle through
    private static final double[] TEST_ANGLES = {0, 45, 90, -45, -90, 30, -30, 60, -60};

    @Override
    public void runOpMode() throws InterruptedException {
        DcMotorEx turret = hardwareMap.get(DcMotorEx.class, "turret");
        turret.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turret.setTargetPosition(0);
        turret.setPower(MAX_POWER);
        turret.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        PIDFCoefficients pidf = turret.getPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION);

        // Which coefficient is selected: 0=P, 1=I, 2=D, 3=F
        int selected = 0;
        String[] labels = {"P", "I", "D", "F"};
        double[] steps = {P_STEP, I_STEP, D_STEP, F_STEP};

        int angleIndex = 0;
        double targetAngle = 0;

        // Debounce
        boolean prevDpadUp = false, prevDpadDown = false;
        boolean prevDpadLeft = false, prevDpadRight = false;
        boolean prevCross = false, prevCircle = false, prevTriangle = false, prevSquare = false;
        boolean prevRB = false, prevLB = false;

        telemetry.addLine("=== Turret PIDF Tuner ===");
        telemetry.addLine("GP1 dpad L/R: select P/I/D/F");
        telemetry.addLine("GP1 dpad U/D: adjust value");
        telemetry.addLine("GP1 cross/circle: cycle target angles");
        telemetry.addLine("GP1 square: go to 0");
        telemetry.addLine("GP1 triangle: apply PIDF");
        telemetry.addLine("GP1 bumpers: fine adjust power");
        telemetry.addData("Current PIDF", "P=%.2f I=%.4f D=%.2f F=%.2f", pidf.p, pidf.i, pidf.d, pidf.f);
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // --- Select coefficient with dpad left/right ---
            if (gamepad1.dpad_left && !prevDpadLeft) {
                selected = (selected - 1 + 4) % 4;
            }
            if (gamepad1.dpad_right && !prevDpadRight) {
                selected = (selected + 1) % 4;
            }

            // --- Adjust selected coefficient with dpad up/down ---
            if (gamepad1.dpad_up && !prevDpadUp) {
                adjustPIDF(pidf, selected, steps[selected]);
            }
            if (gamepad1.dpad_down && !prevDpadDown) {
                adjustPIDF(pidf, selected, -steps[selected]);
            }

            // --- Apply PIDF with triangle ---
            if (gamepad1.triangle && !prevTriangle) {
                turret.setPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION, pidf);
            }

            // --- Cycle target angles ---
            if (gamepad1.cross && !prevCross) {
                angleIndex = (angleIndex + 1) % TEST_ANGLES.length;
                targetAngle = TEST_ANGLES[angleIndex];
                turret.setTargetPosition(anglesToTicks(targetAngle));
            }
            if (gamepad1.circle && !prevCircle) {
                angleIndex = (angleIndex - 1 + TEST_ANGLES.length) % TEST_ANGLES.length;
                targetAngle = TEST_ANGLES[angleIndex];
                turret.setTargetPosition(anglesToTicks(targetAngle));
            }

            // --- Zero with square ---
            if (gamepad1.square && !prevSquare) {
                targetAngle = 0;
                angleIndex = 0;
                turret.setTargetPosition(0);
            }

            // --- Adjust max power with bumpers ---
            double power = turret.getPower();
            if (gamepad1.right_bumper && !prevRB) {
                power = Math.min(power + 0.05, 1.0);
                turret.setPower(power);
            }
            if (gamepad1.left_bumper && !prevLB) {
                power = Math.max(power - 0.05, 0.05);
                turret.setPower(power);
            }

            // --- Continuous target via right stick for manual positioning ---
            if (Math.abs(gamepad1.right_stick_x) > 0.1) {
                targetAngle += gamepad1.right_stick_x * 0.5;
                targetAngle = Math.max(-180, Math.min(180, targetAngle));
                turret.setTargetPosition(anglesToTicks(targetAngle));
            }

            // Save debounce state
            prevDpadUp = gamepad1.dpad_up;
            prevDpadDown = gamepad1.dpad_down;
            prevDpadLeft = gamepad1.dpad_left;
            prevDpadRight = gamepad1.dpad_right;
            prevCross = gamepad1.cross;
            prevCircle = gamepad1.circle;
            prevTriangle = gamepad1.triangle;
            prevSquare = gamepad1.square;
            prevRB = gamepad1.right_bumper;
            prevLB = gamepad1.left_bumper;

            // --- Telemetry ---
            int currentPos = turret.getCurrentPosition();
            int targetPos = turret.getTargetPosition();
            int error = targetPos - currentPos;
            double currentAngle = currentPos / TICKS_PER_DEG;

            telemetry.addLine("=== Turret PIDF Tuner ===");
            telemetry.addData("Selected", ">>> %s <<<", labels[selected]);
            telemetry.addData("PIDF", "P=%.2f  I=%.4f  D=%.2f  F=%.2f", pidf.p, pidf.i, pidf.d, pidf.f);
            telemetry.addLine("---");
            telemetry.addData("Target angle", "%.1f deg", targetAngle);
            telemetry.addData("Current angle", "%.1f deg", currentAngle);
            telemetry.addData("Target ticks", targetPos);
            telemetry.addData("Current ticks", currentPos);
            telemetry.addData("Error", "%d ticks (%.1f deg)", error, error / TICKS_PER_DEG);
            telemetry.addData("Power limit", "%.2f", turret.getPower());
            telemetry.addLine("---");
            telemetry.addLine("dpad L/R=select  U/D=adjust");
            telemetry.addLine("cross/circle=cycle  square=zero  tri=apply");
            telemetry.addLine("bumpers=power  R stick=manual");
            telemetry.update();
        }
    }

    private int anglesToTicks(double angle) {
        return (int) Math.floor(angle * TICKS_PER_DEG);
    }

    private void adjustPIDF(PIDFCoefficients pidf, int index, double delta) {
        switch (index) {
            case 0: pidf.p = Math.max(0, pidf.p + delta); break;
            case 1: pidf.i = Math.max(0, pidf.i + delta); break;
            case 2: pidf.d = Math.max(0, pidf.d + delta); break;
            case 3: pidf.f = Math.max(0, pidf.f + delta); break;
        }
    }
}
