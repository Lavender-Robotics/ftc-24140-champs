package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class DriveSubsystem {
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

    // ---------- ROBOT-CENTRIC ----------
    // Inputs: x = strafe left(+), y = forward(+), rx = ccw(+)
    public void driveRobotCentric(double x, double y, double rx) {
        double flP = y + x + rx;
        double blP = y - x + rx;
        double frP = y - x - rx;
        double brP = y + x - rx;
        normalizeAndSet(flP, frP, blP, brP);
    }

    // ---------- FIELD-CENTRIC ----------
    public void driveFieldCentric(double x, double y, double rx) {
        double h = getHeadingRad();
        double rotX = x * Math.cos(-h) - rx * Math.sin(-h);
        double rotY = x * Math.sin(-h) + rx * Math.cos(-h);

        rotX *= 1.1; // strafe boost

        double flP = rotY + rotX + y;
        double blP = rotY - rotX + y;
        double frP = rotY - rotX - y;
        double brP = rotY + rotX - y;

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