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
