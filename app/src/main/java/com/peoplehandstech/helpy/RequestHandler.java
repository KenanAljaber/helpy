package com.peoplehandstech.helpy;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestHandler {

    private static HashMap<String,ArrayList<Request>> usersRequest=new HashMap<>();
    private static HashMap<String,ArrayList<Rating>> usersRates=new HashMap<>();



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

    public static boolean checkUserToRate (User currUser,String otherUserID , Context context)
    {
        boolean requestAccepted=checkUserRequest(currUser,otherUserID,context);
        if(requestAccepted)
        {

            ArrayList<Rating> currUserRating=usersRates.get(currUser.getId());
            if(currUserRating!=null)
            {
                if(currUserRating.size()==0)
                {
                  //  Toast.makeText(context,"current user rating size is  0",Toast.LENGTH_SHORT).show();
                    return true;
                }
                for(Rating rating:currUserRating)
                {
                    if(rating.getId().equals(otherUserID))
                    {
                        return false;
                    }
                }
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
                for (Request currentNotifiacion: currUserRequests)
                {
                    if(currentNotifiacion.getRequestId().equals(otherUserID) &&
                            currentNotifiacion.getTitle().equals(context.getString(R.string.request_accepted)))
                    {
                        return true;
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
