package com.example.makingandtinkering;

import java.time.LocalDateTime;

public class HistoryItem {


    private String dateTime;
    private String units;
    private String reading;
    private String mode;

    public HistoryItem(String dateTime, String units, String reading, String mode) {
        this.dateTime = dateTime;
        this.units = units;
        this.reading = reading;
        this.mode = mode;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getReading() {
        return reading;
    }

    public void setReading(String reading) {
        this.reading = reading;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return "HistoryItem{" +
                "dateTime=" + dateTime +
                ", units='" + units + '\'' +
                ", reading='" + reading + '\'' +
                ", mode='" + mode + '\'' +
                '}';
    }
}
