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

public class RobotArmActions {
    public static final ArmActionWithRestedAngle REST = new ArmActionWithRestedAngle(0, 1, 0);
    public static final ArmPositionalAction HIGH_BASKET = new ArmPositionalAction(0, 0, 3080, -671);
    public static final ArmPositionalAction FAR_REACHING_PICKUP = new ArmPositionalAction(1, 0.06, 0, -246);
    public static final ArmActionWithRestedAngle SHORT_REACHING_PICKUP = new ArmActionWithRestedAngle(1, 0.19, 0);
    public static final ArmPositionalAction SPECIMEN_APPROACH = new ArmPositionalAction(0, 1, 0, -464);
    public static final ArmPositionalAction SPECIMEN_HANG = new ArmPositionalAction(0, 0, 0, -464);
    public static final ReleaseClaw RELEASE_CLAW = new ReleaseClaw(1);
    public static final ReleaseClaw CLOSE_CLAW = new ReleaseClaw(0);
}
