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

package com.popov.poct;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.popov.poct.model.Answer;
import com.popov.poct.model.Question;
import com.popov.poct.model.Test;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import org.jsoup.Jsoup;

import static com.popov.poct.cloud.Cloud.errorReport;

/**
 * Created by popov on 27.07.2016.
 */
public class TestLoader {
    public void loadTest(String jsonString, Settings settings, boolean isFullScreen) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
        Test test = null;
        try {
            test = gson.fromJson(jsonString, Test.class);
        } catch (Exception e) {
            errorReport(e, jsonString, settings);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Ошибка при загркузке теста");
            alert.setContentText("Произошла ошибка при загркузке теста");
            alert.getDialogPane().setExpandableContent(new TextArea(e.getMessage()));
            alert.showAndWait();
        }

        for (Question question : test.getThemes().get(0).getQuestions()) {

            for (Answer answer : question.getAnswers()) {
                try {
                    answer.setText(Jsoup.parse(answer.getText().replace("amp;", "").replace("\\n", "")).text());
                } catch (NullPointerException npe) {
                }

                try {
                    answer.setValue(Jsoup.parse(answer.getValue().replace("amp;", "").replace("\\n", "")).text());
                } catch (NullPointerException npe) {
                }
            }
        }
        CreatorView.creatorView(test.getName(), test.getThemes().get(0).getQuestions().size(), settings, true, false, test, isFullScreen, settings.getScanAlert(), settings.getRbsList());
    }
}
