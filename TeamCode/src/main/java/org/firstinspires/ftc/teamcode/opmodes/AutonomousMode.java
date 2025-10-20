package org.firstinspires.ftc.teamcode.opmodes;

import com.buddyram.rframe.RobotException;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Autonomous", group = "Comp")
public class AutonomousMode extends BaseOpmode {

    @Override
    public void execute() throws RobotException, InterruptedException {
        this.decodeBot.runAutonomous();
    }
}