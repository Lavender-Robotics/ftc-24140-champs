package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.subsystems.DriveSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.ShooterSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TransportSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FeederSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.VisionSubsystem;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;

import org.firstinspires.ftc.teamcode.util.Toggle;
import java.util.List;


@TeleOp(name = "MainTeleOp2")
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
        drive  = new DriveSubsystem(hardwareMap, telemetry);
        intake = new IntakeSubsystem(hardwareMap);
        transport = new TransportSubsystem(hardwareMap);
        feeder = new FeederSubsystem(hardwareMap);
        shooter = new ShooterSubsystem(hardwareMap);
        vision = new VisionSubsystem(hardwareMap, telemetry);



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


            // Shooter - Hood
            shooter.moveHoodManually(gamepad1.left_bumper, gamepad1.left_trigger > 0.2);

            // Shooter
            if (shooterToggle.update(gamepad1.b)) {shooter.setEnabled(!shooter.isEnabled());}
            if (shooter.isEnabled()) shooter.forward(1200.0);
            else shooter.stop();



            // Telemetry
            vision.getTagTelemetry(); // for vision test

            telemetry.addData("Feeder Enabled", feeder.isEnabled());
            telemetry.addData("Shooter Enabled", shooter.isEnabled());
            telemetry.addData("Shooter Velocity", shooter.getVelocity());
            telemetry.addData("Hood Position", shooter.getHoodPosition());


            telemetry.update();
        }

        vision.close();
    }
}