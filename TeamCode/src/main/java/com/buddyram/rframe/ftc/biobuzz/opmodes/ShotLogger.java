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

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Logs flywheel speed while shooting, with the PIDF in Flywheel.PIDF. No tuning, just numbers.
 * Hold R bumper to shoot (intake at 0.7). Each press is a "volley": its shots are found the same way
 * as the Flywheel PIDF Tuner does, shown on telemetry and added to the session stats (how far off the
 * wheel is at each shot, by shot position, dips, recovery). Between volleys it also tracks how
 * steady the wheel holds speed with no balls. Everything goes to CSV in /sdcard/FIRST/shot_logger.
 */
@TeleOp(name = "BIOBUZZ Shot Logger", group = "Biobuzz")
public class ShotLogger extends LinearOpMode {
    private static final String TAG = "ShotLogger";
    private static final int CPR = 28;
    private static final double INTAKE_SHOOT_POWER = 0.7;
    private static final double INTAKE_LOAD_POWER = 0.5;
    private static final double BAND_RPM = Flywheel.THRESHOLD;
    private static final double RPM_STEP = 50;
    private static final double HOOD_STEP = 0.01;
    private static final double MIN_DROP_RPM = 80;   // shot detection threshold floor, same as the tuner
    private static final long NOISE_WINDOW_MS = 500; // steady samples used to size the shot threshold
    private static final long STABLE_MS = 300;       // volley ends once back in band this long...
    private static final long POST_MAX_MS = 2500;    // ...or this long after letting go of R bumper
    private static final long HOLD_SETTLE_MS = 1000; // holding stats start this long after things settle
    private static final int POSITIONS = 6;          // per-shot-position averages for shots 1..6

    /** Running mean / sd / min / max (Welford). */
    private static class Stats {
        int n;
        double mean, m2, absSum, min = Double.MAX_VALUE, max = -Double.MAX_VALUE;

        void add(double x) {
            n++;
            double d = x - mean;
            mean += d / n;
            m2 += d * (x - mean);
            absSum += Math.abs(x);
            min = Math.min(min, x);
            max = Math.max(max, x);
        }

        double sd() {
            return n > 1 ? Math.sqrt(m2 / n) : 0;
        }

        double absMean() {
            return n > 0 ? absSum / n : 0;
        }

        double worst() {
            return Math.abs(max) > Math.abs(min) ? max : min;
        }
    }

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

    // Session stats
    private Stats shotErr = new Stats();
    private Stats shotDrop = new Stats();
    private Stats shotRecover = new Stats();
    private int neverRecovered;
    private Stats volleySpread = new Stats();
    private Stats[] byPosition = newPositions();
    private Stats holdErr = new Stats();
    private int volleys;
    private List<String> lastVolley = new ArrayList<>();

    private File logDir;
    private String session;

    @Override
    public void runOpMode() {
        motor = hardwareMap.get(DcMotorEx.class, "LFly");
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, Flywheel.PIDF);
        rpmMotor = new RPMMotor(motor, CPR);
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
        if (blocker != null) blocker.setAngle(blocker.OPEN);
        if (hood != null) hood.setAngle(hoodPos);
        setupLog();

        telemetry.addLine("=== Shot Logger ===");
        telemetry.addLine("CIRCLE flywheel on/off, hold R bumper to shoot, L bumper to load");
        telemetry.addData("PIDF", fmtPidf());
        telemetry.addData("log", logDir != null ? logDir.getPath() : "unavailable");
        telemetry.update();
        waitForStart();

        Gamepad cur = new Gamepad();
        Gamepad prev = new Gamepad();
        FlywheelPIDFTuner.Rec rec = new FlywheelPIDFTuner.Rec();
        boolean flywheelOn = false;
        boolean inVolley = false;
        double fireT = 0, releaseT = 0, inBandSince = -1, dropRpm = MIN_DROP_RPM, noise = 0;
        double volts = battery.getVoltage();
        double lastDisturbT = 0;      // target change, flywheel on, or a volley
        boolean settledSince = false; // back in band since the last disturbance
        long lastTelemetryMs = 0;

