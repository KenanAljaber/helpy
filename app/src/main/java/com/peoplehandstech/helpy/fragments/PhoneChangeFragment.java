package com.peoplehandstech.helpy.fragments;


import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.peoplehandstech.helpy.utilites.DATABASE;
import com.peoplehandstech.helpy.R;
import com.peoplehandstech.helpy.utilites.UserHandler;
import com.peoplehandstech.helpy.activities.UserProfileActivity;
import com.peoplehandstech.helpy.models.User;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 */
public class PhoneChangeFragment extends Fragment implements View.OnClickListener {
    private EditText phoneEdtTxt, passwordEdtTxt,codeEdtTxt;
    private RelativeLayout sendCodeButtonRL,phoneRL,passwordRL,codeRL,verifyButtonRL;
    private ImageView goBackImageView;
    private User currUser;
    private String sentCode,mVerificationId,newPhoneNumber;
    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;
    private boolean dataChecked=false;
    private boolean passwordCorrect=false;



    public PhoneChangeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_phone_change, container, false);

        phoneEdtTxt = v.findViewById(R.id.phone_fragment_new_phone_number_EditText);
        passwordEdtTxt = v.findViewById(R.id.phone_fragment_password_editText);
        sendCodeButtonRL = v.findViewById(R.id.phone_fragment_sendCode_button_RLayout);
        goBackImageView = v.findViewById(R.id.phone_fragment_goBack_imageView);
        phoneRL = v.findViewById(R.id.phone_fragment_new_phone_number_RLayout);
        passwordRL = v.findViewById(R.id.phone_fragment_password_RL);
        codeEdtTxt = v.findViewById(R.id.phone_fragment_code_EditText);
        codeRL = v.findViewById(R.id.phone_fragment_code_RL);
        verifyButtonRL = v.findViewById(R.id.phone_fragment_verify_button_RLayout);
        progressBar = v.findViewById(R.id.phone_fragment_progressBar);




        phoneEdtTxt.setOnClickListener(this);
        passwordEdtTxt.setOnClickListener(this);
        sendCodeButtonRL.setOnClickListener(this);
        goBackImageView.setOnClickListener(this);
        phoneRL.setOnClickListener(this);
        passwordRL.setOnClickListener(this);
        codeEdtTxt.setOnClickListener(this);
        codeRL.setOnClickListener(this);
        verifyButtonRL.setOnClickListener(this);


         firebaseUser= DATABASE.getFUser();



        if (getActivity().getIntent().getSerializableExtra("current User") != null) {
            currUser = (User) getActivity().getIntent().getSerializableExtra("current User");
        }
        return v;
    }

    public void onClick(View view) {
        if (view.getId() == goBackImageView.getId()) {
            getFragmentManager().popBackStackImmediate();
        }
        if(view.getId()== sendCodeButtonRL.getId())
        {
            Objects.requireNonNull(getActivity()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            boolean checked =checkData();
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            if(checked)
            {

                newPhoneNumber=phoneEdtTxt.getText().toString();
                sendCode(newPhoneNumber);


                sendCodeButtonRL.setVisibility(View.GONE);
                passwordRL.setVisibility(View.GONE);
                phoneRL.setVisibility(View.GONE);
                codeRL.setVisibility(View.VISIBLE);
                verifyButtonRL.setVisibility(View.VISIBLE);

            }
        }
        if(view.getId()==verifyButtonRL.getId())
        {
            progressBar.setVisibility(View.VISIBLE);
            String code= codeEdtTxt.getText().toString().trim();
            if(code.isEmpty() || code.length()<6)
            {
                codeEdtTxt.setError("ادخل الكود ...");
                codeEdtTxt.requestFocus();
                progressBar.setVisibility(View.GONE);
                return;
            }

            verifyCode(code);
        }

    }






    private void sendCode(String number) {
        Toast.makeText(getContext(),getString(R.string.codeHasBeenSent),Toast.LENGTH_SHORT).show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(number,60, TimeUnit.SECONDS,getActivity(),mCallBacks);
    }




    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            sentCode=phoneAuthCredential.getSmsCode();
            codeEdtTxt.setTextColor(Color.GREEN);
            codeEdtTxt.setText( sentCode);
            codeEdtTxt.setEnabled(false);
            if(sentCode!=null)
            {

                verifyCode(sentCode);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

        }
        @Override
        public void onCodeSent (@NonNull String verificationId,
                                @NonNull PhoneAuthProvider.ForceResendingToken token)
        {
            mVerificationId=verificationId;
        }
    };


    private void verifyCode(String sentCode)
    {
        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(mVerificationId,sentCode);
        firebaseUser.updatePhoneNumber(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(),getString(R.string.changes_has_been_saved),Toast.LENGTH_SHORT).show();
                currUser.setPhoneNumber(newPhoneNumber);
//                DATABASE.updateUser(currUser.getId(),currUser);
                UserHandler.updateUserInfoByAttribute("phoneNumber",newPhoneNumber,currUser,()->{
                    UserHandler.setCurrentUser(currUser);
                    UserProfileActivity.updateUser();
                    getFragmentManager().popBackStackImmediate();
                    progressBar.setVisibility(View.GONE);
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),"failed",Toast.LENGTH_SHORT).show();
            }
        });
    }



    private boolean checkData() {


        final Handler handler=new Handler();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                if(!dataChecked){
                    handler.postDelayed(this,1000);
                }
            }
        };

        AuthCredential credential= EmailAuthProvider.getCredential(currUser.geteMail(),passwordEdtTxt.getText().toString());
        FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    dataChecked=true;
                    passwordCorrect=true;
                }else{
                    dataChecked=true;
                    passwordCorrect=false;
                    passwordEdtTxt.setError("Wrong password!");
                    passwordEdtTxt.requestFocus();
                }
            }
        });

//        if (!passwordEdtTxt.getText().toString().equals(currUser.getPassword()))
//        {
//            passwordEdtTxt.setError(getString(R.string.invalid_password));
//            passwordEdtTxt.setText("");
//            passwordEdtTxt.requestFocus();
//            return false;
//        }
        if(!Patterns.PHONE.matcher(phoneEdtTxt.getText().toString()).matches())
        {
            phoneEdtTxt.setError(getString(R.string.invalid_phone_number));
            phoneEdtTxt.setText("");
            phoneEdtTxt.requestFocus();
            return false;
        }
        if(phoneEdtTxt.getText().toString().equals(currUser.getPhoneNumber()))
        {
            phoneEdtTxt.setError(getString(R.string.this_is_your_current_number));
            phoneEdtTxt.setText("");
            phoneEdtTxt.requestFocus();
            return false;
        }
        if(passwordEdtTxt.getText().toString().isEmpty() || phoneEdtTxt.getText().toString().isEmpty())
        {
            Toast.makeText(getContext(),getString(R.string.please_fill_all_info),Toast.LENGTH_SHORT).show();
            return false;
        }
        handler.postDelayed(runnable,1000);


       if(passwordCorrect){
           return true;
       }else
           return false;

    }
}
