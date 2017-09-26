package com.asus.cnsetupwizard;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;

import java.util.Calendar;

/**
 * Created by czy on 17-5-15.
 */

public class CNSetupWizardApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();
        setSysDate(2017,0,1);
    }

    public void setSysDate(int year,int month,int day){
        Calendar c = Calendar.getInstance();
        if(c.get(Calendar.YEAR) < 2017)
        {
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, day);

            long when = c.getTimeInMillis();

            if(when / 1000 < Integer.MAX_VALUE){
                ((AlarmManager)getSystemService(Context.ALARM_SERVICE)).setTime(when);
            }
        }

    }
}
