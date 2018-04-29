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
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by popov on 26.07.2016.
 */
public class Settings {
    private final File scansPath;
    private String APP_NAME = "Создание тестов для РОСТ";
    private String APP_VERSION = "3.1";
    private boolean debug;
    private boolean isDarkTheme;
    private File pathToROSTFolder;
    private Color treeViewBorderColor;
    private JSONObject settings;
    private File programSettingsPath;
    private String uid;
    private String aboutTitle;
    private String aboutHeader;
    private String aboutText;
    private String helpUrl;
    private Alert scanAlert;
    private List<RubberBandSelection> rbsList;
    public Settings() {
        pathToROSTFolder = new File("C:" + System.getProperty("file.separator") + "ROST_tests");
        scansPath = new File("C:" + System.getProperty("file.separator") + "ROST_tests" + System.getProperty("file.separator")+ "сканированные изображения");
        treeViewBorderColor = Color.GRAY;
        isDarkTheme = false;
        programSettingsPath = new File(pathToROSTFolder + System.getProperty("file.separator") + "settings");
        uid = Cloud.generateUID(200);
        rbsList = new ArrayList<>();
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public Color getTreeViewBorderColor() {
        return treeViewBorderColor;
    }

    public void setTreeViewBorderColor(Color treeViewBorderColor) {
        this.treeViewBorderColor = treeViewBorderColor;
    }

    public boolean isDarkTheme() {
        return isDarkTheme;
    }

    public void setDarkTheme(boolean darkTheme) {
        isDarkTheme = darkTheme;
    }

    public JSONObject getSettings() {
        return settings;
    }

    public void setSettings(JSONObject settings) {
        this.settings = settings;
    }

    public File getProgramSettingsPath() {
        return programSettingsPath;
    }

    public void setProgramSettingsPath(File programSettingsPath) {
        this.programSettingsPath = programSettingsPath;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAPP_NAME() {
        return APP_NAME;
    }

    public void setAPP_NAME(String APP_NAME) {
        this.APP_NAME = APP_NAME;
    }

    public String getAPP_VERSION() {
        return APP_VERSION;
    }

    public void setAPP_VERSION(String APP_VERSION) {
        this.APP_VERSION = APP_VERSION;
    }

    public File getPathToROSTFolder() {
        return pathToROSTFolder;
    }

    public void setPathToROSTFolder(File pathToROSTFolder) {
        this.pathToROSTFolder = pathToROSTFolder;
    }

    public String getAboutTitle() {
        return aboutTitle;
    }

    public void setAboutTitle(String aboutTitle) {
        this.aboutTitle = aboutTitle;
    }

    public String getAboutHeader() {
        return aboutHeader;
    }

    public void setAboutHeader(String aboutHeader) {
        this.aboutHeader = aboutHeader;
    }

    public String getAboutText() {
        return aboutText;
    }

    public void setAboutText(String aboutText) {
        this.aboutText = aboutText;
    }

    public String getHelpUrl() {
        return helpUrl;
    }

    public void setHelpUrl(String helpUrl) {
        this.helpUrl = helpUrl;
    }

    public File getScansPath() {
        return scansPath;
    }

    public Alert getScanAlert() {
        return scanAlert;
    }

    public void setScanAlert(Alert scanAlert) {
        this.scanAlert = scanAlert;
    }

    public List<RubberBandSelection> getRbsList() {
        return rbsList;
    }
}