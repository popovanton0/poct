package com.popov.poct.cloud;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import com.popov.poct.MainView;
import com.popov.poct.Settings;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.net.URI;
import java.security.SecureRandom;
import java.util.*;

import static org.apache.commons.lang.exception.ExceptionUtils.getStackTrace;

/**
 * Created by popov on 27.07.2016.
 */

public class Cloud {

    static DatabaseReference ref;
    static DatabaseReference user;
    static DatabaseReference issues;
    static DatabaseReference updateInfo;
    static DatabaseReference crashes;

    public static void setup(Settings settings) throws FileNotFoundException {

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setServiceAccount(new FileInputStream("path/to/serviceAccount.json"))
                .setDatabaseUrl("https://<databaseName>.firebaseio.com")
                .build();
        FirebaseApp.initializeApp(options);


        ref = FirebaseDatabase.getInstance().getReference();
        user = FirebaseDatabase.getInstance().getReference("/users/" + settings.getUid());
        issues = FirebaseDatabase.getInstance().getReference("/issues");
        updateInfo = FirebaseDatabase.getInstance().getReference("/updateInfo");
        crashes = FirebaseDatabase.getInstance().getReference("/crashes");
        DatabaseReference specialMsg = FirebaseDatabase.getInstance().getReference("/users/" + settings.getUid() + "/specialMsg");

// ADD USER
        user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    try {
                        createUser(ref, settings);
                        System.out.println("success");
                    } catch (Exception e) {
                        errorReport(e, "error while creating a user", settings);
                        System.out.println("error while creating a user");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        user.child("isOnline").setValue(true);
        user.child("lastLaunchTime").setValue(Calendar.getInstance().getTime().toString());
        user.child("launchesNumber").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user.child("launchesNumber").setValue((long) dataSnapshot.getValue() + 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        ref.child("/about").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                settings.setAboutTitle((String) dataSnapshot.child("title").getValue());
                settings.setAboutHeader((String) dataSnapshot.child("header").getValue());
                settings.setAboutText((String) dataSnapshot.child("text").getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        checkUpdates(settings);
        user.child("/currentVersion").setValue(settings.getAPP_VERSION());

// SPECIAL MESSAGE
        specialMsg.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle((String) dataSnapshot.child("title").getValue());
                        alert.setHeaderText((String) dataSnapshot.child("header").getValue());
                        alert.setContentText((String) dataSnapshot.child("text").getValue());
                        alert.showAndWait();
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

    }

    private static void createUser(DatabaseReference ref, Settings settings) throws Exception {
        DatabaseReference usersRef = ref.child("/users/");
        Map<String, Object> usersList = new HashMap<>();

        Map<String, Object> mapProperties = new HashMap<>();
        System.getProperties().put("CurrentTime", Calendar.getInstance().getTime().toString());
        Properties systemProperties = System.getProperties();
        for (Map.Entry<Object, Object> x : systemProperties.entrySet()) {
            mapProperties.put(((String) x.getKey()).replace("/", "\\").replace(".", "•").replace("[", "").replace("]", ""), x.getValue());
        }

        Map<String, Object> user = new HashMap<>();
        Map<String, Object> questionStatistic = new HashMap<>();

        user.put("questionStatistic", questionStatistic);
        user.put("currentVersion", settings.getAPP_VERSION());
        user.put("createdTestsNumber", 0);
        user.put("installationDate", Calendar.getInstance().getTime().toString());
        user.put("isOnline", true);
        user.put("lastLaunchTime", Calendar.getInstance().getTime().toString());
        user.put("launchesNumber", 1);
        user.put("specialMsg", "");
        user.put("uid", settings.getUid());
        user.put("systemParams", mapProperties);

        usersList.put(settings.getUid(), user);
        usersRef.updateChildren(usersList);
    }

    public static void checkUpdates(Settings settings) {
        updateInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double version = 0;
                try {
                    version = (double) dataSnapshot.child("latestVersion").getValue();
                } catch (Exception e) {
                    version = (long) dataSnapshot.child("latestVersion").getValue();
                }
                if (version > Double.valueOf(settings.getAPP_VERSION())) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", new ButtonType("Обновить", ButtonBar.ButtonData.FINISH));
                        alert.setTitle((String) dataSnapshot.child("shortUpdateMsg").getValue());
                        alert.setHeaderText((String) dataSnapshot.child("shortUpdateMsg").getValue());
                        alert.setContentText((String) dataSnapshot.child("updateMsg").getValue());

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get().getText().equals("Обновить")) {
                            if (Desktop.isDesktopSupported()) {
                                try {
                                    Desktop.getDesktop().browse(new URI((String) dataSnapshot.child("url").getValue()));
                                } catch (Exception e) {
                                    errorReport(e, "рабочий стол не поддерживается", settings);

                                    Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                                    alert1.setHeaderText("Ссылка для скачивания: ");
                                    alert1.getDialogPane().setContent(new TextField((String) dataSnapshot.child("url").getValue()));
                                    alert1.showAndWait();
                                }
                            } else {
                                Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                                alert1.setHeaderText("Ссылка для скачивания: ");
                                alert1.getDialogPane().setContent(new TextField((String) dataSnapshot.child("url").getValue()));
                                alert1.showAndWait();
                            }
                        }
                    });
                }

