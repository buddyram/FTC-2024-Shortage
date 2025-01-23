package org.firstinspires.ftc.teamcode.opmodes;

import com.buddyram.rframe.BaseLogger;
import com.buddyram.rframe.Logger;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.SmartLogWrapper;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.drive.HolonomicPositionDriveAdapter;
import com.buddyram.rframe.drive.MecanumDriveTrain;
import com.buddyram.rframe.ftc.Motor;
import com.buddyram.rframe.ftc.SparkFunOTOSOdometry;
import com.buddyram.rframe.ftc.intothedeep.ShortageBot;
import com.buddyram.rframe.ftc.intothedeep.arm.Claw;
import com.buddyram.rframe.ftc.intothedeep.arm.Elbow;
import com.buddyram.rframe.ftc.intothedeep.arm.Extension;
import com.buddyram.rframe.ftc.intothedeep.arm.RobotArm;
import com.buddyram.rframe.ftc.intothedeep.arm.Shoulder;
import com.buddyram.rframe.ftc.intothedeep.arm.Wrist;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;

public abstract class BaseOpmode extends LinearOpMode {
    public static final Pose3D DEFAULT_POSITION = new Pose3D( // pose
            new Vector3D(87, 8.5, 0), // position
            new Vector3D(0, 0, 180), // rotation
            new Vector3D(0, 0, 0), // position velocity
            new Vector3D(0, 0, 0) // rotation velocity
    );

    public static Pose3D currentPosition = DEFAULT_POSITION;

    protected ShortageBot shortageBot;

    @Override
    public void runOpMode() throws InterruptedException {
        this.initializeHardware();
        this.waitForStart();
        this.shortageBot.getLogger().track("odometry", () -> this.shortageBot.getOdometry().get(), shortageBot);

        Thread rememberLastPosition = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                if (opModeIsActive()) {
                    BaseOpmode.currentPosition = this.shortageBot.getOdometry().get();
                    Thread.yield();
                }
            }
        });
        rememberLastPosition.setPriority(Thread.MIN_PRIORITY);
        rememberLastPosition.start();


        try {
            this.execute();
        } catch (RobotException e) {
            throw new RuntimeException(e);
        } finally {
            rememberLastPosition.interrupt();
            rememberLastPosition.join();
        }

    }

    public abstract void execute() throws RobotException, InterruptedException;

    public void initializeHardware() throws InterruptedException {
        DcMotor motorFR = hardwareMap.get(DcMotor.class, "motorFR");
        DcMotor motorFL = hardwareMap.get(DcMotor.class, "motorFL");
        DcMotor motorBR = hardwareMap.get(DcMotor.class, "motorBR");
        DcMotor motorBL = hardwareMap.get(DcMotor.class, "motorBL");
        SparkFunOTOSOdometry odometry = new SparkFunOTOSOdometry(
            hardwareMap.get(SparkFunOTOS.class, "otos"),
            BaseOpmode.currentPosition
        );
        odometry.init();


        DcMotor armext = hardwareMap.get(DcMotor.class, "armext");
        DcMotor armrotL = hardwareMap.get(DcMotor.class, "armrotL");
        DcMotor armrotR = hardwareMap.get(DcMotor.class, "armrotR");
        Servo armElbowL = hardwareMap.get(Servo.class, "armElbowL");
        Servo armElbowR = hardwareMap.get(Servo.class, "armElbowR");
        Servo claw = hardwareMap.get(Servo.class, "claw");
        Servo wrist = hardwareMap.get(Servo.class, "wrist");
        RevColorSensorV3 colorSensor = hardwareMap.get(RevColorSensorV3.class, "color");
        DigitalChannel frontBumper = hardwareMap.get(DigitalChannel.class, "bumper_front");
        DigitalChannel backBumper = hardwareMap.get(DigitalChannel.class, "bumper_back");
        armrotL.setTargetPosition(0);
        armrotR.setTargetPosition(0);
        armrotL.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armrotR.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armrotL.setPower(0.5);
        armrotR.setPower(0.5);
        claw.setPosition(0);
        this.shortageBot = new ShortageBot() {
            public boolean isActive() {
                return opModeIsActive();
            }
        };
        RobotArm arm = new RobotArm(
            new Claw(claw, this.shortageBot),
            new Wrist(wrist, this.shortageBot),
            new Elbow(armElbowL, armElbowR, this.shortageBot),
            new Extension(armext, this.shortageBot),
            new Shoulder(armrotL, armrotR, this.shortageBot),
            this.shortageBot
        );

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
//        MecanumDriveTrain drive = new MecanumDriveTrain(
//                new Motor(motorFL, -1),
//                new Motor(motorFR, -1),
//                new Motor(motorBL, -1),
//                new Motor(motorBR),
//                0.7
//        );
        MecanumDriveTrain drive = new MecanumDriveTrain(
                new Motor(motorFL, -1),
                new Motor(motorFR, -1),
                new Motor(motorBL, -1),
                new Motor(motorBR, -1),
                1
        );

        HolonomicPositionDriveAdapter adapter = new HolonomicPositionDriveAdapter(drive, odometry);
        adapter.init();

        this.shortageBot.init(
            logger,
            adapter,
            odometry,
            arm,
            frontBumper,
            backBumper
        );
    }
}
