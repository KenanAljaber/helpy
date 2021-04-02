package com.peoplehandstech.helpy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class SetLocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Location location;
    private GoogleMap mMap;
    private LocationManager lManager;
    private LocationListener lListener;
    private Button setLoc;
    private boolean clicked;
    private  Location selectedLocation;
    public static boolean isNull;
    private boolean gpsFlag;
    private ProgressBar pbar;
    private TextView waitText;
    private   Intent i;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_location);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setLoc=findViewById(R.id.set_location_setLocation_Button);
        clicked=false;
        lManager=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        pbar=findViewById(R.id.set_location_progressBar);
        waitText=findViewById(R.id.set_location_wait_TextView);
        lListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if(!lManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            buildAlertMessageNoGps(this);


        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(this,new String []{Manifest.permission.ACCESS_FINE_LOCATION},1);

        }
        else
        {
            lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, lListener);
        }



    }



    public void goToSignUp (View view)
    {
        i =new Intent(Intent.ACTION_MAIN);
        // if user clicked on the map we will send the selected location
        if(clicked)
        {
            com.peoplehandstech.helpy.Location loc=new com.peoplehandstech.helpy.Location(selectedLocation.getLatitude(),selectedLocation.getLongitude());
            i.putExtra("user location",loc);
           // i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );

            setResult(SignUp.RESULT_OK,i);
            finish();
        }
        //if not we will send the last known location
        else
        {
            com.peoplehandstech.helpy.Location loc=new com.peoplehandstech.helpy.Location(location.getLatitude(),location.getLongitude());
            i.putExtra("user location",loc);
           // i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
            setResult(SignUp.RESULT_OK,i);
            finish();
        }

    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(20);
        // check if we got the permission to use the gps

            // if user has used any other apps that saved his location we will get this location
            location=getLastKnownLocation();
            if(location!=null)
            {
                isNull=false;
                LatLng yourLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(yourLocation).title(getString(R.string.your_location)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12.0f));
            }
            else
            {
                Handler handler=new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        SetLocationActivity.this.finish();
                    }
                },1000);
                pbar.setVisibility(View.VISIBLE);
                waitText.setVisibility(View.VISIBLE);


            }

        //making a instance of on map listener
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //update the clicked flag to trace whether the user select location by himself or not
                clicked=true;
                // clear the map from any marker when user click
                mMap.clear();
                // get the gps provider
             selectedLocation=new Location(LocationManager.GPS_PROVIDER);
               // initialize the latitude and longitude of location to the selected location of the user
                selectedLocation.setLatitude(latLng.latitude);
                selectedLocation.setLongitude(latLng.longitude);
               // here we add the marker where the user has clicked
                mMap.addMarker(new MarkerOptions().position(latLng).title("your location"));

            }
        });

    }
    /*
    this method is a helper method to get the last known location of the user in case
    if the GPS is turned off, how it works:
    loop through all location providers and see which one
    is not null to get the last know location of the user
   */
    private Location getLastKnownLocation() {
        List<String> providers = lManager.getProviders(true);
        Location bestLocation = null;
        Location l;
        for (String provider : providers) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
            {
                l = lManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null
                        || l.getAccuracy() < bestLocation.getAccuracy()) {
                    //  ALog.d("found best last known location: %s", l);
                    bestLocation = l;
                }
            }


        }
        if (bestLocation == null) {
            return null;
        }
        return bestLocation;
    }
    public  boolean buildAlertMessageNoGps(Context context) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(getString(R.string.your_gps_is_not_working))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        gpsFlag =true;
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        gpsFlag =false;
                        dialog.cancel();


                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
        return gpsFlag;
    }
}
