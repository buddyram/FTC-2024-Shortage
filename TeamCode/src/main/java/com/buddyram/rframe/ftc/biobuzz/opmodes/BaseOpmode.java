package com.buddyram.rframe.ftc.biobuzz.opmodes;

import com.buddyram.rframe.BaseLogger;
import com.buddyram.rframe.CachedOdometry;
import com.buddyram.rframe.Logger;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.SmartLogWrapper;
import com.buddyram.rframe.Utils;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.drive.HolonomicPositionDriveAdapter;
import com.buddyram.rframe.drive.MecanumDriveTrain;
import com.buddyram.rframe.ftc.GoBildaPinpointOdometry;
import com.buddyram.rframe.ftc.Motor;
import com.buddyram.rframe.ftc.RPMMotor;
import com.buddyram.rframe.ftc.biobuzz.Globals;
import com.buddyram.rframe.ftc.biobuzz.BiobuzzBot;
import com.buddyram.rframe.ftc.biobuzz.Robot.intake.Intake;
import com.buddyram.rframe.ftc.biobuzz.Robot.intake.Sweeper;
import com.buddyram.rframe.ftc.biobuzz.Robot.launcher.Blocker;
import com.buddyram.rframe.ftc.biobuzz.Robot.launcher.Flywheel;
import com.buddyram.rframe.ftc.biobuzz.Robot.launcher.Hood;
import com.buddyram.rframe.ftc.biobuzz.Robot.launcher.Launcher;
import com.buddyram.rframe.ftc.biobuzz.Robot.launcher.Turret;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import java.util.List;

public abstract class BaseOpmode extends LinearOpMode {
    BiobuzzBot biobuzzBot;
    CachedOdometry<Pose3D> cachedOdometry;
    // The bare drivetrain: drive instructions are relative to the robot. biobuzzBot.getDrive() goes through
    // HolonomicPositionDriveAdapter, which subtracts the heading and so drives relative to the field.
    MecanumDriveTrain robotCentricDrive;
    private boolean rumbling = true; // so the first call sets the LED red if not ready yet

    public static final Pose3D DEFAULT_POSITION = new Pose3D(
            new Vector3D(0, 0, 0), // position
            new Vector3D(0, 0, 0), // rotation
            new Vector3D(0, 0, 0), // position velocity
            new Vector3D(0, 0, 0) // rotation velocity
    );

    public static Pose3D currentPosition = DEFAULT_POSITION;

    public static final double START_HOOD = 0.55; // starting hood position for the manual / test opmodes
    public static final Pose3D ZERO = new Pose3D(new Vector3D(), new Vector3D(), new Vector3D(), new Vector3D());
    // Blue start: bottom right corner, back against the wall, facing left (heading 90 = facing -x).
    // Wall offsets are the old Teleop corner reset (7.98, 8.94) swapped, since the robot is turned 90.
    private static final Pose3D BLUE_START = new Pose3D(
            new Vector3D(144 - 8.94, 7.98, 0), new Vector3D(0, 0, 90), new Vector3D(), new Vector3D());

