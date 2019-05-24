package com.newproject.ted.emergencyhealthdriver;

public class PatientDetails {

    private String age;
    private String allergies;
    private String bloodtype;
    private String gender;
    private String medicalcondition;
    private String name;
    private String phonenumber;
    private String userid;

    public PatientDetails(){

    }

    public PatientDetails(String age, String allergies, String bloodtype, String gender, String medicalcondition, String name, String phonenumber, String userid) {
        this.age = age;
        this.allergies = allergies;
        this.bloodtype = bloodtype;
        this.gender = gender;
        this.medicalcondition = medicalcondition;
        this.name = name;
        this.phonenumber = phonenumber;
        this.userid = userid;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getBloodtype() {
        return bloodtype;
    }

    public void setBloodtype(String bloodtype) {
        this.bloodtype = bloodtype;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMedicalcondition() {
        return medicalcondition;
    }

    public void setMedicalcondition(String medicalcondition) {
        this.medicalcondition = medicalcondition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
