package com.peoplehandstech.helpy.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.peoplehandstech.helpy.OnEmailCheckListener;
import com.peoplehandstech.helpy.R;

public class PasswordRecoveryActivity extends AppCompatActivity implements OnEmailCheckListener {

    private RelativeLayout search, cancel;
    private EditText emailEditText;
    private TextView weSEntYouEmail;
    private String TAG = "PASSWORD_RECOVERY";
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResend;
    private String sentCode;
    private String email;
    private boolean searching;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_recovery);

        search = findViewById(R.id.passwordRecovery_search_RL);
        cancel = findViewById(R.id.passwordRecovery_cancel_RL);
        emailEditText = findViewById(R.id.passwordRecovery_phoneNumber_ET);
        weSEntYouEmail=findViewById(R.id.weSentYouMail);
        mAuth=FirebaseAuth.getInstance();
        searching=false;
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PasswordRecoveryActivity.this.finish();
            }
        });


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(emailEditText.getText())) {
                    emailEditText.setError("please type your email address!");
                    emailEditText.requestFocus();
                    return;
                }
                email= emailEditText.getText().toString();

              if(Patterns.EMAIL_ADDRESS.matcher(email).matches() && !searching){
                  checkEmailExistsOrNot(email,PasswordRecoveryActivity.this);
                  Toast.makeText(getApplicationContext(),"Please wait...",Toast.LENGTH_LONG).show();
                  searching=true;
              }else{
                  Toast.makeText(getApplicationContext(),"Please make sure of the email address!",Toast.LENGTH_LONG).show();
                  searching=false;
              }


            }
        });
    }
    private void sendResetEmail (String email){
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        emailEditText.setText("");
                        weSEntYouEmail.setText("Please check your email inbox and junk files, we sent you the password reset link.");
                        weSEntYouEmail.setTextColor(getColor(R.color.mustard));
                        search.setEnabled(false);
                        Log.d(TAG,"Email has been sent");
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                        }
                    }
                });
    }

    void checkEmailExistsOrNot(String email, OnEmailCheckListener emailCheckListener){
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                Log.d(TAG,""+task.getResult().getSignInMethods().size());
                if (task.getResult().getSignInMethods().size() == 0){
                    // email not existed
                    emailCheckListener.onEmailRegistered(false);
                    Log.d(TAG,"checkEmailExistsOrNot method >>> mail does not exist");
                }else {
                    // email existed
                    emailCheckListener.onEmailRegistered(true);
                    Log.d(TAG,"checkEmailExistsOrNot method >>> mail does exists");
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onEmailRegistered(boolean isRegistered) {
        if(isRegistered){
            Log.d(TAG,"exits");
            sendResetEmail(email);
        }else{
            emailEditText.setText("");
           weSEntYouEmail.setText("We did not find any accounts,\nYour search did not return any results. Please try again with other information.");
           weSEntYouEmail.setTextColor(getColor(R.color.mainOrange));
           weSEntYouEmail.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            searching=false;
            Log.d(TAG,"does not exist");
        }
    }


}

