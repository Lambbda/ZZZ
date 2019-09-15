package com.example.calendar;

import android.os.Bundle;
import android.widget.*;
import java.util.Date;
import java.util.ArrayList;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    ArrayList<Date> start = new ArrayList<>();
    ArrayList<Date> end = new ArrayList<>();
    long zzz;               //Difference between push and release in hours
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToggleButton button = findViewById(R.id.button);
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TextView tv = findViewById(R.id.textView);
                String display;
                if (isChecked) {
                    start.add(java.util.Calendar.getInstance().getTime());
                    display = "Sleeping...";
                    tv.setText(display);
                } else {
                    end.add(java.util.Calendar.getInstance().getTime());
                    for (int i=0;i<end.size();i++){
                        zzz+=(end.get(i).getTime()- start.get(i).getTime());
                    }
                    display = zzz/1000+" seconds";
                    tv.setText(display);
                }
            }
        });
        }
    }

