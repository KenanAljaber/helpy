package com.peoplehandstech.helpy.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.peoplehandstech.helpy.UserDataChangesCallback;
import com.peoplehandstech.helpy.utilites.DATABASE;
import com.peoplehandstech.helpy.R;
import com.peoplehandstech.helpy.utilites.UserHandler;
import com.peoplehandstech.helpy.models.User;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class MailChangeFragment extends Fragment implements View.OnClickListener, UserDataChangesCallback {

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

    private void changeEmail() {
        if (!infoEmpty()) {
            final FirebaseUser firebaseUser = DATABASE.getFUser();
            Objects.requireNonNull(getActivity()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            if (DATABASE.checkIfEmailExists(newMail.getText().toString(), currUSer.getId())) {

                errorMessage.setVisibility(View.VISIBLE);
                newMail.requestFocus();
                newMail.setError(getString(R.string.mail_already_exists));
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(newMail.getText().toString()).matches()) {
                errorMessage.setVisibility(View.VISIBLE);
                newMail.requestFocus();
                newMail.setError(getString(R.string.please_enter_a_valid_mail));
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                return;
            }
            errorMessage.setVisibility(View.GONE);


            AuthCredential credential = EmailAuthProvider.getCredential(oldMail.getText().toString(), password.getText().toString());

            FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        firebaseUser.updateEmail(newMail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                currUSer.seteMail(newMail.getText().toString());

//                                DATABASE.updateUser(currUSer.getId(),currUSer);
                                UserHandler.updateUserInfoByAttribute("eMail", newMail.getText().toString(), currUSer,MailChangeFragment.this);

                            }
                        });
                    } else {
                        errorMessage.setVisibility(View.VISIBLE);
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                }
            });


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


    @Override
    public void onChangesCompleted() {
        Toast.makeText(getContext(), getString(R.string.changes_has_been_saved), Toast.LENGTH_SHORT).show();
        oldMail.setText("");
        password.setText("");
        newMail.setText("");
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
