package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.subsystems.DriveSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FeederSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.ShooterSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TransportSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.VisionSubsystem;

@Autonomous(name="MainAutonoum")
public class MainAuto extends OpMode {
    private DcMotorEx fl;
    private DcMotorEx br;
    private DcMotorEx bl;
    private DcMotorEx fr;
    private DriveSubsystem drive;
    private IntakeSubsystem intake;
    private TransportSubsystem transport;
    private FeederSubsystem feeder;
    private ShooterSubsystem shooter;
    private VisionSubsystem vision;
    private ElapsedTime timer;
    private final ElapsedTime feederTimer = new ElapsedTime();
    private boolean feederExtended = false;

    @Override
    public void init() {
        drive = new DriveSubsystem(hardwareMap, telemetry);
        intake = new IntakeSubsystem(hardwareMap);
        transport = new TransportSubsystem(hardwareMap);
        feeder = new FeederSubsystem(hardwareMap);
        shooter = new ShooterSubsystem(hardwareMap);
        vision = new VisionSubsystem();
        vision.init(hardwareMap);
        timer = new ElapsedTime();
        telemetry.addLine("Robot Kullanıma Hazır ");
        telemetry.update();
        vision.start();
        fl = hardwareMap.get(DcMotorEx.class, "frontLeftMotor");
        bl = hardwareMap.get(DcMotorEx.class, "backLeftMotor");
        fr = hardwareMap.get(DcMotorEx.class, "frontRightMotor");
        br = hardwareMap.get(DcMotorEx.class, "backRightMotor");

        // Donanım yönleri
        fl.setDirection(DcMotorSimple.Direction.REVERSE);
        bl.setDirection(DcMotorSimple.Direction.REVERSE);
        fr.setDirection(DcMotorSimple.Direction.FORWARD);
        br.setDirection(DcMotorSimple.Direction.FORWARD);

        fl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        bl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        fr.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        br.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

    }

    @Override
    public void start() {
        // START'a basildigi an zamanlama sifirlanir (init'te degil!)
        timer.reset();
        feederTimer.reset();
        feederExtended = false;
    }

    @Override
    public void loop() {
        double t = timer.seconds();

        if (t < 1.0) {
            // 1) Ilk 1 saniye geri git
            backward();
        } else if (t < 3.0) {
            // 2) Vision ile hizalan: mesafe -> ileri/geri, heading -> donus
            double forward = vision.getGoalDistanceCorrection();
            double turn    = vision.getGoalHeadingCorrection();
            drive.driveRobotCentric(0.0, forward, turn, 1.0);
        } else {
            // 3) Hizalama bitti: yerinde dur, shooter + feeder + intake + transport
            stop();
                 shooter.forward(1000);
            if (shooter.getVelocity() >= 980 && shooter.getVelocity() <= 1020) {
                // Her 300 ms'de bir extend <-> retract arasında geçiş yap
                if (feederTimer.milliseconds() >= 300) {
                    if (feederExtended) {
                        feeder.retractServo();
                    } else {
                        feeder.extendServo();
                    }
                    feederExtended = !feederExtended;
                    feederTimer.reset();
                }
            }
            intake.forward(1.0);
            transport.forward(1.0);
        }
    }

    public void forward(){
        fl.setPower(0.5);
        br.setPower(0.5);
        fr.setPower(0.5);
        bl.setPower(0.5);

    }
    public void backward(){
        fl.setPower(-0.6);
        br.setPower(-0.6);
        fr.setPower(-0.6);
        bl.setPower(-0.6);
    }
    public void stop(){
        fl.setPower(0.0);
        br.setPower(0.0);
        fr.setPower(0.0);
        bl.setPower(0.0);

    }
}
