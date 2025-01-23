package org.firstinspires.ftc.teamcode.opmodes;

import com.buddyram.rframe.Color;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.drive.HolonomicPositionDriveAdapter;
import com.buddyram.rframe.drive.MecanumDriveTrain;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.ftc.Motor;
import com.buddyram.rframe.ftc.SparkFunOTOSOdometry;
import com.buddyram.rframe.ftc.intothedeep.BotUtils;
import com.buddyram.rframe.ftc.intothedeep.actions.BackwardsClipAction;
import com.buddyram.rframe.ftc.intothedeep.actions.RobotActions;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;


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
    public final Color YELLOW = new Color(452, 533, 144, 376);
    public final Color RED = new Color(276, 165, 97, 180);
    public final Color BLUE = new Color(72, 139, 272, 161);
    public final Color FLOOR = new Color(62, 106, 91, 86);

    @Override
    public void execute() throws InterruptedException, RobotException {
        Gamepad previousGamepad1 = new Gamepad();
        Gamepad currentGamepad1 = new Gamepad();
        boolean mode = true;
        while (opModeIsActive()) {
            previousGamepad1.copy(currentGamepad1);
            currentGamepad1.copy(gamepad1);
            if (mode) {
                HolonomicDriveInstruction i = shortageBot.calculateRelativeDriveInstruction(
                    new Vector3D(currentGamepad1.left_stick_x, -currentGamepad1.left_stick_y, 0),
                    Math.sqrt(Math.pow(currentGamepad1.left_stick_x, 2) + Math.pow(-currentGamepad1.left_stick_y, 2))
                );
                shortageBot.getDrive().drive(new HolonomicDriveInstruction(currentGamepad1.right_stick_x, i.speed, i.direction));
                shortageBot.getLogger().log("speed", i.speed);
                shortageBot.getLogger().log("gamepad left sticks", currentGamepad1.left_stick_x + ", " + -currentGamepad1.left_stick_y);
                shortageBot.getLogger().log("gamepad right stick", currentGamepad1.right_stick_x);
                System.out.println(Math.sqrt(Math.pow(currentGamepad1.left_stick_x, 2) + Math.pow(currentGamepad1.left_stick_y, 2)));
                if (currentGamepad1.left_bumper) {
                    RobotActions.RELEASE_CLAW.run(shortageBot);
                } else if (currentGamepad1.right_bumper) {
                    RobotActions.CLOSE_CLAW.run(shortageBot);
                }
            } else {
                if (currentGamepad1.y) {
                    new BackwardsClipAction().run(shortageBot);
                } else if (currentGamepad1.back) {
                    this.shortageBot.getOdometry().setPosition(BaseOpmode.DEFAULT_POSITION);
                }
            }
            if (currentGamepad1.start && !previousGamepad1.start) {
                mode = !mode;
            }
        }
    }
}
