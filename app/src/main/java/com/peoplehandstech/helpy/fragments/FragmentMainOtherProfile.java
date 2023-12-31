package com.peoplehandstech.helpy.fragments;


import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.peoplehandstech.helpy.notification.MyNotifications;
import com.peoplehandstech.helpy.models.NotificationFCM;
import com.peoplehandstech.helpy.R;
import com.peoplehandstech.helpy.utilites.RequestHandler;
import com.peoplehandstech.helpy.utilites.UserHandler;
import com.peoplehandstech.helpy.models.Request;
import com.peoplehandstech.helpy.models.User;
import com.peoplehandstech.helpy.notification.FirebaseNotificationHandler;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import static com.peoplehandstech.helpy.models.Request.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentMainOtherProfile extends Fragment implements View.OnClickListener {

    private RelativeLayout messageTextRL, sendRequestRL;
    private EditText messageTextEditTExt;
    private boolean clicked;
    private static User markerUser;
    private User currentUser;
    private ArrayList<Request> markerUserRequest;

    public FragmentMainOtherProfile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_test, container, false);

        messageTextRL = v.findViewById(R.id.fragment_other_main_sendMessageRL);
        messageTextEditTExt = v.findViewById(R.id.fragment_other_main_sendMessageEditTExt);
        sendRequestRL = v.findViewById(R.id.fragment_other_main_sendRequestButtonRL);
        messageTextRL.setOnClickListener(this);
        sendRequestRL.setOnClickListener(this);
        currentUser = UserHandler.getCurrentUser();
        markerUserRequest = new ArrayList<>();
        if (getActivity().getIntent().getSerializableExtra("marker user") != null) {
            markerUser = (User) getActivity().getIntent().getSerializableExtra("marker user");
            markerUserRequest = RequestHandler.getUsersRequest().get(markerUser.getId());
            markerUser.setPending(markerUserRequest);

        }


        return v;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == messageTextRL.getId()) {
            messageTextEditTExt.requestFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
        if (view.getId() == sendRequestRL.getId()) {
            if (!clicked) {
                clicked = true;
                if(currentUser.searchFriends(markerUser.getId())){
                    Toast.makeText(getContext(),markerUser.getName() + " is already your friend." , Toast.LENGTH_SHORT).show();
                    messageTextEditTExt.setText("");
                    clicked = false;
                    return;
                }
                if (markerUser.searchRequest(currentUser.getId())) {
                    Toast.makeText(getContext(), getString(R.string.you_have_already_sent_a_request_to) + " " + markerUser.getName(), Toast.LENGTH_SHORT).show();
                    messageTextEditTExt.setText("");
                    clicked = false;
                    return;
                }
                if (!messageTextEditTExt.getText().toString().isEmpty()) {
                    String message = messageTextEditTExt.getText().toString();
                    Request request = new Request(currentUser.getId(), message, currentUser.getName(), getString(R.string.new_request));
                    clicked = false;

                    markerUserRequest.add(request);
                    markerUser.setPending(markerUserRequest);
                    sendRequestRL.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.brown)));
                    //adding the request to the marker user in Firebase database
                    FirebaseDatabase.getInstance().getReference().child(markerUser.getCity()).child(markerUser.getId()).child(TAG)
                            .child(Request.NEW_REQUEST + currentUser.getId()).setValue(request);
                   // Toast.makeText(getContext(), getResources().getText(R.string.request_has_been_sent) + " " + markerUser.getName(), Toast.LENGTH_SHORT).show();
                    int askedForHelp = currentUser.getAskedForHelp() + 1;
                    currentUser.setAskedForHelp(askedForHelp);
                    UserHandler.setCurrentUser(currentUser);
                    UserHandler.updateUserInfoByAttribute("askedForHelp", askedForHelp, currentUser,()->{
                    messageTextEditTExt.setText("");
                    clicked = false;


                    NotificationFCM notificationFCM = new NotificationFCM(markerUser.getName(), message, currentUser.getName(), markerUser.getToken());
                    JSONObject notification = null;
                    try {
                        notification = FirebaseNotificationHandler.createJSONObject(notificationFCM,currentUser,markerUser,"needs your help!", MyNotifications.ASK_FOR_HELP);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    FirebaseNotificationHandler.sendNotification(notification,getContext());
                    });


                } else {
                    Toast.makeText(getContext(), getResources().getText(R.string.please_tell) + " " + markerUser.getName() + " " + getResources().getText(R.string.how_to_help), Toast.LENGTH_SHORT).show();
                    clicked = false;
                }
            }
        }
    }


}
