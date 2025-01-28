package org.firstinspires.ftc.teamcode.opmodes;

import android.annotation.SuppressLint;

import com.buddyram.rframe.Color;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.actions.MultiAction;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.ftc.intothedeep.BotUtils;
import com.buddyram.rframe.ftc.intothedeep.actions.BackwardsClipAction;
import com.buddyram.rframe.ftc.intothedeep.actions.RobotActions;
import com.buddyram.rframe.ftc.intothedeep.actions.SpecimenCollectAction;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;


/*
 OFF	0	0	1	Rest
-671	-3080	0	0.1	High Basket
-246	0	1	0.06	Far Reaching Pickup Init
OFF	0	1	0.19	Short Reaching Pickup Init
-335	0	0	0.35	Specimen Approach
-165	0	0	0.35	Specimen Hang
 */
@TeleOp(name = "Teleop", group = "Comp")
public class TeleopMode extends BaseOpmode {
    private static final double ELBOW_SPEED = 0.02;
    private static final double WRIST_SPEED = 0.02;
    public final Color YELLOW = new Color(452, 533, 144, 376);
    public final Color RED = new Color(276, 165, 97, 180);
    public final Color BLUE = new Color(72, 139, 272, 161);
    public final Color FLOOR = new Color(62, 106, 91, 86);
    public final int MAX_SHOULDER_SPEED = 30;

    @SuppressLint("DefaultLocale")
    @Override
    public void execute() throws InterruptedException, RobotException {
        Gamepad previousGamepad1 = new Gamepad();
        Gamepad currentGamepad1 = new Gamepad();
        Gamepad previousGamepad2 = new Gamepad();
        Gamepad currentGamepad2 = new Gamepad();
        boolean mode = true;
        while (opModeIsActive()) {
            previousGamepad1.copy(currentGamepad1);
            previousGamepad2.copy(currentGamepad1);
            currentGamepad1.copy(gamepad1);
            currentGamepad2.copy(gamepad2);
            if (mode) {
                shortageBot.getLogger().log("gamepad1 left sticks", currentGamepad1.left_stick_x + ", " + -currentGamepad1.left_stick_y);
                shortageBot.getLogger().log("gamepad1 right stick", currentGamepad1.right_stick_x);
                shortageBot.getLogger().log("g2 triggers",
                        String.format("l: .%2f, r: .%2f", currentGamepad2.left_trigger, currentGamepad2.right_trigger));
                shortageBot.getLogger().log("shoulder delta", Math.round((currentGamepad2.left_trigger - currentGamepad2.right_trigger) * MAX_SHOULDER_SPEED));
                shortageBot.getLogger().log("elbow", this.shortageBot.getArm().elbow.getPosition());
                shortageBot.getLogger().log("wrist", this.shortageBot.getArm().wrist.getPosition());
                this.shortageBot.getArm().angle.incrementTargetPosition(Math.round((currentGamepad2.right_trigger - currentGamepad2.left_trigger) * MAX_SHOULDER_SPEED));
                this.shortageBot.getArm().elbow.incrementTargetPosition(currentGamepad2.dpad_left ? ELBOW_SPEED : currentGamepad2.dpad_right ? -ELBOW_SPEED : 0);
                this.shortageBot.getArm().wrist.incrementTargetPosition(currentGamepad2.dpad_down ? -WRIST_SPEED : currentGamepad2.dpad_up ? WRIST_SPEED : 0);
                if (currentGamepad2.left_bumper) {
                    RobotActions.RELEASE_CLAW.run(shortageBot);
                } else if (currentGamepad2.right_bumper) {
                    RobotActions.CLOSE_CLAW.run(shortageBot);
                }


                runDriveControls(currentGamepad1);


                this.shortageBot.getArm().angle.incrementTargetPosition(Math.round((currentGamepad2.right_trigger - currentGamepad2.left_trigger) * MAX_SHOULDER_SPEED));

            } else {
                if (currentGamepad1.y) {
                    new BackwardsClipAction().run(shortageBot);
                } else if (currentGamepad1.back) {
                    this.shortageBot.getOdometry().setPosition(BaseOpmode.DEFAULT_POSITION);
                }

                if (currentGamepad1.x) {
                    RobotActions.GO_TO_GRAB_FROM_WALL_POSITION.run(this.shortageBot);
                }
                if (currentGamepad1.a) {
                    new SpecimenCollectAction().run(this.shortageBot);
                }

                if (currentGamepad1.dpad_up) {
//                    new MultiAction<>(
//                            BotUtils.driveTo(new Vector3D(117, 58, 0), true),
//                            BotUtils.driveTo(new Vector3D(117, 58, 0))
//
//                    ).run(this.shortageBot);
                }

                if (currentGamepad1.b) {
                    RobotActions.REST.run(this.shortageBot);
                }

                if (currentGamepad1.dpad_down) {
                    RobotActions.PICKUP_SHORT_SCAN_POSITION.run(this.shortageBot);
                }
                if (currentGamepad1.dpad_left) {
                    RobotActions.PICKUP_SHORT_GRAB_POSITION.run(this.shortageBot);
                }
            }
            if (currentGamepad1.start && !previousGamepad1.start) {
                mode = !mode;
            }
        }
    }

    private void runDriveControls(Gamepad currentGamepad1) throws RobotException {
        if (currentGamepad1.x) {
            BotUtils.rotateTo(90).run(this.shortageBot);
        } else if (currentGamepad1.b) {
            BotUtils.rotateTo(-90).run(this.shortageBot);
        } else if (currentGamepad1.y) {
            BotUtils.rotateTo(0).run(this.shortageBot);
        } else if (currentGamepad1.a) {
            BotUtils.rotateTo(180).run(this.shortageBot);
        }

        double speed = 0.7;

        if (currentGamepad1.left_bumper) {
            speed = 1;
        } else if (currentGamepad1.right_bumper) {
            speed = 0.5;
        }

        if (currentGamepad1.dpad_up) {
            this.shortageBot.getDrive().drive(this.shortageBot.calculateRelativeDriveInstruction(new Vector3D(0, 1, 0), speed));
        } else if (currentGamepad1.dpad_down) {
            this.shortageBot.getDrive().drive(this.shortageBot.calculateRelativeDriveInstruction(new Vector3D(0, -1, 0), speed));
        } else if (currentGamepad1.dpad_right) {
            this.shortageBot.getDrive().drive(this.shortageBot.calculateRelativeDriveInstruction(new Vector3D(1, 0, 0), speed));
        } else if (currentGamepad1.dpad_left) {
            this.shortageBot.getDrive().drive(this.shortageBot.calculateRelativeDriveInstruction(new Vector3D(-1, 0, 0), speed));
        } else {
            double speedLevel = Math.sqrt(Math.pow(currentGamepad1.left_stick_x, 2) + Math.pow(currentGamepad1.left_stick_y, 2));
            HolonomicDriveInstruction i = this.shortageBot.calculateRelativeDriveInstruction(
                new Vector3D(currentGamepad1.left_stick_x, -currentGamepad1.left_stick_y, 0),
                speed * speedLevel);
            shortageBot.getDrive().drive(new HolonomicDriveInstruction(currentGamepad1.right_stick_x, i.speed, i.direction + this.shortageBot.getOdometry().get().rotation.z));
        }
    }
}
