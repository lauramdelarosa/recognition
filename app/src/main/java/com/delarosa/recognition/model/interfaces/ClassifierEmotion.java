package com.delarosa.recognition.model.interfaces;


import com.delarosa.recognition.model.dto.ClassificationEmotion;

/**
 * interfaz pública para el clasificador
 * expone su nombre y la función de reconocimiento
 * que dio algunos píxeles dibujados como entrada
 * clasifica lo que ve como una imagen MNIST
 */

public interface ClassifierEmotion {
    String name();

    ClassificationEmotion recognize(final float[] pixels);
}
