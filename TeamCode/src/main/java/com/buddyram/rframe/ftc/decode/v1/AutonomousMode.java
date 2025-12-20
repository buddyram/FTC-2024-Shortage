package com.buddyram.rframe.ftc.decode.v1;

import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.ftc.decode.Globals;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Autonomous", group = "Decode")
public class AutonomousMode extends BaseOpmode {
    @Override
    public void execute() throws RobotException, InterruptedException {
        Globals.DID_RUN_AUTO = true;
        this.decodeBot.runAutonomous(true);
    }
}