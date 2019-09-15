package com.example.calendar;

import android.os.Bundle;
import android.widget.*;
import java.util.Date;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Date t_push, t_release;
    long zzz;               //Difference between push and release in hours
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToggleButton tbutton = findViewById(R.id.button);
        tbutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    t_push=java.util.Calendar.getInstance().getTime();
                } else {
                    t_release=java.util.Calendar.getInstance().getTime();
                    zzz = (t_push.getTime() - t_release.getTime())/ (60 * 60 * 1000) % 24;
                    TextView tv = findViewById(R.id.textView);
                    String display = ""+zzz;
                    tv.setText(display);
                }
            }
        });
        }
    }

