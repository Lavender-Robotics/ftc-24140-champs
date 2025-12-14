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
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FeederSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.ShooterSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TransportSubsystem;

@Autonomous(name = "BüyükÜçgenMavi", group = "Autonomous")
@Configurable
public class BüyükÜçgen extends OpMode {

    private TelemetryManager panelsTelemetry;
    public Follower follower;
    private int pathState;
    private Paths paths;

    // Subsystems
    private IntakeSubsystem intake;
    private FeederSubsystem feeder;
    private ShooterSubsystem shooter;
    private TransportSubsystem transport;

    // Timer for shot sequences
    private ElapsedTime shotTimer;
    private boolean shotInProgress = false;

    // Shot sequence parameters
    private static final double SHOOTER_VELOCITY = 1200.0;
    private static final double SHOT_DURATION = 1.0; // seconds for complete shot cycle

    @Override
    public void init() {
        panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();

        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose(72, 8, Math.toRadians(90)));

        // Initialize subsystems
        intake = new IntakeSubsystem(hardwareMap);
        feeder = new FeederSubsystem(hardwareMap);
        shooter = new ShooterSubsystem(hardwareMap);
        transport = new TransportSubsystem(hardwareMap);

        shotTimer = new ElapsedTime();

        paths = new Paths(follower);
        pathState = 0;

        panelsTelemetry.debug("Status", "Initialized");
        panelsTelemetry.update(telemetry);
    }

    @Override
    public void start() {
        follower.followPath(paths.Path1);
        pathState = 1;
        shotTimer.reset();
    }

    @Override
    public void loop() {
        follower.update();
        pathState = autonomousPathUpdate();

        panelsTelemetry.debug("Path State", pathState);
        panelsTelemetry.debug("X", follower.getPose().getX());
        panelsTelemetry.debug("Y", follower.getPose().getY());
        panelsTelemetry.debug("Heading", Math.toDegrees(follower.getPose().getHeading()));
        panelsTelemetry.debug("Shot In Progress", shotInProgress);
        panelsTelemetry.debug("Shooter Velocity", shooter.getVelocity());
        panelsTelemetry.update(telemetry);
    }

    public static class Paths {
        public PathChain Path1;
        public PathChain Path2;
        public PathChain Path3;
        public PathChain Path4;
        public PathChain Path5;
        public PathChain Path6;
        public PathChain Path7;

        public Paths(Follower follower) {
            Path1 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(22.000, 123.000), new Pose(71.000, 133.000))
                    )
                    .setLinearHeadingInterpolation(Math.toRadians(144), Math.toRadians(180))
                    .build();

            Path2 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(71.000, 133.000), new Pose(46.000, 83.000))
                    )
                    .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
                    .build();

            Path3 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(46.000, 83.000), new Pose(22.000, 83.000))
                    )
                    .setConstantHeadingInterpolation(Math.toRadians(180))
                    .build();

            Path4 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(22.000, 83.000), new Pose(71.000, 133.000))
                    )
                    .setConstantHeadingInterpolation(Math.toRadians(180))
                    .build();

            Path5 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(71.000, 133.000), new Pose(41.000, 59.000))
                    )
                    .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
                    .build();

            Path6 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(41.000, 59.000), new Pose(22.000, 59.000))
                    )
                    .setTangentHeadingInterpolation()
                    .build();

            Path7 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(22.000, 59.000), new Pose(71.000, 133.000))
                    )
                    .setConstantHeadingInterpolation(Math.toRadians(180))
                    .build();
        }
    }

    /**
     * Performs a complete shot sequence:
     * 1. Spin up shooter
     * 2. Wait for velocity
     * 3. Extend feeder to push ball
     * 4. Retract feeder
     * 5. Stop shooter
     */
    private void executeShot() {
        if (!shotInProgress) {
            // Start shot sequence
            shooter.forward(SHOOTER_VELOCITY);
            shotTimer.reset();
            shotInProgress = true;
        } else {
            double elapsed = shotTimer.seconds();

            if (elapsed < 0.3) {
                // Spin up phase - shooter only
                shooter.forward(SHOOTER_VELOCITY);
            } else if (elapsed < 0.6) {
                // Push ball - extend feeder
                shooter.forward(SHOOTER_VELOCITY);
                feeder.extendServo();
            } else if (elapsed < SHOT_DURATION) {
                // Retract feeder
                shooter.forward(SHOOTER_VELOCITY);
                feeder.retractServo();
            } else {
                // Shot complete - stop everything
                shooter.stop();
                feeder.retractServo();
                shotInProgress = false;
            }
        }
    }

    /**
     * Manages intake operation during path
     */
    private void runIntake() {
        intake.forward(1.0);
        transport.forward(1.0);
    }

    /**
     * Stops all intake/transport
     */
    private void stopIntake() {
        intake.stop();
        transport.stop();
    }

    public int autonomousPathUpdate() {
        switch (pathState) {
            case 1: // Path1 - move to first shot position
                if (!follower.isBusy()) {
                    // Start shot sequence at end of Path1
                    shotTimer.reset();
                    shotInProgress = false;
                    pathState = 11; // Transition to shot state
                }
                break;

            case 11: // Execute first shot
                executeShot();
                if (!shotInProgress) {
                    // Shot complete, move to next path
                    follower.followPath(paths.Path2);
                    pathState = 2;
                }
                break;

            case 2: // Path2 - transition
                if (!follower.isBusy()) {
                    follower.followPath(paths.Path3);
                    pathState = 3;
                }
                break;

            case 3: // Path3 - intake during movement
                runIntake();
                if (!follower.isBusy()) {
                    stopIntake();
                    follower.followPath(paths.Path4);
                    pathState = 4;
                }
                break;

            case 4: // Path4 - return to shot position
                if (!follower.isBusy()) {
                    // Start second shot sequence
                    shotTimer.reset();
                    shotInProgress = false;
                    pathState = 41; // Transition to shot state
                }
                break;

            case 41: // Execute second shot
                executeShot();
                if (!shotInProgress) {
                    // Shot complete, move to next path
                    follower.followPath(paths.Path5);
                    pathState = 5;
                }
                break;

            case 5: // Path5 - transition
                if (!follower.isBusy()) {
                    follower.followPath(paths.Path6);
                    pathState = 6;
                }
                break;

            case 6: // Path6 - intake during movement
                runIntake();
                if (!follower.isBusy()) {
                    stopIntake();
                    follower.followPath(paths.Path7);
                    pathState = 7;
                }
                break;

            case 7: // Path7 - return to final shot position
                if (!follower.isBusy()) {
                    // Start third shot sequence
                    shotTimer.reset();
                    shotInProgress = false;
                    pathState = 71; // Transition to shot state
                }
                break;

            case 71: // Execute third shot
                executeShot();
                if (!shotInProgress) {
                    // All shots complete
                    pathState = 8;
                }
                break;

            case 8: // Complete
                // Autonomous complete - ensure everything is stopped
                stopIntake();
                shooter.stop();
                feeder.retractServo();
                break;
        }

        return pathState;
    }
}