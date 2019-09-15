package com.example.calendar;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.widget.*;
import java.util.Date;
import java.util.ArrayList;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    ArrayList<Date> start = new ArrayList<>();  //stores times the button was pushed
    ArrayList<Date> end = new ArrayList<>();    //and released
    String display;                             //TextView content

    public boolean saveArray(ArrayList<Date> array)
    {
        SharedPreferences memory = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = memory.edit();
        editor.putInt("Array_size", array.size());

        for(int i=0;i<array.size();i++)
        {
            editor.remove("Array_" + i);
            editor.putString("Array_" + i, Long.toString(array.get(i).getTime()));
        }
        return editor.commit();
    }

    public void loadArray(ArrayList<Date> array)
    {
        SharedPreferences memory = PreferenceManager.getDefaultSharedPreferences(this);
        array.clear();
        int size = memory.getInt("Array_size", 0);

        for(int i=0;i<size;i++)
        {
            array.add(new Date(Long.parseLong(memory.getString("Array_" + i, null))));
        }
    }

    public void trimtimeline(int i){                                       //checks if given sleep episode is within 24 hours from now
        Date yesterday = new Date(new Date().getTime() - 24 * 3600 * 1000l);
        if (end.get(i).getTime()<yesterday.getTime()) {             //if sleep episode ended over 24 hours ago, discard it
            end.remove(i);
            saveArray(end);
            start.remove(i);
            saveArray(start);}
            else if (start.get(i).getTime()<yesterday.getTime()){    //trim episode to begin at least 24 hours from now
                start.set(i,yesterday);
                saveArray(start);
            }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToggleButton button = findViewById(R.id.button);
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView tv = findViewById(R.id.textView);
                if (isChecked) {
                    start.add(new Date());
                    display = "Sleeping...";
                    tv.setText(display);
                    saveArray(start);
                } else {
                    end.add(new Date());
                    saveArray(end);
                    long total = 0;                     //overall time slept
                    loadArray(start);
                    loadArray(end);
                    for (int i=0;i<end.size();i++){     //sums all sleep episodes in past 24 hours
                        trimtimeline(i);
                        total+=(end.get(i).getTime()- start.get(i).getTime());
                    }
                    display = total/(1000l*60*60)+" hrs "+total/(1000l*60)+" min"+total/1000l+" sec";
                    if (total>1000l*3600*24) display = "OVER 24 HOURS :o"; //workaround for trim bug
                    tv.setText(display);
                }
            }
        });
        }
    }

