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

@Autonomous(name = "MaviTaxiKüçükÜçgen", group = "Autonomous")
@Configurable
public class MaviTaxiKüçükÜçgen extends OpMode {
    private TelemetryManager panelsTelemetry;
    private Follower follower;
    private int pathState;
    private Paths paths;

    @Override
    public void init() {
        panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();
        follower = Constants.createFollower(hardwareMap);

        // Starting pose - robot'un başlangıç pozisyonu
        follower.setStartingPose(new Pose(56.000, 10.000, Math.toRadians(90)));

        paths = new Paths(follower);
        pathState = 0;

        panelsTelemetry.debug("Status", "Initialized - Ready for Taxi");
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
        panelsTelemetry.debug("Path State", pathState);
        panelsTelemetry.debug("X Position", follower.getPose().getX());
        panelsTelemetry.debug("Y Position", follower.getPose().getY());
        panelsTelemetry.debug("Heading (deg)", Math.toDegrees(follower.getPose().getHeading()));
        panelsTelemetry.debug("Is Busy", follower.isBusy());
        panelsTelemetry.update(telemetry);
    }

    public static class Paths {
        public PathChain TaxiPath;

        public Paths(Follower follower) {
            // Taxi path: (56, 10) → (56, 36) - Düz ileri hareket
            TaxiPath = follower.pathBuilder()
                    .addPath(new BezierLine(
                            new Pose(56.000, 10.000),
                            new Pose(56.000, 36.000)))
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
                // State 3: Taxi tamamlandı - idle state
                // Robot artık hedef pozisyonda ve hiçbir şey yapılmıyor
                break;
        }

        return pathState;
    }

    @Override
    public void stop() {
        // OpMode durdurulduğunda follower'ı temizle
        follower.breakFollowing();
    }
}