package com.guy.class23b_ands_2;

public class MyLoc {

    private double lat;
    private double lon;
    private double speed;
    private double bearing;

    public MyLoc() {}

    public MyLoc(double lat, double lon, double speed, double bearing) {
        this.lat = lat;
        this.lon = lon;
        this.speed = speed;
        this.bearing = bearing;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }
}
