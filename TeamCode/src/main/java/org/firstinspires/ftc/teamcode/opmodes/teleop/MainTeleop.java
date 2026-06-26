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
    private boolean reverse = false;

    // Toggles
    private final Toggle feederToggle = new Toggle();
    private final Toggle assistedShooterToggle = new Toggle();
    private final Toggle manualShooterToggle = new Toggle();
    private final Toggle intakeToggle = new Toggle();
    private final Toggle slowModeToggle = new Toggle();

    // Durum Değişkenleri
    private boolean assistedMode = false;
    private boolean manualMode = false;
    private boolean isIntakeActive = false;
    private boolean isSlowMode = false;

    // Sequenced Atış ve Kicker Zamanlayıcıları
    private long assistedStartTime = 0;
    private long kickerTimer = 0;

    @Override
    public void runOpMode() throws InterruptedException {
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

            // --- DYNAMIC SLOW MODE KONTROLÜ (Back Butonu) ---
            if (slowModeToggle.update(gamepad1.back)) {
                isSlowMode = !isSlowMode;
            }

            // Şasi Sürüşü
            double currentDriveFactor = isSlowMode ? SLOW_MODE_FACTOR : 1.0;
            drive.driveRobotCentric(x, y, rx, currentDriveFactor);

            // AprilTag ile Otomatik Hizalama (X Butonu)
            if (gamepad1.x){
                double turn = vision.getGoalHeadingCorrection();
                double fwd  = vision.getGoalDistanceCorrection();
                drive.driveRobotCentric(0, fwd, turn, currentDriveFactor);
            }

            // --- INTAKE TOGGLE / MANUAL REVERSE KONTROLÜ (A ve Dpad) ---
            if (gamepad1.dpad_down) {
                intake.stop();
                transport.reverse(1.0);
                reverse = true;
                isIntakeActive = false;
            } else if (gamepad1.dpad_up) {
                intake.stop();
                transport.forward(1.0);
                reverse = true;
                isIntakeActive = false;
            } else {
                reverse = false;

                if (intakeToggle.update(gamepad1.a)) {
                    isIntakeActive = !isIntakeActive;
                    if (isIntakeActive) {
                        assistedMode = false;
                        manualMode = false;
                    }
                }
            }

            // --- SHOOTER MOD SEÇİMLERİ (B ve RB) ---
            if (assistedShooterToggle.update(gamepad1.b)) {
                assistedMode = !assistedMode;
                if (assistedMode) {
                    manualMode = false;
                    isIntakeActive = false;

                    assistedStartTime = System.currentTimeMillis();
                    kickerTimer = System.currentTimeMillis();
                }
            }

            if (manualShooterToggle.update(gamepad1.right_bumper)) {
                manualMode = !manualMode;
                if (manualMode) {
                    assistedMode = false;
                    isIntakeActive = false;
                }
            }

            // Shooter kesintisiz dönme kuralı
            if (assistedMode || manualMode) {
                shooter.forward(1000.0);
            } else {
                shooter.stop();
            }


            // --- TRANSPORT VE INTAKE'İN HİÇ DURMADIĞI SEQUENCE SHOOTING ---
            if (assistedMode) {
                long elapsedTime = System.currentTimeMillis() - assistedStartTime;

                if (elapsedTime < 2000) {
                    // HIZLANMA FAZI: İlk 2 saniye shooter hızlanırken topları koruma amaçlı geride tut
                    transport.reverse(0.2);
                    intake.stop();
                    feeder.retractServo();
                    kickerTimer = System.currentTimeMillis();
                }
                else {
                    // İSTEDİĞİN GÜNCELLEME: B modu aktif ve ilk hızlanma bittiyse,
                    // hız veya döngü ne olursa olsun transport ve intake HİÇ DURMADAN sürekli dönüyor.
                    transport.forward(0.8);
                    intake.forward(0.7);

                    // Toplam Periyot: 1200ms (500ms Kicker Yukarıda / 700ms Kicker Aşağıda)
                    long cycleTime = (System.currentTimeMillis() - kickerTimer) % 1200;
                    boolean kickerExtended = (cycleTime < 500);

                    // Kicker sadece shooter hızı güvenli limitin (1000 RPM) üzerindeyse çalışır
                    if (shooter.getVelocity() >= 1000) {
                        if (kickerExtended) {
                            feeder.extendServo(); // Kicker yukarı vuruyor, transport zaten arkadan ringi presliyor
                        } else {
                            feeder.retractServo(); // Kicker aşağı inip sonraki ringin yolunu açıyor
                        }
                    } else {
                        // Eğer shooter hızı 1000'in altına düşerse, motor toparlanana kadar kicker güvenle aşağıda bekler
                        feeder.retractServo();
                    }
                }
            }
            else if (isIntakeActive) {
                intake.forward(1.0);
                transport.forward(0.6);
                feeder.retractServo();
            }
            else if (!reverse) {
                intake.stop();
                transport.stop();

                if (feederToggle.update(gamepad1.y)) {
                    feeder.setEnabled(!feeder.isEnabled());
                }

                if (feeder.isEnabled()) {
                    feeder.extendServo();
                } else {
                    feeder.retractServo();
                }
            }

            // Telemetry Verileri
            telemetry.addData("Slow Mode (Back)", isSlowMode ? "AKTİF (YAVAŞ)" : "KAPALI (TAM GÜÇ)");
            telemetry.addData("[MOD] Sequenced Shooting (B)", assistedMode ? "TAM KESİNTİSİZ BESLEME" : "KAPALI");
            telemetry.addData("Shooter Velocity", shooter.getVelocity());
            telemetry.update();
        }
    }
}