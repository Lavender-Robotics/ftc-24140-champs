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

// Kendi Subsystem'lerinizin import'ları
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.ShooterSubsystem;


@Autonomous(name = "KırmızıBüyükÜçgen3top", group = "Autonomous")
@Configurable // Panels
public class KırmızıBüyükÜçgen3top extends OpMode {

    private TelemetryManager panelsTelemetry;
    public Follower follower;
    private int pathState;
    private Paths paths;
    private ElapsedTime timer;

    // Subsystem'ler
    private ShooterSubsystem shooterSubsystem;
    private IntakeSubsystem intakeSubsystem;

    // Subsystem Güç/Hız Ayarları
    private static final double SHOOTER_VELOCITY = 1500.0; // Atış Hızı (Encoder biriminde)
    private static final double INTAKE_POWER = 1.0; // Toplama Gücü (0.0 - 1.0 arası)

    @Override
    public void init() {
        panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();
        timer = new ElapsedTime();

        // 1. Follower ve Başlangıç Pozisyonu
        follower = Constants.createFollower(hardwareMap);

        // Başlangıç pozisyonu Path2'ye yakın ayarlandı
        follower.setStartingPose(new Pose(121, 125, Math.toRadians(37)));

        // 2. Paths (Yollar)
        paths = new Paths(follower);
        pathState = 0;

        // 3. Subsystem'lerin Başlatılması
        shooterSubsystem = new ShooterSubsystem(hardwareMap);
        intakeSubsystem = new IntakeSubsystem(hardwareMap);

        panelsTelemetry.debug("Status", "Initialized");
        panelsTelemetry.update(telemetry);
    }

    @Override
    public void start() {
        pathState = 0;
    }

    @Override
    public void loop() {
        follower.update();
        pathState = autonomousPathUpdate();

        // Log values
        panelsTelemetry.debug("Path State", pathState);
        panelsTelemetry.debug("X", follower.getPose().getX());
        panelsTelemetry.debug("Y", follower.getPose().getY());
        panelsTelemetry.debug("Heading", follower.getPose().getHeading());
        panelsTelemetry.debug("Is Busy", follower.isBusy());
        if (pathState == 2) {
            panelsTelemetry.debug("Timer", timer.seconds());
            panelsTelemetry.debug("Shooter Vel", shooterSubsystem.getVelocity());
        }
        panelsTelemetry.update(telemetry);
    }

    // --- PATHS CLASS (Yollar Tanımlanıyor) ---
    public static class Paths {

        public PathChain Path2;

        public Paths(Follower follower) {
            Path2 = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(121.000, 125.000), new Pose(90.000, 90.000))
                    )
                    .setLinearHeadingInterpolation(Math.toRadians(37), Math.toRadians(48))
                    .build();
        }
    }

    // --- OTONOM DURUM MAKİNESİ (SWITCH CASE) ---
    public int autonomousPathUpdate() {
        switch (pathState) {
            case 0:
                // State 0: Yola Başla (Path2)
                follower.followPath(paths.Path2);
                return 1;

            case 1:
                // State 1: Yolun bitmesini bekle
                if (!follower.isBusy()) {
                    // Yol bitti, alt sistemleri çalıştırma durumuna geç
                    timer.reset();

                    // Atıcıyı ve Toplayıcıyı Başlat (Kullanılan metotlar altsistem dosyalarından alınmıştır)
                    shooterSubsystem.forward(SHOOTER_VELOCITY);
                    intakeSubsystem.forward(INTAKE_POWER);

                    return 2;
                }
                break;

            case 2:
                // State 2: 12 saniye boyunca Shot ve Intake yap
                if (timer.seconds() >= 12.0) {
                    // 12 saniye doldu, alt sistemleri durdur
                    shooterSubsystem.stop();
                    intakeSubsystem.stop();

                    return 3; // Son duruma geç
                }
                break;

            case 3:
                // State 3: Otonom Bitti
                panelsTelemetry.debug("Status", "Autonomous Finished!");
                break;
        }

        return pathState;
    }
}