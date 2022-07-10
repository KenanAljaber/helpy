package com.peoplehandstech.helpy.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.peoplehandstech.helpy.R;

public class SelectAGenderActivity extends AppCompatActivity {

    private RelativeLayout maleUser,femaleUser, next,topBar;
    private  ImageView goBack;
    private  String userGender;
    private Animation animation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_agender);
        animation= AnimationUtils.loadAnimation(SelectAGenderActivity.this,R.anim.right_translate);
        maleUser=findViewById(R.id.select_gender_maleUser);
        femaleUser=findViewById(R.id.select_gender_femaleUser);
        next =findViewById(R.id.select_gender_next_button);
        goBack=findViewById(R.id.select_gender_goBack);
        topBar=findViewById(R.id.select_gender_topBar);

        maleUser.setAnimation(animation);
        femaleUser.setAnimation(AnimationUtils.loadAnimation(SelectAGenderActivity.this,R.anim.translate_from_left));
        next.setAnimation(AnimationUtils.loadAnimation(SelectAGenderActivity.this,R.anim.translate_from_bottom));


        topBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                topBar.startAnimation(AnimationUtils.loadAnimation(SelectAGenderActivity.this,R.anim.rotation));

            }
        });

    }
    public void selectGender (View view)
    {
//        String mustardColor="#FEA201";
//        String brownColor="#87643E";

        int viewId=view.getId();
        switch (viewId)
        {
            case R.id.select_gender_maleUser :
                 userGender="male";
                femaleUser.getBackground().setTint(getColor(R.color.brown));
                 maleUser.getBackground().setTint(getColor(R.color.mustard));
                 maleUser.startAnimation(AnimationUtils.loadAnimation(this,R.anim.scale_bigger));
                break;

            case R.id.select_gender_femaleUser:
                 userGender="female";
                maleUser.getBackground().setTint(getColor(R.color.brown));
                femaleUser.getBackground().setTint(getColor(R.color.mustard));
                femaleUser.startAnimation(AnimationUtils.loadAnimation(this,R.anim.scale_bigger));

                break;

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        femaleUser.getBackground().setTint(getColor(R.color.brown));
        maleUser.getBackground().setTint(getColor(R.color.brown));
        this.finish();
    }

    public void goToSignUp (View view)
    {
     next.startAnimation(AnimationUtils.loadAnimation(this,R.anim.scale_bigger));
     if(TextUtils.isEmpty(userGender))
     {
         Toast.makeText(SelectAGenderActivity.this,getString(R.string.please_choose_a_gender),Toast.LENGTH_SHORT).show();
     }
     else
     {
         Intent i=new Intent(SelectAGenderActivity.this, SignUpActivity.class);
         i.putExtra("Gender",userGender);
         i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
         startActivity(i);
     }


    }



    public void goBack (View view)
    {
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SelectAGenderActivity.this.finish();
            }
        },1000);
        goBack.startAnimation(AnimationUtils.loadAnimation(SelectAGenderActivity.this,R.anim.scale_bigger));
        femaleUser.getBackground().setTint(getColor(R.color.brown));
        maleUser.getBackground().setTint(getColor(R.color.brown));

    }
}
