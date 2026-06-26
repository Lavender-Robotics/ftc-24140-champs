package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class DriveSubsystem {
    public static final double SLOW_MODE_FACTOR = 0.35;
    private boolean fieldCentric      = false;
    private boolean lastRightBumper   = false;
    private final DcMotorEx fl, fr, bl, br;
    private final IMU imu;
    private final Telemetry telemetry;

    public DriveSubsystem(HardwareMap hw, Telemetry telemetry) {
        this.telemetry = telemetry;

        fl = hw.get(DcMotorEx.class, "frontLeftMotor");
        bl = hw.get(DcMotorEx.class, "backLeftMotor");
        fr = hw.get(DcMotorEx.class, "frontRightMotor");
        br = hw.get(DcMotorEx.class, "backRightMotor");

        // Donanım yönleri
        fl.setDirection(DcMotorSimple.Direction.REVERSE);
        bl.setDirection(DcMotorSimple.Direction.REVERSE);
        fr.setDirection(DcMotorSimple.Direction.FORWARD);
        br.setDirection(DcMotorSimple.Direction.FORWARD);

        fl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        bl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        fr.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        br.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Encoder'lari kullan: kapali-cevrim hiz kontrolu (her teker komut hizina kilitlenir)
        for (DcMotorEx m : new DcMotorEx[]{fl, bl, fr, br}) {
            m.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            m.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }

        imu = hw.get(IMU.class, "imu");
        IMU.Parameters params = new IMU.Parameters(
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.LEFT,
                        RevHubOrientationOnRobot.UsbFacingDirection.UP));
        imu.initialize(params);
    }

    public void resetYaw() { imu.resetYaw(); }
    public double getHeadingRad() { return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS); }
    public double getHeadingDeg() { return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES); }

    /** Teshis: dort motorun encoder sayaclarini telemetriye basar. */
    public void logEncoders(Telemetry t) {
        t.addData("FL pos", fl.getCurrentPosition());
        t.addData("FR pos", fr.getCurrentPosition());
        t.addData("BL pos", bl.getCurrentPosition());
        t.addData("BR pos", br.getCurrentPosition());
    }
    public void control(Gamepad gamepad) {
        control(gamepad, 0.0, false);
    }
    //public void control(Gamepad gamepad) {
        //control(gamepad, 0.0, false);
    //}
    public void control(Gamepad gamepad, double rxOverride, boolean useOverride) {
        if (gamepad.right_bumper && !lastRightBumper) {
            fieldCentric = !fieldCentric;
        }
        lastRightBumper = gamepad.right_bumper;

        double y      = -gamepad.left_stick_y;
        double x      = gamepad.left_stick_x;
        double rx     = useOverride ? rxOverride : gamepad.right_stick_x;
        double factor = gamepad.left_trigger > 0.1 ? SLOW_MODE_FACTOR : 1.0;

        if (fieldCentric) {
            driveFieldCentric(x, y, rx, factor);
        } else {
            driveRobotCentric(x, y, rx, factor);
        }
    }
    // ---------- ROBOT-CENTRIC ----------
    // Inputs: x = strafe left(+), y = forward(+), rx = ccw(+)
    public void driveRobotCentric(double x, double y, double rx, double factor) {
        double flP = y + x + rx * factor;
        double blP = y - x + rx * factor;
        double frP = y - x - rx * factor;
        double brP = y + x - rx * factor;
        normalizeAndSet(flP, frP, blP, brP);
    }

    // ---------- FIELD-CENTRIC ----------
    public void driveFieldCentric(double x, double y, double rx, double factor) {
        double h = getHeadingRad();
        // Oteleme vektorunu (x, y) -h kadar dondur; rx (donus) ayri terim
        double rotX = x * Math.cos(-h) - y * Math.sin(-h);
        double rotY = x * Math.sin(-h) + y * Math.cos(-h);

        rotX *= 1.1; // strafe boost

        double flP = rotY + rotX + rx;
        double blP = rotY - rotX + rx;
        double frP = rotY - rotX - rx;
        double brP = rotY + rotX - rx;

        normalizeAndSet(flP, frP, blP, brP);
    }

    private void normalizeAndSet(double flP, double frP, double blP, double brP) {
        double max = Math.max(Math.max(Math.abs(flP), Math.abs(frP)),
                Math.max(Math.abs(blP), Math.abs(brP)));
        if (max > 1.0) {
            flP /= max; frP /= max; blP /= max; brP /= max;
        }
        fl.setPower(flP);
        fr.setPower(frP);
        bl.setPower(blP);
        br.setPower(brP);
    }
}