        while (opModeIsActive()) {
            prev.copy(cur);
            cur.copy(gamepad1);
            for (LynxModule hub : hubs) {
                hub.clearBulkCache();
            }
            double rpm = rpmMotor.getRPM();
            double now = rec.lastT();

            // Controls
            if (cur.circle && !prev.circle) {
                flywheelOn = !flywheelOn;
                lastDisturbT = now;
                settledSince = false;
            }
            if (cur.dpad_up && !prev.dpad_up) targetRpm = Range.clip(targetRpm + RPM_STEP, 500, Flywheel.MAX);
            if (cur.dpad_down && !prev.dpad_down) targetRpm = Range.clip(targetRpm - RPM_STEP, 500, Flywheel.MAX);
            if (cur.dpad_right && !prev.dpad_right) hoodPos = Range.clip(hoodPos + HOOD_STEP, 0, 1);
            if (cur.dpad_left && !prev.dpad_left) hoodPos = Range.clip(hoodPos - HOOD_STEP, 0, 1);
            if (hood != null) hood.setAngle(hoodPos);
            if (cur.options && !prev.options) resetStats();
            double newSetpoint = flywheelOn ? targetRpm : 0;
            if (newSetpoint != setpoint) {
                lastDisturbT = now;
                settledSince = false;
            }
            setTarget(newSetpoint);

            boolean shoot = cur.right_bumper;
            if (intake != null) intake.setPower(shoot ? INTAKE_SHOOT_POWER : cur.left_bumper ? INTAKE_LOAD_POWER : 0);
            if (blocker != null) blocker.setAngle(shoot ? blocker.CLOSED : blocker.OPEN);
            rec.add(setpoint, rpm, shoot);
            now = rec.lastT();
            boolean inBand = flywheelOn && Math.abs(rpm - setpoint) <= BAND_RPM;

            if (!inVolley) {
                if (shoot) {
                    // Start a volley; size the shot threshold from how noisy the wheel was just before
                    inVolley = true;
                    fireT = now;
                    releaseT = now;
                    noise = rec.stats(rec.indexAt(now - NOISE_WINDOW_MS), rec.size() - 1)[1];
                    dropRpm = Math.max(MIN_DROP_RPM, 5 * noise);
                    volts = battery.getVoltage();
                    inBandSince = -1;
                } else {
                    // Holding speed with no balls
                    if (inBand && !settledSince) {
                        // Up to speed after spin-up, a target change or a volley: one buzz to say READY
                        settledSince = true;
                        gamepad1.rumble(300);
                    }
                    if (flywheelOn && settledSince && now - lastDisturbT > HOLD_SETTLE_MS) holdErr.add(rpm - setpoint);
                    if (rec.size() > 2000) rec.dropBefore(now - 1000);
                }
            } else {
                if (shoot) {
                    releaseT = now;
                    inBandSince = -1;
                } else {
                    if (inBand) {
                        if (inBandSince < 0) inBandSince = now;
                    } else {
                        inBandSince = -1;
                    }
                    if ((inBandSince >= 0 && now - inBandSince >= STABLE_MS) || now - releaseT >= POST_MAX_MS) {
                        finishVolley(rec, rec.indexAt(fireT), dropRpm, noise, volts);
                        inVolley = false;
                        lastDisturbT = now;
                        settledSince = false;
                        rec.dropBefore(now - 1000);
                    }
                }
            }

            long nowMs = System.currentTimeMillis();
            if (nowMs - lastTelemetryMs > 100) {
                lastTelemetryMs = nowMs;
                String state = !flywheelOn ? "OFF (CIRCLE)" : inVolley ? (shoot ? "SHOOTING" : "recording recovery")
                        : inBand ? "READY" : "not at speed";
                showTelemetry(rpm, state);
            }
        }

