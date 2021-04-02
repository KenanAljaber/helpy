package com.peoplehandstech.helpy;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PopupWindow extends Activity {

    private static Button yes,no;
    private static int width,height;
    private static TextView title;
    private static String text;
    private static boolean modify=false;
    private static boolean ready =false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_window);


        yes=findViewById(R.id.yes);
        no=findViewById(R.id.no);
        title=findViewById(R.id.title);




        // to get the exact size of the phone screen
        DisplayMetrics dm=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

         width=dm.widthPixels;
         height=dm.heightPixels;
// the number that we multiply with width and height represents the percentage of the screen ,
// so if we want the size of the window to cover 60% if the screen so we multiply both width and height with 0.6
        getWindow().setLayout((int)(width*.8),(int)(height*.2));
        if(modify){
            title.setText(text);
            System.out.println("WINDOW its true");
            ready =false;
        }

    }

    public void yesButton (View view)
    {
        if (!modify)
        {
            System.out.println("WINDOW its false");
            finish();
            System.exit(0);
        }else{
            NotificationsList.setUserAccepts(true);
            modify=false;
            ready =true;
            finish();

        }

    }
    public void noButton (View view)
    {
        ready =true;
        PopupWindow.this.finish();

    }

    public static String getText() {
        return text;
    }

    public static void setText(String text) {
        PopupWindow.text = text;
    }

    public static boolean isModify() {
        return modify;
    }

    public static void setModify(boolean modify) {
        PopupWindow.modify = modify;
    }

    public static boolean isReady() {
        return ready;
    }
}
