package com.newproject.ted.emergencyhealthdriver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //Variables to try and current updated location
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;

    //Variable to help show notification
    private NotificationManagerCompat notificationManager;


    //variables just to test youtube video
    private MarkerOptions place1, place2;

    private Polyline currentPolyline;

    private LatLng driver;
    private LatLng patient;


    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference plocation;

    //variables to store driver location from the database
    private double driverlatitude;
    private double driverlongitude;
    private double patientlatitude;
    private double patientlongitude;


    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    //variables to test reading from the database
    //Variable to store username
    private String username;


    private String status;


    //This is a variable to test the routes
    private LatLng driverLocation;


    private LatLng patientLocation;


    private DatabaseReference availabledriver;


    //Variables to show the driver information
    private TextView notify;
    private CardView drivernotify;

    private TextView patientnumber;

    //Accept request button
    private Button accept_request;


    //Button to show patient details
    private Button patientdetails;

    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};


    private static final int DEFAULT_ZOOM = 15;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private CameraPosition mCameraPosition;

    private String userid;

    private static final String TAG = MapsActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        setContentView(R.layout.activity_maps);

        //Initializing the notification
        notificationManager = NotificationManagerCompat.from(this);


         userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //Testing to see if youtube video works
        //polylines = new ArrayList<>();

        //connecting to the firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();


        //Textview to keep patient updated when he or she makes a request
        notify = findViewById(R.id.driver_text);

        //Cardview to show the patient updated information
        drivernotify = findViewById(R.id.card);


        patientnumber = findViewById(R.id.patient_number);
        patientnumber.setVisibility(View.GONE);


        //Button to accept the patient request
        accept_request = findViewById(R.id.accept_request);
        accept_request.setVisibility(View.GONE);


        //Button to show patient details
        patientdetails = findViewById(R.id.showpatient);
        patientdetails.setVisibility(View.GONE);


        //showing information in the textview
        //This part has to be changed if you change the logic of the app
        notify.setText("Welcome to Emergency Health Driver");


        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // Getting the token to be used for firebase cloud messaging
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }


                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        String msg = "The token is now working";
                        Log.d("Token", token);
                        //Toast.makeText(MapsActivity.this, msg, Toast.LENGTH_SHORT).show();


                    }
                });



    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));



        checkRequest();

    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(9000);
        mLocationRequest.setFastestInterval(9000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);




        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastKnownLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

        //Commenting this out to see if it works in the map
       // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom((latLng), DEFAULT_ZOOM));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        try {

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("currentdriverlocation");
            GeoFire geofire = new GeoFire(ref);
            geofire.setLocation(userid, new GeoLocation(location.getLatitude(), location.getLongitude()), new
                    GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            Log.e(TAG, "GeoFire Complete");
                        }
                    });



        }catch (Exception e){
            Log.d("WTF", "The user has signed out");
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }















    /*

    //COMMENTING OUT ALL OF MY CODE TO SEE IF GEOFIRE WILL WORK
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }

    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // Prompt the patient for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();


        //Get the current location of the device and set the position of the map.
        //uncomment this if you are facing difficulties!!!
        getDeviceLocation();




        //Get the current location of the driver and the patient
        //getPatientLocation();


        //Trying to get the route
        // new FetchURL(MapsActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition(), "driving"), "driving");

        //getDriverLocation();

        //getPatientId();

        //Calling checkrequest to see if there is there is a request
        checkRequest();


    }






    //Method to get permission from the driver to access location
    private void getLocationPermission() {
        //
         // Request location permission, so that we can get the location of the
         // device. The result of the permission request is handled by a callback,
         //onRequestPermissionsResult.
         //
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

    }


    //Method to handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permission[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                //If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;


                }
            }
            updateLocationUI();
        }
    }

    public void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }

        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    private void getDeviceLocation() {
        //
         // Get the best and most recent location of the device, which may be null in rare
         //cases when a location is not available.

        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            //Set the map's camera position to the current location of the device
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults");
                            Log.e(TAG, "Exception: &s,", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        } catch (NullPointerException e) {
            Log.e("Exception", e.getMessage());

        }


    }

    */














    //variables used in getdriverlocation
    private String driverId;
    //This is the code to get the driver whose location is tech hospital

    private String patientid;


    //THis method is supposed to get the location of the patient stored in patient request
    //I need to loop through the patient location because there is more than one
    //patient request
    //THere is currently an error with this method so deal with it. The latlng is returning null
    private void getPatientLocation() {

        //Trying to get the patient latitude and longitude from the database
        DatabaseReference patientlocation = mDatabase.child("patientrequest");

        patientlocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot test : dataSnapshot.getChildren()) {
                    String testvalue = test.getKey();


                    if (testvalue.equals(patientid)) {
                        patientlatitude = (double) test.child("latitude").getValue();
                        patientlongitude = (double) test.child("longitude").getValue();



                    }


                }
                patientLocation = new LatLng(patientlatitude, patientlongitude);
                getDriverLocation();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





    }


    //currently debugging getpatientid
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


                    }
                }



                getPatientLocation();
                getpatientdetails();
                changeRequestStatus();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





    }

    private String patientname;
    private String phonenumber;
    //Creating a method to get the patient details
    private void getpatientdetails(){

        DatabaseReference patientdetails = mDatabase.child("patientdetails");

        patientdetails.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot test: dataSnapshot.getChildren()){
                        String key = test.getKey();
                        if(key.equals(patientid)){
                            //Write code to show name and email of the patient
                            patientnumber.setVisibility(View.VISIBLE);
                            patientname = test.child("name").getValue().toString();
                            phonenumber = test.child("phonenumber").getValue().toString();
                            notify.setText("Patient name: "+ patientname);
                            patientnumber.setText("Patient Phone Number: " + phonenumber);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    //Method to Change the status of an ambulance request
    private void changeRequestStatus(){
        //Code to tell the patient that the request has been received
        status = "accepted";

        DatabaseReference request = mDatabase.child("patientrequest").child(patientid);

        HashMap<String,Object> map = new HashMap<>();
        map.put("status",status);

        request.updateChildren(map);


    }


    private void getDriverLocation() {

        availabledriver = mDatabase.child("driverhospitallocation").child(userid);

        availabledriver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {


                        driverlatitude = (double) dataSnapshot.child("latitude").getValue();
                        driverlongitude = (double) dataSnapshot.child("longitude").getValue();


                    driverLocation = new LatLng(driverlatitude, driverlongitude);

                    //Debugging
                    String patienttest = String.valueOf(patientlatitude);
                    String patienttester = String.valueOf(patientlongitude);




                    place1 = new MarkerOptions().position(driverLocation).title("Driver Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.ambulancemarker));
                    place2 = new MarkerOptions().position(patientLocation).title("Patient Location");


                    mMap.addMarker(place1);
                    mMap.addMarker(place2);

                    new FetchURL(MapsActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition(), "driving"), "driving");
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(driverlatitude, driverlongitude), DEFAULT_ZOOM));


                    //testing to see if the moving the camera on the map will work
                    // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(driverlatitude, driverlongitude), DEFAULT_ZOOM));
                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }


        });


    }

    //variables to help in checkrequest method
    private String patientrequestid;
    private double newpatientlatitude;
    private  double newpatientlongitude;


    //Method to accept a request
    private void acceptRequest(DataSnapshot dataSnapshot){
        if (dataSnapshot.exists()){


            //Show a request button
            accept_request.setVisibility(View.VISIBLE);
            notify.setText("A patient has made a request for an ambulance");
            //Debugging
            Log.d("WTF", "Accept Request true is working   ");


        }else {
            //show a message showing that there is no current request now
            //hide request button

            //Debugging
            Log.d("WTF", "Accept Request False is working   " );


            accept_request.setVisibility(View.GONE);
            notify.setText("There is currently no request for an ambulance");
        }


    }


    //checking to see if there is a request
    private void checkRequest(){


        /*
        //I changed patientrequest to redundantpatient request
        //If there is any error just change it back!!!!
        DatabaseReference checkrequest = mDatabase.child("extrapatientrequest");

        //This is to test if patientrequest exists in the database
        //This is
        checkrequest.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    //Debugging
                    Log.d("WTF", "The patient request reference exists");
                } else {
                    notify.setText("There is currently no request for  an ambulance");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        checkrequest.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
             if(dataSnapshot.exists()) {

                 //Debugging
                 Log.d("WTF", "OnChildAdded is working");

                 patientrequestid = dataSnapshot.getKey();
                 newpatientlatitude = (double) dataSnapshot.child("latitude").getValue();
                 newpatientlongitude = (double) dataSnapshot.child("longitude").getValue();
             }
            acceptRequest(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()) {
                    //Debugging
                    Log.d("WTF", "OnChildChanged is working");

                    patientrequestid = dataSnapshot.getKey();
                    newpatientlatitude = (double) dataSnapshot.child("latitude").getValue();
                    newpatientlongitude = (double) dataSnapshot.child("longitude").getValue();
                }
                acceptRequest(dataSnapshot);

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //Debugging
                Log.d("WTF", "OnChildRemoved is working");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //Debugging
                Log.d("WTF", "OnChildMoved is working");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Debugging
                Log.d("WTF", "OnCancelled is working");

            }
        });

  */


          //New code to check for patienrequest
        DatabaseReference newcheck = mDatabase.child("users").child("driver").child(userid);
        newcheck.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String patient = dataSnapshot.child("patientrequest").getValue().toString();
                if(patient.equals("none")){
                    notify.setText("There is currently no request for  an ambulance");
                }else {
                    //Show the accept request button
                    accept_request.setVisibility(View.VISIBLE);
                    notify.setText("A patient has made a request for an ambulance");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    public void changeDriverStatus(){
        //changing the value of patientrequest
        String newpatientrequest = "none";
        DatabaseReference patientIdRemove = mDatabase.child("users").child("driver").child(userid);


        HashMap<String,Object> newmap = new HashMap<>();
        newmap.put("patientrequest",newpatientrequest);

        //I just added an onsuccess listener
        patientIdRemove.updateChildren(newmap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Final", "The data has been changed to none once");
            }
        });

        }


    public void AcceptRequestClickHandler(View view) {
        getPatientId();
        //notify.setText("Please follow the route to the location of the patient.");
        accept_request.setVisibility(View.GONE);

        //Deleting the value in redundantlocation
        //If the app stops working just remove this code!!!!!!!!
        DatabaseReference  remove= mDatabase.child("extrapatientrequest");
        remove.removeValue();


        changeDriverStatus();



        //Making the patient details button visible
        patientdetails.setVisibility(View.VISIBLE);


        //Removing the current location marker from the map
        try {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }catch (SecurityException e ){
            Log.e("Exception", e.getMessage() );

        }


    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    //method to get the url
    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }


    public void PatientDetailsClickHandler(View view) {
        Intent intent = new Intent(this,PatientInfoActivity.class);
        startActivity(intent);
    }


    public void LogoutOnClickHandler(View view) {
    FirebaseAuth.getInstance().signOut();
    Intent intent = new Intent(this, MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);

    }}



