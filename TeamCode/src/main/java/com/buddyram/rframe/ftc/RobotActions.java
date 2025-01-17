package com.buddyram.rframe.ftc;

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
import com.buddyram.rframe.ftc.intothedeep.arm.ArmUtils;
import com.buddyram.rframe.ftc.intothedeep.arm.Claw;
import com.buddyram.rframe.ftc.intothedeep.arm.Elbow;

public class RobotActions {
    public static final DriveTowardsAction DRIVE_FORWARD = new DriveTowardsAction(new Vector3D(0, 1000, 0), true);
    public static final DriveTowardsAction DRIVE_BACKWARD = new DriveTowardsAction(new Vector3D(0, -1000, 0), true);
    public static final RobotAction REST = ArmUtils.positionArmAtRest(0,1, 0);
    public static final RobotAction STOP = new StopDrivingAction();
    public static final RobotAction GRAB_FROM_WALL = ArmUtils.positionArmAtRest(0.45,0.25, 0);
    public static final RobotAction SPECIMEN_HANG = ArmUtils.positionArm(0.45, 0.56,0, 0);
    public static final RobotAction SPECIMEN_HANG_ELBOW_DOWN = Elbow.moveTo(0.34);
    public static final RobotAction RELEASE_CLAW = Claw.moveTo(Claw.OPEN);
    public static final RobotAction CLOSE_CLAW = Claw.moveTo(Claw.CLOSE);
    public static final ConditionalWrapperAction COLLISION_HANG = new ConditionalWrapperAction(
        new MultiAction(DRIVE_FORWARD, SPECIMEN_HANG), new FrontBumperCondition()
    );
    public static final ConditionalWrapperAction REVERSE_TO_SAFETY = new ConditionalWrapperAction(
        DRIVE_BACKWARD,
        (drive) -> drive.getOdometry().get().position.y < 24
    );
    public static final MultiAction COLLISION_HANG_RELEASE = new MultiAction(
        CLOSE_CLAW, COLLISION_HANG, SPECIMEN_HANG_ELBOW_DOWN, ArmUtils.wait(300), RELEASE_CLAW, REST, ArmUtils.wait(300)
    );
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