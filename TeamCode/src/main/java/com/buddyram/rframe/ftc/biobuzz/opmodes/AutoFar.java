package com.buddyram.rframe.ftc.biobuzz.opmodes;

import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.ftc.biobuzz.BiobuzzBot.AutoSequenceConfig;
import com.buddyram.rframe.ftc.biobuzz.BiobuzzBot.AutoStep;
import com.buddyram.rframe.ftc.biobuzz.BotUtilsNew;
import com.buddyram.rframe.ftc.biobuzz.Globals;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "BIOBUZZ Auto Far 9", group = "Biobuzz")
public class AutoFar extends BaseOpmode {
    @Override
    public void execute() throws RobotException, InterruptedException {
        Globals.DID_RUN_AUTO = true;
        biobuzzBot.runAutoSequence(new AutoSequenceConfig(
            /* firstShotPos */     new Vector3D(60, 24, 0),
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
