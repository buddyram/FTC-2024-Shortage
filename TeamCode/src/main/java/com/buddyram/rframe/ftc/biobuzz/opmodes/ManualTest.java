package com.buddyram.rframe.ftc.biobuzz.opmodes;

import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.drive.MecanumDriveTrain;
import com.buddyram.rframe.ftc.GoBildaPinpointOdometry;
import com.buddyram.rframe.ftc.Motor;
import com.buddyram.rframe.ftc.RPMMotor;
import com.buddyram.rframe.ftc.biobuzz.BiobuzzBot;
import com.buddyram.rframe.ftc.biobuzz.Globals;
import com.buddyram.rframe.ftc.biobuzz.Robot.intake.Intake;
import com.buddyram.rframe.ftc.biobuzz.Robot.intake.Sweeper;
import com.buddyram.rframe.ftc.biobuzz.Robot.launcher.Blocker;
import com.buddyram.rframe.ftc.biobuzz.Robot.launcher.Flywheel;
import com.buddyram.rframe.ftc.biobuzz.Robot.launcher.Hood;
import com.buddyram.rframe.ftc.biobuzz.Robot.launcher.Turret;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * Free drive / free aim / free shoot. Odometry starts in the alliance's corner (bottom right facing
 * blue, or the same spot rotated 180 degrees around the field center for red) and the turret can
 * auto-aim at its alliance HIVE's CELL on the side of the field it's on (SQUARE toggles it), or be aimed by hand.
 * Drive is robot-relative so it works anywhere, without a field.
 */
@Disabled // replaced by BIOBUZZ Manual Teleop
@TeleOp(name = "BIOBUZZ Manual Test", group = "Biobuzz")
public class ManualTest extends LinearOpMode {

    private static final double TURRET_DEG_PER_S = 90.0;
    private static final double RPM_PER_S = 1000.0;
    private static final double HOOD_PER_S = 0.25;
    private static final double START_RPM = Flywheel.DEFAULT_RPM;
    private static final double START_HOOD = BaseOpmode.START_HOOD;
    private static final Pose3D ZERO = BaseOpmode.ZERO;

    private GoBildaPinpointOdometry odometry;
    private boolean isRed = Globals.IS_RED != null && Globals.IS_RED;

