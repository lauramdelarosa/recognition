package com.delarosa.recognition.model.assetsModel;

import android.content.Context;
import android.graphics.Bitmap;

import com.delarosa.recognition.model.Classifiers.ObjectClassifier;
import com.delarosa.recognition.model.interfaces.ClassifierObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * Esta clase se encarga de retornar el valor del tipo de objeto
 */
public class ObjectModel {
    //region type model
    public static final String TYPE_MODEL = "typeModel.tflite";
    public static final String LABEL_TYPE = "labelObject.txt";
    public static final int INPUT_SIZE_TYPE = 224;
    List<ClassifierObject.Recognition> results = new ArrayList<>();
    //endregion
    private Context context;
    private Executor executor = Executors.newSingleThreadExecutor();
    private ClassifierObject classifierObject;

    public ObjectModel(Context context) {
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
                    classifierObject = ObjectClassifier.create(context.getAssets(), TYPE_MODEL, LABEL_TYPE, INPUT_SIZE_TYPE);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    public String detectObject(Bitmap bitmap) {

        //envia la info a la interfaz
        results = classifierObject.recognizeImage(bitmap);

        // Toast.makeText(context, results.toString(), Toast.LENGTH_LONG).show();
        return String.valueOf(results.toString());
    }


    private void close() {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifierObject.close();
            }
        });
    }

}
