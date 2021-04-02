package com.peoplehandstech.helpy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.peoplehandstech.helpy.MyNotifications.CHANNEL_1_ID;


public class GetHelpActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private FirebaseUser fUser;
    private Intent i;
    private FirebaseAuth mAuth;
    public static HashMap<Marker,String> markers;
    public static HashMap<Marker,Uri> photos;
    public static HashMap<String,Uri> usersPhotos;
    public static Activity GET_HELP;
    private static ArrayList<User> allUsers;
    private User currUser;
    private RelativeLayout requestsRL,pendingRequestsCircle;
    private TextView pendingRequestsNumber;
    private ImageView requestsImageView;
    private ProgressBar pbar;
    private int UPDATE_NOTIFICATIONS_CODE=133;
    private Handler handler;
    private final static String TAG="GET_HELP_ACTIVITY";
    private boolean cameraOnCurrUser=false;
    private NotificationManagerCompat nManager;
    boolean notified=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_help);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        GET_HELP =this;
        Toast.makeText(GetHelpActivity.this, getString(R.string.successfully_log_in), Toast.LENGTH_SHORT).show();
        pbar=findViewById(R.id.getHelp_progressBar);
        requestsRL=findViewById(R.id.getHelp_requests_RL);
        requestsImageView=findViewById(R.id.getHelp_requests_ImageView);
        pendingRequestsCircle=findViewById(R.id.getHelp_pendingRequests_circle);
        pendingRequestsNumber=findViewById(R.id.getHelp_pendingRequestNumber);
        markers=new HashMap<>();
        photos=new HashMap<>();
        mAuth=FirebaseAuth.getInstance();
        fUser=mAuth.getCurrentUser();
        handler=new Handler();

        pbar.setVisibility(View.GONE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.viewMap);
        mapFragment.getMapAsync(this);

        nManager= NotificationManagerCompat.from(this);

    }



    private final Runnable refreshData =new Runnable() {
        @Override
        public void run() {
            if(DATABASE.isReadyToGo())
            {
                getData();
                System.out.println(TAG+" refreshing the users");
                DATABASE.setReadyToGo(false);
                DATABASE.setUsers();
                System.out.println(TAG+" setting the data ");
                handler.postDelayed(refreshData,10000);
            }
        }
    };

    @Override
    public void onBackPressed() {

//        startActivity(new Intent(GetHelpActivity.this,PopupWindow.class));
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
    public void goToPendingRequest (View view)
    {

        i=new Intent(GetHelpActivity.this, NotificationsList.class);
        i.putExtra("current user",currUser);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if(currUser!=null)
        {
            startActivityForResult(i,UPDATE_NOTIFICATIONS_CODE);
        }
        else
            return;
    }


    private Marker lastClicked;
    private HashSet<String> markerIdSet=new HashSet<>();

    @Override
    public boolean onMarkerClick(final Marker marker) {
//        if(markers.containsKey(marker))
//        {
//            String currId=markers.get(marker);
//            User userOnMap=DATABASE.getUser(currId);
//            if(userOnMap!=null)
//            {
//                setCameraOnCurrUser(userOnMap);
//            }
//
//        }
        if(!markerIdSet.contains(marker.getId()))
        {
            marker.showInfoWindow();
            marker.hideInfoWindow();
            Handler handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    marker.showInfoWindow();
                }
            },150);
            markerIdSet.add(marker.getId());
            return true;
        }
        else
        {
            if (lastClicked != null && lastClicked.equals(marker)) {
                lastClicked = null;
                marker.hideInfoWindow();
                return true;
            } else {
                lastClicked = marker;
                return false;
            }

        }
    }



    public void getData ()
    {
        if(getIntent().getSerializableExtra("current user with updated notifications")!=null)
        {
            currUser =(User)getIntent().getSerializableExtra("current user with updated notifications");
        }
        else
        {
//            currUser =(User)getIntent().getSerializableExtra("current user");
            currUser=DATABASE.getUser(fUser.getUid());
            if(currUser==null)
            {
                System.out.println(TAG + " current user is null");
            }
            else
            {
                System.out.println(TAG + "current user is not null");
            }
        }


        if(currUser==null)
        {
            Log.d("GET_HELP_ACTIVITY_CURR-USER","its null");

        }
        else
        {
//            Log.d(TAG +" CURR-USER",String.valueOf(currUser.getLatitude())+" ,"+String.valueOf(currUser.getLongitude()));
//            Log.d(TAG+"PHOTOS",String.valueOf(DATABASE.getPhotos().size()));
            UserHandler.setCurrentUser(currUser);
            if(!cameraOnCurrUser)
            {
                setCameraOnCurrUser(currUser);
                cameraOnCurrUser=true;
            }

            showPendingNotifications(currUser.getPending());


        }
        //clear map from markers
        mMap.clear();
        // add markers again in case there is new users
        for(User currentUser:allUsers)
        {
            if(!currentUser.getId().equals(currUser.getId()))
            {

                addMarkers(currentUser);
            }
        }
    }


    private void setCameraOnCurrUser(User currUser)
    {
        LatLng currUserLoc=new LatLng(currUser.getLatitude(),currUser.getLongitude());
        if(currUserLoc!=null)
        {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currUserLoc);

            mMap.moveCamera(cameraUpdate);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currUserLoc.latitude, currUserLoc.longitude), 12.0f));
        }

    }


    private void addMarkers ( User user)
    {
        LatLng userLoc = new LatLng(user.getLatitude(), user.getLongitude());
         Marker marker=mMap.addMarker(new MarkerOptions().position(userLoc).title(user.getName()).snippet(user.getHowToHelp()));
         photos.put(marker,usersPhotos.get(user.getId()));
        marker.setTag(user.getId());
        markers.put(marker, user.getId());
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker);
        marker.setIcon(bitmapDescriptor);
        CustomInfoWindowAdapter currentWindow =new CustomInfoWindowAdapter(getApplicationContext());
        mMap.setInfoWindowAdapter(currentWindow);
        mMap.setOnMarkerClickListener(this);
    }





    public void goToProfile(View view)
    {
        if(currUser!=null)
        {
           i =new Intent(GetHelpActivity.this, UserProfile.class);
            i.putExtra("current User",currUser);
            startActivity(i);
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(20);
        getData();
        DATABASE.setReadyToGo(false);
        DATABASE.setUsers();
        handler.postDelayed(refreshData,10000);

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                String userId=markers.get(marker);
               // Toast.makeText(getApplicationContext(),userId,Toast.LENGTH_SHORT).show();
                User markerUser=DATABASE.getUser(userId);
                if(markerUser!=null)
                {
                    i=new Intent(GetHelpActivity.this,OtherUserProfile.class);
                    i.putExtra("marker user",markerUser);
                    startActivity(i);
                }
                else{
                    //Toast.makeText(getApplicationContext(),"its null",Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        handler.postDelayed(refreshData,3000);
    }

    @Override
    protected void onPause() {

        super.onPause();
//        handler.removeCallbacks(refreshData);

    }




    public  User getUser()
    {
        return currUser;
    }

    public static void setAllUsers(ArrayList<User> allUsers) {
        GetHelpActivity.allUsers = allUsers;
       // Toast.makeText(GET_HELP,"number of children is from get help Activity"+String.valueOf(allUsers),Toast.LENGTH_SHORT).show();
    }


    public static void setUsersPhotos(HashMap<String, Uri> usersPhotos) {
        GetHelpActivity.usersPhotos = usersPhotos;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==UPDATE_NOTIFICATIONS_CODE)
        {
            if(resultCode==Activity.RESULT_OK && data != null)
            {
                currUser=(User)data.getSerializableExtra("updated notifications user");
                System.out.println("GET_HELP_ACTIVITY : changes happened "+String.valueOf(currUser.getPending().size()));
                showPendingNotifications(currUser.getPending());

            }else if (resultCode==Activity.RESULT_CANCELED)
            {
                currUser=(User)data.getSerializableExtra("updated notifications user");
                showPendingNotifications(currUser.getPending());
                System.out.println("GET_HELP_ACTIVITY : there is no changes");
            }
        }
    }
    private void showPendingNotifications (ArrayList<Request> requests)
    { int total=0;
        for (Request currentRequest : requests)
        {
            String title= currentRequest.getTitle();
            if(title.equals(getString(R.string.new_request)) || title.equals("Solicitud nueva"))
            {
                total++;
            }
            else if(title.equals(getString(R.string.request_accepted)) && !currentRequest.isSeen() ||
            title.equals("Solicitud acceptada") && !currentRequest.isSeen())
            {
                total++;
            }
        }

        if(total>0)
        {
            requestsImageView.setImageDrawable(getDrawable(R.drawable.request_mustard));
            pendingRequestsCircle.setVisibility(View.VISIBLE);
            pendingRequestsNumber.setText(String.valueOf(total));
            if(!notified)
            {
                notified=true;
                Notification notification=new NotificationCompat.Builder(this,CHANNEL_1_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground).setContentText("You have a new Notification")
                        .setContentTitle("Hi "+currUser.getName()).setPriority(NotificationCompat.PRIORITY_HIGH).setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE).build();

                nManager.notify(1,notification);
            }

        }else
        {
            notified=false;
            requestsImageView.setImageDrawable(getDrawable(R.drawable.empty_request));
            pendingRequestsCircle.setVisibility(View.GONE);
        }
    }


}
