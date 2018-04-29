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
