package org.firstinspires.ftc.teamcode.opmodes;

import com.buddyram.rframe.ftc.LimelightOdometry;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

@TeleOp(name = "Limelight", group = "aLimelight")
public class Limelight extends LinearOpMode {

    private Limelight3A limelightDEVICE;

    @Override
    public void runOpMode() throws InterruptedException
    {
        limelightDEVICE = hardwareMap.get(Limelight3A.class, "limelight");

        LimelightOdometry limelight = new LimelightOdometry(limelightDEVICE);

        /*
         * Starts polling for data.
         */
        limelight.init();
        waitForStart();
        while (opModeIsActive()) {
            telemetry.addData("pos", limelight.get());
            telemetry.update();
        }
    }
}
