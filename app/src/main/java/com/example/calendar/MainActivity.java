package com.example.calendar;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.widget.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    String display,extra;                             //TextView content
    String dateformat = "yyyy.MM.dd.HH.mm.ss";
    List<String> timeline = new LinkedList();

    public boolean saveArray()
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor mEdit1 = sp.edit();
        mEdit1.putInt("Status_size", timeline.size());

        for(int i=0;i<timeline.size();i++)
        {
            mEdit1.remove("Status_" + i);
            mEdit1.putString("Status_" + i, timeline.get(i));
        }

        return mEdit1.commit();
    }

    public void loadArray()
    {
        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(this);
        timeline.clear();
        int size = mSharedPreference1.getInt("Status_size", 0);

        for(int i=0;i<size;i++)
        {
            timeline.add(mSharedPreference1.getString("Status_" + i, null));
        }

    }

    public void trimArray() //ensures dates aren't before 24 hours from now
    {
        boolean done = false;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.SECOND, -10); //use DATE -1 for finished product
        Date yesterday = cal.getTime();     //24 hours from now

        while(!done){
            Date push = new Date();
            Date pull = new Date();       //placeholders for timestamps from list

            if (timeline.size()>1&&timeline.get(1)!=null) try {
                push = new SimpleDateFormat(dateformat).parse(timeline.get(0));
                pull = new SimpleDateFormat(dateformat).parse(timeline.get(1));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (pull.getTime()<yesterday.getTime()){
                timeline.remove(0);
                timeline.remove(0);  //who needs loops tbh
            } else if (push.getTime()<yesterday.getTime()){
                timeline.set(0,new SimpleDateFormat(dateformat).format(yesterday));
                done = true;
            } else done = true;
        }

        extra = "limit \n" + new SimpleDateFormat(dateformat).format(yesterday);
        TextView tv2 = findViewById(R.id.textView2);
        tv2.setText(extra);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToggleButton button = findViewById(R.id.button);
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView tv = findViewById(R.id.textView);
                String timeStamp = new SimpleDateFormat(dateformat).format(new Date());
                loadArray();
                timeline.add(timeStamp);
                trimArray();
                saveArray();
                if (isChecked) {
                    tv.setText("Sleeping...");
                } else {
                    display = "first click \n"+timeline.get(0)+"\nlast click\n"+timeline.get(timeline.size()-1);
                    tv.setText(display);
                }
            }
        });
    }
}
