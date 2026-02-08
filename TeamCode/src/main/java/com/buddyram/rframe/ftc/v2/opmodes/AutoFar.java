package com.buddyram.rframe.ftc.v2.opmodes;

import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.ftc.v2.BotUtilsNew;
import com.buddyram.rframe.ftc.v2.Globals;
import com.buddyram.rframe.ftc.v2.NewDecodeBot.AutoSequenceConfig;
import com.buddyram.rframe.ftc.v2.NewDecodeBot.AutoStep;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Auto Far 9", group = "Decode")
public class AutoFar extends BaseOpmode {
    @Override
    public void execute() throws RobotException, InterruptedException {
        Globals.DID_RUN_AUTO = true;
        decodeBot.runAutoSequence(new AutoSequenceConfig(
            /* shootPos */           new Vector3D(60, 24, 0),
            /* shootHeading */       90,
            /* steps */              new AutoStep[]{AutoStep.SHOOT, AutoStep.FAR, AutoStep.SHOOT, AutoStep.MIDDLE, AutoStep.SHOOT},
            /* parkPos */            new Vector3D(30, 36, 0),
            /* firstTurretWaitMs */  2000,
            /* turretWaitMs */       500
        ));
    }

    @Override
    protected Vector3D getBlueStartPosition() { return new Vector3D(48, 8.86, 0); }
    @Override
    protected double getBlueStartHeading() { return 90; }
    @Override
    protected Vector3D getRedStartPosition() {
        return BotUtilsNew.mirrorIfRed(new Vector3D(48, 8.86, 0), true);
    }
    @Override
    protected double getRedStartHeading() { return BotUtilsNew.mirrorIfRed(90, true); }
}
