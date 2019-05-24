package com.newproject.ted.emergencyhealthdriver;

public class DriverLocation {

    private double longitude;
    private double latitude;
    private String hospitalname;

    public DriverLocation(){

    }

    public DriverLocation(double latitude, double longitude,String hospitalname){
         this.latitude = latitude;
         this.longitude = longitude;
         this.hospitalname = hospitalname;
    }


    public double getLongitude() {
        return longitude;
    }


    public double getLatitude() {
        return latitude;
    }

    public String getHospitalname() {
        return hospitalname;
    }
}
