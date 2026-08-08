package org.firstinspires.ftc.teamcode.opmodes;

import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.RobotException;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
@Disabled
@TeleOp(name = "OTOSCalibration", group = "Calibration")
public class OTOSCalibrationMode extends BaseOpmode {
    @Override
    public void execute() throws RobotException, InterruptedException {
//        this.decodeBot.odometry.getRelative().setPosition(new Pose3D());
        while (this.decodeBot.isActive()) {
            if (gamepad1.circle) {
//                this.decodeBot.odometry.getRelative().setPosition(new Pose3D());
            }
//            Pose3D position = this.decodeBot.odometry.getRelative().get();
//            telemetry.addData("OTOS Location", position.position + ",r" + position.rotation.z);
            telemetry.update();
        }
    }
}
