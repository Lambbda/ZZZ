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
                    for (int i=0;i<end.size();i++){
                        total+=(end.get(i).getTime()- start.get(i).getTime());
                    }
                    display = "Slept "+total/1000+" seconds for last 24 hrs";
                    tv.setText(display);
                }
            }
        });
        }
    }

