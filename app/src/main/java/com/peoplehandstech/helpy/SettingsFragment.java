package com.peoplehandstech.helpy;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener {

    private TextView generalSettingsB,generalSettingsS,logOutText;
    private RelativeLayout generalSettingsR,logOutR;
    private LinearLayout generalSettingLL,securityLL,locationLL;
    private User currUser;



    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view= inflater.inflate(R.layout.settings_fragment, container, false);
        generalSettingsB=view.findViewById(R.id.settings_fragment_generalSettingsTextView);
        generalSettingsS=view.findViewById(R.id.settings_fragment_generalSettings_small_TextView);
        generalSettingsR=view.findViewById(R.id.settings_fragment_generalSettingsRLayout);
        generalSettingLL=view.findViewById(R.id.settings_fragment_GeneralSettingsLLayout);
        locationLL=view.findViewById(R.id.settings_fragment_LocationLLayout);
        securityLL=view.findViewById(R.id.settings_fragment_SecurityLLayout);
        logOutR=view.findViewById(R.id.settings_fragment_logOut_button_RLayout);
        logOutText=view.findViewById(R.id.settings_fragment_logOut_button_TextView);

        generalSettingLL.setOnClickListener(this);
        generalSettingsB.setOnClickListener(this);
        generalSettingsS.setOnClickListener(this);
        generalSettingsR.setOnClickListener(this);
        securityLL.setOnClickListener(this);
        logOutR.setOnClickListener(this);
        logOutText.setOnClickListener(this);
        locationLL.setOnClickListener(this);

        getCurrUser();

        return view;
    }
    private void getCurrUser ()
    {
        if(getActivity().getIntent()!=null)
        {
            Intent i= getActivity().getIntent();
            currUser =(User)i.getSerializableExtra("current User");
        }
    }
    private void logOut ()
    {
        FirebaseAuth.getInstance().signOut();
        Intent i=new Intent(getContext(),MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        GetHelpActivity.GET_HELP.finish();
        getActivity().finish();
    }

    private void generalSetting ()
    {
        Fragment fragment=new EditUserFragment();
        if(fragment.isAdded())
        {
            return;
        }
        else
        {
            FragmentManager fragmentManager=getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_grom_left,R.anim.exit_from_right,R.anim.enter_from_right,R.anim.exit_from_left);
            fragmentTransaction.replace(R.id.frameContainer, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

    }

    @Override
    public void onClick(View v) {
        if(v.getId()== generalSettingsB.getId() || v.getId()== generalSettingsS.getId() || v.getId()== generalSettingsR.getId() || v.getId()==generalSettingLL.getId())
        {
            generalSetting();

        }
        if(v.getId()==logOutR.getId() || v.getId()==logOutText.getId())
        {
            logOut();
        }
        if(v.getId()==securityLL.getId())
        {
            securitySettings();
        }
        if(v.getId()==locationLL.getId())
        {
            editLocation();
        }
    }

    private void securitySettings() {
        Fragment fragment=new SecuritySettingsFragment();
        FragmentManager fragmentManager=getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_grom_left,R.anim.exit_from_right,R.anim.enter_from_right,R.anim.exit_from_left);
        fragmentTransaction.replace(R.id.frameContainer, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    private void editLocation() {
        Fragment fragment=new MapFragment();
        FragmentManager fragmentManager=getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_grom_left,R.anim.exit_from_right,R.anim.enter_from_right,R.anim.exit_from_left);
        fragmentTransaction.replace(R.id.frameContainer, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
