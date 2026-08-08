package org.firstinspires.ftc.teamcode.opmodes;

import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.ftc.LimelightOdometry;
import com.buddyram.rframe.ftc.SparkFunOTOSOdometry;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Limelight", group = "aLimelight")
public class Limelight extends LinearOpMode {

    private Limelight3A limelightDEVICE;

    @Override
    public void runOpMode() throws InterruptedException
    {
        limelightDEVICE = hardwareMap.get(Limelight3A.class, "limelight");
        SparkFunOTOSOdometry otos = new SparkFunOTOSOdometry(
                hardwareMap.get(SparkFunOTOS.class, "otos"),
                new Pose3D()
        );

        LimelightOdometry limelight = new LimelightOdometry(limelightDEVICE);

        /*
         * Starts polling for data.
         */
        limelight.init();
        otos.init();
        otos.setPosition(limelight.get());
        telemetry.addData("starting pos", limelight.get());
        telemetry.update();
        waitForStart();
        while (opModeIsActive()) {
            double otosYaw = otos.get().rotation.z;
            double limelightYaw = otosYaw <= 0 ? otosYaw + 180 : otosYaw - 180;
            limelight.updateOrientation(limelightYaw);

            telemetry.addData("pos", limelight.get());
            telemetry.addData("rotz", otosYaw);
            telemetry.update();
        }
        limelightDEVICE.stop();
    }
}
