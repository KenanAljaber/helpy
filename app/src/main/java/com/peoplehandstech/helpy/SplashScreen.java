package com.peoplehandstech.helpy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.peoplehandstech.helpy.activities.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class SplashScreen extends AppCompatActivity {


    private static final int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

//        loadSecrets();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent homeIntetn = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(homeIntetn);
                finish();
            }
        }, SPLASH_TIME_OUT);

    }

    private void loadSecrets() {
        try {
            Properties properties = new Properties();
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open("secrets.properties");
            properties.load(inputStream);

            String googleApiKey=properties.getProperty("google_maps_key");
            String googleApiSecret = properties.getProperty("google_maps_secret");

            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = appInfo.metaData;
            metaData.putString("com.google.android.geo.API_KEY", "");
            Log.d("SPLASH_SCREEN","secret key loaded correctly "+googleApiSecret);
        } catch (IOException | PackageManager.NameNotFoundException | NullPointerException exception) {
            Log.d("SPLASH_SCREEN", "Could not find file: " + exception.toString());
        }
    }

}
