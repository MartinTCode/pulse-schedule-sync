package com.pulse.frontend.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.BooleanProperty;

public class ScheduleRow {
    
    private final StringProperty kurs = new SimpleStringProperty(); 
    private final StringProperty aktivitet = new SimpleStringProperty();
    private final StringProperty larare = new SimpleStringProperty();
    private final StringProperty startDatum = new SimpleStringProperty();
    private final StringProperty slutDatum = new SimpleStringProperty();
    private final StringProperty startTid = new SimpleStringProperty();
    private final StringProperty slutTid = new SimpleStringProperty();
    private final StringProperty plats = new SimpleStringProperty();
    private final BooleanProperty andrad = new SimpleBooleanProperty();

    // Constructor
    public ScheduleRow(String kurs, String aktivitet, String larare, String startDatum, String slutDatum, String startTid, String slutTid, String plats, boolean andrad) {
        this.kurs.set(kurs);
        this.aktivitet.set(aktivitet);
        this.larare.set(larare);
        this.startDatum.set(startDatum);
        this.slutDatum.set(slutDatum);
        this.startTid.set(startTid);
        this.slutTid.set(slutTid);
        this.plats.set(plats);
        this.andrad.set(andrad);

    }

    // Getters for properties
    public String getKurs() {
        return kurs.get();
    }

    public String getAktivitet() {
        return aktivitet.get();
    }

    public String getLarare() {
        return larare.get();
    }

    public String getStartDatum() {
        return startDatum.get();
    }

    public String getSlutDatum() {
        return slutDatum.get();
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

    public StringProperty aktivitetProperty() {
        return aktivitet;
    }

    public StringProperty larareProperty() {
        return larare;
    }

    public StringProperty startDatumProperty() {
        return startDatum;
    }

    public StringProperty slutDatumProperty() {
        return slutDatum;
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

    // Setters
    public void setLarare(String larare) {
        this.larare.set(larare);
    }

    public void setKurs(String kurs) {
        this.kurs.set(kurs);
    }

    public void setAktivitet(String aktivitet) {
        this.aktivitet.set(aktivitet);
    }

    public void setStartDatum(String startDatum) {
        this.startDatum.set(startDatum);
    }

    public void setSlutDatum(String slutDatum) {
        this.slutDatum.set(slutDatum);
    }

    public void setPlats(String plats) {
        this.plats.set(plats);
    }

    public void setStartTid(String startTid) {
        this.startTid.set(startTid);
    }

    public void setSlutTid(String slutTid) {
        this.slutTid.set(slutTid);
    }

    public void setAndrad(boolean andrad) {
        this.andrad.set(andrad);
    }
    
}
