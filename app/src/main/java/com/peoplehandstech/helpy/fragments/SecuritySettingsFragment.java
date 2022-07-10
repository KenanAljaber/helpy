package com.peoplehandstech.helpy.fragments;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.peoplehandstech.helpy.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class SecuritySettingsFragment extends Fragment implements View.OnClickListener {

    private LinearLayout changeMailL,changePasswordL,changePhoneL;
    private RelativeLayout backButtonR;

    public SecuritySettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_security_settings, container, false);

        changeMailL=view.findViewById(R.id.security_fragment_emailChange_LLayout);
        changePasswordL=view.findViewById(R.id.security_fragment_passwordChange_LLayout);
        changePhoneL=view.findViewById(R.id.security_fragment_phoneChange_LLayout);
        backButtonR=view.findViewById(R.id.security_fragment_backButton_RLayout);



        changeMailL.setOnClickListener(this);
        changePasswordL.setOnClickListener(this);
        changePhoneL.setOnClickListener(this);
        backButtonR.setOnClickListener(this);



        return view;
    }

    public void onClick (View view)
    {
        if(view.getId()==changeMailL.getId())
        {
            Fragment changeMailFragment=new MailChangeFragment();
            FragmentManager manager=getFragmentManager();
            FragmentTransaction transaction=manager.beginTransaction();
            transaction.setCustomAnimations(R.anim.enter_grom_left,R.anim.exit_from_right,R.anim.enter_from_right,R.anim.exit_from_left);
            transaction.replace(R.id.frameContainer,changeMailFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            return;
        }
        else if (view.getId()==changePasswordL.getId())
        {
            Fragment changeMailFragment=new PasswordChangeFragment();
            FragmentManager manager=getFragmentManager();
            FragmentTransaction transaction=manager.beginTransaction();
            transaction.setCustomAnimations(R.anim.enter_grom_left,R.anim.exit_from_right,R.anim.enter_from_right,R.anim.exit_from_left);
            transaction.replace(R.id.frameContainer,changeMailFragment);
            transaction.addToBackStack(null);
            transaction.commit();

            return;
        }
        else if (view.getId()==changePhoneL.getId())
        {
            Fragment changeMailFragment=new PhoneChangeFragment();
            FragmentManager manager=getFragmentManager();
            FragmentTransaction transaction=manager.beginTransaction();
            transaction.setCustomAnimations(R.anim.enter_grom_left,R.anim.exit_from_right,R.anim.enter_from_right,R.anim.exit_from_left);
            transaction.replace(R.id.frameContainer,changeMailFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            return;
        }
        else if (view.getId()==backButtonR.getId())
        {
            getFragmentManager().popBackStackImmediate();
        }

    }

}
