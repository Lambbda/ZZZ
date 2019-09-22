package com.example.calendar;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    String display;                             //TextView content
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
        SharedPreferences mSharedPreference1 =   PreferenceManager.getDefaultSharedPreferences(this);
        timeline.clear();
        int size = mSharedPreference1.getInt("Status_size", 0);

        for(int i=0;i<size;i++)
        {
            timeline.add(mSharedPreference1.getString("Status_" + i, null));
        }

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToggleButton button = findViewById(R.id.button);
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
                timeline.add(timeStamp);
                saveArray();
                loadArray();
                TextView tv = findViewById(R.id.textView);
                display = timeline.get(timeline.size()-1);
                tv.setText(display);
            }
        });
    }
}
