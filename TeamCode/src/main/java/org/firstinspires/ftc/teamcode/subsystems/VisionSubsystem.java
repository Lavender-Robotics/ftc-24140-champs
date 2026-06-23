//package org.firstinspires.ftc.teamcode.subsystems;
//
//import android.util.Size;
//import com.qualcomm.robotcore.hardware.HardwareMap;
//
//import org.firstinspires.ftc.robotcore.external.Telemetry;
//import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
//import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
//import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
//import org.firstinspires.ftc.robotcore.external.navigation.Position;
//import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
//import org.firstinspires.ftc.vision.VisionPortal;
//import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
//import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
//import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
//
//import java.util.List;
//
//
//public class VisionSubsystem {
//
//    private static final String WEBCAM_NAME = "Webcam 1";
//
//
//    private AprilTagProcessor aprilTag;
//    private VisionPortal visionPortal;
//    private HardwareMap hardwareMap;
//    private final Telemetry telemetry;
//
//
//
//    private Position cameraPosition = new Position(DistanceUnit.INCH, 0, 0, 0, 0);
//    private YawPitchRollAngles cameraOrientation = new YawPitchRollAngles(AngleUnit.DEGREES, 0, -90, 0, 0);
//
//
//
//    public VisionSubsystem(HardwareMap hardwareMap, Telemetry telemetry) {
//        this.hardwareMap = hardwareMap;
//        this.telemetry = telemetry;
//
//        aprilTag = new AprilTagProcessor.Builder()
////                .setDrawAxes(true)
////                .setDrawCubeProjection(true)
////                .setDrawTagOutline(true)
////                .setTagLibrary(AprilTagGameDatabase.getCenterStageTagLibrary())
////                .setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
//                .setCameraPose(cameraPosition, cameraOrientation)
//                .build();
//
//        VisionPortal.Builder builder = new VisionPortal.Builder();
//        builder.setCamera(hardwareMap.get(WebcamName.class, WEBCAM_NAME));
//        builder.setCameraResolution(new Size(640, 480));
//        builder.addProcessor(aprilTag);
//
//        visionPortal = builder.build();
//    }
//
//
//    public void getTagTelemetry() {
//
//        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
//        telemetry.addData("# AprilTags Detected", currentDetections.size());
//
//        // Step through the list of detections and display info for each one.
//        for (AprilTagDetection detection : currentDetections) {
//            if (detection.metadata != null) {
//                telemetry.addLine(String.format("\n==== (ID %d) %s", detection.id, detection.metadata.name));
//                // Only use tags that don't have Obelisk in them
//                if (!detection.metadata.name.contains("Obelisk")) {
//                    telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)",
//                            detection.robotPose.getPosition().x,
//                            detection.robotPose.getPosition().y,
//                            detection.robotPose.getPosition().z));
//                    telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)",
//                            detection.robotPose.getOrientation().getPitch(AngleUnit.DEGREES),
//                            detection.robotPose.getOrientation().getRoll(AngleUnit.DEGREES),
//                            detection.robotPose.getOrientation().getYaw(AngleUnit.DEGREES)));
//                }
//            } else {
//                telemetry.addLine(String.format("\n==== (ID %d) Unknown", detection.id));
//                telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", detection.center.x, detection.center.y));
//            }
//        }   // end for() loop
//
//        // Add "key" information to telemetry
//        telemetry.addLine("\nkey:\nXYZ = X (Right), Y (Forward), Z (Up) dist.");
//        telemetry.addLine("PRY = Pitch, Roll & Yaw (XYZ Rotation)");
//
//    }
//
//
//
//    public void close() { if (visionPortal != null) visionPortal.close(); }
//
//}
