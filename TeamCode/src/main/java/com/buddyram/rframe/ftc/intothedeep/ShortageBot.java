package com.buddyram.rframe.ftc.intothedeep;

import com.buddyram.rframe.PrintLogger;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.drive.HolonomicDriveTrain;
import com.buddyram.rframe.Logger;
import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.Utils;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.drive.Navigatable;
import com.buddyram.rframe.drive.VirtualHolonomicDriveTrain;
import com.buddyram.rframe.drive.VirtualOdometrySensor;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.ftc.intothedeep.actions.BackwardsClipAction;
import com.buddyram.rframe.ftc.intothedeep.arm.RobotArm;
import com.buddyram.rframe.ftc.intothedeep.actions.RobotActions;
import com.buddyram.rframe.RobotException;
import com.qualcomm.robotcore.hardware.DigitalChannel;

import java.util.ArrayList;

public class ShortageBot implements Navigatable<HolonomicDriveTrain> {
    public static final double TARGET_POSITION_THRESHOLD = 1;
    public static final double TARGET_ROTATION_THRESHOLD = 1;
    public static final double SLOW_DISTANCE_THRESHOLD = 7;
    private Logger logger;
    private DigitalChannel frontBumper;

    public DigitalChannel getBackBumper() {
        return backBumper;
    }

    private DigitalChannel backBumper;


    public DigitalChannel getFrontBumper() {
        return frontBumper;
    }

    private HolonomicDriveTrain drive;

    public HolonomicDriveTrain getDrive() {
        return drive;
    }


    public Odometry<Pose3D> getOdometry() {
        return odometry;
    }

    private Odometry<Pose3D> odometry;
    private RobotArm arm;

    public static void main(String[] args) {
        System.out.println("Starting autonomous simulator v1.1.1 ...");
        PrintLogger logger = new PrintLogger(System.out);
        VirtualHolonomicDriveTrain drive = new VirtualHolonomicDriveTrain(24, 10 / 27.1);
        VirtualOdometrySensor odometry = new VirtualOdometrySensor(
                new Pose3D( // pose
                    new Vector3D(96, 8.5, 0), // position
                    new Vector3D(0, 0, 180), // rotation
                    new Vector3D(0, 0, 0), // position velocity
                    new Vector3D(0, 0, 0)
                ),
                drive
        );
        //AutonomousDrive main = new AutonomousDrive(odometry, logger, drive);
//        System.out.println("Initializing...");
//        main.init();
//        System.out.println("Initialized! Starting main program...");
//        main.run();
    }

    public ShortageBot() {
        this(null, null, null, null, null, null);
    }

    public ShortageBot(Logger logger, HolonomicDriveTrain drive, Odometry<Pose3D> odometry, RobotArm arm, DigitalChannel frontBumper, DigitalChannel backBumper) {
        this.odometry = odometry;
        this.logger = logger;
        this.drive = drive;
        this.arm = arm;
        this.frontBumper = frontBumper;
        this.backBumper = backBumper;
    }

    public void runAutonomous() throws RobotException, InterruptedException {
        ArrayList<RobotAction<ShortageBot>> actions = new ArrayList<>();

        // ACTIONS START HERE

        actions.add(BotUtils.driveTowardsUntil(72, 15, p -> p.x <= 80));
        actions.add(RobotActions.STOP);
        actions.add(new BackwardsClipAction());
        actions.add(BotUtils.driveTowardsUntil(96, 34, p -> p.x >= 96));
//        actions.add(BotUtils.driveTo(116, 30, p -> p.x >= 100)); // account for drift
        actions.add(RobotActions.REST);
        actions.add(BotUtils.driveTo(new Vector3D(126, 31, 0)));
        actions.add(RobotActions.PICKUP_SHORT);
        actions.add(BotUtils.wait(300));
        actions.add(RobotActions.CLOSE_CLAW);
        actions.add(BotUtils.wait(300));
        actions.add(BotUtils.rotateTo(180));
        actions.add(BotUtils.driveTo(new Vector3D(135, 31, 0)));

//        actions.add(BotUtils.driveTo(126, 30, p -> p.x >= 126, 0.3));
//        actions.add(BotUtils.rotateTo(0)); // Adjust angle
//        actions.add(BotUtils.rotateTo(180)); -<
//        actions.add(BotUtils.driveTowardsUntil(106, 34, p -> p.x >= 106, 0.5));
////        actions.add(RobotActions.PICKUP_SHORT);
//        // Begin Sample 1
//        actions.add(BotUtils.driveTowardsUntil(106, 44, p -> p.y >= 44, 1));
//        actions.add(BotUtils.driveTowardsUntil(106, 55, p -> p.y >= 55, 0.4)); // SLOW 0.5
//        actions.add(BotUtils.driveTowardsUntil(116, 55, p -> p.x >= 116, 0.4)); // Drive above SLOW 0.5
//        actions.add(BotUtils.driveTowardsUntil(116, 35, p -> p.y <= 35));  // Push
//        actions.add(BotUtils.driveTowardsUntil(116, 30, p -> p.y <= 30, 0.4));  // Push
//        // Begin Sample 2
//        actions.add(BotUtils.driveTowardsUntil(116, 44, p -> p.y >= 44));
//        actions.add(BotUtils.driveTowardsUntil(116, 55, p -> p.y >= 55, 0.4));
////        actions.add(RobotActions.STOP);
//        actions.add(BotUtils.rotateTo(180)); // Adjust angle -<
//        actions.add(BotUtils.driveTo(123, 55, p -> p.x >= 123)); // Drive above
//        actions.add(BotUtils.driveTo(122, 23, p -> p.y <= 23));  // Push
//        // Begin Sample 3
//        actions.add(BotUtils.driveTo(124, 55, p -> p.y >= 55));
//        actions.add(RobotActions.STOP);
//        actions.add(BotUtils.rotateTo(180)); // Adjust angle
//        actions.add(BotUtils.driveTo(132, 55, p -> p.x >= 132)); // Drive above
//        actions.add(BotUtils.driveTo(128, 23, p -> p.y <= 23));  // Push
        // Start Specimen hanging
//        actions.add(BotUtils.driveTo(120, 36, p -> p.x >= 120)); // Position for pickup
        actions.add(RobotActions.STOP);

        // ACTIONS END HERE

        while (this.isActive() && !actions.isEmpty()) {
            if (actions.get(0).run(this)) {
                actions.remove(0);
                System.out.println("next!! " + actions.size());
            }
        }
    }

    public boolean isActive() {
        return true;
    }

    public void init(Logger logger, HolonomicDriveTrain drive, Odometry<Pose3D> odometry, RobotArm arm, DigitalChannel frontBumper, DigitalChannel backBumper) {
        this.odometry = odometry;
        this.logger = logger;
        this.drive = drive;
        this.arm = arm;
        this.frontBumper = frontBumper;
        this.backBumper = backBumper;
    }

    public HolonomicDriveInstruction calculateRelativeDriveInstruction(Vector3D relativeTarget, double speed) {
        return this.calculateDriveInstruction(relativeTarget.add(this.odometry.get().position), speed);
    }

    public HolonomicDriveInstruction calculateDriveInstruction(Vector3D target, double speed) {
        double rotationInstruction = 0, driveSpeedInstruction = 0, driveAngleInstruction = 0;
        Pose3D pos = this.odometry.get();
        driveSpeedInstruction = speed;
        driveAngleInstruction = pos.position.calculateRotation(target).z;

        return new HolonomicDriveInstruction(rotationInstruction, driveSpeedInstruction, driveAngleInstruction);
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    public RobotArm getArm() {
        return arm;
    }
}

