package com.buddyram.rframe.ftc.v2.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
@Disabled
@TeleOp(name = "Turret Auto Tuner", group = "Diagnostic")
public class TurretAutoTuner extends LinearOpMode {

    private static final double TICKS_PER_DEG = 28.0 * 363.0 / 20.0 / 360.0;
    private static final double MAX_POWER = 0.4;

    // Step test parameters
    private static final int SAMPLE_COUNT = 200;
    private static final int SAMPLE_INTERVAL_MS = 10; // 2 seconds of data
    // Ordered so direction reversals happen at larger angles (tests belt slip resistance)
    private static final double[] TEST_ANGLES = {30, -30, 60, 90, -90, 45, 135, -135, 160, -160};

    // Tuning thresholds
    private static final double SETTLE_THRESHOLD_DEG = 2.0;
    private static final double OVERSHOOT_LIMIT = 0.10;      // 10% overshoot is acceptable
    private static final double STEADY_ERROR_LIMIT_DEG = 3.0;

    enum State {
        WAITING,       // press cross to start tuning
        RETURNING,     // moving back to 0 before next test
        TESTING,       // recording step response
        ANALYZING,     // computing adjustments
        PAUSED,        // belt slipped or user paused — recenter manually
        DONE
    }

    @Override
    public void runOpMode() throws InterruptedException {
        DcMotorEx turret = hardwareMap.get(DcMotorEx.class, "turret");
        turret.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turret.setTargetPosition(0);
        turret.setPower(MAX_POWER);
        turret.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        PIDFCoefficients pidf = turret.getPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION);
        PIDFCoefficients startPidf = new PIDFCoefficients(pidf.p, pidf.i, pidf.d, pidf.f);

