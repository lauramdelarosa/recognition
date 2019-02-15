package com.delarosa.recognition.model.dto;

/**
 * Dto que utiliza EmotionModel para funcionar
 */

public class ClassificationEmotion {

    private float conf;
    private String label;

    public ClassificationEmotion() {
        this.conf = -1.0F;
        this.label = null;
    }

    public void update(float conf, String label) {
        this.conf = conf;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public float getConf() {
        return conf;
    }
}
