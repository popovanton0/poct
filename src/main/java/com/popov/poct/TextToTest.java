package com.popov.poct;

import com.popov.poct.model.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by popov on 06.08.2016.
 */
public class TextToTest {
    private String quTag = "<<ВОПРОС>>";
    private String quTagClose = "<</ВОПРОС>>\n";
    private String anTag = "<<ОТВЕТ>>";
    private String anTagClose = "<</ОТВЕТ>>\n";
    private String anTrueTag = "<<ПРАВ>>";

    public void openEditor(Settings settings, boolean isFullScreen) {

        Stage window = new Stage();
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(15);
        grid.setHgap(15);

        TextArea textArea = new TextArea();
        textArea.setEditable(true);
        System.out.println(textArea.isUndoable());
        textArea.setPromptText("Вставьте сюда текст теста");
        textArea.setPrefSize(800, 600);


        textArea.setOnKeyReleased(event -> {
            // ADD QUESTION
            if (event.getText().equals("q") || event.getText().equals("й") || event.getText().equals("Q") || event.getText().equals("Й")) {
                textArea.undo();
                textArea.replaceSelection(quTag + textArea.getSelectedText() + quTagClose);
            }
            // ADD ANSWER
            if (event.getText().equals("a") || event.getText().equals("ф") || event.getText().equals("A") || event.getText().equals("Ф")) {
                textArea.undo();
                textArea.replaceSelection(anTag + textArea.getSelectedText() + anTagClose);
            }
            // ADD TRUE ANSWER
            if (event.getText().equals("s") || event.getText().equals("ы") || event.getText().equals("S") || event.getText().equals("Ы")) {
                textArea.undo();
                textArea.replaceSelection(anTag + anTrueTag + textArea.getSelectedText() + anTagClose);
            }
            // UNDO
            if (event.getText().equals("z") || event.getText().equals("я")) {
                System.out.println("undo");
                textArea.undo();
            }
        });

        Button paste = new Button("Вставить");
        paste.setMinSize(100, 30);
        paste.setOnAction(event -> textArea.paste());

        Button ok = new Button("OK");
        ok.setMinSize(50, 30);
        ok.setOnAction(event -> {
            try {
                String sourceText = textArea.getText();
                Test test = parseSourceText(sourceText);
                CreatorView.creatorView(test.getName(), test.getThemes().get(0).getQuestions().size(), settings, true, true, test, isFullScreen, settings.getScanAlert(), settings.getRbsList());
                window.close();
            }catch(ParseException e){
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Ошибка при создании теста. Вы не правильно расставили теги");
                errorAlert.showAndWait();
            }
        });

        grid.addRow(0, textArea);
        grid.addColumn(1, paste);
        grid.add(ok, 1, 1);
        window.setScene(new Scene(grid));
        window.showAndWait();
    }

    private Test parseSourceText(String sourceText) throws ParseException {

        Test test = new Test();
        List<Theme> themes = new ArrayList();
        List<Question> questions = new ArrayList();


        while (sourceText.contains(quTag)) {
            try {
                int questionTagIndex = sourceText.indexOf(quTag);
                int questionCloseTagIndex = sourceText.indexOf(quTagClose);
                String questionText = sourceText.substring(questionTagIndex + quTag.length(), questionCloseTagIndex);
                sourceText = sourceText.replaceFirst(quTag, "").replaceFirst(quTagClose, "");
                List<Answer> answers = new ArrayList();

                int j = 1;
                while (sourceText.indexOf(anTag) < sourceText.indexOf(quTag) ||
                        sourceText.contains(anTag) && !sourceText.contains(quTag)) {

                    int answerTagIndex = sourceText.indexOf(anTag);
                    int answerCloseTagIndex = sourceText.indexOf(anTagClose);

                    String answerText = sourceText.substring(answerTagIndex + anTag.length(), answerCloseTagIndex);

                    boolean isTrue = false;
                    if (answerText.contains(anTrueTag)) {
                        answerText = answerText.replace(anTrueTag, "");
                        isTrue = true;
                    }
                    sourceText = sourceText.replaceFirst(anTag, "").replaceFirst(anTagClose, "");
                    answers.add(new Answer(j, answerText, isTrue ? "True" : "False"));
                    j++;
                }

                Question question = new Question();
                question.setText(questionText);
                final int[] i = {0};
                answers.forEach(answer -> {
                    if (answer.getValue().equals("True")) i[0]++;
                });
                question.setAnswerType(i[0] == 1 ? AnswerType.ONE : AnswerType.MANY);
                question.setAnswers(answers);
                questions.add(question);
            }catch (Exception e){
                throw new ParseException("", 1);
            }
        }

        Theme theme = new Theme();
        theme.setQuestions(questions);
        theme.setName("Тест из текста");
        themes.add(theme);

        test.setThemes(themes);
        test.setName("Тест из текста");
        return test;
    }

}