package com.peoplehandstech.helpy.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.peoplehandstech.helpy.OnEmailCheckListener;
import com.peoplehandstech.helpy.utilites.DATABASE;
import com.peoplehandstech.helpy.R;
import com.peoplehandstech.helpy.models.User;

import java.util.concurrent.TimeUnit;

public class VerficatePhoneActivity extends AppCompatActivity implements OnEmailCheckListener {
    private String mVerficationId,phoneNumber,sentCode,userPhoto;
    private User user;
    private EditText phoneCodeEditText;
    private PhoneAuthProvider.ForceResendingToken mResend;
    private RelativeLayout activate;
    private TextView backToSignupText;
    private ProgressBar pBar;
    private FirebaseAuth mAuth;
    private DatabaseReference reff;
    public  static  boolean verficated=false;
    private boolean executed =false;
    private Uri uri;
    private StorageReference mStorageRef;
    private FirebaseUser firebaseUser;
    private User currentUser;
    private Runnable signUserIn;
    private Handler handler;
    private static String TAG="VERIFICATION_PHONE";
    //private boolean errorHappened=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verficate_phone);

        phoneCodeEditText=findViewById(R.id.verificationEditText);
        activate=findViewById(R.id.activate);
       backToSignupText=findViewById(R.id.backToSignupText);
        pBar=findViewById(R.id.progressBar);
        mStorageRef= FirebaseStorage.getInstance().getReference();

        mAuth=FirebaseAuth.getInstance();
        reff= FirebaseDatabase.getInstance().getReference();
        handler=new Handler();

        //getting the user that has been sent from the sign up Activity and the phone number
        if(getIntent().getSerializableExtra("userFromSignUp")!=null)
        {
            currentUser =(User)getIntent().getSerializableExtra("userFromSignUp");

           uri =getIntent().getParcelableExtra("uri");
            phoneNumber= currentUser.getPhoneNumber();

            checkEmailExistsOrNot(currentUser.geteMail(),this);

        }
        else
        {
            phoneNumber="000";
            Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show();
        }

//        //+593982293031
//        if(!executed)
//        {
//            //checkPhoneNumIfExists(currentUser);
//            executed =true;
//        }

        sendCode(phoneNumber);


    }




    void checkEmailExistsOrNot(String email,OnEmailCheckListener emailCheckListener){
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
            if(FirebaseAuth.getInstance().getCurrentUser()!=null){
                FirebaseAuth.getInstance().getCurrentUser().delete();
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this,"The email address is already used please check!",Toast.LENGTH_LONG).show();
                Log.d(TAG,"onEmailRegistered this email is already in use");
                VerficatePhoneActivity.this.finish();
            }else{
                Toast.makeText(this,"The email address is already used please check!",Toast.LENGTH_LONG).show();
                Log.d(TAG,"onEmailRegistered this email is already in use");
                VerficatePhoneActivity.this.finish();
            }
        }
    }

    // use this method to check whether user phone number is already exists or not
