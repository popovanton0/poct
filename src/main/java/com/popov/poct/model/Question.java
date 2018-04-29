/*
 * Copyright 2016 Anton Popov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
