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

            // Translational PIDF - Balanced for all error ranges
            .translationalPIDFCoefficients(new PIDFCoefficients(
                    0.1,    // P: Mid-range between Pedro's 0.03 and 0.4
                    0.0,    // I: Zero (no integral for path following)
                    0.01,   // D: Light damping for stability
                    0.015   // F: Feedforward from Pedro's example
            ))

            // Heading PIDF - Balanced for all error ranges
            .headingPIDFCoefficients(new PIDFCoefficients(
                    1.2,    // P: Mid-range between Pedro's 0.8 and 2.5
                    0.0,    // I: Zero
                    0.05,   // D: Damping for rotational stability
                    0.01    // F: Feedforward from Pedro's example
            ))

            // Drive PIDF - Motor velocity control
            .drivePIDFCoefficients(new FilteredPIDFCoefficients(
                    0.1,     // P: From Pedro's primary example
                    0.0,     // I: Zero
                    0.00035, // D: From Pedro's example
                    0.6,     // F: Critical feedforward (DO NOT CHANGE)
                    0.015    // Filter: From Pedro's example
            ))

            .centripetalScaling(0.0005)  // From Pedro's official example

            .useSecondaryTranslationalPIDF(false)
            .useSecondaryHeadingPIDF(false)
            .useSecondaryDrivePIDF(false)

            .forwardZeroPowerAcceleration(-28.262818322015583)
            .lateralZeroPowerAcceleration(-48.37583974791519);

    // Path Constraints - From Pedro's official example
    public static final PathConstraints pathConstraints = new PathConstraints(
            0.995,   // tValue constraint (path completion threshold)
            0.1,     // Velocity constraint weight
            0.1,     // Translational constraint weight
            0.009,   // Heading constraint weight
            50,      // Timeout (seconds)
            1.25,    // Braking strength
            10,      // Bezier curve search limit (NEVER CHANGE)
            1        // Braking start distance
    );

    public static final MecanumConstants driveConstants = new MecanumConstants()
            .maxPower(1.0)
            .rightFrontMotorName("front_right")
            .rightRearMotorName("back_right")
            .leftRearMotorName("back_left")
            .leftFrontMotorName("front_left")
            .xVelocity(67.34647063007506)
            .yVelocity(55.24511959045891)
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


    public static Follower createFollower(HardwareMap hardwareMap) {
        return new FollowerBuilder(followerConstants, hardwareMap)
                .pathConstraints(pathConstraints)
                .mecanumDrivetrain(driveConstants)
                .pinpointLocalizer(localizerConstants)
                .build();
    }
}