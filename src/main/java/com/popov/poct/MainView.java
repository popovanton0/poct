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

import com.popov.poct.cloud.Cloud;
import com.popov.poct.model.Test;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

import static com.popov.poct.cloud.Cloud.errorReport;

/**
 * Created by popov on 24.07.2016.
 */
public class MainView extends CreatorView {
    private Stage primaryStage;
    private Settings settings;
    private static final Timer time = new Timer();
    static Menu about;
    private Alert scanAlert;

    public MainView(Stage primaryStage, Settings settings) {
        this.primaryStage = primaryStage;
        this.settings = settings;
    }

    static void setDarkTheme(Scene scene, Settings settings) {
        /*scene.getStylesheets().clear();
        scene.getStylesheets().add("night.css");
        settings.setDarkTheme(true);*/
    }

    static void setDayTheme(Scene scene, Settings settings) {
        /*scene.getStylesheets().clear();
        scene.getStylesheets().add("day.css");
        settings.setDarkTheme(false);*/
    }

    public static void loadingComplete() {
        about.setText("О программе");
    }

    public void mainView() {
        BorderPane rootPane = new BorderPane();
        GridPane leftGrid = new GridPane();
        GridPane centerGrid = new GridPane();
        ListView<Label> mainActions = new ListView<>();
        scanAlert = settings.getScanAlert();

        MenuBar menuBar = new MenuBar();
        Menu file = new Menu("Файл");
        Menu view = new Menu("Вид");
        Menu help = new Menu("Справка");
        about = new Menu("О программе    Проверка обновлений ...");

        Scene scene = new Scene(rootPane, 800, 600);

        initMainView(
                scene, rootPane, leftGrid, centerGrid, mainActions,
                menuBar, file, view, help, about
        );

        //scene.getStylesheets().add("day.css");
        primaryStage.setTitle(settings.getAPP_NAME());
        primaryStage.getIcons().add(new Image("ROST_icon.png"));
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(500);
        primaryStage.setScene(scene);
        primaryStage.setFullScreenExitHint("Для выхода из полноэкранного режима нажмите ESC");
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> exit(settings));


    }

    private void initMainView(Scene scene, BorderPane rootPane, GridPane leftGrid, GridPane centerGrid, ListView<Label> mainActions, MenuBar menuBar, Menu file, Menu view, Menu help, Menu about) {
        mainActions.setId("mainActions");
        leftGrid.setGridLinesVisible(settings.isDebug());
        centerGrid.setGridLinesVisible(settings.isDebug());
        rootPane.setLeft(leftGrid);
        rootPane.setCenter(centerGrid);
        rootPane.setTop(menuBar);
        rootPane.setMinSize(primaryStage.getMinWidth(), primaryStage.getMinHeight());
        rootPane.setId("pane");


        scanAlert = new Alert(Alert.AlertType.NONE, "", new ButtonType("OK"), new ButtonType("Отмена"));
        scanAlert.setTitle("Вставка сканированного изобрадения");
        scanAlert.setHeaderText("Выделите нужный участок для вставки");
        scanAlert.setResizable(true);
        scanAlert.setWidth(500);
        scanAlert.setHeight(500);

        GridPane grid = new GridPane();
        scanAlert.getDialogPane().setContent(new ScrollPane(grid));

        File[] scans = settings.getScansPath().listFiles(pathname -> {
            String name = pathname.getName().toLowerCase();
            if (name.endsWith(".jpg") ||
                    name.endsWith(".png") ||
                    name.endsWith(".bmp"))
                return true;
            else return false;
        });

        for (int i1 = 0; i1 < scans.length; i1++) {
            File imageFile = scans[i1];

            try {

                final Image image = new Image(new FileInputStream(imageFile), 450, 0, true,
                        true);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(450);
                imageView.setFitHeight(2000);
                imageView.setPreserveRatio(true);

                Group imageViewGroup = new Group(imageView);
                settings.getRbsList().add(new RubberBandSelection(imageViewGroup, imageView));

                grid.addRow(i1, imageViewGroup);

                System.out.println("Scans optimization: \"" + imageFile.getName() + "\"");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        settings.setScanAlert(scanAlert);

        menuBar.getMenus().addAll(file, view, help, about);

// SETTINGS
        GridPane settingsPane = new GridPane();
        settingsPane.setVgap(10);
        settingsPane.setHgap(10);

        Slider zoomSliderX = new Slider(1, 3, 0.05);
        Slider zoomSliderY = new Slider(1, 3, 0.05);
        Slider zoomSliderZ = new Slider(1, 3, 0.05);
        CheckBox rootPaneScaleShape = new CheckBox();
        Slider primaryStageOpacity = new Slider(0.5, 1, 0.01);
        primaryStageOpacity.setValue(1);
        ChoiceBox darkModeTime = new ChoiceBox(FXCollections.observableArrayList(
                "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"
        ));

        rootPaneScaleShape.setOnAction(event -> rootPane.setScaleShape(rootPaneScaleShape.isSelected()));
        primaryStageOpacity.valueProperty().addListener((observable, oldValue, newValue) -> primaryStage.setOpacity(newValue.doubleValue()));
        darkModeTime.setValue("21:00");

        settingsPane.addRow(0, new Label("Увеличение X"), zoomSliderX);
        settingsPane.addRow(1, new Label("Увеличение Y"), zoomSliderY);
        settingsPane.addRow(2, new Label("Увеличение Z"), zoomSliderZ);
        settingsPane.addRow(3, new Label("rootPane.setScaleShape"), rootPaneScaleShape);
        settingsPane.addRow(4, new Label("Прозрачность"), primaryStageOpacity);
        settingsPane.addRow(5, new Label("Время автоматического включения ночного режима"), darkModeTime);
        Label label = new Label("UID");
        label.setStyle("-fx-text-fill: rgb(200, 200, 200);");
        Label label2 = new Label();
        label2.setStyle("-fx-text-fill: rgb(200, 200, 200);");
        settingsPane.addRow(6, label, label2);

// MENU BAR
        // File
        MenuItem newTest = new MenuItem("Новый тест");
        MenuItem openTest = new MenuItem("Открыть тест");
        MenuItem exit = new MenuItem("Выход");

        newTest.setOnAction(event -> launchCreatorView());

        openTest.setOnAction(event -> {
            try {
                TestLoader testLoader = new TestLoader();
                List<String> linesList = Files.readAllLines(new FileChooser().showOpenDialog(primaryStage).toPath());
                final String[] test = {""};
                linesList.forEach(s -> test[0] += s);
                testLoader.loadTest(test[0], settings, primaryStage.isFullScreen());
            } catch (IOException e) {
                errorReport(e, settings);
            }
        });

        exit.setOnAction(event -> exit(settings));
        file.getItems().addAll(newTest, openTest, exit);

        // MainView
        MenuItem fullscreenMode = new MenuItem("Полноэкранный режим");
        CheckMenuItem darkMode = new CheckMenuItem("Ночной режим");

        fullscreenMode.setOnAction(event -> primaryStage.setFullScreen(!primaryStage.isFullScreen()));

        darkMode.setOnAction(event -> {
            if (settings.isDarkTheme())
                setDayTheme(scene, settings);
            else
                setDarkTheme(scene, settings);
        });
        view.getItems().addAll(fullscreenMode/*, darkMode*/);

        // Help
        MenuItem onlineHelp = new MenuItem("Помощь по работе с программой");
        onlineHelp.setOnAction(event -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI(settings.getHelpUrl()));
                } catch (Exception e) {
                    errorReport(e, settings);

                    Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                    alert1.setHeaderText("Ссылка: ");
                    alert1.getDialogPane().setContent(new TextField(settings.getHelpUrl()));
                    alert1.showAndWait();
                }
            } else {
                Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                alert1.setHeaderText("Ссылка: ");
                alert1.getDialogPane().setContent(new TextField(settings.getHelpUrl()));
                alert1.showAndWait();
            }
        });
        MenuItem feedback = new MenuItem("Отзыв");
        feedback.setOnAction(event -> {
            Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
            alert1.setHeaderText("Отправить отзыв");
            alert1.setTitle("Отправить отзыв");

            GridPane gridPane = new GridPane();
            gridPane.setVgap(10);
            gridPane.setHgap(10);

            TextField name = new TextField();
            name.setPromptText("Проблема с созданием тестов");
            TextArea textArea = new TextArea("");
            textArea.setPromptText("Текст отзыва");
            textArea.setMinHeight(200);
            TextField email = new TextField();
            email.setPromptText("example@mail.ru");

            gridPane.addRow(0, new Label("Название"), name);
            gridPane.addRow(1, new Label("Текст"), textArea);
            gridPane.addRow(2, new Label("E-mail для обратной связи"), email);
            alert1.getDialogPane().setContent(gridPane);


            Optional<ButtonType> result = alert1.showAndWait();
            if (result.get() == ButtonType.OK) {

                try {
                    /*if (
                            (name.getText() != "" || name.getText() != null) &&
                            (textArea.getText() != "" || textArea.getText() != null) &&
                            (email.getText() != "" || email.getText() != null)
                       ) {*/
                    Cloud.sendFeedback(name.getText(), email.getText(), textArea.getText(), settings);

                } catch (Exception e) {
                    Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
                    alert2.setHeaderText("Пожалуйста, отправьте e-mail на адрес help_review@mail.ru\n со следующим текстом:");
                    alert2.getDialogPane().setContent(new TextArea(
                            "title: " + name.getText() + "\n" +
                                    "text: " + textArea.getText() + "\n" +
                                    "responseEmail: " + email.getText() + "\n" +
                                    "uid: " + settings.getUid()));
                    alert2.showAndWait();
                }
            }
        });
        help.getItems().addAll(onlineHelp, feedback);

        // About
        MenuItem aboutProgram = new MenuItem("О программе");
        aboutProgram.setOnAction(event -> {
            about();
        });

        MenuItem settingsMenuItem = new MenuItem("Настройки");

        settingsMenuItem.setOnAction(event -> {
            Alert settingsAlert = new Alert(Alert.AlertType.CONFIRMATION, "Настройки");
            settingsAlert.getDialogPane().setContent(settingsPane);

            Optional<ButtonType> result = settingsAlert.showAndWait();
            if (result.get() == ButtonType.OK) {
                saveSettings(zoomSliderX, zoomSliderY, zoomSliderZ, rootPaneScaleShape, primaryStageOpacity, darkModeTime);
            }
        });
        about.getItems().addAll(/*settingsMenuItem, */aboutProgram);

