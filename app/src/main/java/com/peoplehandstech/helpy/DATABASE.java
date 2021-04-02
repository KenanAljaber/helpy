package com.peoplehandstech.helpy;


import android.net.Uri;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.HashMap;
public class DATABASE {

    private static final String TAG = "DATABASE";
    private static FirebaseDatabase fDatabase = FirebaseDatabase.getInstance();
    private static DatabaseReference databaseRef = fDatabase.getReference();
    private static boolean READY_TO_GO = false;
    private static ArrayList<User> users = new ArrayList<>();
    private static HashMap<String, Uri> photos = new HashMap<>();
    private static HashMap<String ,ArrayList<Request>>requestMap=new HashMap<>();
    private static HashMap<String ,ArrayList<Rating>> ratingMap=new HashMap<>();
    /**
     * {@link #TAG} represents the class name tag for debug issues
     * {@link #fDatabase} an instance of the fireBase database class
     * {@link #fDatabase} a reference to the database instance
     * {@link #READY_TO_GO} flag to trace if fetching data from database is done
     * {@link #users} the arrayList that will carry all the users
     * {@link #photos} a hashMap carry all profile photos of users
     */

    /**
     * use this method to check whether {@param email} is already exists
     * @param email the email that you need to check it
     * @param userId the user id who want to use this mail
     * @return
     */



    public static boolean checkIfEmailExists(final String email, String userId) {
        for (User currUser : users) {
            if (currUser.geteMail().equals(email) && !currUser.getId().equals(userId)) {
                return true;
            }
        }




        return false;
    }

    /**
     * @return object of fireBase user of the current user that is signed in.
     */


    public static FirebaseUser getFUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }


//

    private static void fillUserInfo(Iterable<DataSnapshot> children, User user, Notification type) {

        Notification notification;
        boolean isRequest=false;
        if (type instanceof Request)
        {
            notification=new Request();
            isRequest=true;
        }
        else {
            notification=new Rating();
        }
        for (DataSnapshot child : children) {
            notification = child.getValue(notification.getClass());
            if (notification != null) {
                if(isRequest){
                    Request request= (Request) notification;
                    requestMap.get(user.getId()).add(request);
                }else{
                    Rating request= (Rating) notification;
                    ratingMap.get(user.getId()).add(request);
                }
            }
        }
        setUserData(isRequest,user,new ArrayList());

    }
    private static void setUserData (boolean isRequest,User user,ArrayList notificationArrayList){
        if(isRequest)
        {
            user.setPending(requestMap.get(user.getId()));
        }else
        {
            user.setPersonsRated(ratingMap.get(user.getId()));
        }
    }



    /**
     * what this method does is
     * first get all users and save them in {@link #users} arrayList so we can access it anytime
     * second get all notifications related for each user and set them in a HashMap <String userID ,ArrayList<Request>> and then add for each user his arrayList
     * third in the same way we done the notifications we also do it with ratings
     * finally call getPhotosFromDatabase to get all users photos
     */

    public static void setUsers() {
        databaseRef.child("User").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                photos.clear();
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                long count = dataSnapshot.getChildrenCount();
                User user = new User();
                for (DataSnapshot child : children) {

                    if (child != null) {
                        user = child.getValue(User.class);
                        users.add(user);
                        ArrayList<Request> requestArrayList = new ArrayList<>();
                        ArrayList<Rating> ratingArrayList = new ArrayList<>();
                        ratingMap.put(user.getId(),ratingArrayList);
                        requestMap.put(user.getId(), requestArrayList);
                    }


                    Iterable<DataSnapshot> notificationKids = child.child("Notifications").getChildren();
                    fillUserInfo(notificationKids, user, new Request());

                    Iterable<DataSnapshot> ratingKids = child.child("my rates").getChildren();
                    fillUserInfo(ratingKids,user,new Rating());

                    if (requestMap.get(user.getId()) == null) {
                        System.out.println(TAG + " request map is null");
                    } else {

                        RequestHandler.setUsersRequest(requestMap);
                        RequestHandler.setUsersRates(ratingMap);
                    }
                }
                if (users.size() > 0) {
                    GetHelpActivity.setAllUsers(users);
                    getPhotosfromDatabase(count);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * this method fetches all users photos from database
     * @param children the number of users in database so we make sure that number of photos has a match with number of users
     */
    private static void getPhotosfromDatabase(final long children) {

        if (users != null) {
            for (final User user : users) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images").child(user.getId());
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        photos.put(user.getId(), uri);
                        if (photos.size() == children) {
                                GetHelpActivity.setUsersPhotos(photos);
                                System.out.println(TAG+" number of photos from database "+String.valueOf(photos.size()));
                                setReadyToGo(true);
                        }
                    }
                });

            }
        }
    }

    public static ArrayList<User> getUsers() {
        return users;
    }

    public static User getUser(String id) {
        if (users != null) {
            for (User user : users) {
                if (user.getId().equals(id)) {
                    return user;
                }
            }

        }
        return null;
    }

    public static HashMap<String, Uri> getPhotos() {
        if (photos.size() > 0) {
            return photos;
        }
        return null;

    }

    public static void updateUserPhoto(String id, Uri uri) {
        if (photos.containsKey(id)) {
            photos.put(id, uri);
        }
    }

    public static void setPhotos(HashMap<String, Uri> photos) {
        DATABASE.photos = photos;
    }


    public static boolean isReadyToGo() {
        return READY_TO_GO;
    }

    public static void setReadyToGo(boolean readyToGo) {
        READY_TO_GO = readyToGo;
    }
}
