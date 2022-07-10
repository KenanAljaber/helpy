package com.peoplehandstech.helpy.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.peoplehandstech.helpy.utilites.DATABASE;
import com.peoplehandstech.helpy.R;
import com.peoplehandstech.helpy.models.User;


/**
 * A simple {@link Fragment} subclass.
 */
public class PasswordChangeFragment extends Fragment implements View.OnClickListener {

    private EditText oldPassword,newPassword;
    private ImageView goBack;
    private RelativeLayout saveBtn;
    private TextView errorMessage;
    private User currUser ;
    private ProgressBar changingPassword;

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
        changingPassword=v.findViewById(R.id.changingPasswordPB);
        errorMessage.setVisibility(View.GONE);

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

     // i need to change this method so user opassowrd will not be shown to anyone
    public void onClick (View view)
    {
        if(view.getId()==saveBtn.getId())
        {
            if(!infoEmpty())
            {
                if(currUser!=null)
                {
                    changingPassword.setVisibility(View.VISIBLE);

                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    FirebaseUser fUser= DATABASE.getFUser();

                    AuthCredential credential= EmailAuthProvider.getCredential(currUser.geteMail(),oldPassword.getText().toString());



                    FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                FirebaseAuth.getInstance().getCurrentUser().updatePassword(newPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        Toast.makeText(getActivity(),"Password changed",Toast.LENGTH_LONG).show();
                                        oldPassword.setText("");
                                        newPassword.setText("");
                                        changingPassword.setVisibility(View.GONE);
                                    }

                                });
                            }else{
                                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                oldPassword.requestFocus();
                                oldPassword.setError("please make sure of current password");
                                oldPassword.setText("");
                                newPassword.setText("");
                                changingPassword.setVisibility(View.GONE);
                            }
                        }
                    });
//                    if(oldPassword.getText().toString().equals(currUser.getPassword()))
//                    {
//
//                        fUser.updatePassword(newPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                Toast.makeText(getContext(),getString(R.string.changes_has_been_saved),Toast.LENGTH_SHORT).show();
//                                currUser.setPassword(newPassword.getText().toString());
//                                UserHandler.updateUserInfo("password",newPassword,currUser);
//
//                                errorMessage.setVisibility(View.GONE);
//                                getFragmentManager().popBackStackImmediate();
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Toast.makeText(getContext(),getString(R.string.something_wrong_happened),Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//                    else
//                    {
//                        oldPassword.requestFocus();
//                        errorMessage.setVisibility(View.VISIBLE);
//                    }
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
