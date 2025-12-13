package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class ShooterSubsystem {

    // ================= DONANIMLAR =================
    private final DcMotorEx shooterMotorP; // Primary
    private final DcMotorEx shooterMotorS; // Secondary
    private final Servo hoodServo;

    private boolean enabled = false;

    // ================= KALİBRASYON TABLOLARI =================
    // Mesafe (inç) tablosu
    private final double[] calRangesIn  = { 40,   60,   80,   100 };

    // O mesafeye karşılık gelen Hız (Derece/Saniye)
    private final double[] calSpeedsDeg = { 1500, 1600, 1800, 2200 };

    // O mesafeye karşılık gelen Hood Pozisyonu (0.0 - 1.0 arası)
    // NOT: Bu değerleri robotunu test ederek bulmalısın!
    private final double[] calHoodPos   = { 0.45, 0.40, 0.35, 0.30 };

    private static final double MAX_DPS = 2500.0; // Maksimum hız limiti

    // ================= CONSTRUCTOR =================
    public ShooterSubsystem(HardwareMap hw) {
        // --- Motor Tanımları ---
        shooterMotorP = hw.get(DcMotorEx.class, "motor_shooter_p");
        shooterMotorS = hw.get(DcMotorEx.class, "motor_shooter_s");

        // --- Servo Tanımı ---
        // Config dosyasında servo isminin "servo_hood" olduğundan emin ol
        hoodServo = hw.get(Servo.class, "servo_hood");

        // --- Yön Ayarları ---
        shooterMotorP.setDirection(DcMotorSimple.Direction.REVERSE);
        shooterMotorS.setDirection(DcMotorSimple.Direction.FORWARD);

        // --- Davranış Ayarları ---
        shooterMotorP.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooterMotorS.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        // --- Encoder ve Mod Ayarları ---
        // Önce encoderları sıfırla
        shooterMotorP.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shooterMotorS.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        // Hız kontrolü (Velocity Control) için RUN_USING_ENCODER kullanıyoruz
        shooterMotorP.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        shooterMotorS.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    // ================= ENABLE / DISABLE =================
    public void setEnabled(boolean on) {
        enabled = on;
        if (!on) {
            stopShooter();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    private void stopShooter() {
        shooterMotorP.setVelocity(0);
        shooterMotorS.setVelocity(0);
    }

    // ================= MAIN API =================
    /**
     * Mesafeye göre hem atıcı hızını hem de hood açısını ayarlar.
     * @param rangeInches Hedefe olan uzaklık (inç)
     */
    public void regulateByRange(double rangeInches) {
        if (!enabled) {
            stopShooter();
            return;
        }

        // 1. Enterpolasyon ile hedef değerleri bul
        double targetSpeedDegPerSec = interpolate(rangeInches, calRangesIn, calSpeedsDeg);
        double targetHoodPosition   = interpolate(rangeInches, calRangesIn, calHoodPos);

        // 2. Motorlara hız ver (RUN_USING_ENCODER olduğu için setVelocity kullanıyoruz)
        // AngleUnit.DEGREES kullanarak derece/saniye cinsinden hız veriyoruz.
        shooterMotorP.setVelocity(targetSpeedDegPerSec, AngleUnit.DEGREES);
        shooterMotorS.setVelocity(targetSpeedDegPerSec, AngleUnit.DEGREES);

        // 3. Servoyu ayarla
        hoodServo.setPosition(targetHoodPosition);
    }

    // Manuel test veya sabit atışlar için
    public void setManualPower(double speedDPS, double hoodPos) {
        if(!enabled) return;
        shooterMotorP.setVelocity(speedDPS, AngleUnit.DEGREES);
        shooterMotorS.setVelocity(speedDPS, AngleUnit.DEGREES);
        hoodServo.setPosition(hoodPos);
    }

    // Telemetri için mevcut hızı döner (Primary motoru baz alır)
    public double getCurrentVelocity() {
        return shooterMotorP.getVelocity(AngleUnit.DEGREES);
    }

    // ================= HELPER (INTERPOLATION) =================
    // Hem hız hem de hood için tek bir enterpolasyon fonksiyonu kullanabiliriz
    private double interpolate(double r, double[] ranges, double[] outputs) {
        // Menzil dışı (çok yakın)
        if (r <= ranges[0]) return outputs[0];

        // Menzil dışı (çok uzak)
        if (r >= ranges[ranges.length - 1]) return outputs[outputs.length - 1];

        // Aradaki değerler için lineer enterpolasyon
        for (int i = 1; i < ranges.length; i++) {
            if (r <= ranges[i]) {
                double t = (r - ranges[i - 1]) / (ranges[i] - ranges[i - 1]);
                return outputs[i - 1] + t * (outputs[i] - outputs[i - 1]);
            }
        }
        return outputs[outputs.length - 1];
    }
}