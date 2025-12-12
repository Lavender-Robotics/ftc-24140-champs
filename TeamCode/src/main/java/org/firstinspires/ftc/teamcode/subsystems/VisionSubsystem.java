package org.firstinspires.ftc.teamcode.subsystems;

import android.util.Size;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;

import java.util.List;


public class VisionSubsystem {

    private static final double CAMERA_HEIGHT = 12.992126; // Kameranın yerden yüksekliği
    private static final double CAMERA_ANGLE = 10.0;  // Kameranın açısı
    private static final double TAG_HEIGHT = 6.0;     // AprilTag'in merkez yüksekliği (örnek)
    private static final String WEBCAM_NAME = "Webcam 1";


    private AprilTagProcessor aprilTag;
    private VisionPortal visionPortal;
    private HardwareMap hardwareMap;

    private List<AprilTagDetection> detectedTags;
    private final Telemetry telemetry;



    public VisionSubsystem(HardwareMap hardwareMap, Telemetry telemetry) {
        this.hardwareMap = hardwareMap;
        this.telemetry = telemetry;

        aprilTag = new AprilTagProcessor.Builder()
                .setDrawAxes(true)
                .setDrawCubeProjection(true)
                .setDrawTagOutline(true)
                .setTagLibrary(AprilTagGameDatabase.getCenterStageTagLibrary())
                .setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
                .build();

        VisionPortal.Builder builder = new VisionPortal.Builder();
        builder.setCamera(hardwareMap.get(WebcamName.class, WEBCAM_NAME));
        builder.setCameraResolution(new Size(640, 480));
        builder.addProcessor(aprilTag);

        visionPortal = builder.build();
    }

    public void update() { detectedTags = aprilTag.getDetections(); }
    public List<AprilTagDetection> getDetections() { return detectedTags; }
    public AprilTagDetection getTagByID(int id) {
        for (AprilTagDetection detection : detectedTags) {
            if (detection.id == id) return detection;
        }
        return null;
    }

    public void close() { if (visionPortal != null) visionPortal.close(); }

    // Telemetry
    public void displayDetectionTelemetry(AprilTagDetection tag){
        if (tag == null) return;
        if (tag.metadata != null) {
            telemetry.addLine(String.format("\n=== (ID %d) %s ===", tag.id, tag.metadata.name));
            telemetry.addLine(String.format("XYZ %6.1f, %6.1f, %6.1f  (inch)", tag.ftcPose.x, tag.ftcPose.y, tag.ftcPose.z));
            telemetry.addLine(String.format("PRY %6.1f, %6.1f, %6.1f  (deg)", tag.ftcPose.pitch, tag.ftcPose.roll, tag.ftcPose.yaw));
            telemetry.addLine(String.format("RBE %6.1f, %6.1f, %6.1f  (inch, deg, deg)", tag.ftcPose.range, tag.ftcPose.bearing, tag.ftcPose.elevation));
        }else {
            telemetry.addLine(String.format("\n==== (ID %d) Unknown", tag.id));
            telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", tag.center.x, tag.center.y));
        }

        telemetry.addLine("\nkey:\nXYZ = X (Right), Y (Forward), Z (Up) dist.");
        telemetry.addLine("PRY = Pitch, Roll & Yaw (XYZ Rotation)");
        telemetry.addLine("RBE = Range, Bearing & Elevation");

    }


















//    public double getDistanceToTag(AprilTagDetection detection){
//        if (detection == null || detection.ftcPose == null) return 0;
//
//        double deltaHeight = TAG_HEIGHT - CAMERA_HEIGHT;
//
//        double detectionAngle = detection.ftcPose.elevation;
//        double totalAngleDeg = CAMERA_ANGLE + detectionAngle;
//        double totalAngleRad = Math.toRadians(totalAngleDeg);
//
//        if (Math.abs(Math.tan(totalAngleRad)) < 0.001) return 0; // Çok küçük açı hatası
//        double distance = deltaHeight / Math.tan(totalAngleRad);
//
//        return Math.abs(distance);
//    }






}
