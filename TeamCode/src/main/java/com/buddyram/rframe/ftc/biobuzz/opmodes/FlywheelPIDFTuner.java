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
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Flywheel velocity PIDF tuner. Every test is a numbered "round" that is logged to telemetry,
 * RobotLog and CSV files in /sdcard/FIRST/flywheel_tuner (pull them with adb).
 *
 *  - SHOT round (CROSS): spins up, waits until the RPM holds steady, then you load 4 balls and
 *    press CROSS to fire. Feeds with the intake at 0.7, finds each shot's RPM dip and scores how
 *    consistent the 4 shots were (RPM right before each shot, how low each dip went).
 *  - STEP test (SQUARE, no balls): spin-up time, how steady the RPM holds (drift / noise), and how it
 *    recovers from a 500 RPM drop (overshoot, settle time), which is what happens between shots.
 *  - F calibration (TRIANGLE, no balls): runs the motor open loop at the target speed and measures
 *    the feedforward, F = 32767 * power / ticks per second.
 *
 * With auto-tune on (CIRCLE), each scored round moves one of P, I, D a step and keeps whatever scores
 * better (coordinate search). Shot and step rounds are scored on different scales, so each has its
 * own search. When happy, copy the PIDF into Flywheel.PIDF.
 */
@TeleOp(name = "BIOBUZZ Flywheel PIDF Tuner", group = "Biobuzz")
public class FlywheelPIDFTuner extends LinearOpMode {
    private static final String TAG = "FlywheelTuner";
    private static final int CPR = 28;
    private static final double INTAKE_SHOOT_POWER = 0.7;
    private static final double INTAKE_LOAD_POWER = 0.5;
    private static final int BALLS = 4;
    private static final double BAND_RPM = Flywheel.THRESHOLD; // "at speed" band, same as Flywheel.isReady()

    private static final long STABLE_MS = 500;          // must stay in band this long to count as steady
    private static final long SPINUP_TIMEOUT_MS = 8000;
    private static final long PRE_FIRE_MS = 300;        // steady samples kept in front of the shots
    private static final long FIRE_MAX_MS = 4000;       // feed window for the 4 balls
    private static final long AFTER_LAST_SHOT_MS = 500; // stop feeding this long after the 4th shot
    private static final long POST_FIRE_MS = 2500;      // keep recording the recovery after feeding
    private static final double MIN_DROP_RPM = 80;      // shot detection threshold floor
    private static final long PRE_SHOT_WINDOW_MS = 50;  // RPM "at the shot" is averaged over this window

    private static final long HOLD_MS = 4000;
    private static final double STEP_DOWN_RPM = 500;
    private static final long STEP_DOWN_MS = 1500;
    private static final long RECOVER_MS = 3000;

    private static final long FCAL_MAX_MS = 8000;
    private static final long FCAL_WINDOW_MS = 500;
    private static final double FCAL_STEADY_RPM = 10;
    private static final long FCAL_AVG_MS = 1000;

    private static final int HISTORY = 4;
    private static final String[] PARAMS = {"TARGET RPM", "P", "I", "D", "F", "HOOD"};
    private static final double[] PARAM_STEP = {50, 5, 0.1, 0.5, 0.25, 0.01};
    private static final String[] PARAM_FORMAT = {"%.0f", "%.1f", "%.3f", "%.2f", "%.3f", "%.3f"};

    private DcMotorEx motor;
    private RPMMotor rpmMotor;
    private DcMotor intake;
    private Blocker blocker;
    private Hood hood;
    private VoltageSensor battery;
    private List<LynxModule> hubs;

    private double targetRpm = Flywheel.DEFAULT_RPM;
    private double hoodPos = BaseOpmode.START_HOOD;
    private double setpoint = Double.NaN;
    private final PIDFCoefficients pidf = new PIDFCoefficients(
            Flywheel.PIDF.p, Flywheel.PIDF.i, Flywheel.PIDF.d, Flywheel.PIDF.f);

    private final Tuner shotTuner = new Tuner();
    private final Tuner stepTuner = new Tuner();
    private boolean autoTune = false;
    private int round = 0;
    private boolean aborted;
    private final LinkedList<String> history = new LinkedList<>();
    private List<String> lastResult = new ArrayList<>();

    private final Gamepad cur = new Gamepad();
    private final Gamepad prev = new Gamepad();
    private long holdStart, lastRepeat, lastTelemetryMs;

    private File logDir;
    private String session;

