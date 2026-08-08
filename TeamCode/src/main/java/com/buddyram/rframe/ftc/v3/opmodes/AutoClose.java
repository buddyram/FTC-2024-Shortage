package com.buddyram.rframe.ftc.v3.opmodes;

import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.ftc.v3.NewDecodeBot.AutoSequenceConfig;
import com.buddyram.rframe.ftc.v3.NewDecodeBot.AutoStep;
import com.buddyram.rframe.ftc.v3.Globals;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Auto Close 12", group = "Decode")
public class AutoClose extends BaseOpmode {
    @Override
    public void execute() throws RobotException, InterruptedException {
        Globals.DID_RUN_AUTO = true;
        decodeBot.runAutoSequence(new AutoSequenceConfig(
            /* firstShotPos */ new Vector3D(50, 84, 0),
            /* shootPos */     new Vector3D(60, 120, 0),
            /* shootHeading */ 90,
            /* steps */        new AutoStep[]{AutoStep.SHOOT, AutoStep.MIDDLE, AutoStep.OPEN_GATE_LEFT, AutoStep.SHOOT, AutoStep.NEAR, AutoStep.SHOOT, AutoStep.FAR, AutoStep.SHOOT},
            /* parkPos */      new Vector3D(24, 72, 0)
        ));
    }
}
