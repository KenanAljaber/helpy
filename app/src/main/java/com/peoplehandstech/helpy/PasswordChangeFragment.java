package com.peoplehandstech.helpy;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;


/**
 * A simple {@link Fragment} subclass.
 */
public class PasswordChangeFragment extends Fragment implements View.OnClickListener {

    private EditText oldPassword,newPassword;
    private ImageView goBack;
    private RelativeLayout saveBtn;
    private TextView errorMessage;
    private User currUser ;

    public PasswordChangeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_password_change, container, false);

        oldPassword=v.findViewById(R.id.password_fragment_oldPassword_EditText);
        newPassword=v.findViewById(R.id.password_fragment_newPassword_EditText);
        goBack=v.findViewById(R.id.password_fragment_goBack_ImageView);
        saveBtn=v.findViewById(R.id.password_fragment_fragment_save_button_RLayout);
        errorMessage=v.findViewById(R.id.password_fragment_error_message);


        oldPassword.setOnClickListener(this);
        newPassword.setOnClickListener(this);
        goBack.setOnClickListener(this);
        saveBtn.setOnClickListener(this);

        if(getActivity().getIntent().getSerializableExtra("current User")!=null)
        {
            currUser=(User)getActivity().getIntent().getSerializableExtra("current User");
        }

        return v;
    }
    public void onClick (View view)
    {
        if(view.getId()==saveBtn.getId())
        {
            if(!infoEmpty())
            {
                if(currUser!=null)
                {
                    FirebaseUser fUser=DATABASE.getFUser();
                    if(oldPassword.getText().toString().equals(currUser.getPassword()))
                    {
                        fUser.updatePassword(newPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(),getString(R.string.changes_has_been_saved),Toast.LENGTH_SHORT).show();
                                currUser.setPassword(newPassword.getText().toString());
                                UserHandler.updateUserInfo("password",newPassword,currUser);
                                oldPassword.setText("");
                                newPassword.setText("");
                                errorMessage.setVisibility(View.GONE);
                                getFragmentManager().popBackStackImmediate();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(),getString(R.string.something_wrong_happened),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else
                    {
                        oldPassword.requestFocus();
                        errorMessage.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
        if(view.getId()==goBack.getId())
        {
            getFragmentManager().popBackStackImmediate();
        }
    }
    private boolean infoEmpty ()
    {
        boolean isEmpty;

        if(newPassword.getText().toString().isEmpty() || oldPassword.getText().toString().isEmpty() )
        {
            Toast.makeText(getContext(),getString(R.string.password_should_contain_6_characters),Toast.LENGTH_SHORT).show();
            isEmpty=true;
        }
        else if ( newPassword.getText().toString().length()<6|| oldPassword.getText().toString().length()<6)
        {
            Toast.makeText(getContext(),getString(R.string.password_should_contain_6_characters),Toast.LENGTH_SHORT).show();
            isEmpty=true;
        }
        else
        {
            isEmpty=false;
        }
        return isEmpty;
    }

}
