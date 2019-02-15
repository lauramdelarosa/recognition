package com.delarosa.recognition.model.Classifiers;


//Provides access to an application's raw asset files;

import android.content.res.AssetManager;

import com.delarosa.recognition.model.dto.ClassificationEmotion;
import com.delarosa.recognition.model.interfaces.ClassifierEmotion;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * tensorFlow Completo
 */
public class EmotionClassifier implements ClassifierEmotion {


    private static final float THRESHOLD = 0.1f;

    private TensorFlowInferenceInterface tfHelper;

    private String name;
    private String inputName;
    private String outputName;
    private int inputSize;
    private boolean feedKeepProb;

    private List<String> labels;
    private float[] output;
    private String[] outputNames;


    public static EmotionClassifier create(AssetManager assetManager, String name, String modelPath, String labelFile, int inputSize, String inputName, String outputName, boolean feedKeepProb, int numClasses) throws IOException {
        //intialize a classifier
        EmotionClassifier classifier = new EmotionClassifier();

        //store its name, input and output labels
        classifier.name = name;

        classifier.inputName = inputName;
        classifier.outputName = outputName;

        //read labels for label file
        classifier.labels = loadLabelList(assetManager, labelFile);

        //set its model path and where the raw asset files are
        //classifier.tfHelper = new TensorFlowInferenceInterface(assetManager, modelPath);
        classifier.tfHelper = loadModelFile(assetManager, modelPath);

        //how big is the input?
        classifier.inputSize = inputSize;

        // Pre-allocate buffer.
        classifier.outputNames = new String[]{outputName};

        classifier.outputName = outputName;
        classifier.output = new float[numClasses];

        classifier.feedKeepProb = feedKeepProb;

        return classifier;
    }

    //given a saved drawn model, lets read all the classification labels that are
    //stored and write them to our in memory labels list
    private static List<String> loadLabelList(AssetManager am, String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(am.open(fileName)));

        String line;
        List<String> labels = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            labels.add(line);
        }

        br.close();
        return labels;
    }

    private static TensorFlowInferenceInterface loadModelFile(AssetManager assetManager, String modelPath) throws IOException {

        return new TensorFlowInferenceInterface(assetManager, modelPath);
        //AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        //FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        //FileChannel fileChannel = inputStream.getChannel();
        //long startOffset = fileDescriptor.getStartOffset();
        //long declaredLength = fileDescriptor.getDeclaredLength();
        //return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public ClassificationEmotion recognize(final float[] pixels) {

        //using the interface
        //give it the input name, raw pixels from the drawing,
        //input size
        tfHelper.feed(inputName, pixels, 1, inputSize, inputSize, 1);

        //probabilities
        if (feedKeepProb) {
            tfHelper.feed("keep_prob", new float[]{1});
        }
        //get the possible outputs
        tfHelper.run(outputNames);

        //get the output
        tfHelper.fetch(outputName, output);

        // Find the best classification
        //for each output prediction
        //if its above the threshold for accuracy we predefined
        //write it out to the view
        ClassificationEmotion ans = new ClassificationEmotion();
        for (int i = 0; i < output.length; ++i) {
            System.out.println(output[i]);
            System.out.println(labels.get(i));
            if (output[i] > THRESHOLD && output[i] > ans.getConf()) {
                ans.update(output[i], labels.get(i));
            }
        }

        return ans;
    }
}
