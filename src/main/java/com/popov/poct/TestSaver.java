package com.popov.poct;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.popov.poct.cloud.Cloud;
import com.popov.poct.model.Test;
import javafx.scene.control.Alert;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import static com.popov.poct.cloud.Cloud.errorReport;


/**
 * Created by popov on 27.07.2016.
 */
public class TestSaver {
    private Test test;

    public TestSaver(Test test) {
        this.test = test;
    }

    public String saveTest(long seconds, boolean isTextToTest, boolean isLoad, Settings settings) {

        test.setInfo("Тест создан с помощью программы " + settings.getAPP_NAME());


        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
        String json = gson.toJson(test);
        try {
            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(
                                    settings.getPathToROSTFolder().getAbsolutePath() + System.getProperty("file.separator") + test.getName() + ".rost"
                            ), "UTF-8"
                    )
            );
            bw.write(json);
            bw.close();
            try {
                Cloud.testCreated(test.getThemes().get(0).getQuestions().size(), seconds, isTextToTest, isLoad);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create().toJson(test);

        } catch (Exception e) {
            errorReport(e, json, settings);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Возникла ошибка при сохранении теста");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            System.exit(1);
            return "";
        }
    }
}
