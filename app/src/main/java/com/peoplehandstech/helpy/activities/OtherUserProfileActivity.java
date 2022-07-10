package com.peoplehandstech.helpy.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.peoplehandstech.helpy.R;
import com.peoplehandstech.helpy.utilites.UserHandler;
import com.peoplehandstech.helpy.adapters.PagerViewAdapter;
import com.peoplehandstech.helpy.models.User;

import de.hdodenhof.circleimageview.CircleImageView;

public class OtherUserProfileActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {

    private TextView name,helpText,askForHelpLable,rateLable;
    private static TextView reputation,helped,askedForHelp;
    private CircleImageView circleImageView;
    private static User markerUser;
    private ViewPager viewPager;
    private PagerViewAdapter mPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);


        askForHelpLable=findViewById(R.id.other_user_profile_askForHelp_Tab);
        rateLable=findViewById(R.id.other_user_profile_rate_Tab);
        viewPager=findViewById(R.id.other_user_viewPager);
        name =findViewById(R.id.other_user_profile_nameText);
        helpText =findViewById(R.id.other_user_profile_helpText);
        circleImageView =findViewById(R.id.other_user_profile_profile_picture);
        reputation=findViewById(R.id.other_profile_reputation_text);
        helped=findViewById(R.id.other_profile_helped_text);
        askedForHelp=findViewById(R.id.other_profile_asked_for_help_text);



        markerUser =(User)getIntent().getSerializableExtra("marker user");
        mPagerAdapter=new PagerViewAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(mPagerAdapter);
        viewPager.addOnPageChangeListener(this);
        askForHelpLable.setOnClickListener(this);
        rateLable.setOnClickListener(this);
        changeTabs(0);


//        loadMainFragment();
        Glide.with(getApplicationContext()).load(GetHelpActivity.usersPhotos.get(markerUser.getId())).into(circleImageView);
        name.setText(markerUser.getName());
        helpText.setText(markerUser.getHowToHelp());

        setProfileInfo(markerUser.getReputation(),reputation);
        setProfileInfo(markerUser.getHelped(),helped);
        setProfileInfo(markerUser.getAskedForHelp(),askedForHelp);

    }

    private void setProfileInfo(int reputationInt,TextView textView) {
        if(reputationInt== 0)
        {
            textView.setText(String.valueOf(reputationInt));
            textView.setTextColor(getColor(R.color.almostBlack));
        }
        if(reputationInt<0)
        {
            textView.setText(String.valueOf(reputationInt));
            textView.setTextColor(getColor(R.color.red));
        }
        else{
            textView.setText(String.valueOf(reputationInt));
        }
    }

    public void onClick (View view)
    {
        if(view.getId()==askForHelpLable.getId())
        {
            viewPager.setCurrentItem(0);
        }

        if(view.getId()==rateLable.getId())
        {
            viewPager.setCurrentItem(1);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        changeTabs(position);
    }

    private void changeTabs(int position) {
        if (position==0)
        {
            askForHelpLable.setTextColor(getColor(R.color.mustard));
            askForHelpLable.setTextSize(20);

            rateLable.setTextColor(getColor(R.color.brown));
            rateLable.setTextSize(14);
        }


        if (position==1)
        {
            askForHelpLable.setTextColor(getColor(R.color.brown));
            askForHelpLable.setTextSize(14);

            rateLable.setTextColor(getColor(R.color.mustard));
            rateLable.setTextSize(20);
        }



    }
    public  static void updateUser ()
    {
        markerUser= UserHandler.getMarkerUser();
        if(markerUser!=null)
        {
            reputation.setText(String.valueOf(markerUser.getReputation()));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
