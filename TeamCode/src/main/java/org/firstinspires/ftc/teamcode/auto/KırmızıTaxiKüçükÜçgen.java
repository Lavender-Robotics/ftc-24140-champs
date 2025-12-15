package org.firstinspires.ftc.teamcode.auto; // Yeni paket ismini koruduk

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

// Lütfen bu importları kendi projenizdeki doğru konumlara göre ayarlayın
import org.firstinspires.ftc.teamcode.subsystems.ShooterSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TransportSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FeederSubsystem;


@Autonomous(name = "Kırmızı küçük üçgen top", group = "Autonomous")
@Configurable
public class KırmızıTaxiKüçükÜçgen extends OpMode {

    private TelemetryManager panelsTelemetry;
    private Follower follower;

    // Subsystems
    private ShooterSubsystem shooter;
    private TransportSubsystem transport;
    private FeederSubsystem feeder;

    private int pathState;
    private Paths paths;
    private ElapsedTime timer;

    // Atış Zamanlama Sabitleri
    private static final double SHOOTER_VELOCITY = 1800.0;     // Shooter hız hedefi
    private static final double SHOOTER_SPINUP_TIME = 1.5;   // Shooter motorlarının hızlanma süresi (sn)
    private static final double SHOT_DURATION = 0.3;         // Her shot için Feeder'ın bekleme süresi (sn)
    private static final double SHOT_DELAY = 0.2;            // Shotlar arası bekleme süresi (sn)
    private static final int TOTAL_SHOTS = 3;                // Toplam atış sayısı
    private static final double TRANSPORT_POWER = 1.0;       // Transport (Taşıyıcı) gücü

    private int shotCount;
    private double stateStartTime; // Durum değişim zamanını kaydetmek için

    @Override
    public void init() {
        panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();
        timer = new ElapsedTime();

        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose(72, 8, Math.toRadians(90)));

        // Subsystem'leri Başlat
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
        pathState = 0; // İlk yola başlamak için 0 olarak ayarlandı
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

    // --- YOLLAR ---
    public static class Paths {
        // İsim çakışmasını düzeltmek için Path2 ve Path3 kullanıldı.
        public PathChain Path2;
        public PathChain Path3;

        public Paths(Follower follower) {
            // Path 2: İlk yol (Sizin önceki kodunuzdan)
            Path2 = follower.pathBuilder()
                    .addPath(new BezierLine(
                            new Pose(82.000, 10.000),
                            new Pose(84.969, 13.161)))
                    .setLinearHeadingInterpolation(Math.toRadians(90), Math.toRadians(68))
                    .build();

            // Path 3: İkinci yol (Sizin önceki kodunuzdan)
            Path3 = follower.pathBuilder()
                    .addPath(new BezierLine(
                            new Pose(84.969, 13.161),
                            new Pose(113.807, 14.903)))
                    .setLinearHeadingInterpolation(Math.toRadians(62), Math.toRadians(90))
                    .build();
        }
    }

    // --- DURUM MAKİNESİ (SWITCH CASE) ---
    public int autonomousPathUpdate() {
        switch (pathState) {
            case 0:
                // State 0: Path2'yi takip et
                follower.followPath(paths.Path2);
                return 1;

            case 1:
                // State 1: Path2 tamamlanana kadar bekle
                if (!follower.isBusy()) {
                    follower.followPath(paths.Path3); // Path2 bitti, Path3'e başla
                    return 2;
                }
                break;

            case 2:
                // State 2: Path3 tamamlanana kadar bekle
                if (!follower.isBusy()) {
                    stateStartTime = timer.seconds(); // Zamanı kaydet
                    shooter.setEnabled(true);
                    shooter.forward(SHOOTER_VELOCITY); // Shooter'ı çalıştır
                    return 3;
                }
                break;

            case 3:
                // State 3: Shooter spin-up (hızlanmasını bekle)
                if (timer.seconds() - stateStartTime > SHOOTER_SPINUP_TIME) {
                    stateStartTime = timer.seconds(); // Atış döngüsü için zamanı sıfırla
                    shotCount = 0;
                    return 4;
                }
                break;

            case 4:
                // State 4: Transport ile topu besle (Atış Döngüsü Başlangıcı)
                transport.forward(TRANSPORT_POWER);
                return 5;

            case 5:
                // State 5: Feeder ile topu vur
                feeder.setEnabled(true);
                feeder.extendServo();
                stateStartTime = timer.seconds(); // Shot süresi için zamanı başlat
                return 6;

            case 6:
                // State 6: Shot tamamlanana kadar bekle
                if (timer.seconds() - stateStartTime > SHOT_DURATION) {
                    feeder.retractServo();
                    transport.stop();
                    shotCount++;

                    if (shotCount < TOTAL_SHOTS) {
                        return 7; // Bir sonraki shot için bekleme durumuna geç
                    } else {
                        return 8; // Tüm shotlar tamamlandı, temizlik durumuna geç
                    }
                }
                break;

            case 7:
                // State 7: Shotlar arası kısa bekleme (Feeder geri çekilme)
                if (timer.seconds() - stateStartTime > SHOT_DURATION + SHOT_DELAY) {
                    feeder.setEnabled(false);
                    return 4; // Bir sonraki shot için döngüye geri dön (State 4)
                }
                break;

            case 8:
                // State 8: Shotlar tamamlandı - temizlik
                shooter.stop();
                shooter.setEnabled(false);
                transport.stop();
                feeder.setEnabled(false);
                feeder.retractServo();
                return 9; // Son state - otonom tamamlandı

            case 9:
                // State 9: Otonom tamamlandı - hiçbir şey yapma
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