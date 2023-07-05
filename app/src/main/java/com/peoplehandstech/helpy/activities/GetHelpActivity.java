package com.peoplehandstech.helpy.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.peoplehandstech.helpy.UsersFetchingCallback;
import com.peoplehandstech.helpy.utilites.DATABASE;
import com.peoplehandstech.helpy.R;
import com.peoplehandstech.helpy.utilites.RequestHandler;
import com.peoplehandstech.helpy.utilites.UserHandler;
import com.peoplehandstech.helpy.adapters.CustomInfoWindowAdapter;
import com.peoplehandstech.helpy.models.Request;
import com.peoplehandstech.helpy.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.peoplehandstech.helpy.notification.MyNotifications.CHANNEL_1_ID;


public class GetHelpActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static GetHelpActivity activity;


    private float cameraInitialZoom=6.0f;
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
    private EditText whatHelpYouNeedEDTTXT;
    private RelativeLayout searchRLButton;
    private static RelativeLayout unreadMessage;
    private RelativeLayout requestsRL,pendingRequestsCircle;
    private TextView pendingRequestsNumber;
    private ImageView searchImage;
    private ImageView requestsImageView,friendsImageView;
    private ProgressBar pbar;
    private int UPDATE_NOTIFICATIONS_CODE=133;
    private Handler handler;
    private final static String TAG="GET_HELP_ACTIVITY";
    private boolean cameraOnCurrUser=false;
    private NotificationManagerCompat nManager;
    boolean notified=false;
    private ArrayList<User> filteredUsers;
    private boolean searched;



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
        friendsImageView=findViewById(R.id.friends_ImageView);
        pendingRequestsCircle=findViewById(R.id.getHelp_pendingRequests_circle);
        pendingRequestsNumber=findViewById(R.id.getHelp_pendingRequestNumber);
        markers=new HashMap<>();
        photos=new HashMap<>();
        filteredUsers=new ArrayList<>();
        mAuth=FirebaseAuth.getInstance();
        fUser=mAuth.getCurrentUser();
        handler=new Handler();
        unreadMessage=findViewById(R.id.getHelp_unReadMessage_circle);
        whatHelpYouNeedEDTTXT =findViewById(R.id.what_help_you_need_editText);
        searchRLButton=findViewById(R.id.search_relativeLayout_getHelp);
        searchImage=findViewById(R.id.searchImage);
        searched=false;
        activity=this;

        pbar.setVisibility(View.GONE);
        if(getIntent().getSerializableExtra("current user from phone verification")!=null){
            currUser =(User)getIntent().getSerializableExtra("current user from phone verification");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.viewMap);
        mapFragment.getMapAsync(this);

        nManager= NotificationManagerCompat.from(this);


        friendsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =new Intent(GetHelpActivity.this, FriendsListActivity.class);
                startActivity(i);
            }
        });


        //filter users logic
        searchRLButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(whatHelpYouNeedEDTTXT.getText())){
                    whatHelpYouNeedEDTTXT.setError("Please enter a keyword..");
                    return;
                }else{
                    if(!searched){
                        //search for users with the specific keyword in their help way
                        filteredUsers.clear();
                        filteredUsers=UserHandler.filterUser(whatHelpYouNeedEDTTXT.getText().toString());
                        if(filteredUsers.size()==0){
                            Toast.makeText(GetHelpActivity.this,"Did not find results",Toast.LENGTH_LONG).show();
                        }
                        showData(filteredUsers);
                        //change the button image
                        searchRLButton.getBackground().setColorFilter(getColor(R.color.almostGrey), PorterDuff.Mode.SRC_ATOP);
                        searchImage.setImageResource(R.drawable.ic_close_black_24dp);
                        searched=true;

                        //hide keyboard
                        // Check if no view has focus:
                        View view = GetHelpActivity.this.getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }

                    }else{
                        //search is already done get all users back
                            searched=false;
                            showData();
                        //empty the edit text
                        whatHelpYouNeedEDTTXT.setText("");
                        //change the button image
                        searchRLButton.getBackground().setColorFilter(getColor(R.color.brown), PorterDuff.Mode.SRC_ATOP);
                        searchImage.setImageResource(R.drawable.ic_search_black_24dp);
                    }

                }
            }
        });

        DATABASE.listenToNewPhotos();
        DATABASE.removeMessageListener();

    }

    public static GetHelpActivity instance (){
        return activity;
    }


    public static void showUnreadMessage (boolean show){
        if(unreadMessage!=null){
            if (show){
                unreadMessage.setVisibility(View.VISIBLE);
            }else{
                unreadMessage.setVisibility(View.GONE);
            }
        }
    }

    public static boolean getUnreadMessageVisibility (){
        if(unreadMessage!=null){
            if(unreadMessage.getVisibility()== View.VISIBLE){
                return  true;
            }else{
                return  false;
            }
        }
        return false;

    }


