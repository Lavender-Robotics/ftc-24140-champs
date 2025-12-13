package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

@Autonomous(name = "PornoSeksAM", group = "Autonomous")
@Configurable
public class yamanSeks extends OpMode {

    private TelemetryManager panelsTelemetry;
    private Follower follower;

    private ElapsedTime timer;
    private int pathState;

    private PathChain path1, path2, path3, path4;

    @Override
    public void init() {
        panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose(72, 8, Math.toRadians(90)));

        timer = new ElapsedTime();
        pathState = 0;

        buildPaths();

        panelsTelemetry.debug("Status", "Ready");
        panelsTelemetry.update(telemetry);
    }

    @Override
    public void start() {
        timer.reset();
        pathState = 1;
    }

    @Override
    public void loop() {
        follower.update();
        autonomousStateMachine();

        panelsTelemetry.debug("State", pathState);
        panelsTelemetry.debug("X", follower.getPose().getX());
        panelsTelemetry.debug("Y", follower.getPose().getY());
        panelsTelemetry.debug("Heading (deg)", Math.toDegrees(follower.getPose().getHeading()));
        panelsTelemetry.update(telemetry);
    }

    private void buildPaths() {
        path1 = follower.pathBuilder()
                .addPath(new BezierLine(new Pose(72, 10), new Pose(72, 40)))
                .setLinearHeadingInterpolation(Math.toRadians(90), Math.toRadians(180))
                .build();

        path2 = follower.pathBuilder()
                .addPath(new BezierLine(new Pose(72, 40), new Pose(72, 50)))
                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(90))
                .build();

        path3 = follower.pathBuilder()
                .addPath(new BezierLine(new Pose(72, 50), new Pose(62, 50)))
                .setConstantHeadingInterpolation(Math.toRadians(90))
                .build();

        path4 = follower.pathBuilder()
                .addPath(new BezierLine(new Pose(62, 50), new Pose(82, 50)))
                .setConstantHeadingInterpolation(Math.toRadians(90))
                .build();
    }

    private void autonomousStateMachine() {
        switch (pathState) {
            case 1:
                follower.followPath(path1);
                pathState = 2;
                break;

            case 2:
                if (!follower.isBusy()) {
                    follower.followPath(path2);
                    pathState = 3;
                }
                break;

            case 3:
                if (!follower.isBusy()) {
                    follower.followPath(path3);
                    pathState = 4;
                }
                break;

            case 4:
                if (!follower.isBusy()) {
                    follower.followPath(path4);
                    pathState = 5;
                }
                break;

            case 5:
                if (!follower.isBusy()) {
                    pathState = 6;
                }
                break;

            case 6:
                break;

            default:
                break;
        }
    }
}