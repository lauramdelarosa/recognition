package com.delarosa.recognition.model.assetsModel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

import com.delarosa.recognition.model.Classifiers.EmotionClassifier;
import com.delarosa.recognition.model.dto.ClassificationEmotion;
import com.delarosa.recognition.model.interfaces.ClassifierEmotion;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class EmotionModel {

    //region type model
    public static final String TYPE_MODEL = "emotionModel.pb";
    public static final String LABEL_TYPE = "labelEmotionEnglish.txt";
    public static final int INPUT_SIZE_TYPE = 48;
    //endregion
    private Context context;
    private Executor executor = Executors.newSingleThreadExecutor();
    private ClassifierEmotion classifierEmotion;

    public EmotionModel(Context context) {
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
                    classifierEmotion = EmotionClassifier.create(context.getAssets(), "CNN", TYPE_MODEL, LABEL_TYPE, INPUT_SIZE_TYPE, "input", "output_50", true, 7);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });

    }

    public String detectEmotion(Bitmap bitmap) {

        Bitmap image = bitmap;
        Bitmap grayImage = toGrayScale(image);
        Bitmap resizedImage = getResizedBitmap(grayImage, INPUT_SIZE_TYPE, INPUT_SIZE_TYPE);
        int pixelArray[];

        //Initialize the intArray with the same size as the number of pixels on the image
        pixelArray = new int[resizedImage.getWidth() * resizedImage.getHeight()];

        //copy pixel data from the Bitmap into the 'intArray' array
        resizedImage.getPixels(pixelArray, 0, resizedImage.getWidth(), 0, 0, resizedImage.getWidth(), resizedImage.getHeight());


        float normalizedPixels[] = new float[pixelArray.length];
        for (int i = 0; i < pixelArray.length; i++) {
            // 0 for white and 255 for black
            int pix = pixelArray[i];
            int b = pix & 0xff;
            //  normalized_pixels[i] = (float)((0xff - b)/255.0);
            // normalized_pixels[i] = (float)(b/255.0);
            normalizedPixels[i] = (float) (b);

        }
        System.out.println(normalizedPixels);
        Log.d("pixel_values", String.valueOf(normalizedPixels));
        String text = null;

        try {
            final ClassificationEmotion res = classifierEmotion.recognize(normalizedPixels);
            //if it can't classify, output a question mark
            if (res.getLabel() == null) {
                text = "Status: " + ": ?\n";
            } else {
                //else output its name
                //text = String.format("%s: %s, %f\n", "Status: ", res.getLabel(), res.getConf());
                text = String.format(res.getLabel());
            }
        } catch (Exception e) {
            System.out.print("Exception:" + e.toString());
            e.printStackTrace();
        }

        //  Toast.makeText(context, text, Toast.LENGTH_LONG).show();
        return text;

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