//    private final Runnable refreshData =new Runnable() {
//        @Override
//        public void run() {
//            if(DATABASE.isReadyToGo())
//            {
//                showData();
//                System.out.println(TAG+" refreshing the users");
//                DATABASE.setReadyToGo(false);
//                DATABASE.setUsers(currUser.getCity());
//                System.out.println(TAG+" setting the data ");
////                handler.postDelayed(refreshData,10000);
//            }
//        }
//    };

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

        i=new Intent(GetHelpActivity.this, NotificationsListActivity.class);
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
    // this method is used to show only users with a specific criteria
    public void showData (ArrayList<User> filteredUsers){
        mMap.clear();
        // add markers again in case there is new users
        for(User currentUser:filteredUsers)
        {
            if(currentUser!=null && !currentUser.getId().equals(currUser.getId()))
            {

                addMarkers(currentUser);
            }
        }
    }

    public void showData()
    {
        if(getIntent().getSerializableExtra("current user with updated notifications")!=null)
        {
            currUser =(User)getIntent().getSerializableExtra("current user with updated notifications");
        }
        else
        {
//            currUser =(User)getIntent().getSerializableExtra("current user");
            if(DATABASE.getUser(fUser.getUid())!=null){
                currUser=DATABASE.getUser(fUser.getUid());
            }

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

            //showPendingNotifications(currUser.getPending());


        }
        //clear map from markers
        mMap.clear();
        allUsers=DATABASE.getUsers();
        currUser=UserHandler.getCurrentUser();
        // add markers again in case there is new users
        for(User currentUser:allUsers)
        {
            if(currentUser!=null && !currentUser.getId().equals(currUser.getId()))
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
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currUserLoc.latitude, currUserLoc.longitude),cameraInitialZoom));
        }

    }


    private void addMarkers (@NonNull User user)
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
           i =new Intent(GetHelpActivity.this, UserProfileActivity.class);
            i.putExtra("current User",currUser);
            startActivity(i);
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(20);
        showData();
        refreshNotifications();

//        refreshUsers(this.userCallback);

        //DATABASE.setUsers(currUser.getCity());
        //handler.postDelayed(refreshData,10000);



        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                String userId=markers.get(marker);
               // Toast.makeText(getApplicationContext(),userId,Toast.LENGTH_SHORT).show();
                User markerUser=DATABASE.getUser(userId);
                if(markerUser!=null)
                {
                    i=new Intent(GetHelpActivity.this, OtherUserProfileActivity.class);
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
        Toast.makeText(getApplicationContext(),"ONRESUME",Toast.LENGTH_SHORT).show();
        if(UserHandler.isLocationChanged()){
            cameraOnCurrUser=false;
            this.showData();
            UserHandler.setLocationChanged(false);
        }
//        handler.postDelayed(refreshData,3000);
    }

    @Override
    protected void onPause() {

        super.onPause();
//        handler.removeCallbacks(refreshData);

    }

    private void refreshUsers (UsersFetchingCallback userCallback){

        Log.d(TAG,"refreshUsers method >>> processing");
        DATABASE.setUsers(UserHandler.getCurrentUser().getCity(),userCallback);
    }

    private void refreshNotifications () {


        Log.d(TAG,"number of requests "+String.valueOf(currUser.getPending().size()));
        if(currUser.getPending().size()==0){
            requestsImageView.setImageDrawable(getDrawable(R.drawable.ic_baseline_notifications_24));
            pendingRequestsCircle.setVisibility(View.GONE);
        }

        DatabaseReference notificationReff = FirebaseDatabase.getInstance().getReference().child(currUser.getCity()).child(currUser.getId()).child(Request.TAG);
        notificationReff.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {

                        Log.d(TAG,"there is a new pending notification");
                        DATABASE.refreshNotifications(currUser);
                        Runnable checkIfNotificaionUpdated=new Runnable() {
                            @Override
                            public void run() {
                                if (DATABASE.NOTI_REFRESHED){
                                    showPendingNotifications(currUser.getPending());
                                }    else{
                                    handler.postDelayed(this,1000);
                                }
                            }
                        };
                        handler.postDelayed(checkIfNotificaionUpdated,1000);
                }


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {

                    Log.d(TAG,"there is a new pending notification");
                    DATABASE.refreshNotifications(currUser);
                    Runnable checkIfNotificaionUpdated=new Runnable() {
                        @Override
                        public void run() {
                            if (DATABASE.NOTI_REFRESHED){
                                showPendingNotifications(currUser.getPending());
                            }    else{
                                handler.postDelayed(this,1000);
                            }
                        }
                    };
                    handler.postDelayed(checkIfNotificaionUpdated,1000);
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    Log.d(TAG,"there is a new pending notification");
                    DATABASE.refreshNotifications(currUser);
                    Runnable checkIfNotificaionUpdated=new Runnable() {
                        @Override
                        public void run() {
                            if (DATABASE.NOTI_REFRESHED){
                                showPendingNotifications(currUser.getPending());
                            }    else{
                                handler.postDelayed(this,1000);
                            }
                        }
                    };
                    handler.postDelayed(checkIfNotificaionUpdated,1000);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

    public static void updateMarkerPhoto(String userID,Uri uri){
        for(Marker marker: photos.keySet()){
            if(marker.getTag().equals(userID))
            {
                photos.put(marker,uri);
            }
        }
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
            Log.d(TAG,"this is the total of notifiactions and requests  from ShowPendingNotifications method "+total);
            requestsImageView.setImageDrawable(getDrawable(R.drawable.ic_baseline_notifications_active_24));
            pendingRequestsCircle.setVisibility(View.VISIBLE);
            pendingRequestsNumber.setText(String.valueOf(total));
            if(!notified)
            {
                notified=true;
                Notification notification=new NotificationCompat.Builder(this,CHANNEL_1_ID)
                        .setSmallIcon(R.drawable.ic_baseline_notifications_24).setContentText("You have a new Notification")
                        .setContentTitle("Hi "+currUser.getName()).setPriority(NotificationCompat.PRIORITY_HIGH).setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE).build();

                nManager.notify(1,notification);
            }

        }else
        {
            notified=false;
            requestsImageView.setImageDrawable(getDrawable(R.drawable.ic_baseline_notifications_24));
            pendingRequestsCircle.setVisibility(View.GONE);
        }
    }

    private UsersFetchingCallback userCallback=new UsersFetchingCallback() {
        @Override
        public void onLocationReady(String country) {

        }

        @Override
        public void onLocationNull() {

        }

        @Override
        public void onLocationError() {

        }

        @Override
        public void onUsersSet() {

        }
    };
}
