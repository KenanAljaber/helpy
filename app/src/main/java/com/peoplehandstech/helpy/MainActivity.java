package com.peoplehandstech.helpy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
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

import java.util.ArrayList;

import static com.peoplehandstech.helpy.MyNotifications.CHANNEL_1_ID;

public class MainActivity extends AppCompatActivity {

    // private Button goToMap;
    private TextView signUp;
    private Button signin;
    private EditText userName, password;
    private FirebaseAuth mAuth;
    private ProgressBar pBar;
    private boolean signUpClicked, signInClicked;
    private ArrayList<User> usersTest;
    //PENDING_REQUESTS_DOWNLOADED is the state of requests downloads
    private static boolean ALL_USERS_DOWNLOADED;
    private Handler handler;
    private  User currentUser;


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
        signInClicked = false;
        signUpClicked = false;
        usersTest = new ArrayList<>();
        handler = new Handler(getMainLooper());


        ALL_USERS_DOWNLOADED=false;


        mAuth = FirebaseAuth.getInstance();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }



    }

    @Override
    protected void onResume() {
        super.onResume();
        signInClicked = false;
        signUpClicked = false;
    }

    public void signUp(View view) {
        if (!signUpClicked) {

            signUpClicked = true;
            Intent i = new Intent(MainActivity.this, SelectAGender.class);
            startActivity(i);
        }

    }

    public void signIn(View view) {



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
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            final String emailAddress = noSpace(userName.getText().toString());

            mAuth.signInWithEmailAndPassword(emailAddress, password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        DATABASE.setReadyToGo(false);
                        DATABASE.setUsers();

                        // a runnable instance that contains the signing in progress
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {


                                if(DATABASE.isReadyToGo() )
                                {
                                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    currentUser = DATABASE.getUser(id);
                                    currentUser.setPending(RequestHandler.getUsersRequest().get(currentUser.getId()));
                                    userName.setText("");
                                    password.setText("");
                                    pBar.setVisibility(View.GONE);
                                    Intent i = new Intent(MainActivity.this, GetHelpActivity.class);
                                    i.putExtra("current user", currentUser);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);
                                    finish();
                                }
                                else {

                                    // if not wait another second
                                    handler.postDelayed(this, 1000);
                                }
                            }
                        };
                        // wait for PENDING_REQUESTS_DOWNLOADED to be true and database to be ready
                        handler.postDelayed(runnable, 1000);

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



