package com.delarosa.recognition;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import static java.lang.Math.abs;

/**
 * Face tracker for each detected individual. This maintains a face graphic within the app's
 * associated face overlay.
 */
public class GraphicFaceTracker extends Tracker<Face> {
    private static GraphicFaceTrackerInterface graphicFaceTrackerInterface;
    private boolean canTakePicture = true;
    private CameraSource mCameraSource = null;
    private int timeToWaitNextPhoto = 3;

    GraphicFaceTracker(CameraSource mCameraSource) {
        this.mCameraSource = mCameraSource;

    }

    GraphicFaceTracker() {

    }

    /**
     * Establece la interfaz
     *
     * @param listener interfaz para el manejo de eventos con PlayerActivity
     */
    public void setListener(GraphicFaceTrackerInterface listener) {
        graphicFaceTrackerInterface = listener;
    }

    /**
     * Start tracking the detected face instance within the face overlay.
     */
    @Override
    public void onNewItem(int faceId, Face item) {

    }

    /**
     * Actualiza siempre que encuentra una cara nueva, o hay moviemiento en la cara encontrada
     * cuando obtiene el tamaño del recuadro del overlay, toma una foto de ese recuadro y lo
     * manda a los diferentes modelos para obtener las caracteriticas
     */
    @Override
    public void onUpdate(FaceDetector.Detections<Face> detectionResults, final Face face) {
        try {
            if (mCameraSource != null) {


                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        final int id = face.getId();
                        float x = face.getPosition().x + face.getWidth() / 2;
                        float y = face.getPosition().y + face.getHeight() / 2;

                        float xOffset = face.getWidth() / 3.0f;
                        float yOffset = face.getHeight() / 3.0f;
                        final float left = x - xOffset;
                        final float top = y - yOffset;
                        final float right = x + xOffset;
                        final float bottom = y + yOffset;

                        final int width = (int) abs(right - left);
                        final int height = (int) abs(top - bottom);


                        // if (canTakePicture && ((top > 0 && left > 0) && (top < height && left < width))) {
                        if (canTakePicture && ((top > 0 && left > 0))) {

                            try {
                                canTakePicture = false;
                                mCameraSource.takePicture(null, new CameraSource.PictureCallback() {

                                    @Override
                                    public void onPictureTaken(byte[] bytes) {

                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        if (bitmap != null && !bitmap.equals("")) {
                                            Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, (int) left, (int) top, width, height);
                                            graphicFaceTrackerInterface.getData(croppedBitmap, bitmap, id);
                                        }
                                    }
                                });

                                new CountDownTimer(timeToWaitNextPhoto * 1000, 3000) {

                                    public void onTick(long millisUntilFinished) {
                                    }

                                    public void onFinish() {
                                        canTakePicture = true;
                                    }
                                }.start();


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                });


            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Hide the graphic when the corresponding face was not detected.  This can happen for
     * intermediate frames temporarily (e.g., if the face was momentarily blocked from
     * view).
     */
    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults) {

    }

    /**
     * Called when the face is assumed to be gone for good. Remove the graphic annotation from
     * the overlay.
     */
    @Override
    public void onDone() {

    }
}