    @Override
    public void runOpMode() {
        motor = hardwareMap.get(DcMotorEx.class, "LFly");
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rpmMotor = new RPMMotor(motor, CPR);
        applyPidf();
        setTarget(0);

        intake = hardwareMap.tryGet(DcMotor.class, "intake");
        Servo blockerServo = hardwareMap.tryGet(Servo.class, "blocker");
        blocker = blockerServo == null ? null : new Blocker(null, blockerServo);
        Servo hoodServo = hardwareMap.tryGet(Servo.class, "hood");
        hood = hoodServo == null ? null : new Hood(null, hoodServo);
        ServoImplEx turretServo = hardwareMap.tryGet(ServoImplEx.class, "turret");
        if (turretServo != null) new Turret(null, turretServo); // centers the turret
        battery = hardwareMap.voltageSensor.iterator().next();
        hubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : hubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }
        setFeed(false);
        if (hood != null) hood.setAngle(hoodPos);
        setupLog();

        telemetry.addLine("=== Flywheel PIDF Tuner ===");
        telemetry.addLine("SHOT round: CROSS, load 4 balls, CROSS again to fire");
        telemetry.addLine("STEP test (no balls): SQUARE, F calibration (no balls): TRIANGLE");
        telemetry.addData("intake", intake != null ? "ok" : "MISSING");
        telemetry.addData("blocker", blocker != null ? "ok" : "MISSING");
        telemetry.addData("hood", hood != null ? "ok" : "MISSING");
        telemetry.addData("start PIDF", fmtPidf());
        telemetry.addData("log", logDir != null ? logDir.getPath() : "unavailable");
        telemetry.update();
        waitForStart();

        boolean spinning = false;
        int selected = 0;
        while (opModeIsActive()) {
            poll();
            if (pressed(cur.cross, prev.cross)) {
                runShotRound();
                spinning = true;
                continue;
            }
            if (pressed(cur.square, prev.square)) {
                runStepTest();
                spinning = true;
                continue;
            }
            if (pressed(cur.triangle, prev.triangle)) {
                runFCalibration();
                spinning = true;
                continue;
            }
            if (pressed(cur.circle, prev.circle)) autoTune = !autoTune;
            if (pressed(cur.share, prev.share)) spinning = !spinning;
            if (pressed(cur.options, prev.options)) revertToBest();
            if (pressed(cur.dpad_right, prev.dpad_right)) selected = (selected + 1) % PARAMS.length;
            if (pressed(cur.dpad_left, prev.dpad_left)) selected = (selected + PARAMS.length - 1) % PARAMS.length;
            int dir = repeat(cur.dpad_up, prev.dpad_up) ? 1 : repeat(cur.dpad_down, prev.dpad_down) ? -1 : 0;
            if (dir != 0) adjust(selected, dir);
            if (intake != null) intake.setPower(cur.left_bumper ? INTAKE_LOAD_POWER : 0);
            setTarget(spinning ? targetRpm : 0);
            showMenu(selected, spinning);
        }