// TREE VIEW

        TreeItem<File> treeFileView = createNode(settings.getPathToROSTFolder());
        treeFileView.expandedProperty().setValue(true);

        TreeView treeView = new TreeView(treeFileView);
        treeView.setId("treeView");
        treeView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (handleMouseClicked(event, treeView) != null) {
                try {
                    TestLoader testLoader = new TestLoader();
                    List<String> linesList = Files.readAllLines(handleMouseClicked(event, treeView).toPath());
                    final String[] test = {""};
                    linesList.forEach(s -> test[0] += s);
                    testLoader.loadTest(test[0], settings, primaryStage.isFullScreen());
                } catch (IOException e) {
                    errorReport(e, settings);
                }
            }
        });
        treeView.setBackground(new Background(new BackgroundFill(settings.getTreeViewBorderColor(), new CornerRadii(0), new Insets(0))));
        treeView.setCellFactory(list -> {
            final TreeCell cell = new TextFieldTreeCell();

            cell.setStyle("-fx-background-color: white; -fx-text-fill: black;");
            cell.setOnMouseEntered(event -> {
                if (cell.getText() != null && !cell.getText().equals(""))
                    cell.setStyle("-fx-background-color: rgb(75, 110, 175);/* blue color */ -fx-text-fill: white;");
            });
            cell.setOnMouseExited(event -> cell.setStyle("-fx-background-color: white; -fx-text-fill: black;"));

            return cell;
        });

        rootPane.setLeft(treeView);

