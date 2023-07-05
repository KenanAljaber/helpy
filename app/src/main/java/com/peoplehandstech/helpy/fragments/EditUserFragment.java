package com.peoplehandstech.helpy.fragments;


import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.peoplehandstech.helpy.CustomSpinner;
import com.peoplehandstech.helpy.R;
import com.peoplehandstech.helpy.UserDataChangesCallback;
import com.peoplehandstech.helpy.utilites.UserHandler;
import com.peoplehandstech.helpy.activities.UserProfileActivity;
import com.peoplehandstech.helpy.models.User;

import java.util.HashMap;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditUserFragment extends Fragment implements View.OnClickListener, UserDataChangesCallback {

    private Spinner spn;
    private View view;
    private ArrayAdapter<String> arrayAdapter;
    private User currUser;
    private RelativeLayout saveButtonRLayout,anotherHelpR;
    private EditText anotherHelpEditTExt,nameEditText;
    private String newName,newHelpMethod;
    private ImageView goToSettingsImage;
    private static boolean changed;

    public EditUserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         view= inflater.inflate(R.layout.fragment_edit_user_general_info, container, false);

         spn=view.findViewById(R.id.edit_user_fragment_spinner);
         anotherHelpR=view.findViewById(R.id.edit_user_fragment_anotherHelp_RLayout);
        saveButtonRLayout=view.findViewById(R.id.edit_user_fragment_save_button_RLayout);
        anotherHelpEditTExt=view.findViewById(R.id.edit_user_fragment_anotherHelp_EditTExt);
        nameEditText=view.findViewById(R.id.edit_user_fragment_name_EditTExt);
        goToSettingsImage=view.findViewById(R.id.edit_user_fragment_goToSettings_imageView);

        saveButtonRLayout.setOnClickListener(this);
        goToSettingsImage.setOnClickListener(this);

        if(Objects.requireNonNull(getActivity()).getIntent()!=null)
        {
            currUser=(User)getActivity().getIntent().getSerializableExtra("current User");
        }

         arrayAdapter=new ArrayAdapter<String>(Objects.requireNonNull(getContext()),android.R.layout.simple_spinner_item ){
             @NonNull
             @Override
             public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                 View view = super.getView(position, convertView, parent);
                 TextView tv =  view.findViewById(android.R.id.text1);
                 tv.setTextColor(getContext().getColor(R.color.mainOrange));
                 Typeface typeFace = ResourcesCompat.getFont(getContext(), R.font.dubai_light);
                 tv.setTypeface(typeFace);

                 tv.setTextSize(13);
                 return view;
             }


         };

         CustomSpinner.fillOptions(arrayAdapter,spn, Objects.requireNonNull(getContext()));
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        spn.setSelection(0);
//        CustomSpinner.setItemFirst(currUser.getHowToHelp(),spn,arrayAdapter,getContext());

        spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==spn.getCount()-1)
                {
                    anotherHelpR.setVisibility(View.VISIBLE);
                }
                else
                {
                    anotherHelpR.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

         return view;
    }



    public void onClick (View view)
    {
        if(view.getId()==saveButtonRLayout.getId())
        {
            save();
        }
        if(view.getId()==goToSettingsImage.getId())
        {
            goToSettings();
        }
    }

    private void goToSettings() {

        getFragmentManager().popBackStackImmediate();
    }


    public void save ()
    {

        if(nameEditText.getText().toString().isEmpty() )
        {
            newName=currUser.getName();

        }
        else
        {
            newName=nameEditText.getText().toString();
        }
        if(spn.getSelectedItemPosition()==0)
        {
            newHelpMethod=currUser.getHowToHelp();

        }
        if(spn.getSelectedItemPosition()==spn.getCount()-1 )
        {
            if(anotherHelpEditTExt.getText().toString().isEmpty())
            {
                anotherHelpEditTExt.requestFocus();
                anotherHelpEditTExt.setError(getString(R.string.tell_us_how_would_you_help));
                return;
            }
            else
            {
                newHelpMethod=anotherHelpEditTExt.getText().toString();
            }
        }
        if(spn.getSelectedItemPosition()!=spn.getCount()-1 && spn.getSelectedItemPosition()!=0)
        {
            newHelpMethod=spn.getSelectedItem().toString();
        }

        String id= FirebaseAuth.getInstance().getUid();
        if(id!=null)
        {
            //DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference().child("User");
            currUser.setHowToHelp(newHelpMethod);
            currUser.setName(newName);
            HashMap<String,Object>updates=new HashMap<>();
            updates.put("name",newName);
            updates.put("howToHelp",newHelpMethod);
            UserHandler.updateUserInfo(updates,currUser,this::onChangesCompleted);

        }
    }

    @Override
    public void onChangesCompleted() {
        Toast.makeText(getContext(),getString(R.string.changes_has_been_saved),Toast.LENGTH_SHORT).show();
        nameEditText.setText("");
        changed=true;
        UserHandler.setCurrentUser(currUser);
        UserProfileActivity.updateUser();
        goToSettings();
    }
}
