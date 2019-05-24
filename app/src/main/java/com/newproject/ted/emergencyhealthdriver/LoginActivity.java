package com.newproject.ted.emergencyhealthdriver;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity  {

    private FirebaseAuth mAuth;

    private static String TAG = LoginActivity.class.getName();

    private AutoCompleteTextView emailview;
    private EditText passwordview;
    private ProgressBar spinner;
    private View mLoginFormView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        //Initializing the spinner
        spinner = (ProgressBar)findViewById(R.id.login_progress);
        spinner.setVisibility(View.GONE);

        emailview = findViewById(R.id.email);
        passwordview = findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.login_form);
    }


    public void SigninOnClickHandler(View view) {

        String email = emailview.getText().toString();
        String password = passwordview.getText().toString();
        validateForm(email,password);
        login(email,password);
    }

    private void login(String email, String password) {

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            Toast.makeText(getApplicationContext(),"Please Fill all fields",Toast.LENGTH_SHORT).show();
        }else {
            mLoginFormView.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {


                        Log.e(TAG, "signIn: Success!");

                        //Update UI with the information of the current user
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Log.e(TAG, "signIn: Failed", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();

                        updateUI(null);

                    }

                }
            });

        }


    }


    private void validateForm(String email, String password){
        if(!email.contains("@")){
            emailview.setError("Enter correct email address");

        }

        if(TextUtils.isEmpty(email)){
            emailview.setError("Enter email address");

        }
        if(TextUtils.isEmpty(password)){
            passwordview.setError("Enter password");

        }




    }




    private void updateUI(FirebaseUser user){

        if (user != null) {
            Intent intent = new Intent(this,MapsActivity.class);
            startActivity(intent);



            Toast.makeText(LoginActivity.this, "The user is signed in",Toast.LENGTH_SHORT).show();
        } else {

            mLoginFormView.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.GONE);
            Toast.makeText(LoginActivity.this,"The user is not signed in ",Toast.LENGTH_SHORT).show();
        }

    }


}