        setTarget(0);
        if (intake != null) intake.setPower(0);
    }

    private void finishVolley(FlywheelPIDFTuner.Rec rec, int fireIdx, double dropRpm, double noise, double volts) {
        int v = ++volleys;
        List<FlywheelPIDFTuner.Shot> shots = FlywheelPIDFTuner.detectShots(rec, fireIdx, dropRpm);
        List<String> lines = new ArrayList<>();
        lines.add(String.format(Locale.US, "--- last volley #%d: %d shots @%.0f, %.1fV ---", v, shots.size(), targetRpm, volts));
        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE, sum = 0;
        for (int k = 0; k < shots.size(); k++) {
            FlywheelPIDFTuner.Shot s = shots.get(k);
            double err = s.pre - targetRpm;
            shotErr.add(err);
            shotDrop.add(s.pre - s.low);
            if (s.recoverMs >= 0) shotRecover.add(s.recoverMs); else neverRecovered++;
            if (k < POSITIONS) byPosition[k].add(err);
            min = Math.min(min, s.pre);
            max = Math.max(max, s.pre);
            sum += s.pre;
            lines.add(String.format(Locale.US, "shot %d: at %.0f (%+.0f)  low %.0f  drop %.0f  back %s",
                    k + 1, s.pre, err, s.low, s.pre - s.low,
                    s.recoverMs < 0 ? "-" : String.format(Locale.US, "%.0fms", s.recoverMs)));
            appendShot(v, k + 1, s, volts);
        }
        if (shots.size() >= 2) {
            volleySpread.add(max - min);
            lines.add(String.format(Locale.US, "spread %.0f | avg err %+.0f | noise sd %.0f",
                    max - min, sum / shots.size() - targetRpm, noise));
        } else if (shots.isEmpty()) {
            lines.add("no shots found (threshold " + Math.round(dropRpm) + " rpm)");
        }
        lastVolley = lines;
        StringBuilder log = new StringBuilder();
        for (String line : lines) {
            log.append(line).append(" | ");
        }
        RobotLog.ii(TAG, log.toString());
        saveRaw(rec, fireIdx, v);
    }

    private void showTelemetry(double rpm, String state) {
        telemetry.addLine("=== Shot Logger ===  OPTIONS resets stats");
        telemetry.addLine("R bumper shoot | L bumper load | CIRCLE flywheel | D-pad RPM / hood");
        telemetry.addData("state", state);
        telemetry.addData("target / rpm", "%.0f / %.0f (%+.0f)", setpoint, rpm, rpm - setpoint);
        telemetry.addData("hood", "%.2f", hoodPos);
        telemetry.addData("PIDF", fmtPidf());

        for (String line : lastVolley) {
            telemetry.addLine(line);
        }

        telemetry.addLine(String.format(Locale.US, "--- all shots: %d in %d volleys ---", shotErr.n, volleys));
        if (shotErr.n > 0) {
            telemetry.addLine(String.format(Locale.US, "off at shot: avg %+.0f | avg abs %.0f | sd %.0f | worst %+.0f",
                    shotErr.mean, shotErr.absMean(), shotErr.sd(), shotErr.worst()));
            telemetry.addLine(String.format(Locale.US, "drop: avg %.0f, max %.0f | back in band: avg %s%s",
                    shotDrop.mean, shotDrop.max,
                    shotRecover.n > 0 ? String.format(Locale.US, "%.0fms", shotRecover.mean) : "-",
                    neverRecovered > 0 ? " (" + neverRecovered + " before next shot)" : ""));
            if (volleySpread.n > 0) {
                telemetry.addLine(String.format(Locale.US, "volley spread: avg %.0f, worst %.0f",
                        volleySpread.mean, volleySpread.max));
            }
            StringBuilder pos = new StringBuilder("avg off by shot #:");
            for (int k = 0; k < POSITIONS; k++) {
                if (byPosition[k].n > 0) {
                    pos.append(String.format(Locale.US, "  %d: %+.0f", k + 1, byPosition[k].mean));
                }
            }
            telemetry.addLine(pos.toString());
        }

        telemetry.addLine("--- holding speed, no balls ---");
        if (holdErr.n > 0) {
            telemetry.addLine(String.format(Locale.US, "off: avg %+.0f | avg abs %.0f | sd %.0f | range %+.0f to %+.0f",
                    holdErr.mean, holdErr.absMean(), holdErr.sd(), holdErr.min, holdErr.max));
        } else {
            telemetry.addLine("waiting for the wheel to settle");
        }
        telemetry.addData("log", logDir != null ? logDir.getPath() + "/" + session + "_*" : "unavailable");
        telemetry.update();
    }

    private void resetStats() {
        shotErr = new Stats();
        shotDrop = new Stats();
        shotRecover = new Stats();
        neverRecovered = 0;
        volleySpread = new Stats();
        byPosition = newPositions();
        holdErr = new Stats();
        lastVolley = new ArrayList<>();
        RobotLog.ii(TAG, "stats reset after volley " + volleys);
    }

    private static Stats[] newPositions() {
        Stats[] positions = new Stats[POSITIONS];
        for (int k = 0; k < POSITIONS; k++) {
            positions[k] = new Stats();
        }
        return positions;
    }

    private void setTarget(double rpm) {
        if (rpm == setpoint) return;
        setpoint = rpm;
        rpmMotor.setRPM(rpm);
    }

    private static String fmtPidf() {
        return String.format(Locale.US, "P=%.1f I=%.3f D=%.2f F=%.3f",
                Flywheel.PIDF.p, Flywheel.PIDF.i, Flywheel.PIDF.d, Flywheel.PIDF.f);
    }

    // ---------------------------------------------------------------- CSV logging

    private void setupLog() {
        session = new SimpleDateFormat("MMdd_HHmmss", Locale.US).format(new Date());
        File dir = new File(AppUtil.FIRST_FOLDER, "shot_logger");
        if (dir.isDirectory() || dir.mkdirs()) {
            logDir = dir;
            append(new File(logDir, session + "_shots.csv"),
                    "volley,shot,target_rpm,at_shot_rpm,error_rpm,low_rpm,drop_rpm,recover_ms,hood,battery_v,p,i,d,f");
        }
    }

    private void appendShot(int volley, int shot, FlywheelPIDFTuner.Shot s, double volts) {
        if (logDir == null) return;
        append(new File(logDir, session + "_shots.csv"), String.format(Locale.US,
                "%d,%d,%.0f,%.1f,%.1f,%.1f,%.1f,%s,%.3f,%.2f,%.3f,%.4f,%.3f,%.4f",
                volley, shot, targetRpm, s.pre, s.pre - targetRpm, s.low, s.pre - s.low,
                s.recoverMs < 0 ? "" : String.format(Locale.US, "%.0f", s.recoverMs), hoodPos, volts,
                Flywheel.PIDF.p, Flywheel.PIDF.i, Flywheel.PIDF.d, Flywheel.PIDF.f));
    }

    private void saveRaw(FlywheelPIDFTuner.Rec rec, int fireIdx, int volley) {
        if (logDir == null) return;
        int from = rec.indexAt(rec.t.get(Math.min(fireIdx, rec.size() - 1)) - 300);
        try (PrintWriter w = new PrintWriter(new FileWriter(
                new File(logDir, String.format(Locale.US, "%s_v%03d.csv", session, volley))))) {
            w.println("t_ms,setpoint_rpm,rpm,feeding");
            for (int i = from; i < rec.size(); i++) {
                w.println(String.format(Locale.US, "%.1f,%.0f,%.1f,%d",
                        rec.t.get(i), rec.sp.get(i), rec.rpm.get(i), rec.feeding.get(i) ? 1 : 0));
            }
        } catch (IOException e) {
            RobotLog.ee(TAG, e, "could not write volley " + volley);
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