// p currUser= the current user who is trying to sign up

    private void checkPhoneNumIfExists (final User currentUser)
    {
        Log.i("checkPhoneNumIfExists method","processing");
         FirebaseDatabase fDb=FirebaseDatabase.getInstance();
            DatabaseReference dbRef=fDb.getReference();
            dbRef.child(currentUser.getCity()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                    for(DataSnapshot child:children)
                    {
                        if( child!=null)
                        {
                            User user= child.getValue(User.class);
                            if(user!=null && !user.getPhoneNumber().isEmpty())
                            {
                                if(currentUser.getPhoneNumber().equals(user.getPhoneNumber()) || currentUser.geteMail().equals(user.geteMail()))
                                {
//                                    Toast.makeText(VerficatePhoneActivity.this,"هذا المستخدم موجود مسبقاً",Toast.LENGTH_SHORT).show();
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    Intent i =new Intent(VerficatePhoneActivity.this, SignUpActivity.class);
                                    i.putExtra("userFromVerificate",currentUser);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);
                                    finish();



                                }
                            }

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        executed =true;

    }


    //use this method to control the text view backToSignupText
    public void backToSignUP (View view)
    {/*
            if(!errorHappened){
                Toast.makeText(getApplicationContext(),"Please wait for the previous code",Toast.LENGTH_SHORT).show();
            }else{
                sendCode(phoneNumber);
                errorHappened=false;
            }*/
    }

    // use this method the set the action when back button pressed
    @Override
    public void onBackPressed ()
    {
        Intent i =new Intent(VerficatePhoneActivity.this, SignUpActivity.class);
        i.putExtra("the user",user);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }


    // use this method to send verification code to the user phone number
    // p String number= the user phone number
    public void sendCode( String number)
    {

        if(!number.isEmpty()  )
        {
            Log.i("sendCode method","processing");
            Log.d(TAG,"sendCode method >>> processing");
            PhoneAuthOptions options= PhoneAuthOptions.newBuilder(mAuth)
                    .setPhoneNumber(number)
                    .setTimeout(100L,TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(mCallBacks)
                    .build();
            PhoneAuthProvider.verifyPhoneNumber(options);

        }
        else
        {
            //errorHappened=true;
            Toast.makeText(VerficatePhoneActivity.this,"please enter phone number...",Toast.LENGTH_SHORT).show();
        }



    }




    // use this method to get the code that user entered and compare it with the sent one from Fire base
    //use it to control the activate button
    // this method calls verifyCode method
    public void activate (View view)
    {
        String code= phoneCodeEditText.getText().toString().trim();
        if(code.isEmpty() || code.length()<6)
        {
            phoneCodeEditText.setError(getString(R.string.enter_code));
            phoneCodeEditText.requestFocus();
            return;
        }
        pBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        verifyCode(code);
    }


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            //Toast.makeText(VerficatePhoneActivity.this,"تم تفعيل الحساب ",Toast.LENGTH_SHORT).show();
            Log.d(TAG,"mCallBack onVerificationCompleted method >>> processing ");
            sentCode=phoneAuthCredential.getSmsCode();
            phoneCodeEditText.setTextColor(Color.GREEN);
            phoneCodeEditText.setText( sentCode);
//            phoneCodeEditText.setEnabled(false);

            if(sentCode!=null)
            {

                verifyCode(sentCode);
            }


        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(VerficatePhoneActivity.this,"Error happened ",Toast.LENGTH_SHORT).show();
            Toast.makeText(VerficatePhoneActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
            Log.i("Error",e.getMessage());
            Log.d(TAG,"mCallBack onVerificationFailed method >>> processing ");
            e.printStackTrace();
           // errorHappened=true;


        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token)
        {
            mVerficationId = verificationId;
            mResend =token;
            Log.d(TAG,"mCallBack onCodeSent method >>> processing ");
            Log.d(TAG,"mCallBack onCodeSent method >>> mVerficationId "+ mVerficationId);

        }

    };

    //use this method to make a credential object with the code user entered and the verification id we get from onCodeSent method
    // this method call signInWithCredential to sign the user in
    // p String code = the code user entered
    private void verifyCode (String code)
    {
        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(mVerficationId,code);
        signInWithCredential(credential);

    }

    // this method has been invoked in verifyCode to sign the user in
    private void signInWithCredential(PhoneAuthCredential credential)
    {
        Log.i("signinWithCredential method","processing");
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                    boolean isNew=authResult.getAdditionalUserInfo().isNewUser();
                   if(!isNew){
                        Toast.makeText(VerficatePhoneActivity.this,"User is already exists please sign in..",Toast.LENGTH_LONG).show();
                        Log.d(TAG,"User is already exist");
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        pBar.setVisibility(View.GONE);
                        Intent i=new Intent(VerficatePhoneActivity.this,MainActivity.class);
                        startActivity(i);
                        VerficatePhoneActivity.this.finish();
                    }else{
                    firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
                    //createUser();
                       test();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {


                    phoneCodeEditText.requestFocus();
                    phoneCodeEditText.setError(getString(R.string.make_sure_of_the_code));
                    pBar.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Log.d(TAG,"the verification error is "+e.getMessage());
                Toast.makeText(VerficatePhoneActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();

            }
        });

    }
    private void test(){

        Task<Void> updateEmail = firebaseUser.updateEmail(currentUser.geteMail());
        Log.d(TAG,"test method >>> processing");
        updateEmail.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> emailUpdated) {
                Log.d(TAG,"test method >>> emailUpdated onComplete");
                if(emailUpdated.isSuccessful()){
                    Task<Void> updatePassword= firebaseUser.updatePassword(currentUser.getPassword());
                    Log.d(TAG,"test method >>> emailUpdated emailUpdated.isSuccessful()");
                    updatePassword.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> passwordUpdated) {
                            if (passwordUpdated.isSuccessful()){

                                StorageReference filePath=mStorageRef.child("images").child(firebaseUser.getUid());
                                filePath.putFile(uri)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                // Get a URL to the uploaded content
                                                userPhoto = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                                                if(!userPhoto.isEmpty())
                                                {
                                                    user=new User(currentUser.getGender(),userPhoto, currentUser.getName(),phoneNumber, currentUser.geteMail()
                                                            , currentUser.getHowToHelp(), "***", currentUser.getLatitude(), currentUser.getLongitude());
                                                    user.setPhoto(userPhoto);
                                                    user.setCity(currentUser.getCity());
                                                    user.setVerified(true);
                                                    String uid=firebaseUser.getUid();
                                                    user.setId(uid);

                                                    setUserToken();
                                                    Log.i("fuser","not null");
                                                    //set user location in the database
                                                    reff.child("Users Locations")
                                                            .child(user.getId())
                                                            .setValue(user.getCity());



                                                    DATABASE.setReadyToGo(false);



                                                    signUserIn =new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            //when database is finish processing
                                                            if(DATABASE.isReadyToGo())
                                                            {
                                                                Toast.makeText(VerficatePhoneActivity.this,getString(R.string.registered_successfully),Toast.LENGTH_LONG).show();
                                                                Intent i=new Intent(VerficatePhoneActivity.this,GetHelpActivity.class);
                                                                i.putExtra("current user from phone verification",user);
                                                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                //we update this flag to true so we can shut down the sigUp activity
                                                                verficated=true;
                                                                startActivity(i);
                                                                VerficatePhoneActivity.this.finish();

                                                            }else{
                                                                handler.postDelayed(this,150);
                                                            }
                                                        }
                                                    };


                                                }
                                                else
                                                {
                                                    Toast.makeText(VerficatePhoneActivity.this,"empty",Toast.LENGTH_LONG).show();
                                                }

                                            }
                                        });





                            }else
                              {
                                  getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                  if(FirebaseAuth.getInstance().getCurrentUser()!=null){
                                      FirebaseAuth.getInstance().signOut();
                                  }
                                  pBar.setVisibility(View.GONE);
                                  Log.d(TAG,"test method >>> failed updatePassword!");
                                  Toast.makeText(VerficatePhoneActivity.this,"failed updatePassword!",Toast.LENGTH_SHORT).show();
                                  VerficatePhoneActivity.this.finish();
                              }
                        }
                    });
                }else
                    {

                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
                            FirebaseAuth.getInstance().signOut();
                        }
                        pBar.setVisibility(View.GONE);
                        firebaseUser.delete();
                        Log.d(TAG,"test method  >>>  failed updateEmail!");
                        Log.d(TAG," test method >>> Error happened");
                        Toast.makeText(VerficatePhoneActivity.this,"failed updateEmail!",Toast.LENGTH_SHORT).show();
                        VerficatePhoneActivity.this.finish();
                }

            }
        });




    }

    // use this method to link the email and password with the phone number , and to upload the user object to database
    private void createUser()
    {
        Log.i(TAG,"createUser method  processing");
        Log.d(TAG,"createUser method "+currentUser.geteMail()+firebaseUser.getUid());
        boolean success=FirebaseAuth.getInstance().getCurrentUser().updateEmail(currentUser.geteMail()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG,"createUser method updateEmail onSuccess  processing");
                firebaseUser.updatePassword(currentUser.getPassword())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                       // imageUploader();
                        Log.d(TAG,"Success signing up with email");
                        StorageReference filePath=mStorageRef.child("images").child(firebaseUser.getUid());
                        filePath.putFile(uri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        // Get a URL to the uploaded content
                                        userPhoto = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                                        if(!userPhoto.isEmpty())
                                        {
                                            user=new User(currentUser.getGender(),userPhoto, currentUser.getName(),phoneNumber, currentUser.geteMail()
                                                    , currentUser.getHowToHelp(), "***", currentUser.getLatitude(), currentUser.getLongitude());
                                            user.setPhoto(userPhoto);
                                            user.setCity(currentUser.getCity());
                                            user.setVerified(true);
                                            String uid=firebaseUser.getUid();
                                            user.setId(uid);

                                            setUserToken();
                                            Log.i("fuser","not null");
                                            //set user location in the database
                                            reff.child("Users Locations")
                                                    .child(user.getId())
                                                    .setValue(user.getCity());



                                            DATABASE.setReadyToGo(false);



                                            signUserIn =new Runnable() {
                                                @Override
                                                public void run() {
                                                    //when database is finish processing
                                                    if(DATABASE.isReadyToGo())
                                                    {
                                                        Toast.makeText(VerficatePhoneActivity.this,getString(R.string.registered_successfully),Toast.LENGTH_LONG).show();
                                                        Intent i=new Intent(VerficatePhoneActivity.this,GetHelpActivity.class);
                                                        i.putExtra("current user from phone verification",user);
                                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        //we update this flag to true so we can shut down the sigUp activity
                                                        verficated=true;
                                                        startActivity(i);
                                                        VerficatePhoneActivity.this.finish();

                                                    }else{
                                                        handler.postDelayed(this,150);
                                                    }
                                                }
                                            };


                                        }
                                        else
                                        {
                                            Toast.makeText(VerficatePhoneActivity.this,"empty",Toast.LENGTH_LONG).show();
                                        }

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle unsuccessful uploads
                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        FirebaseAuth.getInstance().signOut();

                                        // ...
                                    }
                                });



                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(VerficatePhoneActivity.this,"something went wrong please check your information",Toast.LENGTH_SHORT).show();
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        FirebaseAuth.getInstance().signOut();
                        VerficatePhoneActivity.this.finish();
                    }
                });
            }
        }).isSuccessful();
        Log.d(TAG,"error is "+success);
        if(!success){
            firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    if(FirebaseAuth.getInstance().getCurrentUser()!=null){
                        FirebaseAuth.getInstance().signOut();
                    }
                    pBar.setVisibility(View.GONE);
                    Toast.makeText(VerficatePhoneActivity.this,"failed sign up!",Toast.LENGTH_SHORT).show();
                    VerficatePhoneActivity.this.finish();
                }
            });


        }
        Log.d(TAG,""+success);

    }

    private void setUserToken() {
        // get the token of the device and save it
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {

                if(!task.isSuccessful()){
                    Log.d(TAG,"field in generating new token");

                }else{
                    String token=task.getResult();
                    Log.d(TAG,"This is the token "+token);
                    currentUser.setToken(token);
                    user.setToken(token);

                    //upload the user to the database
                    reff.child(user.getCity())
                            .child(user.getId())
                            .setValue(user);

                    //start downloading all users in the same user's country
                    DATABASE.setUsers(user.getCity());

                    //if database is ready sign user in
                    handler.postDelayed(signUserIn,150);



                }
            }
        });
    }


}
