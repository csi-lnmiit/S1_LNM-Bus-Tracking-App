package com.itskshitizsh.bustrajectory;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 101; // Request Code which will be passed to onRequestPermissionResult
    private static final int MY_PERMISSION_REQUEST_COARSE_LOCATION = 102;
    Marker marker;
    private GoogleMap mMap;
    private FusedLocationProviderApi locationProvider = LocationServices.FusedLocationApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Double myLatitude;
    private Double myLongitude;
    private boolean permissionIsGranted = false;

    FirebaseAuth mAuth;
    List<User> coordinate;
    FirebaseDatabase database;
    DatabaseReference myRef;

    Calendar calendar;
    SimpleDateFormat simpleDateFormat;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Drivers");

        coordinate = new ArrayList<>();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
        } else {
            permissionIsGranted = true;
            //Toast.makeText(getApplicationContext(), "Location permission already granted! ", Toast.LENGTH_SHORT).show();
        }
        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10 * 1000); //Looking to the national provider every 10 seconds.
        locationRequest.setFastestInterval(5 * 1000); // See if the location available so we're gonna set up fastest interval
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Set priority to which provider we use.
        myLatitude = 26.9363;   // Initializing with latitute and longitude of LNMIIT, Jaipur.
        myLongitude = 75.9235;  // 26.9363° N, 75.9235° E
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng sydney = new LatLng(myLatitude, myLongitude);
        marker = mMap.addMarker(new MarkerOptions().position(sydney).title("Marker is your location."));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    @Override
    public void onConnected(Bundle bundle) {
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
            } else {
                permissionIsGranted = true;
            }
            return;
        }
        // Location Request will setup here.
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        myLatitude = location.getLatitude();
        myLongitude = location.getLongitude();

        Toast.makeText(getApplicationContext(), getString(R.string.values_updated), Toast.LENGTH_SHORT).show();
        marker.remove();
        LatLng next = new LatLng(myLatitude, myLongitude);
        MarkerOptions mk = new MarkerOptions().position(next).title(getString(R.string.myLoc));
        mk.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon128));
        marker = mMap.addMarker(mk);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(next));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));

        uploadCoordinates2FireBase();   //this method is actually performing the write operation
    }

    /*
     * This method is saving a new set of Coordinates to the
     * Firebase Real Time Database
     * */
    private void uploadCoordinates2FireBase() {

        String coordin = myLatitude + " , " + myLongitude;
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);     // Date and Time in 24hrs
        String temp = simpleDateFormat.format(calendar.getTime());
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null)      // Whenever a user is Logged In
        {
            //getting a unique id using push().getKey() method
            //it will create a unique id and we will use it as the Primary Key for our Storage
            String id = myRef.push().getKey();
            // Creating an user object
            User user = new User(id, coordin, temp);
            // saving the user
            myRef.child(id).setValue(user);
            // displaying a success toast
            Toast.makeText(this, getString(R.string.successfullyUploaded) + myLatitude + "° N, " + myLongitude + "° E", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (permissionIsGranted) {
            if (googleApiClient.isConnected()) {
                requestLocationUpdates();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (permissionIsGranted) // Suspend services on pause
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (permissionIsGranted) googleApiClient.disconnect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    permissionIsGranted = true;
                } else {
                    //permission denied
                    permissionIsGranted = false;
                    Toast.makeText(getApplicationContext(), "This app requires location permission to be granted", Toast.LENGTH_SHORT).show();
                    showSettingsAlert();
                }
                break;
            case MY_PERMISSION_REQUEST_COARSE_LOCATION:
                // do something for coarse location
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menuObj) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.goto_profile, menuObj);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuLogout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.menuProfile:
                startActivity(new Intent(MapsActivity.this, ProfileActivity.class));
                break;
        }
        return true;
    }

    private void showSettingsAlert() {
        AlertDialog.Builder al = new AlertDialog.Builder(MapsActivity.this);
        al.setMessage(getString(R.string.permission_message)).setCancelable(false).setPositiveButton(getString(R.string.settingbtn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Opening Location Settings
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                Intent switchTo = new Intent(MapsActivity.this, ProfileActivity.class);
                startActivity(switchTo);
            }
        });
        AlertDialog alertDialog = al.create();
        alertDialog.setTitle(getString(R.string.permission_title));
        alertDialog.show();

    }
}