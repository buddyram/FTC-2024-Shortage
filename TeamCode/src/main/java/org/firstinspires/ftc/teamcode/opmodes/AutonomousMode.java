package org.firstinspires.ftc.teamcode.opmodes;

import com.buddyram.rframe.RobotException;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Autonomous Specimen", group = "Comp", preselectTeleOp = "Teleop")
public class AutonomousMode extends BaseOpmode {

    @Override
    public void execute() throws RobotException, InterruptedException {
        this.shortageBot.runAutonomous();
    }
}
