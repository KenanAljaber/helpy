package com.peoplehandstech.helpy;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;


/**
 * A simple {@link Fragment} subclass.
 */
public class MailChangeFragment extends Fragment implements View.OnClickListener {

    private EditText oldMail,password,newMail;
    private RelativeLayout saveButtonR;
    private ImageView goBackImageView;
    private User currUSer;
    private TextView errorMessage;
    private boolean changed;

    public MailChangeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_mail_change, container, false);

        oldMail=v.findViewById(R.id.mail_fragment_oldMail_EditText);
        password=v.findViewById(R.id.mail_fragment_password_EditText);
        newMail=v.findViewById(R.id.mail_fragment_newMail_EditText);
        saveButtonR=v.findViewById(R.id.mail_fragment_fragment_save_button_RLayout);
        goBackImageView=v.findViewById(R.id.mail_fragment_goBack_ImageView);
        errorMessage=v.findViewById(R.id.mail_fragment_error_message);

        oldMail.setOnClickListener(this);
        password.setOnClickListener(this);
        newMail.setOnClickListener(this);
        saveButtonR.setOnClickListener(this);
        goBackImageView.setOnClickListener(this);

        if (getActivity().getIntent().getSerializableExtra("current User")!=null)
        {
            currUSer=(User)getActivity().getIntent().getSerializableExtra("current User");
        }


        return v;
    }

    public void onClick (View view )
    {
        if(view.getId()==saveButtonR.getId())
        {
            changeEmail();

        }
        if(view.getId()==goBackImageView.getId())
        {
            getFragmentManager().popBackStackImmediate();
        }
    }

    private void changeEmail()
    {
        if(!infoEmpty())
        {
            final FirebaseUser firebaseUser=DATABASE.getFUser();
            if(firebaseUser.getEmail().equals(oldMail.getText().toString()) && currUSer.getPassword().equals(password.getText().toString()) )
            {
                if(DATABASE.checkIfEmailExists(newMail.getText().toString(),currUSer.getId())  )
                {

                    errorMessage.setVisibility(View.VISIBLE);
                    newMail.requestFocus();
                    newMail.setError(getString(R.string.mail_already_exists));
                            return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(newMail.getText().toString()).matches())
                {
                    errorMessage.setVisibility(View.VISIBLE);
                    newMail.requestFocus();
                    newMail.setError(getString(R.string.please_enter_a_valid_mail));
                    return;
                }
                errorMessage.setVisibility(View.GONE);
                AuthCredential authCredential= EmailAuthProvider.getCredential(oldMail.getText().toString(),password.getText().toString());
                firebaseUser.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firebaseUser.updateEmail(newMail.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                currUSer.seteMail(newMail.getText().toString());

//                                DATABASE.updateUser(currUSer.getId(),currUSer);
                                UserHandler.updateUserInfo("eMail",newMail.getText().toString(),currUSer);
                                Toast.makeText(getContext(),getString(R.string.changes_has_been_saved),Toast.LENGTH_SHORT).show();
                                oldMail.setText("");
                                password.setText("");
                                newMail.setText("");

                            }
                        });

                    }
                });
            }
            else
            {
                errorMessage.setVisibility(View.VISIBLE);
            }
        }
    }
    private boolean infoEmpty ()
    {

        if(oldMail.getText().toString().isEmpty() || password.getText().toString().isEmpty() || newMail.getText().toString().isEmpty())
        {
            Toast.makeText(getContext(),getString(R.string.please_fill_all_info),Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;

        }



}
