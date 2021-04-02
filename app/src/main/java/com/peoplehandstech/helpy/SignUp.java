package com.peoplehandstech.helpy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUp extends AppCompatActivity {

    private static final int PICK_IMAGE_CODE = 111;
    private static Activity signup;
    private String gender;
    private Uri uri;
    private ImageView cameraImageView;
    private CircleImageView circleImageView;
    private EditText name, phoneNumber, mail, password, anotherHelpText;
    private TextView locationNote;
    private Spinner spn;
    private RelativeLayout locationButton, nextButton, anotherHelpRLayout;

    private ArrayList<String> options;
    private ArrayAdapter<String> arrayAdapter;

    private LocationManager lManager;
    private boolean gpsFlag;
    private Location userLoc;

    private static final int LOCATION_CODE = 134;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        signup = this;
        if (getIntent().getStringExtra("Gender") != null) {
            gender = getIntent().getStringExtra("Gender");
        }
        findViews();
        options = new ArrayList<String>();

        lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationButton.setEnabled(true);
        locationNote.setVisibility(View.INVISIBLE);

        //show a message alert in case user dose not has gps on
        if (!lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (!buildAlertMessageNoGps(SignUp.this)) {

                locationButton.setEnabled(false);
                locationNote.setVisibility(View.VISIBLE);
            }
        }


        //we set the options of the selected item of the spinner
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                tv.setTextColor(getColor(R.color.mainOrange));
                Typeface typeFace = ResourcesCompat.getFont(SignUp.this, R.font.dubai_light);
                tv.setTypeface(typeFace);

                tv.setTextSize(13);
                return view;

            }
        };
        //here we add the options we want to the spinner by adding all strings to an arrayList and then loop through it and add it one by one
        fillOptions(arrayAdapter);
        //we set a custom layout.xml to control color size font of all items in spinner dropDown list
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);


        // setting the adapter to the spinner and getting the chosen one
        spn.setAdapter(arrayAdapter);

        // handle the event of item selected
        spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view;
                if (getText(R.string.another_help).equals(tv.getText().toString())) {
                    anotherHelpRLayout.setVisibility(View.VISIBLE);
                    anotherHelpText.requestFocus();


                } else {
                    anotherHelpRLayout.setVisibility(View.GONE);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void backToSelectGender (View view)
    {
        Intent i=new Intent(SignUp.this,SelectAGender.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(VerficatePhone.verficated)
        {
            SignUp.this.finish();
        }
        if (lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationButton.setEnabled(true);
            locationNote.setVisibility(View.INVISIBLE);
        }
    }


    public void next(View view) {
        if (checkAllView()) {

            if (!Patterns.EMAIL_ADDRESS.matcher(mail.getText().toString()).matches()) {
                mail.setError(getString(R.string.please_enter_a_valid_mail));
                mail.requestFocus();
                return;
            }
            if (password.getText().toString().length() < 6) {
                password.setError(getString(R.string.password_should_contain_6_characters));
                password.requestFocus();
                return;
            }

            if (userLoc == null) {

                locationNote.setTextColor(Color.RED);
                Toast.makeText(this, getString(R.string.please_set_location), Toast.LENGTH_SHORT).show();
                return;
            }
            if(uri==null)
            {
                circleImageView.requestFocus();
                Toast.makeText(this, getString(R.string.please_select_aPhoto), Toast.LENGTH_SHORT).show();
                return;
            }
            String helpWay;
            if (anotherHelpRLayout.getVisibility() == View.VISIBLE && TextUtils.isEmpty(anotherHelpText.getText())) {
                anotherHelpText.requestFocus();
                anotherHelpText.setError(getString(R.string.tell_us_how_would_you_help));
                return;
            }
            if (anotherHelpRLayout.getVisibility() == View.VISIBLE && !TextUtils.isEmpty(anotherHelpText.getText())) {
                helpWay = anotherHelpText.getText().toString();
            } else {
                helpWay = spn.getSelectedItem().toString();
            }
            User user = new User(gender, "", name.getText().toString(), phoneNumber.getText().toString(), mail.getText().toString(),
                    helpWay, password.getText().toString(), userLoc.getLatitude(), userLoc.getLongitude());

            Intent i = new Intent(SignUp.this, VerficatePhone.class);
            i.putExtra("userFromSignUp",user);
            i.putExtra("uri",uri);
            startActivity(i);

        }
    }


    private boolean checkAllView() {
        ArrayList<EditText> views = new ArrayList<>();
        views.add(name);
        views.add(phoneNumber);
        views.add(mail);
        views.add(password);

        if (spn.getSelectedItem().toString().equals(getString(R.string.select_a_way_toHelp))) {
            spn.requestFocus();
            Toast.makeText(SignUp.this, getString(R.string.please_select_help), Toast.LENGTH_SHORT).show();
            return false;
        }
        for (View view : views) {
            EditText eText = (EditText) view;
            String text = eText.getText().toString();
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(this, getString(R.string.please_fill_all_info), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }


    public void selectPhoto(View view) {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), PICK_IMAGE_CODE);
    }

    public void setLocation(View view) {
        Intent intent = new Intent(SignUp.this, SetLocationActivity.class);
        startActivityForResult(intent, LOCATION_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == LOCATION_CODE && resultCode == RESULT_OK && data != null) {
                userLoc = (Location) data.getSerializableExtra("user location");
            }

            if (requestCode == PICK_IMAGE_CODE && resultCode == RESULT_OK && data != null) {
                try {
                    uri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(uri);
                    //  Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    if (selectedImage != null) {
                        circleImageView.setImageBitmap(selectedImage);
                        cameraImageView.setVisibility(View.INVISIBLE);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i("the image is in catch", "pilas");
                }
            }
        }
    }
        public boolean buildAlertMessageNoGps (Context context){

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(getString(R.string.your_gps_is_not_working))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            gpsFlag = true;
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            gpsFlag = false;
                            dialog.cancel();


                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
            return gpsFlag;
        }

        private void fillOptions (ArrayAdapter < String > spnAdapter)
        {
            options.add(getResources().getString(R.string.select_a_way_toHelp));
            options.add(getResources().getString(R.string.couple_days_ofHosting));
            options.add(getResources().getString(R.string.translating));
            options.add(getResources().getString(R.string.official_docs_help));
            options.add(getResources().getString(R.string.finding_aJob));
            options.add(getResources().getString(R.string.financial_help_orFood));
            options.add(getResources().getString(R.string.another_help));
            for (String helpMethod : options) {
                spnAdapter.add(helpMethod);
            }
        }
        private void findViews ()
        {
            cameraImageView = findViewById(R.id.signUp_camera_imageView);
            circleImageView = findViewById(R.id.signUp_circle_imageView);
            name = findViewById(R.id.signUp_Name_editText);
            phoneNumber = findViewById(R.id.signUp_PhoneNumber_editText);
            mail = findViewById(R.id.signUp_Email_editText);
            password = findViewById(R.id.signUp_Password_editText);
            spn = findViewById(R.id.signUp_spinner);
            locationButton = findViewById(R.id.signUp_location_rLayout);
            nextButton = findViewById(R.id.signUp_next_rLayout);
            locationNote = findViewById(R.id.signUp_locationNote_EditText);
            anotherHelpRLayout = findViewById(R.id.signUp_anotherHelp_rLayout);
            anotherHelpText = findViewById(R.id.signUp_anotherHelp_EditText);

        }

}
