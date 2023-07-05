package com.peoplehandstech.helpy.utilites;

import android.content.Context;
import android.util.Log;

import com.peoplehandstech.helpy.R;
import com.peoplehandstech.helpy.models.Rating;
import com.peoplehandstech.helpy.models.Request;
import com.peoplehandstech.helpy.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class RequestHandler {

    private static HashMap<String,ArrayList<Request>> usersRequest=new HashMap<>();
    private static HashMap<String,ArrayList<Rating>> usersRates=new HashMap<>();
    static String TAG="RequestHandler";



    public static  int getRequestIndex (String id,ArrayList<Request> requests)
    {
        for(int i = 0; i< requests.size(); i++)
        {
            if(requests.get(i).getRequestId().equals(id))
            {
                return i;
            }

        }
        return -1;
    }

    public static HashMap<String, ArrayList<Request>> getUsersRequest() {
        return usersRequest;
    }

    public static void setUsersRequest(HashMap<String, ArrayList<Request>> usersRequest) {
        RequestHandler.usersRequest = usersRequest;
    }


    public static boolean didIRateThisUser (){
       User user=UserHandler.getMarkerUser();
       User currentUser=UserHandler.getCurrentUser();

       if(user==null ||user.getPeopleRated()==null || user.getPeopleRated().size()==0 ){
           return false;
       }else
       {
           for(Rating rating:user.getPeopleRated())
           {
               if(rating.getId().equals(currentUser.getId())){
                   return true;
               }
           }
       }
       return false;
    }

    public static boolean checkUserToRate (User currUser, String otherUserID , Context context)
    {
        boolean requestAccepted=checkUserRequest(currUser,otherUserID,context);
        Log.d(TAG,"checkUserToRate method >>> requestAccepted "+requestAccepted);
        if(requestAccepted)
        {

            ArrayList<Rating> currUserRating=usersRates.get(currUser.getId());
            if(currUserRating!=null)
            {
                if(currUserRating.size()==0)
                {
                    Log.d(TAG,"checkUserToRate method >>> currUserRating size is 0 ");
                  //  Toast.makeText(context,"current user rating size is  0",Toast.LENGTH_SHORT).show();
                    return true;
                }
               Optional<Rating> op=currUserRating.stream().filter(it -> it.getId().equals(otherUserID)).findFirst();
                if(op.isPresent()){
                    Log.d(TAG,"checkUserToRate method >>> ratingId is equal to otherUserID ");
                    return false;
                }
               /* for(Rating rating:currUserRating)
                {
                    if(rating.getId().equals(otherUserID))
                    {
                        Log.d(TAG,"checkUserToRate method >>> ratingId is equal to otherUserID ");
                        return false;
                    }
                }*/
                return true;
            }
            else
            {
               // Toast.makeText(context,"current user rating size is  null",Toast.LENGTH_SHORT).show();
            }
        }
        return false;

    }

    private static boolean checkUserRequest(User currUser, String otherUserID, Context context)
    {
        ArrayList<Request> currUserRequests =new ArrayList<>();
        currUserRequests =usersRequest.get(currUser.getId());
        if(currUserRequests !=null)
        {
            if(currUserRequests.size() == 0)
            {
                return false;
            }
            else{
                for (Request currentNotification: currUserRequests)
                {
                    if(currentNotification.getRequestId().equals(otherUserID))
                    {
                        Log.d(TAG,"checkUserRequest method >>> requestExist "+otherUserID);
                        Log.d(TAG,"checkUserRequest method >>> requestInfo "+currentNotification.toString());
                       if (currentNotification.getTitle().contains("Accepted"))
                        {
                            Log.d(TAG,"checkUserRequest method >>> requestTitleCorrect "+otherUserID);
                            return true;
                    }

                    }
                }
            }
        }
        else
        {
          //  Toast.makeText(context,"current user notification  is  null",Toast.LENGTH_SHORT).show();
        }
        return false;

    }

    public static HashMap<String, ArrayList<Rating>> getUsersRates() {
        return usersRates;
    }

    public static void setUsersRates(HashMap<String, ArrayList<Rating>> usersRates) {
        RequestHandler.usersRates = usersRates;
    }
}
