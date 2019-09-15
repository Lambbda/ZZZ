package com.example.calendar;

import android.os.Bundle;
import android.widget.*;
import java.time.*;
import java.util.Date;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Duration zzz;
    Date t_push, t_release;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToggleButton tbutton = (ToggleButton) findViewById(R.id.tbutton);
        tbutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    t_push=java.util.Calendar.getInstance().getTime();
                } else {
                    t_release=java.util.Calendar.getInstance().getTime();
                    
                }
            }
        });
        }
    }

