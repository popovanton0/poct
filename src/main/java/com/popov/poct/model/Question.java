package com.popov.poct.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by popov on 24.07.2016.
 */
public class Question {
    @SerializedName("DELETEDATE")
    private String deleteDate = null;
    @SerializedName("NUMBER")
    private int number;
    @SerializedName("COMMENT")
    private String comment = "";
    @SerializedName("ANSWERTYPE")
    private String answerType;
    @SerializedName("TEXT")
    private String text;
    @SerializedName("NAME")
    private String name;
    @SerializedName("DIFFICULTID")
    private int difficultId;
    @SerializedName("ISREGISTRSENSITIVENESS")
    private boolean isRegisterSense;
    @SerializedName("ISSPACESENSITIVENESS")
    private boolean isSpaceSense;
    @SerializedName("ISDELETED")
    private boolean isDeleted;
    @SerializedName("ISREGEXPR")
    private boolean isRegexp;
    @SerializedName("ANSWERS")
    private List<Answer> answers = new ArrayList<Answer>();

    public Question(){

    }

    public String getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(String deleteDate) {
        this.deleteDate = deleteDate;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAnswerType() {
        return answerType;
    }

    public void setAnswerType(String answerType) {this.answerType = answerType;}

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        this.name = text;
    }

    public int getDifficultId() {
        return difficultId;
    }

    public void setDifficultId(int difficultId) {
        this.difficultId = difficultId;
    }

    public boolean isRegisterSense() {
        return isRegisterSense;
    }

    public void setRegisterSense(boolean registerSense) {
        isRegisterSense = registerSense;
    }

    public boolean isSpaceSense() {
        return isSpaceSense;
    }

    public void setSpaceSense(boolean spaceSense) {
        isSpaceSense = spaceSense;
    }

    public boolean isdeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        this.isDeleted = deleted;
    }

    public boolean isRegexp() {
        return isRegexp;
    }

    public void setRegexp(boolean regexp) {
        isRegexp = regexp;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }
}
