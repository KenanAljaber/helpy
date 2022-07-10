package com.peoplehandstech.helpy;

import android.content.Context;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

 public class CustomSpinner{

    private static ArrayList<String> options;

    public static  void fillOptions (ArrayAdapter<String> spnAdapter,Spinner spn,Context context)
    {
        options=new ArrayList<>();
        options.add(context.getResources().getString(R.string.select_a_way_toHelp));
        options.add(context.getResources().getString(R.string.couple_days_ofHosting));
        options.add(context.getResources().getString(R.string.translating));
        options.add(context.getResources().getString(R.string.official_docs_help));
        options.add(context.getResources().getString(R.string.finding_aJob));
        options.add(context.getResources().getString(R.string.financial_help_orFood));
        options.add(context.getResources().getString(R.string.another_help));
        for(String helpMethod:options)
        {
            spnAdapter.add(helpMethod);
        }
        spn.setAdapter(spnAdapter);
    }

     static void setItemFirst (String help,Spinner spn,ArrayAdapter<String> spnAdapter,Context context)
    {
        if(getAllSpinnerItems(spn)!=null)
        {

            List <String> spnItems=getAllSpinnerItems(spn);

            for(int i=0;i<spnItems.size();i++)
            {
                if(help.equals(spnItems.get(i)))
                {

                    spn.setSelection(i);
                    return;
                }
            }
            spn.setSelection(spn.getCount()-1);
            return;

        }
        else
        {
            fillOptions(spnAdapter,spn,context);
            for(int i=1;i<options.size();i++)
            {
                if(help.equals(options.get(i)))
                {
                    spn.setSelection(i);
                    return;
                }
            }

        }
    }

    private static List<String> getAllSpinnerItems (Spinner spn)
        {
            List<String> spinItems=new ArrayList<>();
            Adapter adapter=spn.getAdapter();
            int count=adapter.getCount();
            for(int i=0;i<count;i++)
            {
                String str=(String)adapter.getItem(i);
                spinItems.add(str);
            }
            if(spinItems.size()>0)
            {
                return spinItems;
            }
            else
                return null;
        }

}