    @Override
    public void runOpMode() {
        MecanumDriveTrain drive = buildDrive();
        Turret turret = buildTurret();
        Flywheel flywheel = buildFlywheel();
        Hood hood = buildHood();
        Blocker blocker = buildBlocker();
        Intake intake = buildIntake();
        odometry = buildOdometry();

        // Alliance pick sets the start corner and which goal to aim at
        while (!isStarted() && !isStopRequested()) {
            if (gamepad1.cross) isRed = false;
            if (gamepad1.circle) isRed = true;
            telemetry.addLine("=== BIOBUZZ Manual Test ===");
            telemetry.addData("alliance (X blue / O red)", isRed ? "RED" : "BLUE");
            telemetry.addData("start corner", isRed
                    ? "red wall, audience end, facing the scoring side"
                    : "blue wall, scoring end, facing the audience side");
            telemetry.addLine("Sticks: drive (robot-relative), click for full speed");
            telemetry.addLine("SQUARE: auto-aim on/off, TRIANGLE: reset odometry to start, CROSS: odometry to 0,0,0");
            telemetry.addLine("Aims at your HIVE's CELL on the side (scoring / audience) you're on");
            telemetry.addLine("Triggers: aim turret (auto-aim: trim offset), OPTIONS: center turret / clear offset");
            telemetry.addLine("D-pad up/down: flywheel RPM, CIRCLE: flywheel on/off");
            telemetry.addLine("D-pad left/right: hood");
            telemetry.addLine("R bumper: shoot, L bumper: intake");
            telemetry.addData("drive", drive != null ? "ok" : "MISSING");
            telemetry.addData("odometry", odometry != null ? "ok" : "MISSING");
            telemetry.addData("turret", turret != null ? "ok" : "MISSING");
            telemetry.addData("flywheel", flywheel != null ? "ok" : "MISSING");
            telemetry.addData("hood", hood != null ? "ok" : "MISSING");
            telemetry.addData("blocker", blocker != null ? "ok" : "MISSING");
            telemetry.addData("intake", intake != null ? "ok" : "MISSING");
            telemetry.update();
        }
        if (isStopRequested()) return;

        Globals.IS_RED = isRed;
        // Only used for the goal position and aim math, no hardware
        BiobuzzBot aimBot = new BiobuzzBot(null, odometry, null, null, null, null, isRed);
        resetOdometry();

        double turretAngle = 0;
        double rpm = START_RPM;
        double hoodPos = START_HOOD;
        boolean flywheelOn = false;
        boolean prevCircle = gamepad1.circle; // circle also picks red during init
        boolean autoAim = odometry != null;
        boolean prevSquare = false;
        ElapsedTime timer = new ElapsedTime();

        while (opModeIsActive()) {
            double dt = timer.seconds();
            timer.reset();

            // Drive, robot-relative
            if (drive != null) {
                double speed = gamepad1.left_stick_button || gamepad1.right_stick_button ? 1 : 0.8;
                double speedLevel = Math.hypot(gamepad1.left_stick_x, gamepad1.left_stick_y);
                drive.drive(new HolonomicDriveInstruction(
                        gamepad1.right_stick_x * speed,
                        speed * speedLevel,
                        Math.toDegrees(Math.atan2(-gamepad1.left_stick_y, gamepad1.left_stick_x))
                ));
            }

            // Odometry
            Double aimAngle = null;
            if (odometry != null) {
                if (gamepad1.triangle) resetOdometry();
                if (gamepad1.cross) odometry.setPosition(ZERO);
                Pose3D pose = odometry.get();
                Vector3D goal = aimBot.getTargetGoal();
                aimAngle = aimBot.currentTurretAngle();
                telemetry.addData("alliance", isRed ? "RED" : "BLUE");
                telemetry.addData("pos x / y (in)", "%.2f / %.2f", pose.position.x, pose.position.y);
                telemetry.addData("heading (deg)", "%.2f", pose.rotation.z);
                telemetry.addData("aiming at", "%s HIVE, %s side CELL (%.1f, %.1f)", isRed ? "RED" : "BLUE",
                        aimBot.isAimingAtScoringCell() ? "scoring" : "audience", goal.x, goal.y);
                telemetry.addData("distance to goal (in)", "%.1f", pose.position.distance(goal));
            }

            // Turret
            if (gamepad1.square && !prevSquare && odometry != null) {
                autoAim = !autoAim;
                // Pick up manual aim from wherever the turret is now so it doesn't jump
                if (!autoAim && turret != null) turretAngle = turret.getCurrentAngle();
            }
            prevSquare = gamepad1.square;
            if (turret != null) {
                double triggers = gamepad1.left_trigger - gamepad1.right_trigger;
                if (autoAim && aimAngle != null) {
                    aimBot.turretOffset += triggers * TURRET_DEG_PER_S * dt;
                    if (gamepad1.options) aimBot.turretOffset = 0;
                    turretAngle = Range.clip(aimAngle, Turret.MIN_ANGLE, Turret.MAX_ANGLE);
                } else {
                    turretAngle += triggers * TURRET_DEG_PER_S * dt;
                    if (gamepad1.options) turretAngle = 0;
                    turretAngle = Range.clip(turretAngle, Turret.MIN_ANGLE, Turret.MAX_ANGLE);
                }
                turret.setAngle(turretAngle);
                telemetry.addData("auto-aim", odometry == null ? "unavailable (no odometry)" : autoAim ? "ON (SQUARE)" : "off (SQUARE)");
                if (aimAngle != null) {
                    String reach = aimAngle < Turret.MIN_ANGLE || aimAngle > Turret.MAX_ANGLE ? " OUT OF RANGE" : "";
                    telemetry.addData("auto-aim angle (deg)", "%.1f%s", aimAngle, reach);
                    telemetry.addData("auto-aim offset (deg)", "%.1f", aimBot.turretOffset);
                }
                telemetry.addData("turret target / current (deg)", "%.1f / %.1f", turretAngle, turret.getCurrentAngle());
            }

            // Flywheel
            if (flywheel != null) {
                if (gamepad1.circle && !prevCircle) flywheelOn = !flywheelOn;
                prevCircle = gamepad1.circle;
                if (gamepad1.dpad_up) rpm += RPM_PER_S * dt;
                if (gamepad1.dpad_down) rpm -= RPM_PER_S * dt;
                rpm = Range.clip(rpm, Flywheel.MIN, Flywheel.MAX);
                flywheel.setRPM(flywheelOn ? rpm : 0);
                telemetry.addData("flywheel", flywheelOn ? (flywheel.isReady() ? "ON - ready" : "ON - spinning up") : "off (CIRCLE)");
                telemetry.addData("flywheel set / actual (RPM)", "%.0f / %.0f", rpm, flywheel.getRPM());
            }

            // Hood
            if (hood != null) {
                if (gamepad1.dpad_right) hoodPos += HOOD_PER_S * dt;
                if (gamepad1.dpad_left) hoodPos -= HOOD_PER_S * dt;
                hoodPos = Range.clip(hoodPos, hood.MIN, hood.MAX);
                hood.setAngle(hoodPos);
                telemetry.addData("hood", "%.3f", hoodPos);
            }

            // Shoot / intake, same as the real teleop
            boolean shoot = gamepad1.right_bumper;
            boolean feeding = shoot || gamepad1.left_bumper;
            if (blocker != null) {
                blocker.setAngle(shoot ? blocker.CLOSED : blocker.OPEN);
            }
            if (intake != null) {
                intake.enableMode(feeding ? Intake.Modes.INTAKING : Intake.Modes.IDLE);
                if (shoot) {
                    intake.enableMode(Intake.Modes.INTAKING_FAST);
                }
            }
            telemetry.addData("feed", shoot ? "SHOOTING" : feeding ? "intaking" : "idle");

            telemetry.update();
        }
    }

