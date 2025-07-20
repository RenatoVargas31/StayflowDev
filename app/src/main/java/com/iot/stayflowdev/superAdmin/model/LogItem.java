package com.iot.stayflowdev.superAdmin.model;

import androidx.annotation.DrawableRes;

import com.iot.stayflowdev.R;

public class LogItem {
    public static final String CATEGORY_ALL = "all";
    public static final String CATEGORY_HOTELS = "hotels";
    public static final String CATEGORY_ACCOUNT = "account";
    public static final String CATEGORY_RESERVATION = "reservation";

    public String id; // Agregar ID del log para poder marcarlo como leído
    public String title;
    public String timestamp;
    public String description;
    public String category;
    public int iconResId;
    public boolean isRead; // Agregar campo para indicar si está leído

    public LogItem(String title, String timestamp, String description, String category) {
        this.title = title;
        this.timestamp = timestamp;
        this.description = description;
        this.category = category;
        this.iconResId = getIconResourceForCategory(category);
        this.isRead = false;
    }

    public LogItem(String title, String timestamp, String description, String category, @DrawableRes int iconResId) {
        this.title = title;
        this.timestamp = timestamp;
        this.description = description;
        this.category = category;
        this.iconResId = iconResId;
        this.isRead = false;
    }

    // Constructor completo con ID y estado de lectura
    public LogItem(String id, String title, String timestamp, String description, String category, @DrawableRes int iconResId, boolean isRead) {
        this.id = id;
        this.title = title;
        this.timestamp = timestamp;
        this.description = description;
        this.category = category;
        this.iconResId = iconResId;
        this.isRead = isRead;
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