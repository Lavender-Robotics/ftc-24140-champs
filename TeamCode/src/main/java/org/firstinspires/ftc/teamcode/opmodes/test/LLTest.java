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

        drive.driveRobotCentric(x, y, rx);
    }

    @Override
    public void stop() {
        LL.stop();
    }
}