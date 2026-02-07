package com.buddyram.rframe.ftc.v2.opmodes;

import com.buddyram.rframe.BaseLogger;
import com.buddyram.rframe.CachedOdometry;
import com.buddyram.rframe.GroundingOdometry;
import com.buddyram.rframe.Logger;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.SmartLogWrapper;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.drive.HolonomicPositionDriveAdapter;
import com.buddyram.rframe.drive.MecanumDriveTrain;
import com.buddyram.rframe.ftc.LimelightOdometry;
import com.buddyram.rframe.ftc.Motor;
import com.buddyram.rframe.ftc.RPMMotor;
import com.buddyram.rframe.ftc.SparkFunOTOSOdometry;
import com.buddyram.rframe.ftc.v2.BotUtilsNew;
import com.buddyram.rframe.ftc.v2.Globals;
import com.buddyram.rframe.ftc.v2.NewDecodeBot;
import com.buddyram.rframe.ftc.v2.Robot.intake.Intake;
import com.buddyram.rframe.ftc.v2.Robot.intake.Sweeper;
import com.buddyram.rframe.ftc.v2.Robot.launcher.Blocker;
import com.buddyram.rframe.ftc.v2.Robot.launcher.Flywheel;
import com.buddyram.rframe.ftc.v2.Robot.launcher.Hood;
import com.buddyram.rframe.ftc.v2.Robot.launcher.Launcher;
import com.buddyram.rframe.ftc.v2.Robot.launcher.Turret;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;

public abstract class BaseOpmode extends LinearOpMode {
    NewDecodeBot decodeBot;
    CachedOdometry<Pose3D> cachedOdometry;


    public static final Pose3D DEFAULT_POSITION = new Pose3D( // pose
            new Vector3D(0, 0, 0), //            new Vector3D(0, 9, 0), // position
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
                    this.decodeBot.controlIntake();
                    this.decodeBot.autoAim();
                    this.decodeBot.adjustFlywheelSpeed();
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
            this.decodeBot.launcher.turret.setAngle(0);
            this.cachedOdometry.cleanup();
            Thread.sleep(100);

        }

    }


    public abstract void execute() throws RobotException, InterruptedException;
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
                if (gamepad1.cross) {
                    isRed = 0;
                }
                if (gamepad1.circle) {
                    isRed = 1;
                }
            }
            Globals.IS_RED = isRed == 1;
        } else {
            isRed = Globals.IS_RED ? 1 : 0;
        }
        telemetry.addData("isRed", isRed);




        Limelight3A limelightDEVICE = hardwareMap.get(Limelight3A.class, "limelight");

        LimelightOdometry limelight = new LimelightOdometry(limelightDEVICE) {
            @Override
            public Pose3D get() {
                return super.getMT1();
            }
        };
        /*
         * Starts polling for data.
         */
        if (Globals.POSITION == null) {
            try {
                limelight.init();
            } catch (Exception e) {
                limelight.initWithBackup(BotUtilsNew.mirrorIfRed(52.88, isRed == 1));
            }
        } else {
            telemetry.addLine("Used Backup Position Value");
            limelight.initWithBackup(Globals.POSITION.rotation.z);
        }


        SparkFunOTOSOdometry otosOdometry = new SparkFunOTOSOdometry(
                hardwareMap.get(SparkFunOTOS.class, "otos"),
                BotUtilsNew.mirrorIfRed(DEFAULT_POSITION, isRed == 1)
        );
        if (!otosOdometry.init()) {
            telemetry.addData("OTOS", "Failed");
        }
        try {
            otosOdometry.setPosition(limelight.get());
            telemetry.addData("OTOS", "used limelight");
        } catch (RuntimeException e) {
            if (Globals.POSITION == null) {
                telemetry.addData("OTOS", "used auto starting");
                otosOdometry.setPosition(
                        new Pose3D(
                                BotUtilsNew.mirrorIfRed(new Vector3D(-44.19 + 72, 58.26 + 72, 0), isRed == 1),
                                new Vector3D(0, 0, BotUtilsNew.mirrorIfRed(52.88, isRed == 1)),
                                new Vector3D(),
                                new Vector3D()
                        )
                );
            } else {
                otosOdometry.setPosition(Globals.POSITION);
                telemetry.addData("OTOS", "used cached");
            }
        }

        telemetry.update();
        Thread.sleep(1000);

        GroundingOdometry<Pose3D> groundingOdometry = new GroundingOdometry<>(limelight, otosOdometry, () -> {
            Pose3D res = otosOdometry.get();
            double otosYaw = res.rotation.z;
            double limelightYaw = otosYaw <= 0 ? otosYaw + 180 : otosYaw - 180;
            limelight.updateOrientation(limelightYaw);
            System.out.println("res: " + res + " \nmagnitude: " + res.positionVelocity.magnitude());
            return false;
            //return res.positionVelocity.magnitude() + res.rotationVelocity.magnitude() < 0.7;
        });


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

        this.cachedOdometry = new CachedOdometry<>(groundingOdometry);
        HolonomicPositionDriveAdapter adapter = new HolonomicPositionDriveAdapter(drive, this.cachedOdometry);
        adapter.init();



        DcMotorEx motorFly = hardwareMap.get(DcMotorEx.class, "LFly");
        motorFly.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFly.setVelocity(0);
        PIDFCoefficients pidNew = new PIDFCoefficients(354, 0, 0, 20);
        motorFly.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, pidNew);

        DcMotor motorInt = hardwareMap.get(DcMotor.class, "intake");
        motorInt.setPower(0);
        telemetry.addData("Status", "Initialized");
        DcMotor turret = hardwareMap.get(DcMotor.class, "turret");
        if (reset) {
            turret.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        }
        turret.setTargetPosition(0);
        turret.setPower(0.4);
        turret.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        Servo blocker = hardwareMap.get(Servo.class, "blocker");
        Servo hood = hardwareMap.get(Servo.class, "hood");
        Launcher launcher = new Launcher(
                this.decodeBot,
                new Flywheel(this.decodeBot, new RPMMotor(motorFly, 28)),
                new Turret(this.decodeBot, turret),
                new Hood(this.decodeBot, hood),
                new Blocker(this.decodeBot, blocker)
        );
        PIDFCoefficients pidOrig = motorFly.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER);
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
        Intake intake = new Intake(this.decodeBot, new Sweeper(this.decodeBot, motorInt));
        this.decodeBot.init(logger, new CachedOdometry<>(otosOdometry), adapter, launcher, intake, limelight, isRed == 1);
        while (! this.isStarted()) {
            telemetry.addData("P,I,D (orig)", "%.04f, %.04f, %.0f, %.04f",
                    pidOrig.p, pidOrig.i, pidOrig.d, pidOrig.f);
            groundingOdometry.sync();
            telemetry.addData("POS", groundingOdometry.get());
            telemetry.addData("POSotos", otosOdometry.get());
            telemetry.addData("POSlime", limelight.get());
            telemetry.update();
        }
    }
}
