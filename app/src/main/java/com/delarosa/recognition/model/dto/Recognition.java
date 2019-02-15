package com.delarosa.recognition.model.dto;

import java.io.Serializable;

/**
 * esta clase es un DTO (Data Transfer Object) para el envio de los parametros a arkbox
 */
public class Recognition implements Serializable {
    private String type;
    private String sex;
    private String age;
    private String emotion;

    public Recognition(String type, String sex, String age, String emotion) {
        this.type = type;
        this.sex = sex;
        this.age = age;
        this.emotion = emotion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }
}
