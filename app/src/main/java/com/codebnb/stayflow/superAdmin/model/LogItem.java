package com.codebnb.stayflow.superAdmin.model;

import androidx.annotation.DrawableRes;

import com.codebnb.stayflow.R;

public class LogItem {
    public static final String CATEGORY_ALL = "all";
    public static final String CATEGORY_HOTELS = "hotels";
    public static final String CATEGORY_ACCOUNT = "account";
    public static final String CATEGORY_RESERVATION = "reservation";

    public String title;
    public String timestamp;
    public String description;
    public String category;
    public int iconResId;

    public LogItem(String title, String timestamp, String description, String category) {
        this.title = title;
        this.timestamp = timestamp;
        this.description = description;
        this.category = category;
        this.iconResId = getIconResourceForCategory(category);
    }

    public LogItem(String title, String timestamp, String description, String category, @DrawableRes int iconResId) {
        this.title = title;
        this.timestamp = timestamp;
        this.description = description;
        this.category = category;
        this.iconResId = iconResId;
    }

    // For backward compatibility
    public LogItem(String title, String timestamp, String description) {
        this(title, timestamp, description, CATEGORY_ALL);
    }

    @DrawableRes
    private int getIconResourceForCategory(String category) {
        switch (category) {
            case CATEGORY_HOTELS:
                return R.drawable.ic_hotel;
            case CATEGORY_ACCOUNT:
                return R.drawable.ic_perfil;  // Cambiado a ic_perfil que parece existir en tu proyecto
            case CATEGORY_RESERVATION:
                return R.drawable.ic_calendar;  // Asume que existe este icono, si no, cámbialo
            case CATEGORY_ALL:
            default:
                return R.drawable.ic_layers;  // Icono genérico
        }
    }
}