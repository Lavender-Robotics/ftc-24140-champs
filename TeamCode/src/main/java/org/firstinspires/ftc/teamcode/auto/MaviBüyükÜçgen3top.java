package org.firstinspires.ftc.teamcode.auto;

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
import org.firstinspires.ftc.teamcode.subsystems.ShooterSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TransportSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FeederSubsystem;

@Autonomous(name = "MaviBüyükÜçgen", group = "Autonomous")
@Configurable
public class MaviBüyükÜçgen3top extends OpMode {
    private TelemetryManager panelsTelemetry;
    private Follower follower;

    private ShooterSubsystem shooter;
    private TransportSubsystem transport;
    private FeederSubsystem feeder;

    private int pathState;
    private Paths paths;
    private ElapsedTime timer;

    // Shot timing constants
    private static final double SHOOTER_SPINUP_TIME = 1.5;  // Shooter spin-up süresi
    private static final double SHOT_DURATION = 0.3;        // Her shot için süre
    private static final int TOTAL_SHOTS = 3;               // Toplam atış sayısı

    private int shotCount;
    private double stateStartTime;

    @Override
    public void init() {
        panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();
        timer = new ElapsedTime();

        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose(72, 8, Math.toRadians(90)));

        shooter = new ShooterSubsystem(hardwareMap);
        transport = new TransportSubsystem(hardwareMap);
        feeder = new FeederSubsystem(hardwareMap);

        paths = new Paths(follower);

        pathState = 0;
        shotCount = 0;

        panelsTelemetry.debug("Status", "Initialized");
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
        pathState = autonomousPathUpdate();

        panelsTelemetry.debug("Path State", pathState);
        panelsTelemetry.debug("Shot Count", shotCount);
        panelsTelemetry.debug("X", follower.getPose().getX());
        panelsTelemetry.debug("Y", follower.getPose().getY());
        panelsTelemetry.debug("Heading", Math.toDegrees(follower.getPose().getHeading()));
        panelsTelemetry.debug("Shooter Velocity", shooter.getVelocity());
        panelsTelemetry.update(telemetry);
    }

    public static class Paths {
        public PathChain Path1;
        public PathChain Path2;

        public Paths(Follower follower) {
            Path1 = follower.pathBuilder()
                    .addPath(new BezierLine(
                            new Pose(23.000, 124.000),
                            new Pose(72.000, 124.000)))
                    .setLinearHeadingInterpolation(Math.toRadians(144), Math.toRadians(180))
                    .build();

            Path2 = follower.pathBuilder()
                    .addPath(new BezierLine(
                            new Pose(72.000, 124.000),
                            new Pose(72.000, 134.000)))
                    .setConstantHeadingInterpolation(Math.toRadians(180))
                    .build();
        }
    }

    public int autonomousPathUpdate() {
        switch (pathState) {
            case 1:
                // State 1: Path1'i takip et
                follower.followPath(paths.Path1);
                pathState = 2;
                break;

            case 2:
                // State 2: Path1 tamamlanana kadar bekle
                if (!follower.isBusy()) {
                    pathState = 3;
                }
                break;

            case 3:
                // State 3: Path2'yi takip et
                follower.followPath(paths.Path2);
                pathState = 4;
                break;

            case 4:
                // State 4: Path2 tamamlanana kadar bekle
                if (!follower.isBusy()) {
                    stateStartTime = timer.seconds();
                    shooter.setEnabled(true);
                    shooter.forward(1200.0);  // Shooter'ı çalıştır
                    pathState = 5;
                }
                break;

            case 5:
                // State 5: Shooter spin-up - motorların hızlanmasını bekle
                if (timer.seconds() - stateStartTime > SHOOTER_SPINUP_TIME) {
                    stateStartTime = timer.seconds();
                    shotCount = 0;
                    pathState = 6;
                }
                break;

            case 6:
                // State 6: Transport ile topu besle
                transport.forward(1.0);
                pathState = 7;
                break;

            case 7:
                // State 7: Feeder ile topu vur
                feeder.setEnabled(true);
                feeder.extendServo();
                stateStartTime = timer.seconds();
                pathState = 8;
                break;

            case 8:
                // State 8: Shot tamamlanana kadar bekle
                if (timer.seconds() - stateStartTime > SHOT_DURATION) {
                    feeder.retractServo();
                    transport.stop();
                    shotCount++;

                    if (shotCount < TOTAL_SHOTS) {
                        // Bir sonraki shot için hazırlan
                        stateStartTime = timer.seconds();
                        pathState = 9;
                    } else {
                        // Tüm shotlar tamamlandı
                        pathState = 10;
                    }
                }
                break;

            case 9:
                // State 9: Shotlar arası kısa bekleme
                if (timer.seconds() - stateStartTime > 0.2) {
                    feeder.setEnabled(false);
                    pathState = 6;  // Bir sonraki shot için döngüye geri dön
                }
                break;

            case 10:
                // State 10: Shotlar tamamlandı - temizlik
                shooter.stop();
                shooter.setEnabled(false);
                transport.stop();
                feeder.setEnabled(false);
                feeder.retractServo();
                pathState = 11;  // Son state - otonom tamamlandı
                break;

            case 11:
                // State 11: Otonom tamamlandı - hiçbir şey yapma
                break;
        }

        return pathState;
    }

    @Override
    public void stop() {
        // OpMode durdurulduğunda tüm subsystemleri durdur
        shooter.stop();
        shooter.setEnabled(false);
        transport.stop();
        feeder.setEnabled(false);
        feeder.retractServo();
    }
}