    /**
     * Start pose for the alliance. The BIOBUZZ field is rotated, not mirrored, for red (loading zones,
     * gardens and HIVES all swap that way): the blue pose turned 180 degrees around the field center.
     */
    public static Pose3D startFor(boolean isRed) {
        if (!isRed) return BLUE_START;
        return new Pose3D(
                new Vector3D(144 - BLUE_START.position.x, 144 - BLUE_START.position.y, 0),
                new Vector3D(0, 0, Utils.normalizeAngle(BLUE_START.rotation.z + 180)),
                new Vector3D(), new Vector3D());
    }
    public void runOpMode() throws InterruptedException {
        this.initializeHardware();
        Thread rememberLastPosition = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                if (opModeIsActive()) {
                    this.cachedOdometry.refresh();
                    this.biobuzzBot.updateGlobals();
                    if (this.biobuzzBot.launcher != null) {
                        this.biobuzzBot.launcher.wheel.update();
                        this.biobuzzBot.controlIntake();
                        this.biobuzzBot.autoAim();
                        this.biobuzzBot.adjustFlywheelSpeed();
                    }
                    Thread.yield();
                }
            }
        });
        rememberLastPosition.setPriority(Thread.MIN_PRIORITY);
        rememberLastPosition.start();
        this.waitForStart();


        try {
            this.execute();
        } catch (RobotException e) {
            throw new RuntimeException(e);
        } finally {
            rememberLastPosition.interrupt();
            rememberLastPosition.join();
            if (this.biobuzzBot.launcher != null) this.biobuzzBot.launcher.turret.setAngle(0);
            this.cachedOdometry.cleanup();
            Thread.sleep(100);

        }

    }


    public abstract void execute() throws RobotException, InterruptedException;

    /**
     * Rumbles gamepad 1 the whole time the shot is ready (flywheel at speed, turret on a reachable aim, robot
     * still) and turns its LED green, red otherwise, with the reasons on telemetry. Only sends a command when
     * that changes, since a new rumble every loop floods the gamepad. Returns whether it's ready.
     */
    protected boolean rumbleWhenReadyToShoot() {
        List<String> reasons = this.biobuzzBot.notReadyReasons();
        boolean ready = reasons.isEmpty();
        if (ready) {
            telemetry.addLine(">> READY TO SHOOT");
        } else {
            for (String reason : reasons) {
                telemetry.addLine(">> NOT READY: " + reason);
            }
        }
        if (ready && !rumbling) {
            gamepad1.rumble(Gamepad.RUMBLE_DURATION_CONTINUOUS);
            gamepad1.setLedColor(0, 255, 0, Gamepad.LED_DURATION_CONTINUOUS);
        } else if (!ready && rumbling) {
            gamepad1.stopRumble();
            gamepad1.setLedColor(255, 0, 0, Gamepad.LED_DURATION_CONTINUOUS);
        }
        rumbling = ready;
        return ready;
    }

    protected Vector3D getBlueStartPosition() { return startFor(false).position; }
    protected double getBlueStartHeading() { return startFor(false).rotation.z; }
    protected Vector3D getRedStartPosition() { return startFor(true).position; }
    protected double getRedStartHeading() { return startFor(true).rotation.z; }
    public void initializeHardware() throws InterruptedException {
        Boolean reset = null;
        if (Globals.DID_RUN_AUTO == null) {
            telemetry.addData("DID RUN AUTO? YES: SQUARE , NO: TRIANGLE", "");
            telemetry.update();
            while (reset == null) {
                if (gamepad1.square) {
                    reset = false;
                } else if (gamepad1.triangle) {
                    reset = true;
                }
            }
        } else {
            telemetry.addData("USED CACHE", "");
            reset = !Globals.DID_RUN_AUTO;
        }



        DcMotor motorFR = hardwareMap.get(DcMotor.class, "dfr");
        DcMotor motorFL = hardwareMap.get(DcMotor.class, "dfl");
        DcMotor motorBR = hardwareMap.get(DcMotor.class, "dbr");
        DcMotor motorBL = hardwareMap.get(DcMotor.class, "dbl");
        motorFR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorFL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorBR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorBL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        int isRed = -1;
        if (Globals.IS_RED == null) {
            telemetry.addLine("O for red and X for blue.");
            telemetry.update();
            while (isRed == -1) {
                if (gamepad1.cross || gamepad2.cross) {
                    isRed = 0;
                }
                if (gamepad1.circle || gamepad2.circle) {
                    isRed = 1;
                }
            }
            Globals.IS_RED = isRed == 1;
        } else {
            isRed = Globals.IS_RED ? 1 : 0;
        }
        telemetry.addData("isRed", isRed);

        // Pinpoint odometry setup
        GoBildaPinpointDriver odo = hardwareMap.get(GoBildaPinpointDriver.class, "odo");
        // From the CAD: the forward pod sits 152 mm left of center (left = positive), the strafe pod ~3 mm behind
        odo.setOffsets(151.9422952843, -2.8999939136, DistanceUnit.MM);
        odo.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        odo.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.FORWARD, GoBildaPinpointDriver.EncoderDirection.FORWARD);
        odo.resetPosAndIMU();
        Thread.sleep(300);

        GoBildaPinpointOdometry pinpointOdometry = new GoBildaPinpointOdometry(odo);

        // Set starting position
        if (Globals.POSITION != null) {
            pinpointOdometry.setPosition(Globals.POSITION);
            telemetry.addData("Odometry", "used cached position");
        } else {
            if (isRed != 1) {
                pinpointOdometry.setPosition(new Pose3D(
                        getBlueStartPosition(),
                        new Vector3D(0, 0, getBlueStartHeading()),
                        new Vector3D(),
                        new Vector3D()
                ));
            } else {
                pinpointOdometry.setPosition(new Pose3D(
                        getRedStartPosition(),
                        new Vector3D(0, 0, getRedStartHeading()),
                        new Vector3D(),
                        new Vector3D()
                ));
            }
            telemetry.addData("Odometry", "used starting position");
        }

        telemetry.update();
        Thread.sleep(1000);

        this.biobuzzBot = new BiobuzzBot() {
            @Override
            public boolean isActive() {
                return opModeIsActive();
            }
        };
        MecanumDriveTrain drive = new MecanumDriveTrain(
                new Motor(motorFL, -1),
                new Motor(motorFR, 1),
                new Motor(motorBL, -1),
                new Motor(motorBR, 1),
                1
        );

        this.cachedOdometry = new CachedOdometry<>(pinpointOdometry);
        this.robotCentricDrive = drive;
        HolonomicPositionDriveAdapter adapter = new HolonomicPositionDriveAdapter(drive, this.cachedOdometry);
        adapter.init();



        Launcher launcher = new Launcher(biobuzzBot, null, null, null, null);
        Intake intake = null;
        try {
            DcMotorEx motorFly = hardwareMap.get(DcMotorEx.class, "LFly");
            // Reversed so positive RPM spins the right way and reads back positive
            motorFly.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motorFly.setVelocity(0);
            motorFly.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, Flywheel.PIDF);

            DcMotor motorInt = hardwareMap.get(DcMotor.class, "intake");
            motorInt.setPower(0);
            ServoImplEx turret = hardwareMap.get(ServoImplEx.class, "turret");

            Servo blockerServo = hardwareMap.get(Servo.class, "blocker");
            Servo hood = hardwareMap.get(Servo.class, "hood");
            launcher = new Launcher(
                    this.biobuzzBot,
                    new Flywheel(this.biobuzzBot, new RPMMotor(motorFly, 28)),
                    new Turret(this.biobuzzBot, turret),
                    new Hood(this.biobuzzBot, hood),
                    new Blocker(this.biobuzzBot, blockerServo)
            );
            intake = new Intake(
                    this.biobuzzBot,
                    new Sweeper(this.biobuzzBot, motorInt));
            telemetry.addData("Subsystems", "Initialized");
        } catch (Exception e) {
            telemetry.addData("Subsystems", "Not found - drivetrain only");
        }

        Logger logger = new SmartLogWrapper(
                new BaseLogger() {
                    public void log(String caption, Object value) {
                        telemetry.addData(caption, value);
                    }

                    public void flush() {
                        telemetry.update();
                    }
                }
        );
        this.biobuzzBot.init(logger, this.cachedOdometry, adapter, launcher, intake, pinpointOdometry, isRed == 1);
        while (! this.isStarted()) {
            this.cachedOdometry.refresh();
            telemetry.addData("POS", this.cachedOdometry.get());
            telemetry.addData("Pinpoint Status", odo.getDeviceStatus());
            telemetry.update();
        }
    }
}
