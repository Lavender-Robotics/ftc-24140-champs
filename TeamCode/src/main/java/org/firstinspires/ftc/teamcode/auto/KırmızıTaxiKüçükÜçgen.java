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
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;

@Autonomous(name = "Pedro Pathing Autonomous", group = "Autonomous")
@Configurable
public class KırmızıTaxiKüçükÜçgen extends OpMode {
    private TelemetryManager panelsTelemetry;
    public Follower follower;
    private ShooterSubsystem shooter;
    private IntakeSubsystem intake;

    private int pathState;
    private Paths paths;
    private ElapsedTime shootTimer;

    // Shooting parametreleri (3 top için 12 saniye)
    private static final double SHOOT_DURATION = 12.0;  // 12 saniye
    private static final double SHOOTER_VELOCITY = 1800.0;  // Shooter hızı
    private static final double INTAKE_POWER = 1.0;

    @Override
    public void init() {
        panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();
        follower = Constants.createFollower(hardwareMap);

        // Subsystem'leri başlat
        shooter = new ShooterSubsystem(hardwareMap);
        intake = new IntakeSubsystem(hardwareMap);

        // Timer'ı oluştur
        shootTimer = new ElapsedTime();

        // Başlangıç pozisyonu (Kırmızı Küçük Üçgen)
        follower.setStartingPose(new Pose(72, 8, Math.toRadians(90)));

        paths = new Paths(follower);
        pathState = 0;

        panelsTelemetry.debug("Status", "Initialized");
        panelsTelemetry.update(telemetry);
    }

    @Override
    public void start() {
        // Otonom başladığında ilk state'e geç
        pathState = 1;
    }

    @Override
    public void loop() {
        follower.update();
        pathState = autonomousPathUpdate();

        // Telemetry güncellemeleri
        panelsTelemetry.debug("Path State", getStateName(pathState));
        panelsTelemetry.debug("X", String.format("%.1f", follower.getPose().getX()));
        panelsTelemetry.debug("Y", String.format("%.1f", follower.getPose().getY()));
        panelsTelemetry.debug("Heading", String.format("%.1f°", Math.toDegrees(follower.getPose().getHeading())));
        panelsTelemetry.debug("Is Busy", follower.isBusy());

        // Shooting state'inde timer bilgisi
        if (pathState == 3) {
            panelsTelemetry.debug("Shoot Timer", String.format("%.1f / %.1f sec",
                    shootTimer.seconds(), SHOOT_DURATION));
            panelsTelemetry.debug("Shooter Velocity", shooter.getVelocity());
            panelsTelemetry.debug("Balls Shot", "3 balls");
        }

        panelsTelemetry.update(telemetry);
    }

    public static class Paths {
        public PathChain Path1;

        public Paths(Follower follower) {
            // Kırmızı Küçük Üçgen path
            Path1 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(85.000, 10.000), new Pose(88.000, 14.000))
                    )
                    .setLinearHeadingInterpolation(Math.toRadians(90), Math.toRadians(69))
                    .build();
        }
    }

    public int autonomousPathUpdate() {
        switch (pathState) {
            case 1:
                // State 1: Path'i başlat
                follower.followPath(paths.Path1);
                pathState = 2;
                break;

            case 2:
                // State 2: Path tamamlanana kadar bekle
                if (!follower.isBusy()) {
                    // Path bitti, shooting için timer'ı başlat
                    shootTimer.reset();
                    pathState = 3;
                }
                break;

            case 3:
                // State 3: 12 saniye shooter + intake çalıştır (3 top atacak)
                shooter.forward(SHOOTER_VELOCITY);
                intake.forward(INTAKE_POWER);

                // 12 saniye doldu mu kontrol et
                if (shootTimer.seconds() >= SHOOT_DURATION) {
                    // Shooter ve intake'i durdur
                    shooter.stop();
                    intake.stop();
                    pathState = 4;
                }
                break;

            case 4:
                // State 4: Otonom tamamlandı - Idle
                // 3 top atıldı, robot durdu
                break;
        }

        return pathState;
    }

    // State isimlerini telemetry için okunabilir yap
    private String getStateName(int state) {
        switch (state) {
            case 0: return "0: Init";
            case 1: return "1: Start Path";
            case 2: return "2: Following Path";
            case 3: return "3: Shooting (3 balls - 12s)";
            case 4: return "4: Complete";
            default: return String.valueOf(state);
        }
    }

    @Override
    public void stop() {
        // OpMode durdurulduğunda her şeyi temizle
        shooter.stop();
        intake.stop();
        follower.breakFollowing();
    }
}