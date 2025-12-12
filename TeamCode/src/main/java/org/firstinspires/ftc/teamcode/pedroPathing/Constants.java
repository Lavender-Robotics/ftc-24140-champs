package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.control.FilteredPIDFCoefficients;
import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;
import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.pedropathing.ftc.localization.constants.PinpointConstants;
import com.pedropathing.paths.PathConstraints;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;


public class Constants {
    public static final FollowerConstants followerConstants = new FollowerConstants()
            .mass(13.0)

            // Translational PIDF: scaled for mass and 312 RPM limitation
            // P increased (0.15) to counteract heavier inertia
            // I remains zero (no steady-state error in this regime)
            // D increased (0.02) for damping oscillation from added mass
            .translationalPIDFCoefficients(new PIDFCoefficients(0.15, 0, 0.02, 0))
            //.secondaryTranslationalPIDFCoefficients(new PIDFCoefficients(0.12, 0, 0.015, 0))

            // Heading PIDF: rotational dynamics scale differently than translation
            // P increased (0.14) for tighter heading hold
            // D increased (0.025) to prevent wobble from moment of inertia
            .headingPIDFCoefficients(new PIDFCoefficients(0.14, 0, 0.025, 0))
            //.secondaryHeadingPIDFCoefficients(new PIDFCoefficients(0.11, 0, 0.02, 0))

            // Drive PIDF (motor velocity control): critical for 312 RPM motors
            // P increased (0.14) for responsive acceleration
            // D increased (0.015) for smooth ramp without overshoot
            // F (feedforward) keeps the same (0.6) as motor characteristics dominate
            .drivePIDFCoefficients(new FilteredPIDFCoefficients(0.14, 0.0, 0.015, 0.6, 0.0))
            //.secondaryDrivePIDFCoefficients(new FilteredPIDFCoefficients(0.11, 0, 0.012, 0.6, 0.01))

            .centripetalScaling(0.005)

            .useSecondaryTranslationalPIDF(false)
            .useSecondaryHeadingPIDF(false)
            .useSecondaryDrivePIDF(false)
            .forwardZeroPowerAcceleration(-29.484908198835896)
            .lateralZeroPowerAcceleration(-46.33016375242318);

    // ========== PATH (YOL) KISITLAMALARI ==========
    public static final PathConstraints pathConstraints = new PathConstraints(
            64.82643620047983,   // max lineer hız (inç/sn) – measured
            53.84999000369095    // max lineer ivme (inç/sn²) – measured
    );

    public static final MecanumConstants driveConstants = new MecanumConstants()
            .maxPower(1.0)
            .rightFrontMotorName("front_right")
            .rightRearMotorName("back_right")
            .leftRearMotorName("back_left")
            .leftFrontMotorName("front_left")
            .xVelocity(65.15093081016241)
            .yVelocity(55.679219884196606)
            .leftFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
            .leftRearMotorDirection(DcMotorSimple.Direction.REVERSE)
            .rightFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
            .rightRearMotorDirection(DcMotorSimple.Direction.FORWARD);

    public static final PinpointConstants localizerConstants = new PinpointConstants()
            .distanceUnit(DistanceUnit.INCH)
            .hardwareMapName("pinpoint")
            .encoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD)
            .forwardEncoderDirection(GoBildaPinpointDriver.EncoderDirection.FORWARD)
            .strafeEncoderDirection(GoBildaPinpointDriver.EncoderDirection.FORWARD)
            .forwardPodY(-1.772)
            .strafePodX(-6.89);

//    public static final double FORWARD_MULTIPLIER = -30176.036036758956;
//    public static final double LATERAL_MULTIPLIER = 42188.036632755916;
//    public static final double TURN_MULTIPLIER    = 1.00659472956212005;

    public static Follower createFollower(HardwareMap hardwareMap) {
        return new FollowerBuilder(followerConstants, hardwareMap)
                .pathConstraints(pathConstraints)
                .mecanumDrivetrain(driveConstants)
                .pinpointLocalizer(localizerConstants)
                .build();
    }


}