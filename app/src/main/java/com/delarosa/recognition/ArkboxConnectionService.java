package com.delarosa.recognition;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.delarosa.recognition.model.assetsModel.AgeModel;
import com.delarosa.recognition.model.assetsModel.EmotionModel;
import com.delarosa.recognition.model.assetsModel.GenderModel;
import com.delarosa.recognition.model.assetsModel.ObjectModel;
import com.delarosa.recognition.model.dto.Recognition;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.IOException;

import androidx.core.app.ActivityCompat;

public class ArkboxConnectionService extends Service implements GraphicFaceTrackerInterface {
    /**
     * por medio de esta variable llegan los datos del beat
     */
    public static final int DATA_MODELS = 1;
    //conecta con watcherService
    public static final int ARKBOX_RECOGNITION = 8;
    private static final String TAG = "FaceTracker";
    private static final int RC_HANDLE_GMS = 9001;
    final Messenger recognitionMessenger = new Messenger(new IncomingHandler(this));
    GraphicFaceTracker graphicFaceTracker;
    /**
     * Messenger para comunicacion con ArkboxConnectionService.
     */
    private Messenger recognitionServiceMessenger = null;
    /**
     * Indica si ArkboxConnectionService ya esta vinculado.
     */
    private boolean recognitionServiceBound;
    /**
     * Interaccion con interfaz de ArkboxConnectionService.
     */
    private final ServiceConnection recognitionServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            recognitionServiceMessenger = new Messenger(service);
            recognitionServiceBound = true;
            Log.d("ArkboxConnectionService", "seConectooooooooooo");
        }

        public void onServiceDisconnected(ComponentName className) {
            recognitionServiceMessenger = null;
            recognitionServiceBound = false;
        }
    };
    private CameraSource mCameraSource = null;
    private String objectDetected;
    private String genderDetected;
    private String ageDetected;
    private String emotionDetected;
    private ObjectModel objectModel;
    private EmotionModel emotionModel;
    private GenderModel genderModel;
    private AgeModel ageModel;

    /**
     * conecta el servicio con el que lo necesite y obtiene una instancia de la conexion.
     *
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        return recognitionMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (!recognitionServiceBound) {
            bindService();
        }
        objectModel = new ObjectModel(this);
        emotionModel = new EmotionModel(this);
        genderModel = new GenderModel(this);
        ageModel = new AgeModel(this);

        graphicFaceTracker = new GraphicFaceTracker();
        graphicFaceTracker.setListener(this);

        createCameraSource();
        Log.d("ArkboxConnectionService", "onCreate");
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {


        GraphicFaceTrackerFactory graphicFaceTrackerFactory = new GraphicFaceTrackerFactory();

        final FaceDetector detector = new FaceDetector.Builder(this).setClassificationType(FaceDetector.ALL_CLASSIFICATIONS).build();
        detector.setProcessor(new MultiProcessor.Builder<>(graphicFaceTrackerFactory).build());
        // detector.setProcessor(new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory()).build());

        if (!detector.isOperational()) {
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(this, detector).setFacing(CameraSource.CAMERA_FACING_FRONT).build();
        graphicFaceTrackerFactory.setCameraSource(mCameraSource);
        startCameraSource();
    }

    /**
     * Intenta vincular el servicio expuesto por esta app.
     * se realiza la comunicacion entre aplicaciones
     */
    private void bindService() {
        try {
            Intent intent = new Intent("ArkboxWatcherService");
            intent.setPackage("co.arkbox");
            bindService(intent, recognitionServiceConnection, BIND_AUTO_CREATE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendDataToArkbox(String jsonObjectRecognition) {

        Bundle bundle = new Bundle();
        bundle.putString("recognition", jsonObjectRecognition);
        android.os.Message msg = android.os.Message.obtain(null, ArkboxConnectionService.ARKBOX_RECOGNITION, 0, 0);
        msg.setData(bundle);

        try {
            if (recognitionServiceBound) {
                recognitionServiceMessenger.send(msg);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (code != ConnectionResult.SUCCESS) {
            //  Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS).show();
        }

        if (mCameraSource != null) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mCameraSource.start();
                //mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }


        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
        Log.d("ArkboxConnectionService", "onDestroy");
    }

    /**
     * identifica todas las variables ( tipo de objetivo,sexo, edad, emocion)
     *
     * @param croppedBitmap  recuadro de la cara seleccionada
     * @param completeBitmap foto completa para identificar objetos
     */
    @Override
    public void getData(Bitmap croppedBitmap, Bitmap completeBitmap, int id) {
        try {

            objectDetected = objectModel.detectObject(getScaleBitmap(completeBitmap, objectModel.INPUT_SIZE_TYPE));
            emotionDetected = emotionModel.detectEmotion(getScaleBitmap(croppedBitmap, emotionModel.INPUT_SIZE_TYPE));
            genderDetected = genderModel.detectGender(getScaleBitmap(croppedBitmap, genderModel.INPUT_SIZE_TYPE));
            ageDetected = ageModel.detectAge(getScaleBitmap(croppedBitmap, ageModel.IMAGE_INPUT_SIZE));
            final String message = String.valueOf("ID: " + id + "\n" + objectDetected + "\n" + emotionDetected + "\n" + genderDetected + "\n" + ageDetected);


            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(ArkboxConnectionService.this, message, Toast.LENGTH_LONG).show();
                }
            });

            Log.i("messageModels", message);
            Recognition recognition = new Recognition(objectDetected, genderDetected, ageDetected, emotionDetected);
            Gson gson = new GsonBuilder().create();
            JSONObject data = new JSONObject(gson.toJson(recognition));
            String jsonData = data.toString();


            sendDataToArkbox(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * crea un bitmap escalado con las dimensiones requeridas
     *
     * @param bitmap    a escalar
     * @param inputSize dimesion qa la que quiero escalar
     * @return
     */
    private Bitmap getScaleBitmap(Bitmap bitmap, int inputSize) {
        return Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false);
    }


    class IncomingHandler extends Handler {
        Context context;

        public IncomingHandler(Context context) {
            this.context = context;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DATA_MODELS:

                    Bundle dataRecognition = msg.getData();
                    String happy = dataRecognition.getString("recognition");
                    sendDataToArkbox(happy);
                    break;

                default:
                    super.handleMessage(msg);

            }
        }

    }
}
