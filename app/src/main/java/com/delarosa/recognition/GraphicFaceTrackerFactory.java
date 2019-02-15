package com.delarosa.recognition;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

/**
 * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
 * uses this factory to create face trackers as needed -- one for each individual.
 */
public class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
    CameraSource mCameraSource;

    public GraphicFaceTrackerFactory() {

    }

    public void setCameraSource(CameraSource mCameraSource) {
        this.mCameraSource = mCameraSource;
    }

    @Override
    public Tracker<Face> create(Face face) {
        return new GraphicFaceTracker(mCameraSource);
    }
}
