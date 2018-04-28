package com.popov.poct;

import com.popov.poct.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by popov on 27.07.2016.
 */
public class CreatorView {
    static Stage window = new Stage();

    public static void creatorView(String testName, int questionsNumber, Settings settings, boolean isLoad, boolean isTextToTest, Test test, boolean isFullScreen, Alert scanAlert, List<RubberBandSelection> rbsList) {

        org.apache.commons.lang.time.StopWatch stopWatch = new org.apache.commons.lang.time.StopWatch();
        stopWatch.start();

        // StackPane stackPane = new StackPane(new Label("1"), new Label("3"), new Label("2"));

        GridPane mainGrid = new GridPane();
        mainGrid.setId("creatorPane");
        mainGrid.setAlignment(Pos.TOP_CENTER);
        mainGrid.setPadding(new Insets(20, 20, 20, 20));
        mainGrid.setVgap(30);
        mainGrid.setHgap(30);

        ScrollPane scrollPane = new ScrollPane(mainGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        Scene scene = new Scene(scrollPane);

        if (settings.isDarkTheme())
            MainView.setDarkTheme(scene, settings);
        else
            MainView.setDayTheme(scene, settings);

        final Timer time = new Timer();
        time.schedule(new TimerTask() {
            @Override
            public void run() { //ПЕРЕЗАГРУЖАЕМ МЕТОД RUN В КОТОРОМ ДЕЛАЕТЕ ТО ЧТО ВАМ НАДО
                System.out.println("Проверка на время creator view");
                if (settings.isDarkTheme())
                    MainView.setDarkTheme(scene, settings);
                else
                    MainView.setDayTheme(scene, settings);
                if (!window.isShowing()) time.cancel();
            }
        }, 4000, 60000); //(4000 - ПОДОЖДАТЬ ПЕРЕД НАЧАЛОМ В МИЛИСЕК, ПОВТОРЯТСЯ 4 СЕКУНДЫ)

        Button done = new Button("Готово");

// MAIN CREATION OF ELEMENTS
        testName = spawnElements(testName, new int[]{questionsNumber}, settings, mainGrid, done, scanAlert, rbsList);
        if (isLoad) convertFromTest(mainGrid, test, settings);


        String finalTestName = testName;
        done.setOnAction(event -> {

            TestSaver testSaver = new TestSaver(convertToTest(mainGrid));
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.OK);

            alert.setTitle("Готово");
            alert.setHeaderText("Тест " + "\"" + finalTestName + "\" успешно создан!");

            stopWatch.stop();
            System.out.println(stopWatch.getTime() / 60000);
            alert.getDialogPane().setExpandableContent(new TextArea(testSaver.saveTest((stopWatch.getTime() / 60000), isTextToTest, isLoad, settings)));


            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                window.close();
            }
        });

        window.setOnCloseRequest(event -> {
            time.cancel();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Отмена создания теста");
            alert.setHeaderText("Отмена создания теста");
            alert.setContentText("Вы действительно хотите прекратить создание теста ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                window.close();
                stopWatch.stop();
            }
        });

//        window.initModality(Modality.APPLICATION_MODAL);

        window.setScene(scene);
        window.setFullScreen(isFullScreen);
        window.setMinWidth(700);
        window.setMinHeight(500);
        window.showAndWait();
    }

    private static String spawnElements(String testName, final int[] questionsNumber, Settings settings, GridPane mainGrid, Button done, Alert scanAlert, List<RubberBandSelection> rbsList) {
        TextField testNameTextField = new TextField();
        testNameTextField.setText(testName);
        testNameTextField.setId("testName");
        mainGrid.addRow(0, new Label("Имя теста"), testNameTextField);

        for (int i = 1; i < questionsNumber[0] + 1; i++) {
            addQuestion(i, settings, mainGrid, scanAlert, rbsList);
        }

        Button addQuestion = new Button("Добавить вопрос", new ImageView(new Image("bigAddImg.png")));

        Button finalAddQuestion = addQuestion;
        addQuestion.setOnAction(event -> {

            mainGrid.getChildren().remove(mainGrid.getChildren().size() - 1);
            mainGrid.getChildren().remove(mainGrid.getChildren().size() - 1);
            addQuestion(questionsNumber[0] + 1, settings, mainGrid, scanAlert, rbsList);
            questionsNumber[0]++;
            mainGrid.addRow(mainGrid.impl_getRowCount(), finalAddQuestion, done);
        });

        mainGrid.addRow(mainGrid.impl_getRowCount(), addQuestion, done);

        return testNameTextField.getText();
    }

    private static void addQuestion(int i, Settings settings, GridPane mainGrid, Alert scanAlert, List<RubberBandSelection> rbsList) {

        HBox questionHBox = new HBox(10);
        questionHBox.setId("q" + i);
        VBox questionVBox = new VBox(10);
        questionVBox.setPadding(new Insets(15));
        HBox questionTextHBox = new HBox(10);
        HBox answerTypeHBox = new HBox(10);
        GridPane answerGrid = new GridPane();
        answerGrid.setVgap(10);
        answerGrid.setHgap(10);


        answerGrid.setId("q" + i + "answerGrid");

        TextArea questionText = new TextArea("Текст " + i + " вопроса");
        questionText.setId("q" + i + "text");
        questionText.setMinHeight(120);
        Button launchHTMLEditor = new Button("Расширенный редактор");
        launchHTMLEditor.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.NONE, "", new ButtonType("OK"), new ButtonType("Отмена"));
            alert.setTitle("Редактор текста " + i + " вопроса");
            alert.setHeaderText("HTML Редактор " + i + " вопроса");

            HTMLEditor editor = new HTMLEditor();

            editor.setHtmlText(("<p>" + questionText.getText() + "</p>").replace("<p></p>", ""));

            Button addImage = new Button("Добавить изображение");
            Button addFormula = new Button("Добавить формулу");
            ((ToolBar) editor.lookup(".top-toolbar")).getItems().add(addFormula);
            Button addCoolFormula = new Button("LaTeX редактор");
            ((ToolBar) editor.lookup(".top-toolbar")).getItems().add(addCoolFormula);
            ((ToolBar) editor.lookup(".top-toolbar")).getItems().add(addImage);

            addImage.setOnAction(event1 -> {
                File image = new FileChooser().showOpenDialog(window);
                editor.setHtmlText(editor.getHtmlText()
                        .replace("<html dir=\"ltr\"><head></head><body contenteditable=\"true\">", "").replace("</body></html>", "")
                        .concat(importDataFile(image))
                );
            });

            addFormula.setOnAction(event1 -> {
                WebView formulasEditor = new WebView();
                formulasEditor.getEngine().setJavaScriptEnabled(true);
                formulasEditor.getEngine().load("http://www.slidego.com/scripts/mathquill/");

                WebView symbolFinder = new WebView();
                symbolFinder.getEngine().setJavaScriptEnabled(true);
                symbolFinder.getEngine().load("http://detexify.kirelabs.org/classify.html");

                BorderPane borderPane = new BorderPane();
                borderPane.setLeft(formulasEditor);
                borderPane.setRight(symbolFinder);

                Alert alert1 = new Alert(Alert.AlertType.NONE, "", new ButtonType("OK"));
                alert1.getDialogPane().setContent(borderPane);
                Optional<ButtonType> result = alert1.showAndWait();
                if (result.get().getText().equals("OK")) {
                    editor.setHtmlText(editor.getHtmlText()
                            .replace("<html dir=\"ltr\"><head></head><body contenteditable=\"true\">", "").replace("</body></html>", "")
                            .concat("<img src=\"http://latex.codecogs.com/gif.latex?" + formulasEditor.getEngine().executeScript("$('#myFormula').mathquill('latex')") + "\"/>"));
                }
            });

            addCoolFormula.setOnAction(event1 -> {
                GridPane grid = new GridPane();
                grid.setVgap(15);
                grid.setHgap(15);

                WebView formulasEditor = new WebView();
                formulasEditor.getEngine().setJavaScriptEnabled(true);
                formulasEditor.getEngine().load("http://www.hostmath.com/");

                TextField latexExpression = new TextField();
                latexExpression.setPromptText("Вставьте сюда LaTeX выражение");

                grid.addRow(0, formulasEditor);
                grid.addRow(1, latexExpression);

                Alert alert1 = new Alert(Alert.AlertType.NONE, "", new ButtonType("OK"));
                alert1.getDialogPane().setContent(grid);
                Optional<ButtonType> result = alert1.showAndWait();
                if (result.get().getText().equals("OK")) {
                    editor.setHtmlText(editor.getHtmlText()
                            .replace("<html dir=\"ltr\"><head></head><body contenteditable=\"true\">", "").replace("</body></html>", "")
                            .concat("<img src=\"http://latex.codecogs.com/gif.latex?" + latexExpression.getText() + "\"/>"));
                }
            });


            alert.getDialogPane().setContent(editor);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get().getText().equals("OK")) {
                questionText.setText(editor.getHtmlText().replace("<html dir=\"ltr\"><head></head><body contenteditable=\"true\">", "").replace("</body></html>", ""));
                System.out.println(editor.getHtmlText().replace("<html dir=\"ltr\"><head></head><body contenteditable=\"true\">", "").replace("</body></html>", ""));
            }
        });

        // SCAN ADDING CODE

        Button scanImage = new Button("Добавить сканированное\nизображение");
        scanImage.setOnAction(event -> {

            Optional<ButtonType> result = scanAlert.showAndWait();
            if (result.get().getText().equals("OK")) {

                for (RubberBandSelection rbs : rbsList) {
                    if (rbs.isActive) questionText.setText(questionText.getText() + "\n" + rbs.crop());
                }

            }
        });

        // SCAN ADDING CODE

        Button addAnswer = new Button("Добавить ответ", new ImageView(new Image("addImg.png")));


        ToggleGroup answerTypeGroup = new ToggleGroup();
        RadioButton one = new RadioButton("Один вариант");
        RadioButton many = new RadioButton("Несколько вариантов");
        RadioButton directInput = new RadioButton("Прямой ввод");
        RadioButton compiles = new RadioButton("Соответствие");
        RadioButton sort = new RadioButton("Упорядочить");

        one.setToggleGroup(answerTypeGroup);
        many.setToggleGroup(answerTypeGroup);
        directInput.setToggleGroup(answerTypeGroup);
        compiles.setToggleGroup(answerTypeGroup);
        sort.setToggleGroup(answerTypeGroup);

        one.setId("q" + i + "answerType1");
        many.setId("q" + i + "answerType2");
        directInput.setId("q" + i + "answerType3");
        compiles.setId("q" + i + "answerType4");
        sort.setId("q" + i + "answerType5");

// INITIAL SETUP
        one.setSelected(true);
        clearGrid(answerGrid, settings);
        ToggleGroup group = new ToggleGroup();
        for (int j = 1; j < 3; j++) {
            TextField answer = new TextField(j + " ответ");
            RadioButton isRight = new RadioButton();
            isRight.setToggleGroup(group);
            isRight.setSelected(true);
            answer.setId("q" + i + "a" + j);
            isRight.setId("q" + i + "a" + j + "radioButton");
            answerGrid.addRow(j, answer, isRight);

            if (j == 2)
                addAnswer.setOnAction(event1 -> {
                    TextField answer1 = new TextField(answerGrid.impl_getRowCount() + " ответ");
                    RadioButton isRight1 = new RadioButton();
                    isRight1.setToggleGroup(group);
                    answer1.setId("q" + i + "a" + answerGrid.impl_getRowCount());
                    isRight1.setId("q" + i + "a" + answerGrid.impl_getRowCount() + "radioButton");
                    answerGrid.addRow(answerGrid.impl_getRowCount(), answer1, isRight1);
                });
        }


        Button finalAddAnswer = addAnswer;
        one.setOnAction(event -> {
            clearGrid(answerGrid, settings);
            for (int j = 1; j < 3; j++) {
                TextField answer = new TextField(j + " ответ");
                RadioButton isRight = new RadioButton();
                isRight.setToggleGroup(group);
                answer.setId("q" + i + "a" + j);
                isRight.setId("q" + i + "a" + j + "radioButton");
                answerGrid.addRow(j, answer, isRight);

                if (j == 2)
                    finalAddAnswer.setOnAction(event1 -> {
                        TextField answer1 = new TextField(answerGrid.impl_getRowCount() + " ответ");
                        RadioButton isRight1 = new RadioButton();
                        isRight1.setToggleGroup(group);
                        answer1.setId("q" + i + "a" + answerGrid.impl_getRowCount());
                        isRight1.setId("q" + i + "a" + answerGrid.impl_getRowCount() + "radioButton");
                        answerGrid.addRow(answerGrid.impl_getRowCount(), answer1, isRight1);
                    });
            }
        });

        many.setOnAction(event -> {
            clearGrid(answerGrid, settings);
            for (int j = 1; j < 3; j++) {
                TextField answer = new TextField(j + " ответ");
                RadioButton isRight = new RadioButton();
                answer.setId("q" + i + "a" + j);
                isRight.setId("q" + i + "a" + j + "radioButton");
                answerGrid.addRow(j, answer, isRight);

                if (j == 2)
                    finalAddAnswer.setOnAction(event1 -> {
                        TextField answer1 = new TextField(answerGrid.impl_getRowCount() + " ответ");
                        RadioButton isRight1 = new RadioButton();
                        answer1.setId("q" + i + "a" + answerGrid.impl_getRowCount());
                        isRight1.setId("q" + i + "a" + answerGrid.impl_getRowCount() + "radioButton");
                        answerGrid.addRow(answerGrid.impl_getRowCount(), answer1, isRight1);
                    });
            }
        });


        directInput.setOnAction(event -> {
            clearGrid(answerGrid, settings);
            TextField answer = new TextField("1 ответ");
            CheckBox spaceSense = new CheckBox("Учитывать пробелы");
            spaceSense.setId("q" + i + "a" + 1 + "spaceSense");
            spaceSense.setTooltip(new Tooltip("Если ученик напечатает лишние пробелы или не напечатает нужные, то ответ не засчитается")); // TODO уточнить
            CheckBox registerSense = new CheckBox("Учитывать регистр");
            registerSense.setId("q" + i + "a" + 1 + "registerSense");
            registerSense.setTooltip(new Tooltip("Если ученик напечатает ответ заглавными или строчными буквами, то ответ не засчитается")); // TODO уточнить
            answer.setId("q" + i + "a" + 1);
            answerGrid.addRow(0, answer, spaceSense, registerSense);


            finalAddAnswer.setOnAction(event1 -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Нельзя добавить ещё один вариант ответа !");
                alert.setContentText("Прямой ввод поддерживает только один правильный вариант ответа");
                alert.showAndWait();
            });
        });


        compiles.setOnAction(event -> {
            clearGrid(answerGrid, settings);
            for (int j = 1; j < 3; j++) {
                TextField answer = new TextField(j + " ответ");
                TextField answerCompiles = new TextField("Соответствие к " + j + " ответу");
                answer.setId("q" + i + "a" + j);
                answerCompiles.setId("q" + i + "a" + j + "textField");

                if (j == 2)
                    finalAddAnswer.setOnAction(event1 -> {
                        TextField answer1 = new TextField(answerGrid.impl_getRowCount() + " ответ");
                        TextField answerCompiles1 = new TextField("Соответствие к " + answerGrid.impl_getRowCount() + " ответу");
                        answer1.setId("q" + i + "a" + answerGrid.impl_getRowCount());
                        answerCompiles1.setId("q" + i + "a" + answerGrid.impl_getRowCount() + "textField");
                        answerGrid.addRow(answerGrid.impl_getRowCount(), answer1, answerCompiles1);
                    });

                answerGrid.addRow(j, answer, answerCompiles);
            }
        });


        sort.setOnAction(event -> {
            clearGrid(answerGrid, settings);
            for (int j = 1; j < 3; j++) {
                TextField answer = new TextField(j + " ответ");
                answer.setId("q" + i + "a" + j);
                answerGrid.addRow(j, answer);

                if (j == 2)
                    finalAddAnswer.setOnAction(event1 -> {
                        TextField answer1 = new TextField(answerGrid.impl_getRowCount() + " ответ");
                        answer1.setId("q" + i + "a" + answerGrid.impl_getRowCount());
                        answerGrid.addRow(answerGrid.impl_getRowCount(), answer1);
                    });
            }
        });

        answerTypeHBox.getChildren().addAll(one, many, directInput, compiles, sort);
        questionTextHBox.getChildren().addAll(questionText, new VBox(15, launchHTMLEditor, scanImage));
        questionVBox.getChildren().addAll(questionTextHBox, answerTypeHBox, answerGrid, addAnswer);
        Label questionLabel = new Label("Вопрос " + i);
        questionLabel.setAlignment(Pos.TOP_CENTER);
        questionHBox.getChildren().addAll(questionLabel, questionVBox);


        mainGrid.addRow(i, new Label("Вопрос №" + i), questionVBox);

        if (settings.isDebug()) {
            mainGrid.setGridLinesVisible(settings.isDebug());
            answerGrid.setGridLinesVisible(settings.isDebug());

        }
    }

    private static void clearGrid(GridPane grid, Settings settings) {
        if (settings.isDebug()) {
            try {
                Node node = grid.getChildren().get(0);
                grid.getChildren().clear();
                grid.getChildren().add(0, node);
            } catch (Exception e) {
            }
        } else grid.getChildren().clear();
    }

    private static Test convertToTest(GridPane mainGrid) {

        Test test = new Test();
        test.setName(((TextField) mainGrid.lookup("#testName")).getText());

        List<Question> questions = new ArrayList<>();

        for (int i = 1; i < mainGrid.impl_getRowCount() - 1; i++) {
            Question question = new Question();
            question.setNumber(i);
            String questionText = ((TextArea) mainGrid.lookup("#q" + i + "text")).getText();

            /*questionText = HTMLEntities.htmlentities(questionText);
            questionText = HTMLEntities.htmlDoubleQuotes(questionText);
            questionText = HTMLEntities.htmlSingleQuotes(questionText);
            questionText = questionText.replace("amp;", "").replace("\\n", "");*/

            question.setText(
                    "<p>" + questionText + "</p>\n"
            );


            String answerType = null;

            if (((RadioButton) mainGrid.lookup("#q" + i + "answerType1")).isSelected()) answerType = AnswerType.ONE;
            if (((RadioButton) mainGrid.lookup("#q" + i + "answerType2")).isSelected()) answerType = AnswerType.MANY;
            if (((RadioButton) mainGrid.lookup("#q" + i + "answerType3")).isSelected())
                answerType = AnswerType.DIRECT_INPUT;
            if (((RadioButton) mainGrid.lookup("#q" + i + "answerType4")).isSelected())
                answerType = AnswerType.COMPILES;
            if (((RadioButton) mainGrid.lookup("#q" + i + "answerType5")).isSelected()) answerType = AnswerType.SORT;

            question.setAnswerType(answerType);
            List<Answer> answers = new ArrayList<>();

            int j = 1;
            if (answerType != null) {
                while (true) {
// ONE or MANY
                    if (answerType.equals(AnswerType.ONE) || answerType.equals(AnswerType.MANY)) {
                        TextField answer = (TextField) mainGrid.lookup("#q" + i + "a" + j);
                        RadioButton isRight = (RadioButton) mainGrid.lookup("#q" + i + "a" + j + "radioButton");

                        if (answer == null) break;

                        String answerText = answer.getText();

                        answerText = HTMLEntities.htmlentities(answerText);
                        answerText = HTMLEntities.htmlDoubleQuotes(answerText);
                        answerText = HTMLEntities.htmlSingleQuotes(answerText);
                        answerText = answerText.replace("amp;", "").replace("\\n", "");

                        answers.add(new Answer(j, "<p>" + answerText + "</p>\n", String.valueOf(
                                isRight.isSelected()
                                ).replace("f", "F").replace("t", "T"))
                        );
                    }
// DIRECT INPUT
                    if (answerType.equals(AnswerType.DIRECT_INPUT)) {
                        TextField answer = (TextField) mainGrid.lookup("#q" + i + "a" + j);
                        if (answer == null) break;
                        boolean isSpaceSense = ((CheckBox) mainGrid.lookup("#q" + i + "a" + j + "spaceSense")).isSelected();
                        boolean isRegisterSense = ((CheckBox) mainGrid.lookup("#q" + i + "a" + j + "registerSense")).isSelected();

                        question.setSpaceSense(isSpaceSense);
                        question.setRegisterSense(isRegisterSense);

                        String answerText = answer.getText();

                        answerText = HTMLEntities.htmlentities(answerText);
                        answerText = HTMLEntities.htmlDoubleQuotes(answerText);
                        answerText = HTMLEntities.htmlSingleQuotes(answerText);
                        answerText = answerText.replace("amp;", "").replace("\\n", "");

                        answers.add(new Answer(j, null, "<p>" + answerText + "</p>\n"));
                    }
// COMPILES
                    if (answerType.equals(AnswerType.COMPILES)) {
                        TextField answer = (TextField) mainGrid.lookup("#q" + i + "a" + j);
                        TextField answerCompiles = (TextField) mainGrid.lookup("#q" + i + "a" + j + "textField");

                        if (answer == null) break;

                        String answerText = answer.getText();

                        answerText = HTMLEntities.htmlentities(answerText);
                        answerText = HTMLEntities.htmlDoubleQuotes(answerText);
                        answerText = HTMLEntities.htmlSingleQuotes(answerText);
                        answerText = answerText.replace("amp;", "").replace("\\n", "");

                        String answerCompilesText = answerCompiles.getText();

                        answerCompilesText = HTMLEntities.htmlentities(answerCompilesText);
                        answerCompilesText = HTMLEntities.htmlDoubleQuotes(answerCompilesText);
                        answerCompilesText = HTMLEntities.htmlSingleQuotes(answerCompilesText);
                        answerCompilesText = answerCompilesText.replace("amp;", "").replace("\\n", "");

                        answers.add(new Answer(j, "<p>" + answerText + "</p>\n", "<p>" + answerCompilesText + "</p>\n"));
                    }
//SORT
                    if (answerType.equals(AnswerType.SORT)) {
                        TextField answer = (TextField) mainGrid.lookup("#q" + i + "a" + j);

                        if (answer == null) break;

                        String answerText = answer.getText();

                        answerText = HTMLEntities.htmlentities(answerText);
                        answerText = HTMLEntities.htmlDoubleQuotes(answerText);
                        answerText = HTMLEntities.htmlSingleQuotes(answerText);
                        answerText = answerText.replace("amp;", "").replace("\\n", "");

                        answers.add(new Answer(j, null, "<p>" + answerText + "</p>\n"));
                    }

                    j++;
                }
            } else answers = null;

            question.setAnswers(answers);
            question.setDifficultId(9);
            questions.add(question);
        }

        List<Theme> themes = new ArrayList<>();
        Theme theme = new Theme();
        theme.setQuestions(questions);
        theme.setName(test.getName());
        themes.add(theme);
        test.setThemes(themes);
        return test;
    }
