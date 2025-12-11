package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.subsystems.DriveSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TransportSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FeederSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.VisionSubsystem;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;

import org.firstinspires.ftc.teamcode.util.Toggle;
import java.util.List;


@TeleOp(name = "MainTeleOp")
public class MainTeleop extends LinearOpMode {



    // Subsystem Init
    private DriveSubsystem drive;
    private IntakeSubsystem intake;
    private TransportSubsystem transport;
    private FeederSubsystem feeder;

    private VisionSubsystem vision;


    // Toggles
    private final Toggle feederToggle = new Toggle();



    @Override
    public void runOpMode() throws InterruptedException {
        drive  = new DriveSubsystem(hardwareMap, telemetry);
        intake = new IntakeSubsystem(hardwareMap);
        transport = new TransportSubsystem(hardwareMap);
        feeder = new FeederSubsystem(hardwareMap);
        vision = new VisionSubsystem(hardwareMap);

        vision.init();

        telemetry.addLine("Robot Kullanıma Hazır ");
        telemetry.update();

        waitForStart();
        if (isStopRequested()) return;

        while (opModeIsActive()) {
            double x  = gamepad1.left_stick_x;
            double rx  = gamepad1.right_stick_x;
            double y = -gamepad1.left_stick_y;

            if (gamepad1.options) {
                drive.resetYaw(); // robot-oriented'te şart değil; kalabilir
            }


            drive.driveRobotCentric(x, y, rx);


            // Intake (A)
            if (gamepad1.a) intake.forward(1.0);
            else intake.stop();


            // Transport
            if (gamepad1.dpad_up)           transport.forward(1.0);
            else if (gamepad1.dpad_down)    transport.reverse(1.0);
            else                            transport.stop();


            // Feeder
            if (feederToggle.update(gamepad1.y))  feeder.setEnabled(!feeder.isEnabled());
            if (feeder.isEnabled())         feeder.extendServo();
            else                            feeder.retractServo();

            // Vision
            List<AprilTagDetection> currentDetections = vision.getDetections();
            if (!currentDetections.isEmpty()) {
                for (AprilTagDetection detection : currentDetections) {
                    if (detection.metadata != null) {

                        double myDistance = vision.getDistanceToTag(detection);

                        telemetry.addLine(String.format("Tag ID: %d", detection.id));
                        telemetry.addLine(String.format("Hesaplanan Mesafe: %.2f inch", myDistance));
                        telemetry.addLine(String.format("SDK Range: %.2f inch", detection.ftcPose.range));
                        telemetry.addLine("-----------------");
                    }
                }
            } else telemetry.addLine("Görüntüde AprilTag Yok.");

            telemetry.update();
        }
    }
}