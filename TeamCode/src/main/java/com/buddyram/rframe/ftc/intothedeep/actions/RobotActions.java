package com.buddyram.rframe.ftc.intothedeep.actions;

/*
Angle	Extension	Claw	Wrist	Name
OFF	0	0	1	Rest
-671	-3080	0	0.1	High Basket
-246	0	1	0.06	Far Reaching Pickup Init
OFF	0	1	0.19	Short Reaching Pickup Init
-335	0	0	0.35	Specimen Approach
-165	0	0	0.35	Specimen Hang
 */

import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.actions.MultiAction;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.drive.StopDrivingAction;
import com.buddyram.rframe.ftc.DriveTowardsAction;
import com.buddyram.rframe.ftc.intothedeep.ShortageBot;
import com.buddyram.rframe.ftc.intothedeep.BotUtils;
import com.buddyram.rframe.ftc.intothedeep.arm.Claw;

public class RobotActions {
    public static final DriveTowardsAction DRIVE_FORWARD = new DriveTowardsAction(DriveTowardsAction.FORWARD_DIRECTION, true);
    public static final DriveTowardsAction DRIVE_BACKWARD = new DriveTowardsAction(DriveTowardsAction.BACKWARD_DIRECTION, true);
    public static final DriveTowardsAction DRIVE_LEFT = new DriveTowardsAction(DriveTowardsAction.LEFT_DIRECTION, true);
    public static final DriveTowardsAction DRIVE_RIGHT = new DriveTowardsAction(DriveTowardsAction.RIGHT_DIRECTION, true);
    public static final ShortageAction RELEASE_CLAW = Claw.moveTo(Claw.OPEN);
    public static final ShortageAction CLOSE_CLAW = Claw.moveTo(Claw.CLOSE);
    public static final RobotAction<ShortageBot> PICKUP_SHORT = BotUtils.positionArmAtRest(0.22,0.1989, 0);
    public static final RobotAction<ShortageBot> PICKUP_SHORT_GRAB = BotUtils.positionArmAtRest(0.22,0.15, 0);// 126, 31      turn 180      135, 31, 180, 117, 30
    public static final RobotAction<ShortageBot> REST = BotUtils.positionArmAtRest(0,1, 0);
    public static final RobotAction<ShortageBot> STOP = new StopDrivingAction<>();
    public static final RobotAction<ShortageBot> GRAB_FROM_WALL = BotUtils.positionArmAtRest(0.4394,0.3372, 0); // 117, 21
    public static final RobotAction<ShortageBot> GO_TO_GRAB_FROM_WALL_POSITION = new MultiAction<>(BotUtils.driveTo(new Vector3D(114, 25, 0), true), BotUtils.rotateTo(180), GRAB_FROM_WALL);
    public static final RobotAction<ShortageBot> PICKUP_SHORT_SCAN_POSITION = new MultiAction<>(RobotActions.RELEASE_CLAW, RobotActions.PICKUP_SHORT, BotUtils.wait(300));
    public static final RobotAction<ShortageBot> PICKUP_SHORT_GRAB_POSITION = new MultiAction<>(RobotActions.RELEASE_CLAW, RobotActions.PICKUP_SHORT_GRAB, BotUtils.wait(300));
    public static final RobotAction<ShortageBot> PICKUP_ARM_UP = BotUtils.positionArmAtRest(0.22,0.2989, 0);


}

//Grab from wall
//Angle 0
//Extension 0
//Claw 1
//Wrist 0.45
//Elbow 0.25
//Clipping position
//Angle -380
//Extension 0
//Claw 1
//Wrist 0.72
//Elbow 0.1