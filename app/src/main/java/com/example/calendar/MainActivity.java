package com.example.calendar;

import android.os.Bundle;
import android.widget.*;
import java.util.Date;
import java.util.ArrayList;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    ArrayList<Date> start = new ArrayList<>();
    ArrayList<Date> end = new ArrayList<>();
    long current;   //time slept in current session
    String display;
    void trimtimeline(int i){   //checks if sleep episode is within 24 hours from now
        Date yesterday = new Date(new Date().getTime() - 60 * 1000l);//24 * 3600
        if (end.get(i).getTime()<yesterday.getTime()) { //if sleep episode ended over 24 hours ago, discard it
            end.remove(i);
            start.remove(i);}
            else if (start.get(i).getTime()<yesterday.getTime())    //only account the latest (24hr) part of sleep episode
                start.set(i,yesterday);
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToggleButton button = findViewById(R.id.button);
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView tv = findViewById(R.id.textView);
                if (isChecked) {
                    start.add(java.util.Calendar.getInstance().getTime());
                    display = "Sleeping...";
                    tv.setText(display);
                } else {
                    end.add(java.util.Calendar.getInstance().getTime());
                    long total = 0;     //overall time slept
                    for (int i=0;i<end.size();i++){ //sum all sleep episodes in past 24 hours
                        trimtimeline(i);
                        total+=(end.get(i).getTime()- start.get(i).getTime());
                    }
                    display = "Slept "+total/1000+" sec in past minute"; //" hrs "+total/1000*60+" min";
                    tv.setText(display);
                }
            }
        });
        }
    }

