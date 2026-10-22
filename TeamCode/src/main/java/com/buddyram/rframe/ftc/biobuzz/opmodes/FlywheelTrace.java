package com.buddyram.rframe.ftc.biobuzz.opmodes;

import com.buddyram.rframe.ftc.RPMMotor;
import com.buddyram.rframe.ftc.biobuzz.Robot.launcher.Blocker;
import com.buddyram.rframe.ftc.biobuzz.Robot.launcher.Flywheel;
import com.buddyram.rframe.ftc.biobuzz.Robot.launcher.Hood;
import com.buddyram.rframe.ftc.biobuzz.Robot.launcher.Turret;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Records the flywheel as fast as the hub allows while shooting, so we can see exactly what a ball
 * does to the wheel and how soon we could notice it. The loop does nothing but read the flywheel:
 * raw encoder count, the hub's own velocity, and (SHARE to change) motor current.
 * Hold R bumper to shoot; each press is saved from 500 ms before to 1500 ms after as one CSV in
 * /sdcard/FIRST/flywheel_trace. Open them with tools/flywheel-trace-viewer.html.
 */
@TeleOp(name = "BIOBUZZ Flywheel Trace", group = "Biobuzz")
public class FlywheelTrace extends LinearOpMode {
    private static final String TAG = "FlywheelTrace";
    private static final int CPR = 28;
    private static final double INTAKE_SHOOT_POWER = 0.7;
    private static final double INTAKE_LOAD_POWER = 0.5;
    private static final double RPM_STEP = 50;
    private static final double HOOD_STEP = 0.01;
    private static final double PRE_MS = 500;   // saved before R bumper goes down
    private static final double POST_MS = 1500; // saved after R bumper comes up
    private static final int SIZE = 1 << 17;    // ring buffer, minutes of samples
    private static final int[] CURRENT_EVERY = {1, 4, 0}; // read current every n loops, 0 = never
    private static final long VOLTS_EVERY_MS = 250;

    // Ring buffer, written every loop
    private final double[] t = new double[SIZE];
    private final int[] pos = new int[SIZE];
    private final double[] hubRpm = new double[SIZE];
    private final double[] amps = new double[SIZE];
    private final float[] sp = new float[SIZE];
    private final float[] hoodAt = new float[SIZE];
    private final float[] volts = new float[SIZE];
    private final boolean[] feed = new boolean[SIZE];
    private long n; // samples written so far

    private File logDir;
    private String session;
    private int takes;
    private volatile String lastSaved = "none yet";

    @Override
    public void runOpMode() {
        DcMotorEx motor = hardwareMap.get(DcMotorEx.class, "LFly");
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, Flywheel.PIDF);
        RPMMotor rpmMotor = new RPMMotor(motor, CPR);
        rpmMotor.setRPM(0);

