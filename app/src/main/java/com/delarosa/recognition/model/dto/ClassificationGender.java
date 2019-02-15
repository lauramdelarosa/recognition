package com.delarosa.recognition.model.dto;

/**
 * Dto que utiliza GenderModel para funcionar
 */


public class ClassificationGender {

    private float conf;
    private String label;

    public ClassificationGender() {
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
