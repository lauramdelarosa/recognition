package com.delarosa.recognition.model.assetsModel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import com.delarosa.recognition.model.Classifiers.GenderClassifier;
import com.delarosa.recognition.model.interfaces.ClassifierGender;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class GenderModel {

    //region type model
    public static final String TYPE_MODEL = "genderModel.pb";
    public static final String LABEL_TYPE = "labelGender.txt";
    public static final int INPUT_SIZE_TYPE = 100;

    private static final int IMAGE_MEAN = 114;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input_input";
    private static final String OUTPUT_NAME = "output/Softmax";

    //endregion
    private Context context;
    private Executor executor = Executors.newSingleThreadExecutor();
    private ClassifierGender classifierGender;

    public GenderModel(Context context) {
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
                    classifierGender = GenderClassifier.create(context.getAssets(), TYPE_MODEL, LABEL_TYPE, INPUT_SIZE_TYPE, IMAGE_MEAN,
                            IMAGE_STD,
                            INPUT_NAME,
                            OUTPUT_NAME);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });

    }

    public String detectGender(Bitmap bitmap) {
        List<ClassifierGender.Recognition> results = null;
        try {
            Bitmap image = bitmap;
            Bitmap grayImage = toGrayScale(image);
            Bitmap resizedImage = getResizedBitmap(grayImage, INPUT_SIZE_TYPE, INPUT_SIZE_TYPE);
            results = classifierGender.recognizeImage(resizedImage);

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
