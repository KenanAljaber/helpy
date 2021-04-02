package com.peoplehandstech.helpy;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.concurrent.TimeUnit;

public class VerficatePhone extends AppCompatActivity {
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
        reff= FirebaseDatabase.getInstance().getReference().child("User");


        //getting the user that has been sent from the sign up Activity and the phone number
        if(getIntent().getSerializableExtra("userFromSignUp")!=null)
        {
            currentUser =(User)getIntent().getSerializableExtra("userFromSignUp");

           uri =getIntent().getParcelableExtra("uri");
            phoneNumber= currentUser.getPhoneNumber();

        }
        else
        {
            phoneNumber="000";
            Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show();
        }

        //+593982293031
        if(!executed)
        {
            checkPhoneNumIfExists(currentUser);
            executed =true;
        }

        sendCode(phoneNumber);

        FirebaseAuth auth=FirebaseAuth.getInstance();

    }


    private void imageUploader ()
    {
        StorageReference filePath=mStorageRef.child("images").child(firebaseUser.getUid());
        filePath.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        userPhoto = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                        if(!userPhoto.isEmpty())
                        {
                            user.setPhoto(userPhoto);
                        }
                        else
                        {
                            Toast.makeText(VerficatePhone.this,"empty",Toast.LENGTH_LONG).show();
                        }




                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                    }
                });
    }




// use this method to check whether user phone number is already exists or not
// p currUser= the current user who is trying to sign up

    private void checkPhoneNumIfExists (final User currentUser)
    {
        Log.i("checkPhoneNumIfExists method","processing");
         FirebaseDatabase fDb=FirebaseDatabase.getInstance();
            DatabaseReference dbRef=fDb.getReference();
            dbRef.child("User").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                    for(DataSnapshot child:children)
                    {
                        if(currentUser!=null && child!=null)
                        {
                            User user= child.getValue(User.class);
                            if(user!=null && !user.getPhoneNumber().isEmpty())
                            {
                                if(currentUser.getPhoneNumber().equals(user.getPhoneNumber()) || currentUser.geteMail().equals(user.geteMail()))
                                {
//                                    Toast.makeText(VerficatePhone.this,"هذا المستخدم موجود مسبقاً",Toast.LENGTH_SHORT).show();
                                    Intent i =new Intent(VerficatePhone.this,SignUp.class);
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
    {
        Intent i =new Intent(VerficatePhone.this,SignUp.class);
        i.putExtra("the user",user);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    // use this method the set the action when back button pressed
    @Override
    public void onBackPressed ()
    {
        Intent i =new Intent(VerficatePhone.this,SignUp.class);
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
            PhoneAuthProvider.getInstance().verifyPhoneNumber(number,
                    60,
                    TimeUnit.SECONDS,
                    VerficatePhone.this,
                    mCallBacks);

        }
        else
        {
            Toast.makeText(VerficatePhone.this,"الرجاء ادخال رقم هاتف صحيح ",Toast.LENGTH_SHORT).show();
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
            //Toast.makeText(VerficatePhone.this,"تم تفعيل الحساب ",Toast.LENGTH_SHORT).show();
            sentCode=phoneAuthCredential.getSmsCode();
            phoneCodeEditText.setTextColor(Color.GREEN);
            phoneCodeEditText.setText( sentCode);
            phoneCodeEditText.setEnabled(false);

            if(sentCode!=null)
            {

                verifyCode(sentCode);
            }


        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(VerficatePhone.this,"Error happened ",Toast.LENGTH_SHORT).show();
            Toast.makeText(VerficatePhone.this,e.getMessage(),Toast.LENGTH_LONG).show();
            Log.i("Error",e.getMessage());
            e.printStackTrace();


        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token)
        {
            mVerficationId = verificationId;
            mResend =token;


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
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){

                    phoneCodeEditText.requestFocus();
                    phoneCodeEditText.setError(getString(R.string.make_sure_of_the_code));
                    pBar.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
                if(task.isSuccessful())
                {
                    boolean isNew=task.getResult().getAdditionalUserInfo().isNewUser();
                    if(isNew)
                    {
                        firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
                        createUser();
                    }else{
                        Toast.makeText(VerficatePhone.this,getString(R.string.this_number_already_exits),Toast.LENGTH_SHORT).show();
                        pBar.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        Intent i =new Intent(VerficatePhone.this,SignUp.class);
                        i.putExtra("userFromVerificate", currentUser);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(VerficatePhone.this,"something went wrong",Toast.LENGTH_SHORT).show();
            }
        });
    }


    // use this method to link the email and password with the phone number , and to upload the user object to database
    private void createUser()
    {
        Log.i("createUser method","processing");

        firebaseUser.updateEmail(currentUser.geteMail()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                firebaseUser.updatePassword(currentUser.getPassword()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                       // imageUploader();
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
                                                    , currentUser.getHowToHelp(), currentUser.getPassword(), currentUser.getLatitude(), currentUser.getLongitude());
                                            user.setPhoto(userPhoto);
                                            user.setVerificated(true);
                                            String uid=firebaseUser.getUid();
                                            user.setId(uid);
                                            Log.i("fuser","not null");
                                            reff.child(uid).setValue(user);
                                            DATABASE.setReadyToGo(false);
                                            DATABASE.setUsers();
                                            final Handler handler=new Handler();
                                            Runnable runnable=new Runnable() {
                                                @Override
                                                public void run() {
                                                    if(DATABASE.isReadyToGo())
                                                    {
                                                        Toast.makeText(VerficatePhone.this,getString(R.string.registered_successfully),Toast.LENGTH_LONG).show();
                                                        Intent i=new Intent(VerficatePhone.this,GetHelpActivity.class);
                                                        i.putExtra("current user",user);
                                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        //we update this flag to true so we can shut down the sigUp activity
                                                        verficated=true;
                                                        startActivity(i);
                                                        VerficatePhone.this.finish();
                                                    }else{
                                                        handler.postDelayed(this,150);
                                                    }
                                                }
                                            };
                                            handler.postDelayed(runnable,150);


                                        }
                                        else
                                        {
                                            Toast.makeText(VerficatePhone.this,"empty",Toast.LENGTH_LONG).show();
                                        }




                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle unsuccessful uploads
                                        // ...
                                    }
                                });



                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(VerficatePhone.this,"something went wrong please check your information",Toast.LENGTH_SHORT).show();
                        VerficatePhone.this.finish();
                    }
                });
            }
        });

    }






}
