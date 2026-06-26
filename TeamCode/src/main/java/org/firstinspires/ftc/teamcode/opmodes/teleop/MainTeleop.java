package org.firstinspires.ftc.teamcode.opmodes.teleop;

import static org.firstinspires.ftc.teamcode.subsystems.DriveSubsystem.SLOW_MODE_FACTOR;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.subsystems.DriveSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.ShooterSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TransportSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FeederSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.VisionSubsystem;
import org.firstinspires.ftc.teamcode.util.Toggle;

@TeleOp(name = "MainTeleOp")
public class MainTeleop extends LinearOpMode {

    // Subsystem Init
    private DriveSubsystem drive;
    private IntakeSubsystem intake;
    private TransportSubsystem transport;
    private FeederSubsystem feeder;
    private ShooterSubsystem shooter;
    private VisionSubsystem vision;

    // Toggles
    private final Toggle feederToggle = new Toggle();
    private final Toggle shooterToggle = new Toggle();

    @Override
    public void runOpMode() throws InterruptedException {
        // Altsistemlerin Başlatılması
        drive     = new DriveSubsystem(hardwareMap, telemetry);
        intake    = new IntakeSubsystem(hardwareMap);
        transport = new TransportSubsystem(hardwareMap);
        feeder    = new FeederSubsystem(hardwareMap);
        shooter   = new ShooterSubsystem(hardwareMap);
        vision    = new VisionSubsystem();
        vision.init(hardwareMap);

        telemetry.addLine("Robot Kullanıma Hazır ");
        telemetry.update();

        waitForStart();
        if (isStopRequested()) return;

        vision.start();

        while (opModeIsActive()) {
            double x  = gamepad1.left_stick_x;
            double rx = gamepad1.right_stick_x;
            double y  = -gamepad1.left_stick_y;

            if (gamepad1.options) {
                drive.resetYaw();
            }

            // Şasi Sürüşü
            drive.driveRobotCentric(x, y, rx,SLOW_MODE_FACTOR);

            // AprilTag ile Otomatik Hizalama (X Butonu)
            if (gamepad1.x){
                double turn = vision.getGoalHeadingCorrection();
                double fwd  = vision.getGoalDistanceCorrection();
                drive.driveRobotCentric(0, fwd, turn,SLOW_MODE_FACTOR);
            }

            // --- SHOOTER KONTROLÜ (B Butonu Toggle) ---
            if (shooterToggle.update(gamepad1.b)) {
                shooter.setEnabled(!shooter.isEnabled());
            }

            if (shooter.isEnabled()) {
                shooter.forward(1200.0);
            } else {
                shooter.stop();
            }



            if (gamepad1.a || gamepad1.dpad_up || shooter.isEnabled()) {
                transport.forward(1.0);

                if (shooter.isEnabled()) {
                    intake.forward(0.7);  // Shooter aktifken ring besleme hızı
                } else if (gamepad1.a) {
                    intake.forward(1.0);  // Sadece A basılıysa tam güç intake
                } else {
                    intake.stop();        // dpad_up basılıysa intake dursun, sadece transport dönsün
                }
            }
            else if (gamepad1.dpad_down) {
                transport.reverse(1.0);
                intake.stop();
            }
            else {
                // Hiçbir butona basılmıyorsa ve shooter kapalıysa her şeyi güvenle durdur
                transport.stop();
                intake.stop();
            }


            // --- FEEDER KONTROLÜ (Y Butonu Toggle & Otomatik Tetikleme) ---
            if (feederToggle.update(gamepad1.y)) {
                feeder.setEnabled(!feeder.isEnabled());
            }

            // Eğer Y ile feeder açıldıysa VEYA shooter çalışıp yeterli hıza ulaştıysa tetiği uzat
            // (== 1200 kontrolü motor dalgalanmasından dolayı takılabileceği için >= 1150 yapıldı)
            if (feeder.isEnabled() || (shooter.isEnabled() && shooter.getVelocity() >= 1150)) {
                feeder.extendServo();
            } else {
                feeder.retractServo();
            }


            // Telemetry Verileri
            telemetry.addData("Apriltag Seen", vision.hasAnyTarget());
            telemetry.addData("Feeder Enabled", feeder.isEnabled());
            telemetry.addData("Shooter Enabled", shooter.isEnabled());
            telemetry.addData("Shooter Velocity", shooter.getVelocity());
            telemetry.addData("Distance(CM)", vision.getDistance());
            telemetry.update();
        }
    }
}