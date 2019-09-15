package com.example.calendar;

import android.os.Bundle;
import android.widget.*;
import java.util.Date;
import java.util.ArrayList;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    ArrayList<Date> start = new ArrayList<>();
    ArrayList<Date> end = new ArrayList<>();
    boolean isChecked;
    long total;
    long current;
    String display;
    Thread clock = new Thread(){
        public void run(){
            try {
                while(isChecked){
                    Date now = new Date();
                    current = now.getTime()-start.get(start.size()).getTime();
                    display = "Sleeping... "+current+" s";
                    clock.sleep(1000);
                }
            } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("How the fuck did this get interrupted", e);
        }
        }
    };
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
                    clock.start();
                } else {
                    clock.stop();
                    end.add(java.util.Calendar.getInstance().getTime());
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

