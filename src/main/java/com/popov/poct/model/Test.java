package com.popov.poct.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by popov on 24.07.2016.
 */
public class Test {
    @SerializedName("INFO")
    private String info;
    @SerializedName("NAME")
    private String name;
    @SerializedName("ISDELETED")
    private boolean isDeleted;
    @SerializedName("DELETEDATE")
    private String deleteDate;
    @SerializedName("TESTGROUPID")
    private String testGroupId;
    @SerializedName("SUBJECTID")
    private int subjectId;
    @SerializedName("THEMES")
    private List<Theme> themes;

    public List<Theme> getThemes() {
        return themes;
    }

    public void setThemes(List<Theme> themes) {
        this.themes = themes;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String isDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(String deleteDate) {
        this.deleteDate = deleteDate;
    }

    public String getTestGroupId() {
        return testGroupId;
    }

    public void setTestGroupId(String testGroupId) {
        this.testGroupId = testGroupId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }
}
