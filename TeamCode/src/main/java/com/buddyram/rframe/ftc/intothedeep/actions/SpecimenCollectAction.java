package com.buddyram.rframe.ftc.intothedeep.actions;

import static com.buddyram.rframe.ftc.intothedeep.actions.RobotActions.CLOSE_CLAW;
import static com.buddyram.rframe.ftc.intothedeep.actions.RobotActions.RELEASE_CLAW;
import static com.buddyram.rframe.ftc.intothedeep.actions.RobotActions.STOP;

import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.actions.ConditionalWrapperAction;
import com.buddyram.rframe.actions.MultiAction;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.actions.TimeoutWrapperAction;
import com.buddyram.rframe.drive.RotateToAction;
import com.buddyram.rframe.ftc.DriveTowardsAction;
import com.buddyram.rframe.ftc.intothedeep.BackBumperCondition;
import com.buddyram.rframe.ftc.intothedeep.BotUtils;
import com.buddyram.rframe.ftc.intothedeep.ShortageBot;
import com.buddyram.rframe.ftc.intothedeep.arm.Elbow;
import com.buddyram.rframe.ftc.intothedeep.arm.Shoulder;

public class SpecimenCollectAction implements RobotAction<ShortageBot> {
    @Override
    public boolean run(ShortageBot drive) throws RobotException {
        new MultiAction<>(
                BotUtils.rotateTo(0),
                RobotActions.PICKUP_ARM_UP,
                BotUtils.driveTo(new Vector3D(126, 31, 0)),
                BotUtils.rotateTo(0),
                RobotActions.RELEASE_CLAW,
                BotUtils.wait(200),
                RobotActions.PICKUP_SHORT_GRAB_POSITION,
                RobotActions.CLOSE_CLAW,
                BotUtils.wait(300),
                RobotActions.PICKUP_ARM_UP,
                BotUtils.wait(100),
                BotUtils.rotateTo(176),
                RobotActions.RELEASE_CLAW,
                BotUtils.wait(300),

                BotUtils.rotateTo(0),
                BotUtils.driveTo(new Vector3D(135, 31, 0)),
                BotUtils.rotateTo(0),
                RobotActions.PICKUP_ARM_UP,
                RobotActions.RELEASE_CLAW,
                BotUtils.wait(200),
                RobotActions.PICKUP_SHORT_GRAB_POSITION,
                RobotActions.CLOSE_CLAW,
                BotUtils.wait(300),
                RobotActions.PICKUP_ARM_UP,
                BotUtils.wait(100),
                BotUtils.rotateTo(160),
                RobotActions.RELEASE_CLAW,
                BotUtils.wait(300),

                BotUtils.rotateTo(40),


                BotUtils.rotateTo(-31),
                BotUtils.driveTo(new Vector3D(135, 33, 0)),
                BotUtils.rotateTo(-31),
                RobotActions.PICKUP_ARM_UP,
                RobotActions.RELEASE_CLAW,
                BotUtils.wait(200),
                RobotActions.PICKUP_SHORT_GRAB_POSITION,
                RobotActions.CLOSE_CLAW,
                BotUtils.wait(300),
                RobotActions.PICKUP_ARM_UP,
                BotUtils.wait(100),
                BotUtils.driveTo(new Vector3D(123, 24, 0)),
                BotUtils.rotateTo(-145),
                BotUtils.wait(100),
                RobotActions.RELEASE_CLAW
        ).run(drive);
        return true;
    }
}
