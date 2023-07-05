package com.peoplehandstech.helpy.notification;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.peoplehandstech.helpy.notification.MySingleton;
import com.peoplehandstech.helpy.models.NotificationFCM;
import com.peoplehandstech.helpy.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import static com.peoplehandstech.helpy.notification.ServerSide.FCM_API;
import static com.peoplehandstech.helpy.notification.ServerSide.contentType;
import static com.peoplehandstech.helpy.notification.ServerSide.serverKey;

public class FirebaseNotificationHandler {

    private static String TAG="FirebaseNotificationHandler";

    public static void sendNotification(JSONObject notification, final Context context) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        Log.i(TAG, "sendNotification method >>> onResponse: " + response.toString());

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Request error", Toast.LENGTH_LONG).show();
                        // Log.d("PORQUE",error.getMessage());
                        Log.i(TAG, "sendNotification method >>> onErrorResponse: Didn't work");
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }



   public static JSONObject createJSONObject(NotificationFCM notificationFCM, User currentUser,
                                             User targetUser,String additionalMessage,String type) throws FileNotFoundException {

        String TOPIC = "/tokens/"  + targetUser.getToken(); //topic must match with what the receiver subscribed to
        String NOTIFICATION_MESSAGE = notificationFCM.getMessage();

        JSONObject notification = new JSONObject();
        JSONObject notifcationBody = new JSONObject();
        try {
            notifcationBody.put("message", NOTIFICATION_MESSAGE);
            notifcationBody.put("senderName", notificationFCM.getFrom()+" "+additionalMessage);
            notifcationBody.put("userID",currentUser.getId());
            notifcationBody.put("type",type);

            // notifcationBody.put("receiverName", markerUser.getName());

            notification.put("to", TOPIC);
            notification.put("data", notifcationBody);
        } catch (JSONException e) {
            Log.e(TAG, "createJSONObject method >>> onCreate: " + e.getMessage());
        }

        return notification;
    }


}
