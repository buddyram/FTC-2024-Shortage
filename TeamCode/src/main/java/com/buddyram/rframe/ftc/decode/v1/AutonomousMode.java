package com.buddyram.rframe.ftc.decode.v1;

import com.buddyram.rframe.RobotException;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Autonomous", group = "Comp")
public class AutonomousMode extends BaseOpmode {
    @Override
    public void execute() throws RobotException, InterruptedException {
        this.decodeBot.runAutonomous(true);
    }
}