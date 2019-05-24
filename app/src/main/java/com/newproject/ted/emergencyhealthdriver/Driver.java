package com.newproject.ted.emergencyhealthdriver;

public class Driver {

    public String name;
    public String email;
    public String userId;
    public String phonenumber;
    public String patientid;
    public String patientrequest;
    public String instanceid;


    //constructor with no values
    public Driver(){

    }

    public Driver(String name, String email , String userId, String phonenumber,String patientid,String patientrequest, String instanceid){
        this.name = name;
        this.email = email;
        this.userId = userId;
        this.phonenumber = phonenumber;
        this.patientid = patientid;
        this.patientrequest = patientrequest;
        this.instanceid = instanceid;


    }


}