    private MecanumDriveTrain buildDrive() {
        DcMotor fl = hardwareMap.tryGet(DcMotor.class, "dfl");
        DcMotor fr = hardwareMap.tryGet(DcMotor.class, "dfr");
        DcMotor bl = hardwareMap.tryGet(DcMotor.class, "dbl");
        DcMotor br = hardwareMap.tryGet(DcMotor.class, "dbr");
        if (fl == null || fr == null || bl == null || br == null) return null;
        for (DcMotor motor : new DcMotor[]{fl, fr, bl, br}) {
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }
        return new MecanumDriveTrain(new Motor(fl, -1), new Motor(fr, 1), new Motor(bl, -1), new Motor(br, 1), 1);
    }

    private GoBildaPinpointOdometry buildOdometry() {
        GoBildaPinpointDriver odo = hardwareMap.tryGet(GoBildaPinpointDriver.class, "odo");
        if (odo == null) return null;
        // Same setup as BaseOpmode, keep the two in sync
        odo.setOffsets(151.9422952843, -2.8999939136, DistanceUnit.MM);
        odo.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        odo.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.FORWARD, GoBildaPinpointDriver.EncoderDirection.FORWARD);
        odo.resetPosAndIMU();
        sleep(300);
        return new GoBildaPinpointOdometry(odo);
    }

    private void resetOdometry() {
        if (odometry != null) odometry.setPosition(BaseOpmode.startFor(isRed));
    }

    private Turret buildTurret() {
        ServoImplEx servo = hardwareMap.tryGet(ServoImplEx.class, "turret");
        return servo == null ? null : new Turret(null, servo);
    }

    private Flywheel buildFlywheel() {
        DcMotorEx motor = hardwareMap.tryGet(DcMotorEx.class, "LFly");
        if (motor == null) return null;
        // Reversed so positive RPM spins the right way and reads back positive
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motor.setVelocity(0);
        motor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, Flywheel.PIDF);
        return new Flywheel(null, new RPMMotor(motor, 28));
    }

    private Hood buildHood() {
        Servo servo = hardwareMap.tryGet(Servo.class, "hood");
        return servo == null ? null : new Hood(null, servo);
    }

    private Blocker buildBlocker() {
        Servo servo = hardwareMap.tryGet(Servo.class, "blocker");
        return servo == null ? null : new Blocker(null, servo);
    }

    private Intake buildIntake() {
        DcMotor motor = hardwareMap.tryGet(DcMotor.class, "intake");
        return motor == null ? null : new Intake(null, new Sweeper(null, motor));
    }
}