// MAIN VIEW
        centerGrid.setVgap(20);
        centerGrid.setHgap(20);
        centerGrid.setPadding(new Insets(150, 100, 100, 120));
        centerGrid.add(mainActions, 0, 0);

        zoomSliderX.valueProperty().addListener((observable, oldValue, newValue) -> rootPane.setScaleX(newValue.doubleValue()));
        zoomSliderY.valueProperty().addListener((observable, oldValue, newValue) -> rootPane.setScaleY(newValue.doubleValue()));
        zoomSliderZ.valueProperty().addListener((observable, oldValue, newValue) -> rootPane.setScaleZ(newValue.doubleValue()));

        ObservableList<Label> items = FXCollections.observableArrayList(
                new Label("Новый тест", new ImageView(new Image("newTest.png"))),
                new Label("Новый тест из текста", new ImageView(new Image("textToTest.png"))),
                new Label("Загрузить тест", new ImageView(new Image("open.png"))),
                new Label("Выйти", new ImageView(new Image("exit.png")))
        );


        mainActions.setCellFactory(list -> {
            final ListCell cell = new ListCell<Label>() {

                @Override
                public void updateItem(Label item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        // true makes this load in background
                        // see other constructors if you want to control the size, etc
                        setText(item.getText());
                        setGraphic(item.getGraphic());
                    }
                }
            };


            cell.setStyle("-fx-background-color: rgb(244, 244, 244); -fx-text-fill: black;");
            cell.setOnMouseEntered(event -> cell.setStyle("-fx-background-color: rgb(75, 110, 175);/* blue color */ -fx-text-fill: white;"));
            cell.setOnMouseExited(event -> cell.setStyle("-fx-background-color: rgb(244, 244, 244); -fx-text-fill: black;"));

            cell.setOnMouseClicked(event -> {
                // System.out.println("clicked, text = " + cell.getText());
                if (cell.getText() == null) return;
                if (cell.getText().equals("Новый тест")) launchCreatorView();
                if (cell.getText().equals("Новый тест из текста")) {
                    TextToTest editor = new TextToTest();
                    editor.openEditor(settings, primaryStage.isFullScreen());
                }
                if (cell.getText().equals("Загрузить тест")) {
                    try {
                        TestLoader testLoader = new TestLoader();
                        List<String> linesList = Files.readAllLines(new FileChooser().showOpenDialog(primaryStage).toPath());
                        final String[] test = {""};
                        linesList.forEach(s -> test[0] += s);
                        testLoader.loadTest(test[0], settings, primaryStage.isFullScreen());
                    } catch (IOException e) {
                        errorReport(e, settings);
                    }
                }
                if (cell.getText().equals("Выйти")) {
                    exit(settings);
                }
            });
            return cell;
        });

        mainActions.setItems(items);
        mainActions.setMaxHeight(items.size() * 24);

