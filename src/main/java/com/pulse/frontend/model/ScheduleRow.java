package com.pulse.frontend.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.BooleanProperty;

public class ScheduleRow {
    
    private final StringProperty kurs = new SimpleStringProperty();
    private final StringProperty larare = new SimpleStringProperty();
    private final StringProperty datum = new SimpleStringProperty();
    private final StringProperty startTid = new SimpleStringProperty();
    private final StringProperty slutTid = new SimpleStringProperty();
    private final StringProperty plats = new SimpleStringProperty();
    private final BooleanProperty andrad = new SimpleBooleanProperty();

    // Constructor
    public ScheduleRow(String kurs, String larare, String datum, String startTid, String slutTid, String plats, boolean andrad) {
        this.kurs.set(kurs);
        this.larare.set(larare);
        this.datum.set(datum);
        this.startTid.set(startTid);
        this.slutTid.set(slutTid);
        this.plats.set(plats);
        this.andrad.set(andrad);

    }

    // Getters for properties
    public String getKurs() {
        return kurs.get();
    }

    public String getLarare() {
        return larare.get();
    }

    public String getDatum() {
        return datum.get();
    }

    public String getStartTid() {
        return startTid.get();
    }

    public String getSlutTid() {
        return slutTid.get();
    }

    public String getPlats() {
        return plats.get();
    }

    public boolean isAndrad() {
        return andrad.get();
    }

    // Property getters for binding
    public StringProperty kursProperty() {
        return kurs;
    }

    public StringProperty larareProperty() {
        return larare;
    }

    public StringProperty datumProperty() {
        return datum;
    }

    public StringProperty startTidProperty() {
        return startTid;
    }

    public StringProperty slutTidProperty() {
        return slutTid;
    }

    public StringProperty platsProperty() {
        return plats;
    }

    public BooleanProperty andradProperty() {
        return andrad;
    }
    
}
