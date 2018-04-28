package com.popov.poct.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by popov on 24.07.2016.
 */
public class Answer {
    @SerializedName("NUMBER")
    private int number;
    @SerializedName("TEXT")
    private String text;
    @SerializedName("VALUE")
    private String value;

    public Answer(){

    }
    public Answer(int number, String text, String value) {
        this.number = number;
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
