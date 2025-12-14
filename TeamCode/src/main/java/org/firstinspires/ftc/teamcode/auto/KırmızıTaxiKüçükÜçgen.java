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

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

@Autonomous(name = "KırmızıTaxiKüçükÜçgen", group = "Autonomous")
@Configurable
public class KırmızıTaxiKüçükÜçgen extends OpMode {
    private TelemetryManager panelsTelemetry;
    private Follower follower;
    private int pathState;
    private Paths paths;

    @Override
    public void init() {
        panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();
        follower = Constants.createFollower(hardwareMap);

        // Red Taxi başlangıç pozisyonu
        follower.setStartingPose(new Pose(87.000, 8.000, Math.toRadians(90)));

        paths = new Paths(follower);
        pathState = 0;

        panelsTelemetry.debug("Status", "Initialized - Red Taxi");
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
        panelsTelemetry.debug("X Position", String.format("%.1f", follower.getPose().getX()));
        panelsTelemetry.debug("Y Position", String.format("%.1f", follower.getPose().getY()));
        panelsTelemetry.debug("Heading", String.format("%.1f°", Math.toDegrees(follower.getPose().getHeading())));
        panelsTelemetry.debug("Is Busy", follower.isBusy());
        panelsTelemetry.update(telemetry);
    }

    public static class Paths {
        public PathChain TaxiPath;

        public Paths(Follower follower) {
            // Red Taxi path: (87, 8) → (87, 36)
            // 28 inç düz ileri hareket
            TaxiPath = follower.pathBuilder()
                    .addPath(new BezierLine(
                            new Pose(87.000, 8.000),
                            new Pose(87.000, 36.000)))
                    .setTangentHeadingInterpolation()
                    .build();
        }
    }

    public int autonomousPathUpdate() {
        switch (pathState) {
            case 1:
                // State 1: Taxi path'ini başlat
                follower.followPath(paths.TaxiPath);
                pathState = 2;
                break;

            case 2:
                // State 2: Path tamamlanana kadar bekle
                if (!follower.isBusy()) {
                    pathState = 3;
                }
                break;

            case 3:
                // State 3: Taxi tamamlandı - Idle state
                // Robot hedef pozisyonda durdu
                break;
        }

        return pathState;
    }

    // State isimlerini telemetry için okunabilir yap
    private String getStateName(int state) {
        switch (state) {
            case 0: return "0: Init";
            case 1: return "1: Start Taxi";
            case 2: return "2: Moving";
            case 3: return "3: Complete";
            default: return String.valueOf(state);
        }
    }

    @Override
    public void stop() {
        // OpMode durdurulduğunda follower'ı temizle
        follower.breakFollowing();
    }
}