package com.popov.poct.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by popov on 29.07.2016.
 */
public class Theme {
    // "NUMBER":1,"NAME":"Сложение и вычитание","INFO":"","ISDELETED":false,"DELETEDATE":null,
    @SerializedName("NUMBER")
    private int number = 1;
    @SerializedName("NAME")
    private String name;
    @SerializedName("INFO")
    private String info = "";
    @SerializedName("ISDELETED")
    private boolean isDeleted = false;
    @SerializedName("DELETEDATE")
    private String deleteDate;
    @SerializedName("QUESTIONS")
    private List<Question> questions = new ArrayList<Question>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
