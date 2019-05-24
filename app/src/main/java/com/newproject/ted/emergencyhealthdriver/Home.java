package com.newproject.ted.emergencyhealthdriver;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home extends Application {

    public static  final String CHANNEL_1_ID = "Channel1";
    public static final String CHANNEL_2 = "Channel2";

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if(currentUser != null ){
            Intent intent = new Intent(Home.this, MapsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }


        createNotificationChannel();
    }


    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1 = new NotificationChannel(
              CHANNEL_1_ID,
                    "Ambulance Request",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("A patient has made a request for an ambulance");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }


}