        DcMotor intake = hardwareMap.tryGet(DcMotor.class, "intake");
        Servo blockerServo = hardwareMap.tryGet(Servo.class, "blocker");
        Blocker blocker = blockerServo == null ? null : new Blocker(null, blockerServo);
        Servo hoodServo = hardwareMap.tryGet(Servo.class, "hood");
        Hood hood = hoodServo == null ? null : new Hood(null, hoodServo);
        ServoImplEx turretServo = hardwareMap.tryGet(ServoImplEx.class, "turret");
        if (turretServo != null) new Turret(null, turretServo); // centers the turret
        VoltageSensor battery = hardwareMap.voltageSensor.iterator().next();
        // AUTO: a bulk read only happens on the flywheel's hub, and only when we read it again next loop
        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }
        if (blocker != null) blocker.setAngle(blocker.OPEN);
        double hoodPos = BaseOpmode.START_HOOD;
        if (hood != null) hood.setAngle(hoodPos);
        setupLog();

        telemetry.addLine("=== Flywheel Trace ===");
        telemetry.addLine("CIRCLE flywheel on/off, hold R bumper to shoot, L bumper to load");
        telemetry.addLine("D-pad up/down RPM, left/right hood, SHARE current sampling");
        telemetry.addData("log", logDir != null ? logDir.getPath() : "unavailable");
        telemetry.update();
        waitForStart();

        Gamepad cur = new Gamepad();
        Gamepad prev = new Gamepad();
        double targetRpm = Flywheel.DEFAULT_RPM;
        double setpoint = 0;
        boolean flywheelOn = false;
        boolean rumbled = false; // one buzz per spin-up / volley, the speed wobbles in and out of the band
        int currentMode = 0;
        double lastVolts = battery.getVoltage();
        long lastVoltsMs = 0, lastTelemetryMs = 0;
        long takeStart = -1;      // sample index where the current take begins
        double releaseT = -1;     // when R bumper came up, -1 while held or idle
        long loops = 0;
        long rateStartN = 0;
        double rateStartT = 0, hz = 0;
        final long t0 = System.nanoTime();

        while (opModeIsActive()) {
            prev.copy(cur);
            cur.copy(gamepad1);

            // Controls
            if (cur.circle && !prev.circle) flywheelOn = !flywheelOn;
            if (cur.dpad_up && !prev.dpad_up) targetRpm = Range.clip(targetRpm + RPM_STEP, 500, Flywheel.MAX);
            if (cur.dpad_down && !prev.dpad_down) targetRpm = Range.clip(targetRpm - RPM_STEP, 500, Flywheel.MAX);
            if (cur.dpad_right && !prev.dpad_right) hoodPos = Range.clip(hoodPos + HOOD_STEP, 0, 1);
            if (cur.dpad_left && !prev.dpad_left) hoodPos = Range.clip(hoodPos - HOOD_STEP, 0, 1);
            if (cur.share && !prev.share) currentMode = (currentMode + 1) % CURRENT_EVERY.length;
            if (hood != null) hood.setAngle(hoodPos);
            double newSetpoint = flywheelOn ? targetRpm : 0;
            if (newSetpoint != setpoint) {
                setpoint = newSetpoint;
                rpmMotor.setRPM(setpoint);
                rumbled = false;
            }
            boolean shoot = cur.right_bumper;
            if (intake != null) intake.setPower(shoot ? INTAKE_SHOOT_POWER : cur.left_bumper ? INTAKE_LOAD_POWER : 0);
            if (blocker != null) blocker.setAngle(shoot ? blocker.CLOSED : blocker.OPEN);

            // Sample: time stamp is the middle of the bulk read
            long before = System.nanoTime();
            int p = motor.getCurrentPosition();
            double vel = motor.getVelocity();
            long after = System.nanoTime();
            int every = CURRENT_EVERY[currentMode];
            double a = every > 0 && loops % every == 0 ? motor.getCurrent(CurrentUnit.AMPS) : Double.NaN;
            long nowMs = System.currentTimeMillis();
            if (nowMs - lastVoltsMs > VOLTS_EVERY_MS) {
                lastVolts = battery.getVoltage();
                lastVoltsMs = nowMs;
            }
            int i = (int) (n % SIZE);
            double tMs = ((before + after) / 2 - t0) / 1e6;
            t[i] = tMs;
            pos[i] = p;
            hubRpm[i] = vel / CPR * 60;
            amps[i] = a;
            sp[i] = (float) setpoint;
            hoodAt[i] = (float) hoodPos;
            volts[i] = (float) lastVolts;
            feed[i] = shoot;
            n++;
            loops++;

            // Takes: one per R bumper press
            if (shoot) {
                if (takeStart < 0) takeStart = indexBefore(tMs - PRE_MS);
                releaseT = -1;
            } else if (takeStart >= 0) {
                if (releaseT < 0) releaseT = tMs;
                if (tMs - releaseT >= POST_MS) {
                    saveTake(takeStart, n);
                    takeStart = -1;
                    rumbled = false;
                    releaseT = -1;
                }
            }

            boolean ready = flywheelOn && Math.abs(hubRpm[i] - setpoint) < Flywheel.THRESHOLD;
            if (ready && !rumbled && takeStart < 0) {
                gamepad1.rumble(300);
                rumbled = true;
            }

            if (tMs - rateStartT >= 1000) {
                hz = (n - rateStartN) * 1000.0 / (tMs - rateStartT);
                rateStartN = n;
                rateStartT = tMs;
            }
            if (nowMs - lastTelemetryMs > 200) {
                lastTelemetryMs = nowMs;
                telemetry.addLine("=== Flywheel Trace ===");
                telemetry.addLine("R bumper shoot | L bumper load | CIRCLE flywheel | D-pad RPM / hood");
                telemetry.addData("state", !flywheelOn ? "OFF (CIRCLE)" : takeStart >= 0
                        ? (shoot ? "SHOOTING (recording)" : "recording after shot") : ready ? "READY" : "not at speed");
                telemetry.addData("target / rpm", "%.0f / %.0f", setpoint, hubRpm[i]);
                telemetry.addData("hood", "%.2f", hoodPos);
                telemetry.addData("current", every == 0 ? "off (SHARE)" : String.format(Locale.US,
                        "every %d loop%s (SHARE)", every, every == 1 ? "" : "s"));
                telemetry.addData("sample rate", "%.0f Hz (%.1f ms)", hz, hz > 0 ? 1000 / hz : 0);
                telemetry.addData("battery", "%.2f V", lastVolts);
                telemetry.addData("takes saved", "%d, last %s", takes, lastSaved);
                telemetry.addData("log", logDir != null ? logDir.getPath() : "unavailable");
                telemetry.update();
            }
        }

        if (takeStart >= 0) saveTake(takeStart, n);
        rpmMotor.setRPM(0);
        if (intake != null) intake.setPower(0);
    }

    /** First sample at or after tMs that is still in the buffer. */
    private long indexBefore(double tMs) {
        long k = n - 1;
        long oldest = Math.max(0, n - SIZE);
        while (k > oldest && t[(int) ((k - 1) % SIZE)] >= tMs) k--;
        return k;
    }

    private void setupLog() {
        session = new SimpleDateFormat("MMdd_HHmmss", Locale.US).format(new Date());
        File dir = new File(AppUtil.FIRST_FOLDER, "flywheel_trace");
        if (dir.isDirectory() || dir.mkdirs()) logDir = dir;
    }

    /** Copies samples [from, to) and writes them on a background thread so the loop never stalls. */
    private void saveTake(long from, long to) {
        if (logDir == null) return;
        from = Math.max(from, to - SIZE);
        final int count = (int) (to - from);
        final double[] ct = new double[count];
        final int[] cpos = new int[count];
        final double[] chub = new double[count];
        final double[] camps = new double[count];
        final float[] csp = new float[count];
        final float[] chood = new float[count];
        final float[] cvolts = new float[count];
        final boolean[] cfeed = new boolean[count];
        for (int k = 0; k < count; k++) {
            int i = (int) ((from + k) % SIZE);
            ct[k] = t[i];
            cpos[k] = pos[i];
            chub[k] = hubRpm[i];
            camps[k] = amps[i];
            csp[k] = sp[i];
            chood[k] = hoodAt[i];
            cvolts[k] = volts[i];
            cfeed[k] = feed[i];
        }
        final int take = ++takes;
        final File file = new File(logDir, String.format(Locale.US, "%s_t%03d.csv", session, take));
        new Thread(() -> {
            try (Writer w = new BufferedWriter(new FileWriter(file))) {
                w.write("t_ms,pos,hub_rpm,amps,setpoint_rpm,feeding,hood,battery_v\n");
                for (int k = 0; k < count; k++) {
                    w.write(String.format(Locale.US, "%.3f,%d,%.1f,%s,%.0f,%d,%.3f,%.2f\n",
                            ct[k], cpos[k], chub[k], Double.isNaN(camps[k]) ? "" : String.format(Locale.US, "%.3f", camps[k]),
                            csp[k], cfeed[k] ? 1 : 0, chood[k], cvolts[k]));
                }
                lastSaved = file.getName() + " (" + count + " samples)";
            } catch (IOException e) {
                RobotLog.ee(TAG, e, "could not write take " + take);
                lastSaved = "WRITE FAILED";
            }
        }).start();
    }
}