        setTarget(0);
        setFeed(false);
        RobotLog.ii(TAG, "final PIDF " + fmtPidf() + bestText(" best SHOT ", shotTuner) + bestText(" best STEP ", stepTuner));
    }

    // ---------------------------------------------------------------- tests

    private void runShotRound() {
        int n = ++round;
        double[] tested = {pidf.p, pidf.i, pidf.d};
        double testedF = pidf.f;
        double volts = battery.getVoltage();
        Rec rec = new Rec();
        aborted = false;
        setTarget(targetRpm);

        // Spin up and wait until the RPM holds in the band
        double inBandSince = -1;
        double steadyAt = -1;
        while (rec.lastT() < SPINUP_TIMEOUT_MS) {
            if (!tick(rec, false, "SHOT #" + n + ": spinning up... (CIRCLE = cancel)")) {
                abort(n, "SHOT");
                return;
            }
            if (Math.abs(rec.lastRpm() - targetRpm) <= BAND_RPM) {
                if (inBandSince < 0) inBandSince = rec.lastT();
                if (rec.lastT() - inBandSince >= STABLE_MS) {
                    steadyAt = inBandSince;
                    break;
                }
            } else {
                inBandSince = -1;
            }
        }

        // Wait for the driver to fire
        while (true) {
            String status = "SHOT #" + n + ": READY" + (steadyAt < 0 ? " (never got steady!)" : "")
                    + ". Load " + BALLS + " balls, CROSS = FIRE (CIRCLE = cancel)";
            if (!tick(rec, false, status)) {
                abort(n, "SHOT");
                return;
            }
            if (pressed(cur.cross, prev.cross)) break;
            if (rec.size() > 2000) rec.dropBefore(rec.lastT() - 1000);
        }
        double fireT = rec.lastT();
        double noise = rec.stats(rec.indexAt(fireT - STABLE_MS), rec.size())[1];
        double dropRpm = Math.max(MIN_DROP_RPM, 5 * noise);
        rec.dropBefore(fireT - PRE_FIRE_MS);
        int fireIdx = rec.indexAt(fireT);

        // Fire: feed until all the shots are seen, or the window runs out
        setFeed(true);
        double nextCheck = 0;
        while (rec.lastT() - fireT < FIRE_MAX_MS) {
            if (!tick(rec, true, "SHOT #" + n + ": FIRING")) {
                abort(n, "SHOT");
                return;
            }
            if (rec.lastT() >= nextCheck) {
                nextCheck = rec.lastT() + 100;
                List<Shot> shots = detectShots(rec, fireIdx, dropRpm);
                if (shots.size() >= BALLS && rec.lastT() - shots.get(BALLS - 1).tMs > AFTER_LAST_SHOT_MS) break;
            }
        }
        setFeed(false);

        // Keep recording until it's back to steady
        double feedEnd = rec.lastT();
        inBandSince = -1;
        while (rec.lastT() - feedEnd < POST_FIRE_MS) {
            if (!tick(rec, false, "SHOT #" + n + ": recording recovery")) {
                abort(n, "SHOT");
                return;
            }
            if (Math.abs(rec.lastRpm() - targetRpm) <= BAND_RPM) {
                if (inBandSince < 0) inBandSince = rec.lastT();
                if (rec.lastT() - inBandSince >= STABLE_MS) break;
            } else {
                inBandSince = -1;
            }
        }

        // Score: RPM at each shot should match (and be on target), and each dip should go equally low
        List<Shot> shots = detectShots(rec, fireIdx, dropRpm);
        StringBuilder pre = new StringBuilder(), low = new StringBuilder(), back = new StringBuilder();
        double preMin = Double.MAX_VALUE, preMax = -Double.MAX_VALUE, lowMin = Double.MAX_VALUE, lowMax = -Double.MAX_VALUE;
        double preSum = 0;
        for (Shot s : shots) {
            pre.append(String.format(Locale.US, " %.0f", s.pre));
            low.append(String.format(Locale.US, " %.0f", s.low));
            back.append(s.recoverMs < 0 ? " -" : String.format(Locale.US, " %.0fms", s.recoverMs));
            preMin = Math.min(preMin, s.pre);
            preMax = Math.max(preMax, s.pre);
            lowMin = Math.min(lowMin, s.low);
            lowMax = Math.max(lowMax, s.low);
            preSum += s.pre;
        }
        boolean scored = shots.size() >= 2;
        double spread = scored ? preMax - preMin : 0;
        double lowSpread = scored ? lowMax - lowMin : 0;
        double meanErr = shots.isEmpty() ? 0 : preSum / shots.size() - targetRpm;
        double cost = scored ? spread + 0.5 * lowSpread + 0.5 * Math.abs(meanErr) : Double.NaN;

        String tuning = finishRound(shotTuner, tested, cost);
        String line1 = String.format(Locale.US, "#%d SHOT %s @%.0f %.1fV: %d shots, spread %.0f, err %+.0f, cost %s",
                n, fmtPidf(tested, testedF), targetRpm, volts, shots.size(), spread, meanErr, fmtCost(cost))
                + tuning;
        String line2 = "  at shot" + pre + " | low" + low + " | back in band" + back
                + (steadyAt < 0 ? " | NEVER STEADY before firing" : String.format(Locale.US, " | spin-up %.0fms", steadyAt))
                + (shots.size() != BALLS ? " | expected " + BALLS + " shots" : "")
                + String.format(Locale.US, " | noise sd %.0f", noise);
        List<String> result = resultHeader(n, "SHOT", tested, testedF, volts);
        for (int k = 0; k < shots.size(); k++) {
            Shot s = shots.get(k);
            result.add(String.format(Locale.US, "shot %d: at %.0f (%+.0f)  low %.0f  drop %.0f  back %s",
                    k + 1, s.pre, s.pre - targetRpm, s.low, s.pre - s.low,
                    s.recoverMs < 0 ? "-" : String.format(Locale.US, "%.0fms", s.recoverMs)));
        }
        if (shots.size() != BALLS) result.add("!! found " + shots.size() + " shots, expected " + BALLS);
        result.add(String.format(Locale.US, "at-shot spread %.0f | low spread %.0f | avg err %+.0f", spread, lowSpread, meanErr));
        result.add((steadyAt < 0 ? "spin-up NEVER STEADY" : String.format(Locale.US, "spin-up %.0fms", steadyAt))
                + String.format(Locale.US, " | noise sd %.0f | shot threshold %.0f", noise, dropRpm));
        result.add("cost " + fmtCost(cost) + tuning);
        lastResult = result;
        logRound(n, "SHOT", tested, testedF, volts, cost, line1, line2, rec);
    }

    private void runStepTest() {
        int n = ++round;
        double[] tested = {pidf.p, pidf.i, pidf.d};
        double testedF = pidf.f;
        double volts = battery.getVoltage();
        Rec rec = new Rec();
        aborted = false;
        double fromRpm = readRpm();
        setTarget(targetRpm);

        // Spin-up
        double inBandSince = -1;
        double steadyAt = -1;
        while (rec.lastT() < SPINUP_TIMEOUT_MS) {
            if (!tick(rec, false, "STEP #" + n + ": spinning up... (CIRCLE = cancel)")) {
                abort(n, "STEP");
                return;
            }
            if (Math.abs(rec.lastRpm() - targetRpm) <= BAND_RPM) {
                if (inBandSince < 0) inBandSince = rec.lastT();
                if (rec.lastT() - inBandSince >= STABLE_MS) {
                    steadyAt = inBandSince;
                    break;
                }
            } else {
                inBandSince = -1;
            }
        }

        // Hold: noise and drift with nothing touching the wheel
        int holdStart = rec.size();
        if (!runFor(rec, HOLD_MS, "STEP #" + n + ": holding steady")) {
            abort(n, "STEP");
            return;
        }
        double[] hold = rec.stats(holdStart, rec.size());

        // Drop and recover, like the wheel does between shots
        setTarget(targetRpm - STEP_DOWN_RPM);
        if (!runFor(rec, STEP_DOWN_MS, "STEP #" + n + ": dropped " + (int) STEP_DOWN_RPM)) {
            abort(n, "STEP");
            return;
        }
        setTarget(targetRpm);
        int recoverStart = rec.size();
        double t0 = rec.lastT();
        if (!runFor(rec, RECOVER_MS, "STEP #" + n + ": recovering")) {
            abort(n, "STEP");
            return;
        }
        double riseMs = -1, settleMs = 0, peak = -Double.MAX_VALUE;
        boolean inBand = false;
        for (int i = recoverStart; i < rec.size(); i++) {
            double dt = rec.t.get(i) - t0;
            double v = rec.rpm.get(i);
            inBand = Math.abs(v - targetRpm) <= BAND_RPM;
            if (inBand && riseMs < 0) riseMs = dt;
            if (!inBand) settleMs = dt;
            peak = Math.max(peak, v);
        }
        boolean settled = inBand && riseMs >= 0;
        if (!settled) settleMs = RECOVER_MS;
        double overshoot = Math.max(0, peak - targetRpm);
        double meanErr = hold[0] - targetRpm;
        double cost = overshoot + 2 * hold[1] + Math.abs(meanErr) + settleMs / 10;

        String tuning = finishRound(stepTuner, tested, cost);
        String line1 = String.format(Locale.US, "#%d STEP %s @%.0f %.1fV: cost %s",
                n, fmtPidf(tested, testedF), targetRpm, volts, fmtCost(cost))
                + tuning;
        String line2 = (steadyAt < 0
                ? String.format(Locale.US, "  spin-up from %.0f: NEVER STEADY", fromRpm)
                : String.format(Locale.US, "  spin-up from %.0f: %.0fms", fromRpm, steadyAt))
                + String.format(Locale.US, " | hold err %+.0f sd %.0f range %.0f drift %+.0f/s",
                        meanErr, hold[1], hold[3] - hold[2], hold[4])
                + String.format(Locale.US, " | -%.0f recover: rise %s overshoot %.0f settle %s",
                        STEP_DOWN_RPM, riseMs < 0 ? "never" : String.format(Locale.US, "%.0fms", riseMs),
                        overshoot, settled ? String.format(Locale.US, "%.0fms", settleMs) : "never");
        List<String> result = resultHeader(n, "STEP", tested, testedF, volts);
        result.add(steadyAt < 0
                ? String.format(Locale.US, "spin-up from %.0f: NEVER STEADY", fromRpm)
                : String.format(Locale.US, "spin-up from %.0f: %.0fms", fromRpm, steadyAt));
        result.add(String.format(Locale.US, "hold %ds: avg err %+.0f  sd %.0f", HOLD_MS / 1000, meanErr, hold[1]));
        result.add(String.format(Locale.US, "hold range %.0f to %.0f  drift %+.0f rpm/s", hold[2], hold[3], hold[4]));
        result.add(String.format(Locale.US, "-%.0f recover: rise %s  overshoot %.0f  settle %s",
                STEP_DOWN_RPM, riseMs < 0 ? "never" : String.format(Locale.US, "%.0fms", riseMs),
                overshoot, settled ? String.format(Locale.US, "%.0fms", settleMs) : "never"));
        result.add("cost " + fmtCost(cost) + tuning);
        lastResult = result;
        logRound(n, "STEP", tested, testedF, volts, cost, line1, line2, rec);
    }

    private void runFCalibration() {
        int n = ++round;
        double[] tested = {pidf.p, pidf.i, pidf.d};
        double oldF = pidf.f;
        double volts = battery.getVoltage();
        Rec rec = new Rec();
        aborted = false;
        double targetTicks = targetRpm * CPR / 60.0;
        double f = pidf.f > 0 ? pidf.f : 15;
        double power = 0;
        StringBuilder passes = new StringBuilder(" ");
        List<String> result = resultHeader(n, "F CAL", tested, oldF, volts);

        // Open loop at the power F predicts, measure the speed, correct F, then once more to confirm
        setpoint = Double.NaN;
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        for (int pass = 1; pass <= 2; pass++) {
            power = Range.clip(f * targetTicks / 32767.0, 0.05, 1.0);
            motor.setPower(power);
            String status = String.format(Locale.US, "F CAL #%d pass %d/2: power %.3f, waiting for steady RPM", n, pass, power);
            if (!waitSteady(rec, status)) {
                restoreVelocityMode();
                abort(n, "FCAL");
                return;
            }
            int a = rec.size();
            if (!runFor(rec, FCAL_AVG_MS, status)) {
                restoreVelocityMode();
                abort(n, "FCAL");
                return;
            }
            double measured = rec.stats(a, rec.size())[0];
            if (measured < 100) {
                restoreVelocityMode();
                String line = String.format(Locale.US, "#%d FCAL FAILED: %.0f rpm at power %.2f, is the wheel spinning?", n, measured, power);
                result.add("!! FAILED: " + String.format(Locale.US, "%.0f rpm at power %.2f", measured, power));
                result.add("is the wheel spinning / plugged in?");
                lastResult = result;
                logRound(n, "FCAL", tested, oldF, volts, Double.NaN, line, "", rec);
                return;
            }
            f = 32767.0 * power / (measured * CPR / 60.0);
            passes.append(String.format(Locale.US, " power %.3f -> %.0f rpm, F %.3f;", power, measured, f));
            result.add(String.format(Locale.US, "pass %d: power %.3f -> %.0f rpm -> F %.3f", pass, power, measured, f));
        }
        pidf.f = f;
        restoreVelocityMode();

        String line1 = String.format(Locale.US, "#%d FCAL @%.0f %.1fV: F %.3f -> %.3f", n, targetRpm, volts, oldF, f)
                + (power >= 1.0 ? " (FULL POWER, target is out of reach)" : "");
        result.add(String.format(Locale.US, "F %.3f -> %.3f (applied)", oldF, f));
        if (power >= 1.0) result.add("!! FULL POWER, target RPM is out of reach");
        lastResult = result;
        logRound(n, "FCAL", tested, f, volts, Double.NaN, line1, passes.toString(), rec);
    }

    // ---------------------------------------------------------------- analysis

    static class Shot {
        int startIdx, minIdx;
        double tMs, pre, low, recoverMs = -1;
    }

    /**
     * A shot is a drop of more than dropRpm below the highest RPM since the last dip. It ends once the
     * RPM climbs back up from its low, so back-to-back shots that come before full recovery still split.
     */
    static List<Shot> detectShots(Rec rec, int from, double dropRpm) {
        List<Shot> shots = new ArrayList<>();
        double riseRpm = dropRpm / 2;
        int maxIdx = from;
        Shot dip = null;
        for (int i = from; i < rec.size(); i++) {
            double v = rec.rpm.get(i);
            if (dip == null) {
                if (v >= rec.rpm.get(maxIdx)) maxIdx = i;
                if (v < rec.rpm.get(maxIdx) - dropRpm) {
                    dip = new Shot();
                    dip.startIdx = maxIdx;
                    dip.tMs = rec.t.get(maxIdx);
                    dip.pre = rec.stats(rec.indexAt(dip.tMs - PRE_SHOT_WINDOW_MS), maxIdx + 1)[0];
                    dip.minIdx = i;
                    dip.low = v;
                }
            } else {
                if (v < dip.low) {
                    dip.low = v;
                    dip.minIdx = i;
                }
                if (v > dip.low + riseRpm) {
                    shots.add(dip);
                    dip = null;
                    maxIdx = i;
                }
            }
        }
        if (dip != null) shots.add(dip);

        // Time from the start of each shot until back in band, unless the next shot came first
        for (int k = 0; k < shots.size(); k++) {
            Shot s = shots.get(k);
            int end = k + 1 < shots.size() ? shots.get(k + 1).startIdx : rec.size();
            for (int i = s.minIdx; i < end; i++) {
                if (Math.abs(rec.rpm.get(i) - rec.sp.get(i)) <= BAND_RPM) {
                    s.recoverMs = rec.t.get(i) - s.tMs;
                    break;
                }
            }
        }
        return shots;
    }

    /** Samples of one round. Times are ms since the round started. */
    static class Rec {
        final long t0 = System.nanoTime();
        final ArrayList<Double> t = new ArrayList<>();
        final ArrayList<Double> sp = new ArrayList<>();
        final ArrayList<Double> rpm = new ArrayList<>();
        final ArrayList<Boolean> feeding = new ArrayList<>();

        void add(double setpoint, double r, boolean feed) {
            t.add((System.nanoTime() - t0) / 1e6);
            sp.add(setpoint);
            rpm.add(r);
            feeding.add(feed);
        }

        int size() {
            return t.size();
        }

        double lastT() {
            return t.isEmpty() ? 0 : t.get(t.size() - 1);
        }

        double lastRpm() {
            return rpm.isEmpty() ? 0 : rpm.get(rpm.size() - 1);
        }

        int indexAt(double tMs) {
            for (int i = 0; i < t.size(); i++) {
                if (t.get(i) >= tMs) return i;
            }
            return t.size();
        }

        void dropBefore(double tMs) {
            int idx = indexAt(tMs);
            t.subList(0, idx).clear();
            sp.subList(0, idx).clear();
            rpm.subList(0, idx).clear();
            feeding.subList(0, idx).clear();
        }

        /** {mean, std dev, min, max, slope in RPM per second} of the RPM over [a, b). */
        double[] stats(int a, int b) {
            int n = b - a;
            if (n <= 0) return new double[]{0, 0, 0, 0, 0};
            double sum = 0, min = Double.MAX_VALUE, max = -Double.MAX_VALUE, tSum = 0;
            for (int i = a; i < b; i++) {
                double v = rpm.get(i);
                sum += v;
                tSum += t.get(i);
                min = Math.min(min, v);
                max = Math.max(max, v);
            }
            double mean = sum / n, tMean = tSum / n;
            double var = 0, cov = 0, tVar = 0;
            for (int i = a; i < b; i++) {
                double dv = rpm.get(i) - mean, dt = t.get(i) - tMean;
                var += dv * dv;
                cov += dt * dv;
                tVar += dt * dt;
            }
            double slope = tVar > 0 ? cov / tVar * 1000 : 0;
            return new double[]{mean, Math.sqrt(var / n), min, max, slope};
        }
    }

    /**
     * Coordinate search over P, I, D (F comes from the F calibration): step one term, keep it while
     * it scores better, try the other direction when it doesn't, then shrink the step and move on.
     */
    private static class Tuner {
        private static final String[] NAMES = {"P", "I", "D"};
        private static final double[] MAX = {2000, 100, 100};
        private static final double[] MIN_STEP = {2, 0.05, 0.25};
        double[] best;
        double bestCost = Double.NaN;
        final double[] step = new double[3];
        int param = 0;
        int dir = 1;
        boolean flipped = false;
        String note = "";

        /** Records a scored round and returns the P, I, D to try next. */
        double[] report(double[] tested, double cost) {
            if (best == null) {
                best = tested.clone();
                bestCost = cost;
                step[0] = Math.max(20, 0.2 * tested[0]);
                step[1] = Math.max(0.5, 0.2 * tested[1]);
                step[2] = Math.max(1, 0.2 * tested[2]);
                note = "baseline";
            } else if (Arrays.equals(tested, best)) {
                // Same values again: average the scores, they're noisy
                bestCost = (bestCost + cost) / 2;
                note = "re-measured best";
            } else if (cost < bestCost - Math.max(3, 0.03 * bestCost)) {
                note = String.format(Locale.US, "beat %.0f, keep going", bestCost);
                best = tested.clone();
                bestCost = cost;
                step[param] *= 1.5;
                flipped = false;
            } else if (!flipped) {
                note = "not better, try " + NAMES[param] + " the other way";
                dir = -dir;
                flipped = true;
            } else {
                note = "not better either way, next term";
                step[param] = Math.max(MIN_STEP[param], step[param] * 0.5);
                param = (param + 1) % 3;
                dir = 1;
                flipped = false;
            }
            double[] next = best.clone();
            next[param] = Range.clip(best[param] + dir * step[param], 0, MAX[param]);
            if (next[param] == best[param]) {
                dir = -dir;
                next[param] = Range.clip(best[param] + dir * step[param], 0, MAX[param]);
            }
            return next;
        }
    }

    /** Feeds a scored round to the tuner and, with auto-tune on, applies its next guess. */
    private String finishRound(Tuner tuner, double[] tested, double cost) {
        if (Double.isNaN(cost)) return " (not scored, need 2+ shots)";
        double[] next = tuner.report(tested, cost);
        if (!autoTune) return " (" + tuner.note + ")";
        pidf.p = next[0];
        pidf.i = next[1];
        pidf.d = next[2];
        applyPidf();
        return " (" + tuner.note + ") -> next " + fmtPidf();
    }

    private void revertToBest() {
        Tuner tuner = shotTuner.best != null ? shotTuner : stepTuner.best != null ? stepTuner : null;
        double[] values = tuner != null ? tuner.best : new double[]{Flywheel.PIDF.p, Flywheel.PIDF.i, Flywheel.PIDF.d};
        pidf.p = values[0];
        pidf.i = values[1];
        pidf.d = values[2];
        applyPidf();
        String line = "reverted to " + (tuner == shotTuner ? "best SHOT " : tuner == stepTuner ? "best STEP " : "start ") + fmtPidf();
        history.addFirst(line);
        while (history.size() > HISTORY) history.removeLast();
        RobotLog.ii(TAG, line);
    }

    // ---------------------------------------------------------------- loop helpers

    /** One sample. Returns false when the round should stop (CIRCLE or opmode stopped). */
    private boolean tick(Rec rec, boolean feed, String status) {
        poll();
        double rpm = readRpm();
        rec.add(setpoint, rpm, feed);
        long now = System.currentTimeMillis();
        if (now - lastTelemetryMs > 100) {
            lastTelemetryMs = now;
            telemetry.addLine(status);
            telemetry.addData("target / rpm", "%.0f / %.0f (%+.0f)", setpoint, rpm, rpm - setpoint);
            telemetry.addData("PIDF", fmtPidf());
            telemetry.addData("auto-tune", autoTune ? "ON" : "off");
            telemetry.update();
        }
        if (pressed(cur.circle, prev.circle)) aborted = true;
        return opModeIsActive() && !aborted;
    }

    private boolean runFor(Rec rec, long ms, String status) {
        double start = rec.lastT();
        while (rec.lastT() - start < ms) {
            if (!tick(rec, false, status)) return false;
        }
        return true;
    }

    /** Waits until the average RPM stops changing between FCAL_WINDOW_MS windows. */
    private boolean waitSteady(Rec rec, String status) {
        double start = rec.lastT();
        double prevMean = Double.NaN;
        int windowStart = rec.size();
        while (rec.lastT() - start < FCAL_MAX_MS) {
            if (!tick(rec, false, status)) return false;
            if (rec.lastT() - rec.t.get(windowStart) >= FCAL_WINDOW_MS) {
                double mean = rec.stats(windowStart, rec.size())[0];
                if (!Double.isNaN(prevMean) && Math.abs(mean - prevMean) < FCAL_STEADY_RPM) return true;
                prevMean = mean;
                windowStart = rec.size();
            }
        }
        return true;
    }

    private void abort(int n, String kind) {
        setFeed(false);
        String line = "#" + n + " " + kind + " cancelled";
        history.addFirst(line);
        while (history.size() > HISTORY) history.removeLast();
        RobotLog.ii(TAG, line);
    }

    private void poll() {
        prev.copy(cur);
        cur.copy(gamepad1);
    }

    private static boolean pressed(boolean now, boolean before) {
        return now && !before;
    }

    /** Press, then auto-repeat while held. */
    private boolean repeat(boolean now, boolean before) {
        long t = System.currentTimeMillis();
        if (now && !before) {
            holdStart = t;
            lastRepeat = t;
            return true;
        }
        if (now && t - holdStart > 400 && t - lastRepeat > 80) {
            lastRepeat = t;
            return true;
        }
        return false;
    }

    private double readRpm() {
        for (LynxModule hub : hubs) {
            hub.clearBulkCache();
        }
        return rpmMotor.getRPM();
    }

    private void setTarget(double rpm) {
        if (rpm == setpoint) return;
        setpoint = rpm;
        rpmMotor.setRPM(rpm);
    }

    private void restoreVelocityMode() {
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        applyPidf();
        setpoint = Double.NaN;
        setTarget(targetRpm);
    }

    private void applyPidf() {
        motor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER,
                new PIDFCoefficients(pidf.p, pidf.i, pidf.d, pidf.f));
    }

    private void setFeed(boolean on) {
        if (intake != null) intake.setPower(on ? INTAKE_SHOOT_POWER : 0);
        if (blocker != null) blocker.setAngle(on ? blocker.CLOSED : blocker.OPEN);
    }

    private void adjust(int selected, int dir) {
        double s = PARAM_STEP[selected] * dir;
        switch (selected) {
            case 0: targetRpm = Range.clip(targetRpm + s, 500, Flywheel.MAX); break;
            case 1: pidf.p = Math.max(0, pidf.p + s); break;
            case 2: pidf.i = Math.max(0, pidf.i + s); break;
            case 3: pidf.d = Math.max(0, pidf.d + s); break;
            case 4: pidf.f = Math.max(0, pidf.f + s); break;
            case 5:
                hoodPos = Range.clip(hoodPos + s, 0, 1);
                if (hood != null) hood.setAngle(hoodPos);
                break;
        }
        if (selected >= 1 && selected <= 4) applyPidf();
    }

    private double paramValue(int i) {
        switch (i) {
            case 0: return targetRpm;
            case 1: return pidf.p;
            case 2: return pidf.i;
            case 3: return pidf.d;
            case 4: return pidf.f;
            default: return hoodPos;
        }
    }

    // ---------------------------------------------------------------- display and logging

    private void showMenu(int selected, boolean spinning) {
        telemetry.addLine("=== Flywheel PIDF Tuner ===");
        telemetry.addLine("CROSS shot round (4 balls) | SQUARE step test | TRIANGLE F calibrate");
        telemetry.addLine("CIRCLE auto-tune | OPTIONS revert to best | SHARE spin | L bumper intake to load");
        telemetry.addLine("D-pad left/right pick, up/down change:");
        for (int i = 0; i < PARAMS.length; i++) {
            telemetry.addLine((i == selected ? " >  " : "     ") + PARAMS[i] + "  "
                    + String.format(Locale.US, PARAM_FORMAT[i], paramValue(i)));
        }
        telemetry.addData("flywheel", "%.0f rpm (%s)", readRpm(), spinning ? "spinning" : "off");
        telemetry.addData("auto-tune", autoTune ? "ON, changes PIDF after every scored round" : "off, rounds are only logged");
        telemetry.addLine(bestText("best SHOT: ", shotTuner));
        telemetry.addLine(bestText("best STEP: ", stepTuner));
        if (!lastResult.isEmpty()) {
            telemetry.addLine("");
            for (String line : lastResult) {
                telemetry.addLine(line);
            }
        }
        if (!history.isEmpty()) {
            telemetry.addLine("");
            telemetry.addLine("--- rounds ---");
            for (String line : history) {
                telemetry.addLine(line);
            }
        }
        telemetry.addData("log", logDir != null ? logDir.getPath() + "/" + session + "_*" : "unavailable");
        telemetry.update();
    }

    private List<String> resultHeader(int n, String kind, double[] pid, double f, double volts) {
        List<String> result = new ArrayList<>();
        result.add(String.format(Locale.US, "=== LAST: #%d %s ===", n, kind));
        result.add(fmtPidf(pid, f));
        result.add(String.format(Locale.US, "target %.0f rpm | hood %.3f | battery %.1fV", targetRpm, hoodPos, volts));
        return result;
    }

    private static String bestText(String label, Tuner tuner) {
        if (tuner.best == null) return label + "none yet";
        return String.format(Locale.US, "%sP=%.1f I=%.3f D=%.2f cost %.0f",
                label, tuner.best[0], tuner.best[1], tuner.best[2], tuner.bestCost);
    }

    private String fmtPidf() {
        return fmtPidf(new double[]{pidf.p, pidf.i, pidf.d}, pidf.f);
    }

    private static String fmtPidf(double[] pid, double f) {
        return String.format(Locale.US, "P=%.1f I=%.3f D=%.2f F=%.3f", pid[0], pid[1], pid[2], f);
    }

    private static String fmtCost(double cost) {
        return Double.isNaN(cost) ? "-" : String.format(Locale.US, "%.0f", cost);
    }

    private void setupLog() {
        session = new SimpleDateFormat("MMdd_HHmmss", Locale.US).format(new Date());
        File dir = new File(AppUtil.FIRST_FOLDER, "flywheel_tuner");
        if (dir.isDirectory() || dir.mkdirs()) {
            logDir = dir;
            append(summaryFile(), "round,kind,p,i,d,f,target_rpm,hood,battery_v,cost,summary,details");
        }
    }

    private File summaryFile() {
        return new File(logDir, session + "_summary.csv");
    }

    private void logRound(int n, String kind, double[] pid, double f, double volts, double cost,
                          String line1, String line2, Rec rec) {
        history.addFirst(line1);
        while (history.size() > HISTORY) history.removeLast();
        RobotLog.ii(TAG, line1 + " |" + line2);
        if (logDir == null) return;

        append(summaryFile(), String.format(Locale.US, "%d,%s,%.3f,%.4f,%.3f,%.4f,%.0f,%.3f,%.2f,%s,\"%s\",\"%s\"",
                n, kind, pid[0], pid[1], pid[2], f, targetRpm, hoodPos, volts,
                Double.isNaN(cost) ? "" : String.format(Locale.US, "%.1f", cost),
                line1.replace("\"", "'"), line2.trim().replace("\"", "'")));
        try (PrintWriter w = new PrintWriter(new FileWriter(
                new File(logDir, String.format(Locale.US, "%s_r%03d_%s.csv", session, n, kind.toLowerCase(Locale.US)))))) {
            w.println("t_ms,setpoint_rpm,rpm,feeding");
            for (int i = 0; i < rec.size(); i++) {
                w.println(String.format(Locale.US, "%.1f,%.0f,%.1f,%d",
                        rec.t.get(i), rec.sp.get(i), rec.rpm.get(i), rec.feeding.get(i) ? 1 : 0));
            }
        } catch (IOException e) {
            RobotLog.ee(TAG, e, "could not write round " + n);
        }
    }

    private void append(File file, String line) {
        try (PrintWriter w = new PrintWriter(new FileWriter(file, true))) {
            w.println(line);
        } catch (IOException e) {
            RobotLog.ee(TAG, e, "could not write " + file);
        }
    }
}
