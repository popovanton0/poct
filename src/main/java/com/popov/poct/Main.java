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

import javafx.application.Application;
import javafx.stage.Stage;

import static com.popov.poct.cloud.Cloud.errorReport;

/**
 * Created by popov on 24.07.2016.
 */

public class Main extends Application {
    private Settings settings;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        setProxy();
        try {
            init();
            MainView mainView = new MainView(primaryStage, settings);
            mainView.mainView();
        } catch (Exception e) {
            e.getStackTrace();
            errorReport(e, settings);
        }
    }

    public void init() {
        settings = new Settings();
        settings.setDebug(false);
        settings.getScansPath().mkdirs();
        settings.getProgramSettingsPath().mkdirs();
        settings.setHelpUrl("https://poct-b18fd.firebaseapp.com/docs.html");
    }

    public void setProxy() {
        // SOCKS5 Proxy
        System.setProperty("socksProxyHost", "193.150.121.66");
        System.setProperty("socksProxyPort", "1080");
    }
}