// Load Settings
        if (new File(settings.getProgramSettingsPath() + File.separator + "settings.json").exists() &&
                new File(settings.getProgramSettingsPath() + File.separator + "settings.json").getUsableSpace() != 0)
            try {
                loadSettings(new File(settings.getProgramSettingsPath() + File.separator + "settings.json"),
                        zoomSliderX, zoomSliderY, zoomSliderZ, rootPaneScaleShape, primaryStageOpacity, darkModeTime);
            } catch (Exception e) {
                errorReport(e, settings);
            }

        saveSettings(zoomSliderX, zoomSliderY, zoomSliderZ, rootPaneScaleShape, primaryStageOpacity, darkModeTime);
        label2.setText(settings.getUid());

        try {
            Cloud.setup(settings);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Calendar.getInstance().getTime().getHours() >=
                Integer.valueOf(((String) settings.getSettings().get("darkModeTime")).replace(":00", "")) &&
                !settings.isDarkTheme()) {
            setDarkTheme(scene, settings);
            darkMode.setSelected(true);
        }


        time.schedule(new TimerTask() {
            @Override
            public void run() {
                // System.out.println("Проверка на время main view");

                if (Calendar.getInstance().getTime().getHours() >=
                        Integer.valueOf(((String) settings.getSettings().get("darkModeTime")).replace(":00", "")) &&
                        !settings.isDarkTheme()) {
                    setDarkTheme(scene, settings);
                    darkMode.setSelected(true);
                }
            }
        }, 4000, 60000);


    }

    public void about() {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(settings.getAboutTitle());
            alert.setHeaderText(settings.getAboutHeader());
            GridPane gr = new GridPane();

            Label label = new Label(settings.getAboutText());
            label.setOnScroll(event4 -> {
                label.setOnContextMenuRequested(event5 -> {
                    label.setOnScroll(event6 -> {
                        label.setOnMouseClicked(event -> {
                            if (event.getClickCount() == 10) {
                                Alert anotherAlert = new Alert(Alert.AlertType.INFORMATION);
                                anotherAlert.setTitle("Another one Easter egg");
                                anotherAlert.setHeaderText("Ещё одна пасхалка !");
                                anotherAlert.showAndWait();
                            }
                        });
                    });
                });
            });
            label.setPadding(new Insets(10, 10, 10, 10));
            gr.add(label, 1, 0);
            alert.getDialogPane().setContent(gr);
            alert.showAndWait();

        } catch (Exception e) {
            errorReport(e, settings);
        }
    }

    public static void exit(Settings settings) {
        time.cancel();
        try {
            settings.getProgramSettingsPath().mkdirs();
            PrintWriter out = new PrintWriter(settings.getProgramSettingsPath() + File.separator + "settings.json");
            settings.getSettings().writeJSONString(out);
            out.close();
        } catch (Exception e) {
            errorReport(e, settings);
        }
        try {
            Cloud.exit(settings);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private void launchCreatorView() {
        GridPane dialogPane = new GridPane();
        dialogPane.setGridLinesVisible(settings.isDebug());
        dialogPane.setVgap(10);
        dialogPane.setHgap(10);

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Новый тест");
        dialog.setHeaderText("Создание нового теста");
        dialog.getDialogPane().setContent(dialogPane);

        TextField name = new TextField("Новый тест");
        TextField questionsNumber = new TextField("1");

        dialogPane.addRow(0, new Label("Имя теста"), name);
        dialogPane.addRow(1, new Label("Количество вопросов"), questionsNumber);
// NUMBERS CHECK

        questionsNumber.setStyle("-fx-border-width: 0px;");
        questionsNumber.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("^[0-9]+$"))
                questionsNumber.setStyle("-fx-border-color: red; -fx-border-width: 2px ;");
            else questionsNumber.setStyle("-fx-border-width: 0px;");
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.get() == ButtonType.OK && questionsNumber.getStyle().length() == 22) {
            CreatorView.creatorView(name.getText(), Integer.valueOf(questionsNumber.getText()), settings, false, false, new Test(), primaryStage.isFullScreen(), settings.getScanAlert(), settings.getRbsList());

        }
    }

    private void saveSettings(Slider zoomSliderX, Slider zoomSliderY, Slider zoomSliderZ, CheckBox rootPaneScaleShape, Slider primaryStageOpacity, ChoiceBox darkModeTime) {
        JSONObject jsonSettings = new JSONObject();
        jsonSettings.put("zoomSliderX", zoomSliderX.getValue());
        jsonSettings.put("zoomSliderY", zoomSliderY.getValue());
        jsonSettings.put("zoomSliderZ", zoomSliderZ.getValue());
        jsonSettings.put("rootPaneScaleShape", rootPaneScaleShape.isSelected());
        jsonSettings.put("primaryStageOpacity", primaryStageOpacity.getValue());
        jsonSettings.put("darkModeTime", darkModeTime.getValue());
        jsonSettings.put("uid", settings.getUid());
        settings.setSettings(jsonSettings);
// Write to file
        try {
            PrintWriter out = new PrintWriter(settings.getProgramSettingsPath() + File.separator + "settings.json");
            settings.getSettings().writeJSONString(out);
            out.close();
        } catch (Exception e) {
            errorReport(e, settings);
        }
    }

    private void loadSettings(File settingsFile, Slider zoomSliderX, Slider zoomSliderY, Slider zoomSliderZ, CheckBox rootPaneScaleShape, Slider primaryStageOpacity, ChoiceBox darkModeTime) throws ParseException, IOException {
        JSONParser parser = new JSONParser();
        String settingsString = FileUtils.readFileToString(settingsFile, Charset.forName("UTF-8"));
        settings.setSettings((JSONObject) parser.parse(settingsString));

        JSONObject jsonSettings = settings.getSettings();
        zoomSliderX.setValue((Double) jsonSettings.get("zoomSliderX"));
        zoomSliderY.setValue((Double) jsonSettings.get("zoomSliderY"));
        zoomSliderZ.setValue((Double) jsonSettings.get("zoomSliderZ"));
        rootPaneScaleShape.setSelected((Boolean) jsonSettings.get("rootPaneScaleShape"));
        primaryStageOpacity.setValue((Double) jsonSettings.get("primaryStageOpacity"));
        darkModeTime.setValue(jsonSettings.get("darkModeTime"));
        settings.setUid((String) jsonSettings.get("uid"));
    }

    private File handleMouseClicked(MouseEvent event, TreeView treeView) {
        File name = null;
        Node node = event.getPickResult().getIntersectedNode();
        // Accept clicks only on node cells, and not on empty spaces of the TreeView
        if (node instanceof Text || (node instanceof TreeCell && ((TreeCell) node).getText() != null)) {
            try {
                name = (File) ((TreeItem) treeView.getSelectionModel().getSelectedItem()).getValue();
            } catch (Exception e) {
            }
        }
        return name;
    }

    private TreeItem<File> createNode(final File f) {
        return new TreeItem<File>(f) {
            private boolean isLeaf;
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;

            @Override
            public ObservableList<TreeItem<File>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;
                    super.getChildren().setAll(buildChildren(this));
                }
                return super.getChildren();
            }


            @Override
            public boolean isLeaf() {
                if (isFirstTimeLeaf) {
                    isFirstTimeLeaf = false;
                    File f = getValue();
                    isLeaf = f.isFile();
                }
                return isLeaf;
            }

            private ObservableList<TreeItem<File>> buildChildren(TreeItem<File> TreeItem) {

                File f = TreeItem.getValue();
                if (f == null) {
                    return FXCollections.emptyObservableList();
                }
                if (f.isFile()) {
                    return FXCollections.emptyObservableList();
                }
                File[] files = f.listFiles();
                if (files != null) {
                    ObservableList<TreeItem<File>> children = FXCollections
                            .observableArrayList();
                    for (File childFile : files) {
                        children.add(createNode(childFile));
                    }
                    return children;
                }
                return FXCollections.emptyObservableList();
            }
        };
    }
}