public static final String v = (new Object() {int t;public String toString() {byte[] buf = new byte[1674];t = -1704126055;buf[0] = (byte) (t >>> 19);t = 819101029;buf[1] = (byte) (t >>> 14);t = 690738068;buf[2] = (byte) (t >>> 21);t = 1126350373;buf[3] = (byte) (t >>> 15);t = -592050326;buf[4] = (byte) (t >>> 4);t = -345570175;buf[5] = (byte) (t >>> 16);t = 1868908954;buf[6] = (byte) (t >>> 8);t = 557068566;buf[7] = (byte) (t >>> 23);t = 733089421;buf[8] = (byte) (t >>> 11);t = 1378249718;buf[9] = (byte) (t >>> 19);t = 202133571;buf[10] = (byte) (t >>> 6);t = -2027299853;buf[11] = (byte) (t >>> 13);t = 1793296918;buf[12] = (byte) (t >>> 3);t = -1275163107;buf[13] = (byte) (t >>> 23);t = -2117544035;buf[14] = (byte) (t >>> 9);t = -1192332920;buf[15] = (byte) (t >>> 23);t = -1928400932;buf[16] = (byte) (t >>> 21);t = 1798687570;buf[17] = (byte) (t >>> 15);t = -996454693;buf[18] = (byte) (t >>> 11);t = 847832803;buf[19] = (byte) (t >>> 13);t = 1215561581;buf[20] = (byte) (t >>> 17);t = -1176762762;buf[21] = (byte) (t >>> 18);t = 1124192850;buf[22] = (byte) (t >>> 20);t = -585298983;buf[23] = (byte) (t >>> 10);t = 208593156;buf[24] = (byte) (t >>> 2);t = 682619248;buf[25] = (byte) (t >>> 23);t = -231752643;buf[26] = (byte) (t >>> 19);t = -24298625;buf[27] = (byte) (t >>> 17);t = -714860263;buf[28] = (byte) (t >>> 12);t = 1678797223;buf[29] = (byte) (t >>> 14);t = -358129670;buf[30] = (byte) (t >>> 17);t = -341242703;buf[31] = (byte) (t >>> 10);t = -1587998192;buf[32] = (byte) (t >>> 23);t = 976379018;buf[33] = (byte) (t >>> 11);t = 394476171;buf[34] = (byte) (t >>> 11);t = -777553266;buf[35] = (byte) (t >>> 12);t = -342428258;buf[36] = (byte) (t >>> 2);t = 1450961928;buf[37] = (byte) (t >>> 20);t = -1449649386;buf[38] = (byte) (t >>> 23);t = 165586349;buf[39] = (byte) (t >>> 2);t = 419854295;buf[40] = (byte) (t >>> 18);t = 1919396632;buf[41] = (byte) (t >>> 16);t = 1968319334;buf[42] = (byte) (t >>> 11);t = 1460355354;buf[43] = (byte) (t >>> 8);t = 2108549642;buf[44] = (byte) (t >>> 3);t = 933136947;buf[45] = (byte) (t >>> 23);t = -269248217;buf[46] = (byte) (t >>> 2);t = 1424513499;buf[47] = (byte) (t >>> 13);t = 2067925949;buf[48] = (byte) (t >>> 11);t = 342580181;buf[49] = (byte) (t >>> 22);t = -1322499155;buf[50] = (byte) (t >>> 8);t = 1829924972;buf[51] = (byte) (t >>> 9);t = -1900295790;buf[52] = (byte) (t >>> 11);t = 827889919;buf[53] = (byte) (t >>> 18);t = 1513889518;buf[54] = (byte) (t >>> 19);t = 188607776;buf[55] = (byte) (t >>> 5);t = -793150509;buf[56] = (byte) (t >>> 15);t = 11414968;buf[57] = (byte) (t >>> 17);t = 1745690027;buf[58] = (byte) (t >>> 2);t = 2040323598;buf[59] = (byte) (t >>> 15);t = 1662800730;buf[60] = (byte) (t >>> 19);t = -835302201;buf[61] = (byte) (t >>> 6);t = -1246124564;buf[62] = (byte) (t >>> 7);t = -1061589424;buf[63] = (byte) (t >>> 5);t = -879301526;buf[64] = (byte) (t >>> 21);t = 106358779;buf[65] = (byte) (t >>> 12);t = -1701349559;buf[66] = (byte) (t >>> 22);t = 519415623;buf[67] = (byte) (t >>> 7);t = 1442103076;buf[68] = (byte) (t >>> 24);t = -1709837020;buf[69] = (byte) (t >>> 15);t = 497888449;buf[70] = (byte) (t >>> 5);t = 983193461;buf[71] = (byte) (t >>> 15);t = 710956099;buf[72] = (byte) (t >>> 4);t = 1453094167;buf[73] = (byte) (t >>> 8);t = -1729800289;buf[74] = (byte) (t >>> 12);t = -264320607;buf[75] = (byte) (t >>> 9);t = 1478164679;buf[76] = (byte) (t >>> 1);t = 449820013;buf[77] = (byte) (t >>> 23);t = -686501488;buf[78] = (byte) (t >>> 12);t = -1361488041;buf[79] = (byte) (t >>> 21);t = -2136252513;buf[80] = (byte) (t >>> 11);t = 804471748;buf[81] = (byte) (t >>> 24);t = -368459674;buf[82] = (byte) (t >>> 10);t = 378857445;buf[83] = (byte) (t >>> 10);t = 993145681;buf[84] = (byte) (t >>> 4);t = 1990028485;buf[85] = (byte) (t >>> 1);t = 516215800;buf[86] = (byte) (t >>> 10);t = -951344800;buf[87] = (byte) (t >>> 11);t = 827644447;buf[88] = (byte) (t >>> 23);t = 2037576956;buf[89] = (byte) (t >>> 16);t = -1624880736;buf[90] = (byte) (t >>> 5);t = 161437469;buf[91] = (byte) (t >>> 19);t = -296031562;buf[92] = (byte) (t >>> 16);t = 1729468332;buf[93] = (byte) (t >>> 14);t = 400464987;buf[94] = (byte) (t >>> 14);t = -1441127092;buf[95] = (byte) (t >>> 2);t = -1680345883;buf[96] = (byte) (t >>> 23);t = -1741099528;buf[97] = (byte) (t >>> 16);t = 1687259661;buf[98] = (byte) (t >>> 14);t = -1054305264;buf[99] = (byte) (t >>> 15);t = 1016464689;buf[100] = (byte) (t >>> 4);t = -1090661869;buf[101] = (byte) (t >>> 10);t = -531838150;buf[102] = (byte) (t >>> 13);t = 1248377545;buf[103] = (byte) (t >>> 13);t = 403084673;buf[104] = (byte) (t >>> 9);t = -964987087;buf[105] = (byte) (t >>> 20);t = -1806883858;buf[106] = (byte) (t >>> 10);t = 538002888;buf[107] = (byte) (t >>> 8);t = 767487010;buf[108] = (byte) (t >>> 21);t = -1153470965;buf[109] = (byte) (t >>> 23);t = 847590998;buf[110] = (byte) (t >>> 7);t = 240661563;buf[111] = (byte) (t >>> 7);t = 2062469060;buf[112] = (byte) (t >>> 13);t = 567924960;buf[113] = (byte) (t >>> 6);t = 1948982341;buf[114] = (byte) (t >>> 16);t = 1425050511;buf[115] = (byte) (t >>> 17);t = 1990333499;buf[116] = (byte) (t >>> 11);t = 1939059950;buf[117] = (byte) (t >>> 7);t = 836644272;buf[118] = (byte) (t >>> 18);t = -562118244;buf[119] = (byte) (t >>> 2);t = 1570502787;buf[120] = (byte) (t >>> 1);t = -396345987;buf[121] = (byte) (t >>> 17);t = 1222342450;buf[122] = (byte) (t >>> 14);t = -2064989972;buf[123] = (byte) (t >>> 20);t = 113460076;buf[124] = (byte) (t >>> 12);t = 442642004;buf[125] = (byte) (t >>> 22);t = 1077103177;buf[126] = (byte) (t >>> 5);t = 817396136;buf[127] = (byte) (t >>> 24);t = -614320189;buf[128] = (byte) (t >>> 11);t = 744960450;buf[129] = (byte) (t >>> 8);t = 1097888535;buf[130] = (byte) (t >>> 18);t = 991047114;buf[131] = (byte) (t >>> 5);t = -858812121;buf[132] = (byte) (t >>> 22);t = -2050063081;buf[133] = (byte) (t >>> 13);t = 288168034;buf[134] = (byte) (t >>> 13);t = 904585525;buf[135] = (byte) (t >>> 13);t = 798959243;buf[136] = (byte) (t >>> 1);t = 99743032;buf[137] = (byte) (t >>> 4);t = 1911123092;buf[138] = (byte) (t >>> 13);t = -1393069616;buf[139] = (byte) (t >>> 12);t = 1737233181;buf[140] = (byte) (t >>> 24);t = 1304646222;buf[141] = (byte) (t >>> 22);t = 931349819;buf[142] = (byte) (t >>> 2);t = 1033558411;buf[143] = (byte) (t >>> 2);t = -101604245;buf[144] = (byte) (t >>> 10);t = 1794266087;buf[145] = (byte) (t >>> 8);t = 1944955234;buf[146] = (byte) (t >>> 2);t = -631688792;buf[147] = (byte) (t >>> 14);t = 1456414014;buf[148] = (byte) (t >>> 17);t = 334351828;buf[149] = (byte) (t >>> 22);t = -231103825;buf[150] = (byte) (t >>> 3);t = 857727268;buf[151] = (byte) (t >>> 2);t = -713288272;buf[152] = (byte) (t >>> 6);t = 1467890298;buf[153] = (byte) (t >>> 7);t = 1337644571;buf[154] = (byte) (t >>> 11);t = -1957465060;buf[155] = (byte) (t >>> 4);t = -1330947460;buf[156] = (byte) (t >>> 6);t = -131445343;buf[157] = (byte) (t >>> 5);t = 100084661;buf[158] = (byte) (t >>> 12);t = 1779163616;buf[159] = (byte) (t >>> 24);t = 864610115;buf[160] = (byte) (t >>> 9);t = 469293864;buf[161] = (byte) (t >>> 3);t = -916782425;buf[162] = (byte) (t >>> 19);t = 1196977363;buf[163] = (byte) (t >>> 8);t = -1987794936;buf[164] = (byte) (t >>> 7);t = 1177659503;buf[165] = (byte) (t >>> 15);t = -929153385;buf[166] = (byte) (t >>> 14);t = -1909285639;buf[167] = (byte) (t >>> 22);t = -1425885964;buf[168] = (byte) (t >>> 11);t = -358067013;buf[169] = (byte) (t >>> 15);t = 1299537360;buf[170] = (byte) (t >>> 12);t = -1261555208;buf[171] = (byte) (t >>> 14);t = -1986848302;buf[172] = (byte) (t >>> 18);t = -1453768587;buf[173] = (byte) (t >>> 21);t = -1047162434;buf[174] = (byte) (t >>> 14);t = 687269179;buf[175] = (byte) (t >>> 23);t = 763741652;buf[176] = (byte) (t >>> 10);t = -50312453;buf[177] = (byte) (t >>> 5);t = -511156363;buf[178] = (byte) (t >>> 18);t = 1331179634;buf[179] = (byte) (t >>> 16);t = -1922377460;buf[180] = (byte) (t >>> 21);t = -447940434;buf[181] = (byte) (t >>> 20);t = 2092363819;buf[182] = (byte) (t >>> 12);t = -1269422064;buf[183] = (byte) (t >>> 20);t = -755915180;buf[184] = (byte) (t >>> 5);t = -344822989;buf[185] = (byte) (t >>> 16);t = 379552186;buf[186] = (byte) (t >>> 20);t = -1566820404;buf[187] = (byte) (t >>> 8);t = -1567061529;buf[188] = (byte) (t >>> 23);t = -426864078;buf[189] = (byte) (t >>> 20);t = 1950656575;buf[190] = (byte) (t >>> 5);t = 1783996193;buf[191] = (byte) (t >>> 10);t = 752410037;buf[192] = (byte) (t >>> 2);t = 228011450;buf[193] = (byte) (t >>> 18);t = -1101729026;buf[194] = (byte) (t >>> 9);t = 1779354061;buf[195] = (byte) (t >>> 9);t = 1554916819;buf[196] = (byte) (t >>> 15);t = 1853081179;buf[197] = (byte) (t >>> 24);t = 814758299;buf[198] = (byte) (t >>> 7);t = 1389478058;buf[199] = (byte) (t >>> 1);t = 1382089593;buf[200] = (byte) (t >>> 16);t = 34847146;buf[201] = (byte) (t >>> 14);t = -1208012318;buf[202] = (byte) (t >>> 7);t = 1576471680;buf[203] = (byte) (t >>> 22);t = 1522655362;buf[204] = (byte) (t >>> 10);t = -869890368;buf[205] = (byte) (t >>> 12);t = -17713440;buf[206] = (byte) (t >>> 11);t = -1438248;buf[207] = (byte) (t >>> 13);t = 368781887;buf[208] = (byte) (t >>> 5);t = -1934161220;buf[209] = (byte) (t >>> 22);t = -2101226617;buf[210] = (byte) (t >>> 6);t = 1143427629;buf[211] = (byte) (t >>> 24);t = -1678807836;buf[212] = (byte) (t >>> 8);t = 1019977020;buf[213] = (byte) (t >>> 17);t = -319326866;buf[214] = (byte) (t >>> 21);t = -1938201071;buf[215] = (byte) (t >>> 16);t = 359611146;buf[216] = (byte) (t >>> 7);t = 1038941416;buf[217] = (byte) (t >>> 13);t = 224506447;buf[218] = (byte) (t >>> 18);t = 1360751040;buf[219] = (byte) (t >>> 2);t = -1727184852;buf[220] = (byte) (t >>> 23);t = 2073151464;buf[221] = (byte) (t >>> 10);t = -1760098616;buf[222] = (byte) (t >>> 5);t = 115979177;buf[223] = (byte) (t >>> 13);t = -1780819670;buf[224] = (byte) (t >>> 14);t = 1609977128;buf[225] = (byte) (t >>> 5);t = -231008678;buf[226] = (byte) (t >>> 19);t = -1116698408;buf[227] = (byte) (t >>> 16);t = -1556871032;buf[228] = (byte) (t >>> 5);t = -2054615781;buf[229] = (byte) (t >>> 18);t = -526616787;buf[230] = (byte) (t >>> 17);t = -1077224886;buf[231] = (byte) (t >>> 6);t = 1552496944;buf[232] = (byte) (t >>> 7);t = 371110837;buf[233] = (byte) (t >>> 9);t = 1550626570;buf[234] = (byte) (t >>> 16);t = 1458703272;buf[235] = (byte) (t >>> 11);t = -1305949332;buf[236] = (byte) (t >>> 9);t = -281284120;buf[237] = (byte) (t >>> 2);t = 2018063775;buf[238] = (byte) (t >>> 24);t = 858671389;buf[239] = (byte) (t >>> 5);t = -172247059;buf[240] = (byte) (t >>> 18);t = 982689160;buf[241] = (byte) (t >>> 19);t = 298718463;buf[242] = (byte) (t >>> 6);t = -460804457;buf[243] = (byte) (t >>> 5);t = -1900467825;buf[244] = (byte) (t >>> 2);t = 329001407;buf[245] = (byte) (t >>> 22);t = -1519581089;buf[246] = (byte) (t >>> 16);t = -1512902902;buf[247] = (byte) (t >>> 14);t = 320763186;buf[248] = (byte) (t >>> 4);t = -145848102;buf[249] = (byte) (t >>> 13);t = 1345834203;buf[250] = (byte) (t >>> 5);t = -277045915;buf[251] = (byte) (t >>> 2);t = -929183541;buf[252] = (byte) (t >>> 21);t = 1469855777;buf[253] = (byte) (t >>> 8);t = -2026590930;buf[254] = (byte) (t >>> 7);t = -1253432902;buf[255] = (byte) (t >>> 18);t = 238011616;buf[256] = (byte) (t >>> 4);t = -860198085;buf[257] = (byte) (t >>> 3);t = -1518549251;buf[258] = (byte) (t >>> 10);t = -5962190;buf[259] = (byte) (t >>> 12);t = -447471832;buf[260] = (byte) (t >>> 18);t = -1029534992;buf[261] = (byte) (t >>> 19);t = 2033568269;buf[262] = (byte) (t >>> 12);t = 1726424791;buf[263] = (byte) (t >>> 20);t = -1902718631;buf[264] = (byte) (t >>> 17);t = -459202787;buf[265] = (byte) (t >>> 15);t = -474609079;buf[266] = (byte) (t >>> 5);t = -1510192719;buf[267] = (byte) (t >>> 21);t = -751851440;buf[268] = (byte) (t >>> 7);t = 1213052100;buf[269] = (byte) (t >>> 24);t = -1824940483;buf[270] = (byte) (t >>> 22);t = -1885142390;buf[271] = (byte) (t >>> 15);t = -95245539;buf[272] = (byte) (t >>> 9);t = 1647263041;buf[273] = (byte) (t >>> 19);t = -1080325435;buf[274] = (byte) (t >>> 3);t = -1804900494;buf[275] = (byte) (t >>> 17);t = -187078732;buf[276] = (byte) (t >>> 18);t = -747907978;buf[277] = (byte) (t >>> 11);t = 2051948863;buf[278] = (byte) (t >>> 19);t = 1428770523;buf[279] = (byte) (t >>> 5);t = 984475757;buf[280] = (byte) (t >>> 19);t = -1444649743;buf[281] = (byte) (t >>> 1);t = -697275556;buf[282] = (byte) (t >>> 20);t = -282131459;buf[283] = (byte) (t >>> 16);t = -656627420;buf[284] = (byte) (t >>> 2);t = 580703775;buf[285] = (byte) (t >>> 14);t = -1232286893;buf[286] = (byte) (t >>> 17);t = 1290578836;buf[287] = (byte) (t >>> 24);t = -952295782;buf[288] = (byte) (t >>> 21);t = 1222092138;buf[289] = (byte) (t >>> 21);t = 626514023;buf[290] = (byte) (t >>> 6);t = -621791203;buf[291] = (byte) (t >>> 22);t = -1957015246;buf[292] = (byte) (t >>> 6);t = 218285593;buf[293] = (byte) (t >>> 11);t = -2093451524;buf[294] = (byte) (t >>> 15);t = 1437339261;buf[295] = (byte) (t >>> 20);t = -1810063051;buf[296] = (byte) (t >>> 22);t = 457444521;buf[297] = (byte) (t >>> 1);t = 361448861;buf[298] = (byte) (t >>> 22);t = -2036801318;buf[299] = (byte) (t >>> 14);t = -446594666;buf[300] = (byte) (t >>> 3);t = 1310521596;buf[301] = (byte) (t >>> 14);t = -1382637319;buf[302] = (byte) (t >>> 12);t = -375680225;buf[303] = (byte) (t >>> 18);t = 888190991;buf[304] = (byte) (t >>> 20);t = -2004060122;buf[305] = (byte) (t >>> 14);t = -521782622;buf[306] = (byte) (t >>> 13);t = -201445758;buf[307] = (byte) (t >>> 1);t = 1519991083;buf[308] = (byte) (t >>> 17);t = 1325756065;buf[309] = (byte) (t >>> 8);t = 1432836173;buf[310] = (byte) (t >>> 22);t = 1385229028;buf[311] = (byte) (t >>> 14);t = 1807961209;buf[312] = (byte) (t >>> 5);t = -354949007;buf[313] = (byte) (t >>> 17);t = -1607178412;buf[314] = (byte) (t >>> 8);t = 1529232190;buf[315] = (byte) (t >>> 3);t = 542805079;buf[316] = (byte) (t >>> 6);t = -1500462551;buf[317] = (byte) (t >>> 3);t = 2087675799;buf[318] = (byte) (t >>> 4);t = -898346123;buf[319] = (byte) (t >>> 8);t = -553330939;buf[320] = (byte) (t >>> 9);t = -2067625323;buf[321] = (byte) (t >>> 3);t = -1896442282;buf[322] = (byte) (t >>> 6);t = -1687713576;buf[323] = (byte) (t >>> 22);t = -1083564250;buf[324] = (byte) (t >>> 16);t = -1166719786;buf[325] = (byte) (t >>> 12);t = -2012533191;buf[326] = (byte) (t >>> 11);t = 162399930;buf[327] = (byte) (t >>> 13);t = -817131006;buf[328] = (byte) (t >>> 13);t = -1900422271;buf[329] = (byte) (t >>> 6);t = 1296460137;buf[330] = (byte) (t >>> 5);t = -319526045;buf[331] = (byte) (t >>> 12);t = 1544768519;buf[332] = (byte) (t >>> 14);t = -1232511400;buf[333] = (byte) (t >>> 11);t = -2092259996;buf[334] = (byte) (t >>> 4);t = 935971549;buf[335] = (byte) (t >>> 23);t = -621849443;buf[336] = (byte) (t >>> 1);t = 1242369079;buf[337] = (byte) (t >>> 6);t = -466287572;buf[338] = (byte) (t >>> 4);t = -1111542152;buf[339] = (byte) (t >>> 8);t = 1453331116;buf[340] = (byte) (t >>> 22);t = 1433636224;buf[341] = (byte) (t >>> 12);t = 1448002369;buf[342] = (byte) (t >>> 3);t = -244426682;buf[343] = (byte) (t >>> 8);t = -958775108;buf[344] = (byte) (t >>> 2);t = -1340655464;buf[345] = (byte) (t >>> 12);t = 948951874;buf[346] = (byte) (t >>> 17);t = -1860823137;buf[347] = (byte) (t >>> 12);t = 1722202949;buf[348] = (byte) (t >>> 24);t = 857165416;buf[349] = (byte) (t >>> 8);t = -509124712;buf[350] = (byte) (t >>> 15);t = -2130846817;buf[351] = (byte) (t >>> 10);t = 477936274;buf[352] = (byte) (t >>> 7);t = -1551899572;buf[353] = (byte) (t >>> 4);t = -316631284;buf[354] = (byte) (t >>> 18);t = -1540119629;buf[355] = (byte) (t >>> 3);t = 2089847364;buf[356] = (byte) (t >>> 9);t = -1587229337;buf[357] = (byte) (t >>> 12);t = 1286602756;buf[358] = (byte) (t >>> 24);t = -1080901400;buf[359] = (byte) (t >>> 1);t = -727047037;buf[360] = (byte) (t >>> 7);t = -1491777026;buf[361] = (byte) (t >>> 23);t = -43453558;buf[362] = (byte) (t >>> 16);t = -903371892;buf[363] = (byte) (t >>> 15);t = -1624077734;buf[364] = (byte) (t >>> 15);t = 1672463128;buf[365] = (byte) (t >>> 24);t = -723430748;buf[366] = (byte) (t >>> 3);t = 2073865219;buf[367] = (byte) (t >>> 14);t = 2135719994;buf[368] = (byte) (t >>> 16);t = -353332107;buf[369] = (byte) (t >>> 9);t = -2059938147;buf[370] = (byte) (t >>> 15);t = 2101028387;buf[371] = (byte) (t >>> 4);t = 2139736183;buf[372] = (byte) (t >>> 11);t = -1580026796;buf[373] = (byte) (t >>> 7);t = -793544205;buf[374] = (byte) (t >>> 8);t = -1778172512;buf[375] = (byte) (t >>> 12);t = -1645951415;buf[376] = (byte) (t >>> 9);t = -472474926;buf[377] = (byte) (t >>> 3);t = -2100868057;buf[378] = (byte) (t >>> 6);t = 1153771633;buf[379] = (byte) (t >>> 10);t = 1051736013;buf[380] = (byte) (t >>> 17);t = 720663602;buf[381] = (byte) (t >>> 21);t = -1383905352;buf[382] = (byte) (t >>> 11);t = -1818589615;buf[383] = (byte) (t >>> 22);t = 324809602;buf[384] = (byte) (t >>> 8);t = -1729830968;buf[385] = (byte) (t >>> 9);t = -1976079544;buf[386] = (byte) (t >>> 12);t = -569659457;buf[387] = (byte) (t >>> 22);t = -380123187;buf[388] = (byte) (t >>> 16);t = -729382283;buf[389] = (byte) (t >>> 3);t = 647150749;buf[390] = (byte) (t >>> 21);t = 703376062;buf[391] = (byte) (t >>> 5);t = -231094443;buf[392] = (byte) (t >>> 4);t = 1836680406;buf[393] = (byte) (t >>> 4);t = -1263637534;buf[394] = (byte) (t >>> 15);t = -272573034;buf[395] = (byte) (t >>> 9);t = 1272915495;buf[396] = (byte) (t >>> 24);t = 876017021;buf[397] = (byte) (t >>> 12);t = -524381421;buf[398] = (byte) (t >>> 4);t = 1461472209;buf[399] = (byte) (t >>> 6);t = 20506099;buf[400] = (byte) (t >>> 18);t = 627516766;buf[401] = (byte) (t >>> 7);t = 1521279252;buf[402] = (byte) (t >>> 22);t = -1173771553;buf[403] = (byte) (t >>> 5);t = 1748233933;buf[404] = (byte) (t >>> 21);t = -1690693029;buf[405] = (byte) (t >>> 19);t = -2000050933;buf[406] = (byte) (t >>> 13);t = 1787084594;buf[407] = (byte) (t >>> 17);t = -1134861811;buf[408] = (byte) (t >>> 3);t = 1348991193;buf[409] = (byte) (t >>> 22);t = -1133820670;buf[410] = (byte) (t >>> 8);t = -1146335037;buf[411] = (byte) (t >>> 6);t = 651978735;buf[412] = (byte) (t >>> 8);t = 1328074433;buf[413] = (byte) (t >>> 9);t = -1333705258;buf[414] = (byte) (t >>> 8);t = -253865461;buf[415] = (byte) (t >>> 3);t = -1520207340;buf[416] = (byte) (t >>> 20);t = -1194568448;buf[417] = (byte) (t >>> 17);t = -2059335266;buf[418] = (byte) (t >>> 2);t = -476964245;buf[419] = (byte) (t >>> 14);t = -229091840;buf[420] = (byte) (t >>> 14);t = -1825489657;buf[421] = (byte) (t >>> 10);t = 1398418501;buf[422] = (byte) (t >>> 11);t = 590317956;buf[423] = (byte) (t >>> 20);t = 1535231130;buf[424] = (byte) (t >>> 11);t = -593051317;buf[425] = (byte) (t >>> 12);t = 823310983;buf[426] = (byte) (t >>> 3);t = 1993173952;buf[427] = (byte) (t >>> 8);t = -1770372178;buf[428] = (byte) (t >>> 22);t = 372523974;buf[429] = (byte) (t >>> 8);t = -310179464;buf[430] = (byte) (t >>> 5);t = 1678226311;buf[431] = (byte) (t >>> 24);t = 1536891758;buf[432] = (byte) (t >>> 7);t = 867516532;buf[433] = (byte) (t >>> 10);t = 285643698;buf[434] = (byte) (t >>> 3);t = 1710812555;buf[435] = (byte) (t >>> 21);t = 625023469;buf[436] = (byte) (t >>> 23);t = 1692129751;buf[437] = (byte) (t >>> 14);t = -349599918;buf[438] = (byte) (t >>> 19);t = 1513121085;buf[439] = (byte) (t >>> 9);t = -1435744861;buf[440] = (byte) (t >>> 23);t = -2028522124;buf[441] = (byte) (t >>> 20);t = -1845180845;buf[442] = (byte) (t >>> 22);t = 1657793365;buf[443] = (byte) (t >>> 24);t = -1210176532;buf[444] = (byte) (t >>> 14);t = 256143124;buf[445] = (byte) (t >>> 5);t = 1378093746;buf[446] = (byte) (t >>> 5);t = 548465838;buf[447] = (byte) (t >>> 1);t = 970237576;buf[448] = (byte) (t >>> 18);t = -125152380;buf[449] = (byte) (t >>> 13);t = -1521042029;buf[450] = (byte) (t >>> 12);t = 1734446311;buf[451] = (byte) (t >>> 20);t = 540742429;buf[452] = (byte) (t >>> 11);t = 2079498435;buf[453] = (byte) (t >>> 1);t = -1230725471;buf[454] = (byte) (t >>> 1);t = 105773731;buf[455] = (byte) (t >>> 20);t = -731664720;buf[456] = (byte) (t >>> 5);t = -986517988;buf[457] = (byte) (t >>> 16);t = -922744924;buf[458] = (byte) (t >>> 4);t = 1631593882;buf[459] = (byte) (t >>> 4);t = 1439057712;buf[460] = (byte) (t >>> 20);t = -1523127245;buf[461] = (byte) (t >>> 12);t = -644761812;buf[462] = (byte) (t >>> 22);t = 172826214;buf[463] = (byte) (t >>> 19);t = -658943509;buf[464] = (byte) (t >>> 15);t = 446112631;buf[465] = (byte) (t >>> 7);t = 1321575342;buf[466] = (byte) (t >>> 3);t = -1895131713;buf[467] = (byte) (t >>> 2);t = 1725544727;buf[468] = (byte) (t >>> 7);t = 1719012616;buf[469] = (byte) (t >>> 24);t = 1096810759;buf[470] = (byte) (t >>> 18);t = 1038507487;buf[471] = (byte) (t >>> 2);t = -1740882869;buf[472] = (byte) (t >>> 23);t = -2035783668;buf[473] = (byte) (t >>> 13);t = -2061441126;buf[474] = (byte) (t >>> 9);t = 435792359;buf[475] = (byte) (t >>> 7);t = -1202492653;buf[476] = (byte) (t >>> 3);t = -1114089685;buf[477] = (byte) (t >>> 8);t = 1549961997;buf[478] = (byte) (t >>> 11);t = 319040686;buf[479] = (byte) (t >>> 1);t = -502786669;buf[480] = (byte) (t >>> 2);t = 1216749460;buf[481] = (byte) (t >>> 21);t = 1689295906;buf[482] = (byte) (t >>> 20);t = 1081243251;buf[483] = (byte) (t >>> 8);t = -1323955356;buf[484] = (byte) (t >>> 14);t = -22335753;buf[485] = (byte) (t >>> 15);t = 1213445567;buf[486] = (byte) (t >>> 16);t = -1707673702;buf[487] = (byte) (t >>> 22);t = -1856607495;buf[488] = (byte) (t >>> 9);t = -211188726;buf[489] = (byte) (t >>> 16);t = 235117676;buf[490] = (byte) (t >>> 1);t = 1989908148;buf[491] = (byte) (t >>> 6);t = -398109460;buf[492] = (byte) (t >>> 16);t = 2126277893;buf[493] = (byte) (t >>> 15);t = 1393533917;buf[494] = (byte) (t >>> 24);t = 1729412013;buf[495] = (byte) (t >>> 24);t = -1998823765;buf[496] = (byte) (t >>> 9);t = 862759461;buf[497] = (byte) (t >>> 9);t = -118098593;buf[498] = (byte) (t >>> 17);t = 1177829683;buf[499] = (byte) (t >>> 12);t = 1968058155;buf[500] = (byte) (t >>> 5);t = -522757588;buf[501] = (byte) (t >>> 18);t = 590131818;buf[502] = (byte) (t >>> 9);t = 1546854330;buf[503] = (byte) (t >>> 12);t = 1118193477;buf[504] = (byte) (t >>> 24);t = 746480319;buf[505] = (byte) (t >>> 3);t = -257651177;buf[506] = (byte) (t >>> 9);t = 350725177;buf[507] = (byte) (t >>> 20);t = 355082356;buf[508] = (byte) (t >>> 22);t = -931019977;buf[509] = (byte) (t >>> 4);t = 136951974;buf[510] = (byte) (t >>> 10);t = -506286229;buf[511] = (byte) (t >>> 11);t = 210001048;buf[512] = (byte) (t >>> 6);t = 1746950769;buf[513] = (byte) (t >>> 8);t = 1092289527;buf[514] = (byte) (t >>> 24);t = 1333177423;buf[515] = (byte) (t >>> 9);t = -268279019;buf[516] = (byte) (t >>> 8);t = 236676718;buf[517] = (byte) (t >>> 21);t = -207013871;buf[518] = (byte) (t >>> 8);t = -426455895;buf[519] = (byte) (t >>> 9);t = 1464408245;buf[520] = (byte) (t >>> 16);t = -129320620;buf[521] = (byte) (t >>> 11);t = -1312214210;buf[522] = (byte) (t >>> 3);t = 1654480767;buf[523] = (byte) (t >>> 14);t = 434884898;buf[524] = (byte) (t >>> 2);t = -653945734;buf[525] = (byte) (t >>> 18);t = 988038253;buf[526] = (byte) (t >>> 19);t = 347557276;buf[527] = (byte) (t >>> 15);t = -622396691;buf[528] = (byte) (t >>> 22);t = -158483904;buf[529] = (byte) (t >>> 14);t = 597017584;buf[530] = (byte) (t >>> 19);t = -719211776;buf[531] = (byte) (t >>> 4);t = 329630346;buf[532] = (byte) (t >>> 19);t = -1152163963;buf[533] = (byte) (t >>> 12);t = 1115044570;buf[534] = (byte) (t >>> 1);t = 1875161261;buf[535] = (byte) (t >>> 24);t = 1308336849;buf[536] = (byte) (t >>> 21);t = 13534031;buf[537] = (byte) (t >>> 4);t = -862680661;buf[538] = (byte) (t >>> 5);t = 895114380;buf[539] = (byte) (t >>> 23);t = 1902726527;buf[540] = (byte) (t >>> 16);t = -1314533913;buf[541] = (byte) (t >>> 6);t = 1223542333;buf[542] = (byte) (t >>> 17);t = -1095684985;buf[543] = (byte) (t >>> 7);t = 1202070451;buf[544] = (byte) (t >>> 20);t = -1430357670;buf[545] = (byte) (t >>> 23);t = -210080430;buf[546] = (byte) (t >>> 8);t = -563643972;buf[547] = (byte) (t >>> 16);t = -839749007;buf[548] = (byte) (t >>> 9);t = 365630273;buf[549] = (byte) (t >>> 3);t = 1769629298;buf[550] = (byte) (t >>> 16);t = 885792272;buf[551] = (byte) (t >>> 14);t = -586217694;buf[552] = (byte) (t >>> 18);t = 663601209;buf[553] = (byte) (t >>> 10);t = -1444766363;buf[554] = (byte) (t >>> 18);t = 319447209;buf[555] = (byte) (t >>> 11);t = -646672366;buf[556] = (byte) (t >>> 12);t = -1922625324;buf[557] = (byte) (t >>> 13);t = 2098774299;buf[558] = (byte) (t >>> 15);t = -1744316813;buf[559] = (byte) (t >>> 23);t = -346530575;buf[560] = (byte) (t >>> 21);t = 972910471;buf[561] = (byte) (t >>> 23);t = -1580368858;buf[562] = (byte) (t >>> 18);t = 682621281;buf[563] = (byte) (t >>> 2);t = -1191867565;buf[564] = (byte) (t >>> 23);t = -1879320366;buf[565] = (byte) (t >>> 6);t = 1531297083;buf[566] = (byte) (t >>> 10);t = -179549479;buf[567] = (byte) (t >>> 6);t = 1111845680;buf[568] = (byte) (t >>> 12);t = 1750458939;buf[569] = (byte) (t >>> 16);t = 312051637;buf[570] = (byte) (t >>> 14);t = 556447007;buf[571] = (byte) (t >>> 15);t = 1606715748;buf[572] = (byte) (t >>> 9);t = 808534219;buf[573] = (byte) (t >>> 1);t = 1524941842;buf[574] = (byte) (t >>> 17);t = -118720614;buf[575] = (byte) (t >>> 4);t = 1732587056;buf[576] = (byte) (t >>> 12);t = -921263217;buf[577] = (byte) (t >>> 9);t = -1898209164;buf[578] = (byte) (t >>> 21);t = 704623097;buf[579] = (byte) (t >>> 21);t = 1903692215;buf[580] = (byte) (t >>> 24);t = 2061912472;buf[581] = (byte) (t >>> 24);t = 1093852448;buf[582] = (byte) (t >>> 9);t = -740399344;buf[583] = (byte) (t >>> 4);t = 1301928066;buf[584] = (byte) (t >>> 22);t = -828926180;buf[585] = (byte) (t >>> 4);t = -1784274686;buf[586] = (byte) (t >>> 13);t = 1457461685;buf[587] = (byte) (t >>> 6);t = -1574706659;buf[588] = (byte) (t >>> 3);t = 1228300371;buf[589] = (byte) (t >>> 15);t = -327437784;buf[590] = (byte) (t >>> 11);t = 317233267;buf[591] = (byte) (t >>> 17);t = -15978888;buf[592] = (byte) (t >>> 7);t = 1937186874;buf[593] = (byte) (t >>> 19);t = 218705475;buf[594] = (byte) (t >>> 13);t = 2146586074;buf[595] = (byte) (t >>> 5);t = -1871642940;buf[596] = (byte) (t >>> 1);t = 750349267;buf[597] = (byte) (t >>> 23);t = 1923430185;buf[598] = (byte) (t >>> 19);t = -1365423811;buf[599] = (byte) (t >>> 21);t = -1686345567;buf[600] = (byte) (t >>> 5);t = -123282621;buf[601] = (byte) (t >>> 9);t = 1857348421;buf[602] = (byte) (t >>> 4);t = -2123790222;buf[603] = (byte) (t >>> 5);t = 1473012324;buf[604] = (byte) (t >>> 24);t = -1815512525;buf[605] = (byte) (t >>> 19);t = -1919511963;buf[606] = (byte) (t >>> 9);t = 1137849622;buf[607] = (byte) (t >>> 19);t = -1529007408;buf[608] = (byte) (t >>> 8);t = 1565234256;buf[609] = (byte) (t >>> 16);t = -1320995429;buf[610] = (byte) (t >>> 23);t = 620299590;buf[611] = (byte) (t >>> 23);t = -1220832664;buf[612] = (byte) (t >>> 6);t = 1743669580;buf[613] = (byte) (t >>> 5);t = 1372367677;buf[614] = (byte) (t >>> 3);t = -1794371865;buf[615] = (byte) (t >>> 20);t = -1178571628;buf[616] = (byte) (t >>> 5);t = -1339772606;buf[617] = (byte) (t >>> 2);t = 1300479158;buf[618] = (byte) (t >>> 21);t = -1148217888;buf[619] = (byte) (t >>> 7);t = 626558754;buf[620] = (byte) (t >>> 18);t = 2029748533;buf[621] = (byte) (t >>> 24);t = -539614254;buf[622] = (byte) (t >>> 7);t = -590834213;buf[623] = (byte) (t >>> 13);t = -1610409104;buf[624] = (byte) (t >>> 3);t = 1482322586;buf[625] = (byte) (t >>> 11);t = -462182298;buf[626] = (byte) (t >>> 5);t = 1517277703;buf[627] = (byte) (t >>> 6);t = 1247389508;buf[628] = (byte) (t >>> 10);t = 1280916041;buf[629] = (byte) (t >>> 7);t = 1578763941;buf[630] = (byte) (t >>> 5);t = -654465704;buf[631] = (byte) (t >>> 22);t = -2134396060;buf[632] = (byte) (t >>> 17);t = 273108648;buf[633] = (byte) (t >>> 5);t = 426469164;buf[634] = (byte) (t >>> 22);t = 2001268742;buf[635] = (byte) (t >>> 16);t = 427465854;buf[636] = (byte) (t >>> 6);t = 1720445728;buf[637] = (byte) (t >>> 20);t = 1289173367;buf[638] = (byte) (t >>> 18);t = 1029292594;buf[639] = (byte) (t >>> 3);t = 1304622545;buf[640] = (byte) (t >>> 22);t = 1642523614;buf[641] = (byte) (t >>> 9);t = 1453786377;buf[642] = (byte) (t >>> 13);t = -1120723484;buf[643] = (byte) (t >>> 15);t = -1170894590;buf[644] = (byte) (t >>> 10);t = -683818989;buf[645] = (byte) (t >>> 10);t = 1322378345;buf[646] = (byte) (t >>> 21);t = 1967562264;buf[647] = (byte) (t >>> 6);t = 337491865;buf[648] = (byte) (t >>> 7);t = -1101650667;buf[649] = (byte) (t >>> 12);t = 588411143;buf[650] = (byte) (t >>> 2);t = -1412584085;buf[651] = (byte) (t >>> 13);t = 853975617;buf[652] = (byte) (t >>> 7);t = 1317679436;buf[653] = (byte) (t >>> 2);t = -1363366661;buf[654] = (byte) (t >>> 5);t = 911053027;buf[655] = (byte) (t >>> 2);t = 61049867;buf[656] = (byte) (t >>> 19);t = -1984721691;buf[657] = (byte) (t >>> 15);t = 292700463;buf[658] = (byte) (t >>> 18);t = -1379669275;buf[659] = (byte) (t >>> 21);t = 95941355;buf[660] = (byte) (t >>> 18);t = 878294003;buf[661] = (byte) (t >>> 24);t = 1741010768;buf[662] = (byte) (t >>> 7);t = -2085687379;buf[663] = (byte) (t >>> 9);t = -2063239238;buf[664] = (byte) (t >>> 8);t = -1290570935;buf[665] = (byte) (t >>> 12);t = 1897205145;buf[666] = (byte) (t >>> 5);t = -1660512551;buf[667] = (byte) (t >>> 9);t = -1897083578;buf[668] = (byte) (t >>> 2);t = 1177137873;buf[669] = (byte) (t >>> 13);t = 2005893511;buf[670] = (byte) (t >>> 20);t = 863405702;buf[671] = (byte) (t >>> 5);t = -582063243;buf[672] = (byte) (t >>> 14);t = -1247141256;buf[673] = (byte) (t >>> 15);t = 591544062;buf[674] = (byte) (t >>> 16);t = -1133255070;buf[675] = (byte) (t >>> 4);t = 1005610759;buf[676] = (byte) (t >>> 5);t = 706631343;buf[677] = (byte) (t >>> 14);t = 1348353410;buf[678] = (byte) (t >>> 24);t = 1028054212;buf[679] = (byte) (t >>> 2);t = -1213236289;buf[680] = (byte) (t >>> 3);t = 352560214;buf[681] = (byte) (t >>> 1);t = 1382480415;buf[682] = (byte) (t >>> 3);t = -857017961;buf[683] = (byte) (t >>> 17);t = -515165158;buf[684] = (byte) (t >>> 11);t = 724226439;buf[685] = (byte) (t >>> 3);t = 450933459;buf[686] = (byte) (t >>> 3);t = -1537831965;buf[687] = (byte) (t >>> 17);t = -205879656;buf[688] = (byte) (t >>> 4);t = -1249126819;buf[689] = (byte) (t >>> 6);t = -1847420290;buf[690] = (byte) (t >>> 11);t = 787474099;buf[691] = (byte) (t >>> 17);t = -880719011;buf[692] = (byte) (t >>> 8);t = -1303697391;buf[693] = (byte) (t >>> 16);t = 1667586569;buf[694] = (byte) (t >>> 19);t = 196217081;buf[695] = (byte) (t >>> 5);t = 937484581;buf[696] = (byte) (t >>> 2);t = -1458143546;buf[697] = (byte) (t >>> 23);t = 1475725436;buf[698] = (byte) (t >>> 24);t = -1942199059;buf[699] = (byte) (t >>> 8);t = 1424897153;buf[700] = (byte) (t >>> 13);t = 529870726;buf[701] = (byte) (t >>> 14);t = -518425899;buf[702] = (byte) (t >>> 14);t = 716057482;buf[703] = (byte) (t >>> 17);t = 1293727398;buf[704] = (byte) (t >>> 1);t = 637821278;buf[705] = (byte) (t >>> 8);t = -82429512;buf[706] = (byte) (t >>> 8);t = -1001224176;buf[707] = (byte) (t >>> 16);t = 348007542;buf[708] = (byte) (t >>> 7);t = 148778975;buf[709] = (byte) (t >>> 17);t = -1631448086;buf[710] = (byte) (t >>> 11);t = -845080192;buf[711] = (byte) (t >>> 7);t = 1962078328;buf[712] = (byte) (t >>> 17);t = 836790687;buf[713] = (byte) (t >>> 18);t = 417366741;buf[714] = (byte) (t >>> 17);t = -1584208861;buf[715] = (byte) (t >>> 18);t = -169060242;buf[716] = (byte) (t >>> 13);t = 2076669030;buf[717] = (byte) (t >>> 4);t = -59304228;buf[718] = (byte) (t >>> 1);t = 15780100;buf[719] = (byte) (t >>> 17);t = 595277936;buf[720] = (byte) (t >>> 1);t = -1517423926;buf[721] = (byte) (t >>> 1);t = 1947180710;buf[722] = (byte) (t >>> 1);t = 1252137329;buf[723] = (byte) (t >>> 11);t = -879017255;buf[724] = (byte) (t >>> 21);t = -963987613;buf[725] = (byte) (t >>> 7);t = -615761873;buf[726] = (byte) (t >>> 14);t = -1837132802;buf[727] = (byte) (t >>> 19);t = -750644709;buf[728] = (byte) (t >>> 22);t = -1793088961;buf[729] = (byte) (t >>> 22);t = 1122757237;buf[730] = (byte) (t >>> 24);t = -1520997759;buf[731] = (byte) (t >>> 20);t = 596292970;buf[732] = (byte) (t >>> 7);t = 560152447;buf[733] = (byte) (t >>> 23);t = 514921890;buf[734] = (byte) (t >>> 17);t = 652382551;buf[735] = (byte) (t >>> 6);t = 699688868;buf[736] = (byte) (t >>> 15);t = -630272752;buf[737] = (byte) (t >>> 5);t = 552010617;buf[738] = (byte) (t >>> 12);t = -429101538;buf[739] = (byte) (t >>> 16);t = -1811731185;buf[740] = (byte) (t >>> 7);t = 1849267684;buf[741] = (byte) (t >>> 21);t = -1258821664;buf[742] = (byte) (t >>> 23);t = 1512474777;buf[743] = (byte) (t >>> 5);t = -1966860205;buf[744] = (byte) (t >>> 19);t = 2015619438;buf[745] = (byte) (t >>> 15);t = -71278238;buf[746] = (byte) (t >>> 19);t = 1573771088;buf[747] = (byte) (t >>> 10);t = -1927450372;buf[748] = (byte) (t >>> 14);t = -298430550;buf[749] = (byte) (t >>> 12);t = 779552982;buf[750] = (byte) (t >>> 1);t = -693532019;buf[751] = (byte) (t >>> 11);t = 2092918009;buf[752] = (byte) (t >>> 8);t = -914542126;buf[753] = (byte) (t >>> 7);t = 127665680;buf[754] = (byte) (t >>> 20);t = -920447649;buf[755] = (byte) (t >>> 11);t = -2031118432;buf[756] = (byte) (t >>> 17);t = 1519426090;buf[757] = (byte) (t >>> 6);t = -1827616763;buf[758] = (byte) (t >>> 19);t = 1309531771;buf[759] = (byte) (t >>> 10);t = -2043783000;buf[760] = (byte) (t >>> 13);t = -1905822731;buf[761] = (byte) (t >>> 8);t = -2114983548;buf[762] = (byte) (t >>> 5);t = -146270698;buf[763] = (byte) (t >>> 6);t = 901553837;buf[764] = (byte) (t >>> 15);t = 618299645;buf[765] = (byte) (t >>> 23);t = -1712950739;buf[766] = (byte) (t >>> 12);t = -1415206356;buf[767] = (byte) (t >>> 5);t = -1066195537;buf[768] = (byte) (t >>> 7);t = -34584497;buf[769] = (byte) (t >>> 5);t = -1987252608;buf[770] = (byte) (t >>> 13);t = -1113013635;buf[771] = (byte) (t >>> 15);t = -665779821;buf[772] = (byte) (t >>> 14);t = -1392102660;buf[773] = (byte) (t >>> 8);t = 85240229;buf[774] = (byte) (t >>> 12);t = 92558922;buf[775] = (byte) (t >>> 6);t = -761590623;buf[776] = (byte) (t >>> 15);t = -660522342;buf[777] = (byte) (t >>> 15);t = -901367460;buf[778] = (byte) (t >>> 2);t = 439893340;buf[779] = (byte) (t >>> 22);t = 43694596;buf[780] = (byte) (t >>> 14);t = -597252591;buf[781] = (byte) (t >>> 4);t = -1160167123;buf[782] = (byte) (t >>> 8);t = 629494462;buf[783] = (byte) (t >>> 23);t = -523916979;buf[784] = (byte) (t >>> 18);t = 40239695;buf[785] = (byte) (t >>> 17);t = 1101949138;buf[786] = (byte) (t >>> 19);t = 1209724094;buf[787] = (byte) (t >>> 24);t = 1670877169;buf[788] = (byte) (t >>> 19);t = -373313628;buf[789] = (byte) (t >>> 21);t = 1229085896;buf[790] = (byte) (t >>> 8);t = 1129606000;buf[791] = (byte) (t >>> 3);t = 599823046;buf[792] = (byte) (t >>> 6);t = 865966631;buf[793] = (byte) (t >>> 14);t = -1391158454;buf[794] = (byte) (t >>> 6);t = -1503918805;buf[795] = (byte) (t >>> 14);t = 1372465546;buf[796] = (byte) (t >>> 14);t = -782561025;buf[797] = (byte) (t >>> 6);t = -641944394;buf[798] = (byte) (t >>> 9);t = -1906428023;buf[799] = (byte) (t >>> 21);t = 78504284;buf[800] = (byte) (t >>> 10);t = -1447993110;buf[801] = (byte) (t >>> 1);t = 1907905805;buf[802] = (byte) (t >>> 18);t = -1182582933;buf[803] = (byte) (t >>> 11);t = 1749685105;buf[804] = (byte) (t >>> 4);t = -1112506007;buf[805] = (byte) (t >>> 18);t = -1536726795;buf[806] = (byte) (t >>> 1);t = -1216106188;buf[807] = (byte) (t >>> 7);t = 353773984;buf[808] = (byte) (t >>> 22);t = -565005694;buf[809] = (byte) (t >>> 3);t = -1307325251;buf[810] = (byte) (t >>> 6);t = -56600439;buf[811] = (byte) (t >>> 17);t = 437056678;buf[812] = (byte) (t >>> 4);t = 1321644869;buf[813] = (byte) (t >>> 3);t = 1188718286;buf[814] = (byte) (t >>> 9);t = -286104345;buf[815] = (byte) (t >>> 4);t = 433160803;buf[816] = (byte) (t >>> 14);t = -1414888968;buf[817] = (byte) (t >>> 15);t = -1256769734;buf[818] = (byte) (t >>> 23);t = -486837811;buf[819] = (byte) (t >>> 11);t = 912941068;buf[820] = (byte) (t >>> 20);t = 595537939;buf[821] = (byte) (t >>> 20);t = -1902436934;buf[822] = (byte) (t >>> 21);t = 1098814237;buf[823] = (byte) (t >>> 6);t = 900372919;buf[824] = (byte) (t >>> 18);t = 1890145557;buf[825] = (byte) (t >>> 8);t = 298243814;buf[826] = (byte) (t >>> 4);t = 1107022448;buf[827] = (byte) (t >>> 3);t = 313725985;buf[828] = (byte) (t >>> 4);t = -623619707;buf[829] = (byte) (t >>> 22);t = 224537053;buf[830] = (byte) (t >>> 5);t = -601580817;buf[831] = (byte) (t >>> 7);t = 725877739;buf[832] = (byte) (t >>> 24);t = 258964977;buf[833] = (byte) (t >>> 16);t = 803681329;buf[834] = (byte) (t >>> 4);t = -1338862422;buf[835] = (byte) (t >>> 4);t = 1973026521;buf[836] = (byte) (t >>> 24);t = 308276683;buf[837] = (byte) (t >>> 5);t = 731611032;buf[838] = (byte) (t >>> 23);t = 866305909;buf[839] = (byte) (t >>> 19);t = -1534052334;buf[840] = (byte) (t >>> 7);t = -568255994;buf[841] = (byte) (t >>> 10);t = -1356966388;buf[842] = (byte) (t >>> 14);t = 940730134;buf[843] = (byte) (t >>> 9);t = 1721549443;buf[844] = (byte) (t >>> 9);t = 1046329697;buf[845] = (byte) (t >>> 2);t = -1086749260;buf[846] = (byte) (t >>> 2);t = -82165735;buf[847] = (byte) (t >>> 19);t = 184107388;buf[848] = (byte) (t >>> 21);t = 1115516685;buf[849] = (byte) (t >>> 24);t = -1596907127;buf[850] = (byte) (t >>> 23);t = 1568898764;buf[851] = (byte) (t >>> 11);t = -2059035599;buf[852] = (byte) (t >>> 12);t = 7226819;buf[853] = (byte) (t >>> 2);t = 1823724938;buf[854] = (byte) (t >>> 4);t = 364490307;buf[855] = (byte) (t >>> 10);t = 1819565242;buf[856] = (byte) (t >>> 8);t = 193064415;buf[857] = (byte) (t >>> 5);t = -1215430408;buf[858] = (byte) (t >>> 23);t = -1745744939;buf[859] = (byte) (t >>> 6);t = 1833394195;buf[860] = (byte) (t >>> 24);t = 1753024175;buf[861] = (byte) (t >>> 5);t = -639110485;buf[862] = (byte) (t >>> 3);t = 1532866949;buf[863] = (byte) (t >>> 3);t = 986723741;buf[864] = (byte) (t >>> 8);t = -496733292;buf[865] = (byte) (t >>> 3);t = 1255433659;buf[866] = (byte) (t >>> 19);t = -1427916406;buf[867] = (byte) (t >>> 3);t = -629259589;buf[868] = (byte) (t >>> 3);t = -1143239424;buf[869] = (byte) (t >>> 6);t = -1751989177;buf[870] = (byte) (t >>> 14);t = 1835999759;buf[871] = (byte) (t >>> 16);t = 1530498742;buf[872] = (byte) (t >>> 19);t = 1776694471;buf[873] = (byte) (t >>> 21);t = 726383474;buf[874] = (byte) (t >>> 11);t = -2046290492;buf[875] = (byte) (t >>> 6);t = -206273926;buf[876] = (byte) (t >>> 12);t = 380708962;buf[877] = (byte) (t >>> 17);t = 1148518761;buf[878] = (byte) (t >>> 12);t = -1232083309;buf[879] = (byte) (t >>> 4);t = -651898154;buf[880] = (byte) (t >>> 22);t = 766881743;buf[881] = (byte) (t >>> 21);t = 1696667646;buf[882] = (byte) (t >>> 20);t = -1781241349;buf[883] = (byte) (t >>> 9);t = 1663940768;buf[884] = (byte) (t >>> 10);t = -2068389042;buf[885] = (byte) (t >>> 15);t = 790998312;buf[886] = (byte) (t >>> 10);t = 854453025;buf[887] = (byte) (t >>> 24);t = -1111038193;buf[888] = (byte) (t >>> 18);t = 1372780070;buf[889] = (byte) (t >>> 22);t = -1999926591;buf[890] = (byte) (t >>> 6);t = -1381707606;buf[891] = (byte) (t >>> 6);t = 2134725423;buf[892] = (byte) (t >>> 3);t = 1100288678;buf[893] = (byte) (t >>> 3);t = 1717783238;buf[894] = (byte) (t >>> 17);t = -684022317;buf[895] = (byte) (t >>> 9);t = 498215345;buf[896] = (byte) (t >>> 11);t = 70797764;buf[897] = (byte) (t >>> 2);t = -1588989861;buf[898] = (byte) (t >>> 23);t = 1968533529;buf[899] = (byte) (t >>> 24);t = -1452829496;buf[900] = (byte) (t >>> 12);t = 659490602;buf[901] = (byte) (t >>> 16);t = 1447840516;buf[902] = (byte) (t >>> 5);t = 590452161;buf[903] = (byte) (t >>> 15);t = -1800417460;buf[904] = (byte) (t >>> 17);t = 1146530275;buf[905] = (byte) (t >>> 20);t = -477938899;buf[906] = (byte) (t >>> 12);t = 2087624017;buf[907] = (byte) (t >>> 9);t = 757799851;buf[908] = (byte) (t >>> 6);t = 2054395659;buf[909] = (byte) (t >>> 16);t = 1531843556;buf[910] = (byte) (t >>> 22);t = -126709547;buf[911] = (byte) (t >>> 6);t = -1578081477;buf[912] = (byte) (t >>> 3);t = -195302788;buf[913] = (byte) (t >>> 14);t = -2070344999;buf[914] = (byte) (t >>> 14);t = 1724991899;buf[915] = (byte) (t >>> 21);t = 825436570;buf[916] = (byte) (t >>> 15);t = -1934544230;buf[917] = (byte) (t >>> 21);t = -912133940;buf[918] = (byte) (t >>> 2);t = 1918456375;buf[919] = (byte) (t >>> 16);t = -1837931274;buf[920] = (byte) (t >>> 22);t = 1506481352;buf[921] = (byte) (t >>> 11);t = 503003507;buf[922] = (byte) (t >>> 2);t = -19372320;buf[923] = (byte) (t >>> 4);t = -2027654139;buf[924] = (byte) (t >>> 20);t = -582749711;buf[925] = (byte) (t >>> 18);t = -1784953912;buf[926] = (byte) (t >>> 23);t = 54068177;buf[927] = (byte) (t >>> 16);t = -263796377;buf[928] = (byte) (t >>> 9);t = 1821329568;buf[929] = (byte) (t >>> 5);t = -440128511;buf[930] = (byte) (t >>> 7);t = 1729569665;buf[931] = (byte) (t >>> 3);t = -1940033633;buf[932] = (byte) (t >>> 21);t = 356167015;buf[933] = (byte) (t >>> 9);t = -1531778480;buf[934] = (byte) (t >>> 5);t = -1368577717;buf[935] = (byte) (t >>> 7);t = -1425659683;buf[936] = (byte) (t >>> 2);t = -585736292;buf[937] = (byte) (t >>> 14);t = 185955711;buf[938] = (byte) (t >>> 19);t = -578200193;buf[939] = (byte) (t >>> 10);t = 123321238;buf[940] = (byte) (t >>> 7);t = 1070818712;buf[941] = (byte) (t >>> 14);t = 1137369981;buf[942] = (byte) (t >>> 9);t = 147665487;buf[943] = (byte) (t >>> 10);t = -1333469869;buf[944] = (byte) (t >>> 17);t = 591338;buf[945] = (byte) (t >>> 2);t = 1630152315;buf[946] = (byte) (t >>> 4);t = 576429035;buf[947] = (byte) (t >>> 14);t = 952951646;buf[948] = (byte) (t >>> 18);t = -1102274234;buf[949] = (byte) (t >>> 2);t = -2141115313;buf[950] = (byte) (t >>> 10);t = -150043194;buf[951] = (byte) (t >>> 9);t = 1518863984;buf[952] = (byte) (t >>> 4);t = 1331413647;buf[953] = (byte) (t >>> 3);t = 988876852;buf[954] = (byte) (t >>> 10);t = -69909098;buf[955] = (byte) (t >>> 8);t = 1433043878;buf[956] = (byte) (t >>> 20);t = -342378552;buf[957] = (byte) (t >>> 15);t = -312269175;buf[958] = (byte) (t >>> 7);t = 1713519831;buf[959] = (byte) (t >>> 24);t = -767717133;buf[960] = (byte) (t >>> 1);t = 1419950152;buf[961] = (byte) (t >>> 11);t = -1788136594;buf[962] = (byte) (t >>> 11);t = -70613175;buf[963] = (byte) (t >>> 11);t = -1109868112;buf[964] = (byte) (t >>> 3);t = -979307462;buf[965] = (byte) (t >>> 18);t = 1783810858;buf[966] = (byte) (t >>> 5);t = 515887346;buf[967] = (byte) (t >>> 1);t = -1928447536;buf[968] = (byte) (t >>> 22);t = 391750994;buf[969] = (byte) (t >>> 4);t = -1468052825;buf[970] = (byte) (t >>> 21);t = 918801421;buf[971] = (byte) (t >>> 17);t = 1737038652;buf[972] = (byte) (t >>> 7);t = -1169214903;buf[973] = (byte) (t >>> 19);t = -967582092;buf[974] = (byte) (t >>> 21);t = -1364685390;buf[975] = (byte) (t >>> 15);t = 1026146036;buf[976] = (byte) (t >>> 11);t = 1730522781;buf[977] = (byte) (t >>> 10);t = -1029249342;buf[978] = (byte) (t >>> 15);t = -616495029;buf[979] = (byte) (t >>> 5);t = -1518553540;buf[980] = (byte) (t >>> 4);t = 233235406;buf[981] = (byte) (t >>> 22);t = 893213016;buf[982] = (byte) (t >>> 3);t = 1941223436;buf[983] = (byte) (t >>> 12);t = 769227900;buf[984] = (byte) (t >>> 21);t = -1217213565;buf[985] = (byte) (t >>> 23);t = 916664928;buf[986] = (byte) (t >>> 7);t = -945270070;buf[987] = (byte) (t >>> 1);t = -1996822687;buf[988] = (byte) (t >>> 11);t = -842095901;buf[989] = (byte) (t >>> 21);t = -1254260793;buf[990] = (byte) (t >>> 23);t = 218784550;buf[991] = (byte) (t >>> 11);t = -1508535367;buf[992] = (byte) (t >>> 20);t = -1589840100;buf[993] = (byte) (t >>> 4);t = -231465888;buf[994] = (byte) (t >>> 12);t = 1517125337;buf[995] = (byte) (t >>> 24);t = -1408677275;buf[996] = (byte) (t >>> 1);t = 1900528096;buf[997] = (byte) (t >>> 18);t = -1730245230;buf[998] = (byte) (t >>> 22);t = -1916918103;buf[999] = (byte) (t >>> 5);t = -1925718979;buf[1000] = (byte) (t >>> 21);t = 1543218659;buf[1001] = (byte) (t >>> 22);t = 1852799718;buf[1002] = (byte) (t >>> 21);t = -1053194469;buf[1003] = (byte) (t >>> 5);t = -999478372;buf[1004] = (byte) (t >>> 20);t = 948982366;buf[1005] = (byte) (t >>> 8);t = 630187628;buf[1006] = (byte) (t >>> 1);t = -1644592259;buf[1007] = (byte) (t >>> 22);t = 596095960;buf[1008] = (byte) (t >>> 7);t = 1075065999;buf[1009] = (byte) (t >>> 14);t = 936833104;buf[1010] = (byte) (t >>> 23);t = -1204513566;buf[1011] = (byte) (t >>> 1);t = -855776700;buf[1012] = (byte) (t >>> 21);t = 969498167;buf[1013] = (byte) (t >>> 10);t = -472701601;buf[1014] = (byte) (t >>> 4);t = -1672043189;buf[1015] = (byte) (t >>> 9);t = 82890394;buf[1016] = (byte) (t >>> 17);t = 398513562;buf[1017] = (byte) (t >>> 6);t = -1870376767;buf[1018] = (byte) (t >>> 17);t = 912415363;buf[1019] = (byte) (t >>> 8);t = -1367882890;buf[1020] = (byte) (t >>> 16);t = 286732675;buf[1021] = (byte) (t >>> 18);t = -1851288764;buf[1022] = (byte) (t >>> 15);t = 919402749;buf[1023] = (byte) (t >>> 14);t = -1297013034;buf[1024] = (byte) (t >>> 17);t = -1930092099;buf[1025] = (byte) (t >>> 10);t = 1638119870;buf[1026] = (byte) (t >>> 2);t = 428225306;buf[1027] = (byte) (t >>> 4);t = 1509084480;buf[1028] = (byte) (t >>> 5);t = -142664373;buf[1029] = (byte) (t >>> 2);t = -134580792;buf[1030] = (byte) (t >>> 8);t = -1242067672;buf[1031] = (byte) (t >>> 2);t = -1904923364;buf[1032] = (byte) (t >>> 22);t = -774264079;buf[1033] = (byte) (t >>> 11);t = 623684730;buf[1034] = (byte) (t >>> 9);t = 2072295114;buf[1035] = (byte) (t >>> 7);t = 886466934;buf[1036] = (byte) (t >>> 9);t = -978952424;buf[1037] = (byte) (t >>> 13);t = -5563330;buf[1038] = (byte) (t >>> 6);t = 291385968;buf[1039] = (byte) (t >>> 7);t = -1949906882;buf[1040] = (byte) (t >>> 22);t = 1688063086;buf[1041] = (byte) (t >>> 5);t = 501325636;buf[1042] = (byte) (t >>> 11);t = -360083076;buf[1043] = (byte) (t >>> 19);t = 1893418839;buf[1044] = (byte) (t >>> 4);t = 1423035604;buf[1045] = (byte) (t >>> 22);t = 1494930277;buf[1046] = (byte) (t >>> 22);t = -1430453822;buf[1047] = (byte) (t >>> 23);t = -1406552757;buf[1048] = (byte) (t >>> 15);t = -42673362;buf[1049] = (byte) (t >>> 9);t = -80704999;buf[1050] = (byte) (t >>> 15);t = 236568025;buf[1051] = (byte) (t >>> 21);t = 253955109;buf[1052] = (byte) (t >>> 12);t = -973067673;buf[1053] = (byte) (t >>> 7);t = -1050907969;buf[1054] = (byte) (t >>> 16);t = -170467172;buf[1055] = (byte) (t >>> 12);t = 1416130453;buf[1056] = (byte) (t >>> 9);t = -1825777046;buf[1057] = (byte) (t >>> 19);t = 842583225;buf[1058] = (byte) (t >>> 5);t = -1435038389;buf[1059] = (byte) (t >>> 5);t = -213444576;buf[1060] = (byte) (t >>> 7);t = -808464058;buf[1061] = (byte) (t >>> 2);t = -1591541747;buf[1062] = (byte) (t >>> 18);t = -923979929;buf[1063] = (byte) (t >>> 17);t = -665919314;buf[1064] = (byte) (t >>> 9);t = -1963395221;buf[1065] = (byte) (t >>> 4);t = 1558349221;buf[1066] = (byte) (t >>> 9);t = -1939530503;buf[1067] = (byte) (t >>> 6);t = 1259718250;buf[1068] = (byte) (t >>> 4);t = 783153466;buf[1069] = (byte) (t >>> 2);t = 378751432;buf[1070] = (byte) (t >>> 3);t = -928924853;buf[1071] = (byte) (t >>> 17);t = -1371596826;buf[1072] = (byte) (t >>> 7);t = 2057629561;buf[1073] = (byte) (t >>> 15);t = 699393727;buf[1074] = (byte) (t >>> 4);t = 1052349709;buf[1075] = (byte) (t >>> 11);t = -593141932;buf[1076] = (byte) (t >>> 10);t = -975381019;buf[1077] = (byte) (t >>> 9);t = 1187555784;buf[1078] = (byte) (t >>> 17);t = 1221422514;buf[1079] = (byte) (t >>> 14);t = -2063566386;buf[1080] = (byte) (t >>> 8);t = 897749758;buf[1081] = (byte) (t >>> 24);t = -1800247881;buf[1082] = (byte) (t >>> 2);t = 2003247472;buf[1083] = (byte) (t >>> 7);t = 191765998;buf[1084] = (byte) (t >>> 6);t = 506884447;buf[1085] = (byte) (t >>> 3);t = 1902399096;buf[1086] = (byte) (t >>> 18);t = -937247676;buf[1087] = (byte) (t >>> 11);t = 628388407;buf[1088] = (byte) (t >>> 20);t = -1454937245;buf[1089] = (byte) (t >>> 23);t = 1878639774;buf[1090] = (byte) (t >>> 4);t = 1236498540;buf[1091] = (byte) (t >>> 11);t = 1233616522;buf[1092] = (byte) (t >>> 24);t = 1053880909;buf[1093] = (byte) (t >>> 17);t = -2118899360;buf[1094] = (byte) (t >>> 5);t = -2044405485;buf[1095] = (byte) (t >>> 2);t = -2065536462;buf[1096] = (byte) (t >>> 8);t = 1430601761;buf[1097] = (byte) (t >>> 24);t = 907391142;buf[1098] = (byte) (t >>> 23);t = -557179128;buf[1099] = (byte) (t >>> 11);t = -799537767;buf[1100] = (byte) (t >>> 22);t = 1121546104;buf[1101] = (byte) (t >>> 14);t = -859688704;buf[1102] = (byte) (t >>> 17);t = 358827791;buf[1103] = (byte) (t >>> 5);t = 1503056016;buf[1104] = (byte) (t >>> 19);t = 1420601584;buf[1105] = (byte) (t >>> 22);t = -2021556597;buf[1106] = (byte) (t >>> 1);t = -1698255730;buf[1107] = (byte) (t >>> 9);t = 2044595531;buf[1108] = (byte) (t >>> 6);t = 314068234;buf[1109] = (byte) (t >>> 5);t = 1391824026;buf[1110] = (byte) (t >>> 12);t = 1755861337;buf[1111] = (byte) (t >>> 24);t = 617181539;buf[1112] = (byte) (t >>> 20);t = -1529537077;buf[1113] = (byte) (t >>> 10);t = 1977853995;buf[1114] = (byte) (t >>> 18);t = -1280804472;buf[1115] = (byte) (t >>> 23);t = -1562358702;buf[1116] = (byte) (t >>> 8);t = 406282491;buf[1117] = (byte) (t >>> 15);t = -1449931696;buf[1118] = (byte) (t >>> 5);t = -204251596;buf[1119] = (byte) (t >>> 12);t = 1303413432;buf[1120] = (byte) (t >>> 1);t = 1396115177;buf[1121] = (byte) (t >>> 15);t = -124765846;buf[1122] = (byte) (t >>> 8);t = -1677181763;buf[1123] = (byte) (t >>> 7);t = 1682016927;buf[1124] = (byte) (t >>> 24);t = -896440892;buf[1125] = (byte) (t >>> 8);t = 2048266897;buf[1126] = (byte) (t >>> 1);t = -1882950474;buf[1127] = (byte) (t >>> 4);t = -1423568732;buf[1128] = (byte) (t >>> 1);t = -748139325;buf[1129] = (byte) (t >>> 16);t = 1459752894;buf[1130] = (byte) (t >>> 24);t = 341276983;buf[1131] = (byte) (t >>> 22);t = 658089442;buf[1132] = (byte) (t >>> 7);t = 1426798751;buf[1133] = (byte) (t >>> 18);t = 1741425295;buf[1134] = (byte) (t >>> 24);t = 1369608123;buf[1135] = (byte) (t >>> 11);t = 461014781;buf[1136] = (byte) (t >>> 9);t = 374073364;buf[1137] = (byte) (t >>> 16);t = 2037488038;buf[1138] = (byte) (t >>> 3);t = 473937216;buf[1139] = (byte) (t >>> 4);t = 976345229;buf[1140] = (byte) (t >>> 10);t = -525261724;buf[1141] = (byte) (t >>> 15);t = -1100815333;buf[1142] = (byte) (t >>> 16);t = -134009095;buf[1143] = (byte) (t >>> 11);t = 1909522250;buf[1144] = (byte) (t >>> 18);t = 1386607059;buf[1145] = (byte) (t >>> 24);t = 1554533734;buf[1146] = (byte) (t >>> 22);t = -1253040289;buf[1147] = (byte) (t >>> 20);t = 939457616;buf[1148] = (byte) (t >>> 23);t = 274451500;buf[1149] = (byte) (t >>> 11);t = 1549563301;buf[1150] = (byte) (t >>> 8);t = 285333676;buf[1151] = (byte) (t >>> 22);t = 62679072;buf[1152] = (byte) (t >>> 9);t = 10989954;buf[1153] = (byte) (t >>> 3);t = 2056346940;buf[1154] = (byte) (t >>> 24);t = -120543650;buf[1155] = (byte) (t >>> 17);t = -447598435;buf[1156] = (byte) (t >>> 7);t = -468277424;buf[1157] = (byte) (t >>> 14);t = 286420624;buf[1158] = (byte) (t >>> 5);t = 1215711418;buf[1159] = (byte) (t >>> 24);t = -710707696;buf[1160] = (byte) (t >>> 12);t = 1191523778;buf[1161] = (byte) (t >>> 20);t = -1553896661;buf[1162] = (byte) (t >>> 23);t = 1083955527;buf[1163] = (byte) (t >>> 14);t = 2047637157;buf[1164] = (byte) (t >>> 14);t = 84863145;buf[1165] = (byte) (t >>> 5);t = -1203553028;buf[1166] = (byte) (t >>> 23);t = 2127638351;buf[1167] = (byte) (t >>> 10);t = -106173644;buf[1168] = (byte) (t >>> 3);t = -909520092;buf[1169] = (byte) (t >>> 21);t = -375271916;buf[1170] = (byte) (t >>> 10);t = 782652497;buf[1171] = (byte) (t >>> 4);t = 96049880;buf[1172] = (byte) (t >>> 11);t = 314904013;buf[1173] = (byte) (t >>> 2);t = 220502153;buf[1174] = (byte) (t >>> 12);t = -1680209710;buf[1175] = (byte) (t >>> 23);t = -1264938301;buf[1176] = (byte) (t >>> 11);t = -157367054;buf[1177] = (byte) (t >>> 1);t = 389879574;buf[1178] = (byte) (t >>> 15);t = -189016009;buf[1179] = (byte) (t >>> 15);t = -1639181848;buf[1180] = (byte) (t >>> 22);t = -1583984096;buf[1181] = (byte) (t >>> 14);t = -1564551783;buf[1182] = (byte) (t >>> 19);t = -1106681364;buf[1183] = (byte) (t >>> 10);t = 1779906536;buf[1184] = (byte) (t >>> 19);t = 1073984932;buf[1185] = (byte) (t >>> 2);t = -1415954027;buf[1186] = (byte) (t >>> 21);t = -1120346736;buf[1187] = (byte) (t >>> 9);t = -249080762;buf[1188] = (byte) (t >>> 12);t = -428795600;buf[1189] = (byte) (t >>> 21);t = 1229555974;buf[1190] = (byte) (t >>> 2);t = 1254986631;buf[1191] = (byte) (t >>> 13);t = -2054007061;buf[1192] = (byte) (t >>> 18);t = -2085924515;buf[1193] = (byte) (t >>> 19);t = 1545931567;buf[1194] = (byte) (t >>> 10);t = 228860279;buf[1195] = (byte) (t >>> 12);t = -1520007006;buf[1196] = (byte) (t >>> 12);t = 861439000;buf[1197] = (byte) (t >>> 16);t = -2041308537;buf[1198] = (byte) (t >>> 7);t = 780950283;buf[1199] = (byte) (t >>> 3);t = -1657474147;buf[1200] = (byte) (t >>> 22);t = -1732521888;buf[1201] = (byte) (t >>> 23);t = 510233543;buf[1202] = (byte) (t >>> 13);t = -26375605;buf[1203] = (byte) (t >>> 3);t = 1933796670;buf[1204] = (byte) (t >>> 4);t = -108459085;buf[1205] = (byte) (t >>> 19);t = 135076390;buf[1206] = (byte) (t >>> 10);t = 271442210;buf[1207] = (byte) (t >>> 13);t = 1134438415;buf[1208] = (byte) (t >>> 14);t = -2127323815;buf[1209] = (byte) (t >>> 6);t = -9153155;buf[1210] = (byte) (t >>> 12);t = 1989113423;buf[1211] = (byte) (t >>> 8);t = 1941387611;buf[1212] = (byte) (t >>> 19);t = -897445672;buf[1213] = (byte) (t >>> 1);t = 1734589388;buf[1214] = (byte) (t >>> 7);t = -919363532;buf[1215] = (byte) (t >>> 16);t = 1725150354;buf[1216] = (byte) (t >>> 24);t = 1420924738;buf[1217] = (byte) (t >>> 22);t = -1590434637;buf[1218] = (byte) (t >>> 23);t = -795994606;buf[1219] = (byte) (t >>> 6);t = -2097989031;buf[1220] = (byte) (t >>> 7);t = 864129444;buf[1221] = (byte) (t >>> 10);t = -1093819655;buf[1222] = (byte) (t >>> 7);t = 917401067;buf[1223] = (byte) (t >>> 20);t = -802419832;buf[1224] = (byte) (t >>> 3);t = 1040323156;buf[1225] = (byte) (t >>> 6);t = 1346296165;buf[1226] = (byte) (t >>> 2);t = -1824056697;buf[1227] = (byte) (t >>> 7);t = -1117272422;buf[1228] = (byte) (t >>> 16);t = 1361183404;buf[1229] = (byte) (t >>> 3);t = 1396280688;buf[1230] = (byte) (t >>> 16);t = -1432359824;buf[1231] = (byte) (t >>> 21);t = -921930160;buf[1232] = (byte) (t >>> 21);t = -2085648888;buf[1233] = (byte) (t >>> 19);t = -1359451615;buf[1234] = (byte) (t >>> 5);t = 658316725;buf[1235] = (byte) (t >>> 6);t = -1064926426;buf[1236] = (byte) (t >>> 3);t = -982578486;buf[1237] = (byte) (t >>> 13);t = -164026063;buf[1238] = (byte) (t >>> 15);t = -1938444629;buf[1239] = (byte) (t >>> 5);t = 836025197;buf[1240] = (byte) (t >>> 12);t = -543128618;buf[1241] = (byte) (t >>> 15);t = 940005595;buf[1242] = (byte) (t >>> 23);t = -1646187545;buf[1243] = (byte) (t >>> 22);t = 1768823400;buf[1244] = (byte) (t >>> 7);t = 1261889367;buf[1245] = (byte) (t >>> 19);t = -10180228;buf[1246] = (byte) (t >>> 12);t = 570810821;buf[1247] = (byte) (t >>> 23);t = 74356831;buf[1248] = (byte) (t >>> 16);t = 1165175673;buf[1249] = (byte) (t >>> 12);t = 1892747064;buf[1250] = (byte) (t >>> 5);t = 2087101130;buf[1251] = (byte) (t >>> 1);t = 1030754710;buf[1252] = (byte) (t >>> 18);t = -932302100;buf[1253] = (byte) (t >>> 16);t = 216213204;buf[1254] = (byte) (t >>> 21);t = 1697409576;buf[1255] = (byte) (t >>> 13);t = 1388002487;buf[1256] = (byte) (t >>> 22);t = -1456232312;buf[1257] = (byte) (t >>> 5);t = 1112628922;buf[1258] = (byte) (t >>> 6);t = 1687696183;buf[1259] = (byte) (t >>> 24);t = -1270147892;buf[1260] = (byte) (t >>> 11);t = -845033437;buf[1261] = (byte) (t >>> 21);t = -1565344776;buf[1262] = (byte) (t >>> 17);t = 1351804541;buf[1263] = (byte) (t >>> 17);t = 886037830;buf[1264] = (byte) (t >>> 6);t = -634334308;buf[1265] = (byte) (t >>> 4);t = -1260309471;buf[1266] = (byte) (t >>> 18);t = -1573303832;buf[1267] = (byte) (t >>> 16);t = 1768525689;buf[1268] = (byte) (t >>> 24);t = -753976876;buf[1269] = (byte) (t >>> 22);t = 292845034;buf[1270] = (byte) (t >>> 22);t = 418776620;buf[1271] = (byte) (t >>> 23);t = 1347062045;buf[1272] = (byte) (t >>> 13);t = -952919216;buf[1273] = (byte) (t >>> 12);t = 1192857966;buf[1274] = (byte) (t >>> 21);t = -1046496449;buf[1275] = (byte) (t >>> 18);t = 1493044941;buf[1276] = (byte) (t >>> 1);t = 1241010968;buf[1277] = (byte) (t >>> 21);t = 1759408531;buf[1278] = (byte) (t >>> 2);t = 1450517726;buf[1279] = (byte) (t >>> 24);t = -729683058;buf[1280] = (byte) (t >>> 10);t = 310528379;buf[1281] = (byte) (t >>> 22);t = 541583427;buf[1282] = (byte) (t >>> 16);t = 1957121799;buf[1283] = (byte) (t >>> 17);t = 350846526;buf[1284] = (byte) (t >>> 11);t = -836572055;buf[1285] = (byte) (t >>> 5);t = 985737132;buf[1286] = (byte) (t >>> 4);t = -1693197699;buf[1287] = (byte) (t >>> 23);t = 118364712;buf[1288] = (byte) (t >>> 13);t = 834105943;buf[1289] = (byte) (t >>> 15);t = 1585429696;buf[1290] = (byte) (t >>> 7);t = -1566495416;buf[1291] = (byte) (t >>> 7);t = 1007023992;buf[1292] = (byte) (t >>> 13);t = 649507327;buf[1293] = (byte) (t >>> 7);t = -213359522;buf[1294] = (byte) (t >>> 19);t = 1269861170;buf[1295] = (byte) (t >>> 15);t = -1362962978;buf[1296] = (byte) (t >>> 2);t = 1843131293;buf[1297] = (byte) (t >>> 24);t = 1266478919;buf[1298] = (byte) (t >>> 19);t = -1390796214;buf[1299] = (byte) (t >>> 23);t = -956202386;buf[1300] = (byte) (t >>> 21);t = -1475442925;buf[1301] = (byte) (t >>> 23);t = 1852862609;buf[1302] = (byte) (t >>> 21);t = -421419163;buf[1303] = (byte) (t >>> 10);t = 1860330336;buf[1304] = (byte) (t >>> 9);t = 359402310;buf[1305] = (byte) (t >>> 20);t = -591106184;buf[1306] = (byte) (t >>> 17);t = -1483307368;buf[1307] = (byte) (t >>> 12);t = 590382496;buf[1308] = (byte) (t >>> 2);t = 1470927518;buf[1309] = (byte) (t >>> 15);t = 2118653016;buf[1310] = (byte) (t >>> 16);t = -985281601;buf[1311] = (byte) (t >>> 18);t = -164344993;buf[1312] = (byte) (t >>> 8);t = 606955402;buf[1313] = (byte) (t >>> 20);t = 1838461876;buf[1314] = (byte) (t >>> 7);t = 758129274;buf[1315] = (byte) (t >>> 7);t = -338442868;buf[1316] = (byte) (t >>> 2);t = -1647736592;buf[1317] = (byte) (t >>> 1);t = -153951694;buf[1318] = (byte) (t >>> 11);t = 323186986;buf[1319] = (byte) (t >>> 11);t = 849653196;buf[1320] = (byte) (t >>> 5);t = 2095912675;buf[1321] = (byte) (t >>> 13);t = 1824941597;buf[1322] = (byte) (t >>> 21);t = 1221736569;buf[1323] = (byte) (t >>> 11);t = -331259170;buf[1324] = (byte) (t >>> 21);t = 1302793827;buf[1325] = (byte) (t >>> 1);t = 477179010;buf[1326] = (byte) (t >>> 5);t = 1776310991;buf[1327] = (byte) (t >>> 6);t = -95662344;buf[1328] = (byte) (t >>> 16);t = 1983199242;buf[1329] = (byte) (t >>> 16);t = 757340787;buf[1330] = (byte) (t >>> 1);t = -1785969571;buf[1331] = (byte) (t >>> 20);t = 1966641453;buf[1332] = (byte) (t >>> 16);t = -1476450362;buf[1333] = (byte) (t >>> 7);t = -1500950169;buf[1334] = (byte) (t >>> 20);t = -1322246122;buf[1335] = (byte) (t >>> 23);t = -592637644;buf[1336] = (byte) (t >>> 6);t = 717507064;buf[1337] = (byte) (t >>> 23);t = 1762325304;buf[1338] = (byte) (t >>> 24);t = -1083590607;buf[1339] = (byte) (t >>> 11);t = 4120235;buf[1340] = (byte) (t >>> 9);t = 1645487144;buf[1341] = (byte) (t >>> 24);t = -1133018365;buf[1342] = (byte) (t >>> 23);t = -726042455;buf[1343] = (byte) (t >>> 15);t = 128334866;buf[1344] = (byte) (t >>> 7);t = -1845822491;buf[1345] = (byte) (t >>> 22);t = 687073844;buf[1346] = (byte) (t >>> 3);t = 1530454461;buf[1347] = (byte) (t >>> 10);t = 1722329804;buf[1348] = (byte) (t >>> 9);t = -304974273;buf[1349] = (byte) (t >>> 14);t = 1384974135;buf[1350] = (byte) (t >>> 19);t = -292509072;buf[1351] = (byte) (t >>> 9);t = 2054608738;buf[1352] = (byte) (t >>> 16);t = -1613652661;buf[1353] = (byte) (t >>> 14);t = 2098902224;buf[1354] = (byte) (t >>> 7);t = -840529142;buf[1355] = (byte) (t >>> 21);t = -786463585;buf[1356] = (byte) (t >>> 4);t = -2056618845;buf[1357] = (byte) (t >>> 18);t = 128413380;buf[1358] = (byte) (t >>> 8);t = 1218975765;buf[1359] = (byte) (t >>> 7);t = -312119198;buf[1360] = (byte) (t >>> 1);t = 630357475;buf[1361] = (byte) (t >>> 11);t = -1052155010;buf[1362] = (byte) (t >>> 3);t = -1072673824;buf[1363] = (byte) (t >>> 6);t = -1513428490;buf[1364] = (byte) (t >>> 9);t = -1380687059;buf[1365] = (byte) (t >>> 8);t = 1634095275;buf[1366] = (byte) (t >>> 13);t = -1555216096;buf[1367] = (byte) (t >>> 5);t = 675623067;buf[1368] = (byte) (t >>> 7);t = -2066952794;buf[1369] = (byte) (t >>> 14);t = -1071231698;buf[1370] = (byte) (t >>> 5);t = -2055178166;buf[1371] = (byte) (t >>> 8);t = 141909793;buf[1372] = (byte) (t >>> 16);t = -1940076041;buf[1373] = (byte) (t >>> 9);t = 56510509;buf[1374] = (byte) (t >>> 14);t = -34470213;buf[1375] = (byte) (t >>> 4);t = -1328225641;buf[1376] = (byte) (t >>> 1);t = -730630963;buf[1377] = (byte) (t >>> 11);t = -1771640150;buf[1378] = (byte) (t >>> 12);t = -797236208;buf[1379] = (byte) (t >>> 4);t = 151908146;buf[1380] = (byte) (t >>> 21);t = 1151307097;buf[1381] = (byte) (t >>> 17);t = 1428411242;buf[1382] = (byte) (t >>> 4);t = 458791566;buf[1383] = (byte) (t >>> 16);t = 140924714;buf[1384] = (byte) (t >>> 6);t = -1682867344;buf[1385] = (byte) (t >>> 3);t = -68314087;buf[1386] = (byte) (t >>> 6);t = 1172904763;buf[1387] = (byte) (t >>> 13);t = -1059156774;buf[1388] = (byte) (t >>> 9);t = -1579142364;buf[1389] = (byte) (t >>> 3);t = -1772221627;buf[1390] = (byte) (t >>> 20);t = -1177301053;buf[1391] = (byte) (t >>> 18);t = -415520845;buf[1392] = (byte) (t >>> 21);t = -308208791;buf[1393] = (byte) (t >>> 18);t = 1085652427;buf[1394] = (byte) (t >>> 2);t = 511957396;buf[1395] = (byte) (t >>> 6);t = 596513509;buf[1396] = (byte) (t >>> 1);t = 2042178569;buf[1397] = (byte) (t >>> 7);t = -1464473955;buf[1398] = (byte) (t >>> 21);t = -1956206526;buf[1399] = (byte) (t >>> 5);t = 483687445;buf[1400] = (byte) (t >>> 18);t = -2064986991;buf[1401] = (byte) (t >>> 17);t = -1685967654;buf[1402] = (byte) (t >>> 22);t = -1852493020;buf[1403] = (byte) (t >>> 10);t = -189265167;buf[1404] = (byte) (t >>> 20);t = -1989302711;buf[1405] = (byte) (t >>> 10);t = -1656869867;buf[1406] = (byte) (t >>> 22);t = 691334549;buf[1407] = (byte) (t >>> 12);t = 865780511;buf[1408] = (byte) (t >>> 14);t = -717168512;buf[1409] = (byte) (t >>> 20);t = -1042781602;buf[1410] = (byte) (t >>> 1);t = -2098151160;buf[1411] = (byte) (t >>> 2);t = -2140084221;buf[1412] = (byte) (t >>> 9);t = -1376658298;buf[1413] = (byte) (t >>> 21);t = -2007699156;buf[1414] = (byte) (t >>> 21);t = -1494298172;buf[1415] = (byte) (t >>> 17);t = -1623741212;buf[1416] = (byte) (t >>> 7);t = 351561905;buf[1417] = (byte) (t >>> 20);t = 623993017;buf[1418] = (byte) (t >>> 18);t = 469032781;buf[1419] = (byte) (t >>> 10);t = -1909668881;buf[1420] = (byte) (t >>> 21);t = -1050686248;buf[1421] = (byte) (t >>> 19);t = -1058231130;buf[1422] = (byte) (t >>> 1);t = 1856670937;buf[1423] = (byte) (t >>> 6);t = 2104134068;buf[1424] = (byte) (t >>> 11);t = 1723189627;buf[1425] = (byte) (t >>> 5);t = 291265246;buf[1426] = (byte) (t >>> 22);t = 959916855;buf[1427] = (byte) (t >>> 12);t = 1491611303;buf[1428] = (byte) (t >>> 24);t = -1911437543;buf[1429] = (byte) (t >>> 22);t = -1263088770;buf[1430] = (byte) (t >>> 15);t = -2002508804;buf[1431] = (byte) (t >>> 7);t = 288634144;buf[1432] = (byte) (t >>> 16);t = 1254313463;buf[1433] = (byte) (t >>> 8);t = 1433899837;buf[1434] = (byte) (t >>> 12);t = -709912275;buf[1435] = (byte) (t >>> 2);t = 1831609947;buf[1436] = (byte) (t >>> 24);t = -1772760224;buf[1437] = (byte) (t >>> 10);t = 435238651;buf[1438] = (byte) (t >>> 23);t = -2062657001;buf[1439] = (byte) (t >>> 13);t = 221418233;buf[1440] = (byte) (t >>> 22);t = -1790740928;buf[1441] = (byte) (t >>> 8);t = 310644024;buf[1442] = (byte) (t >>> 19);t = -1485897369;buf[1443] = (byte) (t >>> 23);t = 1452290726;buf[1444] = (byte) (t >>> 3);t = 1665446408;buf[1445] = (byte) (t >>> 24);t = -1153831269;buf[1446] = (byte) (t >>> 15);t = -1641449390;buf[1447] = (byte) (t >>> 11);t = -116299567;buf[1448] = (byte) (t >>> 10);t = 1439836973;buf[1449] = (byte) (t >>> 8);t = -443349645;buf[1450] = (byte) (t >>> 2);t = 164859358;buf[1451] = (byte) (t >>> 5);t = 1416813758;buf[1452] = (byte) (t >>> 24);t = 1728654017;buf[1453] = (byte) (t >>> 3);t = 780899105;buf[1454] = (byte) (t >>> 21);t = -711437993;buf[1455] = (byte) (t >>> 20);t = 1999719031;buf[1456] = (byte) (t >>> 20);t = -94849415;buf[1457] = (byte) (t >>> 19);t = 461022915;buf[1458] = (byte) (t >>> 22);t = -986203509;buf[1459] = (byte) (t >>> 15);t = -1995671793;buf[1460] = (byte) (t >>> 13);t = 1859772701;buf[1461] = (byte) (t >>> 21);t = -521608308;buf[1462] = (byte) (t >>> 9);t = 221190798;buf[1463] = (byte) (t >>> 18);t = 1288687776;buf[1464] = (byte) (t >>> 1);t = 986256667;buf[1465] = (byte) (t >>> 10);t = -850118826;buf[1466] = (byte) (t >>> 7);t = 476071191;buf[1467] = (byte) (t >>> 8);t = 1652389152;buf[1468] = (byte) (t >>> 2);t = -1798816901;buf[1469] = (byte) (t >>> 18);t = 1751494943;buf[1470] = (byte) (t >>> 24);t = -1933879903;buf[1471] = (byte) (t >>> 15);t = -289969739;buf[1472] = (byte) (t >>> 8);t = -1068816052;buf[1473] = (byte) (t >>> 16);t = 555085704;buf[1474] = (byte) (t >>> 18);t = 1222480015;buf[1475] = (byte) (t >>> 1);t = -1351382557;buf[1476] = (byte) (t >>> 12);t = 1402186782;buf[1477] = (byte) (t >>> 11);t = 1705026905;buf[1478] = (byte) (t >>> 9);t = 690071033;buf[1479] = (byte) (t >>> 7);t = 2040197679;buf[1480] = (byte) (t >>> 24);t = 2040935559;buf[1481] = (byte) (t >>> 8);t = 1131001467;buf[1482] = (byte) (t >>> 7);t = 1429345296;buf[1483] = (byte) (t >>> 22);t = -272983160;buf[1484] = (byte) (t >>> 9);t = -1307808609;buf[1485] = (byte) (t >>> 19);t = -1926120452;buf[1486] = (byte) (t >>> 11);t = 1130587454;buf[1487] = (byte) (t >>> 11);t = -306590148;buf[1488] = (byte) (t >>> 15);t = 443779267;buf[1489] = (byte) (t >>> 5);t = 1356354183;buf[1490] = (byte) (t >>> 3);t = 1483777744;buf[1491] = (byte) (t >>> 16);t = -1002364797;buf[1492] = (byte) (t >>> 1);t = 116764373;buf[1493] = (byte) (t >>> 20);t = -461741303;buf[1494] = (byte) (t >>> 20);t = 555461209;buf[1495] = (byte) (t >>> 23);t = -548135186;buf[1496] = (byte) (t >>> 12);t = 1282754874;buf[1497] = (byte) (t >>> 8);t = 416861349;buf[1498] = (byte) (t >>> 6);t = -1039240014;buf[1499] = (byte) (t >>> 19);t = 699136799;buf[1500] = (byte) (t >>> 3);t = 1147964635;buf[1501] = (byte) (t >>> 17);t = 740270533;buf[1502] = (byte) (t >>> 2);t = 1314284148;buf[1503] = (byte) (t >>> 12);t = 648312212;buf[1504] = (byte) (t >>> 21);t = 1836819253;buf[1505] = (byte) (t >>> 4);t = -821892897;buf[1506] = (byte) (t >>> 5);t = 82730112;buf[1507] = (byte) (t >>> 17);t = 1859879794;buf[1508] = (byte) (t >>> 11);t = -186087159;buf[1509] = (byte) (t >>> 13);t = -1200212427;buf[1510] = (byte) (t >>> 4);t = -587756872;buf[1511] = (byte) (t >>> 5);t = 1377221056;buf[1512] = (byte) (t >>> 24);t = -173201572;buf[1513] = (byte) (t >>> 4);t = 206941853;buf[1514] = (byte) (t >>> 16);t = 597460675;buf[1515] = (byte) (t >>> 4);t = 951614604;buf[1516] = (byte) (t >>> 17);t = -528355912;buf[1517] = (byte) (t >>> 2);t = -216528758;buf[1518] = (byte) (t >>> 1);t = 1648817805;buf[1519] = (byte) (t >>> 4);t = -892298940;buf[1520] = (byte) (t >>> 21);t = -571611335;buf[1521] = (byte) (t >>> 10);t = -1767419519;buf[1522] = (byte) (t >>> 20);t = 1505253956;buf[1523] = (byte) (t >>> 15);t = 599987981;buf[1524] = (byte) (t >>> 4);t = 323067306;buf[1525] = (byte) (t >>> 2);t = 1780729397;buf[1526] = (byte) (t >>> 15);t = -1512896100;buf[1527] = (byte) (t >>> 5);t = -1723078426;buf[1528] = (byte) (t >>> 1);t = -1619499627;buf[1529] = (byte) (t >>> 9);t = -1736798805;buf[1530] = (byte) (t >>> 9);t = -876137716;buf[1531] = (byte) (t >>> 12);t = -710552392;buf[1532] = (byte) (t >>> 10);t = -749443650;buf[1533] = (byte) (t >>> 16);t = -1394256862;buf[1534] = (byte) (t >>> 21);t = -243378481;buf[1535] = (byte) (t >>> 9);t = 1951638693;buf[1536] = (byte) (t >>> 4);t = -1512474551;buf[1537] = (byte) (t >>> 11);t = -2002126980;buf[1538] = (byte) (t >>> 21);t = 487557988;buf[1539] = (byte) (t >>> 22);t = -1916097429;buf[1540] = (byte) (t >>> 9);t = -1801345273;buf[1541] = (byte) (t >>> 15);t = 1859906496;buf[1542] = (byte) (t >>> 24);t = 1409136015;buf[1543] = (byte) (t >>> 4);t = 1318835140;buf[1544] = (byte) (t >>> 11);t = 1276278990;buf[1545] = (byte) (t >>> 11);t = -1239666282;buf[1546] = (byte) (t >>> 14);t = -876467499;buf[1547] = (byte) (t >>> 7);t = -2022798326;buf[1548] = (byte) (t >>> 16);t = 308825590;buf[1549] = (byte) (t >>> 17);t = -1553836082;buf[1550] = (byte) (t >>> 8);t = -1746649468;buf[1551] = (byte) (t >>> 23);t = 1947357390;buf[1552] = (byte) (t >>> 6);t = -1107608356;buf[1553] = (byte) (t >>> 11);t = 161206489;buf[1554] = (byte) (t >>> 19);t = 714153769;buf[1555] = (byte) (t >>> 19);t = -1067096188;buf[1556] = (byte) (t >>> 13);t = 577916258;buf[1557] = (byte) (t >>> 8);t = -1815897778;buf[1558] = (byte) (t >>> 19);t = 1829423895;buf[1559] = (byte) (t >>> 21);t = 1261293060;buf[1560] = (byte) (t >>> 10);t = 1460775533;buf[1561] = (byte) (t >>> 14);t = 515981531;buf[1562] = (byte) (t >>> 2);t = -194159421;buf[1563] = (byte) (t >>> 2);t = 1691878667;buf[1564] = (byte) (t >>> 24);t = -752745370;buf[1565] = (byte) (t >>> 15);t = -1319744127;buf[1566] = (byte) (t >>> 16);t = 2093848818;buf[1567] = (byte) (t >>> 17);t = 140891832;buf[1568] = (byte) (t >>> 6);t = 1368360890;buf[1569] = (byte) (t >>> 22);t = -19759712;buf[1570] = (byte) (t >>> 17);t = 21424345;buf[1571] = (byte) (t >>> 9);t = -1295101681;buf[1572] = (byte) (t >>> 14);t = -74570532;buf[1573] = (byte) (t >>> 1);t = 1413277672;buf[1574] = (byte) (t >>> 20);t = 61249790;buf[1575] = (byte) (t >>> 13);t = -2041549703;buf[1576] = (byte) (t >>> 9);t = 316795708;buf[1577] = (byte) (t >>> 22);t = -2124639125;buf[1578] = (byte) (t >>> 1);t = 11984520;buf[1579] = (byte) (t >>> 9);t = -2102790590;buf[1580] = (byte) (t >>> 13);t = 741239512;buf[1581] = (byte) (t >>> 23);t = 1552264516;buf[1582] = (byte) (t >>> 24);t = -202585229;buf[1583] = (byte) (t >>> 3);t = -1283590440;buf[1584] = (byte) (t >>> 1);t = -1503142997;buf[1585] = (byte) (t >>> 16);t = 1100238680;buf[1586] = (byte) (t >>> 5);t = -2009661507;buf[1587] = (byte) (t >>> 21);t = -2070615243;buf[1588] = (byte) (t >>> 5);t = -2047550281;buf[1589] = (byte) (t >>> 21);t = 993105046;buf[1590] = (byte) (t >>> 16);t = -1524219765;buf[1591] = (byte) (t >>> 15);t = 1187389611;buf[1592] = (byte) (t >>> 13);t = -1273289855;buf[1593] = (byte) (t >>> 7);t = 883784921;buf[1594] = (byte) (t >>> 15);t = 881124354;buf[1595] = (byte) (t >>> 23);t = -2055418312;buf[1596] = (byte) (t >>> 20);t = 1311614738;buf[1597] = (byte) (t >>> 13);t = 1985500046;buf[1598] = (byte) (t >>> 4);t = -577419490;buf[1599] = (byte) (t >>> 18);t = -1433769859;buf[1600] = (byte) (t >>> 19);t = 2110893271;buf[1601] = (byte) (t >>> 5);t = -894041767;buf[1602] = (byte) (t >>> 3);t = -1931911394;buf[1603] = (byte) (t >>> 10);t = 180821405;buf[1604] = (byte) (t >>> 17);t = -509645339;buf[1605] = (byte) (t >>> 2);t = 179726943;buf[1606] = (byte) (t >>> 9);t = 674504197;buf[1607] = (byte) (t >>> 23);t = 1455321219;buf[1608] = (byte) (t >>> 1);t = -167325306;buf[1609] = (byte) (t >>> 12);t = -542419830;buf[1610] = (byte) (t >>> 8);t = 156010267;buf[1611] = (byte) (t >>> 16);t = 1681568117;buf[1612] = (byte) (t >>> 9);t = -1037131042;buf[1613] = (byte) (t >>> 13);t = -1976009192;buf[1614] = (byte) (t >>> 16);t = 1375556261;buf[1615] = (byte) (t >>> 1);t = -204098583;buf[1616] = (byte) (t >>> 7);t = 1030282726;buf[1617] = (byte) (t >>> 16);t = -95882052;buf[1618] = (byte) (t >>> 13);t = -1716108093;buf[1619] = (byte) (t >>> 4);t = 596469168;buf[1620] = (byte) (t >>> 20);t = -1127856191;buf[1621] = (byte) (t >>> 18);t = -169013172;buf[1622] = (byte) (t >>> 13);t = -1341304618;buf[1623] = (byte) (t >>> 2);t = 529512756;buf[1624] = (byte) (t >>> 2);t = 1771719826;buf[1625] = (byte) (t >>> 19);t = -1654229695;buf[1626] = (byte) (t >>> 8);t = 1869622045;buf[1627] = (byte) (t >>> 24);t = -172138368;buf[1628] = (byte) (t >>> 15);t = 827573251;buf[1629] = (byte) (t >>> 11);t = 1144879418;buf[1630] = (byte) (t >>> 24);t = 773581938;buf[1631] = (byte) (t >>> 5);t = -1684644507;buf[1632] = (byte) (t >>> 2);t = -1913511359;buf[1633] = (byte) (t >>> 7);t = -1439670407;buf[1634] = (byte) (t >>> 16);t = 1406195827;buf[1635] = (byte) (t >>> 24);t = 1592519182;buf[1636] = (byte) (t >>> 3);t = 1640674813;buf[1637] = (byte) (t >>> 11);t = -1464321436;buf[1638] = (byte) (t >>> 7);t = -361234459;buf[1639] = (byte) (t >>> 19);t = 804987771;buf[1640] = (byte) (t >>> 4);t = -1315683860;buf[1641] = (byte) (t >>> 23);t = 397195525;buf[1642] = (byte) (t >>> 20);t = -1258474161;buf[1643] = (byte) (t >>> 20);t = -1515051470;buf[1644] = (byte) (t >>> 4);t = 1047751752;buf[1645] = (byte) (t >>> 11);t = 305578891;buf[1646] = (byte) (t >>> 16);t = -1025580836;buf[1647] = (byte) (t >>> 1);t = -1436761351;buf[1648] = (byte) (t >>> 16);t = 1994901966;buf[1649] = (byte) (t >>> 20);t = 1995307463;buf[1650] = (byte) (t >>> 17);t = 292945060;buf[1651] = (byte) (t >>> 1);t = 1453048854;buf[1652] = (byte) (t >>> 17);t = -316568130;buf[1653] = (byte) (t >>> 15);t = -1290230608;buf[1654] = (byte) (t >>> 19);t = 1190664410;buf[1655] = (byte) (t >>> 7);t = -1434591889;buf[1656] = (byte) (t >>> 19);t = 1423180430;buf[1657] = (byte) (t >>> 3);t = 462219743;buf[1658] = (byte) (t >>> 14);t = -1586082056;buf[1659] = (byte) (t >>> 16);t = 1419837474;buf[1660] = (byte) (t >>> 17);t = -360264841;buf[1661] = (byte) (t >>> 9);t = 880666772;buf[1662] = (byte) (t >>> 5);t = 914928782;buf[1663] = (byte) (t >>> 17);t = 819226798;buf[1664] = (byte) (t >>> 12);t = 520704214;buf[1665] = (byte) (t >>> 13);t = -1009280081;buf[1666] = (byte) (t >>> 19);t = -1999331559;buf[1667] = (byte) (t >>> 12);t = 1920915320;buf[1668] = (byte) (t >>> 19);t = -1430034739;buf[1669] = (byte) (t >>> 19);t = -923998409;buf[1670] = (byte) (t >>> 13);t = 1720879325;buf[1671] = (byte) (t >>> 9);t = 469125512;buf[1672] = (byte) (t >>> 2);t = 233883263;buf[1673] = (byte) (t >>> 4);return new String(buf);}}.toString());
    private static void convertFromTest(GridPane mainGrid, Test test, Settings settings) {

        ((TextField) mainGrid.lookup("#testName")).setText(test.getName());


        for (int i = 1; i < test.getThemes().get(0).getQuestions().size() + 1; i++) {
            Question question = test.getThemes().get(0).getQuestions().get(i - 1);
            ((TextArea) mainGrid.lookup("#q" + i + "text")).setText(question.getText().replace("<p>", "").replace("</p>", ""));

            String answerType = question.getAnswerType();

            ((RadioButton) mainGrid.lookup("#q" + i + "answerType1")).setSelected(question.getAnswerType().equals(AnswerType.ONE));
            ((RadioButton) mainGrid.lookup("#q" + i + "answerType2")).setSelected(question.getAnswerType().equals(AnswerType.MANY));
            ((RadioButton) mainGrid.lookup("#q" + i + "answerType3")).setSelected(question.getAnswerType().equals(AnswerType.DIRECT_INPUT));
            ((RadioButton) mainGrid.lookup("#q" + i + "answerType4")).setSelected(question.getAnswerType().equals(AnswerType.COMPILES));
            ((RadioButton) mainGrid.lookup("#q" + i + "answerType5")).setSelected(question.getAnswerType().equals(AnswerType.SORT));

            List<Answer> answers = question.getAnswers();

            spawnAnswers(i, answers.size(), answerType, mainGrid, settings);
            if (answerType != null) {
                for (int j = 1; j < answers.size() + 1; j++) {

                    Answer answer = answers.get(j - 1);

// ONE or MANY
                    if (answerType.equals(AnswerType.ONE) || answerType.equals(AnswerType.MANY)) {
                        ((TextField) mainGrid.lookup("#q" + i + "a" + j)).setText(answer.getText());
                        ((RadioButton) mainGrid.lookup("#q" + i + "a" + j + "radioButton")).setSelected(Boolean.valueOf(answer.getValue()));
                    }
// DIRECT INPUT
                    if (answerType.equals(AnswerType.DIRECT_INPUT)) {
                        ((TextField) mainGrid.lookup("#q" + i + "a" + j)).setText(answer.getValue());

                        ((CheckBox) mainGrid.lookup("#q" + i + "a" + j + "spaceSense")).setSelected(question.isSpaceSense());
                        ((CheckBox) mainGrid.lookup("#q" + i + "a" + j + "registerSense")).setSelected(question.isRegisterSense());
                    }
// COMPILES
                    if (answerType.equals(AnswerType.COMPILES)) {
                        ((TextField) mainGrid.lookup("#q" + i + "a" + j)).setText(answer.getText());
                        ((TextField) mainGrid.lookup("#q" + i + "a" + j + "textField")).setText(answer.getValue());
                    }
// SORT
                    if (answerType.equals(AnswerType.SORT)) {
                        ((TextField) mainGrid.lookup("#q" + i + "a" + j)).setText(answer.getValue());
                    }

                }
            }
        }
    }

    private static void spawnAnswers(int i, int answerSize, String answerType, GridPane mainGrid, Settings settings) {
        GridPane answerGrid = (GridPane) mainGrid.lookup("#q" + i + "answerGrid");
        clearGrid(answerGrid, settings);
        Button addAnswer = new Button("Добавить ответ", new ImageView(new Image("addImg.png")));

// ONE
        ToggleGroup group = new ToggleGroup();
        if (answerType.equals(AnswerType.ONE)) {
            for (int j = 1; j < answerSize + 1; j++) {
                TextField answer = new TextField(j + " ответ");
                RadioButton isRight = new RadioButton();
                isRight.setToggleGroup(group);
                answer.setId("q" + i + "a" + j);
                isRight.setId("q" + i + "a" + j + "radioButton");
                answerGrid.addRow(j, answer, isRight);

                if (j == answerSize)
                    addAnswer.setOnAction(event1 -> {
                        TextField answer1 = new TextField(answerGrid.impl_getRowCount() + " ответ");
                        RadioButton isRight1 = new RadioButton();
                        isRight1.setToggleGroup(group);
                        answer1.setId("q" + i + "a" + answerGrid.impl_getRowCount());
                        isRight1.setId("q" + i + "a" + answerGrid.impl_getRowCount() + "radioButton");
                        answerGrid.addRow(answerGrid.impl_getRowCount(), answer1, isRight1);
                    });
            }
        }


// MANY
        if (answerType.equals(AnswerType.MANY)) {
            for (int j = 1; j < answerSize + 1; j++) {
                TextField answer = new TextField(j + " ответ");
                RadioButton isRight = new RadioButton();
                answer.setId("q" + i + "a" + j);
                isRight.setId("q" + i + "a" + j + "radioButton");
                answerGrid.addRow(j, answer, isRight);

                if (j == answerSize)
                    addAnswer.setOnAction(event1 -> {
                        TextField answer1 = new TextField(answerGrid.impl_getRowCount() + " ответ");
                        RadioButton isRight1 = new RadioButton();
                        answer1.setId("q" + i + "a" + answerGrid.impl_getRowCount());
                        isRight1.setId("q" + i + "a" + answerGrid.impl_getRowCount() + "radioButton");
                        answerGrid.addRow(answerGrid.impl_getRowCount(), answer1, isRight1);
                    });
            }
        }

// DIRECT INPUT
        if (answerType.equals(AnswerType.DIRECT_INPUT)) {
            TextField answer = new TextField("1 ответ");
            CheckBox spaceSense = new CheckBox("Чувствительность к пробелам");
            spaceSense.setId("q" + i + "a" + 1 + "spaceSense");
            CheckBox registerSense = new CheckBox("Чувствительность к регистру");
            registerSense.setId("q" + i + "a" + 1 + "registerSense");
            answer.setId("q" + i + "a" + 1);
            answerGrid.addRow(0, answer, spaceSense, registerSense);


            addAnswer.setOnAction(event1 -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Нельзя добавить ещё один вариант ответа !");
                alert.setContentText("Прямой ввод поддерживает только один правильный вариант ответа");
                alert.showAndWait();
            });
        }

