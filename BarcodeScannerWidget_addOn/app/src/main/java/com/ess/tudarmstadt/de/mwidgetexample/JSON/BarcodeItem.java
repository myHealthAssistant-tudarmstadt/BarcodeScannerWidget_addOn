package com.ess.tudarmstadt.de.mwidgetexample.JSON;

/**
 * Created by lukas on 06.08.15.
 */
public class BarcodeItem {
    private int id;
    private String title;
    private String pop_content;
    private String obj_date;
    private String obj_time;
    private double longitude;
    private double latitude;
    private String address;
    private String pop_uri;
    private int amount;

    public BarcodeItem(int id, String title, String pop_content, String obj_date, String obj_time,
                       double longitude, double latitude, String address, String pop_uri, int amount
    ) {
        this.id = id;
        this.title = title;
        this.pop_content = pop_content;
        this.obj_date = obj_date;
        this.obj_time = obj_time;
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
        this.pop_uri = pop_uri;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPop_content() {
        return pop_content;
    }

    public String getObj_date() {
        return obj_date;
    }

    public String getObj_time() {
        return obj_time;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getAddress() {
        return address;
    }

    public String getPop_uri() {
        return pop_uri;
    }

    public int getAmount() {
        return amount;
    }
}
