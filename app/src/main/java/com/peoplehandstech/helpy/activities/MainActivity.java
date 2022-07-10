package com.peoplehandstech.helpy.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.peoplehandstech.helpy.R;
import com.peoplehandstech.helpy.models.User;
import com.peoplehandstech.helpy.utilites.FastSignIn;


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // private Button goToMap;
    private MainActivity activity;
    private TextView signUp,forgetPassword;
    private Button signin;
    private EditText userName, password;
    private FirebaseAuth mAuth;
    public static ProgressBar pBar;
    public static Window window;
    public static boolean signInClicked ;
    private boolean   signUpClicked ;
    private ArrayList<User> usersTest;
    //PENDING_REQUESTS_DOWNLOADED is the state of requests downloads
    private static boolean ALL_USERS_DOWNLOADED;
    private Handler handler;
    private  User currentUser;
    public static Context MAIN_ACT_CTX;
    private static String TAG="MAIN_ACTIVITY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //   goToMap=findViewById(R.id.illHelp);
        signUp = findViewById(R.id.mainPage_signUp);
        signin = findViewById(R.id.signIn);
        userName = findViewById(R.id.userNameEditTxt);
        password = findViewById(R.id.passwordEditTxt);
        pBar = findViewById(R.id.progressbar);
        forgetPassword=findViewById(R.id.forgetPasswordTv);
        signInClicked = false;
        signUpClicked = false;
        usersTest = new ArrayList<>();
        handler = new Handler(getMainLooper());
        MAIN_ACT_CTX=this;
        window=getWindow();

        ALL_USERS_DOWNLOADED=false;
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
         pBar.setVisibility(View.VISIBLE);
            //block the screen from touching
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            FastSignIn.signIn(this,true);
        }



        mAuth = FirebaseAuth.getInstance();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this,PasswordRecoveryActivity.class));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            Log.d(TAG,"On resume method >> current user is not null");
        }else{
            Log.d(TAG,"On resume method >> current user is null");
        }
        if(pBar!=null){
            //pBar.setVisibility(View.GONE);
            userName.setText("");
            password.setText("");
        }
        signInClicked = false;
        signUpClicked = false;
    }

    public void signUp(View view) {
        if (!signUpClicked) {

            signUpClicked = true;
            Intent i = new Intent(MainActivity.this, SelectAGenderActivity.class);
            startActivity(i);
        }

    }

    public void signIn(View view) {


        //validations
        if (!signInClicked) {
            signInClicked = true;
            if (userName.getText().toString().isEmpty()) {
                userName.setError(getString(R.string.please_enter_a_valid_mail));
                userName.requestFocus();
                signInClicked = false;
                pBar.setVisibility(View.GONE);
                return;
            }
            if (password.getText().toString().isEmpty()) {
                password.setError(getString(R.string.please_enter_password));
                password.requestFocus();
                signInClicked = false;
                pBar.setVisibility(View.GONE);
                return;
            }
            pBar.setVisibility(View.VISIBLE);
            //block the screen from touching
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            final String emailAddress = noSpace(userName.getText().toString());


            mAuth.signInWithEmailAndPassword(emailAddress, password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG,"signIn method >> user has signed in correctly");
                        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
                            Log.d(TAG,"signIn method >> Auth user from main activity is not null");
                        }
                        FastSignIn.signIn(MainActivity.this,true);

                    } else {

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    signInClicked = false;
                    if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
                        userName.setError(getString(R.string.please_enter_a_valid_mail));
                        userName.requestFocus();
                        userName.setText("");
                        password.setText("");
                        pBar.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    } else {
                        pBar.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        Toast.makeText(MainActivity.this, getString(R.string.failed_to_login_please_check_mail_and_password), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    private String noSpace(String str) {
        String toReturn = "";
        for (char ch : str.toCharArray()) {
            if (!Character.isSpaceChar(ch)) {
                toReturn += ch;
            }
        }
        return toReturn;
    }
}