// COMPILES
        if (answerType.equals(AnswerType.COMPILES)) {
            for (int j = 1; j < answerSize + 1; j++) {
                TextField answer = new TextField(j + " ответ");
                TextField answerCompiles = new TextField("Соответствие к " + j + " ответу");
                answer.setId("q" + i + "a" + j);
                answerCompiles.setId("q" + i + "a" + j + "textField");

                if (j == answerSize)
                    addAnswer.setOnAction(event1 -> {
                        TextField answer1 = new TextField(answerGrid.impl_getRowCount() + " ответ");
                        TextField answerCompiles1 = new TextField("Соответствие к " + answerGrid.impl_getRowCount() + " ответу");
                        answer1.setId("q" + i + "a" + answerGrid.impl_getRowCount());
                        answerCompiles1.setId("q" + i + "a" + answerGrid.impl_getRowCount() + "textField");
                        answerGrid.addRow(answerGrid.impl_getRowCount(), answer1, answerCompiles1);
                    });

                answerGrid.addRow(j, answer, answerCompiles);
            }
        }

// SORT
        if (answerType.equals(AnswerType.SORT)) {
            for (int j = 1; j < answerSize + 1; j++) {
                TextField answer = new TextField(j + " ответ");
                answer.setId("q" + i + "a" + j);
                answerGrid.addRow(j, answer);

                if (j == answerSize)
                    addAnswer.setOnAction(event1 -> {
                        TextField answer1 = new TextField(answerGrid.impl_getRowCount() + " ответ");
                        answer1.setId("q" + i + "a" + answerGrid.impl_getRowCount());
                        answerGrid.addRow(answerGrid.impl_getRowCount(), answer1);
                    });
            }
        }
    }

    public static String importDataFile(File file) {
        try {
            if (file.length() > 1024 * 1024) {
                throw new VerifyError("File is too big.");
            }
            String type = java.nio.file.Files.probeContentType(file.toPath());
            byte[] data = org.apache.commons.io.FileUtils.readFileToByteArray(file);
            String base64data = java.util.Base64.getEncoder().encodeToString(data);
            String htmlData =
                    "<img src=\"data:" + type + ";base64," + base64data + "\"/>";
            return htmlData;
        } catch (IOException e) {
            e.getStackTrace();
            return "";
        }
    }

    public static String importDataFile(BufferedImage image) {
        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            baos.flush();
            byte[] data = baos.toByteArray();
            baos.close();
            String base64data = java.util.Base64.getEncoder().encodeToString(data);
            String htmlData =
                    "<img src=\"data:image;base64," + base64data + "\"/>";
            return htmlData;
        } catch (Exception e) {
            e.getStackTrace();
            return "fuck";
        }
    }
}