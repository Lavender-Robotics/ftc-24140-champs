package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.DriveSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.VisionSubsystem;

@TeleOp(name = "LLTest", group = "test")
public class LLTest extends OpMode {
    private DriveSubsystem drive;
    private VisionSubsystem LL;

    @Override
    public void init() {
        drive = new DriveSubsystem(hardwareMap, telemetry);
        LL = new VisionSubsystem();
        LL.init(hardwareMap);
    }

    @Override
    public void start() {
        LL.start();
        drive.resetYaw();   // "ileri" yonunu su anki heading'e gore sifirla
    }

    @Override
    public void loop() {
        double x  = gamepad1.left_stick_x;
        double y  = -gamepad1.left_stick_y;
        double rx = gamepad1.right_stick_x;

        // Y basılıyken dönüş gücünü stickten değil, Limelight'tan al
        if (gamepad1.y) {
            rx = LL.getGoalHeadingCorrection();
        }

        drive.driveFieldCentric(x, y, rx);

        // --- Teshis telemetrisi (surusu etkilemez) ---
        telemetry.addData("Heading (deg)", "%.1f", drive.getHeadingDeg());
        drive.logEncoders(telemetry);
        telemetry.addLine("TEST: Sol stick ILERI it, sayaclara bak.");
        telemetry.addLine(" - Hepsi duzgun ARTMALI/AZALMALI.");
        telemetry.addLine(" - Biri sabit/zipliyorsa o motorun encoder'i bozuk.");
        telemetry.update();
    }

    @Override
    public void stop() {
        LL.stop();
    }
}