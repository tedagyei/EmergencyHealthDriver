package com.newproject.ted.emergencyhealthdriver;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PatientInfoActivity extends AppCompatActivity {

    private ListView listView;
    private DatabaseReference mDatabase;
    private ArrayAdapter<String> theadapter;
    private ArrayList<String> list;
    PatientDetails patientDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        list = new ArrayList<>();
        patientDetails = new PatientDetails();
        listView = (ListView) findViewById(R.id.newlistview);


        theadapter = new ArrayAdapter<String>(this,R.layout.userinfo,list);

       getPatientId();
    }




    private String patientid;
    private void getPatientId() {
        //Trying to get the patient id from the database
        DatabaseReference getpatient = mDatabase.child("users").child("driver");

        getpatient.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //getting the key of the current driver
                String currentdriver = FirebaseAuth.getInstance().getCurrentUser().getUid();

                //Debugging
                //Log.d("WTF", "Current driverid "+currentdriver);

                for (DataSnapshot test : dataSnapshot.getChildren()) {
                    String driverkey = test.getKey();

                    //Debugging
                    //Log.d("WTF", "Current driverINDB"+driverkey);

                    if (currentdriver.equals(driverkey)) {
                        patientid = (String) test.child("patientid").getValue();

                        //Debugging
                        Log.d("WTF", "Patient who has requested ambulance"+ patientid);
                    }
                }

                getpatientdetails();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    private void getpatientdetails(){

        //Debugging
        Log.d("WTF", "Testing get patient details"+ patientid);


        DatabaseReference patientdetails = mDatabase.child("patientdetails");

        patientdetails.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot test: dataSnapshot.getChildren()){
                        String key = test.getKey();
                        if(key.equals(patientid)){
                            //Write code to pass the information into the listview
                            //Debugging
                            Log.d("WTF", "The inner if is working"+ patientid);
                            patientDetails = test.getValue(PatientDetails.class);
                            list.add(0,"Name: "+ patientDetails.getName());
                            list.add(1, "Age: " + patientDetails.getAge());
                            list.add(2, "Phone Number: " +patientDetails.getPhonenumber());
                            list.add(3, "Blood Type: " + patientDetails.getBloodtype());
                            list.add(4, "Allergies:  " + patientDetails.getAllergies());
                            list.add(5, "Medical Condition: "+ patientDetails.getMedicalcondition());



                        }

                        listView.setAdapter(theadapter);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
