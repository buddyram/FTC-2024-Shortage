package com.buddyram.rframe.ftc.v3.opmodes;

import com.buddyram.rframe.BaseLogger;
import com.buddyram.rframe.CachedOdometry;
import com.buddyram.rframe.Logger;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.SmartLogWrapper;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.drive.HolonomicPositionDriveAdapter;
import com.buddyram.rframe.drive.MecanumDriveTrain;
import com.buddyram.rframe.ftc.GoBildaPinpointOdometry;
import com.buddyram.rframe.ftc.Motor;
import com.buddyram.rframe.ftc.RPMMotor;
import com.buddyram.rframe.ftc.v3.BotUtilsNew;
import com.buddyram.rframe.ftc.v3.Globals;
import com.buddyram.rframe.ftc.v3.NewDecodeBot;
import com.buddyram.rframe.ftc.v3.Robot.intake.Intake;
import com.buddyram.rframe.ftc.v3.Robot.intake.IntakeServoLift;
import com.buddyram.rframe.ftc.v3.Robot.intake.Sweeper;
import com.buddyram.rframe.ftc.v3.Robot.launcher.Blocker;
import com.buddyram.rframe.ftc.v3.Robot.launcher.Flywheel;
import com.buddyram.rframe.ftc.v3.Robot.launcher.Hood;
import com.buddyram.rframe.ftc.v3.Robot.launcher.Launcher;
import com.buddyram.rframe.ftc.v3.Robot.launcher.Turret;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public abstract class BaseOpmode extends LinearOpMode {
    NewDecodeBot decodeBot;
    CachedOdometry<Pose3D> cachedOdometry;

    public static final Pose3D DEFAULT_POSITION = new Pose3D(
            new Vector3D(0, 0, 0), // position
            new Vector3D(0, 0, 0), // rotation
            new Vector3D(0, 0, 0), // position velocity
            new Vector3D(0, 0, 0) // rotation velocity
    );

    public static Pose3D currentPosition = DEFAULT_POSITION;
    public void runOpMode() throws InterruptedException {
        this.initializeHardware();
        Thread rememberLastPosition = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                if (opModeIsActive()) {
                    this.cachedOdometry.refresh();
                    this.decodeBot.updateGlobals();
                    if (this.decodeBot.launcher != null) {
                        this.decodeBot.controlIntake();
                        this.decodeBot.autoAim();
                        this.decodeBot.adjustFlywheelSpeed();
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
            if (this.decodeBot.launcher != null) this.decodeBot.launcher.turret.setAngle(0);
            this.cachedOdometry.cleanup();
            Thread.sleep(100);

        }

    }


    public abstract void execute() throws RobotException, InterruptedException;

    protected Vector3D getBlueStartPosition() {
        return new Vector3D(25.46, 129.8, 0);
    }
    protected double getBlueStartHeading() { return 52.01; }
    protected Vector3D getRedStartPosition() {
        return new Vector3D(118.44, 130.37, 0);
    }
    protected double getRedStartHeading() { return -52.25; }
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
        odo.setOffsets(-165.025, -0.35, DistanceUnit.MM);
        odo.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        odo.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.FORWARD, GoBildaPinpointDriver.EncoderDirection.REVERSED);
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

        this.decodeBot = new NewDecodeBot() {
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
        HolonomicPositionDriveAdapter adapter = new HolonomicPositionDriveAdapter(drive, this.cachedOdometry);
        adapter.init();



        Launcher launcher = new Launcher(decodeBot, null, null, null, null);
        Intake intake = null;
        try {
            DcMotorEx motorFly = hardwareMap.get(DcMotorEx.class, "LFly");
            motorFly.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motorFly.setVelocity(0);
            PIDFCoefficients pidNew = new PIDFCoefficients(354, 0, 0, 40);
            motorFly.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, pidNew);

            DcMotor motorInt = hardwareMap.get(DcMotor.class, "intake");
            motorInt.setPower(0);
            DcMotorEx turret = hardwareMap.get(DcMotorEx.class, "turret");
            if (reset) {
                turret.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            }
            turret.setPower(0.4);
            turret.setTargetPosition(0);
            turret.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            PIDFCoefficients turretPidf = turret.getPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION);
            turretPidf.p = 6;
            turretPidf.i = 0;
            turretPidf.d = 4;
            turretPidf.f = 0;
            turret.setPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION, turretPidf);
            turret.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            turret.setTargetPositionTolerance(1);

            Servo blockerServo = hardwareMap.get(Servo.class, "blocker");
            Servo hood = hardwareMap.get(Servo.class, "hood");
            Servo intakeServoLeft = hardwareMap.get(Servo.class, "intakeLeft");
            Servo intakeServoRight = hardwareMap.get(Servo.class, "intakeRight");
            launcher = new Launcher(
                    this.decodeBot,
                    new Flywheel(this.decodeBot, new RPMMotor(motorFly, 28)),
                    new Turret(this.decodeBot, turret),
                    new Hood(this.decodeBot, hood),
                    new Blocker(this.decodeBot, blockerServo)
            );
            intake = new Intake(
                    this.decodeBot,
                    new Sweeper(this.decodeBot, motorInt),
                    new IntakeServoLift(this.decodeBot, intakeServoLeft, intakeServoRight ));
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
        this.decodeBot.init(logger, this.cachedOdometry, adapter, launcher, intake, pinpointOdometry, isRed == 1);
        while (! this.isStarted()) {
            this.cachedOdometry.refresh();
            telemetry.addData("POS", this.cachedOdometry.get());
            telemetry.addData("Pinpoint Status", odo.getDeviceStatus());
            telemetry.update();
        }
    }
}
