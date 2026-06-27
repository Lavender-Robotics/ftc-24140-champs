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

            // --- INTAKE TOGGLE / MANUAL REVERSE KONTROLÜ (A ve Dpad) ---
            if (gamepad1.dpad_down) {
                shooter.reverse(100);
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
                if(gamepad1.dpad_right){
                    intake.reverse(1.0);
                    reverse = true;
                    isIntakeActive = false;

                }
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
                }
            }

            if (manualShooterToggle.update(gamepad1.right_bumper)) {
                manualMode = !manualMode;
                if (manualMode) {
                    assistedMode = false;
                    isIntakeActive = false;
                }
            }

            // Shooter kesintisiz dönme kuralı (İstediğin gibi 1000.0 velocity)
            if (assistedMode || manualMode) {
                shooter.forward(1000.0);
            } else {
                shooter.stop();
            }


            // --- TAMAMEN YENİLENEN ZAMAN AYARLI SEQUENCE SHOOTING MACROSU ---
            if (assistedMode) {
                long elapsedTime = System.currentTimeMillis() - assistedStartTime;

                if (elapsedTime < 4000) {
                    // 1. ADIM: İlk 4 saniye shooter 1000 hızında döner, mekanizmalar bekler.
                    feeder.retractServo();
                    transport.stop();
                    intake.stop();
                }
                else if (elapsedTime >= 4000 && elapsedTime < 4750) {
                    // 2. ADIM: 4 saniye geçince feeder extendServo yapar (750ms sürer)
                    feeder.extendServo();
                    transport.stop();
                    intake.stop();
                }
                else if (elapsedTime >= 4750 && elapsedTime < 5750) {
                    // 3. ADIM: 750ms sonra feeder retractServo olur ve transport forward çalışır (1 saniye sürer)
                    feeder.retractServo();
                    transport.forward(0.8);
                    intake.stop();
                }
                else if (elapsedTime >= 5750 && elapsedTime < 6500) {
                    // 4. ADIM: 1 saniye geçince transport durur ve feeder extendServo yapar (750ms sürer)
                    feeder.extendServo();
                    transport.stop();
                    intake.stop();
                }
                else if (elapsedTime >= 6500 && elapsedTime < 9000) {
                    // 5. ADIM: 750ms sonra feeder retractServo olur ve intake 2500ms boyunca forward çalışır
                    feeder.retractServo();
                    transport.stop();
                    intake.forward(1.0);
                }
                else if (elapsedTime >= 9000 && elapsedTime < 10000) {
                    // 6. ADIM: Süre bitince intake durur, transport 1 saniye boyunca forward çalışır
                    feeder.retractServo();
                    transport.forward(0.8);
                    intake.stop();
                }
                else if (elapsedTime >= 10000 && elapsedTime < 10750) {
                    // 7. ADIM: Ardından transport durur ve kicker extended (feeder extend) olur (750ms sürer)
                    feeder.extendServo();
                    transport.stop();
                    intake.stop();
                }
                else {
                    // 8. ADIM: 750ms sonra feeder retractServo yapar ve makro tamamen biterek kapanır.
                    feeder.retractServo();
                    transport.stop();
                    intake.stop();
                    assistedMode = false; // B'ye tekrar bastığında sıfırdan başlaması için modu kapatır.
                }
            }
            else if (isIntakeActive) {
                intake.forward(1.0);
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
            telemetry.addData("[MOD] Sequenced Shooting (B)", assistedMode ? "SENKRONİZE MAKRO AKTİF" : "KAPALI");
            telemetry.addData("Shooter Velocity", shooter.getVelocity());
            telemetry.update();
        }
    }
}