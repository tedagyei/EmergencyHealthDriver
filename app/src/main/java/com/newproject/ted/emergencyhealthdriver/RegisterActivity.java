package com.newproject.ted.emergencyhealthdriver;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    private static final String TAG = RegisterActivity.class.getName();
    private AutoCompleteTextView emailview;
    private AutoCompleteTextView nameview;
    private EditText passwordview;
    private EditText confirmpasswordview;
    private EditText phonenumberview;


    //Variables to to used to create a new account
    String email;
    String password;
    String confirmpassword;
    String name;
    String userId;
    String phonenumber;
    String patientid = "empty";
    String patientrequest = "none";


    //Variables to store the coordinates of the hospitals for the map
    private static final double southlat=6.6512641;
    private static final  double southlong=-1.5886292;
    private static final double techlong=-1.574394;
    private static final double techlat=6.686248;
    private static final double bomsolat=6.6844354;
    private static final double bomsolong=-1.5815669;
    private String hospitalname;

    private double actuallatitude;
    private double actuallongitude;
    private String instanceid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailview = findViewById(R.id.register_email);
        passwordview = findViewById(R.id.register_password);
        confirmpasswordview = findViewById(R.id.confirm_password);
        nameview = findViewById(R.id.register_name);
        phonenumberview= findViewById(R.id.phone_number);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //For the dropdown menu
        Spinner spinner = findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.hospitals,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }


    public void newaccountClickHandler(View view) {


        //Changing all the values of the views to strings
        email = emailview.getText().toString();
        password = passwordview.getText().toString();
        confirmpassword = confirmpasswordview.getText().toString();
        name = nameview.getText().toString();
        phonenumber = phonenumberview.getText().toString();
        //userId = mAuth.getUid();



        //Checking to see if  the  name field is empty
        if (TextUtils.isEmpty(name)) {
            nameview.setError("This field is required");

        }

        //Checking to see if  the  email field is empty
        if (TextUtils.isEmpty(email)) {
            emailview.setError("This field is required");

        }

        //Checking to see if  the  password field is empty
        if (TextUtils.isEmpty(password)) {
            passwordview.setError("This field is required");

        }

        //Checking to see if  the  confirmpassword field is empty
        if (TextUtils.isEmpty(confirmpassword)) {
            confirmpasswordview.setError("This field is required");

        }


        //checking if the email is valid
        if (!isEmailValid(email)) {
            emailview.setError("This email is invalid");
        }


        //checking if the passwords match
        if (!confirmPassword(password, confirmpassword)) {
            confirmpasswordview.setError("The passwords do not match");
            passwordview.setError("The passwords do not match");
        }


        //Create a new account and log the driver in
       createAccount(email, password, confirmpassword);


        //Working with the driver class
        //Driver driver = new Driver(name, email, userId);

        //putting the values into the database
        //addUser(name, email, userId, driver);


    }


    //Method to store the driver's info in the
    private void addUser(String name, String email, String userId, Driver driver) {

        mDatabase.child("users").child("driver").child(userId).setValue(driver);

    }


    private void addDriverLocation(String userId, DriverLocation driverLocation){
        mDatabase.child("driverhospitallocation").child(userId).setValue(driverLocation);
    }


    public boolean isEmailValid(String email) {
        if (email.contains("@")) {
            return true;
        } else {
            return false;
        }

    }

    public boolean confirmPassword(String password, String passwordconfirm) {

        if (password.equals(passwordconfirm))
            return true;
        else {
            return false;
        }

    }


    //method to create an account
    private void createAccount(final String email, final String password, final String confirmpassword) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {

            Toast.makeText(getApplicationContext(), "Please Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        } else if(!confirmPassword(password,confirmpassword)) {
            Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
            else{

                //Creating an account for the user
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Sign in success, updateUI with the signed-in user's information
                            Log.e(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);

                              //Creating a user and storing the data in the database
                              userId = user.getUid();


                              //Put the instance token here
                            FirebaseInstanceId.getInstance().getInstanceId()
                                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                            if (!task.isSuccessful()) {
                                                Log.w(TAG, "getInstanceId failed", task.getException());
                                                return;
                                            }


                                            // Get new Instance ID token
                                             instanceid = task.getResult().getToken();
                                            //Working with the driver class
                                            Driver driver = new Driver(name, email, userId, phonenumber,patientid,patientrequest,instanceid);

                                            //putting the values into the database
                                            addUser(name, email, userId, driver);

                                            //After creating a driver associate the driver  with a location
                                            DriverLocation driverLocation = new DriverLocation(actuallatitude,actuallongitude,hospitalname);

                                            //Create a location field in the database
                                            addDriverLocation(userId,driverLocation);



                                            // Log and toast
                                            //String msg = "The token is now working";
                                            //Log.d("Token", token);
                                            //Toast.makeText(MapsActivity.this, msg, Toast.LENGTH_SHORT).show();


                                        }
                                    });




                        } else {
                            //If sign in fails, display a message to the user
                            Log.e(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }

                });

            }


    }


    //method from starkoverflow
    private void updateUI(FirebaseUser user) {

        if (user != null) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);


            Toast.makeText(RegisterActivity.this, "The user is signed in", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(RegisterActivity.this, "The user is not signed in ", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        switch(i){
            case 0:
                actuallatitude=techlat;
                actuallongitude=techlong;
                hospitalname = "Knust Hospital";

                break;

            case 1:
                actuallatitude=southlat;
                actuallongitude=southlong;
                hospitalname = "Kumasi South Hospital";


                break;

            case 2:
                actuallatitude=bomsolat;
                actuallongitude=bomsolong;
                hospitalname = "Bomso Clinic";


                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