        // Start with the motor's default P, zero out I/D/F to tune from scratch
        // (keep default P since it's what the motor controller expects for this motor)
        pidf.p = 100;
        pidf.i = 0;
        pidf.d = 0;
        pidf.f = 0;
        turret.setPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION, pidf);

        State state = State.WAITING;
        int testIndex = 0;
        int iteration = 0;
        int maxIterations = 600;

        // Sample buffers
        int[] samples = new int[SAMPLE_COUNT];
        int sampleIdx = 0;
        long sampleTimer = 0;
        int targetTicks = 0;
        double targetAngle = 0;

        // Analysis results for display
        String lastAnalysis = "none yet";
        String lastAction = "";

        boolean prevCross = false, prevSquare = false, prevTriangle = false;

        telemetry.addLine("=== Turret Auto Tuner ===");
        telemetry.addLine("Press CROSS to start auto-tuning");
        telemetry.addLine("Press SQUARE anytime to pause & recenter");
        telemetry.addLine("Press TRIANGLE to accept current PIDF");
        telemetry.addData("Default PIDF", "P=%.2f I=%.4f D=%.2f F=%.2f",
                startPidf.p, startPidf.i, startPidf.d, startPidf.f);
        telemetry.addData("Starting PIDF", "P=%.2f I=%.4f D=%.2f F=%.2f",
                pidf.p, pidf.i, pidf.d, pidf.f);
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            boolean crossPressed = gamepad1.cross && !prevCross;
            boolean squarePressed = gamepad1.square && !prevSquare;
            boolean trianglePressed = gamepad1.triangle && !prevTriangle;
            prevCross = gamepad1.cross;
            prevSquare = gamepad1.square;
            prevTriangle = gamepad1.triangle;

            // --- PAUSE from any state ---
            if (squarePressed && state != State.PAUSED && state != State.WAITING) {
                turret.setPower(0);
                turret.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                state = State.PAUSED;
            }

            // --- ACCEPT current PIDF and stop ---
            if (trianglePressed && state != State.PAUSED) {
                state = State.DONE;
            }

            switch (state) {
                case WAITING:
                    telemetry.addLine("=== WAITING ===");
                    telemetry.addLine("Press CROSS to begin auto-tune");
                    telemetry.addData("Starting PIDF", "P=%.2f I=%.4f D=%.2f F=%.2f",
                            pidf.p, pidf.i, pidf.d, pidf.f);
                    if (crossPressed) {
                        turret.setPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION, pidf);
                        turret.setPower(MAX_POWER);
                        state = State.RETURNING;
                    }
                    break;

                case RETURNING:
                    turret.setTargetPosition(0);
                    telemetry.addLine("=== RETURNING TO ZERO ===");
                    telemetry.addData("Position", "%.1f deg", turret.getCurrentPosition() / TICKS_PER_DEG);
                    // Wait until near zero
                    if (Math.abs(turret.getCurrentPosition()) < anglesToTicks(SETTLE_THRESHOLD_DEG) + 2) {
                        Thread.sleep(300); // let it settle
                        // Start next test
                        targetAngle = TEST_ANGLES[testIndex % TEST_ANGLES.length];
                        targetTicks = anglesToTicks(targetAngle);
                        sampleIdx = 0;
                        sampleTimer = System.currentTimeMillis();
                        turret.setTargetPosition(targetTicks);
                        state = State.TESTING;
                    }
                    break;

                case TESTING:
                    telemetry.addLine("=== TESTING ===");
                    telemetry.addData("Iteration", "%d / %d", iteration + 1, maxIterations);
                    telemetry.addData("Test angle", "%.0f deg", targetAngle);
                    telemetry.addData("Position", "%.1f deg", turret.getCurrentPosition() / TICKS_PER_DEG);
                    telemetry.addData("Target", "%.1f deg", targetAngle);

                    // Collect samples
                    if (sampleIdx < SAMPLE_COUNT) {
                        long now = System.currentTimeMillis();
                        if (now - sampleTimer >= SAMPLE_INTERVAL_MS) {
                            samples[sampleIdx] = turret.getCurrentPosition();
                            sampleIdx++;
                            sampleTimer = now;
                        }
                    }

                    // Done collecting
                    if (sampleIdx >= SAMPLE_COUNT) {
                        state = State.ANALYZING;
                    }
                    break;

                case ANALYZING: {
                    // Analyze the step response
                    int startPos = samples[0];
                    double stepSize = targetTicks - startPos;

                    // Find overshoot
                    double maxOvershoot = 0;
                    int crossings = 0;
                    boolean wasAbove = false;
                    for (int i = 0; i < SAMPLE_COUNT; i++) {
                        double posFromTarget = samples[i] - targetTicks;
                        boolean isAbove = (stepSize > 0) ? posFromTarget > 0 : posFromTarget < 0;
                        double overshootAmount = Math.abs(posFromTarget);

                        if (isAbove && overshootAmount > maxOvershoot) {
                            maxOvershoot = overshootAmount;
                        }
                        if (i > 0 && isAbove != wasAbove) {
                            crossings++;
                        }
                        wasAbove = isAbove;
                    }
                    double overshootFrac = Math.abs(stepSize) > 0 ? maxOvershoot / Math.abs(stepSize) : 0;

                    // Steady state error (average of last 30 samples)
                    double ssSum = 0;
                    for (int i = SAMPLE_COUNT - 30; i < SAMPLE_COUNT; i++) {
                        ssSum += samples[i] - targetTicks;
                    }
                    double ssErrorTicks = ssSum / 30.0;
                    double ssErrorDeg = Math.abs(ssErrorTicks / TICKS_PER_DEG);

                    // Settling time: last sample index where error > threshold
                    int settleIdx = SAMPLE_COUNT;
                    double settleThreshTicks = SETTLE_THRESHOLD_DEG * TICKS_PER_DEG;
                    for (int i = SAMPLE_COUNT - 1; i >= 0; i--) {
                        if (Math.abs(samples[i] - targetTicks) > settleThreshTicks) {
                            settleIdx = i;
                            break;
                        }
                    }
                    int settleTimeMs = settleIdx * SAMPLE_INTERVAL_MS;
                    boolean settled = settleIdx < SAMPLE_COUNT - 10;

                    boolean oscillating = crossings >= 4;

                    // Build analysis string
                    lastAnalysis = String.format(
                        "overshoot=%.0f%% crossings=%d ssErr=%.1f deg settle=%dms",
                        overshootFrac * 100, crossings, ssErrorDeg, settleTimeMs);

                    // Check if motor barely moved at all (stalled/overdamped)
                    double totalMovement = Math.abs(samples[SAMPLE_COUNT - 1] - samples[0]);
                    boolean stalled = totalMovement < Math.abs(stepSize) * 0.15;

                    // --- Decide what to adjust ---
                    if (stalled) {
                        // Motor couldn't move — D is overdamping or P too low
                        if (pidf.d > 0) {
                            pidf.d *= 0.3;
                            lastAction = "Stalled: D*=0.3";
                        } else {
                            pidf.p *= 3;
                            lastAction = "Stalled: P*=3";
                        }
                    } else if (oscillating) {
                        // Too aggressive — reduce P, add D
                        pidf.p *= 0.8;
                        pidf.d += pidf.p * 0.1;
                        lastAction = "Oscillating: P*=0.8, D+=P*0.1";
                    } else if (overshootFrac > OVERSHOOT_LIMIT) {
                        // Overshooting — add D, slight P reduction
                        pidf.d += pidf.p * 0.05;
                        pidf.p *= 0.9;
                        lastAction = "Overshoot: D+=P*0.05, P*=0.9";
                    } else if (!settled) {
                        // Never settled — P might be too low
                        pidf.p *= 1.3;
                        lastAction = "Didn't settle: P*=1.3";
                    } else if (ssErrorDeg > STEADY_ERROR_LIMIT_DEG) {
                        // Steady-state error — add I
                        pidf.i += 0.005;
                        lastAction = "SS error: I+=0.005";
                    } else {
                        // Good enough — try the next test angle to confirm
                        lastAction = "GOOD — testing next angle";
                        testIndex++;

                        // If we've passed all test angles, we're done
                        if (testIndex >= TEST_ANGLES.length) {
                            state = State.DONE;
                            break;
                        }
                    }

                    // Clamp values — keep D from overdamping (max 30% of P)
                    pidf.p = Math.max(1.5, Math.min(pidf.p, 3000));
                    pidf.i = Math.max(0, Math.min(pidf.i, 0.1));
                    pidf.d = Math.max(0, Math.min(pidf.d, pidf.p * 0.3));

                    turret.setPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION, pidf);
                    turret.setPower(MAX_POWER);

                    iteration++;
                    if (iteration >= maxIterations) {
                        state = State.DONE;
                    } else {
                        state = State.RETURNING;
                    }
                    break;
                }

                case PAUSED:
                    telemetry.addLine("=== PAUSED — RECENTER THE TURRET ===");
                    telemetry.addLine("Manually align turret to center");
                    telemetry.addLine("Press CROSS when centered to reset & continue");
                    telemetry.addLine("Press TRIANGLE to accept current PIDF & stop");
                    if (crossPressed) {
                        // Reset encoder at current physical position = 0
                        turret.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                        turret.setTargetPosition(0);
                        turret.setPower(MAX_POWER);
                        turret.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                        turret.setPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION, pidf);
                        state = State.RETURNING;
                    }
                    if (trianglePressed) {
                        turret.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                        turret.setTargetPosition(0);
                        turret.setPower(MAX_POWER);
                        turret.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                        turret.setPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION, pidf);
                        state = State.DONE;
                    }
                    break;

                case DONE:
                    turret.setTargetPosition(0);
                    telemetry.addLine("=== TUNING COMPLETE ===");
                    telemetry.addData("Final PIDF", "P=%.2f  I=%.4f  D=%.2f  F=%.2f",
                            pidf.p, pidf.i, pidf.d, pidf.f);
                    telemetry.addData("Original PIDF", "P=%.2f  I=%.4f  D=%.2f  F=%.2f",
                            startPidf.p, startPidf.i, startPidf.d, startPidf.f);
                    telemetry.addData("Iterations", iteration);
                    telemetry.addLine("---");
                    telemetry.addLine("Copy these into BaseOpmode / Turret");
                    telemetry.addData("Position", "%.1f deg", turret.getCurrentPosition() / TICKS_PER_DEG);
                    break;
            }

            // Always show PIDF and state (except DONE/PAUSED which handle their own)
            if (state != State.DONE && state != State.PAUSED) {
                telemetry.addLine("---");
                telemetry.addData("PIDF", "P=%.2f  I=%.4f  D=%.2f  F=%.2f",
                        pidf.p, pidf.i, pidf.d, pidf.f);
                telemetry.addData("Last action", lastAction);
                telemetry.addData("Last analysis", lastAnalysis);
                telemetry.addLine("---");
                telemetry.addLine("SQUARE=pause/recenter  TRIANGLE=accept & stop");
            }

            telemetry.update();
        }
    }

    private int anglesToTicks(double angle) {
        return (int) Math.floor(angle * TICKS_PER_DEG);
    }
}
