package com.ess.tudarmstadt.de.mwidgetexample.JSON;

/**
 * Created by lukas on 12.08.15.
 */
public class Barcode {
    private String barcode;
    private String name;
    public Barcode(String barcode, String name) {
        this.barcode = barcode;
        this.name = name;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getName() {
        return name;
    }
}
