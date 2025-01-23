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

import com.buddyram.rframe.actions.ConditionalWrapperAction;
import com.buddyram.rframe.actions.MultiAction;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.actions.TimeoutWrapperAction;
import com.buddyram.rframe.drive.StopDrivingAction;
import com.buddyram.rframe.ftc.DriveTowardsAction;
import com.buddyram.rframe.ftc.intothedeep.BackBumperCondition;
import com.buddyram.rframe.ftc.intothedeep.ShortageBot;
import com.buddyram.rframe.ftc.intothedeep.BotUtils;
import com.buddyram.rframe.ftc.intothedeep.arm.Claw;
import com.buddyram.rframe.ftc.intothedeep.arm.Elbow;
import com.buddyram.rframe.ftc.intothedeep.arm.Shoulder;

public class RobotActions {
    public static final DriveTowardsAction DRIVE_FORWARD = new DriveTowardsAction(DriveTowardsAction.FORWARD_DIRECTION, true);
    public static final DriveTowardsAction DRIVE_BACKWARD = new DriveTowardsAction(DriveTowardsAction.BACKWARD_DIRECTION, true);
    public static final DriveTowardsAction DRIVE_LEFT = new DriveTowardsAction(DriveTowardsAction.LEFT_DIRECTION, true);
    public static final DriveTowardsAction DRIVE_RIGHT = new DriveTowardsAction(DriveTowardsAction.RIGHT_DIRECTION, true);
    public static final RobotAction<ShortageBot> REST = BotUtils.positionArmAtRest(0,1, 0);
    public static final RobotAction<ShortageBot> STOP = new StopDrivingAction<>();
    public static final RobotAction<ShortageBot> GRAB_FROM_WALL = BotUtils.positionArmAtRest(0.45,0.25, 0);
    public static final ShortageAction RELEASE_CLAW = Claw.moveTo(Claw.OPEN);
    public static final ShortageAction CLOSE_CLAW = Claw.moveTo(Claw.CLOSE);
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