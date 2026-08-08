package org.firstinspires.ftc.teamcode.pedroPathing; // make sure this aligns with class location

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.CoordinateSystem;
import com.pedropathing.geometry.Pose;
import com.pedropathing.util.Timer;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import  com.qualcomm.robotcore.eventloop.opmode.OpMode;

@Autonomous(name = "Example Auto", group = "Examples")
public class ExampleAuto extends OpMode {

    private Follower follower;
    private Timer pathTimer, actionTimer, opmodeTimer;

    private int pathState;
    private final Pose startPose = new Pose(19.531, 117.011, Math.toRadians(0)); // Start Pose of our robot.
    Paths paths;

    public void autonomousPathUpdate() throws InterruptedException {
        switch (pathState) {
            case 0:
                Thread.sleep(1000);
                setPathState(1);
                break;
            case 1:
                if (!follower.isBusy()) {
                    Thread.sleep(1000);
                    setPathState(2);
                }
                break;
            case 2:
                if (!follower.isBusy()) {
                    Thread.sleep(1000);
                    setPathState(3);
                    follower.setMaxPower(0.3);
                }
                break;
            case 3:
                if (!follower.isBusy()) {
                    Thread.sleep(1000);
                    setPathState(4);
                    follower.setMaxPower(1);
                }
                break;

            /* You could check for
            - Follower State: "if(!follower.isBusy()) {}"
            - Time: "if(pathTimer.getElapsedTimeSeconds() > 1) {}"
            - Robot Position: "if(follower.getPose().getX() > 36) {}"
            */
        }
    }

    /** These change the states of the paths and actions. It will also reset the timers of the individual switches **/
    public void setPathState(int pState) {
        pathState = pState;
        if (pState >= 1) {
            follower.followPath(paths.getPath(pState));
        }
        pathTimer.resetTimer();
    }

    /** This is the main loop of the OpMode, it will run repeatedly after clicking "Play". **/
    @Override
    public void loop() {

        // These loop the movements of the robot, these must be called continuously in order to work
        follower.update();
        try {
            autonomousPathUpdate();
        } catch (InterruptedException e) {


        }

        // Feedback to Driver Hub for debugging
        telemetry.addData("path state", pathState);
        telemetry.addData("x", follower.getPose().getX());
        telemetry.addData("y", follower.getPose().getY());
        telemetry.addData("heading", follower.getPose().getHeading());
        telemetry.update();
    }
    /** This method is called once at the init of the OpMode. **/
    @Override
    public void init() {
        pathTimer = new Timer();
        opmodeTimer = new Timer();
        opmodeTimer.resetTimer();


        follower = Constants.createFollower(hardwareMap);
        this.paths = new Paths(follower);
        follower.setStartingPose(startPose);

    }

    public Pose getRobotPoseFromCamera() {
//        camera.
        return new Pose().getAsCoordinateSystem(new CoordinateSystem() {
            @Override
            public Pose convertToPedro(Pose pose) {
                double angle = (pose.getHeading() + 90) % 360;
                angle = angle < 0 ? angle + 360 : angle;
                return new Pose(pose.getX(), pose.getY(), angle);
            }

            @Override
            public Pose convertFromPedro(Pose pose) {
                double angle = (pose.getHeading() - 90) % 360;
                angle = angle < 0 ? angle + 360 : angle;
                return new Pose(pose.getX(), pose.getY(), angle);
            }
        });
    }

    /** This method is called once at the start of the OpMode.
     * It runs all the setup actions, including building paths and starting the path system **/
    @Override
    public void start() {
        opmodeTimer.resetTimer();
        setPathState(0);
    }

}

