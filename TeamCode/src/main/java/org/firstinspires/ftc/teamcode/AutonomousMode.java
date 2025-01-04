package org.firstinspires.ftc.teamcode;

import com.buddyram.rframe.HolonomicPositionDriveAdapter;
import com.buddyram.rframe.Logger;
import com.buddyram.rframe.MecanumDriveTrain;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.ftc.ArmShoulder;
import com.buddyram.rframe.ftc.Motor;
import com.buddyram.rframe.ftc.RobotArm;
import com.buddyram.rframe.ftc.SparkFunOTOSOdometry;
import com.buddyram.rframe.ftc.intothedeep.AutonomousDrive;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@Autonomous(name = "Autonomous - v1", group = "Sensor")
public class AutonomousMode extends LinearOpMode {

    public void initializeArm() {
        DcMotor armext = hardwareMap.get(DcMotor.class, "armext");
        DcMotor armrotL = hardwareMap.get(DcMotor.class, "armrotL");
        DcMotor armrotR = hardwareMap.get(DcMotor.class, "armrotR");
        Servo claw = hardwareMap.get(Servo.class, "claw");
        Servo wrist = hardwareMap.get(Servo.class, "wrist");
        armrotL.setTargetPosition(0);
        armrotR.setTargetPosition(0);
        armrotL.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armrotR.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armrotL.setPower(0.5);
        armrotR.setPower(0.5);
        claw.setPosition(0);
    }

    @Override
    public void runOpMode() throws InterruptedException {
        DcMotor motorFR = hardwareMap.get(DcMotor.class, "motorFR");
        DcMotor motorFL = hardwareMap.get(DcMotor.class, "motorFL");
        DcMotor motorBR = hardwareMap.get(DcMotor.class, "motorBR");
        DcMotor motorBL = hardwareMap.get(DcMotor.class, "motorBL");
        SparkFunOTOSOdometry odometry = new SparkFunOTOSOdometry(
                hardwareMap.get(SparkFunOTOS.class, "otos"),
                new Pose3D( // pose
                        new Vector3D(96, 8.5, 0), // position
                        new Vector3D(0, 0, 0), // rotation
                        new Vector3D(0, 0, 0), // position velocity
                        new Vector3D(0, 0, 0)) // rotation velocity
        );
        odometry.init();


        DcMotor armext = hardwareMap.get(DcMotor.class, "armext");
        DcMotor armrotL = hardwareMap.get(DcMotor.class, "armrotL");
        DcMotor armrotR = hardwareMap.get(DcMotor.class, "armrotR");
        Servo claw = hardwareMap.get(Servo.class, "claw");
        Servo wrist = hardwareMap.get(Servo.class, "wrist");
        armrotL.setTargetPosition(0);
        armrotR.setTargetPosition(0);
        armrotL.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armrotR.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armrotL.setPower(0.5);
        armrotR.setPower(0.5);
        claw.setPosition(0);
        RobotArm arm = new RobotArm(claw, wrist, armext, new ArmShoulder(armrotL, armrotR));

        Logger logger = new Logger() {
            public void log(String caption, Object value) {
                telemetry.addData(caption, value);
            }

            public void flush() {
                telemetry.update();
            }
        };
//        MecanumDriveTrain drive = new MecanumDriveTrain(
//                new Motor(motorFL, -1),
//                new Motor(motorFR, -1),
//                new Motor(motorBL, -1),
//                new Motor(motorBR),
//                0.7
//        );
        MecanumDriveTrain drive = new MecanumDriveTrain(
                new Motor(motorFL),
                new Motor(motorFR),
                new Motor(motorBL),
                new Motor(motorBR),
                1
        );

        HolonomicPositionDriveAdapter adapter = new HolonomicPositionDriveAdapter(drive, odometry);
        adapter.init();

        AutonomousDrive autonomous = new AutonomousDrive(
                logger,
                adapter,
                odometry,
                arm
        ) {
            public boolean isActive() {
                return opModeIsActive();
            }
        };
        autonomous.init();
        waitForStart();
        autonomous.run();
        armrotL.setTargetPosition(800);
        armrotR.setTargetPosition(-800);
    }
}