                settings.setHelpUrl((String) dataSnapshot.child("helpUrl").getValue());
                Platform.runLater(MainView::loadingComplete);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public static void exit(Settings settings) {
        ref.child("/users/" + settings.getUid() + "/isOnline").setValue(false);
    }

    public static void testCreated(int questionNumber, long minutes, boolean isTextToTest, boolean isLoad) {
        user.child("createdTestsNumber").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user.child("createdTestsNumber").setValue((long) dataSnapshot.getValue() + 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        Map<String, Object> qu = new HashMap<>();
        Map<String, Object> test = new HashMap<>();

        test.put("creationDate", Calendar.getInstance().getTime().toString());
        test.put("isTextToTest", isTextToTest);
        test.put("isLoad", isLoad);
        test.put("questions", questionNumber);
        test.put("time", minutes);

        qu.put(generateUID(300), test);
        user.child("questionStatistic").updateChildren(qu);
    }

    public static void errorReport(Throwable e, Settings settings) {
        Map<String, Object> crashesChildren = new HashMap<>();
        Map<String, Object> crash = new HashMap<>();
        crash.put("date", Calendar.getInstance().getTime().toString());
        crash.put("exeptionMsg", getStackTrace(e));
        crash.put("uid", settings.getUid());
        String crashUid = generateUID(300);
        crash.put("crashUid", crashUid);
        crashesChildren.put(crashUid, crash);
        crashes.updateChildren(crashesChildren);
    }

    public static void errorReport(Throwable e, String additionalInfo, Settings settings) {
        Map<String, Object> crashesChildren = new HashMap<>();
        Map<String, Object> crash = new HashMap<>();
        crash.put("date", Calendar.getInstance().getTime().toString());
        crash.put("exeptionMsg", getStackTrace(e));
        crash.put("uid", settings.getUid());
        crash.put("additionalInfo", additionalInfo);
        String crashUid = generateUID(300);
        crash.put("crashUid", crashUid);
        crashesChildren.put(crashUid, crash);
        crashes.updateChildren(crashesChildren);
    }

    public static void sendFeedback(String name, String email, String text, Settings settings) throws Exception {

        Map<String, Object> issuesMap = new HashMap<>();
        Map<String, Object> issue = new HashMap<>();
        issue.put("email", email);
        issue.put("name", name);
        issue.put("text", text);
        issue.put("uid", settings.getUid());
        String issueUid = generateUID(300);
        issue.put("issueUid", issueUid);
        issuesMap.put(issueUid, issue);

        issues.updateChildren(issuesMap);
    }

    public static String generateUID(int length) {
        SecureRandom random = new SecureRandom();
        return new BigInteger(length, random).toString(32);
    }

    /*public static void auth(Stage primaryStage, Settings settings) {
        try {
            Desktop.getDesktop().open(new File(settings.getProgramSettingsPath() + File.separator + "newAuth.html"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String authComplete(String uid1, Stage primaryStage) {
        uid = uid1;
        primaryStage.toFront();
        return uid;
    }

    public static void logOut(Settings settings) {
        try {
            Desktop.getDesktop().open(new File(settings.getProgramSettingsPath() + File.separator + "logOut.html"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /*private static void enableCORS(final String origin) {

        options("*//*", (request, response) -> {

            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
        });
    }*/

}
