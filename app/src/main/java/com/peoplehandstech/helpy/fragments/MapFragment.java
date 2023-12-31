package com.peoplehandstech.helpy.fragments;


import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.peoplehandstech.helpy.R;
import com.peoplehandstech.helpy.utilites.UserHandler;
import com.peoplehandstech.helpy.models.User;

import java.io.IOException;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements  OnMapReadyCallback , View.OnClickListener{
    private MapView mMapView;
    private GoogleMap mMap;

    private float cameraInitialZoom=6.0f;
    private Location selectedLocation;
    private String selectedCity;
    private RelativeLayout setLocationRL;
    private ImageView goBack;



    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_map, container, false);

        mMapView =v.findViewById(R.id.map_fragment_mapView);
        setLocationRL=v.findViewById(R.id.map_fragment_setLocation_button_RL);
        goBack=v.findViewById(R.id.map_fragment_goBack_imageView);

        setLocationRL.setOnClickListener(this);
        goBack.setOnClickListener(this);


        mMapView.onCreate(savedInstanceState);
         mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }




        mMapView.getMapAsync(this);


        return v;
    }

    public void onClick (View view)
    {
        if (view.getId()==setLocationRL.getId())
        {
            User currUser= UserHandler.getCurrentUser();
            if(selectedLocation!=null && selectedCity!=null && selectedCity.trim().length()>0)
            {
                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                currUser.setLocation(new com.peoplehandstech.helpy.models.Location(selectedLocation.getLatitude(),selectedLocation.getLongitude()));
                currUser.setLatitude(selectedLocation.getLatitude());
                currUser.setLongitude(selectedLocation.getLongitude());
                HashMap<String,Object>updates=new HashMap<>();
                updates.put("longitude",selectedLocation.getLongitude());
                updates.put("latitude",selectedLocation.getLatitude());
                updates.put("city",selectedCity);
//                UserHandler.setCurrentUser(currUser);
                UserHandler.updateUserInfo(updates,currUser,()->{
                  getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Toast.makeText(getContext(),getString(R.string.changes_has_been_saved),Toast.LENGTH_SHORT).show();
                getFragmentManager().popBackStackImmediate();
                    Log.d("MAP_FRAGMENT","onUserLocationUpdatedComplete");
                });
            }else{
                Toast.makeText(getContext(),getString(R.string.please_set_location),Toast.LENGTH_SHORT).show();
            }

        }
        if(view.getId()==goBack.getId())
        {
            getFragmentManager().popBackStackImmediate();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap =googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setMaxZoomPreference(20);

        // For dropping a marker at a point on the Map
        LatLng sydney = new LatLng(UserHandler.getCurrentUser().getLatitude() ,UserHandler.getCurrentUser().getLongitude());
        mMap.addMarker(new MarkerOptions().position(sydney).title(UserHandler.getCurrentUser().getName()).snippet(getString(R.string.location)));

        // For zooming automatically to the location of the marker
        CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(cameraInitialZoom).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                // clear the map from any marker when user click
                mMap.clear();
                // get the gps provider
                selectedLocation=new Location(LocationManager.GPS_PROVIDER);
                // initialize the latitude and longitude of location to the selected location of the user
                selectedLocation.setLatitude(latLng.latitude);
                selectedLocation.setLongitude(latLng.longitude);
                try{
                selectedCity= com.peoplehandstech.helpy.models.Location.getCityName(latLng.longitude, latLng.latitude, getContext());
                // here we add the marker where the user has clicked
                mMap.addMarker(new MarkerOptions().position(latLng).title(UserHandler.getCurrentUser().getName()));
                }catch(Exception exception){
                    Log.e("MapFragment","could not get the city name "+exception.toString());
                    Toast.makeText(getContext(),"Please select a valid location",Toast.LENGTH_SHORT).show();
                    selectedLocation=null;
                    selectedCity="";
                }
            }
        });
    }
}
