package com.buddyram.rframe.ftc.intothedeep.actions;

import static com.buddyram.rframe.ftc.intothedeep.actions.RobotActions.CLOSE_CLAW;
import static com.buddyram.rframe.ftc.intothedeep.actions.RobotActions.RELEASE_CLAW;
import static com.buddyram.rframe.ftc.intothedeep.actions.RobotActions.REST;
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

public class BackwardsClipAction implements RobotAction<ShortageBot> {
    public static final DriveTowardsAction DRIVE_FORWARD = new DriveTowardsAction(DriveTowardsAction.FORWARD_DIRECTION, true, 0.7);
    public static final DriveTowardsAction DRIVE_BACKWARD = new DriveTowardsAction(DriveTowardsAction.BACKWARD_DIRECTION, true, 0.7);
    public static final DriveTowardsAction DRIVE_LEFT = new DriveTowardsAction(DriveTowardsAction.LEFT_DIRECTION, true, 0.7);
    public static final DriveTowardsAction DRIVE_RIGHT = new DriveTowardsAction(DriveTowardsAction.RIGHT_DIRECTION, true, 0.7);
    public static final RotateToAction<ShortageBot> ALIGN_BACKWARDS = new RotateToAction<>(180, 5);
    public static final RobotAction<ShortageBot> POSITION_ARM = BotUtils.positionArm(0, 1,0, -200);
    public static final ShortageAction HIT_TOP_BAR = Elbow.moveTo(0.67);
    public static final ShortageAction SPECIMEN_HANG_SHOULDER_DOWN = Shoulder.moveTo(-91);
    public static final MultiAction<ShortageBot> CLIP_RELEASE = new MultiAction<>(RELEASE_CLAW, Shoulder.moveTo(-200), Elbow.moveTo(1));
    public static final ConditionalWrapperAction<ShortageBot> BUMP_SUBMERSIBLE = new ConditionalWrapperAction<>(
            new MultiAction<>(DRIVE_BACKWARD), new BackBumperCondition()
    );
    public static final ConditionalWrapperAction<ShortageBot> DRIVE_FORWARD_CLIP = new ConditionalWrapperAction<>(
            DRIVE_FORWARD,
            (drive) -> drive.getOdometry().get().position.y < 24
    );
    public static final MultiAction<ShortageBot> COLLISION_HANG_RELEASE = new MultiAction<>(
            CLOSE_CLAW,
            POSITION_ARM,
            ALIGN_BACKWARDS,
            BUMP_SUBMERSIBLE,
            HIT_TOP_BAR,
            BotUtils.wait(500),
            SPECIMEN_HANG_SHOULDER_DOWN,
            BotUtils.wait(500),
            new TimeoutWrapperAction<>(DRIVE_FORWARD_CLIP, 500),
            STOP,
            CLIP_RELEASE,
            BotUtils.wait(500),
            REST,
            BotUtils.wait(500)
    );
    private static final double MIN_X = 60;
    private static final double MAX_X = 84;
    private static final double MIN_Y = Math.sqrt(162);
    private static final double MAX_Y = 36;

    @Override
    public boolean run(ShortageBot drive) throws RobotException {
        Vector3D position = drive.getOdometry().get().position;
        if (position.x >= MIN_X && position.x <= MAX_X &&
            position.y >= MIN_Y && position.y <= MAX_Y) {
            return COLLISION_HANG_RELEASE.run(drive);
        }
        return false;
    }
}
