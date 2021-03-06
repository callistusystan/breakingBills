package edu.monash.fit3027.breakingbills.models;

/**
 * Library class to represent a library in the About App screen.
 *
 * Created by Callistus on 1/6/2017.
 */

public class Library {

    private String name;
    private String detail;
    private String license;
    private int icon;

    public Library(String name, int icon, String detail, String license) {
        this.name = name;
        this.icon = icon;
        this.detail = detail;
        this.license = license;
    }

    // getters and setters
    public String getName() {
        return name;
    }

    public int getIcon() { return icon; }

    public String getDetail() {
        return detail;
    }

    public String getLicense() {
        return license;
    }
}
