package com.delarosa.recognition.model.assetsModel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import com.delarosa.recognition.model.Classifiers.AgeClassifier;
import com.delarosa.recognition.model.interfaces.ClassifierAge;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class AgeModel {

    //region type model
    public static final String MODEL_FILE = "frozen_age_graph.pb";
    public static final String MODEL_LABELS = "labelAge.txt";
    public static final int IMAGE_INPUT_SIZE = 227;

    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NODE = "input";
    private static final String OUTPUT_NODE = "output/output";

    //endregion
    private Context context;
    private Executor executor = Executors.newSingleThreadExecutor();
    private ClassifierAge classifierAge;

    public AgeModel(Context context) {
        this.context = context;
        initTensorFlowAndLoadModel();

    }

    /**
     * inicializa los modelos de TensorFlow
     */
    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifierAge = AgeClassifier.create(context.getAssets(), MODEL_FILE, MODEL_LABELS, IMAGE_INPUT_SIZE, IMAGE_MEAN,
                            IMAGE_STD,
                            INPUT_NODE,
                            OUTPUT_NODE);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });

    }

    public String detectAge(Bitmap bitmap) {
        List<ClassifierAge.Recognition> results = null;
        try {
            Bitmap image = bitmap;
            Bitmap grayImage = toGrayScale(image);
            Bitmap resizedImage = getResizedBitmap(image, IMAGE_INPUT_SIZE, IMAGE_INPUT_SIZE);
            results = classifierAge.recognizeImage(resizedImage);

        } catch (Exception e) {
            System.out.print("Exception:" + e.toString());
            e.printStackTrace();
        }
        return results.get(0).toString();

    }

    /**
     * convierte a una escala de grises
     *
     * @param bmpOriginal
     * @return
     */
    private Bitmap toGrayScale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayScale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayScale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayScale;
    }


    private Bitmap getResizedBitmap(Bitmap image, int bitmapWidth, int bitmapHeight) {
        return Bitmap.createScaledBitmap(image, bitmapWidth, bitmapHeight, true);
    }


}
