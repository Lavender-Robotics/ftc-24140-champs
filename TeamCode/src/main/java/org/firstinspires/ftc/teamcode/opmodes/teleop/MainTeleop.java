package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.subsystems.DriveSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TransportSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FeederSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.ShooterSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.VisionSubsystem;
import org.firstinspires.ftc.teamcode.util.Toggle;

@TeleOp(name = "MainTeleOp")
public class MainTeleop extends LinearOpMode {

    // Subsystems
    private DriveSubsystem drive;
    private IntakeSubsystem intake;
    private TransportSubsystem transport;
    private FeederSubsystem feeder;
    private ShooterSubsystem shooter;
    private VisionSubsystem vision;

    // Toggles
    private final Toggle feederToggle  = new Toggle();
    private final Toggle shooterToggle = new Toggle();

    @Override
    public void runOpMode() throws InterruptedException {

        drive     = new DriveSubsystem(hardwareMap, telemetry);
        intake    = new IntakeSubsystem(hardwareMap);
        transport = new TransportSubsystem(hardwareMap);
        feeder    = new FeederSubsystem(hardwareMap);
        shooter   = new ShooterSubsystem(hardwareMap);
        vision    = new VisionSubsystem(hardwareMap, telemetry);

        telemetry.addLine("Robot Hazır");
        telemetry.update();

        waitForStart();
        if (isStopRequested()) return;

        while (opModeIsActive()) {

            // ================= DRIVE =================
            double x  = gamepad1.left_stick_x;
            double y  = -gamepad1.left_stick_y;
            double rx = gamepad1.right_stick_x;

            if (gamepad1.options) {
                drive.resetYaw();
            }

            drive.driveRobotCentric(x, y, rx);

            // ================= INTAKE =================
            if (gamepad1.a) intake.forward(1.0);
            else intake.stop();

            // ================= TRANSPORT =================
            if (gamepad1.dpad_up) {
                transport.forward(1.0);
            } else if (gamepad1.dpad_down) {
                transport.reverse(1.0);
            } else {
                transport.stop();
            }

            // ================= FEEDER =================
            if (feederToggle.update(gamepad1.y)) {
                feeder.setEnabled(!feeder.isEnabled());
            }

            if (feeder.isEnabled()) feeder.extendServo();
            else feeder.retractServo();

            // ================= SHOOTER (PIDF) =================
            if (shooterToggle.update(gamepad1.b)) {
                shooter.setEnabled(!shooter.isEnabled());
            }

            // ŞİMDİLİK SABİT MESAFE (inch)
            double targetRangeInches = 75;

            // PIDF HER LOOP’TA ÇALIŞIR
            if (shooter.isEnabled()) {
                shooter.regulateByRange(targetRangeInches);
            }

            // ================= TELEMETRY =================
            telemetry.addData("Shooter Enabled", shooter.isEnabled());
            telemetry.addData("Feeder Enabled", feeder.isEnabled());
            telemetry.update();
        }

        vision.close();
    }
}
